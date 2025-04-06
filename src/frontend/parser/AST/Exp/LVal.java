package frontend.parser.AST.Exp;

import frontend.parser.AST.Leaf.Terminator;
import frontend.parser.AST.Node;
import frontend.symbol.SymbolManager;
import frontend.symbol.symbols.Array.Array;
import frontend.symbol.symbols.Symbol;
import llvm_ir.Error;
import llvm_ir.IRBuilder;
import llvm_ir.Value;
import llvm_ir.component.Constant;
import llvm_ir.component.Instruction;
import utils.ErrorType;
import utils.ParamType;
import utils.Printer;
import utils.SyntaxType;

import java.util.ArrayList;

public class LVal extends Node {
    private Symbol symbol = null;

    public LVal(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.LVal, children, startLine, endLine);
    }

    public int compute() {
        if (symbol == null) {
            return 0;
        }

        int pos = 0; // default: var 0
        if (children.size() > 1) {
            pos = ((Exp) children.get(2)).compute();
        }

        return symbol.getInitialValue(pos);
    }

    /* LVal → Ident ['[' Exp ']'] // c k */

    @Override
    public void analyseSemantic() {
        /* check error */
        // c:未定义的名字
        this.symbol = SymbolManager.getSymbolByNameInGlobalStack(((Terminator) children.get(0)).getContent());
        if (symbol == null) {
            Printer.addError(children.get(0).getEndLine(), ErrorType.c);
        }

        super.analyseSemantic();
    }

    /**
     * 为 LVal 节点生成中间代码。
     * 根据需求生成值（取值）或地址（赋值）。
     *
     * @param forAssign 是否用于赋值（true 表示生成地址，false 表示生成值）
     * @return Value 地址或取值结果
     */
    public Value genIR(boolean forAssign) {
        if (symbol == null) return new Error();

        // gen IR
        Instruction instr;

        // **数组处理**
        // 当 LVal 表示数组时,方括号个数必须和数组变量的维数相同(即定位到元素)
        if (symbol instanceof Array) {
            // 传地址
            if (children.size() <= 2) {
                instr = IRBuilder.genNInsGEPInstr(symbol.getLLVMValue(), new Constant(0));
                return instr;
            }
            // 传值
            Value pos = children.get(2).genIR();
            instr = IRBuilder.genNInsGEPInstr(symbol.getLLVMValue(), pos);
            if (forAssign) {
                return instr;
            } else {
                return IRBuilder.genNInsLoadInstr(instr);
            }
        }

        // **标量处理**
        // 当 LVal 表示单个变量时,不能出现后面的方括号
        else {
            if (forAssign) {
                return symbol.getLLVMValue(); // 返回地址
            } else {
                return IRBuilder.genNInsLoadInstr(symbol.getLLVMValue()); // 加载值
            }
        }
    }

    @Override
    public ParamType getTypeAsParam() {
        if (children.size() == 1) {
            Symbol searchRes = SymbolManager.getSymbolByNameInGlobalStack(((Terminator) children.get(0)).getContent());
            if (searchRes == null) {
                // 搜不到，说明已经出错ERROR C，故这行无需继续报错，报一个VAR假装无事发生。
                return ParamType.VAR;
            } else if (searchRes instanceof Array) {
                return ((Array) searchRes).getParamType();
            } else {
                return ParamType.VAR;
            }
        }
        return ParamType.VAR;
    }
}
