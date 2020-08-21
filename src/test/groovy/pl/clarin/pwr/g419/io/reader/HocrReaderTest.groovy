package pl.clarin.pwr.g419.io.reader

import org.apache.commons.io.FileUtils
import pl.clarin.pwr.g419.struct.Box
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths

class HocrReaderTest extends Specification {

    def "Hocr parser should read a valid list of bounding boxes and construct lines"() {
        given:
            def hocr = File.createTempFile("hocr", ".hocr")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/hocr-sample.hocr"), hocr)
            def document = new HocrReader().parse(hocr.toPath())

        expect:
            document.size() == 2

        and:
            document.get(0).size() == 14

        and:
            document.get(0).get(0).getText() == "PROSPER"
            document.get(0).get(0).getBox() == new Box(305, 571, 607, 615)

        and:
            document.get(0).getLines().size() == 4

        and:
            document.get(0).getLines().get(0).getText() == "PROSPER S.A . "
            document.get(0).getLines().get(1).getText() == "Półroczne sprawozdanie finansowe "
            document.get(0).getLines().get(2).getText() == "za okres zakończony 30 czerwca 2005 roku "
            document.get(0).getLines().get(3).getText() == "1 "

        cleanup:
            FileUtils.deleteQuietly(hocr)
    }

    def "Hocr parser should read a valid list of bounding boxes with valid no"() {
        given:
            def hocr = File.createTempFile("hocr", ".hocr")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/hocr-sample.hocr"), hocr)
            def document = new HocrReader().parse(hocr.toPath())

        expect:
            document.get(0).get(0).getNo() == 1
            document.get(0).get(1).getNo() == 2

        and:
            document.get(1).get(0).getNo() == document.get(0).size()

        cleanup:
            FileUtils.deleteQuietly(hocr)
    }

    @Unroll
    def "bbox no #no should have lineBegin #lineBegin and lineEnd #lineEnd"() {
        given:
            def hocr = File.createTempFile("hocr", ".hocr")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/hocr-sample.hocr"), hocr)
            def document = new HocrReader().parse(hocr.toPath())

        expect:
            document.get(0).get(no).isLineBegin() == lineBegin
            document.get(0).get(no).isLineEnd() == lineEnd

        cleanup:
            FileUtils.deleteQuietly(hocr)

        where:
            no || lineBegin | lineEnd
            0  || true      | false
            1  || false     | false
            2  || false     | true
            3  || true      | false
            4  || false     | false
            5  || false     | true

    }

    @Unroll
    def "bbox no #no should have lineEnd #lineEnd and blockEnd #blockEnd"() {
        given:
            def hocr = File.createTempFile("hocr", ".hocr")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/hocr-sample-blocks.hocr"), hocr)
            def document = new HocrReader().parse(hocr.toPath())

        expect:
            document.get(0).get(no).isBlockEnd() == blockEnd
            document.get(0).get(no).isLineEnd() == lineEnd

        cleanup:
            FileUtils.deleteQuietly(hocr)

        where:
            no || blockEnd | lineEnd
            9  || true     | false
            10 || true     | false
            12 || true     | true
            33 || true     | true

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

    @Unroll
    def "bbox #no should have text value of #text (test encoding fixing)"() {
        given:
            def hocr = File.createTempFile("hocr", ".hocr")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/hocr-sample-encoding.hocr"), hocr)
            def document = new HocrReader().parse(hocr.toPath())

        expect:
            document.get(0).get(no).getText() == text

        cleanup:
            FileUtils.deleteQuietly(hocr)

        where:
            no  || text
            7   || "PÓŁROCZE"
            190 || "Zarządu"

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


    def "Hocr parser and sorting BBoxes should give a correct order of bounding boxes"() {
        given:
            def hocr = File.createTempFile("hocr", ".hocr")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/hocr-sample.hocr"), hocr)
            def document = new HocrReader().parseAndSortBboxes(hocr.toPath())

        expect:
            document.get(0).get(document.get(0).size() - 1).getText() == "1"

        cleanup:
            FileUtils.deleteQuietly(hocr)
    }

    def "Hocr parser without eliminating and sorting BBoxes should give duplicates of redundant bounding boxes"() {
        given:
            def hocr = File.createTempFile("hocr", ".hocr")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/hocr-redundant-bboxes.hocr"), hocr)

        when:
            def documentOnlyParse = new HocrReader().parse(hocr.toPath())

        then:
            documentOnlyParse.get(0).getLines().stream()
                    .map { l -> l.getText() }
                    .filter { text -> text.startsWith("PODPIS OSOBY ,KTÓREJ POWIERZONO PROWADZENIE KSIĄG") }
                    .count() == 3
        and:
            documentOnlyParse.get(0).getLines().stream()
                    .map { l -> l.getText() }
                    .filter { text -> text.endsWith("30 czerwca 2011 r . ") }
                    .count() == 4

        cleanup:
            FileUtils.deleteQuietly(hocr)
    }


    def "Hocr parser and eliminating and sorting BBoxes should give a correct order of not-redundant bounding boxes"() {
        given:
            def hocr = File.createTempFile("hocr", ".hocr")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/hocr-redundant-bboxes.hocr"), hocr)

        when:
            def documentParseAndEliminateAndSort = new HocrReader().parseAndSortBboxes(hocr.toPath())

        then:
            documentParseAndEliminateAndSort.get(0).getLines().stream()
                    .map { l -> l.getText() }
                    .filter { text -> text.startsWith("PODPIS OSOBY ,KTÓREJ POWIERZONO PROWADZENIE KSIĄG") }
                    .count() == 1

        and:
            documentParseAndEliminateAndSort.get(0).getLines().stream()
                    .map { l -> l.getText() }
                    .filter { text -> text.endsWith("30 czerwca 2011 r . ") }
                    .count() == 2


        cleanup:
            FileUtils.deleteQuietly(hocr)
    }


    def "Hocr method splitInterpunctionEnd should correctly process dates"() {
        given:
            def hocr = File.createTempFile("hocr", ".hocr")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/hocr-sample-dates.hocr"), hocr)
            def document = new HocrReader().parseAndSortBboxes(hocr.toPath())

        expect:
            document.get(0).get(0).getText() == "30.08.2011"
            document.get(0).get(1).getText() == "od 01.04.2008 do 30.09.2008"

        cleanup:
            FileUtils.deleteQuietly(hocr)
    }


}
