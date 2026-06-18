public class Eval {
  static java.util.Random rnd = new java.util.Random();

  static int playGame(Bot bot, String netColor, int maxPly) {
    Board b = new Board();
    String turn = "white";
    for (int ply = 0; ply < maxPly; ply++) {
      java.util.List<int[]> moves = b.legalMoves(turn);
      if (moves.isEmpty()) {
        if (b.isInCheck(turn)) {
          String w = turn.equals("white") ? "black" : "white";
          return w.equals(netColor) ? 1 : -1;
        }
        return 0;
      }
      int[] m;
      if (turn.equals(netColor)) {
        m = bot.bestMove(b);
      } else {
        m = moves.get(rnd.nextInt(moves.size()));
      }
      b.move(m[0], m[1], m[2], m[3]);
      turn = turn.equals("white") ? "black" : "white";
    }
    return 0;
  }

  public static void main(String[] a) {
    int n = a.length > 0 ? Integer.parseInt(a[0]) : 20;
    int depth = a.length > 1 ? Integer.parseInt(a[1]) : 2;
    String[] files = { "weights_2000.txt", "weights_4000.txt", "weights_6000.txt", "weights.txt" };
    System.out.println("file,score_vs_random (depth=" + depth + ", n=" + n + ")");
    for (String f : files) {
      java.io.File wf = new java.io.File("weights/" + f);
      if (!wf.exists()) {
        continue;
      }
      Network net = Network.load("weights/" + f);
      double pts = 0;
      for (int i = 0; i < n; i++) {
        String col = (i % 2 == 0) ? "white" : "black";
        Bot bot = new Bot(net, col);
        bot.depth = depth;
        int r = playGame(bot, col, 200);
        pts += r == 1 ? 1.0 : (r == 0 ? 0.5 : 0.0);
      }
      System.out.printf("%s,%.3f%n", f, pts / n);
    }
  }
}
