import javax.swing.SwingUtilities;

public class ConnectNMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ConnectNGame defaultGame = new ConnectNGame(6, 7, 4, "Player 1", "Player 2");
            ConnectNFrame frame = new ConnectNFrame(defaultGame);
            frame.setVisible(false);
            frame.startNewGameDialog();
        });
    }
}
