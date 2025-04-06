package backend.instruction.r_instr;

import backend.Register;
import backend.instruction.RTypeInstruction;

public class CmpRmi extends RTypeInstruction {
    public enum Op {
        slt, sgt, sle, sge, seq, sne
    }

    public CmpRmi(Op op, Register rs, Register rt, Register rd) {
        super(op.name(), rs, rt, rd, null);
    }

    @Override
    public String toString() {
        return instName + " " + rd + ", " + rs + ", " + rt;
    }
}
