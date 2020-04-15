package pl.clarin.pwr.g419.io.reader;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;
import pl.clarin.pwr.g419.HasLogger;
import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.struct.Metadata;

public class DocumentsReader implements HasLogger {

  private final int threads;

  public DocumentsReader(final int threads) {
    this.threads = threads;
  }

  public List<HocrDocument> parse(final Path metadataCsv, final Path hocrIndex) throws Exception {

    final List<Metadata> metadata = loadMetadata(metadataCsv);

    final List<HocrDocument> hocrs;
    if (hocrIndex.toString().endsWith(".hocr")) {
      hocrs = List.of(loadHocrDocument(hocrIndex));
    } else {
      hocrs = loadHocr(hocrIndex);
    }

    final Map<String, Metadata> idToMetadata =
        metadata.stream().collect(Collectors.toMap(Metadata::getId, Function.identity()));
    hocrs.forEach(d -> d.setMetadata(idToMetadata.getOrDefault(d.getId(), new Metadata())));

    return hocrs;
  }

  private List<Metadata> loadMetadata(final Path metadataCsv) throws Exception {
    return new MetadataReader().parse(metadataCsv);
  }

  private List<HocrDocument> loadHocr(final Path hocrIndex) throws Exception {
    final List<HocrDocument> documents = Lists.newArrayList();
    final ThreadPoolExecutor pool =
        (ThreadPoolExecutor) Executors.newFixedThreadPool(this.threads);

    CompletableFuture.allOf(loadPaths(hocrIndex).stream()
        .map(path -> CompletableFuture.runAsync(() -> {
          try {
            final HocrDocument document = loadHocrDocument(path);
            documents.add(document);
            getLogger().info(String.format("%3d page(s) in %s", document.size(), path.toString()));
          } catch (final Exception ex) {
            getLogger().error("[{}] {}", path.toString(), ex.toString());
          }
        }, pool))
        .toArray(CompletableFuture[]::new)).join();

    pool.shutdown();

    return documents;
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
