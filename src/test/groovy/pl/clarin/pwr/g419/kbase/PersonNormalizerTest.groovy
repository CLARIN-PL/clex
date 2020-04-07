package pl.clarin.pwr.g419.kbase

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class PersonNormalizerTest extends Specification {

    @Subject
    def norm = new PersonNormalizer()

    @Unroll
    def "normalizePerson(#value) should return '#expected'"() {
        when:
            def result = norm.normalize(value)

        then:
            result == expected

        where:
            value     || expected
            "__A - B" || "__A-B"
            "__A- B"  || "__A-B"
            "__A -B"  || "__A-B"
    }

}
