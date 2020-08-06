package pl.clarin.pwr.g419.text.annotator


import pl.clarin.pwr.g419.struct.HocrPage
import pl.clarin.pwr.g419.utils.TestUtils
import spock.lang.Specification
import spock.lang.Unroll

class AnnotatorStreetTest extends Specification {

    @Unroll
    def "annotate on '#text' should return '#norm "() {
        given:
            def page = new HocrPage(null, TestUtils.getSequenceOfBboxes(text))
            def annotatorStreetOnly = new AnnotatorStreetOnly()
            def annotatorStreetNrLok = new AnnotatorStreetNrLok()
            def annotatorStreet = new AnnotatorStreet()

        when:
            annotatorStreetOnly.annotate(page)
            annotatorStreetNrLok.annotate(page)
            annotatorStreet.annotate(page)

        then:
            page.getAnnotations().stream().filter { an -> an.getType() == "street" }
                    .collect { it.getNorm() } as Set == norm as Set

        where:
            text                                                   || norm
            "ul. Słowiańska "                                      || ["SŁOWIAŃSKA:"]
            " znajduje się na ul. Słowiańskiej we Wrocławiu"       || ["SŁOWIAŃSKA:"]
            " mieści się przy ul. Słowiańskiej we Wrocławiu"       || ["SŁOWIAŃSKA:"]
            " znajduje się ul. Opolska we Wrocławiu"               || ["OPOLSKA:"]
            " znajduje się przy ul. Opolskiej na Nowym Dworze"     || ["OPOLSKA:"]
            " znajduje się przy ul. Hutniczej we Warszawie"        || ["HUTNICZA:"]
            " zlokalizowane jest przy ul. Znojnej"                 || ["ZNOJNA:"]
            "al. Brygadzistów "                                    || ["BRYGADZISTÓW:"]
            " jest na al. Nyskiej "                                || ["NYSKA:"]

            "ul. Słowiańska 15 we Wrocławiu"                       || ["SŁOWIAŃSKA:15"]
            "przy ul. Opolskiej 21 we Wrocławiu"                   || ["OPOLSKA:21"]
            " na ul. Hutniczej 134 w Poznaniu "                    || ["HUTNICZA:134"]

            "ul. Obrońców Westerplatte "                           || ["OBROŃCÓW WESTERPLATTE:"]

            "ul. Słowiańska 13b"                                   || ["SŁOWIAŃSKA:13b"]
            "ul. Słowiańska 19A"                                   || ["SŁOWIAŃSKA:19A"]

            "ul. Słowiańska 13/19"                                 || ["SŁOWIAŃSKA:13/19"]
            "ul. Słowiańska 13D/19"                                || ["SŁOWIAŃSKA:13D/19"]
            "na ul. Opolskiej 2-4"                                 || ["OPOLSKA:2-4"]
            ///"przy ul. Łąkowej 39-44"                           || [ "Łąkowa:39-44"  -- Orzeszkowej]
            "przy ul. Kasprzaka 39-44"                             || ["KASPRZAKA:39-44"]
            "przy ul. Kasprzaka 302-304 ."                         || ["KASPRZAKA:302-304"]
            "przy ul. Mihai Bravu 302-304 ."                       || ["MIHAI BRAVU:302-304"]
            "(ul. Gdańska 4a  )"                                   || ["GDAŃSKA:4a"]
            "(ul. Gdańska 4a lok.  )"                              || ["GDAŃSKA:4a lok."]   /// !!!!
            "(ul. Gdańska 4a lok C4 )"                             || ["GDAŃSKA:4a lok C4"]
            "(ul. Gdańska 4a lok. C4 )"                            || ["GDAŃSKA:4a lok. C4"]

            "ul. Słowiańska 19A-19D"                               || ["SŁOWIAŃSKA:19A-19D"]
            "ul. Słowiańska 19 lok. 524"                           || ["SŁOWIAŃSKA:19 lok. 524"]
            "ul. Słowiańska 18 lok. 3b"                            || ["SŁOWIAŃSKA:18 lok. 3b"]
            "przy ulicy Kasprzaka 39-44"                           || ["KASPRZAKA:39-44"]
            "przy alei Bohaterów Getta 39a-44c"                    || ["BOHATERÓW GETTA:39a-44c"]

            "Adres siedziby: Warszawa, ul. Orzeszkowej"            || ["ORZESZKOWEJ:"]
            "miejscem jest Poznań, ul. Komisji Edukacji Narodowej" || ["KOMISJI EDUKACJI NARODOWEJ:"]


            // bo mała litera na początku następnego słowa delimituje nazwę !!!!! :-)

    }

}
