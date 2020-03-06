package pl.clarin.pwr.g419.action;

import com.google.common.base.Charsets;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import pl.clarin.pwr.g419.io.reader.HocrReader;
import pl.clarin.pwr.g419.struct.HocrDocument;

@Component
public class ActionLoadHocr extends Action {

  public ActionLoadHocr() {
    super("load-hocr", "Parse a set of hocr files");
  }

  @Override
  public void run() throws Exception {
    final String pathIndex = "../task4-train/index-hocr.list";
    final List<Path> paths = loadPaths(Paths.get(pathIndex));

    for (final Path path : paths) {
      try {
        final HocrDocument document = loadHocrDocument(path);
        System.out.println(String.format("%3d page(s) in %s", document.size(), path.toString()));
      } catch (final Exception ex) {
        getLogger().error("Failed to read {}", path.toString());
      }
    }
  }

  private List<Path> loadPaths(final Path path) throws IOException {
    return FileUtils.readLines(path.toFile(), Charsets.UTF_8).stream()
        .map(Paths::get)
        .map(path.getParent()::resolve)
        .collect(Collectors.toList());
  }

  private HocrDocument loadHocrDocument(final Path path)
      throws SAXException, IOException, ParserConfigurationException {
    return new HocrReader().parse(path);
  }
}
