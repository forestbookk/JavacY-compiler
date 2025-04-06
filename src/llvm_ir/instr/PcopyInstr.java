package llvm_ir.instr;

import llvm_ir.Value;
import llvm_ir.component.Instruction;
import llvm_ir.component.type.IntegerType;

import java.util.ArrayList;

public class PcopyInstr extends Instruction {
    private ArrayList<Value> srcList;
    private ArrayList<Value> dstList;

    public PcopyInstr(String name) {
        super(name, IntegerType.VOID);
        this.srcList = new ArrayList<>();
        this.dstList = new ArrayList<>();
    }

    public ArrayList<Value> getSrcList() {
        return srcList;
    }

    public ArrayList<Value> getDstList() {
        return dstList;
    }

    public void addCopy(Value dst, Value src) {
        dstList.add(dst);
        srcList.add(src);
    }


}
