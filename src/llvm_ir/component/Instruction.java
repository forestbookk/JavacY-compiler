package llvm_ir.component;

import backend.MipsBuilder;
import llvm_ir.User;
import llvm_ir.component.type.LLVMType;

public class Instruction extends User {
    protected BasicBlock parentBB; // 所属的基本块

    public Instruction(String name, LLVMType type) {
        super(name, type);
    }

    public BasicBlock getParentBB() {
        return parentBB;
    }

    public void setParentBB(BasicBlock parentBB) {
        this.parentBB = parentBB;
    }

    public boolean canBeUsed() {
        return false;
    }

    @Override
    public void toAssembly() {
        MipsBuilder.genNInsComment(this.toString());
    }
}
