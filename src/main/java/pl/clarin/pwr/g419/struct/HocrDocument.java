package pl.clarin.pwr.g419.struct;

import java.util.*;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class HocrDocument extends ArrayList<HocrPage> {

  String id;

  Metadata metadata = new Metadata();
  DocContextInfo docContextInfo = new DocContextInfo();

  // zwraca wszystkie strony - także te "tymczasowe" wygenerowane dla nagłówków
  public List<HocrPage> getAllPages() {
    ArrayList<HocrPage> pages = new ArrayList<>(this);
    pages.addAll(getDocContextInfo().getHeaders().stream().map(h -> h.getTmpPage()).collect(Collectors.toList()));
    pages.addAll(getDocContextInfo().getFooters().stream().map(h -> h.getTmpPage()).collect(Collectors.toList()));
    return pages;
  }


  public AnnotationList getAnnotations() {
    return new AnnotationList(this.stream().map(HocrPage::getAnnotations)
        .flatMap(Collection::stream).collect(Collectors.toList()));
  }

  public AnnotationList getAllPagesAnnotations() {
    return new AnnotationList(this.getAllPages().stream().map(HocrPage::getAnnotations)
        .flatMap(Collection::stream).collect(Collectors.toList()));
  }

  public AnnotationList getHeaderAnnotations() {
    return new AnnotationList(this.getDocContextInfo().getHeaders().stream().map(h -> h.getTmpPage()).map(HocrPage::getAnnotations)
        .flatMap(Collection::stream).collect(Collectors.toList()));
  }

  public final AnnotationList getFooterAnnotations() {
    return new AnnotationList(this.getDocContextInfo().getFooters().stream().map(h -> h.getTmpPage()).map(HocrPage::getAnnotations)
        .flatMap(Collection::stream).collect(Collectors.toList()));
  }

  public final AnnotationList getHeaderAndFooterAnnotations() {
    List<Annotation> result = new LinkedList<>();
    result.addAll(getHeaderAnnotations());
    result.addAll(getFooterAnnotations());
    return new AnnotationList(result);
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

  public int calculateNrOfLeadingEmptyPages() {
    int counter = 0;
    for (counter = 0; counter < this.size(); counter++) {
      if (this.get(counter).getNumberOfOriginalLines() != 0)
        break;
    }
    docContextInfo.setLeadingEmptyPages(counter);
    return counter;
  }

  public void trimLeadingEmptyPages() {
    int number = calculateNrOfLeadingEmptyPages();
    if (number > 0) {
      this.removeRange(0, number); /// :-) just because we inherit from ArrayList
      this.stream().forEach(page -> page.setNo(page.getNo() - number));  // przeliczenie stron , hmmm?
    }
  }


}
