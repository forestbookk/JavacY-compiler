package frontend.parser.AST.Leaf;

import frontend.lexer.Token;
import frontend.parser.AST.Node;
import utils.SyntaxType;
import utils.TokenType;

import java.util.ArrayList;

public class Terminator extends Node {
    private Token token;

    public Terminator(Token token) {
        super(SyntaxType.Leaf, new ArrayList<>(), token.getLineNo(), token.getLineNo());
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    public TokenType getTokenType() {
        return this.token.getType();
    }

    public String getContent() {
        return this.token.getContent();
    }

    public int getLineNo() {
        return this.token.getLineNo();
    }
}
