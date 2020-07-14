package pl.clarin.pwr.g419.struct;

public interface Contour {

  int getLeft();

  int getRight();

  int getTop();

  int getBottom();

  default int getWidth() {
    return getRight() - getLeft();
  }

  default int getHeight() {
    return getBottom() - getTop();
  }

  default int getCenterX() {
    return getLeft() + getWidth() / 2;
  }

  default int getCenterY() {
    return getTop() + getHeight() / 2;
  }


  default double overlapX(final Contour target) {
    if ((this.getRight() < target.getLeft())
        ||
        (target.getRight() < this.getLeft())) {
      return 0;
    }
    return overlapCommon(this.getLeft(), this.getRight(), target.getLeft(), target.getRight());
  }

  default double overlapY(final Contour target) {
    if ((this.getBottom() < target.getTop())
        ||
        (target.getBottom() < this.getTop())) {
      return 0;
    }
    return overlapCommon(this.getTop(), this.getBottom(), target.getTop(), target.getBottom());
  }

  private double overlapCommon(int thisFirst, int thisLast, int thatFirst, int thatLast) {

    final double outerFirst = Math.min(thisFirst, thatFirst);
    final double outerLast = Math.max(thisLast, thatLast);
    final double outerRange = Math.abs(outerLast - outerFirst);

    final double innerFirst = Math.max(thisFirst, thatFirst);
    final double innerLast = Math.min(thisLast, thatLast);
    final double innerRange = Math.abs(innerLast - innerFirst);

    return innerRange / outerRange;
  }


  default int distanceXTo(final Contour target) {
    return Math.abs(target.getCenterX() - this.getCenterX());
  }

  default int distanceYTo(final Contour target) {
    return Math.abs(target.getCenterY() - this.getCenterY());
  }

  // zostawiamy odległość podniesioną do kwadratu by nie wchodzić
  // w przetwarzanie zmiennoprzecinkowe - i tak nie jest nam
  // potrzebna sama odległość tylko jej porównanie z innymi
  default int distanceSqrTo(final Contour target) {
    return (distanceXTo(target)) * (distanceXTo(target))
        +
        (distanceYTo(target)) * (distanceYTo(target));
  }


  default String toCoords() {
    return " [t:" + getTop() + ",b:" + getBottom() + "] " +
        "[l:" + getLeft() + ",r:" + getRight() + "]";
  }


}
