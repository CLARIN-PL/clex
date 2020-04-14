package pl.clarin.pwr.g419.action;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import pl.clarin.pwr.g419.action.options.ActionOptionMetadata;
import pl.clarin.pwr.g419.io.reader.MetadataReader;
import pl.clarin.pwr.g419.struct.Metadata;

@Component
public class ActionSample extends Action {

  ActionOptionMetadata optionMetadata = new ActionOptionMetadata();

  public ActionSample() {
    super("sample", "create a random sample of documents");
    this.addOption(optionMetadata);
  }

  @Override
  public void run() throws Exception {
    final List<Metadata> metadata = new MetadataReader().parse(Paths.get(optionMetadata.getString()));

    final Map<String, List<Metadata>> documentsByCompany = metadata.stream()
        .collect(Collectors.groupingBy(o -> o.getCompany()));

    documentsByCompany.entrySet().stream()
        .sorted((o1, o2) -> Integer.compare(o2.getValue().size(), o1.getValue().size()))
        .limit(110)
        .map(e -> new ImmutablePair<>(e.getValue().get(0).getId(), e.getValue()))
        //.sorted(Comparator.comparing(Pair::getKey))
        .map(p -> String.format("%s,%d,%s",
            p.getKey(), p.getValue().size(), metadataToStr(p.getValue())))
        .forEach(System.out::println);
  }

  private String metadataToStr(final Collection<Metadata> metadatas) {
    return metadatas.stream().map(Metadata::getId)
        .map(String::toString)
        .collect(Collectors.joining(";"));
  }

}
