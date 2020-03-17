package pl.clarin.pwr.g419.text.pattern

import pl.clarin.pwr.g419.struct.Bbox
import pl.clarin.pwr.g419.struct.Box
import pl.clarin.pwr.g419.struct.HocrPage
import pl.clarin.pwr.g419.text.normalization.NormalizerMap
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText
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
            result.orElse(new PatternMatch(0, 0, null, null)).getLength() == length

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


    def "find should return matches with valid set of groups"() {
        given:
            def months = ["stycznia", "lutego"] as Set
            def years = ["2018", "2019", "2020"] as Set
            def page = new HocrPage(
                    getSequenceOfBboxes(["1", "stycznia", "2020", "2", "LUTEGO", "2019", "marzec", "2018"] as List))
            def pattern = new Pattern()
                    .next(new MatcherLowerText(["1", "2"] as Set).group("day"))
                    .next(new MatcherLowerText(months).group("month"))
                    .next(new MatcherLowerText(years).group("year"))

        when:
            def result = pattern.find(page)

        then:
            result.size() == 2

        and:
            result.get(0).getGroupValue("day").orElse("") == "1"
            result.get(0).getGroupValue("month").orElse("") == "stycznia"
            result.get(0).getGroupValue("year").orElse("") == "2020"

        and:
            result.get(1).getGroupValue("day").orElse("") == "2"
            result.get(1).getGroupValue("month").orElse("") == "lutego"
            result.get(1).getGroupValue("year").orElse("") == "2019"
    }

    def "find should return matches with valid set of groups and normalized values"() {
        given:
            def monthsMap = Map.of("stycznia", "01", "lutego", "02")
            def years = ["2018", "2019", "2020"] as Set
            def page = new HocrPage(
                    getSequenceOfBboxes(["1", "stycznia", "2020", "2", "LUTEGO", "2019", "marzec", "2018"] as List))
            def pattern = new Pattern()
                    .next(new MatcherLowerText(["1", "2"] as Set).group("day"))
                    .next(new MatcherLowerText(monthsMap.keySet())
                            .group("month")
                            .normalizer(new NormalizerMap(monthsMap)))
                    .next(new MatcherLowerText(years).group("year"))

        when:
            def result = pattern.find(page)

        then:
            result.size() == 2

        and:
            result.get(0).getGroupValue("day").orElse("") == "1"
            result.get(0).getGroupValue("month").orElse("") == "01"
            result.get(0).getGroupValue("year").orElse("") == "2020"

        and:
            result.get(1).getGroupValue("day").orElse("") == "2"
            result.get(1).getGroupValue("month").orElse("") == "02"
            result.get(1).getGroupValue("year").orElse("") == "2019"
    }

    def "find for a pattern with regex should return matches with valid set of groups"() {
        given:
            def months = ["stycznia", "lutego"] as Set
            def years = ["2018", "2019", "2020"] as Set
            def page = new HocrPage(
                    getSequenceOfBboxes(["01.12.2020", "stycznia", "2020", "2", "LUTEGO"] as List))
            def pattern = new Pattern()
                    .next(new MatcherRegexText("([0-9]{1,2})[.]([0-9]{1,2})[.]([0-9]{4})", 10,
                            Map.of(1, "day", 2, "month", 3, "year")).group("date"));

        when:
            def result = pattern.find(page)

        then:
            result.size() == 1

        and:
            result.get(0).getGroupValue("day").orElse("") == "01"
            result.get(0).getGroupValue("month").orElse("") == "12"
            result.get(0).getGroupValue("year").orElse("") == "2020"
            result.get(0).getGroupValue("date").orElse("") == "01.12.2020"
    }

    def getSequenceOfBboxes(List<String> words) {
        Box box = new Box(0, 0, 10, 10)
        return words.collect { new Bbox(0, it, box) } as List
    }
}
