package llvm_ir.component.function;

import backend.MipsBuilder;
import backend.Register;
import llvm_ir.Value;
import llvm_ir.component.BasicBlock;
import llvm_ir.component.type.IntegerType;
import llvm_ir.component.type.LLVMType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Function extends Value {
    private ArrayList<Param> paramList;
    private ArrayList<LLVMType> paramTypeList;
    private ArrayList<BasicBlock> bbList;
    private boolean haveRetIfVoid = false;
    private HashMap<Value, Register> val2reg;

    /* 流图 */
    private HashMap<BasicBlock, ArrayList<BasicBlock>> pre; // 函数内所有基本块的前驱集
    private HashMap<BasicBlock, ArrayList<BasicBlock>> suc; // 函数内所有基本块的后继集

    private HashMap<BasicBlock, BasicBlock> sub2domOfIDom; // 一个sub只能有一个dom
    private HashMap<BasicBlock, ArrayList<BasicBlock>> dom2subOfIDom; // 一个dom可以有多个sub


    public Function(String name, LLVMType type) {
        super(name, type);
        this.paramList = new ArrayList<>();
        this.paramTypeList = new ArrayList<>();
        this.bbList = new ArrayList<>();
        this.val2reg = new HashMap<>();
    }

    public void addParam(Param p) {
        paramList.add(p);
        paramTypeList.add(p.getResultType());
    }

    public ArrayList<LLVMType> getParamTypeList() {
        return paramTypeList;
    }

    public void addBB(BasicBlock bb) {
        bbList.add(bb);
    }


    public boolean isHaveRetIfVoid() {
        if (type != IntegerType.VOID) {
            return true;
        }
        return haveRetIfVoid;
    }

    public void setHaveRetIfVoid(boolean haveRetIfVoid) {
        this.haveRetIfVoid = haveRetIfVoid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("define dso_local ").append(type.toString()).append(" ").append(name).append("(");
        for (int i = 0; i < paramList.size(); i++) {
            sb.append(paramList.get(i).toString());
            sb.append(i == paramList.size() - 1 ? "" : ", ");
        }
        sb.append(") {\n");
        sb.append(bbList.stream()
                .map(BasicBlock::toString)
                .collect(Collectors.joining("\n")));
        sb.append("}\n");
        return sb.toString();
    }

    @Override
    public void toAssembly() {
        super.toAssembly();
        MipsBuilder.genNInsFunction(name.substring(1), this);
        int paramNum = 0;
        for (Param param : paramList) {
            paramNum++;
            if (paramNum <= 3) {
                MipsBuilder.registerManager.allocRegForParam(param, Register.getRegByIndex(Register.A0.ordinal() + paramNum));
            }
            MipsBuilder.getStackManager().decrementOffset(4);
            MipsBuilder.getStackManager().addOffsetForValue(param);
        }
        for (BasicBlock bb : bbList) {
            bb.toAssembly();
        }
    }

    public ArrayList<BasicBlock> getBBList() {
        return bbList;
    }

    public void setSuc(HashMap<BasicBlock, ArrayList<BasicBlock>> suc) {
        this.suc = suc;
    }

    public void setPre(HashMap<BasicBlock, ArrayList<BasicBlock>> pre) {
        this.pre = pre;
    }

    public void setDom2subOfIDom(HashMap<BasicBlock, ArrayList<BasicBlock>> dom2subOfIDom) {
        this.dom2subOfIDom = dom2subOfIDom;
    }

    public void setSub2domOfIDom(HashMap<BasicBlock, BasicBlock> sub2domOfIDom) {
        this.sub2domOfIDom = sub2domOfIDom;
    }

    public HashMap<BasicBlock, BasicBlock> getSub2domOfIDom() {
        return sub2domOfIDom;
    }

    public HashMap<BasicBlock, ArrayList<BasicBlock>> getDom2subOfIDom() {
        return dom2subOfIDom;
    }

    public void setVal2reg(HashMap<Value, Register> val2reg) {
        this.val2reg = val2reg;
    }

    public HashMap<Value, Register> getVal2reg() {
        return val2reg;
    }
}
