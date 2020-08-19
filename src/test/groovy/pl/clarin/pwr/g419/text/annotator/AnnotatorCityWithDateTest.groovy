package pl.clarin.pwr.g419.text.annotator

import pl.clarin.pwr.g419.struct.HocrPage
import pl.clarin.pwr.g419.utils.TestUtils
import spock.lang.Specification
import spock.lang.Unroll

class AnnotatorCityWithDateTest extends Specification {


    @Unroll
    def "annotate on '#text' should return '#norm"() {
        given:
            def page = new HocrPage(null, TestUtils.getSequenceOfBboxes(text))
            def annotatorDate = new AnnotatorDate()
            def annotatorCity = new AnnotatorCity()
            def annotatorCityWithDate = new AnnotatorCityWithDate()

            annotatorDate.annotate(page)
            annotatorCity.annotate(page)


        when:
            annotatorCityWithDate.annotate(page)

        then:
            page.getAnnotations().stream().filter { an -> an.getType() == AnnotatorCityWithDate.CITY_WITH_DATE }
                    .collect { it.getNorm() } as Set == [norm] as Set

        where:
            text                               || norm
            " Warszawa , dnia 1 stycznia 2009" || "Warszawa:2009-01-01"
            " Poznań 2010-01-02"               || "Poznań:2010-01-02"
            " Wrocław - 12.02.2007 "           || "Wrocław:2007-02-12"
            " Wrocław , 14 kwietnia 2008 "     || "Wrocław:2008-04-14"

    }

}
