package org.czyee.guarder.node;

import java.util.List;

public class Node {

	private static List<Node> nodes;

	public static void initNodes(List<Node> nodes){
		Node.nodes = nodes;
	}

	public static Node findByName(String name){
		for (Node node : nodes) {
			if (node.getName().equals(name)){
				return node;
			}
		}
		return null;
	}

	public static List<Node> getAllNodes(){
		return nodes;
	}

	private String name;

	public Node(String name){
		if (name == null){
			throw new IllegalArgumentException("name can't be null");
		}
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
