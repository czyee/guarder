package test.config;

import org.czyee.guarder.node.Node;
import org.czyee.guarder.node.NodeDefiner;

public class TestNodeDefiner implements NodeDefiner {

	@Override
	public Node[] getNodes() {
		return new Node[]{new Node("aaa")};
	}
}
