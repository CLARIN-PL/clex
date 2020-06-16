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
import lombok.extern.java.Log;
import org.apache.commons.lang3.tuple.Pair;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import pl.clarin.pwr.g419.struct.*;
import pl.clarin.pwr.g419.utils.BboxUtils;

@Log
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

  public HocrDocument parse(final Path path)
      throws IOException, SAXException, ParserConfigurationException {

    readDocument(path);

    this.document.setId(getIdFromPath(path));
    this.document.stream().forEach(this::mergeLines);
    this.document.stream().forEach(this::splitInterpunctionEnd);
    this.document.stream().forEach(HocrPage::sortLinesByTop);

    // i teraz w polu lines w każdej stronie mamy linie tekstu wg. kolejności występowania
    // na stronie


    return document;
  }

  public void readDocument(final Path path)
      throws IOException, SAXException, ParserConfigurationException {

    log.info(" Reading :" + path.toString());

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


  private static String getIdFromPath(final Path path) {
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
        this.page = new HocrPage();
        this.page.setNo(pageNo++);
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

  private void mergeLines(final HocrPage page) {
    final List<Pair<Range, Integer>> ranges = BboxUtils.createLines(page);
    final Set<Integer> mergedRangesIndexesToSkipInResult = new HashSet<>();
    for (int i = 0; i < ranges.size() - 1; i++) {
      final Pair<Range, Integer> range = ranges.get(i);
      final Pair<Range, Integer> nextRange = ranges.get(i + 1);
      final Bbox bbox = page.get(range.getRight());
      final Bbox nextBbox = page.get(range.getRight() + 1);
      final Range r1 = range.getKey();
      final Range r2 = nextRange.getKey();
      if ((r1.overlap(r2) > 0.8 || r1.within(r2) > 0.8 || r2.within(r1) > 0.8)
          && bbox.getBox().getRight() <= nextBbox.getBox().getLeft()) {
        bbox.setLineEnd(false);
        bbox.setBlockEnd(true);
        nextBbox.setLineBegin(false);
        r2.setFirstBoxInRangeIndex(r1.getFirstBoxInRangeIndex());
        mergedRangesIndexesToSkipInResult.add(i);
      } else {
        // End of line also indicates end of block
        bbox.setBlockEnd(true);
      }
    }
    if (page.size() > 0) {
      page.get(page.size() - 1).setLineEnd(true);
      page.get(page.size() - 1).setBlockEnd(true);
    }

    final List<Range> mergedLines = new LinkedList<>();
    for (int i = 0; i < ranges.size(); i++) {
      if (!mergedRangesIndexesToSkipInResult.contains(i)) {
        mergedLines.add(ranges.get(i).getLeft());
      }
    }

    page.setLines(mergedLines);

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

        final Box tailBox = new Box(bbox.getBox().getRight(), bbox.getBox().getRight() + tailWidth,
            bbox.getBox().getTop(), bbox.getBox().getBottom());
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
      final Range line = page.getLines().get(lineNr);
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
