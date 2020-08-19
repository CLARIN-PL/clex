package pl.clarin.pwr.g419.text.lemmatizer

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class CompanyLemmatizerTest extends Specification {

    @Subject
    def lemmatizer = new CompanyLemmatizer()

    @Unroll
    def "lemmatize(#word) should return #lemma"() {
        when:
            def output = lemmatizer.lemmatize(word)

        then:
            output == lemma

        where:
            word             || lemma
            "PKO"            || "POWSZECHNA KASA OSZCZĘDNOŚCI"
            "PKO SA"         || "POWSZECHNA KASA OSZCZĘDNOŚCI"
            "4 FUN MEDIA"    || "4FUN MEDIA"
            "4 FUN MEDIA SA" || "4FUN MEDIA"

    }
}
