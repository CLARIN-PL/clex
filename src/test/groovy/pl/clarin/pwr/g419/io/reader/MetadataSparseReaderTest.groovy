package pl.clarin.pwr.g419.io.reader

import org.apache.commons.io.FileUtils
import spock.lang.Specification

class MetadataSparseReaderTest extends Specification {

    def "parser should read a valid metadata object"() {
        given:
            def hocr = File.createTempFile("clex", ".csv")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/metadata_sparce.csv"), hocr)

        when:
            def metadata = new MetadataSparseReader().parse(hocr.toPath())

        then:
            metadata.size() == 3

        and:
            metadata.get(0).getPeople().size() == 5
            metadata.get(1).getPeople().size() == 3
            metadata.get(2).getPeople().size() == 2

        cleanup:
            FileUtils.deleteQuietly(hocr)
    }

}
