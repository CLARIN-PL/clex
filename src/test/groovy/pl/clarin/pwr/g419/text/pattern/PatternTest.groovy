package pl.clarin.pwr.g419.text.pattern

import pl.clarin.pwr.g419.struct.Bbox
import pl.clarin.pwr.g419.struct.Box
import pl.clarin.pwr.g419.struct.HocrPage
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText
import spock.lang.Specification
import spock.lang.Unroll

class PatternTest extends Specification {

    @Unroll
    def "matchesAt for index #index should return #length"() {
        given:
            def months = ["stycznia", "lutego"] as Set
            def years = ["2018", "2019", "2020"] as Set
            def page = new HocrPage(
                    getSequenceOfBboxes(["1", "stycznia", "2020", "2", "LUTEGO", "2019", "marzec", "2018"] as List))
            def pattern = new Pattern()
                    .next(new MatcherLowerText(["1", "2"] as Set))
                    .next(new MatcherLowerText(months))
                    .next(new MatcherLowerText(years))

        when:
            def result = pattern.matchesAt(page, index)

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
    }

    def "find should return a valid list of matches"() {
        given:
            def months = ["stycznia", "lutego"] as Set
            def years = ["2018", "2019", "2020"] as Set
            def page = new HocrPage(
                    getSequenceOfBboxes(["1", "stycznia", "2020", "2", "LUTEGO", "2019", "marzec", "2018"] as List))
            def pattern = new Pattern()
                    .next(new MatcherLowerText(["1", "2"] as Set))
                    .next(new MatcherLowerText(months))
                    .next(new MatcherLowerText(years))

        when:
            def result = pattern.find(page)

        then:
            result.size() == 2

        and:
            result.get(0).indexBegin == 0
            result.get(0).indexEnd == 3

        and:
            result.get(1).indexBegin == 3
            result.get(1).indexEnd == 6
    }

    def getSequenceOfBboxes(List<String> words) {
        Box box = new Box(0, 0, 10, 10)
        return words.collect { new Bbox(it, box) } as List
    }
}
