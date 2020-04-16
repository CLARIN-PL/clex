package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import pl.clarin.pwr.g419.kbase.NeLexicon2;
import pl.clarin.pwr.g419.kbase.lexicon.FirstNameLexicon;
import pl.clarin.pwr.g419.struct.*;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.utils.BboxUtils;

public class AnnotatorPersonVertical extends Annotator {

  java.util.regex.Pattern personPattern =
      java.util.regex.Pattern.compile("\\p{Lu}\\p{Ll}+( \\p{Lu}\\p{Ll}+){1,2}");

  NeLexicon2 neLexicon2 = NeLexicon2.get();
  FirstNameLexicon firstNameLexicon = new FirstNameLexicon();

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

      final Bbox anFirstBox = page.get(an.getIndexBegin());
      Optional<Bboxes> name = Optional.empty();
      String source = "";

      final Optional<Bboxes> blockAbove = findBlockAbove(an, begin, end, lines, page);
      if (blockAbove.isPresent()
          && Math.abs(anFirstBox.getBox().getTop() - blockAbove.get().getBottom().getAsInt()) < 60
      ) {
        name = blockAbove;
        source = "person-ver:name-above";
      }

      if (name.isEmpty()) {
        final Optional<Bboxes> lineBelow = findLineBelow(an, lines, page);
        if (lineBelow.isPresent()
            && Math.abs(anFirstBox.getBox().getBottom() - lineBelow.get().getTop().getAsInt()) < 30) {
          name = extractName(lineBelow.get(), begin, end);
          source = "person-ver:name-below";
        }
      }

      if (name.isPresent()) {
        final Annotation person = new Annotation(AnnotatorPersonHorizontal.PERSON, page,
            an.getIndexBegin(), an.getIndexEnd());
        person.setNorm(Optional.of(String.format("|%s|%s", an.getNorm(), name.get().getText())));
        person.setSource(source);
        person.setScore(Score.HIGH);
        page.getAnnotations().add(person);
      }
    }

  }

  private Optional<Bboxes> extractName(final Bboxes line,
                                       final int begin,
                                       final int end) {
    final Bboxes block = new Bboxes(line.stream()
        .filter(b -> b.getBox().getLeft() > begin && b.getBox().getRight() < end)
        .collect(Collectors.toList()));
    if (block.size() > 0 && personPattern.matcher(block.getText()).matches()
        && isFirstName(block.getFirst().getText())) {
      return Optional.of(block);
    }
    return Optional.empty();
  }

  private boolean isFirstName(final String name) {
    return firstNameLexicon.contains(name)
        || neLexicon2.getNames(NeLexicon2.LIV_PERSON_FIRST).contains(name);
  }

  private boolean isBlock(final Annotation an) {
    final HocrPage page = an.getPage();
    if (page.get(an.getIndexEnd() - 1).isBlockEnd()) {
      return true;
    } else {
      return false;
    }
  }

  private Optional<Bboxes> findBlockAbove(final Annotation an,
                                          final int left, final int right,
                                          final List<Pair<Range, Bboxes>> lines,
                                          final HocrPage page) {
    final Bbox firstBbox = page.get(an.getIndexBegin());
    return lines.stream()
        .filter(line -> line.getLeft().getUpperBound() < firstBbox.getBox().getTop())
        .map(line -> extractName(line.getRight(), left, right))
        .filter(b -> b.isPresent() && b.get().size() > 0)
        .map(Optional::get)
        .sorted((o1, o2) -> Integer.compare(o2.getBottom().getAsInt(), o1.getBottom().getAsInt()))
        .findFirst();
  }

  private Optional<Bboxes> findLineBelow(final Annotation an,
                                         final List<Pair<Range, Bboxes>> lines,
                                         final HocrPage page) {
    final Bbox firstBbox = page.get(an.getIndexBegin());
    final Range firsRange = new Range(firstBbox.getBox().getTop(), firstBbox.getBox().getBottom());
    for (int i = 0; i < lines.size() - 1; i++) {
      final Range line = lines.get(i).getKey();
      if (firsRange.within(line) > 0.9) {
        return Optional.of(lines.get(i + 1).getValue());
      }
    }
    return Optional.empty();
  }

}
