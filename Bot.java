public class Bot {
  Network network;
  String color;
  int depth = 4;

  public Bot(Network network, String color) {
    this.network = network;
    this.color = color;
  }

  public int[] bestMove(Board board) {
    int[] best = null;
    double bestEval = color.equals("white") ? -1e9 : 1e9;
    for (int[] m : board.legalMoves(color)) {
      Board sim = board.copy();
      sim.move(m[0], m[1], m[2], m[3]);
      String next = color.equals("white") ? "black" : "white";
      double eval = minimax(sim, depth - 1, next, -1e9, 1e9);
      boolean better = color.equals("white") ? eval > bestEval : eval < bestEval;
      if (better) {
        bestEval = eval;
        best = m;
      }
    }
    return best;
  }

  double minimax(Board board, int d, String turn, double alpha, double beta) {
    if (d == 0) {
      return network.forward(Encoder.encode(board))[0];
    }
    java.util.List<int[]> moves = board.legalMoves(turn);
    if (moves.isEmpty()) {
      return network.forward(Encoder.encode(board))[0];
    }
    String next = turn.equals("white") ? "black" : "white";
    if (turn.equals("white")) {
      double best = -1e9;
      for (int[] m : moves) {
        Board sim = board.copy();
        sim.move(m[0], m[1], m[2], m[3]);
        best = Math.max(best, minimax(sim, d - 1, next, alpha, beta));
        alpha = Math.max(alpha, best);
        if (alpha >= beta) {
          break;
        }
      }
      return best;
    } else {
      double best = 1e9;
      for (int[] m : moves) {
        Board sim = board.copy();
        sim.move(m[0], m[1], m[2], m[3]);
        best = Math.min(best, minimax(sim, d - 1, next, alpha, beta));
        beta = Math.min(beta, best);
        if (alpha >= beta) {
          break;
        }
      }
      return best;
    }
  }
}
