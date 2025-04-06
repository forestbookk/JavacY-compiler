package frontend.parser;

import frontend.lexer.Lexer;
import frontend.lexer.Token;
import utils.TokenType;

import java.util.ArrayList;
import java.util.Objects;

public class TokenStream {
    private int pos;
    private ArrayList<Token> tokens;

    public TokenStream() {
        this.pos = 0;
        // get all token
        this.tokens = Lexer.getTokenList();
    }

    public TokenType lookType(int offset) {
        return (pos + offset < tokens.size()) ?
                tokens.get(pos + offset).getType() :
                null;
    }

    public int lookLineNo(int offset) {
        return (pos + offset < tokens.size()) ?
                tokens.get(pos + offset).getLineNo() :
                0;
    }

    // read and update pos
    public Token read() {
        if (pos < tokens.size()) {
            if (Parser.isShouldRecord())
                Parser.printToBuffer(tokens.get(pos).toString());
            else
                Parser.getExpBufferForSpecialStmt().add(tokens.get(pos).toString());
        }
        return (pos < tokens.size()) ?
                tokens.get(pos++) : null;
    }

    public boolean hasNext() {
        return pos < tokens.size();
    }

}
