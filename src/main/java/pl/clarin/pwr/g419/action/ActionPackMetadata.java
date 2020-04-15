package pl.clarin.pwr.g419.action;

import java.util.List;
import org.springframework.stereotype.Component;
import pl.clarin.pwr.g419.action.options.ActionOptionInput;
import pl.clarin.pwr.g419.action.options.ActionOptionMetadata;
import pl.clarin.pwr.g419.io.reader.MetadataSparseReader;
import pl.clarin.pwr.g419.io.writer.MetadataWriter;
import pl.clarin.pwr.g419.struct.Metadata;

@Component
public class ActionPackMetadata extends Action {

  ActionOptionMetadata optionMetadata = new ActionOptionMetadata();
  ActionOptionInput optionInput = new ActionOptionInput("path to input csv with metadata in unpacked format");

  public ActionPackMetadata() {
    super("pack-metadata", "convert unpacked csv metadata file to a packed format");
    this.addOption(optionMetadata);
    this.addOption(optionInput);
  }

  @Override
  public void run() throws Exception {
    final List<Metadata> metadata = new MetadataSparseReader().parse(optionInput.getPath());
    MetadataWriter.write(metadata, optionMetadata.getPath());
  }

}
