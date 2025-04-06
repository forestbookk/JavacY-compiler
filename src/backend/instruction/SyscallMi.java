package backend.instruction;

import backend.MipsNode;

public class SyscallMi implements MipsNode {
    @Override
    public String toString() {
        return "syscall";
    }
}
