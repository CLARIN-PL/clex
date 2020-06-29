package pl.clarin.pwr.g419.text.pattern.matcher

import pl.clarin.pwr.g419.struct.HocrPage
import pl.clarin.pwr.g419.utils.TestUtils
import spock.lang.Specification
import spock.lang.Unroll

import java.util.regex.Pattern

class MatcherRegexTextTest extends Specification {

    @Unroll
    def "matchesAt for index #index should return #length"() {
        given:
            def pattern = Pattern.compile("[0-9]{1,2}[.-][0-9]{1,2}[.-][0-9]{4}")
            def page = new HocrPage(null, TestUtils.getSequenceOfBboxes("1.01.2020 ; 02.02 .2020 2020"))
            def matcher = new MatcherRegexText(pattern, 10)

        when:
            def result = matcher.matchesAt(page, index)

        then:
            result.orElse(new MatcherResult(0)).getLength() == length

        where:
            index || length
            0     || 1
            1     || 0
            2     || 2
            3     || 0
            4     || 0
    }

    def "matchesAt should return matcherResult with valid groups"() {
        given:
            def pattern = Pattern.compile("([0-9]{1,2})[.-]([0-9]{1,2})[.-]([0-9]{4})")
            def page = new HocrPage(null, TestUtils.getSequenceOfBboxes("1.01.2020 ; 02.02 .2020 2020"))
            def matcher = new MatcherRegexText(pattern, 10,
                    [1: "day", 2: "month", 3: "year"] as Map)

        when:
            def result = matcher.matchesAt(page, index)

        then:
            result.orElse(new MatcherResult(0)).getLength() == length
            result.orElse(new MatcherResult(0)).getGroupValue("day").orElse("") == day
            result.orElse(new MatcherResult(0)).getGroupValue("month").orElse("") == month
            result.orElse(new MatcherResult(0)).getGroupValue("year").orElse("") == year

        where:
            index || length | day  | month | year
            0     || 1      | "1"  | "01"  | "2020"
            1     || 0      | ""   | ""    | ""
            2     || 2      | "02" | "02"  | "2020"
            3     || 0      | ""   | ""    | ""
            4     || 0      | ""   | ""    | ""
    }

    @Unroll
    def "matchesAt with ignore should return valid results"() {
        given:
            def page = new HocrPage(null, TestUtils.getSequenceOfBboxes("11 22 33 44"))
            def matcher = new MatcherRegexText("([0-9]{2})", 2).ignore(["11", "22"] as Set)

        when:
            def result = matcher.matchesAt(page, index)

        then:
            result.orElse(new MatcherResult(0)).getLength() == length

        where:
            index || length
            0     || 0
            1     || 0
            2     || 1
            3     || 1
    }
}
