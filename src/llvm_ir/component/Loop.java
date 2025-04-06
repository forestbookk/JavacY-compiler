package llvm_ir.component;

public class Loop {
    private BasicBlock condBB;
    private BasicBlock bodyBB;
    private BasicBlock updateBB;
    private BasicBlock endBB;

    public Loop(BasicBlock condBB, BasicBlock bodyBB,
                BasicBlock updateBB, BasicBlock endBB) {
        this.condBB = condBB;
        this.bodyBB = bodyBB;
        this.updateBB = updateBB;
        this.endBB = endBB;
    }

    public BasicBlock getBodyBB() {
        return bodyBB;
    }

    public BasicBlock getCondBB() {
        return condBB;
    }

    public BasicBlock getEndBB() {
        return endBB;
    }

    public BasicBlock getUpdateBB() {
        return updateBB;
    }
}
