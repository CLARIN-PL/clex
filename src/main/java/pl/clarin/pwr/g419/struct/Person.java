package pl.clarin.pwr.g419.struct;

import java.util.Date;
import lombok.Data;

@Data
public class Person {
  Date date;
  String name;
  String role;
}
