package backend.instruction.i_instr;

import backend.Register;
import backend.instruction.ITypeInstruction;

public class MemImi extends ITypeInstruction {
    public enum Op {
        // load
        lw, lh, lhu, lbu,lb,
        // store
        sw, sh
    }

    public MemImi(Op op, Register base, Register rt, int immediate) {
        super(op.name(), base, rt, immediate);
    }

    @Override
    public String toString() {
        return instName + " " + rt + ", " + immediate + "(" + rs + ")";
    }
}
