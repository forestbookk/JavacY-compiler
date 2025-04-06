package llvm_ir.instr.io;

import backend.MipsBuilder;
import backend.Register;
import llvm_ir.Value;
import llvm_ir.component.Constant;
import llvm_ir.component.UndefinedValue;
import llvm_ir.component.type.IntegerType;

public class PutCh extends IOInstr {

    public PutCh(String name, Value value) {
        super(name, IntegerType.VOID);
        addOperand(value);
    }

    public Value getValue() {
        return operands.get(0);
    }

    @Override
    public String toString() {
        return "call void @putch(i32 " + getValue().getName() + ")";
    }

    @Override
    public void toAssembly() {
        super.toAssembly();
        Value value = getValue();
        if (value instanceof Constant) {
            MipsBuilder.genNInsLiImi(Register.A0, ((Constant) value).getContent() & 0xff);
        } else if (value instanceof UndefinedValue) {
            MipsBuilder.genNInsLiImi(Register.A0, 0);
        } else if (MipsBuilder.getRegisterForValue(value) != null) {
            Register oriReg = MipsBuilder.getRegisterForValue(value);
            MipsBuilder.truncToI8(oriReg, Register.A0);
            //MipsBuilder.genNInsMoveRmi(oriReg, Register.A0);
        } else {
            // 无优化
            MipsBuilder.loadFromStack(Register.A0, value, false);
            MipsBuilder.truncToI8(Register.A0, Register.A0);
        }
        MipsBuilder.genNInsLiImi(Register.V0, 11);
        MipsBuilder.genNInsSyscallMi();
    }
}
