public class Encoder {
  public static double[] encode(Board board) {
    double[] vec = new double[768];
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        String piece = board.pieceAt(row, col);
        if (piece.equals("")) {
          continue;
        }
        String color = piece.split("_")[0];
        String name = piece.split("_")[1];
        int colorBase = color.equals("white") ? 0 : 6;
        int base = (row * 8 + col) * 12;
        vec[base + colorBase + typeIndex(name)] = 1;
      }
    }
    return vec;
  }

  static int typeIndex(String name) {
    switch (name) {
      case "pawn":
        return 0;
      case "knight":
        return 1;
      case "bishop":
        return 2;
      case "rook":
        return 3;
      case "queen":
        return 4;
      case "king":
        return 5;
      default:
        return -1;
    }
  }

  static double material(double[] vec) {
    double[] val = { 1, 3, 3, 5, 9, 100 };
    double sum = 0;
    for (int i = 0; i < vec.length; i++) {
      if (vec[i] != 0) {
        int slot = i % 12;
        double v = val[slot % 6] / 100;
        sum += (slot < 6) ? v : -v;
      }
    }
    return sum;
  }
}
