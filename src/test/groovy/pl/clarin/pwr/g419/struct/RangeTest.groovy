package pl.clarin.pwr.g419.struct

import spock.lang.Specification
import spock.lang.Unroll

class RangeTest extends Specification {

    @Unroll
    def "[#r1l,#r1u].overlap([#r2l,#r2u]) should return #overlap"() {
        given:
            def r1 = new Range(null, r1l, r1u)
            def r2 = new Range(null, r2l, r2u)

        when:
            def result = r1.overlap(r2)

        then:
            Math.abs(result - overlap) < 0.001

        where:
            r1l | r1u | r2l | r2u || overlap
            0   | 10  | 0   | 10  || 1.0
            0   | 10  | 0   | 5   || 0.5
            0   | 5   | 0   | 10  || 0.5
            0   | 10  | 20  | 20  || 0.0
            20  | 30  | 0   | 10  || 0.0
            0   | 10  | 5   | 15  || 0.5
            0   | 20  | 5   | 15  || 0.5
    }

    @Unroll
    def "[#r1l,#r1u].within([#r2l,#r2u]) should return #within"() {
        given:
            def r1 = new Range(null, r1l, r1u)
            def r2 = new Range(null, r2l, r2u)

        when:
            def result = r1.within(r2)

        then:
            Math.abs(result - within) < 0.001

        where:
            r1l | r1u | r2l | r2u || within
            0   | 10  | 0   | 10  || 1.0
            0   | 10  | 0   | 5   || 0.5
            0   | 5   | 0   | 10  || 1.0
            0   | 10  | 20  | 20  || 0.0
            20  | 30  | 0   | 10  || 0.0
            0   | 10  | 5   | 15  || 0.5
            0   | 20  | 5   | 15  || 0.5
    }

    @Unroll
    def "[#r1l,#r1u].merge(#r2l,#r2u) should return [ml, mu]"() {
        given:
            def r1 = new Range(null, r1l, r1u)

        when:
            r1.merge(r2l, r2u)

        then:
            r1.getTopBound() == ml
            r1.getBottomBound() == mu

        where:
            r1l | r1u | r2l | r2u || ml | mu
            0   | 10  | 0   | 10  || 0  | 10
            0   | 10  | 0   | 5   || 0  | 10
            0   | 5   | 0   | 10  || 0  | 10
            0   | 10  | 20  | 20  || 0  | 20
            20  | 30  | 0   | 10  || 0  | 30
            0   | 10  | 5   | 15  || 0  | 15
            0   | 20  | 5   | 15  || 0  | 20
    }
}
