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
            def annotatorStreetNameStartAbbreviation = new AnnotatorStreetNameStartAbbreviation()
            def annotatorStreetNrLok = new AnnotatorStreetNrLok()
            def annotatorStreetOnly = new AnnotatorStreetOnly()
            def annotatorStreet = new AnnotatorStreet()

        when:
            annotatorStreetNameStartAbbreviation.annotate(page)
            annotatorStreetOnly.annotate(page)
            annotatorStreetNrLok.annotate(page)
            annotatorStreet.annotate(page)


        then:
            page.getAnnotations().stream().filter { an -> an.getType() == "street" }
                    .collect { it.getNorm() } as Set == norm as Set

        where:
            text                                                   || norm
            "ul. Słowiańska "                                      || ["Słowiańska:"]
            " znajduje się na ul. Słowiańskiej we Wrocławiu"       || ["Słowiańska:"]
            " mieści się przy ul. Słowiańskiej we Wrocławiu"       || ["Słowiańska:"]
            " znajduje się ul. Opolska we Wrocławiu"               || ["Opolska:"]
            " znajduje się przy ul. Opolskiej na Nowym Dworze"     || ["Opolska:"]
            " znajduje się przy ul. Hutniczej we Warszawie"        || ["Hutnicza:"]
            " zlokalizowane jest przy ul. Znojnej"                 || ["Znojna:"]
            "al. Brygadzistów "                                    || ["al. Brygadzistów:"]
            " jest na al. Nyskiej "                                || ["al. Nyska:"]
            "ul. Słowiańska 15 we Wrocławiu"                       || ["Słowiańska:15"]
            "przy ul. Opolskiej 21 we Wrocławiu"                   || ["Opolska:21"]
            " na ul. Hutniczej 134 w Poznaniu "                    || ["Hutnicza:134"]
            " na ul. Hutniczej 134W w Poznaniu "                   || ["Hutnicza:134W"]
            "ul. Obrońców Westerplatte "                           || ["Obrońców Westerplatte:"]
            "ul. Słowiańska 13b"                                   || ["Słowiańska:13b"]
            "ul. Słowiańska 19A"                                   || ["Słowiańska:19A"]
            "ul. Słowiańska 13/19"                                 || ["Słowiańska:13/19"]
            "ul. Słowiańska 13D/19"                                || ["Słowiańska:13D/19"]
            "na ul. Opolskiej 2-4"                                 || ["Opolska:2-4"]
            "przy ul. Kasprzaka 39-44"                             || ["Kasprzaka:39-44"]
            "przy ul. Kasprzaka 302-304 ."                         || ["Kasprzaka:302-304"]
            "przy ul. Mihai Bravu 302-304 ."                       || ["Mihai Bravu:302-304"]
            "(ul. Gdańska 4a  )"                                   || ["Gdańska:4a"]
            "(ul. Gdańska 4a lok.  )"                              || ["Gdańska:4a lok."]   /// !!!!
            "(ul. Gdańska 4a lok C4 )"                             || ["Gdańska:4a lok C4"]
            "(ul. Gdańska 4a lok. C4 )"                            || ["Gdańska:4a lok. C4"]
            "(ul. Armii Krajowej 4a lok . C4 )"                    || ["Armii Krajowej:4a lok. C4"]
            "ul. Słowiańska 19A-19D"                               || ["Słowiańska:19A-19D"]
            "ul. Słowiańska 19 lok. 524"                           || ["Słowiańska:19 lok. 524"]
            "ul. Słowiańska 18 lok. 3b"                            || ["Słowiańska:18 lok. 3b"]
            "ul. Słowiańska 18 lok. 3 i następnie ..."             || ["Słowiańska:18 lok. 3"]
            "ul. Słowiańska 18 lok. 3B"                            || ["Słowiańska:18 lok. 3B"]
            "przy ulicy Kasprzaka 39-44"                           || ["Kasprzaka:39-44"]
            "przy alei Bohaterów Getta 39a-44c"                    || ["Bohaterów Getta:39a-44c"]
            "miejscem jest Poznań, ul. Komisji Edukacji Narodowej" || ["Komisji Edukacji Narodowej:"]
            "Opole, ul. Komuny Paryskiej 15 lok . 5c"              || ["Komuny Paryskiej:15 lok. 5c"]
            "w Warszawie przy ul. Tamka 16 lok. U-4"               || ["Tamka:16 lok. U-4"]
            "w Warszawie przy ul. Tamka 16 lok . U-4 ."            || ["Tamka:16 lok. U-4"]
            " ul. Józefa Hellera"                                  || ["Józefa Hellera:"]
            " ul. gen . Hellera"                                   || ["gen. Hellera:"]
            " ul. gen . Hellera 35"                                || ["gen. Hellera:35"]
            " ul. gen . Hellera 35c"                               || ["gen. Hellera:35c"]
            " ul. gen . Hellera 35 c"                              || ["gen. Hellera:35 c"]
            " przy ul. gen . Hellera"                              || ["gen. Hellera:"]
            " ul. św . Ducha 34 b"                                 || ["św. Ducha:34 b"]
            " przy ul. św . Ducha 34 b"                            || ["św. Ducha:34 b"]
            " przy ul. Gr "                                        || []
            " UL E W "                                             || []

    }

}
