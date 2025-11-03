import transitionTable.*;

import java.util.Iterator;

public class Parser {
    private Iterator<State> tokens;
    private State currentToken;

    public Parser(Iterator<State> states) {
        this.tokens = states;
        this.currentToken = states.next();
    }

    private void advance() {
        if (tokens.hasNext()) {
            currentToken = tokens.next();
        }  else {
            currentToken = new State(StateEnum.EOF);
        }
    }

    private void error(String msg) {
        throw new RuntimeException(msg + " at token: " + describe(currentToken));
    }

    private String describe(State s) {
        if (s == null) return "null";
        var e = s.getEnum();
        return switch (e) {
            case Keyword -> "KEYWORD(" + s.getKeyword().value + ")";
            case Identifier -> "IDENTIFIER(" + s.getValue() + ")";
            case Integer -> "INTEGER(" + s.getValue() + ")";
            case Float -> "FLOAT(" + s.getValue() + ")";
            case Operator -> "OPERATOR(" + s.getOperator().name() + ")";
            case Parentheses -> "PARENTHESIS(" + s.getParentheses().name() + ")";
            default -> e.name();
        };
    }

    /* accept helpers */
    private void acceptKeyword(Keyword k) {
        if (currentToken.getEnum() == StateEnum.Keyword && currentToken.getKeyword() == k) {
            advance();
        } else {
            error("expected keyword " + k.value);
        }
    }

    private void acceptOperator(Operator op) {
        if (currentToken.getEnum() == StateEnum.Operator && currentToken.getOperator() == op) {
            advance();
        } else {
            error("expected operator " + op.name());
        }
    }

    private void acceptParenthesis(Parentheses p) {
        if (currentToken.getEnum() == StateEnum.Parentheses && currentToken.getParentheses() == p) {
            advance();
        } else {
            error("expected parenthesis " + p.name());
        }
    }

    private void accept(StateEnum expected) {
        if (currentToken.getEnum() == expected) {
            advance();
        } else {
            error("expected " + expected.name());
        }
    }

    /* Entry point */
    public void program() {
        stmt_list();
        // Optionally ensure EOF
        if (currentToken.getEnum() != StateEnum.EOF) {
            error("expected end of input");
        }
    }

    /* STMT_LIST ::= STATEMENT STMT_LIST | epsilon */
    public void stmt_list() {
        while (isStartOfStatement()) {
            statement();
        }
        // epsilon: do nothing
    }

    private boolean isStartOfStatement() {
        if (currentToken == null) return false;
        switch (currentToken.getEnum()) {
            case Keyword -> {
                Keyword k = currentToken.getKeyword();
                return k == Keyword.For || k == Keyword.While || k == Keyword.If;
            }
            case Identifier, Newline -> { return true; }
            default -> { return false; }
        }
    }

    /* STATEMENT ::= FOR_STMT | WHILE_STMT | IF_STMT | ASSIGN_STMT | newline */
    private void statement() {
        if (currentToken.getEnum() == StateEnum.Keyword) {
            switch (currentToken.getKeyword()) {
                case For -> { for_stmt(); return; }
                case While -> { while_stmt(); return; }
                case If -> { if_stmt(); return; }
                default -> { /* fallthrough to error */ }
            }
        }

        if (currentToken.getEnum() == StateEnum.Identifier) {
            assign_stmt();
            return;
        }

        if (currentToken.getEnum() == StateEnum.Newline) {
            accept(StateEnum.Newline);
            return;
        }

        error("expected statement");
    }

    /* ASSIGN_STMT ::= identifier assignment EXPRESSION newline */
    private void assign_stmt() {
        accept(StateEnum.Identifier);
        acceptOperator(Operator.Assign);
        expression();
        accept(StateEnum.Newline);
    }

    /* FOR_STMT ::= for identifier assignment EXPRESSION in EXPRESSION colon newline BLOCK */
    private void for_stmt() {
        acceptKeyword(Keyword.For);
        accept(StateEnum.Identifier);
        acceptOperator(Operator.Assign);
        expression();
        acceptKeyword(Keyword.In);
        expression();
        acceptOperator(Operator.Colon);
        accept(StateEnum.Newline);
        block();
    }

    /* WHILE_STMT ::= while CONDITION colon newline BLOCK */
    private void while_stmt() {
        acceptKeyword(Keyword.While);
        condition();
        acceptOperator(Operator.Colon);
        accept(StateEnum.Newline);
        block();
    }

    /* IF_STMT ::= if CONDITION colon newline BLOCK ELIF_ELSE_PART */
    private void if_stmt() {
        acceptKeyword(Keyword.If);
        condition();
        acceptOperator(Operator.Colon);
        accept(StateEnum.Newline);
        block();
        elif_else_part();
    }

    /* ELIF_ELSE_PART ::= (elif ...)* | else ... | epsilon */
    private void elif_else_part() {
        while (currentToken.getEnum() == StateEnum.Keyword && currentToken.getKeyword() == Keyword.Elif) {
            acceptKeyword(Keyword.Elif);
            condition();
            acceptOperator(Operator.Colon);
            accept(StateEnum.Newline);
            block();
        }
        if (currentToken.getEnum() == StateEnum.Keyword && currentToken.getKeyword() == Keyword.Else) {
            acceptKeyword(Keyword.Else);
            acceptOperator(Operator.Colon);
            accept(StateEnum.Newline);
            block();
        }
        // epsilon: do nothing
    }

    /* BLOCK :: INDENTED_STMT MORE_INDENTED_STMTS */
    private void block() {
        indented_statement(); // first mandatory
        while (currentToken.getEnum() == StateEnum.Tab) {
            indented_statement();
        }
    }

    /* INDENTED_STMT ::= tab STATEMENT */
    private void indented_statement() {
        accept(StateEnum.Tab);
        statement();
    }

    /* CONDITION ::= EXPRESSION COMP_OP EXPRESSION */
    private void condition() {
        expression();
        comp_op();
        expression();
    }

    /* COMP_OP ::= less_than | greater_than | ... */
    private void comp_op() {
        if (currentToken.getEnum() != StateEnum.Operator) {
            error("invalid comparison operator");
        }
        Operator op = currentToken.getOperator();
        returnIfComparison(op);
        advance();
    }

    private void returnIfComparison(Operator op) {
        switch (op) {
            case Less, Greater, LessEquals, GreaterEquals, Equals, NotEquals -> { /* ok */ }
            default -> error("invalid comparison operator");
        }
    }

    /* EXPRESSION ::= TERM EXPR_PRIME */
    private void expression() {
        term();
        expr_prime();
    }

    /* EXPR_PRIME ::= (add|subtract) TERM EXPR_PRIME | epsilon */
    private void expr_prime() {
        while (currentToken.getEnum() == StateEnum.Operator &&
                (currentToken.getOperator() == Operator.Plus || currentToken.getOperator() == Operator.Minus)) {
            if (currentToken.getOperator() == Operator.Plus) {
                acceptOperator(Operator.Plus);
            } else {
                acceptOperator(Operator.Minus);
            }
            term();
        }
        // epsilon
    }

    /* TERM ::= FACTOR TERM_PRIME */
    private void term() {
        factor();
        term_prime();
    }

    /* TERM_PRIME ::= (multiply|divide) FACTOR TERM_PRIME | epsilon */
    private void term_prime() {
        while (currentToken.getEnum() == StateEnum.Operator &&
                (currentToken.getOperator() == Operator.Multiply || currentToken.getOperator() == Operator.Divide)) {
            if (currentToken.getOperator() == Operator.Multiply) {
                acceptOperator(Operator.Multiply);
            } else {
                acceptOperator(Operator.Divide);
            }
            factor();
        }
        // epsilon
    }

    /* FACTOR ::= left_parenthesis EXPRESSION right_parenthesis | identifier | NUMBER */
    private void factor() {
        if (currentToken.getEnum() == StateEnum.Parentheses &&
                currentToken.getParentheses() == Parentheses.LeftParenthesis) {
            acceptParenthesis(Parentheses.LeftParenthesis);
            expression();
            acceptParenthesis(Parentheses.RightParenthesis);
            return;
        }
        if (currentToken.getEnum() == StateEnum.Identifier) {
            accept(StateEnum.Identifier);
            return;
        }
        if (currentToken.getEnum() == StateEnum.Integer || currentToken.getEnum() == StateEnum.Float) {
            number();
            return;
        }
        error("expected parenthesis, identifier, or number");
    }

    /* NUMBER ::= integer_literal | float_literal */
    private void number() {
        if (currentToken.getEnum() == StateEnum.Integer) {
            accept(StateEnum.Integer);
        } else if (currentToken.getEnum() == StateEnum.Float) {
            accept(StateEnum.Float);
        } else {
            error("expected number");
        }
    }
}