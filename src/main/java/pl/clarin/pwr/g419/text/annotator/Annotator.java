package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import pl.clarin.pwr.g419.HasLogger;
import pl.clarin.pwr.g419.struct.Annotation;
import pl.clarin.pwr.g419.struct.HocrLine;
import pl.clarin.pwr.g419.struct.HocrPage;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;

@Slf4j
public class Annotator implements HasLogger {

  List<Pattern> patterns;
  String type;

  public Annotator(final String type, final Pattern pattern) {
    this.type = type;
    this.patterns = Lists.newArrayList(pattern);
  }

  public Annotator(final String type, final List<Pattern> patterns) {
    this.type = type;
    this.patterns = patterns;
  }

  public void annotate(final HocrPage page) {
    patterns.stream()
        .map(p -> p.find(page))
        .flatMap(Collection::stream)
        .map(m ->
            new Annotation(type, page, m.getIndexBegin(), m.getIndexEnd())
                .withNorm(normalize(m))
                .withScore(m.getScore())
                .withSource(m.getSource()))
        .filter(ann -> isValid(ann))
        .forEach(page::addAnnotation);

//    diagnostyka
//
//    page.getAnnotations().stream()
//        .filter(an -> an.getType() == AnnotatorHeadQuarters.HEADQUARTERS)
//        .forEach(an -> log.debug("DID:\t" + page.getDocument().getId() + "\t PG:\t" + page.getNo() + "\t an=\t" + an.getWholeLineText()));

//    log.debug("[p=" + page.getNo() + "] Anns:" + page.getAnnotations());
  }

  protected String normalize(final PatternMatch pm) {
    return pm.getText();
  }

  protected boolean isValid(final Annotation ann) { return true; }

  protected static Pattern patternSequence(final String sequence, final String name) {
    final Pattern pattern = new Pattern(name);
    for (final String word : sequence.split(" ")) {
      pattern.next(new MatcherLowerText(word));
    }
    return pattern;
  }

  protected static Pattern patternRegexpSequence(final String sequence, final String name) {
    final Pattern pattern = new Pattern(name);
    for (final String word : sequence.split(" ")) {
      pattern.next(new MatcherRegexText(word, 2));
    }
    return pattern;
  }


}
