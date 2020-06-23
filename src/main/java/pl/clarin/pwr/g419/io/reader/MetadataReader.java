package pl.clarin.pwr.g419.io.reader;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import pl.clarin.pwr.g419.HasLogger;
import pl.clarin.pwr.g419.struct.Metadata;
import pl.clarin.pwr.g419.struct.Person;

public class MetadataReader implements HasLogger {

  public List<Metadata> parse(final Path filename) throws Exception {
    final CSVParser parser = CSVParser.parse(filename.toFile(),
        Charset.forName("utf-8"),
        CSVFormat.EXCEL
            .withDelimiter(';')
            .withHeader(Metadata.ID, Metadata.COMPANY, Metadata.DRAWING_DATE,
                Metadata.PERIOD_FROM, Metadata.PERIOD_TO, Metadata.POSTAL_CODE,
                Metadata.CITY, Metadata.STREET, Metadata.STREET_NO, Metadata.PEOPLE, Metadata.SIGN_PAGE)
            .withSkipHeaderRecord());

    final List<Metadata> metadata = Lists.newArrayList();
    for (final CSVRecord record : parser) {
      metadata.add(recordToMetadata(record));
    }
    return metadata;
  }

  private Metadata recordToMetadata(final CSVRecord record) throws IOException, ParseException {
    final Metadata m = new Metadata();
    m.setId(record.get(Metadata.ID));
    m.setCompany(record.get(Metadata.COMPANY));
    m.setDrawingDate(strToDate(record.get(Metadata.DRAWING_DATE)));
    m.setPeriodFrom(strToDate(record.get(Metadata.PERIOD_FROM)));
    m.setPeriodTo(strToDate(record.get(Metadata.PERIOD_TO)));
    m.setPostalCode(record.get(Metadata.POSTAL_CODE));
    m.setCity(record.get(Metadata.CITY));
    m.setStreet(record.get(Metadata.STREET));
    m.setStreetNo(record.get(Metadata.STREET_NO));
    m.setPeople(parsePeople(record.get(Metadata.PEOPLE)));
    m.setSignsPage(record.get(Metadata.SIGN_PAGE));
    return m;
  }

  private Date strToDate(final String date) throws ParseException {
    if (date == null || date.length() == 0) {
      return null;
    }
    return new SimpleDateFormat("yyyy-MM-dd").parse(date);
  }

  @SneakyThrows
  private List<Person> parsePeople(final String str) throws IOException {
    final Pattern p = Pattern.compile("[)], [(]");
    final List<Person> people = Lists.newArrayList();
    if (str.length() < 3) {
      return Lists.newArrayList();
    }
    final String strNoBrackets = str.substring(2, str.length() - 2).trim();
    for (final String part : p.split(strNoBrackets)) {
      final String[] cols = part.substring(1, part.length() - 1).split("['\"], ['\"]");
      if (cols.length != 3) {
        System.out.println("ERROR " + part);
      }
      final Person person = new Person();
      person.setDate(strToDate(cols[0]));
      person.setName(cols[1]);
      person.setRole(normalizeRole(cols[2]));
      people.add(person);
    }
    return people;
  }

  private String normalizeRole(final String role) {
    return role
        .replaceAll("[\\\\]n", " ")
        .replaceAll("[ ]+", " ")
        .toLowerCase();
  }

}
