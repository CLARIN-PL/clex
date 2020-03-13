package pl.clarin.pwr.g419.io.reader

import org.apache.commons.io.FileUtils
import pl.clarin.pwr.g419.struct.Box
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths

class HocrReaderTest extends Specification {

    def "Hocr parser should read a valid list of bounding boxes"() {
        given:
            def hocr = File.createTempFile("hocr", ".hocr")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/hocr-sample.hocr"), hocr)
            def document = new HocrReader().parse(hocr.toPath())

        expect:
            document.size() == 2

        and:
            document.get(0).size() == 13

        and:
            document.get(0).get(0).getText() == "PROSPER"
            document.get(0).get(0).getBox() == new Box(305, 571, 607, 615)

        cleanup:
            FileUtils.deleteQuietly(hocr)
    }

    @Unroll
    def "cleanHocr(#input) should return '#expected'"() {
        when:
            def output = HocrReader.cleanHocr(input)

        then:
            output == expected

        where:
            input                                                          || expected
            "aaa- Page #12\nbbb"                                           || "aaabbb"
            "aaa\nbbb"                                                     || "aaa\nbbb"
            "Converting '/tmp/b0018609-c98f-4903-b6fa-6c4c68a5bd4a':\naaa" || "aaa"

    }

    @Unroll
    def "titleToBox(#title) should return #box"() {
        when:
            def output = HocrReader.titleToBox(title)

        then:
            output == box

        where:
            title             || box
            "aaa 10 20 30 40" || new Box(10, 20, 30, 40)
    }

    def "getIdFromPath(#path) should return #id"() {
        when:
            def output = HocrReader.getIdFromPath(Paths.get(path))

        then:
            output == id

        where:
            path                                                                         || id
            "../task4-train/reports/105194/Grupa_AMBRA_raport_polroczny_31.12.2008.hocr" || "105194"
    }
}
