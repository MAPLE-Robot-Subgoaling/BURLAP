package cat;

public class CausalEdge {

    private int start, end;
    private String relavantVariable;

    public CausalEdge(int start, int end, String var) {
        this.start = start;
        this.end = end;
        this.relavantVariable = var;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getRelavantVariable() {
        return relavantVariable;
    }
}
