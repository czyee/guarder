package org.czyee.guarder.annotated.permission;

import org.czyee.guarder.annotated.annotation.Module;
import org.czyee.guarder.annotated.annotation.Perm;
import org.czyee.guarder.node.Node;
import org.czyee.guarder.node.NodeDefiner;
import org.czyee.guarder.node.PermNode;
import org.czyee.guarder.node.TreeNode;
import org.czyee.guarder.session.SessionUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionHandler {

	private String sessionPermissionKey;

	private NodeDefiner nodeDefiner;

	public void setNodeDefiner(NodeDefiner nodeDefiner) {
		this.nodeDefiner = nodeDefiner;
	}

	/**
	 * 初始化权限
	 * @param permissions
	 * @param moduleSets
	 */
	public void initPermissions(List<Permission> permissions , List<ModuleSet> moduleSets){
		ModuleSet.initModuelSets(moduleSets);
		Permission.initPermissions(permissions);
		Node[] nodes = nodeDefiner.getNodes();
		Node.initNodes(Arrays.asList(nodes));
	}

	/**
	 * 设置会话权限属性的key
	 * @param sessionPermissionKey
	 */
	public void setSessionPermissionKey(String sessionPermissionKey) {
		if (sessionPermissionKey == null){
			throw new IllegalArgumentException("sessionPermissionKey can't be null");
		}
		this.sessionPermissionKey = sessionPermissionKey;
	}

	/**
	 * 授权
	 * @param permPaths
	 */
	public void auth(List<String> permPaths){
		PermissionAttribute permissionAttribute = createPermissionAttribute(permPaths);
		SessionUtil.setAttribute(sessionPermissionKey,permissionAttribute);
	}

	/**
	 * 授权全部权限
	 */
	public void superAuth(){
		List<Permission> allPermissions = Permission.getAllPermissions();
		List<String> list = new ArrayList<>();
		for (Permission allPermission : allPermissions) {
			list.add(allPermission.getPath());
		}
		auth(list);
	}


	public PermissionAttribute createPermissionAttribute(List<String> permPaths){
		if (permPaths == null){
			return null;
		}
		List<Permission> permissions = new ArrayList<>();
		List<ModuleSet> moduleSets = new ArrayList<>();
		for (String permPath : permPaths) {
			if (permPath == null){
				throw new RuntimeException("wrong permission");
			}
			Permission permission = Permission.findByPath(permPath);
			if (permission != null){
				permissions.add(permission);
			}
			ModuleSet moduleSet = ModuleSet.findByPath(permPath);
			if (moduleSet != null){
				moduleSets.add(moduleSet);
			}
		}
		List<ModuleSet> noPermModule = ModuleSet.findNoPermModule();
		//将不需要权限的模块也一并授权
		for (ModuleSet moduleSet : noPermModule) {
			//已授权的模块不含当前模块再添加
			if (!moduleSets.contains(moduleSet)){
				moduleSets.add(moduleSet);
			}
		}
		PermissionAttribute permissionAttribute = new PermissionAttribute();
		permissionAttribute.moduleSets = moduleSets;
		permissionAttribute.permissions = permissions;

		return permissionAttribute;
	}

	/**
	 * 根据会话判断是否允许访问
	 * @param perm
	 * @return
	 */
	public boolean canAccess(Perm perm){
		if (perm == null || !perm.value()){
			return true;
		}
		PermissionAttribute permissionAttribute = getSessionPermissionAttribute();
		return hasPerm(perm,permissionAttribute);
	}

	private boolean hasPerm(Perm perm , PermissionAttribute permissionAttribute){
		if (permissionAttribute == null){
			return false;
		}
		List<Permission> permissions = permissionAttribute.permissions;
		for (Permission permission : permissions) {
			if (permission.getPerm() == perm){
				return true;
			}
		}
		return false;
	}

	private PermissionAttribute getSessionPermissionAttribute(){
		Object attribute = SessionUtil.getAttribute(sessionPermissionKey);
		if (attribute == null){
			return null;
		}
		if (!(attribute instanceof PermissionAttribute)){
			return null;
		}
		return (PermissionAttribute) attribute;
	}

	/**
	 * 获取当前会话的节点列表
	 * @return
	 */
	public List<TreeNode> getSessionTreeNode(){
		PermissionAttribute permissionAttribute = getSessionPermissionAttribute();
		if (permissionAttribute == null){
			return new ArrayList<>(0);
		}
		List<Permission> permissions = permissionAttribute.permissions;
		List<ModuleSet> moduleSets = permissionAttribute.moduleSets;
		List<TreeNode> treeNodes = new ArrayList<>();

		//先获取没有父节点的模块
		for (ModuleSet moduleSet : moduleSets) {
			Module module = moduleSet.getModule();
			Node node = Node.findByName(module.node());
			if (node == null){
				TreeNode treeNode = new TreeNode();
				treeNode.setUrl(moduleSet.getPath());
				treeNode.setName(module.name());
				treeNodes.add(treeNode);
			}
		}
		//获取节点
		for (ModuleSet moduleSet : moduleSets){
			Module module = moduleSet.getModule();
			Node node = Node.findByName(module.node());
			if (node == null){
				continue;
			}
			TreeNode treeNode = newTreeNode(treeNodes, node.getName());
			if (treeNode.getChildren() == null){
				treeNode.setChildren(new ArrayList<>());
			}
			treeNode.setName(node.getName());



			TreeNode moduleNode = new TreeNode();
			moduleNode.setName(module.name());
			moduleNode.setUrl(moduleSet.getPath());
			treeNode.getChildren().add(moduleNode);
		}
		return treeNodes;
	}

	private TreeNode newTreeNode(List<TreeNode> treeNodes , String name){
		for (TreeNode treeNode : treeNodes) {
			if (treeNode.getName().equals(name)){
				return treeNode;
			}
		}
		TreeNode treeNode = new TreeNode();
		treeNodes.add(treeNode);
		return treeNode;
	}


	public List<PermNode> getSimpleTreeNode(List<String> permPaths){
		PermissionAttribute permissionAttribute = createPermissionAttribute(permPaths);
//		List<Node> allNodes = Node.getAllNodes();
		List<ModuleSet> allModuleSets = ModuleSet.getAllModuleSets();
//		List<Permission> allPermissions = Permission.getAllPermissions();

		List<PermNode> permNodes = new ArrayList<>();

		//先获取所有没有父节点的模块
		for (ModuleSet moduleSet : allModuleSets) {
			Node node = Node.findByName(moduleSet.getModule().node());
			if (node == null){
				PermNode moduleNode = new PermNode();
				moduleNode.setName(moduleSet.getModule().name());
				moduleNode.setUrl(moduleSet.getPath());
				moduleNode.setChildren(new ArrayList<>());
				if (moduleSet.getPerm() == null){
					moduleNode.setNoPerm(1);
				}else if (hasPerm(moduleSet.getPerm(),permissionAttribute)){
					moduleNode.setChecked(1);
				}
				permNodes.add(moduleNode);
				List<Permission> permissions = Permission.findByModule(moduleSet.getModule());
				for (Permission permission : permissions) {
					PermNode permissionNode = new PermNode();
					permissionNode.setName(permission.getPerm().name());
					permissionNode.setUrl(permission.getPath());
					if (hasPerm(permission.getPerm(),permissionAttribute)){
						permissionNode.setChecked(1);
					}
					moduleNode.getChildren().add(permissionNode);
				}
			}
		}

		//获取所有节点
		Node[] nodes = nodeDefiner.getNodes();
		for (Node node : nodes) {
			PermNode permNode = new PermNode();
			permNode.setName(node.getName());
			permNode.setChildren(new ArrayList<>());
			List<ModuleSet> moduleSets = ModuleSet.findByNode(node);
			for (ModuleSet moduleSet : moduleSets) {
				PermNode moduleNode = new PermNode();
				moduleNode.setName(moduleSet.getModule().name());
				moduleNode.setUrl(moduleSet.getPath());
				moduleNode.setChildren(new ArrayList<>());
				if (moduleSet.getPerm() == null){
					moduleNode.setNoPerm(1);
					moduleNode.setChecked(1);
				}else if (hasPerm(moduleSet.getPerm(),permissionAttribute)){
					moduleNode.setChecked(1);
				}
				List<Permission> permissions = Permission.findByModule(moduleSet.getModule());
				for (Permission permission : permissions) {
					PermNode permissionNode = new PermNode();
					permissionNode.setUrl(permission.getPath());
					permissionNode.setName(permission.getPerm().name());
					if (hasPerm(permission.getPerm(),permissionAttribute)){
						permissionNode.setChecked(1);
					}
					moduleNode.getChildren().add(permissionNode);
				}
				permNode.getChildren().add(moduleNode);
			}
			permNodes.add(permNode);
		}

		//获取所有没有分组的权限
		List<PermNode> noGroupPerms = null;
		List<Permission> allPermissions = Permission.getAllPermissions();
		for (Permission allPermission : allPermissions) {
			ModuleSet moduleSet = ModuleSet.findByModuleId(allPermission.getPerm().module());
			//没有父节点
			if (moduleSet == null){
				PermNode permNode = new PermNode();
				if (hasPerm(allPermission.getPerm(),permissionAttribute)){
					permNode.setChecked(1);
				}
				permNode.setUrl(allPermission.getPath());
				String permName = allPermission.getPerm().name();
				if ("".equals(permName)){
					permNode.setName(allPermission.getPath());
				}else {
					permNode.setName(permName);
				}
				if (noGroupPerms == null){
					noGroupPerms = new ArrayList<>();
				}
				noGroupPerms.add(permNode);
			}
		}
		if (noGroupPerms != null){
			PermNode permNode = new PermNode();
			permNode.setName("(其他权限)");
			permNode.setChildren(new ArrayList<>());
			//其他权限
			permNodes.add(permNode);


			PermNode moduleNode = new PermNode();
			permNode.getChildren().add(moduleNode);
			moduleNode.setChildren(new ArrayList<>());
			moduleNode.setName("(未分组权限)");
			for (PermNode noGroupPerm : noGroupPerms) {
				moduleNode.getChildren().add(noGroupPerm);
			}
		}
		return permNodes;
	}

	/**
	 * 内部类,存储会话权限的类
	 */
	private static class PermissionAttribute implements Serializable {
		private List<ModuleSet> moduleSets;
		private List<Permission> permissions;
	}
}
