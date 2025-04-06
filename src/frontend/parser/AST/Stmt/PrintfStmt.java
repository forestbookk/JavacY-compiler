package frontend.parser.AST.Stmt;

import frontend.parser.AST.Exp.Exp;
import frontend.parser.AST.Leaf.Terminator;
import frontend.parser.AST.Node;
import llvm_ir.IRBuilder;
import llvm_ir.Value;
import llvm_ir.component.StringLiteral;
import llvm_ir.component.type.IntegerType;
import utils.ErrorType;
import utils.Printer;
import utils.SyntaxType;

import java.util.ArrayList;

public class PrintfStmt extends Node {
    ArrayList<Character> formatChars = new ArrayList<>(); // d: %d, c:%c
    ArrayList<String> allSigns = new ArrayList<>();
    ArrayList<Exp> expNodeList = new ArrayList<>();

    public PrintfStmt(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.Stmt, children, startLine, endLine);
    }

    /* 'printf''('StringConst {','Exp}')'';' // i j l */
    @Override
    public void analyseSemantic() {
        /* check error */
        // l: printf中格式字符与表达式个数不匹配 // 格式字符只有 %d %c
        int expCnt = 0;
        for (int i = 3; i < children.size(); i++) {
            if (children.get(i) instanceof Exp) {
                expCnt++;
                expNodeList.add((Exp) children.get(i));
            }
        }

        boolean flagOfPercent = false;

        ArrayList<Integer> asciiList = ((Terminator) children.get(2)).getToken().getAsciiList();
        for (Integer ch : asciiList) {
            if (ch == '%') {
                flagOfPercent = true;
            } else if (flagOfPercent) {
                switch (ch) {
                    case (int) 'd' -> {
                        formatChars.add('d');
                        allSigns.add("%d");
                    }
                    case (int) 'c' -> {
                        formatChars.add('c');
                        allSigns.add("%c");
                    }
                    default -> {
                        allSigns.add("%");
                        allSigns.add(String.valueOf((char) ((int) ch)));
                    }
                }
                flagOfPercent = false;
            } else {
                allSigns.add(String.valueOf((char) ((int) ch)));
            }
        }

        if (formatChars.size() != expCnt) {
            Printer.addError(children.get(0).getEndLine(), ErrorType.l);
        }

        super.analyseSemantic();
    }

    @Override
    public Value genIR() {
        StringBuilder sb = new StringBuilder();

        ArrayList<Value> expValueList = new ArrayList<>();
        for (Node exp : expNodeList) {
            expValueList.add(exp.genIR());
        }

        int cnt = 0;
        for (String s : allSigns) {
            if (s.charAt(0) == '%' && s.length() > 1) {
                // 如果sb非空，先将其生成IR并输出
                if (!sb.isEmpty()) {
                    StringLiteral sl = IRBuilder.genNInsStringLiteral(sb.toString());
                    IRBuilder.genNInsPutStr(sl);
                    sb.setLength(0);
                }
                // %d
                if (s.charAt(1) == 'd') {
                    // 由于putint的参数是i32类型的，所以如果参数是i8，需要进行拓展
                    Value expValue = expValueList.get(cnt);
                    if (!expValue.getResultType().isInt32()) {
                        expValue = IRBuilder.genNInsZExtOrTruncInstr(IntegerType.CHAR, expValue, IntegerType.INT_32, true);
                    }
                    IRBuilder.genNInsPutIntOrChar(expValue, true);
                    cnt++;
                }
                // %c
                else {
                    // 由于putchar的参数是i32类型的，所以如果参数是i8，需要进行拓展
                    Value expValue = expValueList.get(cnt);
                    if (!expValue.getResultType().isInt32()) {
                        expValue = IRBuilder.genNInsZExtOrTruncInstr(IntegerType.CHAR, expValue, IntegerType.INT_32, true);
                    }
                    IRBuilder.genNInsPutIntOrChar(expValue, false);
                    cnt++;
                }
            } else {
                sb.append(s);
            }
        }
        if (!sb.isEmpty()) {
            StringLiteral sl = IRBuilder.genNInsStringLiteral(sb.toString());
            IRBuilder.genNInsPutStr(sl);
            sb.setLength(0);
        }
        return null;
    }
}
