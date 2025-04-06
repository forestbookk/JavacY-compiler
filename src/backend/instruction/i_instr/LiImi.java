package backend.instruction.i_instr;

import backend.Register;
import backend.instruction.ITypeInstruction;

public class LiImi extends ITypeInstruction {
    public LiImi(Register rd, int immediate) {
        super("li", rd, immediate);
    }

    @Override
    public String toString() {
        return instName + " " + rd + ", " + immediate;
    }
}
