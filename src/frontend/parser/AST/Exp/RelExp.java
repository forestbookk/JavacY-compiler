package frontend.parser.AST.Exp;

import frontend.parser.AST.Leaf.Terminator;
import frontend.parser.AST.Node;
import llvm_ir.IRBuilder;
import llvm_ir.Value;
import llvm_ir.instr.IcmpInstr;
import utils.SyntaxType;
import utils.TokenType;

import java.util.ArrayList;

import static llvm_ir.component.type.IntegerType.INT_32;

public class RelExp extends Node {
    public RelExp(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.RelExp, children, startLine, endLine);
    }

    // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp

    @Override
    public Value genIR() {
        Value op1 = children.get(0).genIR(); // 类型可能是i32，或i1
        if (children.size() == 1) {
            return op1;
        }

        Value op2;
        TokenType tokenType;
        for (int i = 1; i < children.size(); i++) {
            // 当前节点为< > <= >=
            if ((i & 1) == 1) {
                // 记录符号
                tokenType = ((Terminator) children.get(i)).getTokenType();
                // 若op1类型不是i32，则转化为i32
                if (!op1.getResultType().isInt32()) {
                    op1 = IRBuilder.genNInsZExtOrTruncInstr(op1.getResultType(), op1, INT_32,true);
                }
                // 获得下一个AddExp的IR，同样转化为i32
                op2 = children.get(++i).genIR();
                if (!op2.getResultType().isInt32()) {
                    op2 = IRBuilder.genNInsZExtOrTruncInstr(op2.getResultType(), op2, INT_32,true);
                }
                // 根据符号 比较op1和op2
                switch (tokenType) {
                    case LSS -> {
                        op1 = IRBuilder.genNInsIcmpInstr(IcmpInstr.Op.slt, op1, op2);
                    }
                    case LEQ -> {
                        op1 = IRBuilder.genNInsIcmpInstr(IcmpInstr.Op.sle, op1, op2);
                    }
                    case GRE -> {
                        op1 = IRBuilder.genNInsIcmpInstr(IcmpInstr.Op.sgt, op1, op2);
                    }
                    case GEQ -> {
                        op1 = IRBuilder.genNInsIcmpInstr(IcmpInstr.Op.sge, op1, op2);
                    }
                }
            }
        }
        return op1;
    }
}
