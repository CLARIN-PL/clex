package pl.clarin.pwr.g419.kbase.lexicon

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class PersonNameLexiconTest extends Specification {

    @Subject
    @Shared
    def lexicon = new PersonNameLexicon()

    def "constructor should create non-empty lexicon"() {
        expect:
            lexicon.size() > 0
    }

    @Unroll
    def "aproximate(#name) should return #expected"() {
        when:
            def result = lexicon.approximate(name)

        then:
            result == expected

        where:
            name              || expected
            "Jaś"             || "Jaś"
            "Dariusz Krawiec" || "Dariusz Krawiec"
    }

    @Unroll
    def "getFullName(#name) should return #expected"() {
        when:
            def result = lexicon.getFullName(name)

        then:
            result == expected

        where:
            name              || expected
            "Jaś"             || "Jaś"
            "Dariusz Krawiec" || "Dariusz Jacek Krawiec"

    }
}
