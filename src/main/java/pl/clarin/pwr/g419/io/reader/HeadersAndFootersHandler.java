package pl.clarin.pwr.g419.io.reader;

import lombok.extern.slf4j.Slf4j;
import pl.clarin.pwr.g419.struct.HeaderAndFooterStruct;
import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.struct.HocrPage;
import pl.clarin.pwr.g419.struct.Range;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class HeadersAndFootersHandler {

  private enum Type {HEADER, FOOTER}


  public void findAndExtractHeadersAndFooters(HocrDocument document) {
    List<HeaderAndFooterStruct> headers = findAndExtractLeveledHeaders(Type.HEADER, document);
    document.getDocContextInfo().setHeaders(headers);
    log.error("DID:" + document.getId() + " XXXXXXXXXXXX - printing headers XXXXXXXXXXX");
    headers.stream().forEach(h -> log.error(" " + h));

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


    List<HeaderAndFooterStruct> footers = findAndExtractLeveledHeaders(Type.FOOTER, document);
    document.getDocContextInfo().setFooters(footers);
    log.error("DID:" + document.getId() + " XXXXXXXXXXXX - printing footers XXXXXXXXXXX");
    footers.stream().forEach(h -> log.error(" " + h));

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
  }

  private List<HeaderAndFooterStruct> findAndExtractLeveledHeaders(Type type, HocrDocument document) {
    HeaderAndFooterStruct hafs = new HeaderAndFooterStruct();
    return findAndExtractLeveledHeaders(type, document, 0, 0, document.size(), hafs);
  }

  private List<HeaderAndFooterStruct> findAndExtractLeveledHeaders(Type type,
                                                                   HocrDocument document,
                                                                   int level,
                                                                   int sIndex,
                                                                   int eIndex,
                                                                   HeaderAndFooterStruct accumulator) {
    log.debug("Starting findAndExtractLeveledHEaders with: type=" + type + " level=" + level + " sIndex=" + sIndex + " eIndex=" + eIndex + " acc=" + accumulator);

    int HEADER_PAGE_SPAN_THRESHOLD = 3;
    int SIGNIFICANT_DIFF_IN_RANGE_HEIGHT = 10;
    List<HeaderAndFooterStruct> resultHeaderList = new LinkedList<>();

    //przewijaj do strony co ma w ogóle linię na tym poziomie by było od czego zacząć porównywać ...
    int currentIndex = sIndex;
    Range currentRange = null;
    while (
        (currentIndex < document.size())
            &&
            (currentRange = getRange4Level(type, document.get(currentIndex), level)) == null
    ) {
      currentIndex++;
    }
    if (currentIndex == document.size()) {
      return List.of(accumulator);
    }

    int startIndex = currentIndex;
    for (int pageIndex = startIndex + 1; pageIndex < eIndex; pageIndex++) {
      boolean continuingHeader;
      Range newRange = getRange4Level(type, document.get(pageIndex), level);
      if (newRange == null) {
        continuingHeader = false;
      } else {
        if (!currentRange.getText().equalsIgnoreCase(newRange.getText())) {
          //tekst się zmienił - jeśli wczesniej był na tylu stronach, że można zrobić nagłówek to go zrobimy
          continuingHeader = false;
        } else {
          if (currentRange.getHeight() - newRange.getHeight() > SIGNIFICANT_DIFF_IN_RANGE_HEIGHT) {
            continuingHeader = false;
          } else {
            continuingHeader = true;
          }
        }
      }


      if (!continuingHeader) {
        checkIfPossibleToMakeNewLevelHeaderAndMakeIt(startIndex, pageIndex, currentRange, HEADER_PAGE_SPAN_THRESHOLD, resultHeaderList, accumulator);
        currentRange = newRange;
        startIndex = pageIndex;

        while (currentRange == null) {
          if (startIndex == document.size() - 1)
            break;
          startIndex++;
          currentRange = getRange4Level(type, document.get(startIndex), level);
        }
      }
    }
    checkIfPossibleToMakeNewLevelHeaderAndMakeIt(startIndex, eIndex, currentRange, HEADER_PAGE_SPAN_THRESHOLD, resultHeaderList, accumulator);

    if (resultHeaderList.size() == 0) {
      return (level == 0) ? Collections.emptyList() : List.of(accumulator);
    }

    return
        resultHeaderList
            .stream()
            .flatMap(hl ->
                findAndExtractLeveledHeaders(type, document, level + 1, hl.getStartIndex(), hl.getEndIndex() + 1, hl).stream()
            )
            .collect(Collectors.toList());
  }

  private Range getRange4Level(Type type, HocrPage page, int level) {
    int levelForType = getLevelForType(type, page, level);
    List<Range> lines = page.getLines();
    if (lines == null)
      return null;
    if ((levelForType < 0) || (levelForType >= lines.size()))
      return null;
    return lines.get(levelForType);
  }


  private int getLevelForType(Type type, HocrPage page, int level) {
    return (type == Type.HEADER) ? level : page.getLines().size() - level - 1;
  }

  private void checkIfPossibleToMakeNewLevelHeaderAndMakeIt(int startIndex,
                                                            int pageIndex,
                                                            Range currentRange,
                                                            int PAGE_SPAN_THRESHOLD,
                                                            List<HeaderAndFooterStruct> result,
                                                            HeaderAndFooterStruct hafs) {

    if (currentRange != null) {
      if (pageIndex - 1 - startIndex >= PAGE_SPAN_THRESHOLD) {
        HeaderAndFooterStruct newHafs = new HeaderAndFooterStruct(hafs);
        newHafs.setStartIndex(startIndex);
        newHafs.setEndIndex(pageIndex - 1);
        // cały czas coś mi mówi, żeby tu lepiej jednak trzymać Range
        newHafs.getLines().add(currentRange.getText());

        result.add(newHafs);
      }
    }
  }

}
