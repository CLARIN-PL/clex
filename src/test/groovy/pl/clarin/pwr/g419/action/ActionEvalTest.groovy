package pl.clarin.pwr.g419.action

import pl.clarin.pwr.g419.struct.FieldContext
import pl.clarin.pwr.g419.text.normalization.NormalizerString
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class ActionEvalTest extends Specification {

    @Subject
    def action = new ActionEval()

    @Unroll
    def "evalSet for #referenceValues and #extractedValues should return #expected"() {
        when:
            def extracted = extractedValues.collect { new FieldContext<String>(it, "", "") }.collect()
            def result = action.evalSets(id, fieldName, new NormalizerString(), referenceValues as Set, extracted as Set)


        then:
            result as Set == expected as Set

        where:
            referenceValues | extractedValues || expected
            ["a"]           | ["a"]           || [["OK", "id", "field", "a", "a", "", ""]]
            ["b"]           | ["a"]           || [["ERROR", "id", "field", "b", "FalseNegative", "", ""], ["ERROR", "id", "field", "FalsePositive", "a", "", ""]]
            ["b"]           | []              || [["ERROR", "id", "field", "b", "FalseNegative", "", ""]]
            []              | ["a"]           || [["ERROR", "id", "field", "FalsePositive", "a", "", ""]]

            id = "id"
            fieldName = "field"

    }

}
