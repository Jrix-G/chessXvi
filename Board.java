import java.util.ArrayList;
import java.util.List;

public class Board {
  static final int EMPTY = 0;
  static final int WP = 1, WN = 2, WB = 3, WR = 4, WQ = 5, WK = 6;
  static final int BP = 7, BN = 8, BB = 9, BR = 10, BQ = 11, BK = 12;

  private static final int[][] STRAIGHT = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
  private static final int[][] DIAG = { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };
  private static final int[][] KNIGHT = { { 1, 2 }, { 1, -2 }, { -1, 2 }, { -1, -2 }, { 2, 1 }, { 2, -1 }, { -2, 1 },
      { -2, -1 } };

  private final int[][] squares = {
      { BR, BN, BB, BQ, BK, BB, BN, BR },
      { BP, BP, BP, BP, BP, BP, BP, BP },
      { EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY },
      { EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY },
      { EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY },
      { EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY },
      { WP, WP, WP, WP, WP, WP, WP, WP },
      { WR, WN, WB, WQ, WK, WB, WN, WR }
  };

  private int enPassantRow = -1;
  private int enPassantCol = -1;
  private boolean whiteKingMoved = false;
  private boolean blackKingMoved = false;
  private boolean whiteRookKingMoved = false;
  private boolean whiteRookQueenMoved = false;
  private boolean blackRookKingMoved = false;
  private boolean blackRookQueenMoved = false;

  private static boolean isWhite(int p) {
    return p >= 1 && p <= 6;
  }

  private static boolean isBlack(int p) {
    return p >= 7;
  }

  private static boolean sameColor(int p, boolean white) {
    return white ? (p >= 1 && p <= 6) : (p >= 7);
  }

  private static String pieceName(int code) {
    switch ((code - 1) % 6) {
      case 0:
        return "pawn";
      case 1:
        return "knight";
      case 2:
        return "bishop";
      case 3:
        return "rook";
      case 4:
        return "queen";
      default:
        return "king";
    }
  }

  public int codeAt(int row, int col) {
    return squares[row][col];
  }

  public String pieceAt(int row, int col) {
    int p = squares[row][col];
    if (p == EMPTY) {
      return "";
    }
    String color = p <= 6 ? "white" : "black";
    return color + "_" + pieceName(p);
  }

  public boolean isEmpty(int row, int col) {
    return squares[row][col] == EMPTY;
  }

  public void moved(int fromRow, int fromCol, int toRow, int toCol) {
    squares[toRow][toCol] = squares[fromRow][fromCol];
    squares[fromRow][fromCol] = EMPTY;
  }

  public boolean move(int fromRow, int fromCol, int toRow, int toCol) {
    if (!inBounds(fromRow, fromCol) || !inBounds(toRow, toCol)) {
      return false;
    }
    int code = squares[fromRow][fromCol];
    if (code == EMPTY) {
      return false;
    }
    String color = code <= 6 ? "white" : "black"; // Color of the piece, black or white
    String piece = pieceName(code); // Name of the piece like "knight"
    if (!checkIfSame(fromRow, fromCol, toRow, toCol))
      return false;
    if (!wouldBeLegal(fromRow, fromCol, toRow, toCol))
      return false;
    boolean castling = piece.equals("king") && fromRow == toRow && Math.abs(toCol - fromCol) == 2;
    applyMove(piece, color, fromRow, fromCol, toRow, toCol, castling);
    updateStateAfterMove(piece, color, fromRow, fromCol, toRow, toCol);
    return true;
  }

  private boolean isPseudoLegal(String piece, int fromRow, int fromCol, int toRow, int toCol, String color) {
    if (piece.equals("pawn"))
      return checkPawnMove(fromRow, fromCol, toRow, toCol, color);
    if (piece.equals("knight"))
      return checkKnightMove(fromRow, fromCol, toRow, toCol, color);
    if (piece.equals("rook"))
      return checkRookMove(fromRow, fromCol, toRow, toCol, color);
    if (piece.equals("bishop"))
      return checkBishopMove(fromRow, fromCol, toRow, toCol, color);
    if (piece.equals("queen"))
      return checkQueenMove(fromRow, fromCol, toRow, toCol, color);
    if (piece.equals("king"))
      return checkKingMove(fromRow, fromCol, toRow, toCol, color);
    return false;
  }

  private boolean wouldBeLegal(int fromRow, int fromCol, int toRow, int toCol) {
    if (!inBounds(fromRow, fromCol) || !inBounds(toRow, toCol))
      return false;
    if (fromRow == toRow && fromCol == toCol)
      return false;
    int code = squares[fromRow][fromCol];
    if (code == EMPTY)
      return false;
    String color = code <= 6 ? "white" : "black";
    String piece = pieceName(code);
    boolean castling = piece.equals("king") && fromRow == toRow && Math.abs(toCol - fromCol) == 2;
    if (castling) {
      if (!canCastle(fromRow, fromCol, toRow, toCol, color))
        return false;
    } else {
      if (!isPseudoLegal(piece, fromRow, fromCol, toRow, toCol, color))
        return false;
    }
    Undo u = makeMove(fromRow, fromCol, toRow, toCol);
    boolean safe = !inCheck(color);
    unmakeMove(u);
    return safe;
  }

  private void applyMove(String piece, String color, int fromRow, int fromCol, int toRow, int toCol, boolean castling) {
    if (castling) {
      moved(fromRow, fromCol, toRow, toCol);
      if (toCol > fromCol) {
        moved(fromRow, 7, fromRow, toCol - 1);
      } else {
        moved(fromRow, 0, fromRow, toCol + 1);
      }
      return;
    }
    if (piece.equals("pawn") && fromCol != toCol && isEmpty(toRow, toCol)) {
      squares[fromRow][toCol] = EMPTY;
    }
    moved(fromRow, fromCol, toRow, toCol);
    if (piece.equals("pawn") && (toRow == 0 || toRow == 7)) {
      squares[toRow][toCol] = color.equals("white") ? WQ : BQ;
    }
  }

  private void updateStateAfterMove(String piece, String color, int fromRow, int fromCol, int toRow, int toCol) {
    if (piece.equals("pawn") && Math.abs(toRow - fromRow) == 2) {
      enPassantRow = (fromRow + toRow) / 2;
      enPassantCol = fromCol;
    } else {
      enPassantRow = -1;
      enPassantCol = -1;
    }
    if (piece.equals("king")) {
      if (color.equals("white"))
        whiteKingMoved = true;
      else
        blackKingMoved = true;
    }
    if (piece.equals("rook")) {
      if (fromRow == 7 && fromCol == 0)
        whiteRookQueenMoved = true;
      if (fromRow == 7 && fromCol == 7)
        whiteRookKingMoved = true;
      if (fromRow == 0 && fromCol == 0)
        blackRookQueenMoved = true;
      if (fromRow == 0 && fromCol == 7)
        blackRookKingMoved = true;
    }
    if (toRow == 7 && toCol == 0)
      whiteRookQueenMoved = true;
    if (toRow == 7 && toCol == 7)
      whiteRookKingMoved = true;
    if (toRow == 0 && toCol == 0)
      blackRookQueenMoved = true;
    if (toRow == 0 && toCol == 7)
      blackRookKingMoved = true;
  }

  private boolean canCastle(int fromRow, int fromCol, int toRow, int toCol, String color) {
    if (fromCol != 4)
      return false;
    boolean white = color.equals("white");
    if (white) {
      if (fromRow != 7 || whiteKingMoved)
        return false;
    } else {
      if (fromRow != 0 || blackKingMoved)
        return false;
    }
    String opp = opponent(color);
    if (inCheck(color))
      return false;
    boolean kingside = toCol > fromCol;
    if (kingside) {
      if (toCol != 6)
        return false;
      if (white ? whiteRookKingMoved : blackRookKingMoved)
        return false;
      if (squares[fromRow][7] != (white ? WR : BR))
        return false;
      if (!isEmpty(fromRow, 5) || !isEmpty(fromRow, 6))
        return false;
      if (isAttacked(fromRow, 5, opp) || isAttacked(fromRow, 6, opp))
        return false;
    } else {
      if (toCol != 2)
        return false;
      if (white ? whiteRookQueenMoved : blackRookQueenMoved)
        return false;
      if (squares[fromRow][0] != (white ? WR : BR))
        return false;
      if (!isEmpty(fromRow, 1) || !isEmpty(fromRow, 2) || !isEmpty(fromRow, 3))
        return false;
      if (isAttacked(fromRow, 2, opp) || isAttacked(fromRow, 3, opp))
        return false;
    }
    return true;
  }

  // check if pawn movement is correct (black or white)
  public boolean checkPawnMove(int fromRow, int fromCol, int toRow, int toCol, String color) {
    if (color.equals("white")) { // white pawn
      if (fromCol == toCol && fromRow == (toRow + 1) && isEmpty(toRow, toCol)) {
        return true;
      }
      if (fromCol == toCol && fromRow == 6 && fromRow == (toRow + 2) && isEmpty(fromRow - 1, fromCol)
          && isEmpty(toRow, toCol)) {
        return true;
      }
      if (fromRow == (toRow + 1) && (toCol == fromCol + 1 || toCol == fromCol - 1)) {
        if (!isEmpty(toRow, toCol) && isBlack(squares[toRow][toCol])) {
          return true;
        }
        if (toRow == enPassantRow && toCol == enPassantCol) {
          return true;
        }
      }
    }
    if (color.equals("black")) { // black pawn
      if (fromCol == toCol && fromRow == (toRow - 1) && isEmpty(toRow, toCol)) {
        return true;
      }
      if (fromCol == toCol && fromRow == 1 && fromRow == (toRow - 2) && isEmpty(fromRow + 1, fromCol)
          && isEmpty(toRow, toCol)) {
        return true;
      }
      if (fromRow == (toRow - 1) && (toCol == fromCol + 1 || toCol == fromCol - 1)) {
        if (!isEmpty(toRow, toCol) && isWhite(squares[toRow][toCol])) {
          return true;
        }
        if (toRow == enPassantRow && toCol == enPassantCol) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean checkKnightMove(int fromRow, int fromCol, int toRow, int toCol, String color) {
    int dest = squares[toRow][toCol];
    if (dest != EMPTY && sameColor(dest, color.equals("white"))) {
      return false;
    }
    if (fromCol + 1 == toCol && fromRow + 2 == toRow) {
      return true;
    }
    if (fromCol - 1 == toCol && fromRow + 2 == toRow) {
      return true;
    }
    if (fromCol + 2 == toCol && fromRow + 1 == toRow) {
      return true;
    }
    if (fromCol + 2 == toCol && fromRow - 1 == toRow) {
      return true;
    }
    if (fromCol - 2 == toCol && fromRow + 1 == toRow) {
      return true;
    }
    if (fromCol - 2 == toCol && fromRow - 1 == toRow) {
      return true;
    }
    if (fromCol - 1 == toCol && fromRow - 2 == toRow) {
      return true;
    }
    if (fromCol + 1 == toCol && fromRow - 2 == toRow) {
      return true;
    }
    return false;
  }

  // check if rook movement is correct (horizontal or vertical)
  public boolean checkRookMove(int fromRow, int fromCol, int toRow, int toCol, String color) {
    int dest = squares[toRow][toCol];
    if (dest != EMPTY && sameColor(dest, color.equals("white"))) {
      return false;
    }
    if (fromRow == toRow) { // horizontal
      int step = toCol > fromCol ? 1 : -1;
      for (int c = fromCol + step; c != toCol; c += step) {
        if (!isEmpty(fromRow, c)) {
          return false;
        }
      }
      return true;
    }
    if (fromCol == toCol) { // vertical
      int step = toRow > fromRow ? 1 : -1;
      for (int r = fromRow + step; r != toRow; r += step) {
        if (!isEmpty(r, fromCol)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  // check if bishop movement is correct (diagonal)
  public boolean checkBishopMove(int fromRow, int fromCol, int toRow, int toCol, String color) {
    int dest = squares[toRow][toCol];
    if (dest != EMPTY && sameColor(dest, color.equals("white"))) {
      return false;
    }
    if (Math.abs(toRow - fromRow) != Math.abs(toCol - fromCol)) {
      return false;
    }
    int rowStep = toRow > fromRow ? 1 : -1;
    int colStep = toCol > fromCol ? 1 : -1;
    int r = fromRow + rowStep;
    int c = fromCol + colStep;
    while (r != toRow) {
      if (!isEmpty(r, c)) {
        return false;
      }
      r += rowStep;
      c += colStep;
    }
    return true;
  }

  // check if queen movement is correct (rook or bishop)
  public boolean checkQueenMove(int fromRow, int fromCol, int toRow, int toCol, String color) {
    if (fromRow == toRow || fromCol == toCol) {
      return checkRookMove(fromRow, fromCol, toRow, toCol, color);
    }
    return checkBishopMove(fromRow, fromCol, toRow, toCol, color);
  }

  // check if king movement is correct (one square in any direction)
  public boolean checkKingMove(int fromRow, int fromCol, int toRow, int toCol, String color) {
    int dest = squares[toRow][toCol];
    if (dest != EMPTY && sameColor(dest, color.equals("white"))) {
      return false;
    }
    if (Math.abs(toRow - fromRow) <= 1 && Math.abs(toCol - fromCol) <= 1) {
      return true;
    }
    return false;
  }

  public boolean checkIfSame(int fromRow, int fromCol, int toRow, int toCol) {
    if (fromRow != toRow || fromCol != toCol) {
      return true;
    } else {
      System.out.println("Illegal move!");
      return false;
    }
  }

  public boolean isInCheck(String color) {
    return inCheck(color);
  }

  public boolean isCheckmate(String color) {
    return inCheck(color) && !hasLegalMove(color);
  }

  public boolean isStalemate(String color) {
    return !inCheck(color) && !hasLegalMove(color);
  }

  public Board copy() {
    Board b = new Board();
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        b.squares[row][col] = this.squares[row][col];
      }
    }
    b.enPassantRow = this.enPassantRow;
    b.enPassantCol = this.enPassantCol;
    b.whiteKingMoved = this.whiteKingMoved;
    b.blackKingMoved = this.blackKingMoved;
    b.whiteRookKingMoved = this.whiteRookKingMoved;
    b.whiteRookQueenMoved = this.whiteRookQueenMoved;
    b.blackRookKingMoved = this.blackRookKingMoved;
    b.blackRookQueenMoved = this.blackRookQueenMoved;
    return b;
  }

  public static class Undo {
    int fr, fc, tr, tc;
    int moved;
    int captured;
    int capRow, capCol;
    boolean castling;
    int prevEnPassantRow, prevEnPassantCol;
    boolean wkm, bkm, wrk, wrq, brk, brq;
  }

  public Undo makeMove(int fr, int fc, int tr, int tc) {
    Undo u = new Undo();
    u.fr = fr;
    u.fc = fc;
    u.tr = tr;
    u.tc = tc;
    u.prevEnPassantRow = enPassantRow;
    u.prevEnPassantCol = enPassantCol;
    u.wkm = whiteKingMoved;
    u.bkm = blackKingMoved;
    u.wrk = whiteRookKingMoved;
    u.wrq = whiteRookQueenMoved;
    u.brk = blackRookKingMoved;
    u.brq = blackRookQueenMoved;

    int code = squares[fr][fc];
    u.moved = code;
    boolean white = code <= 6;
    String color = white ? "white" : "black";
    String piece = pieceName(code);
    boolean castling = piece.equals("king") && fr == tr && Math.abs(tc - fc) == 2;
    u.castling = castling;

    if (castling) {
      u.captured = 0;
    } else if (piece.equals("pawn") && fc != tc && squares[tr][tc] == EMPTY) {
      u.captured = squares[fr][tc];
      u.capRow = fr;
      u.capCol = tc;
    } else {
      u.captured = squares[tr][tc];
      u.capRow = tr;
      u.capCol = tc;
    }

    applyMove(piece, color, fr, fc, tr, tc, castling);
    updateStateAfterMove(piece, color, fr, fc, tr, tc);
    return u;
  }

  public void unmakeMove(Undo u) {
    enPassantRow = u.prevEnPassantRow;
    enPassantCol = u.prevEnPassantCol;
    whiteKingMoved = u.wkm;
    blackKingMoved = u.bkm;
    whiteRookKingMoved = u.wrk;
    whiteRookQueenMoved = u.wrq;
    blackRookKingMoved = u.brk;
    blackRookQueenMoved = u.brq;

    if (u.castling) {
      squares[u.fr][u.fc] = squares[u.tr][u.tc];
      squares[u.tr][u.tc] = EMPTY;
      if (u.tc > u.fc) {
        squares[u.fr][7] = squares[u.fr][u.tc - 1];
        squares[u.fr][u.tc - 1] = EMPTY;
      } else {
        squares[u.fr][0] = squares[u.fr][u.tc + 1];
        squares[u.fr][u.tc + 1] = EMPTY;
      }
      return;
    }

    squares[u.fr][u.fc] = u.moved;
    squares[u.tr][u.tc] = EMPTY;
    if (u.captured != 0) {
      squares[u.capRow][u.capCol] = u.captured;
    }
  }

  public List<int[]> legalMoves(String color) {
    boolean white = color.equals("white");
    List<int[]> moves = new ArrayList<>();
    for (int r = 0; r < 8; r++) {
      for (int c = 0; c < 8; c++) {
        int code = squares[r][c];
        if (code == EMPTY || !sameColor(code, white)) {
          continue;
        }
        genPiece(code, r, c, moves);
      }
    }
    return moves;
  }

  public List<int[]> legalMovesRef(String color) {
    boolean white = color.equals("white");
    List<int[]> moves = new ArrayList<>();
    for (int fromRow = 0; fromRow < 8; fromRow++) {
      for (int fromCol = 0; fromCol < 8; fromCol++) {
        int code = squares[fromRow][fromCol];
        if (code == EMPTY || !sameColor(code, white)) {
          continue;
        }
        for (int toRow = 0; toRow < 8; toRow++) {
          for (int toCol = 0; toCol < 8; toCol++) {
            if (wouldBeLegal(fromRow, fromCol, toRow, toCol)) {
              moves.add(new int[] { fromRow, fromCol, toRow, toCol });
            }
          }
        }
      }
    }
    return moves;
  }

  private void tryAdd(int fr, int fc, int tr, int tc, List<int[]> moves) {
    if (!inBounds(tr, tc)) {
      return;
    }
    if (wouldBeLegal(fr, fc, tr, tc)) {
      moves.add(new int[] { fr, fc, tr, tc });
    }
  }

  private void slide(int fr, int fc, int[][] dirs, List<int[]> moves) {
    for (int[] d : dirs) {
      int r = fr + d[0];
      int c = fc + d[1];
      while (inBounds(r, c)) {
        tryAdd(fr, fc, r, c, moves);
        if (squares[r][c] != EMPTY) {
          break;
        }
        r += d[0];
        c += d[1];
      }
    }
  }

  private void genPiece(int code, int r, int c, List<int[]> moves) {
    int type = (code - 1) % 6;
    switch (type) {
      case 0:
        int dir = code <= 6 ? -1 : 1;
        tryAdd(r, c, r + dir, c, moves);
        tryAdd(r, c, r + 2 * dir, c, moves);
        tryAdd(r, c, r + dir, c - 1, moves);
        tryAdd(r, c, r + dir, c + 1, moves);
        break;
      case 1:
        for (int[] o : KNIGHT) {
          tryAdd(r, c, r + o[0], c + o[1], moves);
        }
        break;
      case 2:
        slide(r, c, DIAG, moves);
        break;
      case 3:
        slide(r, c, STRAIGHT, moves);
        break;
      case 4:
        slide(r, c, STRAIGHT, moves);
        slide(r, c, DIAG, moves);
        break;
      case 5:
        for (int dr = -1; dr <= 1; dr++) {
          for (int dc = -1; dc <= 1; dc++) {
            if (dr == 0 && dc == 0) {
              continue;
            }
            tryAdd(r, c, r + dr, c + dc, moves);
          }
        }
        tryAdd(r, c, r, c + 2, moves);
        tryAdd(r, c, r, c - 2, moves);
        break;
      default:
        break;
    }
  }

  private boolean hasLegalMove(String color) {
    return !legalMoves(color).isEmpty();
  }

  private boolean inCheck(String color) {
    int[] king = findKing(color);
    if (king == null) {
      return false;
    }
    return isAttacked(king[0], king[1], opponent(color));
  }

  private int[] findKing(String color) {
    int target = color.equals("white") ? WK : BK;
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        if (squares[row][col] == target) {
          return new int[] { row, col };
        }
      }
    }
    return null;
  }

  private boolean isAttacked(int row, int col, String byColor) {
    boolean white = byColor.equals("white");
    int pawn = white ? WP : BP;
    int knight = white ? WN : BN;
    int bishop = white ? WB : BB;
    int rook = white ? WR : BR;
    int queen = white ? WQ : BQ;
    int king = white ? WK : BK;
    int pawnDir = white ? 1 : -1;
    int pr = row + pawnDir;
    if (inBounds(pr, col - 1) && squares[pr][col - 1] == pawn) {
      return true;
    }
    if (inBounds(pr, col + 1) && squares[pr][col + 1] == pawn) {
      return true;
    }
    int[][] knightOffsets = { { 1, 2 }, { 1, -2 }, { -1, 2 }, { -1, -2 }, { 2, 1 }, { 2, -1 }, { -2, 1 }, { -2, -1 } };
    for (int[] off : knightOffsets) {
      int r = row + off[0];
      int c = col + off[1];
      if (inBounds(r, c) && squares[r][c] == knight) {
        return true;
      }
    }
    for (int dr = -1; dr <= 1; dr++) {
      for (int dc = -1; dc <= 1; dc++) {
        if (dr == 0 && dc == 0) {
          continue;
        }
        int r = row + dr;
        int c = col + dc;
        if (inBounds(r, c) && squares[r][c] == king) {
          return true;
        }
      }
    }
    int[][] straight = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
    for (int[] d : straight) {
      int r = row + d[0];
      int c = col + d[1];
      while (inBounds(r, c)) {
        if (squares[r][c] != EMPTY) {
          if (squares[r][c] == rook || squares[r][c] == queen) {
            return true;
          }
          break;
        }
        r += d[0];
        c += d[1];
      }
    }
    int[][] diagonal = { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };
    for (int[] d : diagonal) {
      int r = row + d[0];
      int c = col + d[1];
      while (inBounds(r, c)) {
        if (squares[r][c] != EMPTY) {
          if (squares[r][c] == bishop || squares[r][c] == queen) {
            return true;
          }
          break;
        }
        r += d[0];
        c += d[1];
      }
    }
    return false;
  }

  private String opponent(String color) {
    return color.equals("white") ? "black" : "white";
  }

  private boolean inBounds(int row, int col) {
    return row >= 0 && row < 8 && col >= 0 && col < 8;
  }

}
