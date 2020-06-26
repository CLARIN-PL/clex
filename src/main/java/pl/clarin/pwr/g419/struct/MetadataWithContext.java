package pl.clarin.pwr.g419.struct;

import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class MetadataWithContext {

  FieldContext<String> id = new FieldContext<>();
  FieldContext<String> company = new FieldContext<>();
  FieldContext<Date> drawingDate = new FieldContext<>();
  FieldContext<Date> periodFrom = new FieldContext<>();
  FieldContext<Date> periodTo = new FieldContext<>();
  FieldContext<String> postalCode = new FieldContext<>();
  FieldContext<String> city = new FieldContext<>();
  FieldContext<String> street = new FieldContext<>();
  FieldContext<String> streetNo = new FieldContext<>();
  List<FieldContext<Person>> people = Lists.newArrayList();
  FieldContext<String> signsPage = new FieldContext<>();

}
