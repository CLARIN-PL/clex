package pl.clarin.pwr.g419.struct;

import lombok.Data;

@Data
public class DocContextInfo {

  LineHeightHistogram histogram;
  int mostCommonHeightOfLine;
  int pageNrWithSigns = 0;


}
