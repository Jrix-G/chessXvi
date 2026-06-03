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
    if (!checkIfSame(fromRow, fromCol, toRow, toCol))
      return false;
    if (piece.equals("pawn"))
      return checkPawnMove(fromRow, fromCol, toRow, toCol, color);
    if (piece.equals("knight"))
      return checkKnightMove(fromRow, fromCol, toRow, toCol);
    return false;
  }

  // check if pawn movement is correct (black or white)
  public boolean checkPawnMove(int fromRow, int fromCol, int toRow, int toCol, String color) {
    if (color.equals("white")) { // white pawn
      if (fromCol == toCol && (fromRow == (toRow + 1) || fromRow == (toRow + 2))) {
        moved(fromRow, fromCol, toRow, toCol);
        return true;
      }
    }
    if (color.equals("black")) { // black pawn
      if (fromCol == toCol && (fromRow == (toRow - 1) || fromRow == (toRow - 2))) {
        moved(fromRow, fromCol, toRow, toCol);
        return true;
      }
    }
    return false;
  }

  public boolean checkKnightMove(int fromRow, int fromCol, int toRow, int toCol) {
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

  public boolean checkIfSame(int fromRow, int fromCol, int toRow, int toCol) {
    if (fromRow != toRow || fromCol != toCol) {
      return true;
    } else {
      System.out.println("Illegal move!");
      return false;
    }
  }
}
