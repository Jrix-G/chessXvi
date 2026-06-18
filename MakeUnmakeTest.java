import java.util.*;

public class MakeUnmakeTest {
  static Random r = new Random(42);

  static String sig(Board b) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        sb.append(b.codeAt(i, j)).append(',');
      }
    }
    sb.append('|');
    for (int[] m : b.legalMoves("white")) {
      sb.append(m[0]).append(m[1]).append(m[2]).append(m[3]).append(' ');
    }
    sb.append('|');
    for (int[] m : b.legalMoves("black")) {
      sb.append(m[0]).append(m[1]).append(m[2]).append(m[3]).append(' ');
    }
    return sb.toString();
  }

  public static void main(String[] a) {
    int games = a.length > 0 ? Integer.parseInt(a[0]) : 50;
    long checks = 0;
    long applyFails = 0;
    long undoFails = 0;
    for (int g = 0; g < games; g++) {
      Board b = new Board();
      String turn = "white";
      for (int ply = 0; ply < 120; ply++) {
        List<int[]> moves = b.legalMoves(turn);
        if (moves.isEmpty()) {
          break;
        }
        String before = sig(b);
        for (int[] m : moves) {
          Board copy = b.copy();
          copy.move(m[0], m[1], m[2], m[3]);
          String copySig = sig(copy);
          Board.Undo u = b.makeMove(m[0], m[1], m[2], m[3]);
          if (!sig(b).equals(copySig)) {
            applyFails++;
            if (applyFails <= 5) {
              System.out.println("APPLY mismatch g" + g + " ply" + ply + " " + m[0] + m[1] + m[2] + m[3]);
            }
          }
          b.unmakeMove(u);
          if (!sig(b).equals(before)) {
            undoFails++;
            if (undoFails <= 5) {
              System.out.println("UNDO mismatch g" + g + " ply" + ply + " " + m[0] + m[1] + m[2] + m[3]);
            }
          }
          checks++;
        }
        int[] mv = moves.get(r.nextInt(moves.size()));
        b.move(mv[0], mv[1], mv[2], mv[3]);
        turn = turn.equals("white") ? "black" : "white";
      }
    }
    System.out.println("checks=" + checks + " applyFails=" + applyFails + " undoFails=" + undoFails);
  }
}
