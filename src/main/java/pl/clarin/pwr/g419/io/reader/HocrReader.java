package pl.clarin.pwr.g419.io.reader;

import com.google.common.collect.Maps;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import pl.clarin.pwr.g419.struct.*;
import pl.clarin.pwr.g419.utils.BboxUtils;

@Slf4j
public class HocrReader extends DefaultHandler {

  private StringBuilder value = new StringBuilder();

  private static final String TAG_SPAN = "span";
  private static final String TAG_DIV = "div";
  private static final String ATTR_CLASS = "class";
  private static final String ATTR_CLASS_LINE = "ocrx_line";
  private static final String ATTR_CLASS_WORD = "ocrx_word";
  private static final String ATTR_CLASS_PAGE = "ocr_page";
  private static final String ATTR_TITLE = "title";

  private static final Pattern hocrPageMarker = Pattern.compile("- Page [#][0-9]+\n");
  private static final Pattern hocrConvertMarker = Pattern.compile("Converting [^:]+[:]\n");

  private int bboxNo = 1;
  private int pageNo = 1;
  private HocrDocument document = null;
  private HocrPage page = null;
  private Box lastBox = null;
  private boolean lineNew = false;
  private Optional<Bbox> lastBbox = Optional.empty();
  private final Stack<String> spanClassStack = new Stack<>();

  Map<String, String> encodingFix = Maps.newHashMap();

  public HocrDocument parseAndSortBboxes(final Path path)
      throws IOException, SAXException, ParserConfigurationException {

    parse(path);
    this.document.stream().forEach(p -> eliminateRedundantLines(p));
    this.document = regenerateDocumentFromLines(this.document);
    this.document.stream().forEach(this::mergeLinesSecondteration);
    // teraz w dokumencie kolejność Bboxów jest zgodna z kolejnością posortowanych linii
    new HeadersAndFootersHandler().findAndExtractHeadersAndFooters(document);
    this.document = regenerateDocumentFromLines(this.document);
    this.document.stream().forEach(this::mergeLinesSecondteration);

    return document;
  }

  public HocrDocument parse(final Path path)
      throws IOException, SAXException, ParserConfigurationException {

    readDocument(path);

    this.document.setId(getIdFromPath(path));

    this.document.stream().forEach(this::mergeLinesFirstIteration);
    //this.document.stream().forEach(HocrPage::dumpNrOfLinesAndBlocks);
    //this.document.trimLeadingEmptyPages();
    this.document.calculateNrOfLeadingEmptyPages();
    this.document.stream().forEach(this::splitInterpunctionEnd);
    this.document.stream().forEach(HocrPage::sortLinesByTop);

    // i teraz w polu lines w każdej stronie mamy linie tekstu wg. kolejności występowania
    // na stronie
    return document;
  }

  public void readDocument(final Path path)
      throws IOException, SAXException, ParserConfigurationException {

    //log.info(" Reading :" + path.toString());

    encodingFix.put("Ã\u0093", "Ó");
    encodingFix.put("Ä\u0085", "ą");
    encodingFix.put("Å\u0081", "Ł");
    encodingFix.put("Å\u0082", "ł");
    encodingFix.put("Å\u0084", "ń");
    encodingFix.put("Å\u009A", "Ś");
    encodingFix.put("Ŝ", "ż");

    final String text = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    final SAXParserFactory factory = SAXParserFactory.newInstance();
    final SAXParser parser = factory.newSAXParser();
    final String textWithoutMarkers = cleanHocr(text);
    final InputStream is = new ByteArrayInputStream(textWithoutMarkers.getBytes());
    document = new HocrDocument();
    parser.parse(is, this);

  }


  public static String getIdFromPath(final Path path) {
    return path.getParent().toFile().getName();
  }

  private static String cleanHocr(final String text) {
    String clean = text;
    clean = hocrPageMarker.matcher(clean).replaceAll("");
    clean = hocrConvertMarker.matcher(clean).replaceAll("");
    return clean;
  }

  @Override
  public InputSource resolveEntity(final String publicId, final String systemId) {
    return new InputSource(new StringReader(""));
  }

  @Override
  public void startElement(final String s, final String s1, final String elementName,
                           final Attributes attributes) {
    value = new StringBuilder();
    final String attrClass = attributes.getValue(ATTR_CLASS);
    if (elementName.equalsIgnoreCase(TAG_SPAN)) {
      if (ATTR_CLASS_WORD.equals(attrClass)) {
        final String title = attributes.getValue(ATTR_TITLE);
        lastBox = titleToBox(title);
      } else if (ATTR_CLASS_LINE.equals(attrClass)) {
        lastBbox.ifPresent(b -> b.setLineEnd(true));
        lineNew = true;
      }
      spanClassStack.push(attrClass);
    } else if (elementName.equalsIgnoreCase(TAG_DIV)) {
      if (ATTR_CLASS_PAGE.equals(attrClass)) {
        this.page = new HocrPage(document);
        final String title = attributes.getValue(ATTR_TITLE);
        final Box box = titleToBox(title);
        this.page.setBox(box);
        this.page.setNo(pageNo++);
        //log.trace(" For page " + pageNo + " set Box =" + this.page.getBox());
        document.add(page);
      }
    }
    //lastClass = attributes.getValue(ATTR_CLASS);
  }

  static private Box titleToBox(final String title) throws NumberFormatException {
    final String[] cols = title.split(" ");
    return new Box(Integer.parseInt(cols[1]),
        Integer.parseInt(cols[2]),
        Integer.parseInt(cols[3]),
        Integer.parseInt(cols[4]));
  }

  public String fixEncoding(final String text) {
    String textFixed = text;
    for (final Map.Entry<String, String> entry : this.encodingFix.entrySet()) {
      textFixed = textFixed.replaceAll(entry.getKey(), entry.getValue());
    }
    return textFixed;
  }

  @Override
  public void endElement(final String uri,
                         final String localName,
                         final String elementName) {
    if (elementName.equalsIgnoreCase(TAG_SPAN)) {
      final String spanClass = spanClassStack.pop();
      if (ATTR_CLASS_WORD.equals(spanClass)) {
        lastBbox = Optional.of(new Bbox(bboxNo++, fixEncoding(value.toString()), lastBox));
        if (lineNew) {
          lastBbox.get().setLineBegin(true);
          lineNew = false;
        }
        page.add(lastBbox.get());
      } else if (ATTR_CLASS_LINE.equals(spanClass)) {
        lastBbox.ifPresent(b -> b.setLineEnd(true));
      }
    }
  }

  @Override
  public void characters(final char[] ac, final int start, final int length) {
    for (int i = start; i < start + length; i++) {
      value.append(ac[i]);
    }
  }

  private void mergeLinesFirstIteration(final HocrPage page) {
    mergeLines(page, 1);
  }

  private void mergeLinesSecondteration(final HocrPage page) {
    mergeLines(page, 2);
  }

  private void mergeLines(final HocrPage page, final int iteration) {
    final List<HocrLine> hocrLines = BboxUtils.createLines(page);
    if (iteration == 1) {
      page.setNumberOfOriginalLines(hocrLines.size());
    }

    int blocksCounter = 0;
    int inlineBlocksCounter = 0;
    final Set<Integer> mergedRangesIndexesToSkipInResult = new HashSet<>();
    for (int i = 0; i < hocrLines.size() - 1; i++) {
      final HocrLine r1 = hocrLines.get(i);
      final HocrLine r2 = hocrLines.get(i + 1);
      final Bbox bbox = page.get(r1.getLastBoxInRangeIndex());
      final Bbox nextBbox = page.get(r1.getLastBoxInRangeIndex() + 1);
      if ((r1.overlap(r2) > 0.8 || r1.within(r2) > 0.8 || r2.within(r1) > 0.8)
          && bbox.getRight() <= nextBbox.getLeft()) {
        bbox.setLineEnd(false);
        bbox.setBlockEnd(true);
        blocksCounter++;
        inlineBlocksCounter++;
        nextBbox.setLineBegin(false);
        r2.setFirstBoxInRangeIndex(r1.getFirstBoxInRangeIndex());
        mergedRangesIndexesToSkipInResult.add(i);
      } else {
        // End of line also indicates end of block
        bbox.setBlockEnd(true);
        blocksCounter++;
      }
    }
    if (page.size() > 0) {
      page.get(page.size() - 1).setLineEnd(true);
      page.get(page.size() - 1).setBlockEnd(true);
      blocksCounter++;
    }

    final List<HocrLine> mergedLines = new LinkedList<>();
    for (int i = 0; i < hocrLines.size(); i++) {
      if (!mergedRangesIndexesToSkipInResult.contains(i)) {
        mergedLines.add(hocrLines.get(i));
      }
    }

    if (iteration == 1) {
      page.setNumberOfOriginalBlocks(blocksCounter);
      page.setNumberOfOriginalInlineBlocks(inlineBlocksCounter);
    }

    page.setLines(mergedLines);

  }


  private void eliminateRedundantLines(final HocrPage page) {
    for (int i = page.getLines().size() - 1; i > 0; i--) {
      final HocrLine current = page.getLines().get(i);
      final HocrLine previous = page.getLines().get(i - 1);

      if ((current.overlapY(previous) > 0.8) && ((current.overlapX(previous) > 0.2))) {
        if (current.getText().contains(previous.getText())) {
          page.getLines().remove(i - 1);
        } else if (previous.getText().contains(current.getText())) {
          page.getLines().remove(i);
        }
      }
    }
  }


  private void splitInterpunctionEnd(final HocrPage page) {
    final Pattern p = Pattern.compile("^(.*(\\p{L}|\\p{N}))([).,:-])$");
    // tutaj trzymane są indeksy BBoxów które zostały rozdzielone.Te indeksy są wg numeracji *przed* rodzieleniem
    final List<Integer> splitInterpunctionIndexes = new ArrayList<>();
    for (int i = page.size() - 1; i >= 0; i--) {
      final Bbox bbox = page.get(i);
      final Matcher m = p.matcher(bbox.getText());
      if (m.matches()) {
        final String headText = m.group(1);
        final String tailText = m.group(3);
        final int headWidth = bbox.getBox().getWidth() * headText.length() / bbox.getText().length();
        final int tailWidth = bbox.getBox().getWidth() - headWidth;

        bbox.getBox().setRight(bbox.getBox().getLeft() + headWidth);
        bbox.setText(headText);

        final Box tailBox = new Box(bbox.getBox().getRight(),
            bbox.getBox().getTop(),
            bbox.getBox().getRight() + tailWidth,
            bbox.getBox().getBottom());
        final Bbox tailBbox = new Bbox(bbox.getNo(), tailText, tailBox);
        tailBbox.setLineEnd(bbox.isLineEnd());
        tailBbox.setBlockEnd(bbox.isBlockEnd());
        page.add(i + 1, tailBbox);
        splitInterpunctionIndexes.add(i);

        bbox.setBlockEnd(false);
        bbox.setLineEnd(false);
      }
    }

    // jeśli było coś rozdzielane to trzeba skorygować w każdej dotkniętą zmianą linii
    // wskażniki do startującego ją i kończącego ją BBoxa
    if (splitInterpunctionIndexes.size() > 0) {
      correctLinesInPageAfterSplitInterpunction(page, splitInterpunctionIndexes);
    }

  }

  private void correctLinesInPageAfterSplitInterpunction(final HocrPage page, final List<Integer> splitInterpunctionIndexes) {

    if (splitInterpunctionIndexes.size() == 0) {
      return;
    }

    Collections.reverse(splitInterpunctionIndexes);

    int offset = 0;
    int splitIndexNr = 0;
    int currentSpliInterpunctionIndex = splitInterpunctionIndexes.get(splitIndexNr);
    boolean allSplitInterpunctionIndexesUsed = false;

    for (int lineNr = 0; lineNr < page.getLines().size(); lineNr++) {
      final HocrLine line = page.getLines().get(lineNr);
      if (offset != 0) {
        line.setFirstBoxInRangeIndex(line.getFirstBoxInRangeIndex() + offset);
        line.setLastBoxInRangeIndex(line.getLastBoxInRangeIndex() + offset);
      }

      while ((!allSplitInterpunctionIndexesUsed)
          && (line.containsBboxWithIndex(currentSpliInterpunctionIndex))) {

        offset++;

        if (splitIndexNr < splitInterpunctionIndexes.size() - 1) {
          splitIndexNr++;
          currentSpliInterpunctionIndex = splitInterpunctionIndexes.get(splitIndexNr) + offset;
        } else {
          allSplitInterpunctionIndexesUsed = true;
        }
        line.setLastBoxInRangeIndex(line.getLastBoxInRangeIndex() + 1);
      }
    }

  }


  private HocrDocument regenerateDocumentFromLines(final HocrDocument doc) {
    final HocrDocument resultDoc = new HocrDocument();
    resultDoc.setDocContextInfo(doc.getDocContextInfo());
    resultDoc.setId(doc.getId());

    final List<HocrPage> pages = new ArrayList<>();
    for (int i = 0; i < document.size(); i++) {
      final HocrPage page = document.get(i);
      final HocrPage newPage = new HocrPage(resultDoc, page.generateBboxesFromSortedLines());
      newPage.setBox(page.getBox());
      newPage.setNo(page.getNo());
      pages.add(newPage);
      // histogram i annotacje jeszcze nie wygenerowane
    }
    resultDoc.addAll(pages);

//    resultDoc.stream().forEach(this::mergeLinesSecondteration);
//    doc.stream().forEach(this::splitInterpunctionEnd);
//    doc.stream().forEach(HocrPage::sortLinesByTop);

    return resultDoc;
  }


  private void splitInterpunctionBegin(final HocrPage page) {
    final Pattern p = Pattern.compile("^([(.,:-])(.*(\\p{L}|\\p{N}))$");
    for (int i = page.size() - 1; i >= 0; i--) {
      final Bbox bbox = page.get(i);
      final Matcher m = p.matcher(bbox.getText());
      if (m.matches()) {
        final String head = m.group(1);
        final String tail = m.group(3);
        final int headWidth = bbox.getBox().getWidth() * head.length() / bbox.getText().length();
        final int tailWidth = bbox.getBox().getWidth() - headWidth;

        bbox.getBox().setLeft(bbox.getBox().getLeft() + headWidth);
        bbox.getBox().setRight(bbox.getBox().getLeft() + tailWidth);
        bbox.setText(tail);

        final Box headBox = new Box(bbox.getBox().getRight(), bbox.getBox().getRight() + headWidth,
            bbox.getBox().getTop(), bbox.getBox().getBottom());
        final Bbox headBbox = new Bbox(bbox.getNo(), head, headBox);
        headBbox.setLineBegin(bbox.isLineBegin());
        page.add(i, headBbox);

        bbox.setLineBegin(false);
      }
    }
  }


}
