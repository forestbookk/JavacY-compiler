package llvm_ir;

import frontend.symbol.SymbolManager;
import frontend.symbol.symbols.Symbol;
import llvm_ir.component.BasicBlock;
import llvm_ir.component.GlobalVarDef;
import llvm_ir.component.Initial;
import llvm_ir.component.Loop;
import llvm_ir.component.StringLiteral;
import llvm_ir.component.type.IntegerType;
import llvm_ir.component.type.PointerType;
import llvm_ir.component.function.Function;
import llvm_ir.component.Instruction;
import llvm_ir.component.Module;
import llvm_ir.component.function.Param;
import llvm_ir.component.type.LLVMType;
import llvm_ir.instr.AllocaInstr;
import llvm_ir.instr.BrInstr;
import llvm_ir.instr.CallInstr;
import llvm_ir.instr.CondBrInstr;
import llvm_ir.instr.GEPInstr;
import llvm_ir.instr.IcmpInstr;
import llvm_ir.instr.LoadInstr;
import llvm_ir.instr.RetInstr;
import llvm_ir.instr.StoreInstr;
import llvm_ir.instr.TruncInstr;
import llvm_ir.instr.ZExtInstr;
import llvm_ir.instr.AluInstr;
import llvm_ir.instr.io.GetChar;
import llvm_ir.instr.io.GetInt;
import llvm_ir.instr.io.PutCh;
import llvm_ir.instr.io.PutInt;
import llvm_ir.instr.io.PutStr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class IRBuilder {
    public static boolean OPTIMIZE_MODE = false;
    private static Module module = new Module();
    private static Stack<Loop> loopStack = new Stack<>();
    private static Function curFunction;
    private static BasicBlock curBasicBlock;
    protected static String GLOBAL_VAR_PREFIX = "@global_";
    protected static int GLOBAL_CNT = 0;
    protected static String LOCAL_VAR_PREFIX = "%v";
    private static HashMap<Value, Integer> LOCAL_VAR_CNT_MAP = new HashMap<>();
    protected static String PARAM_VAR_PREFIX = "%a";
    protected static int PARAM_CNT = 0;
    protected static String BB_PREFIX = "bb_";
    protected static int BB_CNT = 0;
    protected static String FUNC_PREFIX = "@func_";
    protected static String STRING_LITERAL_PREFIX = "@str_";
    protected static int STRING_LITERAL_CNT = 0;

    private IRBuilder() {
    }

    public static Module getModule() {
        return module;
    }

    public static void genNInsLoop(BasicBlock condBB, BasicBlock bodyBB,
                                   BasicBlock updateBB, BasicBlock endBB) {
        Loop l = new Loop(condBB, bodyBB, updateBB, endBB);
        loopStack.push(l);
    }

    public static Loop getCurLoop() {
        return loopStack.peek();
    }

    public static Loop exitCurLoop() {
        return loopStack.pop();
    }

    public static Function genNInsFunction(String ident, LLVMType retType) {
        String name = (ident.equals("main")) ? "@" + ident : FUNC_PREFIX + ident;
        Function f = new Function(name, retType);
        curFunction = f;
        module.addFunction(f);
        LOCAL_VAR_CNT_MAP.put(curFunction, 0);
        return f;
    }

    public static BasicBlock genNInsBasicBlock() {
        BasicBlock bb = new BasicBlock(genBBName());
        curBasicBlock = bb;
        curFunction.addBB(bb);
        bb.setParentFunction(curFunction);
        return bb;
    }

    public static void connectBasicBlockWithIRBuilder(BasicBlock bb) {
        curBasicBlock = bb;
        curFunction.addBB(bb);
        bb.setParentFunction(curFunction);
    }

    public static Param genParam(LLVMType type) {
        String name = PARAM_VAR_PREFIX + PARAM_CNT;
        PARAM_CNT++;
        Param param = new Param(name, type);
        curFunction.addParam(param);
        return param;
    }

    public static void insInstr(Instruction i) {
        curBasicBlock.addInstr(i);
        i.setParentBB(curBasicBlock);
    }

    public static Instruction genNInsAllocaInstr(LLVMType type) {
        Instruction i = new AllocaInstr(genLocalVarName(), type);
        insInstr(i);
        return i;
    }

    public static Instruction genNInsStoreInstr(Value src, Value dest) {
        Instruction i = new StoreInstr(src, dest);
        insInstr(i);
        return i;
    }

    public static Instruction genNInsGEPInstr(Value pointer, Value offset) {
        LLVMType type = pointer.type.isInt32() ? new PointerType(IntegerType.INT_32) : new PointerType(IntegerType.CHAR);
        Instruction i = new GEPInstr(genLocalVarName(), type, pointer, offset);
        insInstr(i);
        return i;
    }

    public static GlobalVarDef genNInsGlobalVarDef(LLVMType type, Initial initial, boolean isConstant) {
        String name = GLOBAL_VAR_PREFIX + GLOBAL_CNT;
        GLOBAL_CNT++;
        GlobalVarDef g = new GlobalVarDef(name, type, initial, isConstant);
        module.addGlobalVarDef(g);
        return g;
    }

    public static AluInstr genNInsAluInstr(LLVMType type, AluInstr.AluOp aluOp, Value op1, Value op2) {
        AluInstr i = new AluInstr(genLocalVarName(), type, aluOp, op1, op2);
        insInstr(i);
        return i;
    }

    public static CallInstr genNInsCallInstr(String functionName, ArrayList<Value> paramList, ArrayList<LLVMType> paramTypeList) {
        Symbol funcSymbol = SymbolManager.getSymbolByNameInGlobalStack(functionName);
        Function function = (Function) funcSymbol.getLLVMValue();
        String name = (function.isTypeVoid()) ? null : genLocalVarName();
        CallInstr i = new CallInstr(name, function, paramList, paramTypeList);
        insInstr(i);
        return i;
    }

    public static LoadInstr genNInsLoadInstr(Value pointer) {
        LoadInstr i = new LoadInstr(genLocalVarName(), pointer);
        insInstr(i);
        return i;
    }

    public static RetInstr genNInsRetInstr(Value returnValue) {
        RetInstr i = new RetInstr(curFunction.type, returnValue);
        insInstr(i);
        return i;
    }

    public static BrInstr genNInsBrInstr(BasicBlock targetBB) {
        BrInstr i = new BrInstr(genLocalVarName(), targetBB);
        insInstr(i);
        return i;
    }

    public static CondBrInstr genNInsCondBrInstr(Value cond, BasicBlock trueLabel, BasicBlock falseLabel) {
        CondBrInstr i = new CondBrInstr(genLocalVarName(), cond, trueLabel, falseLabel);
        insInstr(i);
        return i;
    }

    public static IcmpInstr genNInsIcmpInstr(IcmpInstr.Op predicate, Value op1, Value op2) {
        IcmpInstr i = new IcmpInstr(genLocalVarName(), predicate, op1, op2);
        insInstr(i);
        return i;
    }

    public static Instruction genNInsZExtOrTruncInstr(LLVMType fromType, Value op, LLVMType toType, boolean isZExt) {
        Instruction i = isZExt ? new ZExtInstr(genLocalVarName(), fromType, op, toType) : new TruncInstr(genLocalVarName(), fromType, op, toType);
        insInstr(i);
        return i;
    }

    public static StringLiteral genNInsStringLiteral(String str) {
        String name = STRING_LITERAL_PREFIX + STRING_LITERAL_CNT;
        STRING_LITERAL_CNT++;
        StringLiteral s = new StringLiteral(name, str);
        module.addStringLiteral(s);
        return s;
    }

    public static PutStr genNInsPutStr(StringLiteral sl) {
        PutStr i = new PutStr(genLocalVarName(), sl);
        insInstr(i);
        return i;
    }

    public static Instruction genNInsPutIntOrChar(Value value, boolean isInt) {
        Instruction i = isInt ? new PutInt(genLocalVarName(), value) : new PutCh(genLocalVarName(), value);
        insInstr(i);
        return i;
    }

    public static Instruction genNINsGetIntOrChar(boolean isInt) {
        Instruction i = isInt ? new GetInt(genLocalVarName()) : new GetChar(genParamName());
        insInstr(i);
        return i;
    }

    public static void addParamToLastFunction(Param p) {
        llvm_ir.IRBuilder.curFunction.addParam(p);
    }

    public static String genFunctionName(String name) {
        if (name.equals("main")) {
            return "@" + name;
        } else {
            return FUNC_PREFIX + name;
        }
    }

    public static String genParamName() {
        String name = PARAM_VAR_PREFIX + PARAM_CNT;
        PARAM_CNT++;
        return name;
    }

    public static String genBBName() {
        String name = BB_PREFIX + BB_CNT;
        BB_CNT++;
        return name;
    }

    public static String genLocalVarName() {
        int cur_function_cnt = LOCAL_VAR_CNT_MAP.get(curFunction);
        String name = LOCAL_VAR_PREFIX + cur_function_cnt;
        LOCAL_VAR_CNT_MAP.put(curFunction, cur_function_cnt + 1);
        return name;
    }

    public static String genLocalVarNameInSpecificFunc(Function f) {
        int cur_function_cnt = LOCAL_VAR_CNT_MAP.get(f);
        String name = LOCAL_VAR_PREFIX + cur_function_cnt;
        LOCAL_VAR_CNT_MAP.put(curFunction, cur_function_cnt + 1);
        return name;
    }

    public static BasicBlock getCurBasicBlock() {
        return curBasicBlock;
    }

    public static Function getCurFunction() {
        return curFunction;
    }

}
