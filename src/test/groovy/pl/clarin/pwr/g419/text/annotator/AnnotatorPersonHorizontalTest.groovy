package pl.clarin.pwr.g419.text.annotator

import pl.clarin.pwr.g419.struct.HocrPage
import pl.clarin.pwr.g419.utils.TestUtils
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class AnnotatorPersonHorizontalTest extends Specification {

    @Subject
    def annotator = new AnnotatorPersonHorizontal()

    @Unroll
    def "annotate on '#text' should return '#norm"() {
        given:
            def page = new HocrPage(null, TestUtils.getSequenceOfBboxes(text))
            new AnnotatorRole().annotate(page)

        when:
            annotator.annotate(page)

        then:
            page.getAnnotations().stream().filter { an -> an.getType() == "person" }
                    .collect { it.getNorm() } as Set == norm as Set

        where:
            text                                        || norm
            "Prezes – Piotr Bieliński"                  || ["|prezes|Piotr Bieliński"]
            "Anna Boniuk-Gorzelańczyk Członek Zarządu"  || ["|członek zarządu|Anna Boniuk-Gorzelańczyk"]
            "Andrea Manuel Teixeira wiceprezes zarządu" || ["|wiceprezes zarządu|Andrea Manuel Teixeira"]
    }

}
