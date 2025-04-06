package llvm_ir.instr;

import backend.MipsBuilder;
import backend.Register;
import backend.instruction.i_instr.AluImi;
import llvm_ir.component.Instruction;
import llvm_ir.component.type.ArrayType;
import llvm_ir.component.type.LLVMType;
import llvm_ir.component.type.PointerType;

/* %var_name = alloca type, align alignment */
public class AllocaInstr extends Instruction {
    private LLVMType targetType;

    public AllocaInstr(String name, LLVMType targetType) {
        super(name, new PointerType(targetType));
        this.targetType = targetType;
    }

    @Override
    public String toString() {
        return name + " = alloca " + targetType;
    }

    @Override
    public void toAssembly() {
        super.toAssembly();
        Register rd = MipsBuilder.getRegisterForValue(this);
        // 在栈上分配空间
        if (targetType instanceof ArrayType) {
            MipsBuilder.getStackManager().decrementOffset(((ArrayType) targetType).getNumElements() * 4);
        } else {
            MipsBuilder.getStackManager().decrementOffset(4);
        }

        // 若无寄存器可分配：将指针存入栈
        if (rd == null) {
            // 用k0保存分配内存的首地址
            MipsBuilder.genNInsAluImi(AluImi.Op.addiu, Register.SP, Register.K0,
                    MipsBuilder.getStackManager().getCurOffset());
            // 按字存 内存首地址
            MipsBuilder.storeRegValueToStack(this, Register.K0, true);
        }
        // 有寄存器可分配
        else {
            // 直接将栈分配的空间地址赋值给这个寄存器
            MipsBuilder.genNInsAluImi(AluImi.Op.addiu, Register.SP, rd, MipsBuilder.stackManager.getCurOffset());
        }
    }

    @Override
    public boolean canBeUsed() {
        return true;
    }
}
