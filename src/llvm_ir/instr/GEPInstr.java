package llvm_ir.instr;

import backend.MipsBuilder;
import backend.Register;
import backend.instruction.i_instr.AluImi;
import backend.instruction.i_instr.MemImi;
import backend.instruction.r_instr.AluRmi;
import llvm_ir.Value;
import llvm_ir.component.Constant;
import llvm_ir.component.GlobalVarDef;
import llvm_ir.component.Instruction;
import llvm_ir.component.UndefinedValue;
import llvm_ir.component.type.ArrayType;
import llvm_ir.component.type.LLVMType;
import llvm_ir.component.type.PointerType;

import java.util.Objects;

/* %result = getelementptr type, type* %pointer, i32 index */

public class GEPInstr extends Instruction {

    public GEPInstr(String name, LLVMType type, Value pointer, Value offset) {
        super(name, type);
        addOperand(pointer);
        addOperand(offset);
    }

    public Value getPointer() {
        return operands.get(0);
    }

    public Value getOffset() {
        return operands.get(1);
    }

    @Override
    public String toString() {
        Value pointer = getPointer();
        Value offset = getOffset();
        LLVMType targetType = ((PointerType) pointer.getResultType()).getElementType();
        if (targetType instanceof ArrayType) {
            return name + " = getelementptr inbounds " +
                    targetType + ", " +
                    pointer.getResultType() + " " +
                    pointer.getName() + ", i32 0, i32 " +
                    offset.getName();
        }
        return this.name + " = getelementptr inbounds " +
                targetType + ", " +
                pointer.getResultType() + " " +
                pointer.getName() + ", i32 " +
                offset.getName();
    }

    @Override
    public void toAssembly() {
        super.toAssembly();
        int byteNum = 4;
        Register reg1 = Register.K0; // 指针寄存器
        Register reg2 = Register.K1; // offset寄存器
        Register rd = Objects.requireNonNullElse(MipsBuilder.getRegisterForValue(this), Register.K0);

        Value pointer = getPointer();
        Value offset = getOffset();

        if (pointer instanceof GlobalVarDef) {
            MipsBuilder.genNInsLaImi(reg1, pointer.getName().substring(1));
        }
        // 此前指针存在寄存器
        else if (MipsBuilder.getRegisterForValue(pointer) != null) {
            reg1 = MipsBuilder.getRegisterForValue(pointer);
        }
        // 此前指针存在栈上
        else {
            // 无优化 按字取地址-> lw
            MipsBuilder.genNInsMemImi(MemImi.Op.lw,
                    Register.SP, reg1, MipsBuilder.getStackManager().getStackOffset(pointer));
        }

        if (offset instanceof Constant) {
            // 将指针寄存器中存的地址和offset合起来，得到目标地址
            MipsBuilder.genNInsAluImi(AluImi.Op.addiu, reg1, rd, ((Constant) offset).getContent() * byteNum);
        } else if (offset instanceof UndefinedValue) {
            // offset 为 0，所以目标地址和指针地址相等
            MipsBuilder.genNInsMoveRmi(reg1, rd);
        } else {
            reg2 = MipsBuilder.allocateOrLoadRegForValue(offset, reg2, offset.isWord());
//
            // 需要将offset左移两位
            MipsBuilder.genNInsAluRmi(AluRmi.Op.sll, reg2, null, Register.K1, 2);
            MipsBuilder.genNInsAluRmi(AluRmi.Op.addu, Register.K1, reg1, rd, null);
        }

        // 无优化：将rd存入栈，即将指针存储到栈中，指针4字节
        if (MipsBuilder.getRegisterForValue(this) == null) {
            MipsBuilder.storeRegValueToStack(this, rd, true);
//            MipsBuilder.getStackManager().decrementOffset(4);
//            MipsBuilder.getStackManager().addOffsetForValue(this);
//            MipsBuilder.genNInsMemImi(MemImi.Op.sw,
//                    Register.SP, rd, MipsBuilder.getStackManager().getCurOffset());
        }
    }


    @Override
    public boolean canBeUsed() {
        return true;
    }
}
