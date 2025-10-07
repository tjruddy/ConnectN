public class ConnectNGame {
    private int N;
    private int rows;
    private int columns;
    private char[][] board;
    private int[] lastMove = {-1, -1};
    private Player playerOne;
    private Player playerTwo;
    private Player player;
    private int undoAvailable = 0;

    public ConnectNGame() { this(6, 7, 4, "Yellow", "Red"); }

    public ConnectNGame(int row, int col, int win, String one, String two) {
        rows = row; columns = col; N = win;
        board = new char[rows][columns];
        playerOne = new Player('Y', one);
        playerTwo = new Player('R', two);
        player = playerOne;
        initializeBoard();
    }

    public Player getCurrentPlayer() { return player; }
    public Player getPlayerOne() { return playerOne; }
    public Player getPlayerTwo() { return playerTwo; }
    public char[][] getBoard() { return board; }
    public int getRows() { return rows; }
    public int getColumns() { return columns; }
    public int[] getLastMove() { return lastMove; }

    public void initializeBoard() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < columns; c++)
                board[r][c] = 'E';
        lastMove[0] = lastMove[1] = -1;
        player = playerOne;
        undoAvailable = 0;
    }

    private void changePlayer() { player = (player == playerOne) ? playerTwo : playerOne; }

    public boolean isColumnFull(int col) { return board[0][col] != 'E'; }

    public int dropInColumn(int col) {
        if (col < 0 || col >= columns) return -1;
        int row = -1;
        for (int r = rows - 1; r >= 0; r--) {
            if (board[r][col] == 'E') { row = r; break; }
        }
        if (row == -1) return -1;

        board[row][col] = player.getSymbol();
        lastMove[0] = row; lastMove[1] = col;
        undoAvailable = 1;

        if (checkForWin(row, col)) return 10;
        if (checkDraw()) return 9;

        changePlayer();
        return 0;
    }

    public boolean undo() {
        if (undoAvailable == 0 || lastMove[0] < 0) return false;
        board[lastMove[0]][lastMove[1]] = 'E';
        changePlayer();
        undoAvailable = 0;
        return true;
    }

    private boolean checkDraw() {
        for (int c = 0; c < columns; c++)
            if (board[0][c] == 'E') return false;
        return true;
    }

    private boolean checkForWin(int r, int c) {
        char s = board[r][c];
        return count(r,c, 1,0,s) + count(r,c,-1,0,s) - 1 >= N
            || count(r,c, 0,1,s) + count(r,c, 0,-1,s) - 1 >= N
            || count(r,c, 1,1,s) + count(r,c,-1,-1,s) - 1 >= N
            || count(r,c, 1,-1,s) + count(r,c,-1, 1,s) - 1 >= N;
    }

    private int count(int r, int c, int dr, int dc, char s) {
        int ct = 0;
        while (r >= 0 && r < rows && c >= 0 && c < columns && board[r][c] == s) {
            ct++; r += dr; c += dc;
        }
        return ct;
    }
}
