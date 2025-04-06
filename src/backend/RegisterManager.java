package backend;

import llvm_ir.Value;
import llvm_ir.component.BasicBlock;
import llvm_ir.component.Instruction;
import llvm_ir.component.function.Param;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class RegisterManager {
    private HashMap<Value, Register> valToReg; // value到寄存器的映射

    public void setValToReg(HashMap<Value, Register> valToReg) {
        this.valToReg = valToReg;
    }

    public boolean isOn() {
        return this.valToReg != null;
    }

    public Register getRegisterForValue(Value value) {
        if (valToReg == null) return null;
        return valToReg.get(value);
    }

    public ArrayList<Register> getAllocatedRegs() {
        if (valToReg == null) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(new HashSet<>(valToReg.values()));
        }
    }

    public void allocRegForParam(Param param, Register register) {
        if (valToReg == null) {
            return;
        } else {
            valToReg.put(param, register);
        }
    }
}
