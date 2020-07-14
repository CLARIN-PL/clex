package pl.clarin.pwr.g419.text.extractor

import org.apache.commons.io.FileUtils
import pl.clarin.pwr.g419.io.reader.HocrReader
import pl.clarin.pwr.g419.struct.FieldContext
import spock.lang.Specification
import spock.lang.Subject

import java.util.regex.Matcher

class ExtractorPeopleTest extends Specification {

    @Subject
    ExtractorPeople extractor = new ExtractorPeople()

    def "extractor should correctly recognize lastnames  "() {
        given:

            Matcher result = extractor.patternLastName.matcher(lastname)

        expect:
            result.matches() == condition

        where:
            lastname          || condition
            "Lutośławski"     || true
            "s.a ."           || false
            "s . a ."         || false
            "Spółka Akcyjna"  || false
            "spółka akcyjna"  || false
            "Schrędder-Grab"  || true
            "Schrödter"       || true
            "Rybak-Schrödter" || true

    }

}
