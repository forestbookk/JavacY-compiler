package backend.instruction;

import backend.Register;

public class RTypeInstruction extends MipsInstruction {
    protected Register rs; // 源寄存器
    protected Register rt; // 目标寄存器
    protected Register rd; // 结果寄存器
    protected Integer shamt; // 移位量

    public RTypeInstruction(String instName, Register rs, Register rt, Register rd, Integer shamt) {
        super(instName);
        this.rs = rs;
        this.rt = rt;
        this.rd = rd;
        this.shamt = shamt;
    }

}
