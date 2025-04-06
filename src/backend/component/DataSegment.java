package backend.component;

import backend.MipsNode;
import backend.component.Global;
import frontend.symbol.symbols.Array.Array;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class DataSegment {
    private ArrayList<Global.Word> wordList;
    private ArrayList<Global.Asciiz> asciizList;

    public DataSegment() {
        this.wordList = new ArrayList<>();
        this.asciizList = new ArrayList<>();
    }

    public void addData(MipsNode node) {
        if (node instanceof Global.Word) {
            wordList.add((Global.Word) node);
        } else if (node instanceof Global.Asciiz) {
            asciizList.add((Global.Asciiz) node);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(wordList.stream().map(MipsNode::toString).
                collect(Collectors.joining("\n")));
        sb.append('\n');
        sb.append(asciizList.stream().map(MipsNode::toString).
                collect(Collectors.joining("\n")));
        sb.append('\n');
        return sb.toString();
    }
}
