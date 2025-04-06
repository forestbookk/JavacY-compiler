package backend.component;

import backend.MipsNode;
import backend.component.MipsBasicBlock;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class MipsFunction implements MipsNode {
    private String label;
    private ArrayList<MipsBasicBlock> bbList;

    public MipsFunction(String label) {
        this.label = label;
        this.bbList = new ArrayList<>();
    }

    public void addBB(MipsBasicBlock bb) {
        bbList.add(bb);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('\n').append(label).append(":");

        return sb.toString();
    }
}
