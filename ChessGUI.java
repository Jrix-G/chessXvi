import javax.swing.*;
import java.awt.*;

public class ChessGUI {
  static final int SQUARE_SIZE = 80;

  static final Color LIGHT = new Color(240, 217, 181);
  static final Color DARK = new Color(181, 136, 99);

  private int selectedRow = -1;
  private int selectedCol = -1;

  private final JButton[][] squares = new JButton[8][8];
  private final Board board;

  public ChessGUI(Board board) {
    this.board = board;
    JFrame frame = new JFrame("Chess X AI");
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

        square.addActionListener(e -> handleClick(rowSelected, colSelected));
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

  private void handleClick(int row, int col) {
    if (selectedRow == -1) {
      // First click
      selectedRow = row;
      selectedCol = col;
    } else {
      // Second click
      int prevRow = selectedRow;
      int prevCol = selectedCol;
      selectedRow = -1;
      selectedCol = -1;

      boolean legal = board.move(prevRow, prevCol, row, col);
      refresh();
      if (!legal) {
        flashRed(row, col);
      }
      return;
    }
    refresh();
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

        String piece = board.pieceAt(row, col);
        if (piece.startsWith("white")) {
          System.out.println("White piece");
        }

        if (board.isEmpty(row, col)) {
          s.setIcon(null);
        } else {
          s.setIcon(loadIcon(board.pieceAt(row, col)));
        }
      }
    }
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
  static ImageIcon loadIcon(String name) {
    ImageIcon raw = new ImageIcon("icons/" + name + ".png");
    Image scaled = raw.getImage().getScaledInstance(
        SQUARE_SIZE, SQUARE_SIZE, Image.SCALE_SMOOTH);
    return new ImageIcon(scaled);
  }
}
