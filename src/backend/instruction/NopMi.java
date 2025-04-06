package backend.instruction;

import backend.MipsNode;

public class NopMi implements MipsNode {
    @Override
    public String toString() {
        return "nop";
    }
}
