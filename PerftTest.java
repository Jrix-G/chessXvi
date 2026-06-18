import java.util.*;

public class PerftTest {
  static Random r = new Random(7);

  static long perft(Board b, String turn, int depth, boolean useNew) {
    if (depth == 0) {
      return 1;
    }
    List<int[]> moves = useNew ? b.legalMoves(turn) : b.legalMovesRef(turn);
    if (depth == 1) {
      return moves.size();
    }
    String next = turn.equals("white") ? "black" : "white";
    long total = 0;
    for (int[] m : moves) {
      Board.Undo u = b.makeMove(m[0], m[1], m[2], m[3]);
      total += perft(b, next, depth - 1, useNew);
      b.unmakeMove(u);
    }
    return total;
  }

  static boolean check(Board b, String turn, int maxDepth, String label) {
    boolean ok = true;
    for (int d = 1; d <= maxDepth; d++) {
      long pNew = perft(b.copy(), turn, d, true);
      long pRef = perft(b.copy(), turn, d, false);
      String flag = pNew == pRef ? "OK" : "MISMATCH";
      if (pNew != pRef) {
        ok = false;
      }
      System.out.printf("%s depth %d : new=%d ref=%d  %s%n", label, d, pNew, pRef, flag);
    }
    return ok;
  }

  public static void main(String[] a) {
    boolean allOk = true;

    Board start = new Board();
    allOk &= check(start, "white", 4, "start");

    for (int g = 0; g < 8; g++) {
      Board b = new Board();
      String turn = "white";
      int plies = 6 + r.nextInt(20);
      for (int i = 0; i < plies; i++) {
        List<int[]> moves = b.legalMoves(turn);
        if (moves.isEmpty()) {
          break;
        }
        int[] m = moves.get(r.nextInt(moves.size()));
        b.move(m[0], m[1], m[2], m[3]);
        turn = turn.equals("white") ? "black" : "white";
      }
      allOk &= check(b, turn, 3, "rand" + g);
    }

    System.out.println(allOk ? "=== TOUT OK ===" : "=== DIVERGENCE DETECTEE ===");
  }
}
