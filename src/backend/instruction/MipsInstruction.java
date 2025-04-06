package backend.instruction;

import backend.MipsNode;

public class MipsInstruction implements MipsNode {
    protected String instName;

    public MipsInstruction(String instName) {
        this.instName = instName;
    }
}
