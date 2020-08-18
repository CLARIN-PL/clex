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

    lemmas.put("0", "1");
    lemmas.put("-GRAAL_SA", "GRAAL_SA");
    lemmas.put(".BUDOPOL-WROCŁAW_SA", "BUDOPOL-WROCŁAW_SA");
    lemmas.put(".PZU_SA", "POWSZECHNY_ZAKŁAD_UBEZPIECZEŃ_SA");
    lemmas.put("4_FUN_MEDIA_SA", "4FUN_MEDIA_SA");
    lemmas.put("AD.DRĄGOWSKI_SA", "AD.DRĄGOWSKI_SPOŁKA_AKCYJNA");
    lemmas.put("AMREST_HOLDINGS_SE_SA", "AMREST_HOLDINGS_SA");
    lemmas.put("ARMATURA_SA", "ARMATURA_KRAKÓW_SA");
    lemmas.put("ASSECO_SA", "ASSECO_POLAND_SA");
    lemmas.put("ATM_SA", "ATM_SYSTEMY_INFORMATYCZNE_SA");
    lemmas.put("AZOTY_SA", "GRUPA_AZOTY_SA");
    lemmas.put("BANK_MILLENNIUM_SA", "ZAKŁADY_MIĘSNE_HENRYK_KANIA_SA");
    lemmas.put("CASH_FLOW_SA", "CASH_FLOW_SPÓŁA_AKCYJNA");
    lemmas.put("CENTRUM_GIEŁDOWE_SA", "GIEŁDA_PAPIERÓW_WARTOŚCIOWYCH_W_WARSZAWIE_SA");
    lemmas.put("CNT_SA", "CENTRUM_NOWOCZESNYCH_TECHNOLOGII_SA");
    lemmas.put("COLIAN_HOLDING_SA", "COLIAN_HOLDINGS_SA");
    lemmas.put("DOM_MAKLERSKI_WDM_SA", "DOM_MAKLERSKI_WDM_S._A.");
    lemmas.put("EFH_SA", "EUROPEJSKI_FUNDUSZ_HIPOTECZNY_SA");
    lemmas.put("ELZAB_SA", "ZAKŁADY_URZĄDZEŃ_KOMPUTEROWYCH_ELZAB_SA");
    lemmas.put("EMERYTALNE_PZU_SA", "POWSZECHNY_ZAKŁAD_UBEZPIECZEŃ_SA");
    lemmas.put("EUROPEJSKIEGO_FUNDUSZ_HIPOTECZNEGO_SA", "EUROPEJSKI_FUNDUSZ_HIPOTECZNY_SA");
    lemmas.put("FAMUR_SA", "FABRYKA_MASZYN_FAMUR_SA");
    lemmas.put("FON_SE_SA", "FON_SA");
    lemmas.put("FOTA_SA", "FOTA_SA_W_UPADŁOŚCI_UKŁADOWEJ");
    lemmas.put("GROCLIN_SA", "INTER_GROCLIN_AUTO_SA");
    lemmas.put("GRUPA_AZOTY_SA", "GRUPA_AZOTY_ZAKŁADY_CHEMICZNE_POLICE_SA");
    lemmas.put("INVESTMENT_FRIENDS_SA", "FON_SA");
    lemmas.put("INVESTMENT_FRIENDS_SE_SA", "INVESTMENT_FRIENDS_SPÓŁKA_EUROPEJSKA");
    lemmas.put("KOGENERACJA_SA", "ZESPÓŁ_ELEKTROCIEPŁOWNI_WROCŁAWSKICH_KOGENERACJA_SA");
    lemmas.put("LENTEX_SA", "ZAKŁADY_LENTEX_SA");
    lemmas.put("MEWA_SA", "ZAKŁADY_DZIEWIARSKIE_MEWA_SA");
    lemmas.put("MOSTOSTAL_SA", "MOSTOSTAL_ZABRZE_HOLDING_SA");
    lemmas.put("NAVIMOR_INVEST_SA", "ZAKŁAD_BUDOWY_MASZYN_ZREMB-CHOJNICE_SA");
    lemmas.put("OCTAVA_SA", "NARODOWY_FUNDUSZ_INWESTYCYJNY_OCTAVA_SA");
    lemmas.put("P.A.NOVA_SA", "P.A._NOVA_SA");
    lemmas.put("P.R.I.POL-AQUA_SA", "PRZEDSIĘBIORSTWO_ROBÓT_INŻYNIERYJNYCH_POL-AQUA_SA");
    lemmas.put("PAK-VOLT_SA", "ZESPÓŁ_ELEKTROWNI_PĄTNÓW-ADAMÓW-KONIN_SA");
    lemmas.put("PEKABEX_SA", "POZNAŃSKA_KORPORACJA_BUDOWLANA_PEKABEX_SA");
    lemmas.put("PGNIG_SA", "POLSKIE_GÓRNICTWO_NAFTOWE_I_GAZOWNICTWO_SA");
    lemmas.put("PKN_ORLEN_SA", "POLSKI_KONCERN_NAFTOWY_ORLEN_SA");
    lemmas.put("PONAR_SA", "PONAR_-_WADOWICE_SA");
    lemmas.put("PPWK_SA", "PPWK._IM_E._ROMERA_SA");
    lemmas.put("PRZEDSIĘBIORSTWO_HYDRAULIKI_SIŁOWEJ_HYDROTOR_SA", "PRZEDSIĘBIORSTWO_HYDRAULIKI_SIŁOWEJ_'HYDROTOR'_SA");
//lemmas.put("PSR_2008_SA","HYPERION_SA");
//lemmas.put("PSR_KORPORACJI_GOSPODARCZEJ_EFEKT_SA","KORPORACJA_GOSPODARCZA_EFEKT_SA");
//lemmas.put("PSR_KORPORACJI_GOSPODARCZEJ_EFEKT_SA","KORPORACJA_GOSPODARCZA_EFEKT_SA");
    lemmas.put("RAFAKO_SA", "FABRYKA_KOTŁÓW_RAFAKO_SA");
    lemmas.put("RAFAMET_SA", "FABRYKA_OBRABIAREK_RAFAMET_SA");
//lemmas.put("SEGMENT_SIEDZIBA_W_INVESTMENTS_SA", "W_INVESTMENTS_SA");
    lemmas.put("STOMIL_SANOK_SA", "SZPG_STOMIL_SANOK_SA");
    lemmas.put("SWISSMED_SA", "SWISSMED_CENTRUM_ZDROWIA_SA");
    lemmas.put("TOWARZYSTWO_FINANSOWEGO_SKOK_SA", "TOWARZYSTWO_FINANSOWE_SPÓŁDZIELCZYCH_KAS_OSZCZĘDNOŚCIOWO-KREDYTOWYCH_SA");
    lemmas.put("TRIGON_TFI_SA", "BAKALLAND_SA");
    lemmas.put("ULMA_CONSTRUCCION_POLSKA_SA", "ULMA_CONSTRUCCION_SA");
    lemmas.put("UNIMA2000_SA", "UNIMA_2000_SYSTEMY_TELEINFORMATYCZNE_S.A");
    lemmas.put("WSIP_ORAZ_WSIP_SA", "WYDAWNICTWA_SZKOLNE_I_PEDAGOGICZNE_SA");
    lemmas.put("ZAKŁADÓW_LENTEX_SA", "ZAKŁADY_LENTEX_SA");
    lemmas.put("ZAKŁADY_CHEMICZNE_POLICE_SA", "GRUPA_AZOTY_ZAKŁADY_CHEMICZNE_POLICE_SA");
    lemmas.put("ZM_ROPCZYCE_SA", "ZAKŁADY_MAGNEZYTOWE_ROPCZYCE_SA");
    lemmas.put("ZREMB_CHOJNICE_SA", "ZAKŁAD_BUDOWY_MASZYN_ZREMB-CHOJNICE_SA");


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
