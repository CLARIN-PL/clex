package pl.clarin.pwr.g419.io.reader

import org.apache.commons.io.FileUtils
import spock.lang.Specification

class HeadersAndFootersHandlerTest extends Specification {


    def "364141 - HeadersAndFootersHandlerTest should construct valid headers and footers"() {
        given:
            def hocr = File.createTempFile("hocr", ".hocr")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/hocr-headers-364141.hocr"), hocr)
            def document = new HocrReader().parseAndSortBboxes(hocr.toPath())
            document.setId("364141")
            document.docContextInfo.sortHeaders()
            document.docContextInfo.sortFooters()
            def headers = document.docContextInfo.headers
            def footers = document.docContextInfo.footers

        expect:
            headers.size() == 1

            headers[0].startIndex == 0
            headers[0].endIndex == 48
            headers[0].level == 1
            headers[0].lines[0].text == "PS-2017 "
            headers[0].lines[1].text == "Skrócone półroczne skonsolidowane sprawozdanie finansowe "


            footers.size() == 3
            footers[0].startIndex == 0
            footers[0].endIndex == 4
            footers[0].level == 0
            footers[0].lines[0].text == "Poziom zaokrągleń : wszystkie kwoty wyrażone są w tysiącach złotych polskich (o ile nie wskazano inaczej ) "

            footers[1].startIndex == 5
            footers[1].endIndex == 7
            footers[1].level == 1
            footers[1].lines[0].text == "Poziom zaokrągleń : wszystkie kwoty wyrażone są w tysiącach złotych polskich (o ile nie wskazano inaczej ) "
            footers[1].lines[1].text == "Okres objęty spra wozda niem finansowym : 01.01.2017 – 30.06 .2017 Waluta sprawozdawcza : złoty polski (PLN ) "

            footers[2].startIndex == 8
            footers[2].endIndex == 47
            footers[2].level == 0
            footers[2].lines[0].text == "Poziom zaokrągleń : wszystkie kwoty wyrażone są w tysiącach złotych polskich (o ile nie wskazano inaczej ) "

        cleanup:
            FileUtils.deleteQuietly(hocr)
    }

    def "209136 - HeadersAndFootersHandlerTest should construct valid headers and footers"() {
        given:
            def hocr = File.createTempFile("hocr", ".hocr")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/hocr-headers-209136.hocr"), hocr)
            def document = new HocrReader().parseAndSortBboxes(hocr.toPath())
            document.setId("209136")
            document.docContextInfo.sortHeaders()
            document.docContextInfo.sortFooters()
            def headers = document.docContextInfo.headers
            def footers = document.docContextInfo.footers

        expect:
            headers.size() == 1
            headers[0].startIndex == 0
            headers[0].endIndex == 57
            headers[0].level == 0
            headers[0].lines[0].text == "Sprawozdanie Zarządu z działalności Spółki oraz Grupy Kapitałowej za I półrocze 2012 roku . "

            footers.size() == 0

        cleanup:
            FileUtils.deleteQuietly(hocr)
    }

    def "175116 - HeadersAndFootersHandlerTest should construct valid headers and footers"() {
        given:
            def hocr = File.createTempFile("hocr", ".hocr")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/hocr-headers-175116.hocr"), hocr)
            def document = new HocrReader().parseAndSortBboxes(hocr.toPath())
            document.setId("175116")
            document.docContextInfo.sortHeaders()
            document.docContextInfo.sortFooters()
            def headers = document.docContextInfo.headers
            def footers = document.docContextInfo.footers

        expect:
            headers.size() == 4

            headers[0].startIndex == 1
            headers[0].endIndex == 3
            headers[0].level == 2
            headers[0].lines[0].text == "Grupa Kapitałowa NFI Midas S.A . "
            headers[0].lines[1].text == "Skonsolidowany raport za I półrocze 2011 r . "
            headers[0].lines[2].text == "(wszystkie kwoty w tysiącach złotych , jeżeli nie zaznaczono inaczej ) "

            headers[1].startIndex == 4
            headers[1].endIndex == 5
            headers[1].level == 1
            headers[1].lines[0].text == "Grupa Kapitałowa NFI Midas S.A . "
            headers[1].lines[1].text == "Skonsolidowany raport za I półrocze 2011 r . "

            headers[2].startIndex == 6
            headers[2].endIndex == 41
            headers[2].level == 2
            headers[2].lines[0].text == "Grupa Kapitałowa NFI Midas S.A . "
            headers[2].lines[1].text == "Skonsolidowany raport za I półrocze 2011 r . "
            headers[2].lines[2].text == "(wszystkie kwoty w tysiącach złotych , jeżeli nie zaznaczono inaczej ) "

            headers[3].startIndex == 42
            headers[3].endIndex == 59
            headers[3].level == 2
            headers[3].lines[0].text == "NFI Midas S.A . "
            headers[3].lines[1].text == "Skonsolidowany raport za I półrocze 2011 r . "
            headers[3].lines[2].text == "(wszystkie kwoty w tysiącach złotych , jeżeli nie zaznaczono inaczej ) "


            footers.size() == 0

        cleanup:
            FileUtils.deleteQuietly(hocr)
    }

    def "424954 - HeadersAndFootersHandlerTest should construct valid headers and footers"() {
        given:
            def hocr = File.createTempFile("hocr", ".hocr")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/hocr-headers-424954.hocr"), hocr)
            def document = new HocrReader().parseAndSortBboxes(hocr.toPath())
            document.setId("424954")
            document.docContextInfo.sortHeaders()
            document.docContextInfo.sortFooters()
            def headers = document.docContextInfo.headers
            def footers = document.docContextInfo.footers

        expect:
            headers.size() == 3

            headers[0].startIndex == 1
            headers[0].endIndex == 10
            headers[0].level == 2
            headers[0].lines[0].text == "GRUPA KAPITAŁOWA KORPORACJA KGL S.A . Śródroczne skrócone skonsolidowane sprawozdanie finansowe sporządzone na dzień 30 czerwca 2019 r . "
            headers[0].lines[1].text == "i za okres od 1 stycznia 2019 r . do 30 czerwca 2019 r . zawierające Śródroczne skrócone sprawozdanie finansowe spółki Korporacja KGL S.A.(dane w tys . "
            headers[0].lines[2].text == "zł , jeżeli nie zaznaczono inaczej). "

            headers[1].startIndex == 12
            headers[1].endIndex == 27
            headers[1].level == 2
            headers[1].lines[0].text == "GRUPA KAPITAŁOWA KORPORACJA KGL S.A . Śródroczne skrócone skonsolidowane sprawozdanie finansowe sporządzone na dzień 30 czerwca 2019 r . "
            headers[1].lines[1].text == "i za okres od 1 stycznia 2019 r . do 30 czerwca 2019 r . zawierające Śródroczne skrócone sprawozdanie finansowe spółki Korporacja KGL S.A.(dane w tys . "
            headers[1].lines[2].text == "zł , jeżeli nie zaznaczono inaczej). "

            headers[2].startIndex == 30
            headers[2].endIndex == 49
            headers[2].level == 2
            headers[2].lines[0].text == "GRUPA KAPITAŁOWA KORPORACJA KGL S.A . Śródroczne skrócone skonsolidowane sprawozdanie finansowe sporządzone na dzień 30 czerwca 2019 r . "
            headers[2].lines[1].text == "i za okres od 1 stycznia 2019 r . do 30 czerwca 2019 r . zawierające Śródroczne skrócone sprawozdanie finansowe spółki Korporacja KGL S.A.(dane w tys . "
            headers[2].lines[2].text == "zł , jeżeli nie zaznaczono inaczej). "


            footers.size() == 0

        cleanup:
            FileUtils.deleteQuietly(hocr)
    }

    def "175091 - HeadersAndFootersHandlerTest should construct valid headers and footers"() {
        given:
            def hocr = File.createTempFile("hocr", ".hocr")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/hocr-headers-175091.hocr"), hocr)
            def document = new HocrReader().parseAndSortBboxes(hocr.toPath())
            document.setId("175091")
            document.docContextInfo.sortHeaders()
            document.docContextInfo.sortFooters()
            def headers = document.docContextInfo.headers
            def footers = document.docContextInfo.footers

        expect:

            headers.size() == 8

            headers[0].startIndex == 0
            headers[0].endIndex == 0
            headers[0].level == 0
            headers[0].lines[0].text == "Lubawa S.A . "

            headers[1].startIndex == 1
            headers[1].endIndex == 29
            headers[1].level == 2
            headers[1].lines[0].text == "Lubawa S.A . "
            headers[1].lines[1].text == "Śródroczne skrócone skonsolidowane sprawozdanie finansowe w tys . złotych "
            headers[1].lines[2].text == "za okres od 1 stycznia 2011 r . do 30 czerwca 2011 r . "

            headers[2].startIndex == 30
            headers[2].endIndex == 31
            headers[2].level == 0
            headers[2].lines[0].text == "Lubawa S.A . "

            headers[3].startIndex == 32
            headers[3].endIndex == 49
            headers[3].level == 2
            headers[3].lines[0].text == "Lubawa S.A . "
            headers[3].lines[1].text == "Śródroczne skrócone skonsolidowane sprawozdanie finansowe w tys . złotych "
            headers[3].lines[2].text == "za okres od 1 stycznia 2011 r . do 30 czerwca 2011 r . "


            headers[4].startIndex == 50
            headers[4].endIndex == 50
            headers[4].level == 1
            headers[4].lines[0].text == "Lubawa S.A . "
            headers[4].lines[1].text == "Skrócone śródroczne skonsolidowane sprawozdanie finansowe w tys . złotych "

            headers[5].startIndex == 51
            headers[5].endIndex == 68
            headers[5].level == 2
            headers[5].lines[0].text == "Lubawa S.A . "
            headers[5].lines[1].text == "Skrócone śródroczne skonsolidowane sprawozdanie finansowe w tys . złotych "
            headers[5].lines[2].text == "`za okres od 1 stycznia 2011 r . do 30 czerwca 2011 r . "

            headers[6].startIndex == 69
            headers[6].endIndex == 69
            headers[6].level == 1
            headers[6].lines[0].text == "Lubawa S.A . "
            headers[6].lines[1].text == "Skrócone śródroczne skonsolidowane sprawozdanie finansowe w tys . złotych "


            headers[7].startIndex == 70
            headers[7].endIndex == 76
            headers[7].level == 2
            headers[7].lines[0].text == "Lubawa S.A . "
            headers[7].lines[1].text == "Skrócone śródroczne skonsolidowane sprawozdanie finansowe w tys . złotych "
            headers[7].lines[2].text == "`za okres od 1 stycznia 2011 r . do 30 czerwca 2011 r . "

            footers.size() == 1

            footers[0].startIndex == 0
            footers[0].endIndex == 76
            footers[0].level == 0
            footers[0].lines[0].text == "Dane wyrażone są w tys . złotych , o ile nie podano inaczej "


        cleanup:
            FileUtils.deleteQuietly(hocr)
    }

    def "175062 - HeadersAndFootersHandlerTest should construct valid headers and footers"() {
        given:
            def hocr = File.createTempFile("hocr", ".hocr")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/hocr-headers-175062.hocr"), hocr)
            def document = new HocrReader().parseAndSortBboxes(hocr.toPath())
            document.setId("175062")
            document.docContextInfo.sortHeaders()
            document.docContextInfo.sortFooters()
            def headers = document.docContextInfo.headers
            def footers = document.docContextInfo.footers

        expect:
            headers.size() == 2

            headers[0].startIndex == 0
            headers[0].endIndex == 5
            headers[0].level == 1
            headers[0].lines[0].text == "Śródroczne skrócone skonsolidowane sprawozdanie fina nsowe Grupy Unima2000 za okres zako ńczony 30.06 .2011 roku zawierające skrócone "
            headers[0].lines[1].text == "jednostkowe sprawozdanie spółki Unima2000 S.A sporządzone w tysiącacU złotycU polskicU "

            headers[1].startIndex == 8
            headers[1].endIndex == 32
            headers[1].level == 1
            headers[1].lines[0].text == "Śródroczne skrócone skonsolidowane sprawozdanie fina nsowe Grupy Unima2000 za okres zako ńczony 30.06 .2011 roku zawierające skrócone "
            headers[1].lines[1].text == "jednostkowe sprawozdanie spółki Unima2000 S.A sporządzone w tysiącacU złotycU polskicU "


            footers.size() == 0


        cleanup:
            FileUtils.deleteQuietly(hocr)
    }


    def "62576 - HeadersAndFootersHandlerTest should construct valid headers and footers"() {
        given:
            def hocr = File.createTempFile("hocr", ".hocr")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/hocr-headers-62576.hocr"), hocr)
            def document = new HocrReader().parseAndSortBboxes(hocr.toPath())
            document.setId("62576")
            document.docContextInfo.sortHeaders()
            document.docContextInfo.sortFooters()
            def headers = document.docContextInfo.headers
            def footers = document.docContextInfo.footers

        expect:
            headers.size() == 4
            headers[0].startIndex == 3
            headers[0].endIndex == 21
            headers[0].level == 2
            headers[0].lines[0].text == "Grupa kapitałowa North Coast "
            headers[0].lines[1].text == "Półroczne skonsolidowane sprawozdanie finansowe "
            headers[0].lines[2].text == "za okres 1 stycznia do 30 czerwca 2007 "

            headers[1].startIndex == 23
            headers[1].endIndex == 26
            headers[1].level == 2
            headers[1].lines[0].text == "Grupa North Coast "
            headers[1].lines[1].text == "Półroczne skonsolidowane sprawozdanie finansowe "
            headers[1].lines[2].text == "za okres 1 stycznia do 30 czerwca 2006 "

            headers[2].startIndex == 27
            headers[2].endIndex == 30
            headers[2].level == 1
            headers[2].lines[0].text == "Półroczne skonsolidowane sprawozdanie finansowe "
            headers[2].lines[1].text == "za okres 1 stycznia do 30 czerwca 2006 "

            headers[3].startIndex == 32
            headers[3].endIndex == 39
            headers[3].level == 1
            headers[3].lines[0].text == "Półroczne sprawozdanie finansowe "
            headers[3].lines[1].text == "za okres 1 stycznia do 30 czerwca 2006 "

            footers.size() == 1

            footers[0].startIndex == 41
            footers[0].endIndex == 52
            footers[0].level == 1
            footers[0].lines[0].text == "tel . (22 ) 738 31 50 , fax (22 ) 738 31 59 , www.northcoast.com.pl "
            footers[0].lines[1].text == "North Coast S.A., ul . 3 -go Maja 8 , 05-800 Pruszków , NIP 526-02-05-055 "


        cleanup:
            FileUtils.deleteQuietly(hocr)
    }


}
