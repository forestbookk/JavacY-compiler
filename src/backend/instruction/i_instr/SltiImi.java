package backend.instruction.i_instr;

import backend.Register;
import backend.instruction.ITypeInstruction;

public class SltiImi extends ITypeInstruction {
    public SltiImi(Register rs, Register rd, Integer immediate) {
        super("slti", rs, null, immediate);
        this.rd = rd;
    }

    @Override
    public String toString() {
        return instName + " " + rd + ", " + rs + ", " + immediate;
    }
}
