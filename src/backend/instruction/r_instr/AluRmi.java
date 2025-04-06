package backend.instruction.r_instr;

import backend.Register;
import backend.instruction.RTypeInstruction;

public class AluRmi extends RTypeInstruction {
    public enum Op {
        // calc_R
        addu, subu,
        and, or, nor, xor, slt, sltu,
        // shift
        sll, sra, srl
    }

    private Op op;


    // calc_r | shift
    public AluRmi(Op op, Register rs, Register rt, Register rd, Integer shamt) {
        super(op.name(), rs, rt, rd, shamt);
        this.op = op;
    }

    @Override
    public String toString() {
        if (op.ordinal() <= Op.sltu.ordinal())
            return instName + " " + rd + ", " + rs + ", " + rt;
        else
            return instName + " " + rd + "," + rs + "," + shamt;
    }
}
