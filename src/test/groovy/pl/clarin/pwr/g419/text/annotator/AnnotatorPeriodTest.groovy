package pl.clarin.pwr.g419.text.annotator

import pl.clarin.pwr.g419.struct.HocrPage
import pl.clarin.pwr.g419.utils.TestUtils
import spock.lang.Specification
import spock.lang.Unroll

class AnnotatorPeriodTest extends Specification {

    @Unroll
    def "annotate on '#text' should return '#norm"() {
        given:
            def page = new HocrPage(TestUtils.getSequenceOfBboxes(text))
            def annotatorDate = new AnnotatorDate()
            def annotatorPeriod = new AnnotatorPeriod()
            annotatorDate.annotate(page)

        when:
            annotatorPeriod.annotate(page)

        then:
            page.getAnnotations().stream().filter { an -> an.getType() == "period" }
                    .collect { it.getNorm() } as Set == [norm] as Set

        where:
            text                                               || norm
            "1 stycznia 2020 do 01.01.2019"                    || "2020-01-01:2019-01-01"
            "I półrocze 2020"                                  || "2020-01-01:2020-06-30"
            "II półroczu 2010"                                 || "2010-07-01:2010-12-31"
            "6 miesięcy zakończony dnia 30 czerwca 2009"       || "2009-01-01:2009-06-30"
            "6 miesięcy zakończony dnia 31 grudnia 2009"       || "2009-07-01:2009-12-31"
            "sześciu miesięcy zakończony dnia 31 grudnia 2009" || "2009-07-01:2009-12-31"
            "01.01- 30.06.2009"                                || "2009-01-01:2009-06-30"
            "01-06.2008"                                       || "2008-01-01:2008-06-30"
            "PIERWSZE PÓŁROCZE 2019 ROKU"                      || "2019-01-01:2019-06-30"
            "6 miesięcy zakończony 30 czerwca 2010 roku"       || "2010-01-01:2010-06-30"
            "6 miesięcy kończących się 30 czerwca 2011"        || "2011-01-01:2011-06-30"
            "I półroczu 2015 roku"                             || "2015-01-01:2015-06-30"
            "01.01 do 30.06.2015"                              || "2015-01-01:2015-06-30"
            "I półrocze 2015r"                                 || "2015-01-01:2015-06-30"
            "01.01.2009 ROKU DO 30.06.2009 ROKU"               || "2009-01-01:2009-06-30"
            "1 STYCZNIA 2012 ROKU DO 30 CZERWCA 2012 ROKU"     || "2012-01-01:2012-06-30"
            "1 stycznia do 30 czerwca 2013 roku"               || "2013-01-01:2013-06-30"
            "od dnia 01.01.2012 roku do dnia 30.06.2012 roku"  || "2012-01-01:2012-06-30"
            "26.11.2004 r . do 30.06.2005 r ."                 || "2004-11-26:2005-06-30"
            "1 lipca 2008 r . do 31 grudnia 2008 r ."          || "2008-07-01:2008-12-31"
            "ZA OKRES OD 01.01.2008 DO 30.06.2008"             || "2008-01-01:2008-06-30"
    }

}
