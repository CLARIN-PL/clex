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
            text                                               || norm
            "ul. Słowiańska "                                  || ["Słowiańska:"]
            " znajduje się na ul. Słowiańskiej we Wrocławiu"   || ["Słowiańska:"]
            " mieści się przy ul. Słowiańskiej we Wrocławiu"   || ["Słowiańska:"]
            " znajduje się ul. Opolska we Wrocławiu"           || ["Opolska:"]
            " znajduje się przy ul. Opolskiej na Nowym Dworze" || ["Opolska:"]
            " znajduje się przy ul. Hutniczej we Warszawie"    || ["Hutnicza:"]
            " zlokalizowane jest przy ul. Znojnej"             || ["Znojna:"]
            "al. Brygadzistów "                                || ["Brygadzistów:"]
            " jest na al. Nyskiej "                            || ["Nyska:"]

            "ul. Słowiańska 15 we Wrocławiu"                   || ["Słowiańska:15"]
            "przy ul. Opolskiej 21 we Wrocławiu"               || ["Opolska:21"]
            " na ul. Hutniczej 134 w Poznaniu "                || ["Hutnicza:134"]

            "ul. Obrońców Westerplatte "                       || ["Obrońców Westerplatte:"]

            "ul. Słowiańska 13b"                               || ["Słowiańska:13b"]
            "ul. Słowiańska 19A"                               || ["Słowiańska:19A"]

            "ul. Słowiańska 13/19"                             || ["Słowiańska:13/19"]
            "ul. Słowiańska 13D/19"                            || ["Słowiańska:13D/19"]
            "na ul. Opolskiej 2-4"                             || ["Opolska:2-4"]
            ///"przy ul. Łąkowej 39-44"                           || ["Łąkowa:39-44"]  -- Orzeszkowej
            "przy ul. Kasprzaka 39-44"                         || ["Kasprzaka:39-44"]
            "przy ul. Kasprzaka 302-304 ."                     || ["Kasprzaka:302-304"]
            "przy ul. Mihai Bravu 302-304 ."                   || ["Mihai Bravu:302-304"]
            "(ul. Gdańska 4a  )"                               || ["Gdańska:4a"]
            "(ul. Gdańska 4a lok.  )"                          || ["Gdańska:4a lok."]   /// !!!!
// TODO     "(ul. Gdańska 4a lok C4 )"                         || ["Gdańska:4a lok C4"]    - literka w nr. lokalu pierwsza
// TODO     "(ul. Gdańska 4a lok. C4 )"                        || ["Gdańska:4a lok. C4"]   - literka pierwsza w nr. lokalu

            "ul. Słowiańska 19A-19D"                           || ["Słowiańska:19A-19D"]
            "ul. Słowiańska 19 lok. 524"                       || ["Słowiańska:19 lok. 524"]
            "ul. Słowiańska 18 lok. 3b"                        || ["Słowiańska:18 lok. 3b"]
            "przy ulicy Kasprzaka 39-44"                       || ["Kasprzaka:39-44"]
            "przy alei Bohaterów Getta 39a-44c"                || ["Bohaterów Getta:39a-44c"]


            // bo mała litera na początku następnego słowa delimituje nazwę !!!!! :-)

    }

}
