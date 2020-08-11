package pl.clarin.pwr.g419.text.annotator;


import pl.clarin.pwr.g419.struct.HocrPage
import pl.clarin.pwr.g419.utils.TestUtils
import spock.lang.Specification
import spock.lang.Unroll

class AnnotatorStreetNameStartAbbreviationTest extends Specification {

    @Unroll
    def "annotate on '#text' should return '#norm "() {
        given:
            def page = new HocrPage(null, TestUtils.getSequenceOfBboxes(text))
            def annotatorStreetNameStartAbbreviation = new AnnotatorStreetNameStartAbbreviation()


        when:
            annotatorStreetNameStartAbbreviation.annotate(page)


        then:
            page.getAnnotations()
                    .stream()
                    .filter
                    { an -> an.getType() == AnnotatorStreetNameStartAbbreviation.STREET_NAME_START_ABBRV }
                    .collect { it.getNorm() } as Set == norm as Set

        where:
            text                          || norm
            "ul. gen . Hellera "          || ["gen."]
            "ul. św . Mikołaja "          || ["św."]
            "ul. prof . Andrzejewskiego " || ["prof."]
            "ul. Prof . Andrzejewskiego " || ["Prof."]
            "ul. pl . Andrzejewskiego "   || ["pl."]
            "ul. Generalska "             || []
            "ul. Profesorska "            || []
            "ul. Światosława "            || []
            "ul. gen. Rapaporta "         || []
            "ul. GEN . Rapaporta "        || ["GEN."]
            //"ul. GEN. HELLERA "         || ["gen."] // zkładamy że zawsze kropki są odcięte
            "ul. GEN .  HELLERA "         || ["GEN."]
            "ul. ŚW . Ucha"               || ["ŚW."]
    }
}