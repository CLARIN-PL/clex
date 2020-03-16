package pl.clarin.pwr.g419.text.annotator

import pl.clarin.pwr.g419.struct.Bbox
import pl.clarin.pwr.g419.struct.Box
import pl.clarin.pwr.g419.struct.HocrPage
import spock.lang.Specification

class AnnotatorDateTest extends Specification {

    def "annotate should add valid set of annotations to the page"() {
        given:
            def page = new HocrPage(getSequenceOfBboxes(
                    ["1", "stycznia", "2020", "01.01.2019", "01.02", ".2019"] as List))
            def annotator = new AnnotatorDate()

        when:
            annotator.annotate(page)

        then:
            page.getAnnotations().size() == 3

        and:
            page.getAnnotations().collect { it.getText() } as Set ==
                    ["1 stycznia 2020", "01.01.2019", "01.02 .2019"] as Set
    }

    def getSequenceOfBboxes(List<String> words) {
        Box box = new Box(0, 0, 10, 10)
        return words.collect { new Bbox(it, box) } as List
    }

}
