package transitionTable;

public enum Keyword {
    Elif("elif"),
    Else("else"),
    For("for"),
    If("if"),
    While("while");

    public final String value;
    Keyword(String value) {
        this.value = value;
    }
}
