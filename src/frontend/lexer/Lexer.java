package frontend.lexer;

import midend.Optimizer;
import utils.ErrorType;
import utils.Printer;
import utils.TokenType;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.*;

public class Lexer {
    public static boolean TRICK = false;
    private final PushbackInputStream inputStream;
    private static final ArrayList<Token> tokenList = new ArrayList<>();
    private int lineNo;
    private char cur;
    private boolean isError;
    private final StringBuilder content;
    private static final Map<Character, Integer> escCh2Ascii = new HashMap<>() {{
        put('a', 7);
        put('b', 8);
        put('t', 9);
        put('n', 10);
        put('v', 11);
        put('f', 12);
        put('"', 34);
        put('\'', 39);
        put('\\', 92);
        put('0', 0);
    }};

    // Single character token map for better performance
    private static final Map<Character, TokenType> SINGLE_CHAR_TOKENS = Map.ofEntries(
            Map.entry('+', TokenType.PLUS),
            Map.entry('-', TokenType.MINU),
            Map.entry('*', TokenType.MULT),
            Map.entry('%', TokenType.MOD),
            Map.entry(';', TokenType.SEMICN),
            Map.entry(',', TokenType.COMMA),
            Map.entry('(', TokenType.LPARENT),
            Map.entry(')', TokenType.RPARENT),
            Map.entry('[', TokenType.LBRACK),
            Map.entry(']', TokenType.RBRACK),
            Map.entry('{', TokenType.LBRACE),
            Map.entry('}', TokenType.RBRACE)
    );


    public Lexer(PushbackInputStream inputStream) {
        this.inputStream = inputStream;
        this.lineNo = 1;
        this.content = new StringBuilder();
    }

    public void addTokenToList(TokenType type, String content) {
        Token token = new Token(type, content, lineNo);
        tokenList.add(token);
    }

    public void addTokenToList(ArrayList<Integer> asciiList, TokenType type, String content) {
        Token token = new Token(asciiList, type, content, lineNo);
        tokenList.add(token);
    }

    public boolean isNewLine(int cur) throws IOException {
        if (cur == '\r') {
            int nextChar = inputStream.read();
            return nextChar == '\n';  // Handle Windows style "\r\n"
        }
        return cur == '\n';  // Unix style newline
    }

    public boolean updateAndIsNotEOF() throws IOException {
        int ascii = inputStream.read();
        cur = (char) ascii;
        return ascii != -1 && ascii != '\uFFFF';
    }

    public void retChAndSetErr() throws IOException {
        inputStream.unread(cur);
        isError = true;
    }

    public TokenType getTokenType(String s) {
        return switch (s) {
            case "main" -> TokenType.MAINTK;
            case "const" -> TokenType.CONSTTK;
            case "int" -> TokenType.INTTK;
            case "char" -> TokenType.CHARTK;
            case "break" -> TokenType.BREAKTK;
            case "continue" -> TokenType.CONTINUETK;
            case "if" -> TokenType.IFTK;
            case "else" -> TokenType.ELSETK;
            case "for" -> TokenType.FORTK;
            case "getint" -> TokenType.GETINTTK;
            case "getchar" -> TokenType.GETCHARTK;
            case "printf" -> TokenType.PRINTFTK;
            case "return" -> TokenType.RETURNTK;
            case "void" -> TokenType.VOIDTK;
            default -> TokenType.IDENFR;
        };
    }

    private void parseStringOrCharLiteral(char delimiter) throws IOException {
        ArrayList<Integer> asciiList = new ArrayList<>();

        content.setLength(0);  // Clear the content
        //content.append(delimiter);
        while (updateAndIsNotEOF()) {
            if (cur == delimiter) {
                //content.append(cur);
                break;
            }
            if (cur == '\\' && updateAndIsNotEOF() && escCh2Ascii.containsKey(cur)) {
                content.append('\\').append(cur);
                asciiList.add(escCh2Ascii.get(cur));
            } else if (cur >= 32 && cur <= 126) {
                content.append(cur);
                asciiList.add((int) cur);
            } else {
                retChAndSetErr();
                break;
            }
        }
        if (!isError) {
            addTokenToList(asciiList, delimiter == '"' ? TokenType.STRCON : TokenType.CHRCON, content.toString());
        }
    }

    public void solve() throws IOException {
        while (updateAndIsNotEOF()) {
            isError = false;
            content.setLength(0);  // Clear content before use

            // Skip spaces
            if (cur == ' ' || cur == '\t') continue;

                // Handle comment
            else if (cur == '/') {
                if (updateAndIsNotEOF()) {
                    if (cur == '/') {
                        // Single line comment
                        while (updateAndIsNotEOF() && cur != '\n') {
                        }
                        lineNo++; // handle '\n'
                    } else if (cur == '*') {
                        // Multi-line comment
                        int flag = 0;
                        while (updateAndIsNotEOF()) {
                            if (cur == '*') {
                                flag = 1;
                            } else if (flag == 1 && cur == '/') {
                                break;
                            } else {
                                flag = 0;
                                if (cur == '\n') {
                                    lineNo++;
                                }
                            }
                        }
                    } else {
                        inputStream.unread(cur);
                        addTokenToList(TokenType.DIV, "/");
                    }
                }
            }

            // Handle newlines
            else if (isNewLine(cur)) {
                lineNo++;
                continue;
            }

            // Handle single character tokens
            else if (SINGLE_CHAR_TOKENS.containsKey(cur)) {
                addTokenToList(SINGLE_CHAR_TOKENS.get(cur), String.valueOf(cur));
            }

            // && or ||
            else if (cur == '&') {
                if (updateAndIsNotEOF() && cur == '&') {
                    addTokenToList(TokenType.AND, "&&");
                } else {
                    retChAndSetErr();
                    /* check error */
                    // a
                    Printer.addError(lineNo, ErrorType.a);
                    addTokenToList(TokenType.AND, "&");
                }
            } else if (cur == '|') {
                if (updateAndIsNotEOF() && cur == '|') {
                    addTokenToList(TokenType.OR, "||");
                } else {
                    retChAndSetErr();
                    /* check error */
                    // a
                    Printer.addError(lineNo, ErrorType.a);
                    addTokenToList(TokenType.OR, "|");
                }
            }

            // > < = !
            else if (cur == '<') {
                if (updateAndIsNotEOF() && cur == '=') {
                    addTokenToList(TokenType.LEQ, "<=");
                } else {
                    inputStream.unread(cur);
                    addTokenToList(TokenType.LSS, "<");
                }
            } else if (cur == '>') {
                if (updateAndIsNotEOF() && cur == '=') {
                    addTokenToList(TokenType.GEQ, ">=");
                } else {
                    inputStream.unread(cur);
                    addTokenToList(TokenType.GRE, ">");
                }
            } else if (cur == '=') {
                if (updateAndIsNotEOF() && cur == '=') {
                    addTokenToList(TokenType.EQL, "==");
                } else {
                    inputStream.unread(cur);
                    addTokenToList(TokenType.ASSIGN, "=");
                }
            } else if (cur == '!') {
                if (updateAndIsNotEOF() && cur == '=') {
                    addTokenToList(TokenType.NEQ, "!=");
                } else {
                    inputStream.unread(cur);
                    addTokenToList(TokenType.NOT, "!");
                }
            }

            // Handle multi-character tokens (identifiers and keywords)
            else if (isNonDigit(cur)) {
                content.append(cur);
                while (updateAndIsNotEOF() && (isNonDigit(cur) || isDigit(cur))) {
                    content.append(cur);
                }
                inputStream.unread(cur);
                addTokenToList(getTokenType(content.toString()), content.toString());
            }

            // Handle integer constants
            else if (isDigit(cur)) {
                content.append(cur);
                while (updateAndIsNotEOF() && isDigit(cur)) {
                    content.append(cur);
                }
                inputStream.unread(cur);
                addTokenToList(TokenType.INTCON, content.toString());
            }

            // Handle string constants
            else if (cur == '"') {
                parseStringOrCharLiteral('"');
            }

            // Handle character constants
            else if (cur == '\'') {
                parseStringOrCharLiteral('\'');
            }
        }
    }

    public boolean isDigit(int c) {
        return c >= '0' && c <= '9';
    }

    public boolean isNonDigit(int c) {
        return (c == '_') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    public static ArrayList<Token> getTokenList() {
        return tokenList;
    }
}
