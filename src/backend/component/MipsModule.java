package backend.component;

import backend.MipsNode;
import backend.component.DataSegment;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class MipsModule {
    private DataSegment dataSegment;
    private ArrayList<MipsNode> textSegment;

    public MipsModule() {
        dataSegment = new DataSegment();
        textSegment = new ArrayList<>();
    }

    public void addIntoDataSeg(MipsNode node) {
        dataSegment.addData(node);
    }

    public void addIntoTextSegment(MipsNode node) {
        textSegment.add(node);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(".data:\n");
        sb.append(dataSegment.toString());
        sb.append("\n\n.text:\n");
        sb.append(textSegment.stream().map(MipsNode::toString).
                collect(Collectors.joining("\n")));
        return sb.toString();
    }
}
