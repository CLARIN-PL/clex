package pl.clarin.pwr.g419.text.normalization

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class NormalizerNum2DigitTest extends Specification {

    @Subject
    def normalizer = new NormalizerNum2Digit()

    @Unroll
    def "normalize(#value) should return #norm"() {
        when:
            def result = normalizer.normalize(value)

        then:
            result == norm

        where:
            value || norm
            "1"   || "01"
            "9"   || "09"
            "23"  || "23"

    }

}
