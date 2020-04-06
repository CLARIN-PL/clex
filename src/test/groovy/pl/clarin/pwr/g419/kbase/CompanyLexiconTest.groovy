package pl.clarin.pwr.g419.kbase

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class CompanyLexiconTest extends Specification {

    @Subject
    @Shared
    def lexicon = new CompanyLexicon()

    def "constructor should create lexicton with valid number or names"() {
        expect:
            lexicon.size() == 542
    }

    @Unroll
    def "aproximate(#name) should return #expected"() {
        when:
            def result = lexicon.approximate(name)

        then:
            result == expected

        where:
            name                 || expected
            "4 FUN MEDIA"        || "4 FUN MEDIA"
            "4FUN MEDIA"         || "4 FUN MEDIA"
            "4 MEDIA"            || "4 MEDIA"
            "ING BANK ŚLĄSKIEGO" || "ING BANK ŚLĄSKI"
            "IMMOBILE"           || "GRUPA KAPITAŁOWA IMMOBILE"
            "H1 2019 1 MEDIACAP" || "MEDIACAP"
            "RAFAMET"            || "FABRYKA OBRABIAREK RAFAMET"
            "POLNA"              || "ZAKŁADY AUTOMATYKI POLNA"
            ""                   || ""

    }
}
