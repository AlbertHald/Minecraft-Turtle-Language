package dk.aau.p4.abaaja.Lib.Nodes;

public class StringExpNode extends ExpNode {
    @Override
    public void accept(INodeVisitor visitor) {
        visitor.visit(this);
    }
}
