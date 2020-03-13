package pl.clarin.pwr.g419.action;

import com.google.common.collect.Sets;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;
import pl.clarin.pwr.g419.action.options.ActionOptionInput;
import pl.clarin.pwr.g419.action.options.ActionOptionMetadata;
import pl.clarin.pwr.g419.action.options.ActionOptionThreads;
import pl.clarin.pwr.g419.io.reader.DocumentsReader;
import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText;

@Component
public class ActionSearchPatterns extends Action {

  ActionOptionMetadata optionMetadata = new ActionOptionMetadata();
  ActionOptionInput optionInput = new ActionOptionInput();
  ActionOptionThreads optionThreads = new ActionOptionThreads();

  Pattern pattern = getPattern();

  public ActionSearchPatterns() {
    super("search-patterns", "search predefined patterns in the documents");
    this.options.add(optionMetadata);
    this.options.add(optionInput);
    this.options.add(optionThreads);
  }

  @Override
  public void run() throws Exception {
    final DocumentsReader reader = new DocumentsReader(optionThreads.getInteger());
    final List<HocrDocument> documents =
        reader.parse(Paths.get(optionMetadata.getString()), Paths.get(optionInput.getString()));

    documents.forEach(this::processDocument);
  }

  private void processDocument(final HocrDocument document) {
    System.out.println(document.getId());
    final List<String> matches = document.stream()
        .map(pattern::find)
        .flatMap(Collection::stream)
        .map(PatternMatch::toString)
        .collect(Collectors.toList());

    if (matches.size() == 0) {
      System.out.println(document.getId() + " NOT FOUND");
    } else {
      matches.forEach(System.out::println);
    }
  }

  private Pattern getPattern() {
    final Set<String> months = Sets.newHashSet("stycznia", "lutego");
    final Set<String> years = IntStream.range(1900, 2020)
        .mapToObj(Objects::toString).collect(Collectors.toSet());
    final Set<String> days = IntStream.range(1, 13)
        .mapToObj(Objects::toString).collect(Collectors.toSet());
    return new Pattern()
        .next(new MatcherLowerText(days))
        .next(new MatcherLowerText(months))
        .next(new MatcherLowerText(years));
  }
}
