package pl.clarin.pwr.g419.struct

import spock.lang.Specification
import spock.lang.Unroll

class BboxTest extends Specification {

    @Unroll
    def "#text should give #niceLowText "() {
        given:
            def Bbox b = new Bbox(1, text, null);

        when:
            def result = b.getLowNiceText()

        then:
            result == niceLowText

        where:
            text        || niceLowText
            "raz,"      || "raz"
            ".,dwa!"    || "dwa"
            " trzy ? ?" || "trzy"
            "pięć"      || "pięć"
            "?!? żółw"  || "żółw"
            "pięć5@!"   || "pięć"
    }


}
