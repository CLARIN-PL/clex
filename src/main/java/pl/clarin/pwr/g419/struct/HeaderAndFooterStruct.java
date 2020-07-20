package pl.clarin.pwr.g419.struct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class HeaderAndFooterStruct {
  private int startIndex;
  private int endIndex;

  private List<String> lines = new LinkedList<>();


  public HeaderAndFooterStruct(HeaderAndFooterStruct hafs) {
    this.startIndex = hafs.startIndex;
    this.endIndex = hafs.endIndex;
    this.lines = new ArrayList<>(hafs.lines);
  }

}
