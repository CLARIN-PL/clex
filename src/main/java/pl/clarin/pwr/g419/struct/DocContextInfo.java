package pl.clarin.pwr.g419.struct;

import lombok.Data;
import java.util.List;

@Data
public class DocContextInfo {

  LineHeightHistogram histogram;
  int mostCommonHeightOfLine;
  int pageNrWithSigns = 0;
  int leadingEmptyPages = 0;
  
  List<HeaderAndFooterStruct> headers;
  List<HeaderAndFooterStruct> footers;


}
