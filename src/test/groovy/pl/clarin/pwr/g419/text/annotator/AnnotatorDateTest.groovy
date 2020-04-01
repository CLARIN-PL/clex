package pl.clarin.pwr.g419.text.annotator

import pl.clarin.pwr.g419.struct.Bbox
import pl.clarin.pwr.g419.struct.Box
import pl.clarin.pwr.g419.struct.HocrPage
import spock.lang.Specification
import spock.lang.Unroll

class AnnotatorDateTest extends Specification {

    @Unroll
    def "annotate on '#text' should return #date"() {
        given:
            def page = new HocrPage(getSequenceOfBboxes(text.split(" ") as List))
            def annotator = new AnnotatorDate()

        when:
            annotator.annotate(page)

        then:
            page.getAnnotations().collect { it.getText() } as Set == [date] as Set

        where:
            text              || date
            "1 stycznia 2020" || "1 stycznia 2020"
            "x 01.01.2019 x"  || "01.01.2019"
            "01.02 .2019"     || "01.02 .2019"
            "31 grudnia 2009" || "31 grudnia 2009"
    }

    @Unroll
    def "annotate on '#text' should return #norm"() {
        given:
            def page = new HocrPage(getSequenceOfBboxes(text.split(" ") as List))
            def annotator = new AnnotatorDate()

        when:
            annotator.annotate(page)

        then:
            page.getAnnotations().collect { it.getNorm() } as Set == [date] as Set

        where:
            text              || date
            "1 stycznia 2020" || "2020-01-01"
            "x 01.01.2019 x"  || "2019-01-01"
            "01.02 .2019"     || "2019-02-01"
            "31 grudnia 2009" || "2009-12-31"
            "30.09.2008)"     || "2008-09-30"
            "2 września 2005" || "2005-09-02"
    }

    def getSequenceOfBboxes(List<String> words) {
        Box box = new Box(0, 0, 10, 10)
        return words.collect { new Bbox(0, it, box) } as List
    }

}
