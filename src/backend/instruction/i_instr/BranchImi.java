package backend.instruction.i_instr;

import backend.Register;
import backend.instruction.ITypeInstruction;

public class BranchImi extends ITypeInstruction {
    public enum Op {
        // instrName $rs, $rt, offset
        beq, bne,
        // instrName $rs, offset
        bgtz, blez, bgez, bltz
    }

    private Op op;
    private String label;

    public BranchImi(Op op, Register rs, Register rt, String label) {
        super(op.name(), rs, rt, null);
        this.label = label;
        this.op = op;
    }

    @Override
    public String toString() {
        if (op.ordinal() <= Op.bne.ordinal()) {
            return instName + " " + rs + ", " + rt + ", " + label;
        } else {
            return instName + " " + rs + ", " + label;
        }
    }
}
