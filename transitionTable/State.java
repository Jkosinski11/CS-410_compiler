package transitionTable;

import java.io.BufferedWriter;
import java.io.IOException;

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

    public void print(BufferedWriter bw) throws IOException {
        switch (e) {
            case Start ->       bw.write("START");
            case Newline ->     bw.write("NEWLINE");
            case Whitespace ->  bw.write("WHITESPACE");
            case Tab ->         bw.write("TAB");
            case Invalid ->     bw.write("INVALID");
            case Parentheses -> bw.write("PARENTHESIS, "+ p.name());
            case Keyword ->     bw.write("KEYWORD, "+ kw.value);
            case Operator ->    bw.write("OPERATOR, "+ op.name());
            case Identifier ->  bw.write("IDENTIFIER, "+ value);
            case Integer ->     bw.write("INTEGER, "+ value);
            case Float ->       bw.write("FLOAT, "+ value);
            default -> {}
        }
        bw.newLine();
    }
}
