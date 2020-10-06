package pl.clarin.pwr.g419.io.reader;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import pl.clarin.pwr.g419.struct.Metadata;
import pl.clarin.pwr.g419.struct.Person;

import static pl.clarin.pwr.g419.io.reader.MetadataReader.recordToMetadataCommon;

public class MetadataSparseReader {

  public static String PERSON_DATE = "person_date";
  public static String PERSON_NAME = "person_name";
  public static String PERSON_ROLE = "person_role";

  public List<Metadata> parse(final Path filename) throws Exception {
    final CSVParser parser = CSVParser.parse(filename.toFile(),
        StandardCharsets.UTF_8,
        CSVFormat.EXCEL
            .withDelimiter(';')
            .withHeader(Metadata.ID, Metadata.COMPANY, Metadata.DRAWING_DATE,
                Metadata.PERIOD_FROM, Metadata.PERIOD_TO, Metadata.POSTAL_CODE,
                Metadata.CITY, Metadata.STREET, Metadata.STREET_NO,
                PERSON_DATE, PERSON_NAME, PERSON_ROLE)
            .withSkipHeaderRecord());

    final List<Metadata> metadata = Lists.newArrayList();
    Metadata metadataItem = null;
    for (final CSVRecord record : parser) {
      if (record.get(Metadata.ID).trim().length() > 0) {
        metadataItem = recordToMetadata(record);
        metadata.add(metadataItem);
      } else if (metadataItem == null) {
        throw new Exception("Failed to parse the metadata. Row with person data without document metadata.");
      } else {
        recordToPerson(record).ifPresent(metadataItem.getPeople()::add);
      }
    }
    return metadata;
  }

  private Metadata recordToMetadata(final CSVRecord record) throws IOException, ParseException {
    final Metadata m = MetadataReader.recordToMetadataCommon(record);
    recordToPerson(record).ifPresent(m.getPeople()::add);
    return m;
  }

  private Optional<Person> recordToPerson(final CSVRecord record) throws IOException, ParseException {
    if (notEmpty(record.get(PERSON_NAME))) {
      final Person p = new Person();
      p.setDate(strToDate(record.get(PERSON_DATE)));
      p.setName(record.get(PERSON_NAME));
      p.setRole(record.get(PERSON_ROLE));
      return Optional.of(p);
    }
    return Optional.empty();
  }

  private boolean notEmpty(final String value) {
    return value != null && value.length() > 0;
  }

  private Date strToDate(final String date) throws ParseException {
    if (date == null || date.length() == 0) {
      return null;
    }
    return new SimpleDateFormat("yyyy-MM-dd").parse(date);
  }

}
