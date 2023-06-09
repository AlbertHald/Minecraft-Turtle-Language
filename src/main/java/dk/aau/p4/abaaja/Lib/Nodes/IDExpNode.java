package dk.aau.p4.abaaja.Lib.Nodes;

import dk.aau.p4.abaaja.Lib.Visitors.INodeVisitor;

public class IDExpNode extends ExpNode {
    private IDExpNode _idNode;
    public IDExpNode get_idNode() {
        return _idNode;
    }
    public void set_idNode(IDExpNode idNode) {
        this._idNode = idNode;
    }

    public String get_id() {
        return _idNode.get_id();
    }
    public int get_degree() { return _idNode.get_degree(); }
    public String get_contained_id() { return _idNode.get_contained_id(); }

    @Override
    public void accept(INodeVisitor visitor) {
        visitor.visit(this);
    }
}
