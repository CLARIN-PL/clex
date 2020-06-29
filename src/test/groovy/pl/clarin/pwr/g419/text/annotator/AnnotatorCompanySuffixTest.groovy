package pl.clarin.pwr.g419.text.annotator

import pl.clarin.pwr.g419.struct.HocrPage
import pl.clarin.pwr.g419.utils.TestUtils
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class AnnotatorCompanySuffixTest extends Specification {

    @Subject
    def annotator = new AnnotatorCompanySuffix()

    @Unroll
    def "annotate on '#text' should return '#norm"() {
        given:
            def page = new HocrPage(null, TestUtils.getSequenceOfBboxes(text))
            def annotatorDate = new AnnotatorDate()
            annotatorDate.annotate(page)

        when:
            annotator.annotate(page)

        then:
            page.getAnnotations().stream().filter { an -> an.getType() == annotator.COMPANY_SUFFIX }
                    .collect { it.getNorm() } as Set == [norm] as Set

        where:
            text             || norm
            "s.a."           || "s.a."
            "s.a ."          || "s.a ."
            "s . a ."        || "s . a ."
            "Spółka Akcyjna" || "Spółka Akcyjna"
            "spółka akcyjna" || "spółka akcyjna"

    }

}
