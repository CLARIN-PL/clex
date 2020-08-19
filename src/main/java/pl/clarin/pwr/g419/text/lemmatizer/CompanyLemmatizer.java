package pl.clarin.pwr.g419.text.lemmatizer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import pl.clarin.pwr.g419.kbase.CompanySuffix;

public class CompanyLemmatizer {

  CompanySuffix suffix = new CompanySuffix();

  Map<String, String> lemmasWords = Maps.newHashMap();
  Map<String, String> lemmasNames = Maps.newHashMap();

  Set<String> ignore = Sets.newHashSet();

  public CompanyLemmatizer() {
    lemmasWords.put("AGORY", "AGORA");
    lemmasWords.put("BANKU", "BANK");
    lemmasWords.put("DOMU", "DOM");
    lemmasWords.put("FABRYKI", "FABRYKA");
    lemmasWords.put("GRUPY", "GRUPA");
    lemmasWords.put("HANDLOWEGO", "HANDLOWY");
    lemmasWords.put("MAKLERSKIEGO", "MAKLERSKI");
    lemmasWords.put("NARODOWEGO", "NARODOWY");
    lemmasWords.put("TOWARZYSTWA", "TOWARZYSTWO");
    lemmasWords.put("ZACHODNIEGO", "ZACHODNI");
    lemmasWords.put("FUNDUSZU", "FUNDUSZ");
    lemmasWords.put("PRZEDSIĘBIORSTWA", "PRZEDSIĘBIORSTWO");
    lemmasWords.put("PRODUKCYJNO-HANDLOWEGO", "PRODUKCYJNO-HANDLOWE");
    lemmasWords.put("INWESTYCYJNEGO", "INWESTYCYJNY");
    lemmasWords.put("FABRYK", "FABRYKI");
    lemmasWords.put("NFI", "NARODOWY FUNDUSZ INWESTYCYJNY");
    lemmasWords.put("POLSKIEGO", "POLSKI");
    lemmasWords.put("POLSKIEJ", "POLSKA");
    lemmasWords.put("PKO", "POWSZECHNA KASA OSZCZĘDNOŚCI");
    lemmasWords.put("DM", "DOM MAKLERSKI");
    lemmasWords.put("PTI", "POWSZECHNE TOWARZYSTWO INWESTYCYJNE");
    lemmasWords.put("BOŚ", "BANK OCHRONY ŚRODOWISKA");
    lemmasWords.put("PEKAO", "POLSKA KASA OPIEKI");
    lemmasWords.put("PBKM", "POLSKI BANK KOMÓREK MACIERZYSTYCH");

// z arkusza :
    lemmasNames.put("- GRAAL", "GRAAL");
    lemmasNames.put(".BUDOPOL-WROCŁAW", "BUDOPOL-WROCŁAW");
    lemmasNames.put(".PZU", "POWSZECHNY ZAKŁAD UBEZPIECZEŃ");
    lemmasNames.put("4 FUN MEDIA", "4FUN MEDIA");
    lemmasNames.put("AD.DRĄGOWSKI", "AD.DRĄGOWSKI SPOŁKA AKCYJNA");
    lemmasNames.put("AMREST HOLDINGS SE", "AMREST HOLDINGS");
    lemmasNames.put("ARMATURA", "ARMATURA KRAKÓW");
    lemmasNames.put("ASSECO", "ASSECO POLAND");
    lemmasNames.put("ATM", "ATM SYSTEMY INFORMATYCZNE");
    lemmasNames.put("AZOTY", "GRUPA AZOTY");
    lemmasNames.put("BANK MILLENNIUM", "ZAKŁADY MIĘSNE HENRYK KANIA");
    lemmasNames.put("CASH FLOW", "CASH FLOW SPÓŁA AKCYJNA");
    lemmasNames.put("CENTRUM GIEŁDOWE", "GIEŁDA PAPIERÓW WARTOŚCIOWYCH W WARSZAWIE");
    lemmasNames.put("CNT", "CENTRUM NOWOCZESNYCH TECHNOLOGII");
    lemmasNames.put("COLIAN HOLDING", "COLIAN HOLDINGS");
    lemmasNames.put("DOM MAKLERSKI WDM", "DOM MAKLERSKI WDM");
    lemmasNames.put("EFH", "EUROPEJSKI FUNDUSZ HIPOTECZNY");
    lemmasNames.put("ELZAB", "ZAKŁADY URZĄDZEŃ KOMPUTEROWYCH ELZAB");
    lemmasNames.put("EMERYTALNE PZU", "POWSZECHNY ZAKŁAD UBEZPIECZEŃ");
    lemmasNames.put("EUROPEJSKIEGO FUNDUSZ HIPOTECZNEGO", "EUROPEJSKI FUNDUSZ HIPOTECZNY");
    lemmasNames.put("FAMUR", "FABRYKA MASZYN FAMUR");
    lemmasNames.put("FON SE", "FON");
    lemmasNames.put("FOTA", "FOTA SA W UPADŁOŚCI UKŁADOWEJ");
    lemmasNames.put("GROCLIN", "INTER GROCLIN AUTO");
    lemmasNames.put("GRUPA AZOTY", "GRUPA AZOTY ZAKŁADY CHEMICZNE POLICE");
    lemmasNames.put("INVESTMENT FRIENDS", "FON");
    lemmasNames.put("INVESTMENT FRIENDS SE", "INVESTMENT FRIENDS SPÓŁKA EUROPEJSKA");
    lemmasNames.put("KOGENERACJA", "ZESPÓŁ ELEKTROCIEPŁOWNI WROCŁAWSKICH KOGENERACJA");
    lemmasNames.put("LENTEX", "ZAKŁADY LENTEX");
    lemmasNames.put("MEWA", "ZAKŁADY DZIEWIARSKIE MEWA");
    lemmasNames.put("MOSTOSTAL", "MOSTOSTAL ZABRZE HOLDING");
    lemmasNames.put("NAVIMOR INVEST", "ZAKŁAD BUDOWY MASZYN ZREMB-CHOJNICE");
    lemmasNames.put("OCTAVA", "NARODOWY FUNDUSZ INWESTYCYJNY OCTAVA");
    lemmasNames.put("P.A.NOVA", "P.A. NOVA");
    lemmasNames.put("P.R.I.POL-AQUA", "PRZEDSIĘBIORSTWO ROBÓT INŻYNIERYJNYCH POL-AQUA");
    lemmasNames.put("PAK-VOLT", "ZESPÓŁ ELEKTROWNI PĄTNÓW-ADAMÓW-KONIN");
    lemmasNames.put("PEKABEX", "POZNAŃSKA KORPORACJA BUDOWLANA PEKABEX");
    lemmasNames.put("PGNIG", "POLSKIE GÓRNICTWO NAFTOWE I GAZOWNICTWO");
    lemmasNames.put("PKN ORLEN", "POLSKI KONCERN NAFTOWY ORLEN");
    lemmasNames.put("PONAR", "PONAR - WADOWICE");
    lemmasNames.put("PPWK", "PPWK. IM E. ROMERA");
    lemmasNames.put("PRZEDSIĘBIORSTWO HYDRAULIKI SIŁOWEJ HYDROTOR", "PRZEDSIĘBIORSTWO HYDRAULIKI SIŁOWEJ 'HYDROTOR'");
    lemmasNames.put("PSR KORPORACJI GOSPODARCZEJ EFEKT", "KORPORACJA GOSPODARCZA EFEKT");
    lemmasNames.put("PSR KORPORACJI GOSPODARCZEJ EFEKT", "KORPORACJA GOSPODARCZA EFEKT");
    lemmasNames.put("RAFAKO", "FABRYKA KOTŁÓW RAFAKO");
    lemmasNames.put("RAFAMET", "FABRYKA OBRABIAREK RAFAMET");
    lemmasNames.put("SEGMENT SIEDZIBA W INVESTMENTS", "W INVESTMENTS");
    lemmasNames.put("STOMIL SANOK", "SZPG STOMIL SANOK");
    lemmasNames.put("SWISSMED", "SWISSMED CENTRUM ZDROWIA");
    lemmasNames.put("TOWARZYSTWO FINANSOWEGO SKOK", "TOWARZYSTWO FINANSOWE SPÓŁDZIELCZYCH KAS OSZCZĘDNOŚCIOWO-KREDYTOWYCH");
    lemmasNames.put("TRIGON TFI", "BAKALLAND");
    lemmasNames.put("ULMA CONSTRUCCION POLSKA", "ULMA CONSTRUCCION");
    lemmasNames.put("UNIMA2000", "UNIMA 2000 SYSTEMY TELEINFORMATYCZNE");
    lemmasNames.put("WSIP ORAZ WSIP", "WYDAWNICTWA SZKOLNE I PEDAGOGICZNE");
    lemmasNames.put("ZAKŁADÓW LENTEX", "ZAKŁADY LENTEX");
    lemmasNames.put("ZAKŁADY CHEMICZNE POLICE", "GRUPA AZOTY ZAKŁADY CHEMICZNE POLICE");
    lemmasNames.put("ZM ROPCZYCE", "ZAKŁADY MAGNEZYTOWE ROPCZYCE");
    lemmasNames.put("ZREMB CHOJNICE", "ZAKŁAD BUDOWY MASZYN ZREMB-CHOJNICE");

    ignore.add("PÓŁROCZNY");
    ignore.add("DOMINUJĄCA");
    ignore.add("SPÓŁKI");
    ignore.add("FIRMY");
    ignore.add("UDZIAŁ");
    ignore.add("%");
  }

  public String lemmatize(final String text) {
    final String name = Arrays.stream(suffix.stripFromName(text).split(" "))
        .map(orth -> lemmasWords.getOrDefault(orth, orth))
        .filter(orth -> !ignore.contains(orth))
        .collect(Collectors.joining(" "));
    return lemmasNames.getOrDefault(name, name);
  }
}
