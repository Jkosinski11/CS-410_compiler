package transitionTable;

public class State {
    private final StateEnum e;
    private Keyword kw;
    private Parentheses p;
    private Operator op;
    private String value;
    private int count;

    public State(StateEnum e) {
        this.e = e;
    }

    public StateEnum getEnum() {
        return e;
    }

    public int getCount() {
        if (e == StateEnum.Whitespace) return count;
        else throw new IllegalStateException();
    }

    public State setCount(int count) {
        if (e == StateEnum.Whitespace) this.count = count;
        else throw new IllegalStateException();
        return this;
    }

    public Keyword getKeyword() {
        if (e == StateEnum.Keyword) return kw;
        else throw new IllegalStateException();
    }

    public State setKeyword(Keyword kw) {
        if (e == StateEnum.Keyword) this.kw = kw;
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
        if (e == StateEnum.Parentheses) return p;
        else throw new IllegalStateException();
    }

    public State setParentheses(Parentheses p) {
        if (e == StateEnum.Parentheses) this.p = p;
        else throw new IllegalStateException();
        return this;
    }

    public Operator getOperator() {
        if (e == StateEnum.Operator) return op;
        else throw new IllegalStateException();
    }
    public State setOperator(Operator op) {
        if (e == StateEnum.Operator) this.op = op;
        else throw new IllegalStateException();
        return this;
    }

    public void print() {
        switch (e) {
            case Start -> System.out.println("START");
            case Newline -> System.out.println("NEWLINE");
            case Whitespace -> System.out.println("WHITESPACE");
            case Tab -> System.out.println("TAB");
            case Invalid -> System.out.println("INVALID");
            case Parentheses -> System.out.println("PARENTHESIS,\t"+ p.name());
            case Keyword -> System.out.println("KEYWORD,\t"+ kw.value);
            case Operator -> System.out.println("OPERATOR,\t"+ op.name());
            case Identifier -> System.out.println("IDENTIFIER,\t"+ value);
            case Integer -> System.out.println("INTEGER,\t"+ value);
            case Float -> System.out.println("FLOAT,\t"+ value);
            default -> {}
        }
    }
}
