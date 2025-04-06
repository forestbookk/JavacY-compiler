package llvm_ir.instr;

import backend.MipsBuilder;
import backend.Register;
import backend.instruction.i_instr.AluImi;
import backend.instruction.r_instr.AluRmi;
import backend.instruction.r_instr.HiLoRmi;
import backend.instruction.r_instr.MuDiRmi;
import llvm_ir.Value;
import llvm_ir.component.Constant;
import llvm_ir.component.Instruction;
import llvm_ir.component.UndefinedValue;
import llvm_ir.component.type.LLVMType;

import java.util.Objects;

public class AluInstr extends Instruction {
    public enum AluOp {
        ADD, SUB, MUL, SDIV, SREM
    }

    private AluOp aluOp;

    public AluInstr(String name, LLVMType type, AluOp aluOp, Value op1, Value op2) {
        super(name, type);
        this.aluOp = aluOp;
        addOperand(op1);
        addOperand(op2);
    }

    @Override
    public String toString() {
        return name + " = " + aluOp.toString().toLowerCase() + " " +
                type.toString() + " " + getOp1().getName() + ", " + getOp2().getName();
    }

    public void toIType(Register rs, Integer imm, Register rd) {
        switch (aluOp) {
            case ADD -> {
                MipsBuilder.genNInsAluImi(AluImi.Op.addiu, rs, rd, imm);
            }
            case SUB -> {
                MipsBuilder.genNInsAluImi(AluImi.Op.subiu, rs, rd, imm);
            }
            default -> {
                throw new RuntimeException("Wrong AluOp Into IType");
            }
        }
    }

    public void toRType(Value rsValue, Register rs, Value rtValue, Register rt, Register rd) {
        // 处理 op1
        // 若op1为常数：则直接存入reg1
        if (rsValue instanceof Constant) {
            MipsBuilder.genNInsLiImi(rs, ((Constant) rsValue).getContent());
        } else if (rsValue instanceof UndefinedValue) {
            MipsBuilder.genNInsLiImi(rs, 0);
        }
        // 若op1 非常数：
        else {
            rs = MipsBuilder.allocateOrLoadRegForValue(rsValue, rs, true);
        }

        // 处理 op2
        // 若op2为常数：则直接存入reg2
        if (rtValue instanceof Constant) {
            MipsBuilder.genNInsLiImi(rt, ((Constant) rtValue).getContent());
        } else if (rtValue instanceof UndefinedValue) {
            MipsBuilder.genNInsLiImi(rt, 0);
        }
        // 若op2 非常数：
        else {
            rt = MipsBuilder.allocateOrLoadRegForValue(rtValue, rt, true);
        }

        // 处理计算逻辑
        switch (aluOp) {
            case ADD -> {
                MipsBuilder.genNInsAluRmi(AluRmi.Op.addu, rs, rt, rd, null);
            }
            case SUB -> {
                MipsBuilder.genNInsAluRmi(AluRmi.Op.subu, rs, rt, rd, null);
            }
            case MUL -> {
                MipsBuilder.genNInsMuDiRmi(MuDiRmi.Op.mult, rs, rt);
                MipsBuilder.genNInsHiLoRmi(HiLoRmi.Op.mflo, rd);
            }
            case SDIV -> {
                MipsBuilder.genNInsMuDiRmi(MuDiRmi.Op.div, rs, rt);
                MipsBuilder.genNInsHiLoRmi(HiLoRmi.Op.mflo, rd);
            }
            case SREM -> {
                MipsBuilder.genNInsMuDiRmi(MuDiRmi.Op.div, rs, rt);
                MipsBuilder.genNInsHiLoRmi(HiLoRmi.Op.mfhi, rd);
            }
        }
    }

    public boolean isPowerOfTwo(int n) {
        return (n > 0) && ((n & (n - 1)) == 0);
    }

    @Override
    public void toAssembly() {
        super.toAssembly();
        Value op1 = getOp1();
        Value op2 = getOp2();
        final boolean isOp1Imm = op1.isImm();
        final boolean isOp2Imm = op2.isImm();
        Register reg1 = Register.K0;
        Register reg2 = Register.K1;
        Register rd = Objects.requireNonNullElse(
                MipsBuilder.getRegisterForValue(this), Register.K0);

        // 全常数：直接赋值给rd
        if ((aluOp == AluOp.ADD || aluOp == AluOp.SUB) &&
                (op1 instanceof Constant || op1 instanceof UndefinedValue) &&
                (op2 instanceof Constant || op2 instanceof UndefinedValue)) {
            int op1IntValue = op1 instanceof UndefinedValue ? 0 : ((Constant) op1).getContent();
            int op2IntValue = op2 instanceof UndefinedValue ? 0 : ((Constant) op2).getContent();
            switch (aluOp) {
                case ADD -> {
                    MipsBuilder.genNInsLiImi(rd, op1IntValue + op2IntValue);
                }
                case SUB -> {
                    MipsBuilder.genNInsLiImi(rd, op1IntValue - op2IntValue);
                }
            }
        } else if (aluOp == AluOp.MUL && (op1 instanceof UndefinedValue || op2 instanceof UndefinedValue)) {
            MipsBuilder.genNInsLiImi(rd, 0);
        }

        // 乘除常数优化：
        else if (aluOp == AluOp.MUL && (op1 instanceof Constant || op2 instanceof Constant)) {
            int whoToBeConstant = 0;

            if (op1 instanceof Constant && isPowerOfTwo(((Constant) op1).getContent())) {
                whoToBeConstant = 1;
            } else if (op2 instanceof Constant && isPowerOfTwo(((Constant) op2).getContent())) {
                whoToBeConstant = 2;
            } else if ((op1 instanceof Constant && ((Constant) op1).getContent() == 0) ||
                    (op2 instanceof Constant && ((Constant) op2).getContent() == 0)) {
                whoToBeConstant = 3;
            }


            // 结果一定为0
            if (whoToBeConstant == 3) {
                MipsBuilder.genNInsLiImi(rd, 0);
            }
            // 任何一个常数都不是2的幂，则仍然使用常规mul，不优化
            else if (whoToBeConstant == 0) {
                toRType(op1, reg1, op2, reg2, rd);
            } else {
                int constValue = whoToBeConstant == 1 ? ((Constant) op1).getContent() : ((Constant) op2).getContent();

                Register opReg = whoToBeConstant == 1 ? reg2 : reg1;
                Value op = whoToBeConstant == 1 ? op2 : op1;
                if (op instanceof Constant) {
                    MipsBuilder.genNInsLiImi(opReg, ((Constant) op).getContent());
                } else {
                    opReg = MipsBuilder.allocateOrLoadRegForValue(op, opReg, true);
                }

                int n = 0;
                while (constValue > 1) {
                    constValue >>= 1;
                    n++;
                }

                MipsBuilder.genNInsAluRmi(AluRmi.Op.sll, opReg, null, rd, n);
            }
        }

        // calc_i 处理
        else if ((aluOp == AluOp.ADD || aluOp == AluOp.SUB) && isOp2Imm) {
            // 若op1是常数
            if (op1 instanceof Constant) {
                // 将op1存进寄存器
                MipsBuilder.genNInsLiImi(reg1, ((Constant) op1).getContent());
            } else if (op1 instanceof UndefinedValue) {
                MipsBuilder.genNInsLiImi(reg1, 0);
            } else {
                reg1 = MipsBuilder.allocateOrLoadRegForValue(op1, reg1, true);
            }
            // 现在 op1 一定是寄存器形式
            toIType(reg1, ((Constant) op2).getContent(), rd);
        } else if (aluOp == AluOp.ADD && isOp1Imm && !isOp2Imm) {
            if (op2 instanceof Constant) {
                MipsBuilder.genNInsLiImi(reg2, ((Constant) op2).getContent());
            } else if (op2 instanceof UndefinedValue) {
                MipsBuilder.genNInsLiImi(reg2, 0);
            } else {
                reg2 = MipsBuilder.allocateOrLoadRegForValue(op2, reg2, true);
            }
            toIType(reg2, ((Constant) op1).getContent(), rd);
        }
        // calc_r 处理（此时 op1 和 op2 仍然有可能是立即数）
        else {
            toRType(op1, reg1, op2, reg2, rd);
        }

        // 处理rd
        if (MipsBuilder.getRegisterForValue(this) == null) {
            MipsBuilder.storeRegValueToStack(this, rd, true);
        }
    }

    @Override
    public boolean canBeUsed() {
        return true;
    }
}
