import java.util.*;

public class Bot {
  Network network;
  String color;
  int depth = 4;
  static final double MATE = 1e6;
  static final int QMAX = 6;

  public Bot(Network network, String color) {
    this.network = network;
    this.color = color;
  }

  public int[] bestMove(Board board) {
    int[] best = null;
    double bestEval = color.equals("white") ? -1e9 : 1e9;
    List<int[]> moves = board.legalMoves(color);
    orderMoves(board, moves);
    String next = color.equals("white") ? "black" : "white";
    for (int[] m : moves) {
      Board.Undo u = board.makeMove(m[0], m[1], m[2], m[3]);
      double eval = minimax(board, depth - 1, next, -1e9, 1e9);
      board.unmakeMove(u);
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
      return quiesce(board, turn, alpha, beta, 0);
    }
    List<int[]> moves = board.legalMoves(turn);
    if (moves.isEmpty()) {
      if (board.isInCheck(turn)) {
        return turn.equals("white") ? -(MATE + d) : (MATE + d);
      }
      return 0;
    }
    orderMoves(board, moves);
    String next = turn.equals("white") ? "black" : "white";
    if (turn.equals("white")) {
      double best = -1e9;
      for (int[] m : moves) {
        Board.Undo u = board.makeMove(m[0], m[1], m[2], m[3]);
        best = Math.max(best, minimax(board, d - 1, next, alpha, beta));
        board.unmakeMove(u);
        alpha = Math.max(alpha, best);
        if (alpha >= beta) {
          break;
        }
      }
      return best;
    } else {
      double best = 1e9;
      for (int[] m : moves) {
        Board.Undo u = board.makeMove(m[0], m[1], m[2], m[3]);
        best = Math.min(best, minimax(board, d - 1, next, alpha, beta));
        board.unmakeMove(u);
        beta = Math.min(beta, best);
        if (alpha >= beta) {
          break;
        }
      }
      return best;
    }
  }

  double quiesce(Board board, String turn, double alpha, double beta, int qd) {
    double standPat = network.forward(Encoder.encode(board))[0];
    if (qd >= QMAX) {
      return standPat;
    }
    String next = turn.equals("white") ? "black" : "white";
    List<int[]> caps = captures(board, turn);
    orderMoves(board, caps);
    if (turn.equals("white")) {
      double best = standPat;
      if (best >= beta) {
        return best;
      }
      if (best > alpha) {
        alpha = best;
      }
      for (int[] m : caps) {
        Board.Undo u = board.makeMove(m[0], m[1], m[2], m[3]);
        best = Math.max(best, quiesce(board, next, alpha, beta, qd + 1));
        board.unmakeMove(u);
        alpha = Math.max(alpha, best);
        if (alpha >= beta) {
          break;
        }
      }
      return best;
    } else {
      double best = standPat;
      if (best <= alpha) {
        return best;
      }
      if (best < beta) {
        beta = best;
      }
      for (int[] m : caps) {
        Board.Undo u = board.makeMove(m[0], m[1], m[2], m[3]);
        best = Math.min(best, quiesce(board, next, alpha, beta, qd + 1));
        board.unmakeMove(u);
        beta = Math.min(beta, best);
        if (alpha >= beta) {
          break;
        }
      }
      return best;
    }
  }

  List<int[]> captures(Board board, String turn) {
    List<int[]> all = board.legalMoves(turn);
    List<int[]> caps = new ArrayList<>();
    for (int[] m : all) {
      if (!board.isEmpty(m[2], m[3])) {
        caps.add(m);
        continue;
      }
      int code = board.codeAt(m[0], m[1]);
      boolean pawn = code == 1 || code == 7;
      if (pawn && m[1] != m[3]) {
        caps.add(m);
      }
    }
    return caps;
  }

  void orderMoves(Board board, List<int[]> moves) {
    moves.sort((a, b) -> Double.compare(victim(board, b), victim(board, a)));
  }

  double victim(Board board, int[] m) {
    if (board.isEmpty(m[2], m[3])) {
      return 0;
    }
    int code = board.codeAt(m[2], m[3]);
    double[] val = { 1, 3, 3, 5, 9, 100 };
    return val[(code - 1) % 6];
  }
}
