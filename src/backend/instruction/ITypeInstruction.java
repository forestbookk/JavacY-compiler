package backend.instruction;

import backend.Register;

public class ITypeInstruction extends MipsInstruction {
    protected Register rs;
    protected Register rt;
    protected Register rd;
    protected Integer immediate;

    // aluImi
    public ITypeInstruction(String instName, Register rs, Register rt, Integer immediate) {
        super(instName);
        this.rs = rs;
        this.rt = rt;
        this.immediate = immediate;
    }


    // 伪指令 li
    public ITypeInstruction(String instName, Register rd, Integer immediate) {
        super(instName);
        this.rd = rd;
        this.immediate = immediate;
    }
}
