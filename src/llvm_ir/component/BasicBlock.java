package llvm_ir.component;

import backend.MipsBuilder;
import llvm_ir.Value;
import llvm_ir.component.function.Function;
import llvm_ir.component.function.Param;
import llvm_ir.instr.PhiInstr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class BasicBlock extends Value {
    private LinkedList<Instruction> instrList;
    private Function parentFunction;

    /* 活跃变量分析 */
    private boolean isDelete;
    private HashSet<Value> def;
    private HashSet<Value> use;
    private HashSet<Value> in;
    private HashSet<Value> out;

    /* 流图 */
    private ArrayList<BasicBlock> pre; // 前驱
    private ArrayList<BasicBlock> suc; // 后继
    private ArrayList<BasicBlock> dom; // this支配的基本块
    private BasicBlock domOfIDom;
    private ArrayList<BasicBlock> subOfIDom;
    private ArrayList<BasicBlock> df;

    public BasicBlock(String name) {
        super(name, null);
        this.instrList = new LinkedList<>();
        this.isDelete = false;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void addInstr(Instruction i) {
        instrList.add(i);
    }

    public void addPre(BasicBlock preBB) {
        if (pre == null) {
            pre = new ArrayList<>();
        }
        pre.add(preBB);
    }

    public void addPreAtIndex(int index, BasicBlock preBB) {
        if (pre == null) {
            pre = new ArrayList<>();
        }
        pre.add(index, preBB);
    }

    public void removePre(BasicBlock preBB) {
        Iterator<BasicBlock> it = pre.iterator();
        while (it.hasNext()) {
            if (it.next().equals(preBB)) {
                it.remove();
                break;
            }
        }
    }

    public void addSuc(BasicBlock sucBB) {
        if (suc == null) {
            suc = new ArrayList<>();
        }
        suc.add(sucBB);
    }

    public void addSucAtIndex(int index, BasicBlock sucBB) {
        if (suc == null) {
            suc = new ArrayList<>();
        }
        suc.add(index, sucBB);
    }

    public void removeSuc(BasicBlock sucBB) {
        Iterator<BasicBlock> it = suc.iterator();
        while (it.hasNext()) {
            if (it.next().equals(sucBB)) {
                it.remove();
                break;
            }
        }
    }

    public void setParentFunction(Function parentFunction) {
        this.parentFunction = parentFunction;
    }

    public LinkedList<Instruction> getInstrList() {
        return instrList;
    }

    public Instruction getLastInstr() {
        return instrList.getLast();
    }

    public void setPre(ArrayList<BasicBlock> pre) {
        this.pre = pre;
    }

    public void setSuc(ArrayList<BasicBlock> suc) {
        this.suc = suc;
    }

    public ArrayList<BasicBlock> getPre() {
        return pre;
    }

    public ArrayList<BasicBlock> getSuc() {
        return suc;
    }

    public HashSet<Value> getDef() {
        return def;
    }

    public HashSet<Value> getUse() {
        return use;
    }

    public HashSet<Value> getIn() {
        return in;
    }

    public HashSet<Value> getOut() {
        return out;
    }

    public void setIn(HashSet<Value> in) {
        this.in = in;
    }

    public void setOut(HashSet<Value> out) {
        this.out = out;
    }

    public Function getParentFunction() {
        return parentFunction;
    }

    public ArrayList<BasicBlock> getDom() {
        return dom;
    }

    public void setDom(ArrayList<BasicBlock> dom) {
        this.dom = dom;
    }

    public BasicBlock getDomOfIDom() {
        return domOfIDom;
    }

    public ArrayList<BasicBlock> getSubOfIDom() {
        return subOfIDom;
    }

    public void setDomOfIDom(BasicBlock domOfIDom) {
        this.domOfIDom = domOfIDom;
    }

    public void setSubOfIDom(ArrayList<BasicBlock> subOfIDom) {
        this.subOfIDom = subOfIDom;
    }

    public ArrayList<BasicBlock> getDf() {
        return df;
    }

    public void setDf(ArrayList<BasicBlock> df) {
        this.df = df;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(":\n\t");
        sb.append(instrList.stream().map(Object::toString).collect(Collectors.joining("\n\t")));
        sb.append('\n');
        return sb.toString();
    }

    @Override
    public void toAssembly() {
        super.toAssembly();
        MipsBuilder.genNInsBasicBlock(name);
        for (Instruction instruction : instrList) {
            instruction.toAssembly();
        }
    }

    /* Optimize */
    public void computeDefUse() {
        def = new HashSet<>();
        use = new HashSet<>();
        for (Instruction instr : instrList) {
            // phi指令代表赋值，所以它的所有右值？
            if (instr instanceof PhiInstr) {
                for (Value op : instr.getOperands()) {
                    if (op instanceof Instruction || op instanceof Param || op instanceof GlobalVarDef) {
                        use.add(op);
                    }
                }
            }
        }
        for (Instruction instr : instrList) {
            for (Value op : instr.getOperands()) {
                if ((!def.contains(op)) && (op instanceof Instruction || op instanceof Param || op instanceof GlobalVarDef)) {
                    use.add(op);
                }
            }
            if (!use.contains(instr) && instr.canBeUsed()) {
                def.add(instr);
            }
        }
    }
}
