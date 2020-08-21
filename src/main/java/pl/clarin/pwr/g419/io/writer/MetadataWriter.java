package pl.clarin.pwr.g419.io.writer;

import com.google.common.collect.Lists;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.text.StringEscapeUtils;
import pl.clarin.pwr.g419.struct.Metadata;
import pl.clarin.pwr.g419.struct.Person;

public class MetadataWriter {

  public static void write(final List<Metadata> metadata, final Path output) throws IOException {
    try (
        final BufferedWriter writer = Files.newBufferedWriter(output);
        final CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL
            .withDelimiter(';').withHeader(
                Metadata.ID, Metadata.COMPANY, Metadata.DRAWING_DATE,
                Metadata.PERIOD_FROM, Metadata.PERIOD_TO, Metadata.POSTAL_CODE,
                Metadata.CITY, Metadata.STREET, Metadata.STREET_NO, Metadata.PEOPLE
            ))
    ) {
      for (final Metadata metadataRow : metadata) {
        csvPrinter.printRecord(metadataToRecord(metadataRow));
      }
    }
  }

  private static List<String> metadataToRecord(final Metadata metadata) {
    final List<String> record = Lists.newArrayList();
    record.add(metadata.getId());
    record.add(metadata.getCompany());
    record.add(dateToStr(metadata.getDrawingDate()));
    record.add(dateToStr(metadata.getPeriodFrom()));
    record.add(dateToStr(metadata.getPeriodTo()));
    record.add(metadata.getPostalCode());
    record.add(metadata.getCity());
    record.add(metadata.getStreet());
    record.add(metadata.getStreetNo());
    record.add(peopleToStr(metadata.getPeople()));
    return record;
  }

  private static String peopleToStr(final List<Person> people) {
    return "[" + people.stream().map(MetadataWriter::personToStr)
        .collect(Collectors.joining(", ")) + "]";
  }

  private static String personToStr(final Person person) {
    final String value = List.of(dateToStr(person.getDate()), person.getName(), person.getRole())
        .stream()
        .map(StringEscapeUtils::escapeCsv)
        .map(s -> String.format("'%s'", s))
        .collect(Collectors.joining(", "));
    return String.format("(%s)", value);
  }

  private static String dateToStr(final Date date) {
    if (date == null) {
      return "";
    } else {
      return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
  }

}
