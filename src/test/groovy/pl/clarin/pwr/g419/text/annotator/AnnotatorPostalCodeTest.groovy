package pl.clarin.pwr.g419.text.annotator


import pl.clarin.pwr.g419.struct.HocrPage
import pl.clarin.pwr.g419.utils.TestUtils
import spock.lang.Specification
import spock.lang.Unroll

class AnnotatorPostalCodeTest extends Specification {

    @Unroll
    def "annotate on '#text' should return '#norm"() {
        given:
            def page = new HocrPage(null, TestUtils.getSequenceOfBboxes(text))
            def annotatorPostalCode = new AnnotatorPostalCode()

        when:
            annotatorPostalCode.annotate(page)


        then:
            page.getAnnotations().stream().filter { an -> an.getType() == "postal_code" }
                    .collect { it.getNorm() } as Set == norm as Set

        where:
            text                  || norm
            "94-250"              || ["94-250"]  // normalny zmian "-" w input
            "94‐250"              || ["94-250"]  // inny znak "-" w input
            "33-100"              || ["33-100"]
            "333-100"             || []
            "33-1000"             || []
            "Strzegomska, 33-100" || ["33-100"]
            // TODO - dlaczego ?
            //    "Strzegomska,33-100 " || ["33-100"]
            //"33-100b"             || ["33-100"]
            "33-100 "             || ["33-100"]
            //    ",33-100 "            || ["33-100"]
            //    "33-100,"             || ["33-100"]


    }

}
