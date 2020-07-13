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

    final double outerLeft = Math.min(this.getLeft(), target.getLeft());
    final double outerRight = Math.max(this.getRight(), target.getRight());
    final double outerWidth = Math.abs(outerRight - outerLeft);

    final double innerLeft = Math.max(this.getLeft(), target.getLeft());
    final double innerRight = Math.min(this.getRight(), target.getRight());
    final double innerWidth = Math.abs(innerRight - innerLeft);

    return innerWidth / outerWidth;
  }

  default double overlapY(final Contour target) {
    if ((this.getBottom() <
        target.getTop())
        || (target.getBottom() <
        this.getTop())) {
      return 0;
    }

    final double outerTop = Math.min(this.getTop(), target.getTop());
    final double outerBottom = Math.max(this.getBottom(), target.getBottom());
    final double outerHeight = Math.abs(outerBottom - outerTop);

    final double innerTop = Math.max(this.getTop(), target.getTop());
    final double innerBottom = Math.min(this.getBottom(), target.getBottom());
    final double innerHeight = Math.abs(innerBottom - innerTop);

    return innerHeight / outerHeight;
  }

  default int distanceXTo(final Contour target) {
    return Math.abs(target.getCenterX() - this.getCenterX());
  }

  default int distanceYTo(final Contour target) {
    return Math.abs(target.getCenterY() - this.getCenterY());
  }

  // zostawiamy odległość podniesioną do kwadratu by nie wchodzić
  // w przetwarzanie zmiennoprzecinkowe - i tak nie jest nam
  // potrzebna sama odległośći tylko jej porównanie z innymi
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
