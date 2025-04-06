package backend.instruction.i_instr;

import backend.Register;
import backend.instruction.ITypeInstruction;

public class AluImi extends ITypeInstruction {
    public enum Op {
        // shiftv
        sllv, srav, srlv,
        // calc_I
        addiu, subiu, andi, ori, xori, slti, sltiu,
    }

    private Op op;

    // calc_i | shiftv
    public AluImi(Op op, Register rs, Register rd, int immediate) {
        super(op.name(), rs, null, immediate);
        this.rd = rd;
        this.op = op;
    }

    @Override
    public String toString() {
        return instName + " " + rd + ", " + rs + ", " + immediate;
    }
}
