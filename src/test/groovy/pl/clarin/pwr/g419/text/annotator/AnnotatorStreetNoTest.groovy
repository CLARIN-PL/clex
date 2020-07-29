package pl.clarin.pwr.g419.text.annotator


import pl.clarin.pwr.g419.struct.HocrPage
import pl.clarin.pwr.g419.utils.TestUtils
import spock.lang.Specification
import spock.lang.Unroll

class AnnotatorStreetNoTest extends Specification {

    @Unroll
    def "annotate on '#text' should return #norm "() {
        given:
            def page = new HocrPage(null, TestUtils.getSequenceOfBboxes(text))
            def annotatorStreetNo = new AnnotatorStreetNo()

        when:
            annotatorStreetNo.annotate(page)

        then:
            page.getAnnotations().stream().filter { an -> an.getType() == "street_no" }
                    .collect { it.getNorm() } as Set == norm as Set

        where:
            text                                 || norm
            "ul. Słowiańska 15 we Wrocławiu"     || ["15"]
            "ul. Słowiańska 9 we Wrocławiu"      || ["9"]
            "ul. Słowiańska  159  we Wrocławiu"  || ["159"]
            "ul. Słowiańska  4321  we Wrocławiu" || []

            "ul. Słowiańska  we Wrocławiu"       || []


    }

}
