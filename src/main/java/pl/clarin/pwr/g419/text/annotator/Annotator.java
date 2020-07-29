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

    log.trace("[p=" + page.getNo() + "] Anns:" + page.getAnnotations());
  }

  protected String normalize(final PatternMatch pm) {
    return pm.getText();
  }

  protected boolean isValid(final Annotation ann) { return true; }

}
