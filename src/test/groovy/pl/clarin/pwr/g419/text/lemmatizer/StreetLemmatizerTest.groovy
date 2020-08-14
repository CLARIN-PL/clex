package pl.clarin.pwr.g419.text.lemmatizer

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class StreetLemmatizerTest extends Specification {

    @Subject
    def lemmatizer = new StreetLemmatizer()

    @Unroll
    def "lemmatize(#phrase) should return valid lemma '#lemma'"() {
        when:
            def result = lemmatizer.lemmatize(phrase)

        then:
            result == lemma

        where:
            phrase              || lemma
            "Zamkowej"          || "Zamkowa"
            "Unii Europejskiej" || "Unii Europejskiej"
            "al. Hubskiej"      || "al. Hubska"
    }

}
