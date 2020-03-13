package pl.clarin.pwr.g419.action;

import java.nio.file.Paths;
import java.util.List;
import org.springframework.stereotype.Component;
import pl.clarin.pwr.g419.action.options.ActionOptionInput;
import pl.clarin.pwr.g419.action.options.ActionOptionMetadata;
import pl.clarin.pwr.g419.action.options.ActionOptionThreads;
import pl.clarin.pwr.g419.io.reader.DocumentsReader;
import pl.clarin.pwr.g419.struct.HocrDocument;

@Component
public class ActionLoadHocr extends Action {

  ActionOptionMetadata optionMetadata = new ActionOptionMetadata();
  ActionOptionInput optionInput = new ActionOptionInput();
  ActionOptionThreads optionThreads = new ActionOptionThreads();

  public ActionLoadHocr() {
    super("load-hocr", "Parse a set of hocr files");
    this.addOption(optionMetadata);
    this.addOption(optionInput);
    this.addOption(optionThreads);
  }

  @Override
  public void run() throws Exception {
    final DocumentsReader reader = new DocumentsReader(optionThreads.getInteger());
    final List<HocrDocument> documents =
        reader.parse(Paths.get(optionMetadata.getString()), Paths.get(optionInput.getString()));
    System.out.println(documents.size());
  }

}
