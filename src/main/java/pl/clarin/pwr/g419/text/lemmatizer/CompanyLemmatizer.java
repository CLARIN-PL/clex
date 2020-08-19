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

//    TODO : czy takie rozwinięcia pomagają ?
    lemmas.put("PKO", "POWSZECHNA KASA OSZCZĘDNOŚCI");
    lemmas.put("DM", "DOM MAKLERSKI");
    lemmas.put("PTI", "POWSZECHNE TOWARZYSTWO INWESTYCYJNE");
    lemmas.put("BOŚ", "BANK OCHRONY ŚRODOWISKA");
    lemmas.put("PEKAO", "POLSKA KASA OPIEKI");
    lemmas.put("PBKM", "POLSKI BANK KOMÓREK MACIERZYSTYCH");

// z arkusza :
    lemmas.put("", "POWSZECHNA KASA OSZCZĘDNOŚCI");


    lemmas.put("- GRAAL SA", "GRAAL SA");
    lemmas.put(".BUDOPOL-WROCŁAW SA", "BUDOPOL-WROCŁAW SA");
    lemmas.put(".PZU SA", "POWSZECHNY ZAKŁAD UBEZPIECZEŃ SA");
    lemmas.put("4 FUN MEDIA SA", "4FUN MEDIA SA");
    lemmas.put("AD.DRĄGOWSKI SA", "AD.DRĄGOWSKI SPOŁKA AKCYJNA");
    lemmas.put("AMREST HOLDINGS SE SA", "AMREST HOLDINGS SA");
    lemmas.put("ARMATURA SA", "ARMATURA KRAKÓW SA");
    lemmas.put("ASSECO SA", "ASSECO POLAND SA");
    lemmas.put("ATM SA", "ATM SYSTEMY INFORMATYCZNE SA");
    lemmas.put("AZOTY SA", "GRUPA AZOTY SA");
    lemmas.put("BANK MILLENNIUM SA", "ZAKŁADY MIĘSNE HENRYK KANIA SA");
    lemmas.put("CASH FLOW SA", "CASH FLOW SPÓŁA AKCYJNA");
    lemmas.put("CENTRUM GIEŁDOWE SA", "GIEŁDA PAPIERÓW WARTOŚCIOWYCH W WARSZAWIE SA");
    lemmas.put("CNT SA", "CENTRUM NOWOCZESNYCH TECHNOLOGII SA");
    lemmas.put("COLIAN HOLDING SA", "COLIAN HOLDINGS SA");
    lemmas.put("DOM MAKLERSKI WDM SA", "DOM MAKLERSKI WDM S. A.");
    lemmas.put("EFH SA", "EUROPEJSKI FUNDUSZ HIPOTECZNY SA");
    lemmas.put("ELZAB SA", "ZAKŁADY URZĄDZEŃ KOMPUTEROWYCH ELZAB SA");
    lemmas.put("EMERYTALNE PZU SA", "POWSZECHNY ZAKŁAD UBEZPIECZEŃ SA");
    lemmas.put("EUROPEJSKIEGO FUNDUSZ HIPOTECZNEGO SA", "EUROPEJSKI FUNDUSZ HIPOTECZNY SA");
    lemmas.put("FAMUR SA", "FABRYKA MASZYN FAMUR SA");
    lemmas.put("FON SE SA", "FON SA");
    lemmas.put("FOTA SA", "FOTA SA W UPADŁOŚCI UKŁADOWEJ");
    lemmas.put("GROCLIN SA", "INTER GROCLIN AUTO SA");
    lemmas.put("GRUPA AZOTY SA", "GRUPA AZOTY ZAKŁADY CHEMICZNE POLICE SA");
    lemmas.put("INVESTMENT FRIENDS SA", "FON SA");
    lemmas.put("INVESTMENT FRIENDS SE SA", "INVESTMENT FRIENDS SPÓŁKA EUROPEJSKA");
    lemmas.put("KOGENERACJA SA", "ZESPÓŁ ELEKTROCIEPŁOWNI WROCŁAWSKICH KOGENERACJA SA");
    lemmas.put("LENTEX SA", "ZAKŁADY LENTEX SA");
    lemmas.put("MEWA SA", "ZAKŁADY DZIEWIARSKIE MEWA SA");
    lemmas.put("MOSTOSTAL SA", "MOSTOSTAL ZABRZE HOLDING SA");
    lemmas.put("NAVIMOR INVEST SA", "ZAKŁAD BUDOWY MASZYN ZREMB-CHOJNICE SA");
    lemmas.put("OCTAVA SA", "NARODOWY FUNDUSZ INWESTYCYJNY OCTAVA SA");
    lemmas.put("P.A.NOVA SA", "P.A. NOVA SA");
    lemmas.put("P.R.I.POL-AQUA SA", "PRZEDSIĘBIORSTWO ROBÓT INŻYNIERYJNYCH POL-AQUA SA");
    lemmas.put("PAK-VOLT SA", "ZESPÓŁ ELEKTROWNI PĄTNÓW-ADAMÓW-KONIN SA");
    lemmas.put("PEKABEX SA", "POZNAŃSKA KORPORACJA BUDOWLANA PEKABEX SA");
    lemmas.put("PGNIG SA", "POLSKIE GÓRNICTWO NAFTOWE I GAZOWNICTWO SA");
    lemmas.put("PKN ORLEN SA", "POLSKI KONCERN NAFTOWY ORLEN SA");
    lemmas.put("PONAR SA", "PONAR - WADOWICE SA");
    lemmas.put("PPWK SA", "PPWK. IM E. ROMERA SA");
    lemmas.put("PRZEDSIĘBIORSTWO HYDRAULIKI SIŁOWEJ HYDROTOR SA", "PRZEDSIĘBIORSTWO HYDRAULIKI SIŁOWEJ 'HYDROTOR' SA");
//lemmas.put("PSR 2008 SA","HYPERION SA");
//lemmas.put("PSR KORPORACJI GOSPODARCZEJ EFEKT SA","KORPORACJA GOSPODARCZA EFEKT SA");
//lemmas.put("PSR KORPORACJI GOSPODARCZEJ EFEKT SA","KORPORACJA GOSPODARCZA EFEKT SA");
    lemmas.put("RAFAKO SA", "FABRYKA KOTŁÓW RAFAKO SA");
    lemmas.put("RAFAMET SA", "FABRYKA OBRABIAREK RAFAMET SA");
//lemmas.put("SEGMENT SIEDZIBA W INVESTMENTS SA", "W INVESTMENTS SA");
    lemmas.put("STOMIL SANOK SA", "SZPG STOMIL SANOK SA");
    lemmas.put("SWISSMED SA", "SWISSMED CENTRUM ZDROWIA SA");
    lemmas.put("TOWARZYSTWO FINANSOWEGO SKOK SA", "TOWARZYSTWO FINANSOWE SPÓŁDZIELCZYCH KAS OSZCZĘDNOŚCIOWO-KREDYTOWYCH SA");
    lemmas.put("TRIGON TFI SA", "BAKALLAND SA");
    lemmas.put("ULMA CONSTRUCCION POLSKA SA", "ULMA CONSTRUCCION SA");
    lemmas.put("UNIMA2000 SA", "UNIMA 2000 SYSTEMY TELEINFORMATYCZNE S.A");
    lemmas.put("WSIP ORAZ WSIP SA", "WYDAWNICTWA SZKOLNE I PEDAGOGICZNE SA");
    lemmas.put("ZAKŁADÓW LENTEX SA", "ZAKŁADY LENTEX SA");
    lemmas.put("ZAKŁADY CHEMICZNE POLICE SA", "GRUPA AZOTY ZAKŁADY CHEMICZNE POLICE SA");
    lemmas.put("ZM ROPCZYCE SA", "ZAKŁADY MAGNEZYTOWE ROPCZYCE SA");
    lemmas.put("ZREMB CHOJNICE SA", "ZAKŁAD BUDOWY MASZYN ZREMB-CHOJNICE SA");


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
