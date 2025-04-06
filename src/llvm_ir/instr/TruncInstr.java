package llvm_ir.instr;

import backend.MipsBuilder;
import backend.Register;
import llvm_ir.Value;
import llvm_ir.component.Constant;
import llvm_ir.component.Instruction;
import llvm_ir.component.UndefinedValue;
import llvm_ir.component.type.LLVMType;

import java.util.Objects;

public class TruncInstr extends Instruction {
    private LLVMType fromType;

    public TruncInstr(String name, LLVMType fromType, Value op, LLVMType toType) {
        super(name, toType);
        this.fromType = fromType;
        addOperand(op);
    }

    @Override
    public String toString() {
        return name + " = trunc " + fromType + " " + getOp1().getName() + " to " + type;
    }

    @Override
    public void toAssembly() {
        super.toAssembly();
        Value oriValue = getOp1();
        Register rd = Objects.requireNonNullElse(
                MipsBuilder.getRegisterForValue(this), Register.K0);

        if (oriValue instanceof Constant) {
            MipsBuilder.genNInsLiImi(rd, ((Constant) oriValue).getContent() & 0xff);
        } else if (oriValue instanceof UndefinedValue) {
            MipsBuilder.genNInsLiImi(rd, 0);
        } else if (MipsBuilder.getRegisterForValue(oriValue) != null) {
            MipsBuilder.genNInsMoveRmi(MipsBuilder.getRegisterForValue(oriValue), rd);
            MipsBuilder.truncToI8(rd, rd);
        } else {
            MipsBuilder.loadFromStack(rd, oriValue, true);
            MipsBuilder.truncToI8(rd, rd);
        }

        if (MipsBuilder.getRegisterForValue(this) == null) {
            MipsBuilder.storeRegValueToStack(this, rd, true);
        }

//        if (oriValue instanceof Constant) {
//            MipsBuilder.genNInsLiImi(Register.K0, ((Constant) oriValue).getContent() & 0xff);
//            MipsBuilder.storeRegValueToStack(this, Register.K0, true);
//        } else if (oriValue instanceof UndefinedValue) {
//            MipsBuilder.genNInsLiImi(Register.K0, 0);
//            MipsBuilder.storeRegValueToStack(this, Register.K0, true);
//        } else if (MipsBuilder.getRegisterForValue(oriValue) != null) {
//            MipsBuilder.storeRegValueToStack(this, MipsBuilder.getRegisterForValue(oriValue), type.isInt32());
//        } else {
//            MipsBuilder.getStackManager().addOffsetForValue(this,
//                    MipsBuilder.getStackManager().getStackOffset(oriValue));
//        }
    }

    @Override
    public boolean canBeUsed() {
        return true;
    }
}
