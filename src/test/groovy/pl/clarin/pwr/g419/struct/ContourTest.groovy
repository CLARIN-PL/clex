package pl.clarin.pwr.g419.struct

import spock.lang.Specification
import spock.lang.Unroll

class ContourTest extends Specification {

    @Unroll
    def "[#cl,#ct,#cr,#cb].width() should return #width"() {
        given:
            def c1 = new Box(cl, ct, cr, cb)
        when:
            def result = c1.getWidth()
        then:
            width == result
        where:
            cl  | ct  | cr | cb  || width
            10  | 100 | 50 | 500 || 40
            0   | 100 | 0  | 400 || 0
            100 | 0   | 10 | 200 || -90
    }

    @Unroll
    def "[#cl,#ct,#cr,#cb].height() should return #height"() {
        given:
            def c1 = new Box(cl, ct, cr, cb)
        when:
            def result = c1.getHeight()
        then:
            height == result
        where:
            cl  | ct  | cr | cb  || height
            10  | 100 | 50 | 500 || 400
            0   | 100 | 0  | 400 || 300
            100 | 0   | 10 | 200 || 200
            100 | 301 | 10 | 301 || 0
    }

    @Unroll
    def "[#cl,#ct,#cr,#cb].centerX() should return #centerX"() {
        given:
            def c1 = new Box(cl, ct, cr, cb)
        when:
            def result = c1.getCenterX()
        then:
            centerX == result
        where:
            cl  | ct  | cr  | cb  || centerX
            10  | 100 | 50  | 500 || 30
            0   | 100 | 0   | 400 || 0
            100 | 0   | 10  | 200 || 55
            100 | 301 | 101 | 301 || 100
    }

    @Unroll
    def "[#cl,#ct,#cr,#cb].centerY() should return #centerY"() {
        given:
            def c1 = new Box(cl, ct, cr, cb)
        when:
            def result = c1.getCenterY()
        then:
            centerY == result
        where:
            cl  | ct  | cr  | cb  || centerY
            10  | 100 | 50  | 500 || 300
            0   | 100 | 0   | 400 || 250
            100 | 0   | 10  | 200 || 100
            100 | 301 | 101 | 301 || 301
    }


    @Unroll
    def "[#c1l,#c1t,#c1r,#c1b].overlapX([#c2l,#c2t,#c2r,#c2b]) should return #overlapX"() {
        given:
            def c1 = new Box(c1l, c1t, c1r, c1b)
            def c2 = new Box(c2l, c2t, c2r, c2b)
        when:
            def result = c1.overlapX(c2)
        then:
            overlapX == result
        where:
            c1l | c1t | c1r | c1b | c2l | c2t | c2r | c2b || overlapX
            10  | 100 | 50  | 500 | 10  | 100 | 50  | 500 || 1
            10  | 0   | 50  | 0   | 10  | 100 | 50  | 500 || 1
            10  | 100 | 50  | 500 | 0   | 20  | 0   | 50  || 0
            0   | 100 | 1   | 400 | 0   | 100 | 50  | 500 || 0.02
            0   | 100 | 50  | 500 | 25  | 100 | 50  | 500 || 0.5
            25  | 100 | 50  | 500 | 0   | 100 | 50  | 500 || 0.5
            0   | 100 | 50  | 500 | 25  | 100 | 80  | 500 || 0.3125
            0   | 100 | 50  | 500 | 25  | 100 | 100 | 500 || 0.25
    }

    @Unroll
    def "[#c1l,#c1t,#c1r,#c1b].overlapY([#c2l,#c2t,#c2r,#c2b]) should return #overlapY"() {
        given:
            def c1 = new Box(c1l, c1t, c1r, c1b)
            def c2 = new Box(c2l, c2t, c2r, c2b)
        when:
            def result = c1.overlapY(c2)
        then:
            overlapY == result
        where:
            c1l | c1t | c1r | c1b | c2l | c2t | c2r | c2b || overlapY
            10  | 100 | 50  | 500 | 10  | 100 | 50  | 500 || 1
            10  | 0   | 50  | 0   | 10  | 100 | 50  | 500 || 0
            10  | 100 | 50  | 500 | 0   | 20  | 0   | 50  || 0
            0   | 100 | 1   | 400 | 0   | 100 | 50  | 500 || 0.75
            0   | 100 | 50  | 200 | 25  | 200 | 50  | 500 || 0.0
            0   | 100 | 50  | 200 | 25  | 150 | 80  | 200 || 0.5
            0   | 150 | 50  | 200 | 25  | 100 | 100 | 200 || 0.5
    }


    @Unroll
    def "[#c1l,#c1t,#c1r,#c1b].distanceXTo([#c2l,#c2t,#c2r,#c2b]) should return #distanceXTo"() {
        given:
            def c1 = new Box(c1l, c1t, c1r, c1b)
            def c2 = new Box(c2l, c2t, c2r, c2b)
        when:
            def result = c1.distanceXTo(c2)
        then:
            distanceXTo == result
        where:
            c1l | c1t | c1r | c1b | c2l | c2t | c2r | c2b || distanceXTo
            10  | 100 | 50  | 500 | 10  | 100 | 50  | 500 || 0
            10  | 0   | 50  | 0   | 10  | 100 | 50  | 500 || 0
            10  | 100 | 50  | 500 | 0   | 20  | 0   | 50  || 30
            0   | 100 | 1   | 400 | 0   | 100 | 50  | 500 || 25
            0   | 100 | 50  | 500 | 25  | 100 | 50  | 500 || 12
            25  | 100 | 50  | 500 | 0   | 100 | 50  | 500 || 12
            0   | 100 | 50  | 500 | 25  | 100 | 80  | 500 || 27
            0   | 100 | 50  | 500 | 25  | 100 | 100 | 500 || 37
    }


    @Unroll
    def "[#c1l,#c1t,#c1r,#c1b].distanceYTo([#c2l,#c2t,#c2r,#c2b]) should return #distanceYTo"() {
        given:
            def c1 = new Box(c1l, c1t, c1r, c1b)
            def c2 = new Box(c2l, c2t, c2r, c2b)
        when:
            def result = c1.distanceYTo(c2)
        then:
            distanceYTo == result
        where:
            c1l | c1t | c1r | c1b | c2l | c2t | c2r | c2b || distanceYTo
            10  | 100 | 50  | 500 | 10  | 100 | 50  | 500 || 0
            10  | 0   | 50  | 0   | 10  | 100 | 50  | 500 || 300
            10  | 100 | 50  | 500 | 0   | 20  | 0   | 50  || 265
            0   | 100 | 1   | 400 | 0   | 100 | 50  | 500 || 50
            0   | 200 | 50  | 500 | 25  | 100 | 50  | 500 || 50
            25  | 100 | 50  | 108 | 0   | 100 | 50  | 500 || 196
            0   | 100 | 50  | 500 | 25  | 100 | 80  | 108 || 196
            0   | 100 | 50  | 200 | 25  | 200 | 100 | 300 || 100
    }


}
