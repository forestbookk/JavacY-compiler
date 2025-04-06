package llvm_ir.instr.io;

import llvm_ir.component.Instruction;
import llvm_ir.component.type.LLVMType;

public class IOInstr extends Instruction {
    public IOInstr(String name, LLVMType type) {
        super(name, type);
    }
}
