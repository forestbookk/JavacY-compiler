package backend.instruction.r_instr;

import backend.Register;
import backend.instruction.RTypeInstruction;

public class MoveRmi extends RTypeInstruction {
    public MoveRmi(Register rs, Register rd) {
        super("move", rs, null, rd, null);
    }

    @Override
    public String toString() {
        return instName + " " + rd + ", " + rs;
    }
}
