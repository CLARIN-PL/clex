package pl.clarin.pwr.g419.text

import spock.lang.Specification
import spock.lang.Subject

class InformationExtractorTest extends Specification {

    @Subject
    InformationExtractor extractor = new InformationExtractor()


    def "simpyLemmatize(#text) should return '#lemma'"() {
        when:
            def result = extractor.simpyLemmatize(text)

        then:
            result == lemma

        where:
            text              || lemma
            "BANKU MILLENIUM" || "BANK MILLENIUM"
    }
}
