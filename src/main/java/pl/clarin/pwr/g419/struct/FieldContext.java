package pl.clarin.pwr.g419.struct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldContext<T> {

  public FieldContext(final T f, final String c, final String r) {
    this.field = f;
    this.context = c;
    this.rule = r;
  }

  T field;
  String context = "";
  String rule = "";
  int page = 0;

}
