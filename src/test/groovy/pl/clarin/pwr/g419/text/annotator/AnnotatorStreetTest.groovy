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
            def annotatorStreet = new AnnotatorStreet()

        when:
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

            // bo mała litera na początku następnego słowa delimituje nazwę !!!!! :-)

    }

}
