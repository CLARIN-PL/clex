package pl.clarin.pwr.g419.text.annotator

import pl.clarin.pwr.g419.struct.Bbox
import pl.clarin.pwr.g419.struct.Box
import pl.clarin.pwr.g419.struct.HocrPage
import pl.clarin.pwr.g419.text.pattern.Pattern
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText
import spock.lang.Specification

class AnnotatorTest extends Specification {

    def "annotate should add valid set of annotations to the page"() {
        given:
            def months = ["stycznia", "lutego"] as Set
            def page = new HocrPage(
                    getSequenceOfBboxes(["1", "stycznia", "2020", "2", "LUTEGO", "2019", "marzec", "2018"] as List))
            def pattern = new Pattern()
                    .next(new MatcherLowerText(months))
            def annotator = new Annotator("month", pattern)

        when:
            annotator.annotate(page)

        then:
            page.getAnnotations().size() == 2

        and:
            page.getAnnotations().collect { it.getText() } as Set == ["stycznia", "LUTEGO"] as Set
    }

    def getSequenceOfBboxes(List<String> words) {
        Box box = new Box(0, 0, 10, 10)
        return words.collect { new Bbox(it, box) } as List
    }

}
