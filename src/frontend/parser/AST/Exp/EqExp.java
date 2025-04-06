package frontend.parser.AST.Exp;

import frontend.parser.AST.Leaf.Terminator;
import frontend.parser.AST.Node;
import llvm_ir.IRBuilder;
import llvm_ir.Value;
import llvm_ir.component.Constant;
import llvm_ir.component.type.ConstantType;
import llvm_ir.component.type.IntegerType;
import llvm_ir.component.type.LLVMType;
import llvm_ir.instr.IcmpInstr;
import utils.SyntaxType;
import utils.TokenType;

import java.util.ArrayList;

import static llvm_ir.component.type.IntegerType.INT_32;

public class EqExp extends Node {
    public EqExp(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.EqExp, children, startLine, endLine);
    }

    // EqExp → RelExp | EqExp ('==' | '!=') RelExp

    @Override
    public Value genIR() {
        Value op1 = children.get(0).genIR();
        Value op2;
        if (children.size() == 1) {
            // 如果op1不是i1类型的，则将其与0比较
            LLVMType op1Type = op1.getResultType();
            if (!(op1Type instanceof IntegerType && ((IntegerType) op1Type).bitWidth == 1)) {
                op2 = new Constant(0);
                ((ConstantType) op2.getResultType()).setAssignType(op1Type.isInt32() ? INT_32 : IntegerType.CHAR);
                op1 = IRBuilder.genNInsIcmpInstr(IcmpInstr.Op.ne, op1, op2);
            }
            return op1;
        }

        TokenType tokenType;
        for (int i = 1; i < children.size(); i++) {
            // 当前节点为==或!=
            if ((i & 1) == 1) {
                // 记录符号
                tokenType = ((Terminator) children.get(i)).getTokenType();
                // 若op1类型不是i32，则转化为i32
                if (!op1.getResultType().isInt32()) {
                    op1 = IRBuilder.genNInsZExtOrTruncInstr(op1.getResultType(), op1, INT_32, true);
                }
                // 获得下一个RelExp的IR，同样转化为i32
                op2 = children.get(++i).genIR();
                if (!op2.getResultType().isInt32()) {
                    op2 = IRBuilder.genNInsZExtOrTruncInstr(op2.getResultType(), op2, INT_32, true);
                }
                // 根据符号 比较op1和op2
                if (tokenType == TokenType.EQL) {
                    op1 = IRBuilder.genNInsIcmpInstr(IcmpInstr.Op.eq, op1, op2);
                } else {
                    op1 = IRBuilder.genNInsIcmpInstr(IcmpInstr.Op.ne, op1, op2);
                }
            }
        }
        return op1;
    }
}
