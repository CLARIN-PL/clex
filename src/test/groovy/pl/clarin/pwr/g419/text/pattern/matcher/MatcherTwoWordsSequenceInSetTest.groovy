package pl.clarin.pwr.g419.text.pattern.matcher

import pl.clarin.pwr.g419.struct.Bbox
import pl.clarin.pwr.g419.struct.Box
import pl.clarin.pwr.g419.struct.HocrPage
import pl.clarin.pwr.g419.utils.TestUtils
import spock.lang.Specification
import spock.lang.Unroll

class MatcherTwoWordsSequenceInSetTest extends Specification {

    @Unroll
    def "matchesAt for index #index should return #length"() {
        given:
            def texts = ["Opole", "Opole Główne", "Wrocław", "Wrocław Brochów"] as Set
            def page = new HocrPage(null, TestUtils.getSequenceOfBboxes("Także Opole jest inną stacją niż Opole Główne " +
                    "podobnie jak Wrocław Brochów i Wrocław "))
            def matcher = new MatcherTwoWordsSequenceInSet(texts)

        when:
            def result = matcher.matchesAt(page, index)
            print("" + result)

        then:
            result.orElse(new MatcherResult(0)).getLength() == length

        where:
            index || length
            0     || 0
            1     || 1
            2     || 0
            3     || 0
            4     || 0
            5     || 0
            6     || 2
            7     || 0
            8     || 0
            9     || 0
            10    || 2
            11    || 0
            12    || 0
            13    || 1
    }


    def getSequenceOfBboxes(List<String> words) {
        Box box = new Box(0, 0, 10, 10)
        return words.collect { new Bbox(0, it, box) } as List
    }
}
