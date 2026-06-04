import javax.swing.SwingUtilities;

public class Main {
  public static void main(String[] args) {
    Board board = new Board();
    System.out.println("Chess board has started");
    SwingUtilities.invokeLater(() -> new ChessPanel(board));
  }
}
