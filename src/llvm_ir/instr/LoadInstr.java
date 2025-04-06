package llvm_ir.instr;

import backend.MipsBuilder;
import backend.Register;
import backend.instruction.i_instr.MemImi;
import llvm_ir.Value;
import llvm_ir.component.GlobalVarDef;
import llvm_ir.component.Instruction;
import llvm_ir.component.type.PointerType;

import java.util.Objects;

public class LoadInstr extends Instruction {

    public LoadInstr(String name, Value pointer) {
        super(name, ((PointerType) pointer.getResultType()).getElementType());
        addOperand(pointer);
    }

    public Value getPointer() {
        return operands.get(0);
    }

    @Override
    public String toString() {
        return name + " = load " + type.toString() + ", " + type + "* " + getPointer().getName();
    }

    @Override
    public void toAssembly() {
        super.toAssembly();
        Register reg = Register.K0; // 指针寄存器
        Register rd = Objects.requireNonNullElse(MipsBuilder.getRegisterForValue(this), Register.K0); // 存储的是值，而非地址

        Value pointer = getPointer();
        // 如果是全局变量
        if (pointer instanceof GlobalVarDef) {
            MipsBuilder.genNInsLaImi(reg, pointer.getName().substring(1));
        } else {
            reg = MipsBuilder.allocateOrLoadRegForValue(pointer, reg, true);
        }

        // 取指针寄存器上存的地址上的【值】 TODO:存疑
        MipsBuilder.genNInsMemImi( MemImi.Op.lw , reg, rd, 0);
        if (MipsBuilder.getRegisterForValue(this) == null) {
            MipsBuilder.storeRegValueToStack(this, rd, type.isInt32());
        }
    }

    @Override
    public boolean canBeUsed() {
        return true;
    }
}
