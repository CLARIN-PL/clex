package pl.clarin.pwr.g419.struct;

import java.util.Objects;
import lombok.Data;

@Data
public class Annotation {
  String type;
  HocrPage page;
  int indexBegin;
  int indexEnd;

  public Annotation(final String type, final HocrPage page,
                    final int indexBegin, final int indexEnd) {
    this.type = type;
    this.page = page;
    this.indexBegin = indexBegin;
    this.indexEnd = indexEnd;
  }

  public int getLength() {
    return indexEnd - indexBegin;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Annotation that = (Annotation) o;
    return indexBegin == that.indexBegin &&
        indexEnd == that.indexEnd &&
        Objects.equals(type, that.type) &&
        Objects.equals(page, that.page);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, indexBegin, indexEnd);
  }
}
