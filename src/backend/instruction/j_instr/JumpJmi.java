package backend.instruction.j_instr;

import backend.instruction.JTypeInstruction;

public class JumpJmi extends JTypeInstruction {
    public enum Op {
        j, jal
    }

    private Op op;

    public JumpJmi(Op op, String label) {
        super(op.name(), label);
    }

    @Override
    public String toString() {
        return instName + " " + label;
    }
}
