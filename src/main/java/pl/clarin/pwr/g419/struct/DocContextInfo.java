package pl.clarin.pwr.g419.struct;

import lombok.Data;
import java.util.Collections;
import java.util.List;

@Data
public class DocContextInfo {

  LineHeightHistogram histogram;
  int mostCommonHeightOfLine;
  int pageNrWithSigns = 0;
  int leadingEmptyPages = 0;

  List<HeaderAndFooterStruct> headers = Collections.emptyList();
  List<HeaderAndFooterStruct> footers = Collections.emptyList();

  public void sortHeaders() {
    Collections.sort(headers,
        (h1, h2) -> (h1.getStartIndex() == h2.getStartIndex() ? 0 : (h1.getStartIndex() < h2.getStartIndex() ? -1 : 1)));
  }

  public void sortFooters() {
    Collections.sort(footers,
        (f1, f2) -> (f1.getStartIndex() == f2.getStartIndex() ? 0 : (f1.getStartIndex() < f2.getStartIndex() ? -1 : 1)));
  }

}
