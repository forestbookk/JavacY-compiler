package llvm_ir.instr;

import backend.MipsBuilder;
import backend.Register;
import backend.instruction.i_instr.MemImi;
import llvm_ir.Value;
import llvm_ir.component.Constant;
import llvm_ir.component.Instruction;
import llvm_ir.component.UndefinedValue;
import llvm_ir.component.type.LLVMType;

public class ZExtInstr extends Instruction {
    private LLVMType fromType;
    private LLVMType toType;

    public ZExtInstr(String name, LLVMType fromType, Value op, LLVMType toType) {
        super(name, toType);
        addOperand(op);
        this.fromType = fromType;
        this.toType = toType;
    }

    @Override
    public String toString() {
        return name + " = zext " + fromType + " " + getOp1().getName() + " to " + toType;
    }

    @Override
    public void toAssembly() {
        // TO/DO: 有i1
        super.toAssembly();
        Value oriValue = getOp1();
        if (oriValue instanceof Constant) {
            MipsBuilder.genNInsLiImi(Register.K0, ((Constant) oriValue).getContent());
            MipsBuilder.storeRegValueToStack(this, Register.K0, type.isInt32());
        } else if (oriValue instanceof UndefinedValue) {
            MipsBuilder.genNInsLiImi(Register.K0, 0);
            MipsBuilder.storeRegValueToStack(this, Register.K0, type.isInt32());
        } else if (MipsBuilder.getRegisterForValue(oriValue) != null) {
            MipsBuilder.storeRegValueToStack(this, MipsBuilder.getRegisterForValue(oriValue), type.isInt32());
        }
        // 此前原值在栈上
        else {// i8 -> i32 或 i1 -> i32
            MipsBuilder.getStackManager().addOffsetForValue(this, MipsBuilder.getStackManager().getStackOffset(oriValue));
//            if (!fromType.isInt32()) {
//                Register opReg = Register.K0;
//                // 取出op的值
//                MipsBuilder.genNInsMemImi(MemImi.Op.lw, Register.SP, opReg, MipsBuilder.getStackManager().getStackOffset(oriValue));
//                // 将op存入新的字空间
//                MipsBuilder.storeRegValueToStack(this, opReg, true);
//            } else {
//                MipsBuilder.getStackManager().addOffsetForValue(this, MipsBuilder.getStackManager().getStackOffset(oriValue));
//            }
        }
    }

    @Override
    public boolean canBeUsed() {
        return true;
    }
}
