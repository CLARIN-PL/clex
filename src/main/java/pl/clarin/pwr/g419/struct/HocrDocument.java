package pl.clarin.pwr.g419.struct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class HocrDocument extends ArrayList<HocrPage> {

  String id;

  Metadata metadata = new Metadata();
  DocContextInfo docContextInfo = new DocContextInfo();


  public AnnotationList getAnnotations() {
    return new AnnotationList(this.stream().map(HocrPage::getAnnotations)
        .flatMap(Collection::stream).collect(Collectors.toList()));
  }


  public AnnotationList getAnnotationsForSignsPage() {
    if (docContextInfo.getPageNrWithSigns() == 0) {
      return new AnnotationList(new ArrayList<>());
    }

    return new AnnotationList(this.stream()
        .filter(page -> page.getNo() == docContextInfo.getPageNrWithSigns())
        .map(HocrPage::getAnnotations)
        .flatMap(Collection::stream).collect(Collectors.toList()));
  }

  public AnnotationList getAnnotationsForPeople() {
    if (docContextInfo.getPageNrWithSigns() == 0) {
      return new AnnotationList(this.stream()
          .map(HocrPage::getAnnotations)
          .flatMap(Collection::stream).collect(Collectors.toList()));
    } else {
      return new AnnotationList(this.stream()
          .filter(page -> page.getNo() == docContextInfo.getPageNrWithSigns())
          .map(HocrPage::getAnnotations)
          .flatMap(Collection::stream).collect(Collectors.toList()));
    }
  }


  public String getLineInPage(final int line, final int page) {
    return this.get(page).getLines().get(line).getText();
  }


}
