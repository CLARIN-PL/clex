package pl.clarin.pwr.g419.struct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class HeaderAndFooterStruct {
  private int startRange;
  private int endRange;

  private String line;
  private List<Bbox> lineBbox;


}
