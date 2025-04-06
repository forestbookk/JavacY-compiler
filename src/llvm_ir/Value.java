package llvm_ir;

import llvm_ir.component.Constant;
import llvm_ir.component.type.ConstantType;
import llvm_ir.component.type.IntegerType;
import llvm_ir.component.type.LLVMType;
import llvm_ir.component.type.PointerType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;

public class Value {
    protected String name;
    protected LLVMType type;
    protected ArrayList<Use> useList; // 使用这个值的地方

    public Value(String name, LLVMType type) {
        this.name = name;
        this.type = type;
        this.useList = new ArrayList<>();
    }

    public void addUse(Use use) {
        useList.add(use);
    }

    public void delUse(User user) {
        Iterator<Use> it = useList.iterator();
        while (it.hasNext()) {
            Use use = it.next();
            if (use.getUser() == user) {
                it.remove();
                break;
            }
        }
    }

    public IntegerType getIntegerTypeOfValue() {
        LLVMType type = this.type;
        if (type == null) {
            System.out.println(this.getClass());
        }
        if (type instanceof ConstantType) {
            return ((ConstantType) type).getAssignType();
        } else {
            return type.isInt32() ? IntegerType.INT_32 : IntegerType.CHAR;
        }
    }

    public boolean isImm() {
        if (!(this instanceof Constant)) return false;
        int content = ((Constant) this).getContent();
        return content >= -32768 && content <= 32767;
    }

    public boolean isWord() {
        return getIntegerTypeOfValue().isInt32() || type instanceof PointerType;
    }

    public LLVMType getResultType() {
        return type;
    }

    public void setType(LLVMType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public boolean isTypeVoid() {
        return type instanceof IntegerType && ((IntegerType) type).bitWidth == 0;
    }

    public void modifyAllUse(Value value) {
        ArrayList<User> users = useList.stream().map(Use::getUser).collect(Collectors.toCollection(ArrayList::new));
        for (User user : users) {
            boolean res = user.modifyAllOperands(this, value);
            assert res;
        }
    }

    public ArrayList<Use> getUseList() {
        return useList;
    }

    public void toAssembly() {

    }
}
