package backend.instruction.r_instr;

import backend.Register;
import backend.instruction.RTypeInstruction;

public class JrRmi extends RTypeInstruction {
    public JrRmi(Register rs) {
        super("jr", rs, null, null, null);
    }

    @Override
    public String toString() {
        return instName + " " + rs;
    }
}
