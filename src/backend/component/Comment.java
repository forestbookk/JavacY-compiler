package backend.component;

import backend.MipsNode;

public class Comment implements MipsNode {
    private String content;

    public Comment(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "\n# " + content;
    }
}
