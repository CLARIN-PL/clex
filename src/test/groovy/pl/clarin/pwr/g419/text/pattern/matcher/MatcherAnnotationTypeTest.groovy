package pl.clarin.pwr.g419.text.pattern.matcher

import pl.clarin.pwr.g419.struct.Annotation
import pl.clarin.pwr.g419.struct.Bbox
import pl.clarin.pwr.g419.struct.Box
import pl.clarin.pwr.g419.struct.HocrPage
import spock.lang.Specification

class MatcherAnnotationTypeTest extends Specification {

    def "matchesAt for index #index should return #length"() {
        given:
            def page = new HocrPage(
                    getSequenceOfBboxes(["1", "stycznia", "2020", "2", "LUTEGO", "2019", "marzec", "2018"] as List))
            def matcher = new MatcherAnnotationType(type1)
            page.getAnnotations().add(new Annotation(type1, page, 0, 3))
            page.getAnnotations().add(new Annotation(type1, page, 3, 6))
            page.getAnnotations().add(new Annotation(type2, page, 1, 2))
            page.getAnnotations().add(new Annotation(type2, page, 4, 5))

        when:
            def result = matcher.matchesAt(page, index)

        then:
            result.orElse(0) == length

        where:
            index || length
            0     || 3
            1     || 0
            2     || 0
            3     || 3
            4     || 0
            5     || 0
            6     || 0

            type1 = "date"
            type2 = "month"
    }


    def getSequenceOfBboxes(List<String> words) {
        Box box = new Box(0, 0, 10, 10)
        return words.collect { new Bbox(it, box) } as List
    }

}
