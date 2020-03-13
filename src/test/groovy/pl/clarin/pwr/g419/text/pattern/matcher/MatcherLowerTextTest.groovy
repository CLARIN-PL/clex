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
            def page = new HocrPage(
                    getSequenceOfBboxes(["1", "stycznia", "2020", "2", "LUTEGO", "2019", "marzec", "2018"] as List))
            def matcher = new MatcherLowerText(texts)

        when:
            def result = matcher.matchesAt(page, index)

        then:
            result.orElse(0) == length

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


    def getSequenceOfBboxes(List<String> words) {
        Box box = new Box(0, 0, 10, 10)
        return words.collect { new Bbox(it, box) } as List
    }
}
