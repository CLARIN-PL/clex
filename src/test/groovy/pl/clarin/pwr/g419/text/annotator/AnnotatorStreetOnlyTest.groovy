package pl.clarin.pwr.g419.text.annotator


import pl.clarin.pwr.g419.struct.HocrPage
import pl.clarin.pwr.g419.utils.TestUtils
import spock.lang.Specification
import spock.lang.Unroll

class AnnotatorStreetOnlyTest extends Specification {

    @Unroll
    def "annotate on '#text' should return '#norm "() {
        given:
            def page = new HocrPage(null, TestUtils.getSequenceOfBboxes(text))
            def annotatorStreetNameStartAbbreviation = new AnnotatorStreetNameStartAbbreviation()
            def annotatorStreetOnly = new AnnotatorStreetOnly()

        when:
            annotatorStreetNameStartAbbreviation.annotate(page)
            annotatorStreetOnly.annotate(page)

        then:
            page.getAnnotations().stream().filter { an -> an.getType() == AnnotatorStreetOnly.STREET_ONLY }
                    .collect { it.getNorm() } as Set == norm as Set

        where:
            text                                                               || norm
            "ul. Słowiańska "                                                  || ["Słowiańska"]
            " znajduje się na ul. Słowiańskiej we Wrocławiu"                   || ["Słowiańska"]
            " mieści się przy ul. Słowiańskiej we Wrocławiu"                   || ["Słowiańska"]
            " znajduje się ul. Opolska we Wrocławiu"                           || ["Opolska"]
            " znajduje się przy ul. Opolskiej na Nowym Dworze"                 || ["Opolska"]
            " znajduje się przy ul. Hutniczej we Warszawie"                    || ["Hutnicza"]
            " zlokalizowane jest przy ul. Znojnej"                             || ["Znojna"]
            "al. Brygadzistów "                                                || ["Brygadzistów"]
            " jest na al. Nyskiej "                                            || ["Nyska"]

            "ul. Słowiańska 15 we Wrocławiu"                                   || ["Słowiańska"]
            "przy ul. Opolskiej 21 we Wrocławiu"                               || ["Opolska"]
            " na ul. Hutniczej 134 w Poznaniu "                                || ["Hutnicza"]

            "ul. Obrońców Westerplatte "                                       || ["Obrońców Westerplatte"]

            "ul. Słowiańska 13b"                                               || ["Słowiańska"]
            "al Słowiańska 13b"                                                || ["Słowiańska"]
            "ul. Słowiańska 19A"                                               || ["Słowiańska"]

            "ul. Słowiańska 13/19"                                             || ["Słowiańska"]
            "ul. Słowiańska 13D/19"                                            || ["Słowiańska"]
            "na ul. Opolskiej 2-4"                                             || ["Opolska"]
            ///"przy ul. Łąkowej 39-44"                           || [ "Łąkowa"  -- Orzeszkowej]
            "przy ul. Kasprzaka 39-44"                                         || ["Kasprzaka"]
            "przy ul. Kasprzaka 302-304 ."                                     || ["Kasprzaka"]
            "przy ul. Mihai Bravu 302-304 ."                                   || ["Mihai Bravu"]
            "(ul. Gdańska 4a  )"                                               || ["Gdańska"]
            "(ul. Gdańska 4a lok.  )"                                          || ["Gdańska"]   /// !!!!
            "(ul. Gdańska 4a lok C4 )"                                         || ["Gdańska"]
            "(ul. Gdańska 4a lok. C4 )"                                        || ["Gdańska"]
            // właściwie takie jak poniżej wystepują w dokumentach
            "(ul. Armii Krajowej 4a lok . C4 )"                                || ["Armii Krajowej"]

            "ul. Słowiańska 19A-19D"                                           || ["Słowiańska"]
            "ul Słowiańska 19A-19D"                                            || ["Słowiańska"]
            "ul . Słowiańska 19A-19D"                                          || ["Słowiańska"]
            "ul. Słowiańska 19 lok. 524"                                       || ["Słowiańska"]
            "ul. Słowiańska 18 lok. 3b"                                        || ["Słowiańska"]
            "przy ulicy Kasprzaka 39-44"                                       || ["Kasprzaka"]
            "przy alei Bohaterów Getta 39a-44c"                                || ["Bohaterów Getta"]

            // przechodzą gdy jest włączona flaga isPrepositionPartiallyDeterminingLemmatization
            //"Adres siedziby Warszawa, ul. Orzeszkowej"                       || ["Orzeszkowej"]
            // "Wrocław, ul. Konopnickiej 23"                                  || ["Konopnickiej"]

            "miejscem jest Poznań, ul. Komisji Edukacji Narodowej"             || ["Komisji Edukacji Narodowej"]
            "adres mieści się w Poznaniu, przy ul. Komisji Edukacji Narodowej" || ["Komisji Edukacji Narodowej"]
            "Opole, ul. Komuny Paryskiej 15 lok . 5c"                          || ["Komuny Paryskiej"]
            "Opole, ul Komuny Paryskiej 15 lok . 5c"                           || ["Komuny Paryskiej"]
            "Wrocław, pl. Jana Pawła II"                                       || ["Jana Pawła II"]

            "w Warszawie przy ul. Tamka 16 lok. U-4"                           || ["Tamka"]
            "w Warszawie przy ul. Tamka 16 lok . U-4 ."                        || ["Tamka"]
            " ul. Józefa Hellera"                                              || ["Józefa Hellera"]
            " ul. gen . Hellera"                                               || ["gen. Hellera"]
            " ul. gen . Hellera 35"                                            || ["gen. Hellera"]
            " ul. gen . Hellera 35c"                                           || ["gen. Hellera"]
            " ul. gen . Hellera 35 c"                                          || ["gen. Hellera"]
            " ul gen . Hellera 35 c"                                           || ["gen. Hellera"]
            " przy ul. gen . Hellera"                                          || ["gen. Hellera"]
            " ul. św . Ducha 34 b"                                             || ["św. Ducha"]
            " przy ul. św . Ducha 34 b"                                        || ["św. Ducha"]
            " przy ul św . Ducha 34 b"                                         || ["św. Ducha"]
            " ul. Gr"                                                          || []
            " ul Gr"                                                           || []
            " Ul Gr"                                                           || []
            " UL Ic Am I"                                                      || []

            "ul. Prof . Michała Bobrzyńskiego 14"                              || ["Prof. Michała Bobrzyńskiego"]
    }

}
