package pl.clarin.pwr.g419.text.pattern.matcher

import pl.clarin.pwr.g419.struct.Bbox
import pl.clarin.pwr.g419.struct.Box
import pl.clarin.pwr.g419.struct.HocrPage
import spock.lang.Specification
import spock.lang.Unroll

class MatcherNotLowerTextTest extends Specification {

    @Unroll
    def "matchesAt on #text and #pos should return match of length #length"() {
        given:
            def page = new HocrPage(getSequenceOfBboxes("FIRMA S.A.".split(" ") as List))
            def matcher = new MatcherNotLowerText(Set.of("s.a."))

        when:
            def result = matcher.matchesAt(page, index)

        then:
            result.orElse(new MatcherResult(0)).getLength() == length

        where:
            text         | index || length
            "FIRMA S.A." | 0     || 1
            "FIRMA S.A." | 1     || 0
    }

    def getSequenceOfBboxes(List<String> words) {
        Box box = new Box(0, 0, 10, 10)
        return words.collect { new Bbox(0, it, box) } as List
    }

}
