package backend.instruction.i_instr;

import backend.Register;
import backend.instruction.ITypeInstruction;

public class LaImi extends ITypeInstruction {
    private String label;

    public LaImi(Register rd, String label) {
        super("la", rd, null);
        this.label = label;
    }

    @Override
    public String toString() {
        return "la " + rd + ", " + label;
    }
}
