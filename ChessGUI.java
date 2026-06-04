import javax.swing.*;
import java.awt.*;

public class ChessGUI {
  static final int SQUARE_SIZE = 80;

  static final Color LIGHT = new Color(240, 217, 181);
  static final Color DARK = new Color(181, 136, 99);
  static final Color SELECTED = new Color(106, 168, 79);

  private int selectedRow = -1;
  private int selectedCol = -1;

  private String turn = "white";
  private boolean gameOver = false;

  private final JButton[][] squares = new JButton[8][8];
  private final Board board;
  private final boolean flipped;
  private final JFrame frame;

  public ChessGUI(Board board, String color) {
    this.board = board;
    this.flipped = color.equals("black");

    frame = new JFrame("Chess X AI " + turn + " turn");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel panel = new JPanel(new GridLayout(8, 8));

    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        JButton square = new JButton();
        square.setPreferredSize(new Dimension(SQUARE_SIZE, SQUARE_SIZE));
        square.setBorder(null);
        square.setFocusPainted(false);

        if ((row + col) % 2 == 0) {
          square.setBackground(LIGHT);
        } else {
          square.setBackground(DARK);
        }

        final int rowSelected = row;
        final int colSelected = col;

        square.addActionListener(e -> handleClick(rowSelected, colSelected, turn));
        squares[row][col] = square;
        panel.add(square);
      }
    }
    refresh();
    frame.add(panel);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  private void handleClick(int row, int col, String color) {
    if (gameOver) {
      return;
    }
    int boardRow = toBoardRow(row);
    int boardCol = toBoardCol(col);
    if (selectedRow == -1) {
      // First click
      if (board.isEmpty(boardRow, boardCol)) {
        return;
      }
      if (!board.pieceAt(boardRow, boardCol).startsWith(color)) {
        flashRed(row, col);
        return;
      }
      selectedRow = row;
      selectedCol = col;
      refresh();
    } else {
      // Second click
      if (!board.isEmpty(boardRow, boardCol) && board.pieceAt(boardRow, boardCol).startsWith(color)) {
        selectedRow = row;
        selectedCol = col;
        refresh();
        return;
      }
      int prevRow = selectedRow;
      int prevCol = selectedCol;
      selectedRow = -1;
      selectedCol = -1;

      boolean legal = board.move(toBoardRow(prevRow), toBoardCol(prevCol), boardRow, boardCol);
      refresh();
      if (!legal) {
        flashRed(row, col);
        return;
      }
      turn = turn.equals("white") ? "black" : "white";
      frame.setTitle("Chess X AI " + turn + " turn");
      checkEnd();
    }
  }

  private void checkEnd() {
    if (board.isCheckmate(turn)) {
      String winner = turn.equals("white") ? "Black" : "White";
      gameOver = true;
      JOptionPane.showMessageDialog(frame, "Checkmate! " + winner + " wins.");
    } else if (board.isStalemate(turn)) {
      gameOver = true;
      JOptionPane.showMessageDialog(frame, "Stalemate! It's a draw.");
    } else if (board.isInCheck(turn)) {
      frame.setTitle("Chess X AI " + turn + " turn - check");
    }
  }

  // refresh board squares and update
  private void refresh() {
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        JButton s = squares[row][col];

        if ((row + col) % 2 == 0) {
          s.setBackground(LIGHT);
        } else {
          s.setBackground(DARK);
        }
        if (row == selectedRow && col == selectedCol) {
          s.setBackground(SELECTED);
        }
        if (board.isEmpty(toBoardRow(row), toBoardCol(col))) {
          s.setIcon(null);
        } else {
          s.setIcon(loadIcon(board.pieceAt(toBoardRow(row), toBoardCol(col))));
        }
      }
    }
  }

  private int toBoardRow(int row) {
    return flipped ? 7 - row : row;
  }

  private int toBoardCol(int col) {
    return flipped ? 7 - col : col;
  }

  // Flashing red if the move is illegal
  private void flashRed(int row, int col) {
    JButton s = squares[row][col];
    s.setBackground(Color.RED);
    new javax.swing.Timer(100, e -> {
      s.setBackground((row + col) % 2 == 0 ? LIGHT : DARK);
      ((javax.swing.Timer) e.getSource()).stop();
    }).start();
  }

  // Load icons
  private static final java.util.Map<String, ImageIcon> ICON_CACHE = new java.util.HashMap<>();

  static ImageIcon loadIcon(String name) {
    ImageIcon cached = ICON_CACHE.get(name);
    if (cached != null) {
      return cached;
    }
    ImageIcon raw = new ImageIcon("icons/" + name + ".png");
    Image scaled = raw.getImage().getScaledInstance(
        SQUARE_SIZE, SQUARE_SIZE, Image.SCALE_SMOOTH);
    ImageIcon icon = new ImageIcon(scaled);
    ICON_CACHE.put(name, icon);
    return icon;
  }
}
