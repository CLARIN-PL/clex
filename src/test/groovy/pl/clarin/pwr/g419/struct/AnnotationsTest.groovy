package pl.clarin.pwr.g419.struct


import spock.lang.Specification

class AnnotationsTest extends Specification {

    def "when annotation is added the index of annotation should be updated"() {
        given:
            def page = new HocrPage(null,
                    getSequenceOfBboxes(["od", "1", "stycznia", "2020", "do", "2", "LUTEGO", "2019"] as List))
            def annotation = new Annotation("date", page, 1, 4)
            def annotations = new Annotations()

        when:
            annotations.add(annotation)

        then:
            annotations.indexToAnnotations.size() == 3

        and:
            annotations.indexToAnnotations.get(1) == [annotation] as Set
            annotations.indexToAnnotations.get(2) == [annotation] as Set
            annotations.indexToAnnotations.get(3) == [annotation] as Set
    }

    def getSequenceOfBboxes(List<String> words) {
        Box box = new Box(0, 0, 10, 10)
        return words.collect { new Bbox(0, it, box) } as List
    }

}
