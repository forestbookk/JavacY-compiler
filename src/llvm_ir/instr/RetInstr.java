package llvm_ir.instr;

import backend.MipsBuilder;
import backend.Register;
import llvm_ir.Value;
import llvm_ir.component.Constant;
import llvm_ir.component.Instruction;
import llvm_ir.component.UndefinedValue;
import llvm_ir.component.type.LLVMType;

public class RetInstr extends Instruction {

    public RetInstr(LLVMType type, Value returnValue) {
        super(null, type);
        addOperand(returnValue);
    }

    public Value getReturnValue() {
        return operands.get(0);
    }

    @Override
    public String toString() {
        Value returnValue = getReturnValue();
        if (returnValue == null) {
            return "ret void";
        }
        return "ret " + type + " " + returnValue.getName();
    }

    @Override
    public void toAssembly() {
        super.toAssembly();
        Value returnValue = getReturnValue();
        if (returnValue != null) {
            if (returnValue instanceof Constant) {
                MipsBuilder.genNInsLiImi(Register.V0, type.isInt32() ? ((Constant) returnValue).getContent() :
                        ((Constant) returnValue).getContent() & 0xff);
            } else if (returnValue instanceof UndefinedValue) {
                MipsBuilder.genNInsLiImi(Register.V0, 0);
            } else if (MipsBuilder.getRegisterForValue(returnValue) != null) {
                // 如果返回值先前就有分配寄存器，那么需要将值move到V0
                Register oriReg = MipsBuilder.getRegisterForValue(returnValue);
                MipsBuilder.genNInsMoveRmi(oriReg, Register.V0);
                if (!type.isInt32()) {
                    MipsBuilder.truncToI8(Register.V0, Register.V0);
                }
            } else {
                MipsBuilder.loadFromStack(Register.V0, returnValue, type.isInt32());
                if (!type.isInt32()) {
                    MipsBuilder.truncToI8(Register.V0, Register.V0);
                }
                //MipsBuilder.storeRegValueToStack(returnValue, Register.V0, type.isInt32());
            }
        }
        MipsBuilder.genNInsJrRmi(Register.RA);
    }
}
