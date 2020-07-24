package pl.clarin.pwr.g419.struct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class HeaderAndFooterStruct {

  public enum Type {HEADER, FOOTER}

  private Type type;

  // poziom dokumentu - dla korzenia = -1; dla pierwszych linii = 0
  private int level;

  // strona, na której zaczyna się nagłówek/stopka - włącznie
  private int startIndex;
  // strona, na której kończy się nagłówek/stopka - !!! włącznie !!!
  private int endIndex;

  // HocrLine'y składające się na cały nagłówek do tego poziomu idąc od poziomu zerowego do aktualnego.
  private List<HocrLine> lines = new LinkedList<>();

  private List<HeaderAndFooterStruct> nextLevelHeaders = new LinkedList<>();

  private HocrPage tmpPage;


  public HeaderAndFooterStruct(HeaderAndFooterStruct hafs) {
    this.startIndex = hafs.startIndex;
    this.endIndex = hafs.endIndex;
    this.level = hafs.level;
    this.lines = new ArrayList<>(hafs.lines);
  }

  public void generateTmpPageFromLines() {
    List<Bbox> result = lines.stream().flatMap(line -> line.getBboxes().stream()).collect(Collectors.toList());
    HocrPage page = new HocrPage(null, new Bboxes(result));
    // tutaj nie trzeba Boxa ustawiać bo to jest tylko do zrobienia  adnotacji ta strona
    page.setLines(lines);
    this.tmpPage = page;
  }

  public HeaderAndFooterStruct createChild(int startPageIndex, int endPageIndexIncl, HocrLine lineToAdd) {
    HeaderAndFooterStruct newHafs = new HeaderAndFooterStruct(this);
    newHafs.setLevel(this.getLevel() + 1);
    newHafs.setStartIndex(startPageIndex);
    newHafs.setEndIndex(endPageIndexIncl);
    if (lineToAdd != null) {
      newHafs.getLines().add(lineToAdd);
    }
    return newHafs;
  }


  public String toString() {
    return "[ [" + (getStartIndex() /*+ 1*/) + ":" + (getEndIndex() /*+ 1*/) + "]:(" + level + ")\t'" + lines.stream().map(l -> l.getText()).collect(Collectors.joining(" | ")) + "' ]";
  }


}
