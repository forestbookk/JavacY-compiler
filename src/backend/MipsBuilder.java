package backend;

import backend.component.Comment;
import backend.component.Global;
import backend.component.MipsBasicBlock;
import backend.component.MipsFunction;
import backend.component.MipsModule;
import backend.instruction.NopMi;
import backend.instruction.SyscallMi;
import backend.instruction.i_instr.AluImi;
import backend.instruction.i_instr.BranchImi;
import backend.instruction.i_instr.LaImi;
import backend.instruction.i_instr.LiImi;
import backend.instruction.i_instr.MemImi;
import backend.instruction.i_instr.SltiImi;
import backend.instruction.j_instr.JumpJmi;
import backend.instruction.r_instr.AluRmi;
import backend.instruction.r_instr.CmpRmi;
import backend.instruction.r_instr.HiLoRmi;
import backend.instruction.r_instr.JrRmi;
import backend.instruction.r_instr.MoveRmi;
import backend.instruction.r_instr.MuDiRmi;
import frontend.symbol.symbols.Func.Func;
import llvm_ir.Value;
import llvm_ir.component.function.Function;

import java.util.ArrayList;
import java.util.HashMap;

public class MipsBuilder {
    public static StackManager stackManager = new StackManager(); // 管理栈偏移
    public static RegisterManager registerManager = new RegisterManager(); // 管理寄存器分配
    private static MipsModule module = new MipsModule(); // 汇编代码容器

    private MipsBuilder() {
    }

    public static void init() {
        genNInsComment("jump to main function");
        genNInsJumpJmi(JumpJmi.Op.jal, "main");
        //genNInsNopMi();
        genNInsJumpJmi(JumpJmi.Op.j, "end");
    }

    public static void close() {
        genNInsBasicBlock("end");
    }

    public static void truncToI8(Register rs, Register rd) {
        AluImi i = new AluImi(AluImi.Op.andi, rs, rd, 0xff);
        insMipsNode(i);
    }


    // 进入新的函数，初始化必要信息
    public static void enterFunction(Function curFunc) {
        stackManager.init(); // 栈初始化
        registerManager.setValToReg(curFunc.getVal2reg());
        System.out.println("curFunc valtoreg size: " + curFunc.getVal2reg().size());
    }

    public static Register getRegisterForValue(Value value) {
        return registerManager.getRegisterForValue(value);
    }

    public static Register allocateOrLoadRegForValue(Value value, Register register, boolean isValueInt) {
        Register allocatedRegForValue = MipsBuilder.getRegisterForValue(value);
        // value之前已经有分配的寄存器
        if (allocatedRegForValue != null) {
            register = allocatedRegForValue;
        }
        // value之前没有被分配的寄存器 | 无优化
        else {
            loadFromStack(register, value, isValueInt);
        }
        return register;
    }

    public static void loadFromStack(Register reg, Value pointer, boolean isWord) {
        // 如果是栈中的局部变量或其他指针
        Integer stackOffsetOfPointer = MipsBuilder.getStackManager().getStackOffset(pointer);
        if (stackOffsetOfPointer == null) {
            // 如果栈中没有找到对应的偏移量，分配新的栈空间
            MipsBuilder.getStackManager().decrementOffset(4);
            MipsBuilder.getStackManager().addOffsetForValue(pointer);
            stackOffsetOfPointer = MipsBuilder.getStackManager().getCurOffset();
        }
        // 使用 load 指令从栈中加载数据
        MipsBuilder.genNInsMemImi(MemImi.Op.lw, Register.SP, reg, stackOffsetOfPointer);
    }

    public static void storeRegValueToStack(Value value, Register reg, boolean isWord) {
        if (!isWord) {
            truncToI8(reg, reg);
        }
        stackManager.decrementOffset(4);
        stackManager.addOffsetForValue(value);
        genNInsMemImi(MemImi.Op.sw,
                Register.SP, reg, stackManager.getCurOffset());
    }


    public static void insMipsNode(MipsNode mn) {
        if (mn instanceof Global) {
            module.addIntoDataSeg(mn);
        } else {
            module.addIntoTextSegment(mn);
        }
    }

    public static NopMi genNInsNopMi() {
        NopMi nopMi = new NopMi();
        insMipsNode(nopMi);
        return nopMi;
    }

    public static Global.Asciiz genNInsAsciiz(String name, String content) {
        Global.Asciiz asciiz = new Global.Asciiz(name, content);
        insMipsNode(asciiz);
        return asciiz;
    }

    public static MipsNode genNInsUnit(boolean isWord, String name, ArrayList<Integer> values) {
        MipsNode unit = new Global.Word(name, values);
        insMipsNode(unit);
        return unit;
    }

    public static Comment genNInsComment(String content) {
        Comment comment = new Comment(content);
        insMipsNode(comment);
        return comment;
    }

    public static MoveRmi genNInsMoveRmi(Register rs, Register rd) {
        MoveRmi i = new MoveRmi(rs, rd);
        insMipsNode(i);
        return i;
    }

    public static AluImi genNInsAluImi(AluImi.Op op, Register rs, Register rd, int imm) {
        AluImi aluImi = new AluImi(op, rs, rd, imm);
        insMipsNode(aluImi);
        return aluImi;
    }

    public static AluRmi genNInsAluRmi(AluRmi.Op op, Register rs, Register rt, Register rd, Integer shamt) {
        AluRmi aluRmi = new AluRmi(op, rs, rt, rd, shamt);
        insMipsNode(aluRmi);
        return aluRmi;
    }

    public static MuDiRmi genNInsMuDiRmi(MuDiRmi.Op op, Register rs, Register rt) {
        MuDiRmi muDiRmi = new MuDiRmi(op, rs, rt);
        insMipsNode(muDiRmi);
        return muDiRmi;
    }

    public static HiLoRmi genNInsHiLoRmi(HiLoRmi.Op op, Register rs) {
        HiLoRmi hiLoRmi = new HiLoRmi(op, rs);
        insMipsNode(hiLoRmi);
        return hiLoRmi;
    }

    public static MemImi genNInsMemImi(MemImi.Op op, Register rs, Register rt, int imm) {
        MemImi memImi = new MemImi(op, rs, rt, imm);
        insMipsNode(memImi);
        return memImi;
    }

    public static LiImi genNInsLiImi(Register rd, Integer imm) {
        LiImi liImi = new LiImi(rd, imm);
        insMipsNode(liImi);
        return liImi;
    }

    public static JumpJmi genNInsJumpJmi(JumpJmi.Op op, String label) {
        JumpJmi i = new JumpJmi(op, label);
        insMipsNode(i);
        return i;
    }

    public static JrRmi genNInsJrRmi(Register rs) {
        JrRmi i = new JrRmi(rs);
        insMipsNode(i);
        return i;
    }

    public static BranchImi genNInsBranchImi(BranchImi.Op op, Register rs, Register rt, String label) {
        BranchImi i = new BranchImi(op, rs, rt, label);
        insMipsNode(i);
        return i;
    }

    public static LaImi genNInsLaImi(Register rd, String label) {
        LaImi i = new LaImi(rd, label);
        insMipsNode(i);
        return i;
    }

    public static CmpRmi genNInsCmpRmi(CmpRmi.Op op, Register rs, Register rt, Register rd) {
        CmpRmi i = new CmpRmi(op, rs, rt, rd);
        insMipsNode(i);
        return i;
    }

    public static SltiImi genNInsSltiImi(Register rs, Register rd, Integer imm) {
        SltiImi i = new SltiImi(rs, rd, imm);
        insMipsNode(i);
        return i;
    }

    public static MipsBasicBlock genNInsBasicBlock(String label) {
        MipsBasicBlock bb = new MipsBasicBlock(label);
        insMipsNode(bb);
        return bb;
    }

    public static MipsFunction genNInsFunction(String label, Function func) {
        enterFunction(func);
        MipsFunction f = new MipsFunction(label);
        insMipsNode(f);
        return f;
    }

    public static SyscallMi genNInsSyscallMi() {
        SyscallMi i = new SyscallMi();
        insMipsNode(i);
        return i;
    }

    public static MipsModule getModule() {
        return module;
    }

    public static StackManager getStackManager() {
        return stackManager;
    }
}
