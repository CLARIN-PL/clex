package pl.clarin.pwr.g419.text.normalization


import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class NormalizerPersonRoleTest extends Specification {

    @Subject
    def norm = new NormalizerPersonRole()

    @Unroll
    def "normalizeName(#value) should return '#expected'"() {
        when:
            def result = norm.normalizeName(value)

        then:
            result == expected

        where:
            value   || expected
            "A - B" || "A-B"
            "A- B"  || "A-B"
            "A -B"  || "A-B"
    }

    @Unroll
    def "normalizeRole(#value) should return '#expected'"() {
        when:
            def result = norm.normalizeRole(value)

        then:
            result == expected

        where:
            value               || expected
            "członek, dyrektor" || "Członek"
            "prezes zarządu"    || "Prezes Zarządu"
            "PREZES ZARZĄDU"    || "Prezes Zarządu"
    }
}
