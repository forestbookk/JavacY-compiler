package frontend.lexer;

import utils.TokenType;

import java.util.ArrayList;

public class Token {
    private final TokenType type;
    private final String content; // 字面量
    private final int lineNo;
    private ArrayList<Integer> asciiList; // 字符和字符串的ascii字符集

    public Token(TokenType type, String content, int lineNo) {
        this.type = type;
        this.content = content;
        this.lineNo = lineNo;
    }

    public Token(ArrayList<Integer> asciiList, TokenType type, String content, int lineNo) {
        this.asciiList = asciiList;
        this.type = type;
        this.content = content;
        this.lineNo = lineNo;
    }

    public ArrayList<Integer> getAsciiList() {
        return asciiList;
    }

    public TokenType getType() {
        return type;
    }

    public String getContent() {
        return this.content;
    }

    public int getLineNo() {
        return this.lineNo;
    }

    @Override
    public String toString() {
        return type.toString() + " " + content;
    }
}
