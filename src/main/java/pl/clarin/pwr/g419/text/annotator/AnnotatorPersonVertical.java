package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import pl.clarin.pwr.g419.struct.*;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.utils.BboxUtils;

public class AnnotatorPersonVertical extends Annotator {

  java.util.regex.Pattern personPattern = java.util.regex.Pattern.compile("\\p{Lu}\\p{Ll}+( \\p{Lu}\\p{Ll}+){1,2}");

  private static List<Pattern> getPatterns() {
    final List<Pattern> patterns = Lists.newArrayList();
    return patterns;
  }

  public AnnotatorPersonVertical() {
    super(AnnotatorPersonHorizontal.PERSON, getPatterns());
  }

  @Override
  protected String normalize(final PatternMatch pm) {
    return String.format("%s|%s",
        pm.getGroupValue(AnnotatorPersonHorizontal.TITLE).orElse(""),
        pm.getGroupValue(AnnotatorPersonHorizontal.NAME).orElse(""));
  }

  @Override
  public void annotate(final HocrPage page) {
    final AnnotationList al = page.getAnnotationList()
        .filterByType(AnnotatorRole.ROLE);

    final List<Annotation> blocks =
        al.stream().filter(this::isBlock).collect(Collectors.toList());

    final List<Pair<Range, Bboxes>> lines = BboxUtils.createLines2(page);

    for (final Annotation an : blocks) {
      final int begin = page.get(an.getIndexBegin()).getBox().getLeft() - 100;
      final int end = page.get(an.getIndexEnd() - 1).getBox().getRight() + 200;

      final Optional<Bboxes> lineAbove = findLineAbove(an, lines, page);
      if (lineAbove.isPresent()) {
        final Bboxes block = new Bboxes(lineAbove.get().stream()
            .filter(b -> b.getBox().getLeft() > begin && b.getBox().getRight() < end)
            .collect(Collectors.toList()));
        if (block.size() > 0 && personPattern.matcher(block.getText()).matches()
            && !block.getText().contains("Zarządu")) {
          final Annotation person = new Annotation(AnnotatorPersonHorizontal.PERSON, page,
              page.indexOf(block.getFirst()), page.indexOf(block.getLast()));
          person.setNorm(Optional.of(String.format("%s|%s", an.getNorm(), block.getText())));
          page.getAnnotations().add(person);
        }
      }
    }

  }

  private boolean isBlock(final Annotation an) {
    final HocrPage page = an.getPage();
    if (page.get(an.getIndexEnd() - 1).isBlockEnd()) {
      return true;
    } else {
      return false;
    }
  }

  private Optional<Bboxes> findLineAbove(final Annotation an,
                                         final List<Pair<Range, Bboxes>> lines,
                                         final HocrPage page) {
    final Bbox firstBbox = page.get(an.getIndexBegin());
    final Range firsRange = new Range(firstBbox.getBox().getTop(), firstBbox.getBox().getBottom());
    for (int i = 1; i < lines.size(); i++) {
      final Range line = lines.get(i).getKey();
      if (firsRange.within(line) > 0.9) {
        return Optional.of(lines.get(i - 1).getValue());
      }
    }
    return Optional.empty();
  }
}
