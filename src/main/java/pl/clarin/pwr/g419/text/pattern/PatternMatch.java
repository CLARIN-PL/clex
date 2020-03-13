package pl.clarin.pwr.g419.text.pattern;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.Data;
import pl.clarin.pwr.g419.struct.Bbox;
import pl.clarin.pwr.g419.struct.HocrPage;

@Data
@AllArgsConstructor
public class PatternMatch {
  int indexBegin;
  int indexEnd;
  HocrPage page;

  public String getText() {
    return IntStream.range(indexBegin, indexEnd)
        .mapToObj(page::get)
        .map(Bbox::getText)
        .collect(Collectors.joining(" "));
  }

  public String toString() {
    return String.format("[%4d:%4d] = %s", indexBegin, indexEnd, getText());
  }
}
