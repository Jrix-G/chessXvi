import java.util.*;

public class Arena {
  static Random rnd = new Random();

  // joue une partie : 'net' joue netColor, l'autre joue au hasard.
  // renvoie +1 si net gagne, 0 nulle, -1 si net perd
  static int playGame(Network net, String netColor) {
    Bot botNet = new Bot(net, netColor);
    Board board = new Board();
    String turn = "white";
    for (int ply = 0; ply < 400; ply++) {
      List<int[]> moves = board.legalMoves(turn);
      if (moves.isEmpty()) {
        if (board.isInCheck(turn)) {
          String winner = turn.equals("white") ? "black" : "white";
          return winner.equals(netColor) ? 1 : -1;
        }
        return 0;
      }
      int[] m;
      if (turn.equals(netColor)) {
        m = botNet.bestMove(board);
      } else {
        m = moves.get(rnd.nextInt(moves.size()));
      }
      board.move(m[0], m[1], m[2], m[3]);
      turn = turn.equals("white") ? "black" : "white";
    }
    return 0;
  }

  // N parties (moitie blanc, moitie noir), renvoie le score de net dans [0,1]
  static double score(Network net, int n) {
    double points = 0;
    for (int i = 0; i < n; i++) {
      String netColor = (i % 2 == 0) ? "white" : "black";
      int r = playGame(net, netColor);
      points += (r == 1) ? 1.0 : (r == 0 ? 0.5 : 0.0);
    }
    return points / n;
  }

  // Elo relatif vs bot aleatoire (ancre = 0)
  static double elo(double s) {
    if (s <= 0) {
      return -800;
    }
    if (s >= 1) {
      return 800;
    }
    return 400 * Math.log10(s / (1 - s));
  }

  public static void main(String[] args) throws Exception {
    int n = args.length > 0 ? Integer.parseInt(args[0]) : 200;
    java.io.File[] files = new java.io.File("weights").listFiles((d, name) -> name.matches("weights_\\d+\\.txt"));
    List<Integer> games = new ArrayList<>();
    if (files != null) {
      for (java.io.File f : files) {
        games.add(Integer.parseInt(f.getName().replaceAll("\\D", "")));
      }
    }
    Collections.sort(games);
    System.out.println("games,score,elo");
    for (int g : games) {
      Network net = Network.load("weights/weights_" + g + ".txt");
      double s = score(net, n);
      System.out.println(g + "," + String.format(Locale.US, "%.3f", s)
          + "," + String.format(Locale.US, "%.0f", elo(s)));
    }
  }
}
