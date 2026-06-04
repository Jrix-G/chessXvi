public class Board {
  private final String[][] squares = {
      { "black_rook", "black_knight", "black_bishop", "black_queen", "black_king", "black_bishop", "black_knight",
          "black_rook" },
      { "black_pawn", "black_pawn", "black_pawn", "black_pawn", "black_pawn", "black_pawn", "black_pawn",
          "black_pawn" },
      { "", "", "", "", "", "", "", "" },
      { "", "", "", "", "", "", "", "" },
      { "", "", "", "", "", "", "", "" },
      { "", "", "", "", "", "", "", "" },
      { "white_pawn", "white_pawn", "white_pawn", "white_pawn", "white_pawn", "white_pawn", "white_pawn",
          "white_pawn" },
      { "white_rook", "white_knight", "white_bishop", "white_queen", "white_king", "white_bishop", "white_knight",
          "white_rook" }
  };

  private int enPassantRow = -1;
  private int enPassantCol = -1;
  private boolean whiteKingMoved = false;
  private boolean blackKingMoved = false;
  private boolean whiteRookKingMoved = false;
  private boolean whiteRookQueenMoved = false;
  private boolean blackRookKingMoved = false;
  private boolean blackRookQueenMoved = false;

  public String pieceAt(int row, int col) {
    return squares[row][col];
  }

  public boolean isEmpty(int row, int col) {
    return squares[row][col].equals("");
  }

  public void moved(int fromRow, int fromCol, int toRow, int toCol) {
    squares[toRow][toCol] = squares[fromRow][fromCol];
    squares[fromRow][fromCol] = "";
  }

  public boolean move(int fromRow, int fromCol, int toRow, int toCol) {
    if (!inBounds(fromRow, fromCol) || !inBounds(toRow, toCol)) {
      return false;
    }
    String pieceSquare = squares[fromRow][fromCol];
    if (pieceSquare.equals("")) {
      return false;
    }
    String color = pieceSquare.split("_")[0]; // Color of the piece, black or white
    String piece = pieceSquare.split("_")[1]; // Name of the piece like "knight"
    if (!checkIfSame(fromRow, fromCol, toRow, toCol)) return false;
    if (!wouldBeLegal(fromRow, fromCol, toRow, toCol)) return false;
    boolean castling = piece.equals("king") && fromRow == toRow && Math.abs(toCol - fromCol) == 2;
    applyMove(piece, color, fromRow, fromCol, toRow, toCol, castling);
    updateStateAfterMove(piece, color, fromRow, fromCol, toRow, toCol);
    return true;
  }

  private boolean isPseudoLegal(String piece, int fromRow, int fromCol, int toRow, int toCol, String color) {
    if (piece.equals("pawn")) return checkPawnMove(fromRow, fromCol, toRow, toCol, color);
    if (piece.equals("knight")) return checkKnightMove(fromRow, fromCol, toRow, toCol, color);
    if (piece.equals("rook")) return checkRookMove(fromRow, fromCol, toRow, toCol, color);
    if (piece.equals("bishop")) return checkBishopMove(fromRow, fromCol, toRow, toCol, color);
    if (piece.equals("queen")) return checkQueenMove(fromRow, fromCol, toRow, toCol, color);
    if (piece.equals("king")) return checkKingMove(fromRow, fromCol, toRow, toCol, color);
    return false;
  }

  private boolean wouldBeLegal(int fromRow, int fromCol, int toRow, int toCol) {
    if (!inBounds(fromRow, fromCol) || !inBounds(toRow, toCol)) return false;
    if (fromRow == toRow && fromCol == toCol) return false;
    String pieceSquare = squares[fromRow][fromCol];
    if (pieceSquare.equals("")) return false;
    String color = pieceSquare.split("_")[0];
    String piece = pieceSquare.split("_")[1];
    boolean castling = piece.equals("king") && fromRow == toRow && Math.abs(toCol - fromCol) == 2;
    if (castling) {
      if (!canCastle(fromRow, fromCol, toRow, toCol, color)) return false;
    } else {
      if (!isPseudoLegal(piece, fromRow, fromCol, toRow, toCol, color)) return false;
    }
    String[][] backup = snapshot();
    applyMove(piece, color, fromRow, fromCol, toRow, toCol, castling);
    boolean safe = !inCheck(color);
    restore(backup);
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
      squares[fromRow][toCol] = "";
    }
    moved(fromRow, fromCol, toRow, toCol);
    if (piece.equals("pawn") && (toRow == 0 || toRow == 7)) {
      squares[toRow][toCol] = color + "_queen";
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
      if (color.equals("white")) whiteKingMoved = true;
      else blackKingMoved = true;
    }
    if (piece.equals("rook")) {
      if (fromRow == 7 && fromCol == 0) whiteRookQueenMoved = true;
      if (fromRow == 7 && fromCol == 7) whiteRookKingMoved = true;
      if (fromRow == 0 && fromCol == 0) blackRookQueenMoved = true;
      if (fromRow == 0 && fromCol == 7) blackRookKingMoved = true;
    }
    if (toRow == 7 && toCol == 0) whiteRookQueenMoved = true;
    if (toRow == 7 && toCol == 7) whiteRookKingMoved = true;
    if (toRow == 0 && toCol == 0) blackRookQueenMoved = true;
    if (toRow == 0 && toCol == 7) blackRookKingMoved = true;
  }

  private boolean canCastle(int fromRow, int fromCol, int toRow, int toCol, String color) {
    if (fromCol != 4) return false;
    if (color.equals("white")) {
      if (fromRow != 7 || whiteKingMoved) return false;
    } else {
      if (fromRow != 0 || blackKingMoved) return false;
    }
    String opp = opponent(color);
    if (inCheck(color)) return false;
    boolean kingside = toCol > fromCol;
    if (kingside) {
      if (toCol != 6) return false;
      if (color.equals("white") ? whiteRookKingMoved : blackRookKingMoved) return false;
      if (!squares[fromRow][7].equals(color + "_rook")) return false;
      if (!isEmpty(fromRow, 5) || !isEmpty(fromRow, 6)) return false;
      if (isAttacked(fromRow, 5, opp) || isAttacked(fromRow, 6, opp)) return false;
    } else {
      if (toCol != 2) return false;
      if (color.equals("white") ? whiteRookQueenMoved : blackRookQueenMoved) return false;
      if (!squares[fromRow][0].equals(color + "_rook")) return false;
      if (!isEmpty(fromRow, 1) || !isEmpty(fromRow, 2) || !isEmpty(fromRow, 3)) return false;
      if (isAttacked(fromRow, 2, opp) || isAttacked(fromRow, 3, opp)) return false;
    }
    return true;
  }

  // check if pawn movement is correct (black or white)
  public boolean checkPawnMove(int fromRow, int fromCol, int toRow, int toCol, String color) {
    if (color.equals("white")) { // white pawn
      if (fromCol == toCol && fromRow == (toRow + 1) && isEmpty(toRow, toCol)) {
        return true;
      }
      if (fromCol == toCol && fromRow == 6 && fromRow == (toRow + 2) && isEmpty(fromRow - 1, fromCol) && isEmpty(toRow, toCol)) {
        return true;
      }
      if (fromRow == (toRow + 1) && (toCol == fromCol + 1 || toCol == fromCol - 1)) {
        if (!isEmpty(toRow, toCol) && squares[toRow][toCol].startsWith("black")) {
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
      if (fromCol == toCol && fromRow == 1 && fromRow == (toRow - 2) && isEmpty(fromRow + 1, fromCol) && isEmpty(toRow, toCol)) {
        return true;
      }
      if (fromRow == (toRow - 1) && (toCol == fromCol + 1 || toCol == fromCol - 1)) {
        if (!isEmpty(toRow, toCol) && squares[toRow][toCol].startsWith("white")) {
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
    if (!isEmpty(toRow, toCol) && squares[toRow][toCol].startsWith(color)) {
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
    if (!isEmpty(toRow, toCol) && squares[toRow][toCol].startsWith(color)) {
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
    if (!isEmpty(toRow, toCol) && squares[toRow][toCol].startsWith(color)) {
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
    if (!isEmpty(toRow, toCol) && squares[toRow][toCol].startsWith(color)) {
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

  private boolean hasLegalMove(String color) {
    for (int fromRow = 0; fromRow < 8; fromRow++) {
      for (int fromCol = 0; fromCol < 8; fromCol++) {
        if (squares[fromRow][fromCol].equals("") || !squares[fromRow][fromCol].startsWith(color)) {
          continue;
        }
        for (int toRow = 0; toRow < 8; toRow++) {
          for (int toCol = 0; toCol < 8; toCol++) {
            if (wouldBeLegal(fromRow, fromCol, toRow, toCol)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  private boolean inCheck(String color) {
    int[] king = findKing(color);
    if (king == null) {
      return false;
    }
    return isAttacked(king[0], king[1], opponent(color));
  }

  private int[] findKing(String color) {
    String target = color + "_king";
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        if (squares[row][col].equals(target)) {
          return new int[] { row, col };
        }
      }
    }
    return null;
  }

  private boolean isAttacked(int row, int col, String byColor) {
    int pawnDir = byColor.equals("white") ? 1 : -1;
    int pr = row + pawnDir;
    if (inBounds(pr, col - 1) && squares[pr][col - 1].equals(byColor + "_pawn")) {
      return true;
    }
    if (inBounds(pr, col + 1) && squares[pr][col + 1].equals(byColor + "_pawn")) {
      return true;
    }
    int[][] knightOffsets = { { 1, 2 }, { 1, -2 }, { -1, 2 }, { -1, -2 }, { 2, 1 }, { 2, -1 }, { -2, 1 }, { -2, -1 } };
    for (int[] off : knightOffsets) {
      int r = row + off[0];
      int c = col + off[1];
      if (inBounds(r, c) && squares[r][c].equals(byColor + "_knight")) {
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
        if (inBounds(r, c) && squares[r][c].equals(byColor + "_king")) {
          return true;
        }
      }
    }
    int[][] straight = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
    for (int[] d : straight) {
      int r = row + d[0];
      int c = col + d[1];
      while (inBounds(r, c)) {
        if (!squares[r][c].equals("")) {
          if (squares[r][c].equals(byColor + "_rook") || squares[r][c].equals(byColor + "_queen")) {
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
        if (!squares[r][c].equals("")) {
          if (squares[r][c].equals(byColor + "_bishop") || squares[r][c].equals(byColor + "_queen")) {
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

  private String[][] snapshot() {
    String[][] copy = new String[8][8];
    for (int row = 0; row < 8; row++) {
      System.arraycopy(squares[row], 0, copy[row], 0, 8);
    }
    return copy;
  }

  private void restore(String[][] backup) {
    for (int row = 0; row < 8; row++) {
      System.arraycopy(backup[row], 0, squares[row], 0, 8);
    }
  }
}
