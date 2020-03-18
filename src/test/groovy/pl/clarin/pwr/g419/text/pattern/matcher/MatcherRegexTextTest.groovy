package pl.clarin.pwr.g419.text.pattern.matcher

import pl.clarin.pwr.g419.struct.Bbox
import pl.clarin.pwr.g419.struct.Box
import pl.clarin.pwr.g419.struct.HocrPage
import spock.lang.Specification
import spock.lang.Unroll

import java.util.regex.Pattern

class MatcherRegexTextTest extends Specification {

    @Unroll
    def "matchesAt for index #index should return #length"() {
        given:
            def pattern = Pattern.compile("[0-9]{1,2}[.-][0-9]{1,2}[.-][0-9]{4}")
            def page = new HocrPage(
                    getSequenceOfBboxes(["1.01.2020", ";", "02.02", ".2020", "2020"] as List))
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
            def page = new HocrPage(
                    getSequenceOfBboxes(["1.01.2020", ";", "02.02", ".2020", "2020"] as List))
            def matcher = new MatcherRegexText(pattern, 10,
                    Map.of(1, "day", 2, "month", 3, "year"))

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
    
    def getSequenceOfBboxes(List<String> words) {
        Box box = new Box(0, 0, 10, 10)
        return words.collect { new Bbox(0, it, box) } as List
    }

}
