package org.czyee.guarder.annotated.permission;

import org.czyee.guarder.annotated.annotation.Module;
import org.czyee.guarder.node.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * 模块载体
 */
public class ModuleSet extends Permission{

	private static List<ModuleSet> moduleSets;

	public static void initModuelSets(List<ModuleSet> moduleSets){
		List<Node> nodes = Node.getAllNodes();
		List<ModuleSet> copy = new ArrayList<>();
		ModuleSet.moduleSets = moduleSets;
		//得排个序
		//冒泡排序
		for (Node node : nodes) {
			List<ModuleSet> list = findByNode(node);
			for (int i = 0 ; i < list.size() - 1 ; i ++) {
				for (int j = 0 ; j < list.size() - 1 - i ; j ++){
					if (list.get(j).getModule().sort() > list.get(j + 1).getModule().sort()){
						ModuleSet temp = list.get(j);
						list.set(j,list.get(j + 1));
						list.set(j + 1 , temp);
					}
				}
			}
			copy.addAll(list);
		}
		//把没有父节点的模块添加进copy
		for (ModuleSet moduleSet : moduleSets) {
			boolean noParent = true;
			for (Node node : nodes) {
				if (moduleSet.getModule().node().equals(node.getName())){
					noParent = false;
					break;
				}
			}
			if (noParent){
				copy.add(moduleSet);
			}
		}


		ModuleSet.moduleSets = copy;
	}

	public static List<ModuleSet> getAllModuleSets(){
		return moduleSets;
	}

	public static List<ModuleSet> findByNode(Node node){
		List<ModuleSet> list = new ArrayList<>();
		for (ModuleSet moduleSet : moduleSets) {
			if (moduleSet.getModule().node().equals(node.getName())){
				list.add(moduleSet);
			}
		}
		return list;
	}

	/**
	 * 根据路径获取模块
	 * @param path
	 * @return
	 */
	public static ModuleSet findByPath(String path){
		for (ModuleSet moduleSet : moduleSets) {
			if (moduleSet.getPath().equals(path)){
				return moduleSet;
			}
		}
		return null;
	}

	public static ModuleSet findByModuleId(String moduleId) {
		for (ModuleSet moduleSet : moduleSets) {
			if (moduleSet.getModule().id().equals(moduleId)){
				return moduleSet;
			}
		}
		return null;
	}

	public static List<ModuleSet> findNoPermModule(){
		List<ModuleSet> list = new ArrayList<>();
		for (ModuleSet moduleSet : moduleSets) {
			if (moduleSet.getPerm() == null){
				list.add(moduleSet);
			}
		}
		return list;
	}

	private Module module;//对应的模块

	public Module getModule() {
		return module;
	}

	public void setModule(Module module) {
		this.module = module;
	}
}
