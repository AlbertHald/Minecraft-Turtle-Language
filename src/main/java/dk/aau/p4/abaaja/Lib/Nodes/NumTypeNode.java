package dk.aau.p4.abaaja.Lib.Nodes;

import dk.aau.p4.abaaja.Lib.Visitors.INodeVisitor;

public class NumTypeNode extends TypeNode {
    public String get_type() {
        return "NUMBER";
    }

    public NumTypeNode() {}
    public NumTypeNode(int arrayDegree) { this.set_arrayDegree(arrayDegree); }

    @Override
    public void accept(INodeVisitor visitor) {
        visitor.visit(this);
    }
}
