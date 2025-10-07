import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ConnectNFrame extends JFrame {
    private final ConnectNGame game;
    private final JLabel status;
    private final BoardPanel boardPanel;
    private final ImageIcon chipOverlay = new ImageIcon("images/connect_n.png");


    public ConnectNFrame(ConnectNGame game) {
        super("Connect N");
        this.game = game;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setJMenuBar(buildMenuBar());
        status = new JLabel(turnText(), SwingConstants.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        add(status, BorderLayout.NORTH);

        boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        setResizable(false);
        pack();
        setLocationByPlatform(true);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();

        JMenu mGame = new JMenu("Game");
        JMenuItem newGame = new JMenuItem("New Game");
        newGame.addActionListener(e -> startNewGameDialog());
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> dispose());
        mGame.add(newGame);
        mGame.addSeparator();
        mGame.add(exit);

        JMenu mUndo = new JMenu("Undo");
        JMenuItem undoItem = new JMenuItem("Undo Last Move");
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        undoItem.addActionListener(e -> {
            if (game.undo()) {
                boardPanel.repaint();
                status.setText(turnText());
            } else {
                JOptionPane.showMessageDialog(this, "Nothing to undo.", "Undo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        mUndo.add(undoItem);

        JMenu mHelp = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Connect N - rebuilt from 2020–2021 original project by Tristan Ruddy.\\nLeft-click a column to drop a piece.",
            "About", JOptionPane.INFORMATION_MESSAGE));
        mHelp.add(about);

        mb.add(mGame);
        mb.add(mUndo);
        mb.add(mHelp);
        return mb;
    }

    public void startNewGameDialog() {
        JTextField rows = new JTextField(String.valueOf(game.getRows()));
        JTextField cols = new JTextField(String.valueOf(game.getColumns()));
        JTextField winN = new JTextField("4");
        JTextField p1 = new JTextField(game.getPlayerOne().getName());
        JTextField p2 = new JTextField(game.getPlayerTwo().getName());

        JPanel panel = new JPanel(new GridLayout(0,2,6,6));
        panel.add(new JLabel("Rows (4–12):")); panel.add(rows);
        panel.add(new JLabel("Columns (4–12):")); panel.add(cols);
        panel.add(new JLabel("N to win (3–8):")); panel.add(winN);
        panel.add(new JLabel("Player 1 (Yellow):")); panel.add(p1);
        panel.add(new JLabel("Player 2 (Red):")); panel.add(p2);

        if (JOptionPane.showConfirmDialog(this, panel, "New Game", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            int r = clamp(parseInt(rows.getText(), 6), 4, 12);
            int c = clamp(parseInt(cols.getText(), 7), 4, 12);
            int n = clamp(parseInt(winN.getText(), 4), 3, 8);
            game.getPlayerOne().setName(p1.getText());
            game.getPlayerTwo().setName(p2.getText());

            ConnectNGame fresh = new ConnectNGame(r, c,
                Math.min(n, Math.max(3, Math.min(r, c))),
                game.getPlayerOne().getName(), game.getPlayerTwo().getName());
            SwingUtilities.invokeLater(() -> {
                dispose();
                new ConnectNFrame(fresh).setVisible(true);
            });
        }
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }
    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

    private String turnText() { return game.getCurrentPlayer().getName() + "'s turn!"; }

    private class BoardPanel extends JPanel {
        private final int CELL = 72;
        private Image scaledChip;
        BoardPanel() {
            setPreferredSize(new Dimension(game.getColumns()*CELL, game.getRows()*CELL));
            int targetSize = CELL - 8; // slightly smaller than the cell
            scaledChip = chipOverlay.getImage()
                    .getScaledInstance(targetSize, targetSize, Image.SCALE_SMOOTH);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    int col = e.getX() / CELL;
                    if (col < 0 || col >= game.getColumns()) return;
                    int result = game.dropInColumn(col);
                    if (result == -1) return;
                    repaint();

                    if (result == 10) {
                        JOptionPane.showMessageDialog(ConnectNFrame.this,
                            game.getCurrentPlayer().getName() + " wins!",
                            "Game Over", JOptionPane.INFORMATION_MESSAGE);
                        for (MouseListener ml : getMouseListeners()) removeMouseListener(ml);
                    } else if (result == 9) {
                        JOptionPane.showMessageDialog(ConnectNFrame.this, "It's a draw!",
                            "Game Over", JOptionPane.INFORMATION_MESSAGE);
                        for (MouseListener ml : getMouseListeners()) removeMouseListener(ml);
                    } else {
                        status.setText(turnText());
                    }
                }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            g2.setColor(new Color(235, 238, 243));
            g2.fillRect(0,0,getWidth(),getHeight());
            g2.setColor(new Color(160, 170, 185));

            for (int r = 0; r < game.getRows(); r++) {
                for (int c = 0; c < game.getColumns(); c++) {
                    int x = c*CELL, y = r*CELL;
                    g2.drawRect(x, y, CELL, CELL);
                    char token = game.getBoard()[r][c];
                    if (token != 'E') {
                        int pad = 8, d = CELL - pad*2;
                        g2.setColor(token == 'Y' ? new Color(247, 209, 34)
                                                 : new Color(220, 50, 47));
                        g2.fillOval(x+pad, y+pad, d, d);
                        g2.setColor(new Color(0,0,0,50));
                        g2.drawOval(x+pad, y+pad, d, d);
                        Image img = chipOverlay.getImage();
                        g2.drawImage(scaledChip, x + 4, y + 4, this);
                    }

                }
            }
            g2.dispose();
        }
    }
}
