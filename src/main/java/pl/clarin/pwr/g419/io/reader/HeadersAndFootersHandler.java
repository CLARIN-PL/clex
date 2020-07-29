package pl.clarin.pwr.g419.io.reader;

import lombok.extern.slf4j.Slf4j;
import pl.clarin.pwr.g419.struct.*;

import static pl.clarin.pwr.g419.struct.HeaderAndFooterStruct.Type.FOOTER;
import static pl.clarin.pwr.g419.struct.HeaderAndFooterStruct.Type.HEADER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class HeadersAndFootersHandler {

  // przez ile stron musi powtarzać się tekst wiersza byśmy uznali że to jest nagłówek
  final public static int PAGE_SPAN_THRESHOLD = 3;

  // jak wielka musi być różnica w wysokości linii z tym samym tekstem by uznać że to jednak nie takie same linie
  // zmiana z 10 na 15 -> doc. id 175091
  final public static int SIGNIFICANT_DIFF_IN_RANGE_HEIGHT = 15;

  // na jakiej części dokumentu musi mieścić się nagłówek (od góry) lub stopka (od dołu)
  // w zależności od orientacji strony - pozioma czy pionowa
  // zmiana z 1.0/8.0 na 1.0/1.10 -> doc. id 175091
  // ale dla .1.0/10.0 nie łapie całości stopki z doc. 175062 (tam jest jeszcze wklejony obrazek jako podstopka)
  // TODO - może rozbić granice na osobno dolną i górną i lepiej je dobierać
  final public static double FOCUS_ZONE_FRACTION_FOR_VERTICAL_PAGES = 1.0 / 10.0;
  final public static double FOCUS_ZONE_FRACTION_FOR_HORIZONTAL_PAGES = 1.0 / 4.0;  //???


  // przesunięcie numeracji stron dla tymczasowych stron wygenerowanych dla samych nagłówków
  final public static int TMP_PAGE_NR_OFFSET_FOR_HEADERS = 2000;

  // przesunięcie numeracji stron dla tymczasowych stron wygenerowanych dla samych stopek
  final public static int TMP_PAGE_NR_OFFSET_FOR_FOOTERS = 1000;


  public void findAndExtractHeadersAndFooters(HocrDocument document) {

    List<HeaderAndFooterStruct> headers = findAndExtractLeveledHeaders(HEADER, document);
    document.getDocContextInfo().setHeaders(headers);
    document.getDocContextInfo().sortHeaders();
    for (int tmpPageIndex = 0; tmpPageIndex < headers.size(); tmpPageIndex++) {
      headers.get(tmpPageIndex).generateTmpPageFromLines(document, TMP_PAGE_NR_OFFSET_FOR_HEADERS + 1 + tmpPageIndex);
    }
    // wycinanie ze stron linii z nagłówkami
    headers.stream()
        .forEach(h -> document
            .stream()
            .skip(h.getStartIndex())
            .limit(h.getEndIndex() - h.getStartIndex())
            .forEach(page -> {
                  page
                      .getLines()
                      .subList(0, h.getLines().size())
                      .clear();
                }
            )
        );

    // TODO - żeby wycinanie headerów i footerów się nie nałożyło przypadkowo na te same linie dla stron z małą ilością linii
    // trzeba by tu odtworzyć strony już z usuniętymi headerami i dopiero wtedy je puścić do wycinania footerów

    List<HeaderAndFooterStruct> footers = findAndExtractLeveledHeaders(FOOTER, document);
    document.getDocContextInfo().setFooters(footers);
    document.getDocContextInfo().sortFooters();
    for (int tmpPageIndex = 0; tmpPageIndex < footers.size(); tmpPageIndex++) {
      footers.get(tmpPageIndex).generateTmpPageFromLines(document, TMP_PAGE_NR_OFFSET_FOR_FOOTERS + 1 + tmpPageIndex);
    }
    // wycinanie ze stron linii ze stopkami
    footers.stream()
        .forEach(f -> document
            .stream()
            .skip(f.getStartIndex())
            .limit(f.getEndIndex() - f.getStartIndex())
            .forEach(page -> {
                  page
                      .getLines()
                      .subList(page.getLines().size() - f.getLines().size(), page.getLines().size())
                      .clear();
                }
            )
        );

    // diagnostyka
    log.debug("Doc:" + document.getId() + "XXXXXXXXXXXXXXX Printing headers XXXXXXXXXXXXXXX");
    document.getDocContextInfo().getHeaders().stream().forEach(h -> log.debug(" " + h));
    log.debug("Doc:" + document.getId() + "XXXXXXXXXXXXXXX Printing footers XXXXXXXXXXXXXXX");
    document.getDocContextInfo().getFooters().stream().forEach(h -> log.debug(" " + h));
  }

  private List<HeaderAndFooterStruct> findAndExtractLeveledHeaders(HeaderAndFooterStruct.Type type, HocrDocument document) {
    HeaderAndFooterStruct rootHafs = new HeaderAndFooterStruct();

    rootHafs.setStartIndex(0);
    rootHafs.setEndIndex(document.size() - 1);
    rootHafs.setType(type);
    rootHafs.setLevel(-1);

    findHeadersAndBuildHeadersTree(type, document, rootHafs);
    // no i teraz w rootHafs jest lista ewentualnych nagłówków podstawowego poziomu z doczepioną listą nagłówków następnego poziomu itd...

    log.debug("");
    log.debug("DUMPING " + type + "S TREE :");
    dumpHeadersTree(rootHafs);
    log.debug("");

    return linearizeEdgeOfHeadersTree(rootHafs);
  }

  /***
   *
   * @param type - sprawdzany typ: czy nagłówek (HEADER) czy stopka (FOOTER)
   * @param document  - referencja do całego dokumentu. Potrzebna do iterowania po stronach
   * @param previousLevelHeader- jeśli jesteśmy na level >0 to to jest struktura która zawiera dane o nagłówku z poprzedniego poziomu - znalezionym do tej pory
   *                           W środku tej struktury mamy:
   *                                level           - "poziom" nagłówka - technicznie ilość linii w liście lines dla tej konkretnej instancji nagłówka
   *                                startPageIndex  - od której strony włącznie zaczyna się ten nagłówek
   *                                endPageIndex    - do której strony włącznie (!) jest ten nagłówek
   * @return - listę struktur nagłówków/stopek takich, że na pewno na następnym poziomie (level) już nie można było wyodrędnić dalszej
   *            części nagłówka lub stopki. Gdy zmiana "nagłówka" jest co jedną stronę to te obszary nie są zwracane
   *
   */
  private void findHeadersAndBuildHeadersTree(HeaderAndFooterStruct.Type type,
                                              HocrDocument document,
                                              HeaderAndFooterStruct previousLevelHeader) {
//   level - "poziom" nagłówka - czy sprawdzamy pierwszą linię od góry, drugą od góry. Analogicznie dla stopki: ostatnią, przedostatnią
//   startPageIndex - indeks strony od której zaczynamy sprawdzanie.
//   endPageIndex - indeks strony następnej do tej, na której kończymy sprawdzanie. Czyli gdy chcemy sprawdzać do końca dokumentu dajemy document.size()

    int level = previousLevelHeader.getLevel() + 1;
    int startPageIndex = previousLevelHeader.getStartIndex();
    int endPageIndex = previousLevelHeader.getEndIndex() + 1;
    log.debug("Starting findAndExtractLeveledHeadersByTree with: type=" + type +
        " level=" + level +
        " startPageIndex=" + startPageIndex +
        " endPageIndex=" + endPageIndex +
        " prevLeaderHeader=" + previousLevelHeader);

    List<HeaderAndFooterStruct> resultHeaderList = new LinkedList<>();

    int currentPageIndex = startPageIndex;

    // struktura wskazująca czy natrafiliśmy na tekst w linii odpowiadającej poziomowi
    // tego nagłówka (wtedy startTextHocrLine != null) - czy też nie natrafiliśmy na taki tekst
    HocrLine startTextHocrLine = null;
    // indeks strony na której pierwszy raz (w danej iteracji) pojawiło się, że startTextHocrLine != null
    int startTextPageIndex = -1;

    boolean isInsideHeaderState = false;

    // główna pętla
    for (currentPageIndex = startPageIndex; currentPageIndex < endPageIndex; currentPageIndex++) {

      // znajdź najpierw stronę w której jest jakiś tekst w linii odpowiedniej dla tego level
      if (startTextHocrLine == null) {
        assert (!isInsideHeaderState);
        startTextHocrLine = getLineForLevel(type, document.get(currentPageIndex), level, previousLevelHeader);
        if (startTextHocrLine == null) continue;

        // znaleźliśmy linię - zapamiętujemy jej tekst i indeks do strony na której ją znaleźliśmy
        log.trace("cpi=" + currentPageIndex + " startTextHocrLine=" + startTextHocrLine.getText());
        startTextPageIndex = currentPageIndex;
        continue;
      }

      // skoro już mamy jedną znalezioną linię to sprawdzamy analogiczne linie na następnych stronach ...
      HocrLine currentHocrLine = getLineForLevel(type, document.get(currentPageIndex), level, previousLevelHeader);
      log.trace("cpi=" + currentPageIndex + " text=" + (currentHocrLine != null ? "'" + currentHocrLine.getText() + "'" : "---"));

      // ... najpierw obsługa "wyjątku" gdy natrafiliśmy na miejsce gdzie nie ma tekstu ...
      if (currentHocrLine == null) {
        if (isInsideHeaderState) {
          addChildHeaderToTree(startTextPageIndex, currentPageIndex - 1, previousLevelHeader, startTextHocrLine, resultHeaderList);
          isInsideHeaderState = false;
        }
        startTextHocrLine = null;
        startTextPageIndex = -1; // nadmiarowo bo i tak zostanie ustawiony przy zmienianiu startTextHocrLine na != null
        continue;
      }

      // ... obsuga standardowa: mamy w aktualnej linii tekst - możemy porównywać z linią, w której się zaczął pojawiać jakikolwiek tekst
      boolean areLinesTheSame = areLinesTheSame(startTextHocrLine, currentHocrLine);
      log.trace("\tareLinesTheSame=" + areLinesTheSame);
      if (areLinesTheSame) {
        isInsideHeaderState = true;
        assert (startTextHocrLine != null);
        continue;
      } else {
        if (!isInsideHeaderState) {
          // linie nie są te same i wczesniej też nie były
          // przewiń początek ewentualnego następnego nagłówka na aktualną pozycję:
          startTextPageIndex = currentPageIndex;
          startTextHocrLine = currentHocrLine;
          continue;
        } else {
          // linie nie są te same ale wcześniej były - tworzymy nagłówek z tego zbioru wcześniejszych takich samych linii
          addChildHeaderToTree(startTextPageIndex, currentPageIndex - 1, previousLevelHeader, startTextHocrLine, resultHeaderList);
          startTextPageIndex = currentPageIndex;
          startTextHocrLine = currentHocrLine;  // tekst do następnych porównywań już mamy tutaj ...
          isInsideHeaderState = false;
        }
      }
    }

    // wyszliśmy poza pętlę, ale może tam jeszczy był niedokończony Header ...
    if (isInsideHeaderState) {
      addChildHeaderToTree(startTextPageIndex, currentPageIndex - 1, previousLevelHeader, startTextHocrLine, resultHeaderList);
    }

    log.debug("\tresultHeaderList=" + resultHeaderList);
    // jeśli znaleźliśmy jakieś  wiersze-nagłówki na tym poziomie to próbujemy znaleźć dla nich podnagłówki ...
    resultHeaderList
        .stream()
        .forEach(hl ->
            findHeadersAndBuildHeadersTree(type, document, hl)
        );
  }

  private boolean areLinesTheSame(HocrLine firstHocrLine, HocrLine secondHocrLine) {
    boolean result = false;
    if (secondHocrLine == null) {
      result = false;
    } else {
      if (!firstHocrLine.getText().trim().equalsIgnoreCase(secondHocrLine.getText().trim())) {
        // TODO - może sprawdźmy jeszcze czy nie różni się tylko numerem strony (sporo przypadków)
        //tekst się zmienił - jeśli wczesniej był na tylu stronach, że można zrobić nagłówek to go zrobimy
        result = false;
      } else {
        if (firstHocrLine.getHeight() - secondHocrLine.getHeight() > SIGNIFICANT_DIFF_IN_RANGE_HEIGHT) {
          result = false;
        } else {
          // tekst jest dalej taki sam - kontynuacja nagłówka najpewniej
          result = true;
        }
      }
    }
    return result;
  }

  private HocrLine getLineForLevel(HeaderAndFooterStruct.Type type, HocrPage page, int level, HeaderAndFooterStruct prevLevelHeader) {
    int levelForType = getLevelForType(type, page, level);
    List<HocrLine> lines = page.getLines();
    if (lines == null)
      return null;
    if ((levelForType < 0) || (levelForType >= lines.size()))
      return null;

    HocrLine line = lines.get(levelForType);

    // dobra- mamy linię, ale czy ona czasem nie jest za daleko jak na nagłówek albo stopkę ?
    // sprawdzamy najpierw sam układ na stronie ...
    if (page.getOrientation() == Contour.Orientation.VERTICAL) {
      if ((type == HEADER) && (line.getTop() > page.getHeight() * FOCUS_ZONE_FRACTION_FOR_VERTICAL_PAGES))
        return null;
      if ((type == FOOTER) && (line.getBottom() < page.getHeight() * (1 - FOCUS_ZONE_FRACTION_FOR_VERTICAL_PAGES)))
        return null;
    } else {
      if ((type == HEADER) && (line.getTop() > page.getHeight() * FOCUS_ZONE_FRACTION_FOR_HORIZONTAL_PAGES))
        return null;
      if ((type == FOOTER) && (line.getBottom() < page.getHeight() * (1 - FOCUS_ZONE_FRACTION_FOR_HORIZONTAL_PAGES)))
        return null;
    }
    // TODO: może ta następna linia mieście się na tym obszarze ale i tak jest za daleko od juz znalezionych linii nagłówka/stopki
    //   jęsli to by się udało to można by pewnie zwiększyć zakres FOCUS_...

    return line;
  }


  private int getLevelForType(HeaderAndFooterStruct.Type type, HocrPage page, int level) {
    return (type == HEADER) ? level : page.getLines().size() - level - 1;
  }

  private void addChildHeaderToTree(int startPageIndex, int endPageIndex,
                                    HeaderAndFooterStruct previousLevelHeader,
                                    HocrLine startTextHocrLine,
                                    List<HeaderAndFooterStruct> resultHeaderList) {
    if (endPageIndex - startPageIndex + 1 >= PAGE_SPAN_THRESHOLD) { // jednak jeśli ten sam wiersz rozciąga się na za mało stron to nie bierzemy go pod uwagę
      HeaderAndFooterStruct newHafs = previousLevelHeader
          .createChild(startPageIndex, endPageIndex, startTextHocrLine);
      previousLevelHeader.getNextLevelHeaders().add(newHafs);
      resultHeaderList.add(newHafs);
    }
  }

  /***
   * Założenie jest że elementy w liście nextLevelHeaders są posortowane rosnąco (!)
   *
   * @param hafs
   * @return
   */
  private List<HeaderAndFooterStruct> linearizeEdgeOfHeadersTree(HeaderAndFooterStruct hafs) {
    String intend = "\t".repeat(hafs.getLevel() + 1);
    log.trace(intend + "START Linearizing : hafs=" + hafs);

    List<HeaderAndFooterStruct> result;

    if ((hafs.getLevel() == -1) && (hafs.getNextLevelHeaders().size() == 0)) {
      result = Collections.EMPTY_LIST;
    } else if (hafs.getNextLevelHeaders().size() == 0) {
      result = List.of(hafs);
    } else {
      // dla każdego node'a jeśli ma jakieś pod sobą to wymieniamy do na liniową "mieszaninę" jego i tych co ma pod sobą
      result = inMixNode(hafs);
    }

    log.trace(intend + "END-- Linearizing : result = " + result);
    return result;
  }

  private List<HeaderAndFooterStruct> inMixNode(HeaderAndFooterStruct hafs) {
    String intend = "\t".repeat(hafs.getLevel() + 1);
    log.trace(intend + "START InMix : hafs=" + hafs);

    List<HeaderAndFooterStruct> result = new ArrayList<>();
    int currentPageIndex = hafs.getStartIndex();
    for (HeaderAndFooterStruct nextLevelHafs : hafs.getNextLevelHeaders()) {
      if (currentPageIndex < nextLevelHafs.getStartIndex()) {
        if (hafs.getLevel() > -1) {
          // jednak te mini nagłówki też powinny być uwzględnione bo0 jednak u góry jest ekst rozpinający się na większą liczbę stron
          //if (nextLevelHafs.getStartIndex() - 1 - currentPageIndex + 1 >= PAGE_SPAN_THRESHOLD) {
          HeaderAndFooterStruct newHafs = new HeaderAndFooterStruct(hafs);
          // levelu nie zwiększamy bo to właściwie jest na poziomie właśnie poprzedniego levelu robione
          newHafs.setStartIndex(currentPageIndex);
          newHafs.setEndIndex(nextLevelHafs.getStartIndex() - 1);
          result.add(newHafs);
          //}
        }
      }
      result.addAll(linearizeEdgeOfHeadersTree(nextLevelHafs));
      currentPageIndex = nextLevelHafs.getEndIndex() + 1;
    }

    // może została jeszcze przestrzeń między ostatnim podnagłówkiem a końcem nagłówka
    if (currentPageIndex < hafs.getEndIndex()) {
      if (hafs.getLevel() > -1) {
        // jednak te mini nagłówki też powinny być uwzględnione bo0 jednak u góry jes tekst rozpinający się na większą liczbę stron
        //if (hafs.getEndIndex() - 1 - currentPageIndex + 1 >= PAGE_SPAN_THRESHOLD) {
        HeaderAndFooterStruct newHafs = new HeaderAndFooterStruct(hafs);
        // levelu nie zwiększamy bo to właściwie jest na poziomie właśnie poprzedniego levelu robione
        newHafs.setStartIndex(currentPageIndex);
        newHafs.setEndIndex(hafs.getEndIndex() - 1);
        result.add(newHafs);
        //}
      }
    }

    log.trace(intend + "END-- InMix: result =" + result);
    return result;
  }


  private void dumpHeadersTree(HeaderAndFooterStruct rootHafs) {
    HeaderAndFooterStruct currentHafs = rootHafs;
    String intend = "\t".repeat(currentHafs.getLevel() + 1);
    log.debug(intend + "" + currentHafs);
    currentHafs.getNextLevelHeaders().forEach(h -> dumpHeadersTree(h));
  }


}
