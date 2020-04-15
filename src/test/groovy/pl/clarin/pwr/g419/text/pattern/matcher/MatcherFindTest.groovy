package pl.clarin.pwr.g419.text.pattern.matcher

import pl.clarin.pwr.g419.struct.HocrPage
import pl.clarin.pwr.g419.text.pattern.Pattern
import pl.clarin.pwr.g419.utils.TestUtils
import spock.lang.Specification
import spock.lang.Unroll

class MatcherFindTest extends Specification {

    @Unroll
    def "find should return valid result"() {
        given:
            def p = new Pattern().next(new MatcherLowerText("zaakceptowny"))
            def matcher = new MatcherFind(p, maxDistance)
            def text = "w dniu skondolifowany raport został zaakceptowny"
            def page = new HocrPage(TestUtils.getSequenceOfBboxes(text))

        when:
            def result = matcher.matchesAt(page, pos)

        then:
            result.isPresent() == length > 0
            result.orElse(new MatcherResult(0)).length == length

        where:
            pos | maxDistance || length
            0   | 2           || 0
            0   | 6           || 6
            2   | 6           || 4

    }

    def "find should return result with valid groups"() {
        given:
            def p = new Pattern().next(new MatcherLowerText("zaakceptowny").group(groupName))
            def matcher = new MatcherFind(p, 6)
            def text = "w dniu skondolifowany raport został zaakceptowny"
            def page = new HocrPage(TestUtils.getSequenceOfBboxes(text))

        when:
            def result = matcher.matchesAt(page, 0)

        then:
            result.get().getGroupValue(groupName).get() == "zaakceptowny"

        where:
            groupName = "xxx"

    }
}
