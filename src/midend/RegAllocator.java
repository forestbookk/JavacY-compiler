package midend;

import backend.Register;
import frontend.symbol.symbols.Func.Func;
import llvm_ir.Value;
import llvm_ir.component.BasicBlock;
import llvm_ir.component.Instruction;
import llvm_ir.component.function.Function;
import llvm_ir.instr.PhiInstr;
import llvm_ir.instr.ZExtInstr;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class RegAllocator {
    private HashMap<Value, Register> val2reg; // value到寄存器的映射
    private HashMap<Register, Value> reg2val; // 寄存器到value的映射
    private HashMap<Value, Value> lastUseLocation;
    private HashSet<Register> tempRegs;

    public RegAllocator() {
        this.val2reg = new HashMap<>();
        this.reg2val = new HashMap<>();
        this.lastUseLocation = new HashMap<>();

        this.tempRegs = new HashSet<>();
        for (int i = Register.T0.ordinal(); i <= Register.T9.ordinal(); i++) {
            tempRegs.add(Register.getRegByIndex(i));
        }
    }

    public void performRegAllocation() {
        for (Function function : Optimizer.irModule.getFunctionList()) {
            initState();
            allocaRegForBB(function.getBBList().get(0));
            function.setVal2reg(val2reg);
        }
    }

    public void initState() {
        reg2val = new HashMap<>();
        val2reg = new HashMap<>();
        lastUseLocation = new HashMap<>();
    }

    // 分配寄存器
    public void allocaRegForBB(BasicBlock bb) {
        LinkedList<Instruction> instructions = bb.getInstrList();
        Set<Value> definedValues = new HashSet<>();
        Set<Value> unusedValueAfterBlock = new HashSet<>();

        // 记录每个量的最后使用位置
        for (Instruction instr : instructions) {
            for (Value op : instr.getOperands()) {
                lastUseLocation.put(op, instr);
            }
        }

        // 分配寄存器
        for (Instruction instr : instructions) {
            if (!(instr instanceof PhiInstr)) {
                for (Value op : instr.getOperands()) {
                    if (lastUseLocation.get(op) == instr &&
                            !bb.getOut().contains(op) &&
                            val2reg.containsKey(op)) {
                        reg2val.remove(val2reg.get(op));
                        unusedValueAfterBlock.add(op);
                    }
                }
            }

            if (instr.canBeUsed() && !(instr instanceof ZExtInstr)) {
                definedValues.add(instr);
                Register reg = allocaRegForInstr();
                if (reg != null) {
                    if (reg2val.containsKey(reg)) {
                        val2reg.remove(reg2val.get(reg));
                    }
                    reg2val.put(reg, instr);
                    val2reg.put(instr, reg);
                }
            }
        }

        for (BasicBlock subOfIDom : bb.getSubOfIDom()) {
            HashMap<Register, Value> bufferReg2val = new HashMap<>();
            for (Map.Entry<Register, Value> entry : reg2val.entrySet()) {
                if (!subOfIDom.getIn().contains(entry.getValue())) {
                    bufferReg2val.put(entry.getKey(), entry.getValue());
                }
            }

            for (Register reg : bufferReg2val.keySet()) {
                reg2val.remove(reg);
            }

            allocaRegForBB(subOfIDom);

            for (Register reg : bufferReg2val.keySet()) {
                reg2val.put(reg, bufferReg2val.get(reg));
            }
        }

        // release regs of definedValue
        for (Value value : definedValues) {
            if (val2reg.containsKey(value)) {
                reg2val.remove(val2reg.get(value));
            }
        }

        for (Value value : unusedValueAfterBlock) {
            if (val2reg.containsKey(value) && !definedValues.contains(value)) {
                reg2val.put(val2reg.get(value), value);
            }
        }
    }

    public Register allocaRegForInstr() {
        Set<Register> allocated = reg2val.keySet();
        for (Register reg : tempRegs) {
            if (!allocated.contains(reg)) {
                return reg;
            }
        }
        return tempRegs.iterator().next();
    }
}
