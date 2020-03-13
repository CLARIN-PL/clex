package pl.clarin.pwr.g419.io.reader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import pl.clarin.pwr.g419.struct.Bbox;
import pl.clarin.pwr.g419.struct.Box;
import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.struct.HocrPage;

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

  private HocrDocument document = null;
  private HocrPage page = null;
  private String lastClass = "";
  private Box lastBox = null;

  public HocrDocument parse(final Path path)
      throws IOException, SAXException, ParserConfigurationException {

    final String text = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    final SAXParserFactory factory = SAXParserFactory.newInstance();
    final SAXParser parser = factory.newSAXParser();
    final String textWithoutMarkers = cleanHocr(text);
    final InputStream is = new ByteArrayInputStream(textWithoutMarkers.getBytes());
    this.document = new HocrDocument();
    parser.parse(is, this);
    this.document.setId(getIdFromPath(path));
    return document;
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
      }
    } else if (elementName.equalsIgnoreCase(TAG_DIV)) {
      if (ATTR_CLASS_PAGE.equals(attrClass)) {
        this.page = new HocrPage();
        document.add(page);
      }
    }
    lastClass = attributes.getValue(ATTR_CLASS);
  }

  static private Box titleToBox(final String title) throws NumberFormatException {
    final String[] cols = title.split(" ");
    return new Box(Integer.parseInt(cols[1]),
        Integer.parseInt(cols[2]),
        Integer.parseInt(cols[3]),
        Integer.parseInt(cols[4]));
  }

  @Override
  public void endElement(final String uri,
                         final String localName,
                         final String elementName) {
    if (elementName.equalsIgnoreCase(TAG_SPAN) && ATTR_CLASS_WORD.equals(lastClass)) {
      page.add(new Bbox(value.toString(), lastBox));
      lastClass = null;
    }
  }

  @Override
  public void characters(final char[] ac, final int start, final int length) {
    for (int i = start; i < start + length; i++) {
      value.append(ac[i]);
    }
  }
}
