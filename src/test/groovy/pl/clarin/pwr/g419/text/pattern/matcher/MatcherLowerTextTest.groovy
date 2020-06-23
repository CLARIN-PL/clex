package pl.clarin.pwr.g419.text.pattern.matcher

import pl.clarin.pwr.g419.struct.Bbox
import pl.clarin.pwr.g419.struct.Box
import pl.clarin.pwr.g419.struct.HocrPage
import spock.lang.Specification
import spock.lang.Unroll

class MatcherLowerTextTest extends Specification {

    @Unroll
    def "matchesAt for index #index should return #length"() {
        given:
            def texts = ["stycznia", "lutego"] as Set
            def page = new HocrPage(null,
                    getSequenceOfBboxes(["1", "stycznia", "2020", "2", "LUTEGO", "2019", "marzec", "2018"] as List))
            def matcher = new MatcherLowerText(texts)

        when:
            def result = matcher.matchesAt(page, index)

        then:
            result.orElse(new MatcherResult(0)).getLength() == length

        where:
            index || length
            0     || 0
            1     || 1
            2     || 0
            3     || 0
            4     || 1
            5     || 0
            6     || 0
    }


    @Unroll
    def "matchesAt for index #index should return MatchResult with group named 'month' and value '#group'"() {
        given:
            def texts = ["stycznia", "lutego"] as Set
            def page = new HocrPage(null,
                    getSequenceOfBboxes(["1", "stycznia", "2020", "2", "LUTEGO", "2019", "marzec", "2018"] as List))
            def matcher = new MatcherLowerText(texts).group("month")

        when:
            def result = matcher.matchesAt(page, index)

        then:
            result.orElse(new MatcherResult(0)).getGroupValue("month").orElse("") == month

        where:
            index || month
            0     || ""
            1     || "stycznia"
            2     || ""
            3     || ""
            4     || "lutego"
            5     || ""
            6     || ""
    }


    def getSequenceOfBboxes(List<String> words) {
        Box box = new Box(0, 0, 10, 10)
        return words.collect { new Bbox(0, it, box) } as List
    }
}
