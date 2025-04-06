package backend.instruction;

public class JTypeInstruction extends MipsInstruction {
    protected String label;

    public JTypeInstruction(String instName, String label) {
        super(instName);
        this.label = label;
    }
}
