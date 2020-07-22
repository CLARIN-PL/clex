package pl.clarin.pwr.g419.io.reader;

import lombok.extern.slf4j.Slf4j;
import pl.clarin.pwr.g419.struct.HeaderAndFooterStruct;

import static pl.clarin.pwr.g419.struct.HeaderAndFooterStruct.Type.FOOTER;
import static pl.clarin.pwr.g419.struct.HeaderAndFooterStruct.Type.HEADER;

import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.struct.HocrLine;
import pl.clarin.pwr.g419.struct.HocrPage;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class HeadersAndFootersHandler {

  // przez ile stron musi powtarzać się linia byśmy uznali że to jest nagłówek
  final public static int PAGE_SPAN_THRESHOLD = 3;

  // jak wielka musi być różnica w wysokości linii z tym samym tekstem by uznać że to jednak nie takie same linie
  final public static int SIGNIFICANT_DIFF_IN_RANGE_HEIGHT = 10;


  public void findAndExtractHeadersAndFooters(HocrDocument document) {
    List<HeaderAndFooterStruct> headers = findAndExtractLeveledHeaders(HEADER, document);
    document.getDocContextInfo().setHeaders(headers);
    headers.stream().forEach(h -> h.generateTmpPageFromLines());
    // wycinanie ze stron linii z nagłówkami
    headers.stream()
        .forEach(h -> document
            .stream()
            .skip(h.getStartIndex())
            .limit(h.getEndIndex() - h.getStartIndex())
            .forEach(page -> {
                  log.debug("H proc page " + page.getNo());
                  page
                      .getLines()
                      .subList(0, h.getLines().size())
                      .clear();
                }
            )
        );


    List<HeaderAndFooterStruct> footers = findAndExtractLeveledHeaders(FOOTER, document);
    document.getDocContextInfo().setFooters(footers);
    footers.stream().forEach(f -> f.generateTmpPageFromLines());
    // wycinanie ze stron linii ze stopkami
    footers.stream()
        .forEach(f -> document
            .stream()
            .skip(f.getStartIndex())
            .limit(f.getEndIndex() - f.getStartIndex())
            .forEach(page -> {
                  log.debug("F proc page " + page.getNo());
                  page
                      .getLines()
                      .subList(page.getLines().size() - f.getLines().size(), page.getLines().size())
                      .clear();
                }
            )
        );

    // diagnostyka
    log.debug("DID:" + document.getId() + " XXXXXXXXXXXX - printing headers XXXXXXXXXXX");
    headers.stream().forEach(h -> log.debug(" " + h));

    log.debug("DID:" + document.getId() + " XXXXXXXXXXXX - printing footers XXXXXXXXXXX");
    footers.stream().forEach(h -> log.debug(" " + h));
  }

  private List<HeaderAndFooterStruct> findAndExtractLeveledHeaders(HeaderAndFooterStruct.Type type, HocrDocument document) {
    HeaderAndFooterStruct hafs = new HeaderAndFooterStruct();
    hafs.setType(type);
    return findAndExtractLeveledHeaders(type, document, 0, 0, document.size(), hafs);
  }

  /***
   *
   * @param type - sprawdzany typ: czy nagłówek (HEADER) czy stopka (FOOTER)
   * @param document  - referencja do całego dokumentu. Potrzebna do iterowania po stronach
   * @param level - "poziom" nagłówka - czy sprawdzamy pierwszą linię od góry, drugą od góry. Analogicznie dla stopki: ostatnią, przedostatnią
   * @param startPageIndex - indeks strony od której zaczynamy sprawdzanie.
   * @param endPageIndex - indeks strony następnej do tej, na której kończymy sprawdzanie. Czyli gdy chcemy sprawdzać do końca dokumentu dajemy document.size()
   * @param previousLevelHeader- jeśli jesteśmy na level >0 to to jest struktura która zawiera dane o nagłówku z poprzedniego poziomu - znalezionym do tej pory
   * @return - listę struktur nagłówków/stopek takich, że na pewno na następnym poziomie (level) już nie można było wyodrędnić dalszej
   *            części nagłówka lub stopki
   *
   */
  private List<HeaderAndFooterStruct> findAndExtractLeveledHeaders(HeaderAndFooterStruct.Type type,
                                                                   HocrDocument document,
                                                                   int level,
                                                                   int startPageIndex,
                                                                   int endPageIndex,
                                                                   HeaderAndFooterStruct previousLevelHeader) {
    log.debug("Starting findAndExtractLeveledHeaders with: type=" + type +
        " level=" + level +
        " startPageIndex=" + startPageIndex +
        " endPageIndex=" + endPageIndex +
        " prevLeaderHeader=" + previousLevelHeader);

    List<HeaderAndFooterStruct> resultHeaderList = new LinkedList<>();
    List<HeaderAndFooterStruct> finalResultHeaderList = new LinkedList<>();

    // jeśli nie ma w ogóle na stronie linii takich jak trzeba to przewijaj do strony co ma
    // w ogóle linię na tym poziomie by było od czego zacząć porównywać ...
    int currentPageIndex = startPageIndex;
    HocrLine startHeaderHocrLine = null;
    while (
        (currentPageIndex < endPageIndex)
            &&
            (startHeaderHocrLine = getLineForLevel(type, document.get(currentPageIndex), level)) == null
    ) {
      currentPageIndex++;
    }
    if (currentPageIndex == endPageIndex) { // jesli przewinęlismy w ten sposób do końca to po prostu zwracamy nagłówek poprzedniego poziomu
      return List.of(previousLevelHeader);
    }

    int previousHeaderEndPageIndex = startPageIndex - 1;
    boolean isIndexInHeaderState = false;
    int startHeaderIndex = currentPageIndex;

    // główna pętla - startujemy od startHeaderIncdex+1 bo sam startHeaderIndex mamy już sprawdzony i trzymany w startHeaderHocrLine
    for (currentPageIndex = startHeaderIndex + 1; currentPageIndex < endPageIndex; currentPageIndex++) {

      HocrLine currentHocrLine = getLineForLevel(type, document.get(currentPageIndex), level);
      boolean areLinesTheSame = areLinesTheSame(startHeaderHocrLine, currentHocrLine);

      if (!areLinesTheSame) {
        boolean isHeaderCreated = checkIfPossibleToMakeNewLevelHeaderAndMakeIt(startHeaderIndex, currentPageIndex, startHeaderHocrLine, resultHeaderList, previousLevelHeader);
        if (isHeaderCreated) {
          // stworzyliśmy nowy nagłówek ale być może między początkiem nowego a końcem poprzedniego, który tu na tym poziomie
          // też stworzyliśmy jest przerwa i trzeba ją uwzględnić jeśli to jest level >0 bo wtedy trzeba zwrócić ten nagłówek
          // z "niższego" poziomu przykrojony do tej przerwy

          if (level > 0) {
            if (startHeaderIndex - previousHeaderEndPageIndex >= 2) {
              // tak - jest taka przerwa na przynajmniej jedną stronę
              //   - nie sprawdzamy PAGE_SPAN_THRESHOLD bo z niższego poziomu wiemy że to nagłówej na pewno
              HeaderAndFooterStruct newHafs = new HeaderAndFooterStruct(previousLevelHeader);
              newHafs.setStartIndex(previousHeaderEndPageIndex + 1);
              newHafs.setEndIndex(startHeaderIndex - 1);
              // linie zostają takie jak były na poziomie "niżej"

              // ten nagłówek właśnie sprawdziliśmy że na tym poziomie już nic nie ma do niego
              // więc nie powinnismy go sprawdzać już na ewentualne powtórki na jeszcze następnym poziomie - czyli go zwracamy od razu taki jaki jest
              finalResultHeaderList.add(newHafs);
            }
          }


          previousHeaderEndPageIndex = currentPageIndex - 1;
        }
        startHeaderHocrLine = currentHocrLine;
        startHeaderIndex = currentPageIndex;

        while (startHeaderHocrLine == null) {
          if (startHeaderIndex == endPageIndex - 1) {
//          if (startHeaderIndex == endPageIndex) {
            log.debug("startHeaderIndex == endPageIndex");
            if (level > 0) {
              if (startHeaderIndex - previousHeaderEndPageIndex >= 2) {
                // tak - jest taka przerwa na przynajmniej jedną stronę
                //   - nie sprawdzamy PAGE_SPAN_THRESHOLD bo z niższego poziomu wiemy że to nagłówej na pewno
                HeaderAndFooterStruct newHafs = new HeaderAndFooterStruct(previousLevelHeader);
                newHafs.setStartIndex(previousHeaderEndPageIndex + 1);
                newHafs.setEndIndex(startHeaderIndex - 1);
                // linie zostają takie jak były na poziomie "niżej"

                // ten nagłówek właśnie sprawdziliśmy że na tym poziomie już nic nie ma do niego
                // więc nie powinnismy go sprawdzać już na ewentualne powtórki na jeszcze następnym poziomie - czyli go zwracamy od razu taki jaki jest
                finalResultHeaderList.add(newHafs);
              }
            }

            break;
          }
          startHeaderIndex++;
          startHeaderHocrLine = getLineForLevel(type, document.get(startHeaderIndex), level);
        }
      }


    }
    if (startHeaderHocrLine != null) {
      boolean isHeaderCreated = checkIfPossibleToMakeNewLevelHeaderAndMakeIt(startHeaderIndex, endPageIndex, startHeaderHocrLine, resultHeaderList, previousLevelHeader);
      if (!isHeaderCreated) {
        if (level > 0) {
          if (startHeaderIndex - previousHeaderEndPageIndex >= 2) {
            // tak - jest taka przerwa na przynajmniej jedną stronę
            //   - nie sprawdzamy PAGE_SPAN_THRESHOLD bo z niższego poziomu wiemy że to nagłówej na pewno
            HeaderAndFooterStruct newHafs = new HeaderAndFooterStruct(previousLevelHeader);
            newHafs.setStartIndex(previousHeaderEndPageIndex + 1);
            newHafs.setEndIndex(startHeaderIndex - 1);
            // linie zostają takie jak były na poziomie "niżej"

            // ten nagłówek właśnie sprawdziliśmy że na tym poziomie już nic nie ma do niego
            // więc nie powinnismy go sprawdzać już na ewentualne powtórki na jeszcze następnym poziomie - czyli go zwracamy od razu taki jaki jest
            finalResultHeaderList.add(newHafs);
          }
        }
      }
    } else {
      log.debug("startHeaderHocrLine != null");
    }

    if (resultHeaderList.size() == 0) {
      return (level == 0) ? Collections.emptyList() : List.of(previousLevelHeader);
    }

    log.debug("\tfinalResultHeaderList=" + finalResultHeaderList);
    log.debug("\tresultHeaderList=" + resultHeaderList);

    finalResultHeaderList.addAll(
        resultHeaderList
            .stream()
            .flatMap(hl ->
                findAndExtractLeveledHeaders(type, document, level + 1, hl.getStartIndex(), hl.getEndIndex() + 1, hl).stream()
            )
            .collect(Collectors.toList())
    );


    return finalResultHeaderList;
  }

  private boolean areLinesTheSame(HocrLine firstHocrLine, HocrLine secondHocrLine) {
    boolean result = false;
    if (secondHocrLine == null) {
      result = false;
    } else {
      if (!firstHocrLine.getText().equalsIgnoreCase(secondHocrLine.getText())) {
        //tekst się zmienił - jeśli wczesniej był na tylu stronach, że można zrobić nagłówek to go zrobimy
        result = false;
      } else {
        if (firstHocrLine.getHeight() - secondHocrLine.getHeight() > SIGNIFICANT_DIFF_IN_RANGE_HEIGHT) {
          result = false;
          // może jeszcze sprawdzanie czy header lub footer jest na "swojej" połowie dokumentu ???
        } else {
          // tekst jest dalej taki sam - kontynuacja nagłówka najpewniej
          result = true;
        }
      }
    }
    return result;
  }

  private HocrLine getLineForLevel(HeaderAndFooterStruct.Type type, HocrPage page, int level) {
    int levelForType = getLevelForType(type, page, level);
    List<HocrLine> lines = page.getLines();
    if (lines == null)
      return null;
    if ((levelForType < 0) || (levelForType >= lines.size()))
      return null;
    return lines.get(levelForType);
  }


  private int getLevelForType(HeaderAndFooterStruct.Type type, HocrPage page, int level) {
    return (type == HEADER) ? level : page.getLines().size() - level - 1;
  }

  private boolean checkIfPossibleToMakeNewLevelHeaderAndMakeIt(int startPageIndex,
                                                               int currentPageIndex,
                                                               HocrLine startHeaderHocrLine,
                                                               List<HeaderAndFooterStruct> result,
                                                               HeaderAndFooterStruct upLevelHeader) {

    //if (startHeaderHocrLine != null) {
    if (currentPageIndex - startPageIndex >= PAGE_SPAN_THRESHOLD) {
      HeaderAndFooterStruct newHafs = new HeaderAndFooterStruct(upLevelHeader);
      newHafs.setStartIndex(startPageIndex);
      newHafs.setEndIndex(currentPageIndex - 1);
      newHafs.getLines().add(startHeaderHocrLine);

      result.add(newHafs);
      return true;
    }
    //}
    return false;
  }

}
