package pl.clarin.pwr.g419.text.annotator;


import pl.clarin.pwr.g419.struct.HocrPage
import pl.clarin.pwr.g419.utils.TestUtils
import spock.lang.Specification
import spock.lang.Unroll

class AnnotatorStreetNrLokTest extends Specification {

    @Unroll
    def "annotate on '#text' should return '#norm "() {
        given:
            def page = new HocrPage(null, TestUtils.getSequenceOfBboxes(text))
            def annotatorStreetNrLok = new AnnotatorStreetNrLok()

        when:
            annotatorStreetNrLok.annotate(page)


        then:
            page.getAnnotations().stream().filter { an -> an.getType() == AnnotatorStreetNrLok.STREET_NR_LOK }
                    .collect { it.getNorm() } as Set == norm as Set

        where:
            text                           || norm
            "LOK . 4 "                     || ["LOK. 4"]
            "lok . 46 "                    || ["lok. 46"]
            "lok . 46A "                   || ["lok. 46A"]
            "ul. Grajewskiego lok . 102D " || ["lok. 102D"]
            "ul. Grajewskiego lok . U-4 "  || ["lok. U-4"]

            // nie do rozpoznania ze względu na to że może to być drugi wyraz ulicy
            //"ul. Grajewskiego Lok . U-4 "  || ["lok. U-4"]
    }
}