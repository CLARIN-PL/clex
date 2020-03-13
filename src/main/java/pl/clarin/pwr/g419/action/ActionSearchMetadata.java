package pl.clarin.pwr.g419.action;

import java.nio.file.Paths;
import java.util.List;
import org.springframework.stereotype.Component;
import pl.clarin.pwr.g419.action.options.ActionOptionInput;
import pl.clarin.pwr.g419.action.options.ActionOptionMetadata;
import pl.clarin.pwr.g419.action.options.ActionOptionThreads;
import pl.clarin.pwr.g419.io.reader.DocumentsReader;
import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.struct.Person;
import pl.clarin.pwr.g419.utils.HocrDocumentSearch;

@Component
public class ActionSearchMetadata extends Action {

  ActionOptionMetadata optionMetadata = new ActionOptionMetadata();
  ActionOptionInput optionInput = new ActionOptionInput();
  ActionOptionThreads optionThreads = new ActionOptionThreads();

  public ActionSearchMetadata() {
    super("search-metadata", "Parse a set of hocr files");
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
    final HocrDocumentSearch search = new HocrDocumentSearch(document);
    System.out.println(document);
    for (final Person person : document.getMetadata().getPeople()) {
      System.out.println(">> " + person);
      searchPhrase(search, person.getName());
      searchPhrase(search, person.getRole());
    }
    System.out.println();
  }

  private void searchPhrase(final HocrDocumentSearch search, final String phrase) {
    System.out.println(">>-- " + phrase);
    search.search(phrase).stream()
        .map(p -> String.format(">>-->> %s", p.getText()))
        .forEach(System.out::println);
  }
}
