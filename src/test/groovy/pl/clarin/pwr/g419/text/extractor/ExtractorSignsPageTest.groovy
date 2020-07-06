package pl.clarin.pwr.g419.text.extractor

import org.apache.commons.io.FileUtils
import pl.clarin.pwr.g419.io.reader.HocrReader
import pl.clarin.pwr.g419.struct.FieldContext
import spock.lang.Specification
import spock.lang.Subject

class ExtractorSignsPageTest extends Specification {

    @Subject
    ExtractorSignsPage extractor = new ExtractorSignsPage()


    def "extractor should correctly find page with signs "() {
        given:
            def hocr = File.createTempFile("hocr", ".hocr")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/hocr-signs-page-2.hocr"), hocr)
            def document = new HocrReader().parse(hocr.toPath())
            final Optional<FieldContext<String>> signsPageNr = extractor.getSignsPage(document);

        expect:
            document.docContextInfo.pageNrWithSigns == 2
        and:
            signsPageNr.get().field == "2"

        cleanup:
            FileUtils.deleteQuietly(hocr)
    }

    def "extractor should correctly not find page with signs in document where page is not present"() {
        given:
            def hocr = File.createTempFile("hocr", ".hocr")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/hocr-signs-no-signs.hocr"), hocr)
            def document = new HocrReader().parse(hocr.toPath())
            final Optional<FieldContext<String>> signsPageNr = extractor.getSignsPage(document);

        expect:
            document.docContextInfo.pageNrWithSigns == 0

        and:
            signsPageNr.get().field == "0"

        cleanup:
            FileUtils.deleteQuietly(hocr)
    }


}
