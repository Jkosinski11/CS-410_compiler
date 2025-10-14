import transitionTable.*;

public interface Transitions {
    static  State invalid(State ignoredState, Alphabet ignoredAlphabet) {
        return new State(StateEnum.Invalid);
    }

    static State newIdentifier(State ignoredState, Alphabet currentAlphabet) {
        return new State(StateEnum.Identifier).setValue(String.valueOf(currentAlphabet.getChar()));
    }

    static State extendIdentifier(State currentState, Alphabet currentAlphabet) {
        String extended = currentState.getValue() +
                currentAlphabet.getChar();
        return switch (extended) {
            case "if" -> new State(StateEnum.Keyword).setKeyword(Keyword.If);
            case "else" -> new State(StateEnum.Keyword).setKeyword(Keyword.Else);
            case "for" -> new State(StateEnum.Keyword).setKeyword(Keyword.For);
            case "while" -> new State(StateEnum.Keyword).setKeyword(Keyword.While);
            case "elif" -> new State(StateEnum.Keyword).setKeyword(Keyword.Elif);
            default -> new State(StateEnum.Identifier).setValue(extended);
        };
    }

    static State keywordToIdentifier(State currentState, Alphabet currentAlphabet) {
        String newValue = currentState.getKeyword().value +
                currentAlphabet.getChar();
        return new State(StateEnum.Identifier).setValue(newValue);
    }

    static State newInteger(State ignoredState, Alphabet currentAlphabet) {
        return new State(StateEnum.Integer).setValue(String.valueOf(currentAlphabet.getChar()));
    }

    static State extendInteger(State currentState, Alphabet currentAlphabet) {
        String newValue = currentState.getValue() +
                currentAlphabet.getChar();
        return new State(StateEnum.Integer).setValue(newValue);
    }

    static State integerToPoint(State currentState, Alphabet ignoredAlphabet) {
        String newValue = currentState.getValue() +
                '.';
        return new State(StateEnum.Point).setValue(newValue);
    }

    static State toFloat(State currentState, Alphabet currentAlphabet) {
        String newValue = currentState.getValue() +
                currentAlphabet.getChar();
        return new State(StateEnum.Float).setValue(newValue);
    }

    static State tab(State ignoredState, Alphabet ignoredAlphabet) {
        return new State(StateEnum.Tab);
    }

    static State whitespace(State ignoredState, Alphabet ignoredAlphabet) {
        return new State(StateEnum.Whitespace).setCount(1);
    }

    static State extendWhitespace(State currentState, Alphabet ignoredAlphabet) {
        int newCount = currentState.getCount() + 1;
        if (newCount == Main.SPACES_IN_TAB) {
            return new State(StateEnum.Tab);
        }
        return new State(StateEnum.Whitespace).setCount(newCount);
    }

    static State newline(State ignoredState, Alphabet ignoredAlphabet) {
        return new State(StateEnum.Newline);
    }

    static State parenthesis(State ignoredState, Alphabet currentAlphabet) {
        return new State(StateEnum.Parentheses).setParentheses(currentAlphabet.getParentheses());
    }

    static State newOperator(State ignoredState, Alphabet currentAlphabet) {
        return new State(StateEnum.Operator).setOperator(currentAlphabet.getOperator());
    }

    static State extendOperator(State currentState, Alphabet currentAlphabet) {
        if (currentAlphabet.getOperator() == Operator.Assign) {
            return switch (currentState.getOperator()) {
                case Assign -> new State(StateEnum.Operator).setOperator(Operator.Equals);
                case Greater -> new State(StateEnum.Operator).setOperator(Operator.GreaterEquals);
                case Less -> new State(StateEnum.Operator).setOperator(Operator.LessEquals);
                case Bang -> new State(StateEnum.Operator).setOperator(Operator.NotEquals);
                default -> new State(StateEnum.Invalid);
            };
        } else {
            return new State(StateEnum.Invalid);
        }
    }
}
