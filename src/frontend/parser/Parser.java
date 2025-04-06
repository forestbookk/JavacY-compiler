package frontend.parser;

import frontend.lexer.Token;
import frontend.parser.AST.CompUnit;
import frontend.parser.AST.Exp.AddExp;
import frontend.parser.AST.Exp.Character;
import frontend.parser.AST.Exp.Cond;
import frontend.parser.AST.Exp.ConstExp;
import frontend.parser.AST.Exp.EqExp;
import frontend.parser.AST.Exp.Exp;
import frontend.parser.AST.Var.InitVal;
import frontend.parser.AST.Exp.LAndExp;
import frontend.parser.AST.Exp.LOrExp;
import frontend.parser.AST.Exp.LVal;
import frontend.parser.AST.Exp.MulExp;
import frontend.parser.AST.Exp.Number;
import frontend.parser.AST.Exp.PrimaryExp;
import frontend.parser.AST.Exp.RelExp;
import frontend.parser.AST.Exp.UnaryExp;
import frontend.parser.AST.Exp.UnaryOp;
import frontend.parser.AST.Func.FuncDef;
import frontend.parser.AST.Func.FuncFParam;
import frontend.parser.AST.Func.FuncFParams;
import frontend.parser.AST.Func.FuncRParams;
import frontend.parser.AST.Func.FuncType;
import frontend.parser.AST.Func.Block;
import frontend.parser.AST.Func.MainFuncDef;
import frontend.parser.AST.Leaf.Terminator;
import frontend.parser.AST.Node;
import frontend.parser.AST.Stmt.BlockStmt;
import frontend.parser.AST.Stmt.BreakStmt;
import frontend.parser.AST.Stmt.ContinueStmt;
import frontend.parser.AST.Stmt.ExpStmt;
import frontend.parser.AST.Stmt.ForStmt;
import frontend.parser.AST.Stmt.GetCharStmt;
import frontend.parser.AST.Stmt.GetIntStmt;
import frontend.parser.AST.Stmt.IfStmt;
import frontend.parser.AST.Stmt.LValExpStmt;
import frontend.parser.AST.Stmt.PrintfStmt;
import frontend.parser.AST.Stmt.ReturnStmt;
import frontend.parser.AST.Stmt.WholeForStmt;
import frontend.parser.AST.Var.BType;
import frontend.parser.AST.Func.BlockItem;
import frontend.parser.AST.Var.ConstDef;
import frontend.parser.AST.Var.ConstInitVal;
import frontend.parser.AST.Var.Decl;
import frontend.parser.AST.Var.VarDecl;
import frontend.parser.AST.Var.VarDef;
import utils.ErrorType;
import utils.Printer;
import utils.SyntaxType;
import utils.TokenType;

import java.util.ArrayList;

public class Parser {
    private final TokenStream tokenStream;
    private static ArrayList<Node> astNodes;
    private static ArrayList<String> outputBuffer;
    private static ArrayList<String> expBufferForSpecialStmt;
    private static boolean shouldRecord = true;

    private int endLine = 0;
    private int errLine = 0;

    public Parser() {
        tokenStream = new TokenStream();
        outputBuffer = new ArrayList<>();
        astNodes = new ArrayList<>();
        expBufferForSpecialStmt = new ArrayList<>();
    }

    public static boolean isShouldRecord() {
        return shouldRecord;
    }

    public static ArrayList<String> getExpBufferForSpecialStmt() {
        return expBufferForSpecialStmt;
    }

    public Node parseCompUnit() {
        ArrayList<Node> children = new ArrayList<>();
        Node node;

        int startLine = tokenStream.lookLineNo(0);

        while (tokenStream.hasNext()) {
            if (tokenStream.lookType(1) == TokenType.MAINTK) {
                node = parseMainFuncDef();
                children.add(node);
                break;
            } else if (tokenStream.lookType(2) == TokenType.LPARENT) {
                node = parseFuncDef();
                children.add(node);
            } else {
                node = parseDecl();
                children.add(node);
            }
        }
        endLine = tokenStream.lookLineNo(-1);

        Parser.printToBuffer("<" + SyntaxType.CompUnit + ">");
        return new CompUnit(children, startLine, endLine);
    }

    public Node parseDecl() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);
        children.add(tokenStream.lookType(0) == TokenType.CONSTTK ? parseConstDecl() : parseVarDecl());
        endLine = tokenStream.lookLineNo(-1);
        return new Decl(children, startLine, endLine);
    }

    // 缺少分号 i
    public Node parseConstDecl() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);
        // const
        children.add(new Terminator(tokenStream.read()));
        children.add(parseBType());
        children.add(parseConstDef());
        while (tokenStream.lookType(0) == TokenType.COMMA) {
            children.add(new Terminator(tokenStream.read()));
            children.add(parseConstDef());
        }
        checkErrorI(children);

        endLine = tokenStream.lookLineNo(-1);
        Parser.printToBuffer("<" + SyntaxType.ConstDecl + ">");
        return new Node(SyntaxType.ConstDecl, children, startLine, endLine);
    }

    public Node parseBType() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);
        children.add(new Terminator(tokenStream.read()));

        endLine = tokenStream.lookLineNo(-1);
        return new BType(children, startLine, endLine);
    }

    public Node parseConstDef() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);

        // Ident
        children.add(new Terminator(tokenStream.read()));
        if (tokenStream.lookType(0) == TokenType.LBRACK) {
            // [
            children.add(new Terminator(tokenStream.read()));
            // ConstExp
            children.add(parseConstExp());
            // parse ]
            checkErrorK(children);
        }
        // =
        children.add(new Terminator(tokenStream.read()));
        children.add(parseConstInitVal());

        endLine = tokenStream.lookLineNo(-1);
        Parser.printToBuffer("<" + SyntaxType.ConstDef + ">");
        return new ConstDef(children, startLine, endLine);
    }

    public Node parseConstInitVal() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);

        // '{' [ ConstExp { ',' ConstExp } ] '}'
        if (tokenStream.lookType(0) == TokenType.LBRACE) {
            children.add(new Terminator(tokenStream.read())); // '{'
            if (tokenStream.lookType(0) != TokenType.RBRACE) {
                children.add(parseConstExp());
                while (tokenStream.lookType(0) == TokenType.COMMA) {
                    children.add(new Terminator(tokenStream.read()));
                    children.add(parseConstExp());
                }
            }
            children.add(new Terminator(tokenStream.read())); // '}'
        }
        // StringConst
        else if (tokenStream.lookType(0) == TokenType.STRCON) {
            children.add(new Terminator(tokenStream.read()));
        }
        // ConstExp
        else {
            children.add(parseConstExp());
        }

        endLine = tokenStream.lookLineNo(-1);
        printToBuffer("<" + SyntaxType.ConstInitVal + ">");
        return new ConstInitVal(children, startLine, endLine);
    }

    public Node parseVarDecl() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);

        children.add(parseBType());
        children.add(parseVarDef());
        while (tokenStream.lookType(0) == TokenType.COMMA) {
            children.add(new Terminator(tokenStream.read()));
            children.add(parseVarDef());
        }
        checkErrorI(children); // i

        endLine = tokenStream.lookLineNo(-1);
        printToBuffer("<" + SyntaxType.VarDecl + ">");
        return new VarDecl(children, startLine, endLine);
    }

    public Node parseVarDef() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);

        children.add(new Terminator(tokenStream.read())); // Ident
        if (tokenStream.lookType(0) == TokenType.LBRACK) {
            children.add(new Terminator(tokenStream.read())); // '['
            children.add(parseConstExp());
            // parse ]
            checkErrorK(children);
        }
        if (tokenStream.lookType(0) == TokenType.ASSIGN) {
            children.add(new Terminator(tokenStream.read()));
            children.add(parseInitVal());
        }

        endLine = tokenStream.lookLineNo(-1);
        printToBuffer("<" + SyntaxType.VarDef + ">");
        return new VarDef(children, startLine, endLine);
    }

    public Node parseInitVal() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);

        // StringConst
        if (tokenStream.lookType(0) == TokenType.STRCON) {
            children.add(new Terminator(tokenStream.read())); // StrCon
        }
        // '{' [ Exp { ',' Exp } ] '}'
        else if (tokenStream.lookType(0) == TokenType.LBRACE) {
            children.add(new Terminator(tokenStream.read())); // '{'
            if (tokenStream.lookType(0) != TokenType.RBRACE) {
                children.add(parseExp()); // Exp
                while (tokenStream.lookType(0) == TokenType.COMMA) {
                    children.add(new Terminator(tokenStream.read())); // ','
                    children.add(parseExp()); // Exp
                }
            }
            children.add(new Terminator(tokenStream.read())); // '}'
        }
        // Exp
        else {
            children.add(parseExp());
        }
        endLine = tokenStream.lookLineNo(-1);
        printToBuffer("<" + SyntaxType.InitVal + ">");
        return new InitVal(children, startLine, endLine);
    }

    public Node parseFuncDef() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);

        children.add(parseFuncType());
        children.add(new Terminator(tokenStream.read())); // Ident
        children.add(new Terminator(tokenStream.read())); // '('
        // [FuncFPrams] First=['int', 'char']
        if (tokenStream.lookType(0) == TokenType.INTTK ||
                tokenStream.lookType(0) == TokenType.CHARTK) {
            children.add(parseFuncFParams());
        }
        // parse )
        checkErrorJ(children);
        children.add(parseBlock()); // Block
        endLine = tokenStream.lookLineNo(-1);
        printToBuffer("<" + SyntaxType.FuncDef + ">");
        return new FuncDef(children, startLine, endLine);
    }

    public Node parseMainFuncDef() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);
        for (int i = 0; i < 3; i++) {
            // int main (
            children.add(new Terminator(tokenStream.read()));
        }
        // parse )
        checkErrorJ(children);
        children.add(parseBlock());
        endLine = tokenStream.lookLineNo(-1);
        printToBuffer("<" + SyntaxType.MainFuncDef + ">");
        return new MainFuncDef(children, startLine, endLine);
    }

    public Node parseFuncType() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);
        children.add(new Terminator(tokenStream.read()));
        endLine = tokenStream.lookLineNo(-1);
        printToBuffer("<" + SyntaxType.FuncType + ">");
        return new FuncType(children, startLine, endLine);
    }

    public Node parseFuncFParams() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);
        children.add(parseFuncFParam());
        while (tokenStream.lookType(0) == TokenType.COMMA) {
            children.add(new Terminator(tokenStream.read()));
            children.add(parseFuncFParam());
        }
        endLine = tokenStream.lookLineNo(-1);
        printToBuffer("<" + SyntaxType.FuncFParams + ">");
        return new FuncFParams(children, startLine, endLine);
    }

    public Node parseFuncFParam() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);
        children.add(parseBType());
        children.add(new Terminator(tokenStream.read()));
        if (tokenStream.lookType(0) == TokenType.LBRACK) {
            children.add(new Terminator(tokenStream.read())); // '['
            // parse ]
            checkErrorK(children);
        }
        endLine = tokenStream.lookLineNo(-1);
        printToBuffer("<" + SyntaxType.FuncFParam + ">");
        return new FuncFParam(children, startLine, endLine);
    }

    public Node parseBlock() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);
        children.add(new Terminator(tokenStream.read())); // '{'
        while (tokenStream.lookType(0) != TokenType.RBRACE) {
            children.add(parseBlockItem());
        }
        children.add(new Terminator(tokenStream.read())); // '}'
        endLine = tokenStream.lookLineNo(-1);
        printToBuffer("<" + SyntaxType.Block + ">");
        return new Block(children, startLine, endLine);
    }

    public Node parseBlockItem() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);
        TokenType firstTokenType = tokenStream.lookType(0);
        if (firstTokenType == TokenType.CONSTTK ||
                firstTokenType == TokenType.INTTK ||
                firstTokenType == TokenType.CHARTK) {
            children.add(parseDecl());
        } else {
            children.add(parseStmt());
        }
        endLine = tokenStream.lookLineNo(-1);
        return new BlockItem(children, startLine, endLine);
    }

    public void checkErrorI(ArrayList<Node> children) {
        if (tokenStream.lookType(0) == TokenType.SEMICN) {
            // ;
            children.add(new Terminator(tokenStream.read()));
        } else {
            // error i
            errLine = getLastUnTermLineNo();
            Printer.addError(errLine, ErrorType.i);
        }
    }

    public void checkErrorJ(ArrayList<Node> children) {
        // parse )
        if (tokenStream.lookType(0) == TokenType.RPARENT) {
            children.add(new Terminator(tokenStream.read())); //)
        } else {
            // error j
            Printer.addError(getLastUnTermLineNo(), ErrorType.j);
        }
    }

    public void checkErrorK(ArrayList<Node> children) {
        // parse ]
        if (tokenStream.lookType(0) == TokenType.RBRACK) {
            children.add(new Terminator(tokenStream.read())); // ']'
        } else {
            // error k
            Printer.addError(getLastUnTermLineNo(), ErrorType.k);
        }
    }

    public Node parseStmt() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);
        TokenType firstTokenType = tokenStream.lookType(0);
        // ; 第二种Stmt（无Exp）
        if (firstTokenType == TokenType.SEMICN) {
            children.add(new Terminator(tokenStream.read()));
            endLine = tokenStream.lookLineNo(-1);
            printToBuffer("<" + SyntaxType.Stmt + ">");
            return new ExpStmt(children, startLine, endLine);
        }
        // Block
        else if (firstTokenType == TokenType.LBRACE) {
            children.add(parseBlock());
            endLine = tokenStream.lookLineNo(-1);
            printToBuffer("<" + SyntaxType.Stmt + ">");
            return new BlockStmt(children, startLine, endLine);
        }
        // if
        else if (firstTokenType == TokenType.IFTK) {
            children.add(new Terminator(tokenStream.read())); // 'if'
            children.add(new Terminator(tokenStream.read())); // '('
            children.add(parseCond());
            // parse )
            checkErrorJ(children);
            children.add(parseStmt());
            if (tokenStream.lookType(0) == TokenType.ELSETK) {
                children.add(new Terminator(tokenStream.read()));
                children.add(parseStmt());
            }
            endLine = tokenStream.lookLineNo(-1);
            printToBuffer("<" + SyntaxType.Stmt + ">");
            return new IfStmt(children, startLine, endLine);
        }
        // for
        else if (firstTokenType == TokenType.FORTK) {
            children.add(new Terminator(tokenStream.read())); // for
            children.add(new Terminator(tokenStream.read())); // (
            // ? ; : ForStmt
            if (tokenStream.lookType(0) != TokenType.SEMICN) {
                children.add(parseForStmt());
            }
            children.add(new Terminator(tokenStream.read())); // ;
            if (tokenStream.lookType(0) != TokenType.SEMICN) {
                children.add(parseCond());
            }
            children.add(new Terminator(tokenStream.read())); // ;
            if (tokenStream.lookType(0) != TokenType.RPARENT) {
                children.add(parseForStmt());
            }
            children.add(new Terminator(tokenStream.read())); // )
            children.add(parseStmt()); // Stmt

            endLine = tokenStream.lookLineNo(-1);
            printToBuffer("<" + SyntaxType.Stmt + ">");
            return new WholeForStmt(children, startLine, endLine);
        }
        // 'break' ';' | 'continue' ';'
        else if (firstTokenType == TokenType.BREAKTK || firstTokenType == TokenType.CONTINUETK) {
            children.add(new Terminator(tokenStream.read())); // break / continue
            checkErrorI(children);
            endLine = tokenStream.lookLineNo(-1);
            printToBuffer("<" + SyntaxType.Stmt + ">");
            if (((Terminator) children.get(0)).getTokenType() == TokenType.BREAKTK) {
                return new BreakStmt(children, startLine, endLine);
            } else {
                return new ContinueStmt(children, startLine, endLine);
            }
        }
        // return
        else if (firstTokenType == TokenType.RETURNTK) {
            children.add(new Terminator(tokenStream.read())); // return
            // [Exp] First=['(', 'Ident', 'IntConst', 'CharConst', '+', '-', '!']
            firstTokenType = tokenStream.lookType(0);
            if (firstTokenType == TokenType.LPARENT || firstTokenType == TokenType.IDENFR ||
                    firstTokenType == TokenType.INTCON || firstTokenType == TokenType.CHRCON ||
                    firstTokenType == TokenType.PLUS || firstTokenType == TokenType.MINU ||
                    firstTokenType == TokenType.NOT) {
                children.add(parseExp());
            }
            checkErrorI(children);
            endLine = tokenStream.lookLineNo(-1);
            printToBuffer("<" + SyntaxType.Stmt + ">");
            return new ReturnStmt(children, startLine, endLine);
        }
        // printf
        else if (firstTokenType == TokenType.PRINTFTK) {
            children.add(new Terminator(tokenStream.read())); // printf
            children.add(new Terminator(tokenStream.read())); // (
            children.add(new Terminator(tokenStream.read())); // STRCON
            while (tokenStream.lookType(0) == TokenType.COMMA) {
                children.add(new Terminator(tokenStream.read())); // ,
                children.add(parseExp());
            }
            // parse )
            checkErrorJ(children);
            // parse ;
            checkErrorI(children);
            endLine = tokenStream.lookLineNo(-1);
            printToBuffer("<" + SyntaxType.Stmt + ">");
            return new PrintfStmt(children, startLine, endLine);
        }
        // LVal=Exp; [Exp]; LVal=getint(); LVal=getchar();
        else if (firstTokenType == TokenType.IDENFR) {
            // Exp; 判断依据：Exp 的 First 集合之一：Ident '(' [FuncRParams] ')'
            if (tokenStream.lookType(1) == TokenType.LPARENT) {
                children.add(parseExp());
                // parse ;
                checkErrorI(children);
                printToBuffer("<" + SyntaxType.Stmt + ">");
                return new ExpStmt(children, startLine, endLine);
            }

            // exp也许不是最终答案，所以暂时不能加入树，后续需要再做决定
            shouldRecord = false;
            Node exp = parseExp(); // 把LVal也包裹进去了
            shouldRecord = true;

            if (tokenStream.lookType(0) == TokenType.ASSIGN) {
                // 如果之后读入了一个ASSIGN，代表着【第一个Node应为LVal】，应将其剥出来，且只需往buffer加LVal的记录
                // exp - AddExp - MulExp - UnaryExp - PrimaryExp - LVal
                int maxIndex = 0;
                for (int i = expBufferForSpecialStmt.size() - 1; i >= 0; i--) {
                    if (expBufferForSpecialStmt.get(i).equals("<" + SyntaxType.LVal + ">")) {
                        maxIndex = i;
                        break;
                    }
                }
                for (int i = 0; i <= maxIndex; i++) {
                    printToBuffer(expBufferForSpecialStmt.get(i));
                }
                expBufferForSpecialStmt.clear(); // 清空expbuffer

                children.add(exp.children.get(0).children.get(0).children.get(0).children.get(0).children.get(0)); // LVal
                children.add(new Terminator(tokenStream.read())); // =
                if (tokenStream.lookType(0) == TokenType.GETINTTK) {
                    children.add(new Terminator(tokenStream.read())); // getint
                    children.add(new Terminator(tokenStream.read())); // (
                    checkErrorJ(children); // )
                    checkErrorI(children);
                    printToBuffer("<" + SyntaxType.Stmt + ">");
                    return new GetIntStmt(children, startLine, endLine);
                } else if (tokenStream.lookType(0) == TokenType.GETCHARTK) {
                    children.add(new Terminator(tokenStream.read())); // getchar
                    children.add(new Terminator(tokenStream.read())); // (
                    checkErrorJ(children); // )
                    checkErrorI(children);
                    printToBuffer("<" + SyntaxType.Stmt + ">");
                    return new GetCharStmt(children, startLine, endLine);
                } else {
                    // LVal=Exp;
                    children.add(parseExp());
                    checkErrorI(children);
                    printToBuffer("<" + SyntaxType.Stmt + ">");
                    return new LValExpStmt(children, startLine, endLine);
                }
            } else {
                // 【第一个Node为Exp】。Exp; 判断依据：Exp的分支PrimaryExp的分支LVal
                // 需要往buffer里加所有被省略的记录
                for (String buffer : expBufferForSpecialStmt) {
                    printToBuffer(buffer);
                }
                expBufferForSpecialStmt.clear(); // 清空
                children.add(exp); // exp
                // parse ;
                checkErrorI(children);

                printToBuffer("<" + SyntaxType.Stmt + ">");
                return new ExpStmt(children, startLine, endLine);
            }
        }
        // Exp;
        else {
            children.add(parseExp());
            checkErrorI(children); // ;
            endLine = tokenStream.lookLineNo(-1);
            printToBuffer("<" + SyntaxType.Stmt + ">");
            return new ExpStmt(children, startLine, endLine);
        }
    }

    public Node parseForStmt() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);
        children.add(parseLVal());
        children.add(new Terminator(tokenStream.read())); // =
        children.add(parseExp());
        endLine = tokenStream.lookLineNo(-1);
        printToBuffer("<" + SyntaxType.ForStmt + ">");
        return new ForStmt(children, startLine, endLine);
    }

    public Node parseExp() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);
        children.add(parseAddExp());
        endLine = tokenStream.lookLineNo(-1);
        if (shouldRecord) printToBuffer("<" + SyntaxType.Exp + ">");
        else expBufferForSpecialStmt.add("<" + SyntaxType.Exp + ">");
        return new Exp(children, startLine, endLine);
    }

    public Node parseCond() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);
        children.add(parseLOrExp());
        endLine = tokenStream.lookLineNo(-1);
        if (shouldRecord) printToBuffer("<" + SyntaxType.Cond + ">");
        else expBufferForSpecialStmt.add("<" + SyntaxType.Cond + ">");
        return new Cond(children, startLine, endLine);
    }

    public Node parseLVal() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);


        children.add(new Terminator(tokenStream.read())); // Ident
        if (tokenStream.lookType(0) == TokenType.LBRACK) {
            children.add(new Terminator(tokenStream.read())); // [
            children.add(parseExp());
            // parse ]
            checkErrorK(children);
        }
        endLine = tokenStream.lookLineNo(-1);
        if (shouldRecord) printToBuffer("<" + SyntaxType.LVal + ">");
        else expBufferForSpecialStmt.add("<" + SyntaxType.LVal + ">");
        return new LVal(children, startLine, endLine);
    }

    public Node parsePrimaryExp() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);
        TokenType firstTokenType = tokenStream.lookType(0);
        // ( Exp )
        if (firstTokenType == TokenType.LPARENT) {
            children.add(new Terminator(tokenStream.read())); // (
            children.add(parseExp());
            // parse )
            checkErrorJ(children);
        }
        // Number
        else if (firstTokenType == TokenType.INTCON) {
            children.add(parseNumber());
        }
        // Character
        else if (firstTokenType == TokenType.CHRCON) {
            children.add(parseCharacter());
        }
        // LVal
        else {
            children.add(parseLVal());
        }
        endLine = tokenStream.lookLineNo(-1);
        if (shouldRecord) printToBuffer("<" + SyntaxType.PrimaryExp + ">");
        else expBufferForSpecialStmt.add("<" + SyntaxType.PrimaryExp + ">");
        return new PrimaryExp(children, startLine, endLine);
    }

    public Node parseNumber() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);
        children.add(new Terminator(tokenStream.read())); // IntConst
        endLine = tokenStream.lookLineNo(-1);
        if (shouldRecord) printToBuffer("<" + SyntaxType.Number + ">");
        else expBufferForSpecialStmt.add("<" + SyntaxType.Number + ">");
        return new Number(children, startLine, endLine);
    }

    public Node parseCharacter() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);
        children.add(new Terminator(tokenStream.read())); // CharConst
        endLine = tokenStream.lookLineNo(-1);
        if (shouldRecord) printToBuffer("<" + SyntaxType.Character + ">");
        else expBufferForSpecialStmt.add("<" + SyntaxType.Character + ">");
        return new Character(children, startLine, endLine);
    }

    public Node parseUnaryExp() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);
        TokenType firstTokenType = tokenStream.lookType(0);
        // Ident '(' [FuncRParams] ')'
        if (firstTokenType == TokenType.IDENFR && tokenStream.lookType(1) == TokenType.LPARENT) {
            children.add(new Terminator(tokenStream.read())); // Ident
            children.add(new Terminator(tokenStream.read())); // (
            // [FuncRParams] First=['(', 'Ident', 'IntConst', 'CharConst', '+', '-', '!']
            firstTokenType = tokenStream.lookType(0);
            if (firstTokenType == TokenType.LPARENT || firstTokenType == TokenType.IDENFR ||
                    firstTokenType == TokenType.INTCON || firstTokenType == TokenType.CHRCON ||
                    firstTokenType == TokenType.PLUS || firstTokenType == TokenType.MINU ||
                    firstTokenType == TokenType.NOT) {
                children.add(parseFuncRParams());
            }
            checkErrorJ(children);
        }
        // UnaryOp
        else if (firstTokenType == TokenType.PLUS || firstTokenType == TokenType.MINU || firstTokenType == TokenType.NOT) {
            children.add(parseUnaryOp());
            children.add(parseUnaryExp());
        }
        // PrimaryExp
        else {
            children.add(parsePrimaryExp());
        }
        endLine = tokenStream.lookLineNo(-1);
        if (shouldRecord) printToBuffer("<" + SyntaxType.UnaryExp + ">");
        else expBufferForSpecialStmt.add("<" + SyntaxType.UnaryExp + ">");
        return new UnaryExp(children, startLine, endLine);
    }

    public Node parseUnaryOp() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);
        children.add(new Terminator(tokenStream.read())); // '+' | '−' | '!'
        endLine = tokenStream.lookLineNo(-1);
        if (shouldRecord) printToBuffer("<" + SyntaxType.UnaryOp + ">");
        else expBufferForSpecialStmt.add("<" + SyntaxType.UnaryOp + ">");
        return new UnaryOp(children, startLine, endLine);
    }

    public Node parseFuncRParams() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);

        children.add(parseExp());
        while (tokenStream.lookType(0) == TokenType.COMMA) {
            children.add(new Terminator(tokenStream.read())); // ,
            children.add(parseExp());
        }
        endLine = tokenStream.lookLineNo(-1);
        if (shouldRecord) printToBuffer("<" + SyntaxType.FuncRParams + ">");
        else expBufferForSpecialStmt.add("<" + SyntaxType.FuncRParams + ">");
        return new FuncRParams(children, startLine, endLine);
    }

    public Node parseMulExp() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);

        children.add(parseUnaryExp());
        while (tokenStream.lookType(0) == TokenType.MULT ||
                tokenStream.lookType(0) == TokenType.DIV ||
                tokenStream.lookType(0) == TokenType.MOD) {
            if (shouldRecord) printToBuffer("<" + SyntaxType.MulExp + ">");
            else expBufferForSpecialStmt.add("<" + SyntaxType.MulExp + ">");
            children.add(new Terminator(tokenStream.read()));
            children.add(parseUnaryExp());
        }
        endLine = tokenStream.lookLineNo(-1);
        if (shouldRecord) printToBuffer("<" + SyntaxType.MulExp + ">");
        else expBufferForSpecialStmt.add("<" + SyntaxType.MulExp + ">");
        return new MulExp(children, startLine, endLine);
    }

    public Node parseAddExp() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);

        children.add(parseMulExp());
        while (tokenStream.lookType(0) == TokenType.PLUS ||
                tokenStream.lookType(0) == TokenType.MINU) {
            if (shouldRecord) printToBuffer("<" + SyntaxType.AddExp + ">");
            else expBufferForSpecialStmt.add("<" + SyntaxType.AddExp + ">");
            children.add(new Terminator(tokenStream.read()));
            children.add(parseMulExp());
        }
        endLine = tokenStream.lookLineNo(-1);
        if (shouldRecord) printToBuffer("<" + SyntaxType.AddExp + ">");
        else expBufferForSpecialStmt.add("<" + SyntaxType.AddExp + ">");
        return new AddExp(children, startLine, endLine);
    }

    public Node parseRelExp() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);

        children.add(parseAddExp());
        while (tokenStream.lookType(0) == TokenType.LEQ ||
                tokenStream.lookType(0) == TokenType.LSS ||
                tokenStream.lookType(0) == TokenType.GEQ ||
                tokenStream.lookType(0) == TokenType.GRE) {
            if (shouldRecord) printToBuffer("<" + SyntaxType.RelExp + ">");
            else expBufferForSpecialStmt.add("<" + SyntaxType.RelExp + ">");
            children.add(new Terminator(tokenStream.read()));
            children.add(parseAddExp());
        }
        endLine = tokenStream.lookLineNo(-1);
        if (shouldRecord) printToBuffer("<" + SyntaxType.RelExp + ">");
        else expBufferForSpecialStmt.add("<" + SyntaxType.RelExp + ">");
        return new RelExp(children, startLine, endLine);
    }

    public Node parseEqExp() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);

        children.add(parseRelExp());
        while (tokenStream.lookType(0) == TokenType.EQL ||
                tokenStream.lookType(0) == TokenType.NEQ) {
            if (shouldRecord) printToBuffer("<" + SyntaxType.EqExp + ">");
            else expBufferForSpecialStmt.add("<" + SyntaxType.EqExp + ">");
            children.add(new Terminator(tokenStream.read()));
            children.add(parseRelExp());
        }
        endLine = tokenStream.lookLineNo(-1);
        if (shouldRecord) printToBuffer("<" + SyntaxType.EqExp + ">");
        else expBufferForSpecialStmt.add("<" + SyntaxType.EqExp + ">");
        return new EqExp(children, startLine, endLine);
    }

    public Node parseLAndExp() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);

        children.add(parseEqExp());
        while (tokenStream.lookType(0) == TokenType.AND) {
            if (shouldRecord) printToBuffer("<" + SyntaxType.LAndExp + ">");
            else expBufferForSpecialStmt.add("<" + SyntaxType.LAndExp + ">");
            children.add(new Terminator(tokenStream.read()));
            children.add(parseEqExp());
        }
        endLine = tokenStream.lookLineNo(-1);
        if (shouldRecord) printToBuffer("<" + SyntaxType.LAndExp + ">");
        else expBufferForSpecialStmt.add("<" + SyntaxType.LAndExp + ">");
        return new LAndExp(children, startLine, endLine);
    }

    public Node parseLOrExp() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);

        children.add(parseLAndExp());
        while (tokenStream.lookType(0) == TokenType.OR) {
            if (shouldRecord) printToBuffer("<" + SyntaxType.LOrExp + ">");
            else expBufferForSpecialStmt.add("<" + SyntaxType.LOrExp + ">");
            children.add(new Terminator(tokenStream.read()));
            children.add(parseLAndExp());
        }
        endLine = tokenStream.lookLineNo(-1);
        if (shouldRecord) printToBuffer("<" + SyntaxType.LOrExp + ">");
        else expBufferForSpecialStmt.add("<" + SyntaxType.LOrExp + ">");
        return new LOrExp(children, startLine, endLine);
    }

    public Node parseConstExp() {
        ArrayList<Node> children = new ArrayList<>();
        int startLine = tokenStream.lookLineNo(0);

        children.add(parseAddExp());
        endLine = tokenStream.lookLineNo(-1);
        if (shouldRecord) printToBuffer("<" + SyntaxType.ConstExp + ">");
        else expBufferForSpecialStmt.add("<" + SyntaxType.ConstExp + ">");
        return new ConstExp(children, startLine, endLine);
    }

    public static void printToBuffer(String content) {
        outputBuffer.add(content);
    }

    public static ArrayList<String> getOutputBuffer() {
        return outputBuffer;
    }

    public static void addASTNodes(Node node) {
        astNodes.add(node);
    }

    public int getLastUnTermLineNo() {
        return astNodes.get(astNodes.size() - 1).getEndLine();
    }

    // DEBUG
    public static void printBuffer() {
        outputBuffer.forEach(System.out::println);
    }

}
