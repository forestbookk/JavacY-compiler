package backend.component;

import backend.MipsNode;
import backend.instruction.MipsInstruction;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class MipsBasicBlock implements MipsNode {
    private String label;
    private ArrayList<MipsInstruction> instrList;

    public MipsBasicBlock(String label) {
        this.label = label;
        this.instrList = new ArrayList<>();
    }

    public void addInstr(MipsInstruction i) {
        instrList.add(i);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(label).append(":");
        //sb.append(instrList.stream().map(Object::toString).collect(Collectors.joining("\n\t")));
        return sb.toString();
    }
}
