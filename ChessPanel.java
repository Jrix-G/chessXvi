import javax.swing.*;
import java.awt.*;

public class ChessPanel {

  public ChessPanel(Board board) {
    JPanel panel = new JPanel();

    JButton white = new JButton("White");
    JButton black = new JButton("Black");

    panel.add(white);
    panel.add(black);

    JFrame frameChoice = new JFrame("Make your choice");
    frameChoice.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frameChoice.add(panel);
    frameChoice.pack();
    frameChoice.setLocationRelativeTo(null);
    frameChoice.setVisible(true);

    white.addActionListener(e -> {
      frameChoice.dispose();
      new ChessGUI(board, "white");
    });
    black.addActionListener(e -> {
      frameChoice.dispose();
      new ChessGUI(board, "black");
    });
  }
}
