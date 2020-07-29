package pl.clarin.pwr.g419.text.annotator


import pl.clarin.pwr.g419.struct.HocrPage
import pl.clarin.pwr.g419.utils.TestUtils
import spock.lang.Specification
import spock.lang.Unroll

class AnnotatorStreetTest extends Specification {

    @Unroll
    def "annotate on '#text' should return '#norm #number "() {
        given:
            def page = new HocrPage(null, TestUtils.getSequenceOfBboxes(text))
            def annotatorStreetPrefix = new AnnotatorStreetPrefix()
            def annotatorStreet = new AnnotatorStreet()
            def annotatorStreetNo = new AnnotatorStreetNo()

            annotatorStreetPrefix.annotate(page)
            annotatorStreetNo.annotate(page)

        when:
            annotatorStreet.annotate(page)

        then:
            page.getAnnotations().stream().filter { an -> an.getType() == "street" }
                    .collect { it.getNorm() } as Set == norm as Set
            page.getAnnotations().stream().filter { an -> an.getType() == "street_no" }
                    .collect { it.getNorm() } as Set == number as Set

        where:
            text                                               || norm                      | number
            "ul. Słowiańska "                                  || ["Słowiańska"]            | []
            " znajduje się na ul. Słowiańskiej we Wrocławiu"   || ["Słowiańska"]            | []
            " mieści się przy ul. Słowiańskiej we Wrocławiu"   || ["Słowiańska"]            | []
            " znajduje się ul. Opolska we Wrocławiu"           || ["Opolska"]               | []
            " znajduje się przy ul. Opolskiej na Nowym Dworze" || ["Opolska"]               | []
            " znajduje się przy ul. Hutniczej we Warszawie"    || ["Hutnicza"]              | []
            " zlokalizowane jest przy ul. Znojnej"             || ["Znojna"]                | []
            "al. Brygadzistów "                                || ["Brygadzistów"]          | []
            " jest na al. Nyskiej "                            || ["Nyska"]                 | []

            "ul. Słowiańska 15 we Wrocławiu"                   || ["Słowiańska"]            | ["15"]
            "przy ul. Opolskiej 21 we Wrocławiu"               || ["Opolska"]               | ["21"]
            " na ul. Hutniczej 134 w Poznaniu "                || ["Hutnicza"]              | ["134"]

            "ul. Obrońców Westerplatte "                       || ["Obrońców Westerplatte"] | []

            // bo mała litera delimituje nazwę !!!!! :-)

    }

}
