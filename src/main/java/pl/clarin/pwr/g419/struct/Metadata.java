package pl.clarin.pwr.g419.struct;

import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import lombok.Data;
import pl.clarin.pwr.g419.utils.DateUtils;

@Data
public class Metadata {

  public static final String ID = "id";
  public static final String COMPANY = "company";
  public static final String DRAWING_DATE = "drawing_date";
  public static final String PERIOD_FROM = "period_from";
  public static final String PERIOD_TO = "period_to";
  public static final String POSTAL_CODE = "postal_code";
  public static final String CITY = "city";
  public static final String STREET = "street";
  public static final String STREET_NO = "street_no";
  public static final String PEOPLE = "people";
  public static final String SIGN_PAGE = "sign_page";

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
  String signsPage;


  public static Metadata of(List<List<String>> records) {
    Metadata metadata = new Metadata();

    List<String> first = records.get(0);
    metadata.setId(first.get(1));

    for (List<String> record : records) {
      switch (record.get(2)) {
        case DRAWING_DATE:
          metadata.setDrawingDate(DateUtils.parseDate(record.get(4)));
          break;
        case PERIOD_FROM:
          metadata.setPeriodFrom(DateUtils.parseDate(record.get(4)));
          break;
        case PERIOD_TO:
          metadata.setPeriodTo(DateUtils.parseDate(record.get(4)));
          break;
        case COMPANY:
          metadata.setCompany(record.get(4));
          break;
        case POSTAL_CODE:
          metadata.setPostalCode(record.get(4));
          break;
        case CITY:
          metadata.setCity(record.get(4));
          break;
        case STREET:
          metadata.setStreet(record.get(4));
          break;
        case STREET_NO:
          metadata.setStreetNo(record.get(4));
          break;
        default:
      }
    }

    return metadata;
  }


}
