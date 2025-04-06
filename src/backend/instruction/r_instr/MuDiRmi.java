package backend.instruction.r_instr;

import backend.Register;
import backend.instruction.RTypeInstruction;

public class MuDiRmi extends RTypeInstruction {
    public enum Op {
        mult, div
    }

    public MuDiRmi(Op op, Register rs, Register rt) {
        super(op.name(), rs, rt, null, null);
    }

    @Override
    public String toString() {
        return instName + " " + rs + ", " + rt;
    }
}
