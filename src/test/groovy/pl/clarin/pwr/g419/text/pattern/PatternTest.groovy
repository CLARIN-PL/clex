package pl.clarin.pwr.g419.text.pattern


import pl.clarin.pwr.g419.struct.HocrPage
import pl.clarin.pwr.g419.text.normalization.NormalizerStringMap
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText
import pl.clarin.pwr.g419.utils.TestUtils
import spock.lang.Specification
import spock.lang.Unroll

class PatternTest extends Specification {

    @Unroll
    def "matchesAt for index #index should return #length"() {
        given:
            def months = ["stycznia", "lutego"] as Set
            def years = ["2018", "2019", "2020"] as Set
            def page = new HocrPage(null, TestUtils.getSequenceOfBboxes("1 stycznia 2020 2 LUTEGO 2019 marzec 2018"))
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
            def page = new HocrPage(null, TestUtils.getSequenceOfBboxes("1 stycznia 2020 2 LUTEGO 2019 marzec 2018"))
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
            def page = new HocrPage(null, TestUtils.getSequenceOfBboxes("1 stycznia 2020 2 LUTEGO 2019 marzec 2018"))
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
            def monthsMap = ["stycznia": "01", "lutego": "02"] as Map
            def years = ["2018", "2019", "2020"] as Set
            def page = new HocrPage(null, TestUtils.getSequenceOfBboxes("1 stycznia 2020 2 LUTEGO 2019 marzec 2018"))
            def pattern = new Pattern()
                    .next(new MatcherLowerText(["1", "2"] as Set).group("day"))
                    .next(new MatcherLowerText(monthsMap.keySet())
                            .group("month")
                            .normalizer(new NormalizerStringMap(monthsMap)))
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
            def page = new HocrPage(null, TestUtils.getSequenceOfBboxes("01.12.2020 stycznia 2020 2 LUTEGO"))
            def pattern = new Pattern()
                    .next(new MatcherRegexText("([0-9]{1,2})[.]([0-9]{1,2})[.]([0-9]{4})", 10,
                            [1: "day", 2: "month", 3: "year"] as Map).group("date"))

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

    @Unroll
    def "find for pattern with matchLine should return valid result for '#text'"() {
        given:
            def page = new HocrPage(null, TestUtils.getSequenceOfBboxes(text))
            def pattern = new Pattern().matchLine()
                    .next(new MatcherLowerText("grupa"))
                    .next(new MatcherLowerText("abc"))

        when:
            def result = pattern.find(page)

        then:
            result.size() == found

        where:
            text                   || found
            "Grupa abc"            || 1
            "Grupa abc| Grupa abc" || 2
            "Grupa| abc"           || 0
            "Grupa abc def"        || 0
            "prev Grupa abc"       || 0
    }
}
