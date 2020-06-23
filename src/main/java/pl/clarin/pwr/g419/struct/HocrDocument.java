package pl.clarin.pwr.g419.struct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class HocrDocument extends ArrayList<HocrPage> {

  String id;

  Metadata metadata = new Metadata();
  int pageNrWithSigns = 0;


  public AnnotationList getAnnotations() {
    return new AnnotationList(this.stream().map(HocrPage::getAnnotations)
        .flatMap(Collection::stream).collect(Collectors.toList()));
  }

  public AnnotationList getAnnotationsForSignsPage() {
    if (pageNrWithSigns == 0) {
      return new AnnotationList(new ArrayList<>());
    }

    return new AnnotationList(this.stream()
        .filter(page -> page.getNo() == pageNrWithSigns)
        .map(HocrPage::getAnnotations)
        .flatMap(Collection::stream).collect(Collectors.toList()));
  }

  public String getLineInPage(final int line, final int page) {
    return this.get(page).getLines().get(line).getText();
  }


}
