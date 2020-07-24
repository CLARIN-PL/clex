package pl.clarin.pwr.g419.text.lemmatizer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CompanyLemmatizer {

  Map<String, String> lemmas = Maps.newHashMap();

  Set<String> ignore = Sets.newHashSet();

  public CompanyLemmatizer() {
    lemmas.put("AGORY", "AGORA");
    lemmas.put("BANKU", "BANK");
    lemmas.put("DOMU", "DOM");
    lemmas.put("FABRYKI", "FABRYKA");
    lemmas.put("GRUPY", "GRUPA");
    lemmas.put("HANDLOWEGO", "HANDLOWY");
    lemmas.put("MAKLERSKIEGO", "MAKLERSKI");
    lemmas.put("NARODOWEGO", "NARODOWY");
    lemmas.put("TOWARZYSTWA", "TOWARZYSTWO");
    lemmas.put("ZACHODNIEGO", "ZACHODNI");
    lemmas.put("FUNDUSZU", "FUNDUSZ");
    lemmas.put("PRZEDSIĘBIORSTWA", "PRZEDSIĘBIORSTWO");
    lemmas.put("PRODUKCYJNO-HANDLOWEGO", "PRODUKCYJNO-HANDLOWE");
    lemmas.put("INWESTYCYJNEGO", "INWESTYCYJNY");
    lemmas.put("FABRYK", "FABRYKI");
    lemmas.put("NFI", "NARODOWY FUNDUSZ INWESTYCYJNY");
    lemmas.put("POLSKIEGO", "POLSKI");
    lemmas.put("POLSKIEJ", "POLSKA");

    ignore.add("PÓŁROCZNY");
    ignore.add("DOMINUJĄCA");
    ignore.add("SPÓŁKI");
    ignore.add("FIRMY");
    ignore.add("UDZIAŁ");
    ignore.add("%");

    ignore.add("SPÓŁKA AKCYJNA");
    ignore.add("S.A");
    ignore.add("S.A.");

  }

  public String lemmatize(final String text) {
    return Arrays.stream(text.split(" "))
        .map(orth -> lemmas.getOrDefault(orth, orth))
        .filter(orth -> !ignore.contains(orth))
        .collect(Collectors.joining(" "));
  }
}
