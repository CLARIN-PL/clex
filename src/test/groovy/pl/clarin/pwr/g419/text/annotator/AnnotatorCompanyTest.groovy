package pl.clarin.pwr.g419.text.annotator


import pl.clarin.pwr.g419.struct.HocrPage
import pl.clarin.pwr.g419.utils.TestUtils
import spock.lang.Specification
import spock.lang.Unroll

class AnnotatorCompanyTest extends Specification {

    @Unroll
    def "annotate on '#text' should return '#norm"() {
        given:
            def page = new HocrPage(null, TestUtils.getSequenceOfBboxes(text))
            def annotatorPeriod = new AnnotatorPeriod()
            def annotatorCompanyPrefix = new AnnotatorCompanyPrefix()
            def annotatorCompanySuffix = new AnnotatorCompanySuffix()
            def annotatorCompany = new AnnotatorCompany()
            annotatorPeriod.annotate(page)
            annotatorCompanyPrefix.annotate(page)
            annotatorCompanySuffix.annotate(page)

        when:
            annotatorCompany.annotate(page)

        then:
            page.getAnnotations().stream().filter { an -> an.getType() == "company" }
                    .collect { it.getNorm() } as Set == norm as Set

        where:
            text                                                                        || norm
            "sprawozdanie finansowe oktan sa"                                           || ["oktan sa"]
            "nazwa jednostki : oktan sa"                                                || ["oktan sa"]
            "nazwa jednostki : oktan s.a."                                              || ["oktan s.a."]
            "działalności grupy kapitałowej energamontaż-północ w I-szym półroczu 2009" || ["energamontaż-północ"]
            "Raport Grupy Kapitałowej Banku Milennium S.A."                             || ["Banku Milennium S.A."]
            "Grupy Kapitałowej ELEKTROTIM"                                              || ["ELEKTROTIM"]
            "Grupy Kapitałowej ELEKTROTIM AA"                                           || []
            "Grupy Kapitałowej| ELEKTROTIM"                                             || []
    }

}
