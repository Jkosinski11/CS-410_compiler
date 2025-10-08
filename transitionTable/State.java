package transitionTable;

public class State {
    private EnumState e;
    private Keyword kw;
    private Parentheses p;
    private Operator op;
    private String value;
    private int count;

    public State(EnumState e) {
        this.e = e;
    }

    public EnumState getEnum() {
        return e;
    }

    public State setEnum(EnumState e) {
        this.e = e;
        return this;
    }

    public int getCount() {
        if (e == EnumState.Whitespace) return count;
        else throw new IllegalStateException();
    }

    public State setCount(int count) {
        if (e == EnumState.Whitespace) this.count = count;
        else throw new IllegalStateException();
        return this;
    }

    public Keyword getKeyword() {
        if (e == EnumState.Keyword) return kw;
        else throw new IllegalStateException();
    }

    public State setKeyword(Keyword kw) {
        if (e == EnumState.Keyword) this.kw = kw;
        else throw new IllegalStateException();
        return this;
    }

    public String getValue() {
        return switch (e) {
            case Identifier, Point, Integer, Float -> value;
            default -> throw new IllegalStateException();
        };
    }

    public State setValue(String value) {
        switch (e) {
            case Identifier, Point, Integer, Float -> this.value = value;
            default -> throw new IllegalStateException();
        }
        return this;
    }

    public Parentheses getParentheses() {
        if (e == EnumState.Parentheses) return p;
        else throw new IllegalStateException();
    }

    public State setParentheses(Parentheses p) {
        if (e == EnumState.Parentheses) this.p = p;
        else throw new IllegalStateException();
        return this;
    }

    public Operator getOperator() {
        if (e == EnumState.Operator) return op;
        else throw new IllegalStateException();
    }
    public State setOperator(Operator op) {
        if (e == EnumState.Operator) this.op = op;
        else throw new IllegalStateException();
        return this;
    }
}
