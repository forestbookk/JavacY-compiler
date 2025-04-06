package backend.instruction.r_instr;

import backend.Register;
import backend.instruction.RTypeInstruction;

public class HiLoRmi extends RTypeInstruction {
    public enum Op {
        mfhi, mflo, mthi, mtlo
    }

    public HiLoRmi(Op op, Register rs) {
        super(op.name(), rs, null, null, null);
    }

    @Override
    public String toString() {
        return instName + " " + rs;
    }
}
