package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import pl.clarin.pwr.g419.kbase.lexicon.CityLexicon;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherTwoWordsSequenceInSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AnnotatorCity extends Annotator {

  public static String CITY = "city";

  // bierzemy tylko największe 2000 miast - nawet
  // niektóre z nich są oznaczone postfixem "DELETE" by nie brać ich pod uwagę
  public static CityLexicon cityLexicon = new CityLexicon(2000);

  private static List<Pattern> getPatterns() {

    final Set<String> cityNames = Sets.newHashSet();

    cityNames.addAll(cityLexicon);
    // uwzględniamy także miasta pisane całe wielkimi literami
    cityNames.addAll(cityNames.stream().map(String::toUpperCase).collect(Collectors.toList()));

    final List<Pattern> patterns = Lists.newArrayList();

    patterns.add(new Pattern("city1")
        .next(new MatcherTwoWordsSequenceInSet(cityNames).group(CITY)));


    // TODO -- "z siedzibą w Kuźni Raciborskiej" doc.id. 118609, odmiana miast
    // === "z siedzibą w Grajewie" doc.id 118721, odmiana miast
    // Siedziba:
    // Siedzaibą jest :
    // zadanym okresie siedziba Spółki dominującej mieściła się w Warszawie, przy ul. Gotarda 9. doc. 118724
    // Adres siedziby :   ul. Jasielska 16 , 60-476 Poznań doc. 118734
    // DZ BANK Polska S.A. z siedzibą w Warszawie przy ul. Pl. Piłsudskiego 3 zarejest doc.id 118739
    // Siedzibą podmiotu dominującego jest Tuchola. doc.id 118754

    // NKT Cables S.A. – podmiot dominujący; ul. Gajowa 3, Warszowice; - nie ma kodu

    //Jednostce dominującej nadano numer statystyczny REGON 390453104. Siedziba Spółki mieści się w Legnicy, przy ulicy
    //Rynek 28.  doc.id 118762

    //APATOR SA wpisany jest do Krajowego Rejestru Sądowego,
    //Rejestru Przedsiębiorców pod nr KRS 0000056456. Siedziba spółki zlokalizowana jest w Toruniu przy
    //ul. Żółkiewskiego 21/29  - doc.id 118811

    //GINO ROSSI Spółka Akcyjna w Słupsku, ul. Owocowa 24, zarejestrowana - doc.id 118864

    //Spółka dominująca Grupy Kapitałowej Wola Info S.A. z siedzibą w Warszawie została utworzona uchwałą - doc.od 118867

    //Siedziba: Warszawa
    //Adres: 02-305 Warszawa, Al. Jerozolimskie 136  doc.id 118902

    //WASKO S.A. została utworzona Aktem Notarialnym z dnia 16.11.1999 roku. Siedziba Spółki
    //mieści się w Gliwicach, ul. Berbeckiego 6. doc.id 118923

    // Siedziba:  Wejherowo, ul. Zachodnia 22 - doc.id 118940

    // Siedziba i adres: ul. Ludźmierska 29, 34-300 Nowy Targ // doc.id 118946

    // Firma Handlowa Jago Spółka Akcyjna,
    //z siedzibą w Krzeszowicach, ul. Daszyńskiego 10A, 32-056 Krzeszowice  - doc.id 118946

    // Informacje o Podmiocie Dominującym
    //NETMEDIA S.A. została wpisana do Krajowego Rejestru Sądowego pod numerem 0000259747.
    //Siedziba Spółki mieści się w Warszawie przy ulicy Woronicza nr 15  - doc.id 118950

    //w której jednostką dominującą jest
    //Narodowy Fundusz Inwestycyjny „Midas” S.A. z siedzibą w Warszawie przy Al. Jana Pawła II 29, - doc.id 119245


    //ednostka dominująca Cognor S.A. (”jednostka dominująca”, „spółka dominująca”, ”Spółka”) z
    //siedzibą w Gdańsku przy ul. Budowlanych 42  - doc.id 119858

    // jednostką dominującą”) z siedzibą w
    //Warszawie przy ulicy Poleczki 13, obejmującego: doc.id 13615


    return patterns;
  }

  public AnnotatorCity() {
    super(CITY, getPatterns());
  }

  @Override
  protected String normalize(final PatternMatch pm) {
    return pm.getGroupValue(CITY).orElse(pm.getText());
  }

}
