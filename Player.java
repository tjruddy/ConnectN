public class Player {
    private final char symbol;
    private String name;

    public Player(char symbol) {
        this(symbol, symbol == 'Y' ? "Yellow" : "Red");
    }

    public Player(char symbol, String name) {
        this.symbol = symbol;
        this.name = name == null || name.isBlank()
                ? (symbol == 'Y' ? "Yellow" : "Red")
                : name;
    }

    public char getSymbol() { return symbol; }
    public String getName() { return name; }
    public void setName(String name) {
        if (name != null && !name.isBlank()) this.name = name;
    }
}
