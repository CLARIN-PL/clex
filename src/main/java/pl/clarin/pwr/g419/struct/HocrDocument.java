package pl.clarin.pwr.g419.struct;

import java.util.ArrayList;
import lombok.Data;

@Data
public class HocrDocument extends ArrayList<HocrPage> {

  String id;

  Metadata metadata;

}
