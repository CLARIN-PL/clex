package pl.clarin.pwr.g419.action

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class ActionEvalTest extends Specification {

    @Subject
    def action = new ActionEval()

    @Unroll
    def "evalSet for #referenceValues and #extractedValues should return #expected"() {
        when:
            def result = action.evalSets(id, fieldName, referenceValues as Set, extractedValues as Set)

        then:
            result as Set == expected as Set

        where:
            referenceValues | extractedValues || expected
            ["a"]           | ["a"]           || [["OK", "id", "field", "a", "a"]]
            ["b"]           | ["a"]           || [["ERROR", "id", "field", "b", "FalseNegative"], ["ERROR", "id", "field", "FalsePositive", "a"]]
            ["b"]           | []              || [["ERROR", "id", "field", "b", "FalseNegative"]]
            []              | ["a"]           || [["ERROR", "id", "field", "FalsePositive", "a"]]

            id = "id"
            fieldName = "field"

    }

    @Unroll
    def "normalizePerson(#value) should return '#expected'"() {
        when:
            def result = action.normalizePerson(value)

        then:
            result == expected

        where:
            value   || expected
            "A - B" || "A-B"
            "A- B"  || "A-B"
            "A -B"  || "A-B"
    }
}
