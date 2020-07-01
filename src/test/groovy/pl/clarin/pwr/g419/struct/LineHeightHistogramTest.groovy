package pl.clarin.pwr.g419.struct

import org.apache.commons.io.FileUtils
import pl.clarin.pwr.g419.io.reader.HocrReader
import pl.clarin.pwr.g419.struct.LineHeightHistogram
import spock.lang.Specification

class LineHeightHistogramTest extends Specification {

    def "Hocr parser should read a valid list of bounding boxes and construct lines and histogram"() {
        given:
            def hocr = File.createTempFile("hocr", ".hocr")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/hocr-sample.hocr"), hocr)
            def document = new HocrReader().parse(hocr.toPath())

        expect:
            new LineHeightHistogram(document.get(0)).data.size() == 3

        and:
            new LineHeightHistogram(document).data.size() == 10

        and:
            new LineHeightHistogram(document.get(0)).data.get(30).size() == 1
            new LineHeightHistogram(document.get(0)).data.get(44).size() == 1
            new LineHeightHistogram(document.get(0)).data.get(60).size() == 2

        and:
            new LineHeightHistogram(document).data.get(60).size() == 6

        cleanup:
            FileUtils.deleteQuietly(hocr)
    }
}
