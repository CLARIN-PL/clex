package pl.clarin.pwr.g419.utils

import pl.clarin.pwr.g419.kbase.NeLexicon2
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class NeLexicon2Test extends Specification {

    @Subject
    @Shared
    def nelexicon2 = NeLexicon2.get()

    def "get(nam_liv_person_last) should return a valid set of first names"() {
        when:
            def size = nelexicon2.getNames("nam_liv_person_last").size()

        then:
            size == 371390
    }

}
