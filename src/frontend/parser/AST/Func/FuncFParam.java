package frontend.parser.AST.Func;

import frontend.parser.AST.Leaf.Terminator;
import frontend.parser.AST.Node;
import frontend.symbol.SymbolManager;
import frontend.symbol.symbols.Array.Array;
import frontend.symbol.symbols.Var.Char;
import frontend.symbol.symbols.Array.CharArray;
import frontend.symbol.symbols.Var.Int;
import frontend.symbol.symbols.Array.IntArray;
import frontend.symbol.symbols.Symbol;
import llvm_ir.IRBuilder;
import llvm_ir.Value;
import llvm_ir.component.Instruction;
import llvm_ir.component.type.IntegerType;
import llvm_ir.component.function.Param;
import llvm_ir.component.type.LLVMType;
import llvm_ir.component.type.PointerType;
import utils.SyntaxType;

import java.util.ArrayList;

import static llvm_ir.component.type.IntegerType.CHAR;
import static llvm_ir.component.type.IntegerType.INT_32;

public class FuncFParam extends Node {
    private Symbol symbol;

    public FuncFParam(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.FuncFParam, children, startLine, endLine);
    }

    // FuncFParam → BType Ident ['[' ']'] // b
    public void createSymbol() {
        String ident = ((Terminator) children.get(1)).getContent();
        if (children.size() > 2) {
            // BfTypeArray
            switch (((Terminator) (children.get(0).children.get(0))).getTokenType()) {
                case INTTK -> {
                    symbol = new IntArray(ident);
                }
                case CHARTK -> {
                    symbol = new CharArray(ident);
                }
            }
        } else {
            // BfType
            switch (((Terminator) (children.get(0).children.get(0))).getTokenType()) {
                case INTTK -> {
                    symbol = new Int(ident);
                }
                case CHARTK -> {
                    symbol = new Char(ident);
                }
            }
        }
        // 设置全局属性
        symbol.setGlobal(false);
    }

    @Override
    public void analyseSemantic() {
        createSymbol();
        /* check error */
        // b: 名字重定义 Param存在Func-Symbol，留给Block再注册
        SymbolManager.lastFunc.addParamAndLineNo(symbol, children.get(0).getEndLine());
        super.analyseSemantic();
    }

    @Override
    public Value genIR() {
        // gen IR
        // 设置LLVMType
        // 声明数组为函数参数的时候，数组会退化为指针
        LLVMType type = (symbol.isInt() ?
                (symbol instanceof Array ? new PointerType(IntegerType.INT_32) : INT_32) :
                (symbol instanceof Array ? new PointerType(IntegerType.CHAR) : CHAR));
        // 创建参数
        Param param = IRBuilder.genParam(type);
        // 分配内存和保存值
        // 如果参数是数组类型，则直接将param作为symbol的llvm值，不需额外存储
        if (symbol instanceof Array) {
            symbol.setLLVMValue(param);
        }
        // 如果参数是整数类型或char类型，先创建一个指针类型的指令（Alloca），为了避免直接对形参的值进行修改
        else {
            Instruction allocaInstr = IRBuilder.genNInsAllocaInstr(param.getResultType());
            symbol.setLLVMValue(allocaInstr);
            IRBuilder.genNInsStoreInstr(param, allocaInstr);
        }

        super.genIR();
        return null;
    }
}
