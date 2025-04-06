package llvm_ir.instr.io;

import backend.MipsBuilder;
import backend.Register;
import llvm_ir.component.type.IntegerType;

public class GetInt extends IOInstr {
    public GetInt(String name) {
        super(name, IntegerType.INT_32);
    }

    @Override
    public String toString() {
        return name + " = call i32 () @getint()";
    }


    @Override
    public void toAssembly() {
        super.toAssembly();
        MipsBuilder.genNInsLiImi(Register.V0, 5);
        MipsBuilder.genNInsSyscallMi();
        if (MipsBuilder.getRegisterForValue(this) != null) {
            MipsBuilder.genNInsMoveRmi(Register.V0, MipsBuilder.getRegisterForValue(this));
        } else {
            MipsBuilder.storeRegValueToStack(this, Register.V0, true);
        }
    }

    @Override
    public boolean canBeUsed() {
        return true;
    }
}
