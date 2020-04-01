package pl.clarin.pwr.g419.struct;

import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class Metadata {

  public static String ID = "id";
  public static String COMPANY = "company";
  public static String DRAWING_DATE = "drawing_date";
  public static String PERIOD_FROM = "period_from";
  public static String PERIOD_TO = "period_to";
  public static String POSTAL_CODE = "postal_code";
  public static String CITY = "city";
  public static String STREET = "street";
  public static String STREET_NO = "street_no";
  public static String PEOPLE = "people";

  String id;
  String company;
  Date drawingDate;
  Date periodFrom;
  Date periodTo;
  String postalCode;
  String city;
  String street;
  String streetNo;
  List<Person> people = Lists.newArrayList();
}
