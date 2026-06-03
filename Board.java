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
    String pieceSquare = squares[fromRow][fromCol];
    if (pieceSquare.equals("")) {
      return false;
    }
    String color = pieceSquare.split("_")[0]; // Color of the piece, black or white
    String piece = pieceSquare.split("_")[1]; // Name of the piece like "knight"
    if (!checkIfSame(fromRow, fromCol, toRow, toCol)) return false;
    if (piece.equals("pawn")) return checkPawnMove(fromRow, fromCol, toRow, toCol, color);
    if (piece.equals("knight")) return checkKnightMove(fromRow, fromCol, toRow, toCol, color);
    if (piece.equals("rook")) return checkRookMove(fromRow, fromCol, toRow, toCol, color);
    if (piece.equals("bishop")) return checkBishopMove(fromRow, fromCol, toRow, toCol, color);
    if (piece.equals("queen")) return checkQueenMove(fromRow, fromCol, toRow, toCol, color);
    if (piece.equals("king")) return checkKingMove(fromRow, fromCol, toRow, toCol, color);
    return false;
  }

  // check if pawn movement is correct (black or white)
  public boolean checkPawnMove(int fromRow, int fromCol, int toRow, int toCol, String color) {
    if (color.equals("white")) { // white pawn
      if (fromCol == toCol && fromRow == (toRow + 1) && isEmpty(toRow, toCol)) {
        moved(fromRow, fromCol, toRow, toCol);
        return true;
      }
      if (fromCol == toCol && fromRow == 6 && fromRow == (toRow + 2) && isEmpty(fromRow - 1, fromCol) && isEmpty(toRow, toCol)) {
        moved(fromRow, fromCol, toRow, toCol);
        return true;
      }
      if (fromRow == (toRow + 1) && (toCol == fromCol + 1 || toCol == fromCol - 1) && !isEmpty(toRow, toCol) && squares[toRow][toCol].startsWith("black")) {
        moved(fromRow, fromCol, toRow, toCol);
        return true;
      }
    }
    if (color.equals("black")) { // black pawn
      if (fromCol == toCol && fromRow == (toRow - 1) && isEmpty(toRow, toCol)) {
        moved(fromRow, fromCol, toRow, toCol);
        return true;
      }
      if (fromCol == toCol && fromRow == 1 && fromRow == (toRow - 2) && isEmpty(fromRow + 1, fromCol) && isEmpty(toRow, toCol)) {
        moved(fromRow, fromCol, toRow, toCol);
        return true;
      }
      if (fromRow == (toRow - 1) && (toCol == fromCol + 1 || toCol == fromCol - 1) && !isEmpty(toRow, toCol) && squares[toRow][toCol].startsWith("white")) {
        moved(fromRow, fromCol, toRow, toCol);
        return true;
      }
    }
    return false;
  }

  public boolean checkKnightMove(int fromRow, int fromCol, int toRow, int toCol, String color) {
    if (!isEmpty(toRow, toCol) && squares[toRow][toCol].startsWith(color)) {
      return false;
    }
    if (fromCol + 1 == toCol && fromRow + 2 == toRow) {
      moved(fromRow, fromCol, toRow, toCol);
      return true;
    }
    if (fromCol - 1 == toCol && fromRow + 2 == toRow) {
      moved(fromRow, fromCol, toRow, toCol);
      return true;
    }
    if (fromCol + 2 == toCol && fromRow + 1 == toRow) {
      moved(fromRow, fromCol, toRow, toCol);
      return true;
    }
    if (fromCol + 2 == toCol && fromRow - 1 == toRow) {
      moved(fromRow, fromCol, toRow, toCol);
      return true;
    }
    if (fromCol - 2 == toCol && fromRow + 1 == toRow) {
      moved(fromRow, fromCol, toRow, toCol);
      return true;
    }
    if (fromCol - 2 == toCol && fromRow - 1 == toRow) {
      moved(fromRow, fromCol, toRow, toCol);
      return true;
    }
    if (fromCol - 1 == toCol && fromRow - 2 == toRow) {
      moved(fromRow, fromCol, toRow, toCol);
      return true;
    }
    if (fromCol + 1 == toCol && fromRow - 2 == toRow) {
      moved(fromRow, fromCol, toRow, toCol);
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
      moved(fromRow, fromCol, toRow, toCol);
      return true;
    }
    if (fromCol == toCol) { // vertical
      int step = toRow > fromRow ? 1 : -1;
      for (int r = fromRow + step; r != toRow; r += step) {
        if (!isEmpty(r, fromCol)) {
          return false;
        }
      }
      moved(fromRow, fromCol, toRow, toCol);
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
    moved(fromRow, fromCol, toRow, toCol);
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
      moved(fromRow, fromCol, toRow, toCol);
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
}
