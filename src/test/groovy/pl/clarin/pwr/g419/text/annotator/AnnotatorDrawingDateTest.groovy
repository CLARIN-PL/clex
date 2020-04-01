package pl.clarin.pwr.g419.text.annotator

import pl.clarin.pwr.g419.struct.HocrPage
import pl.clarin.pwr.g419.utils.TestUtils
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class AnnotatorDrawingDateTest extends Specification {

    @Subject
    def annotator = new AnnotatorDrawingDate()

    @Unroll
    def "annotate on '#text' should return '#norm"() {
        given:
            def page = new HocrPage(TestUtils.getSequenceOfBboxes(text))
            def annotatorDate = new AnnotatorDate()
            annotatorDate.annotate(page)

        when:
            annotator.annotate(page)

        then:
            page.getAnnotations().stream().filter { an -> an.getType() == "drawing_date" }
                    .collect { it.getNorm() } as Set == [norm] as Set

        where:
            text                                  || norm
            "Katowice, dnia 2 września 2005 roku" || "2005-09-02"

    }
}
