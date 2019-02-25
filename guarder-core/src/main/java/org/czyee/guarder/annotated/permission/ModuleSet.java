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
		//得排个序
		//冒泡排序
		for (int i = 0 ; i < moduleSets.size() - 1 ; i ++) {
			for (int j = 0 ; j < moduleSets.size() - 1 - i ; j ++){
				if (moduleSets.get(j).getModule().sort() > moduleSets.get(j + 1).getModule().sort()){
					ModuleSet temp = moduleSets.get(j);
					moduleSets.set(j,moduleSets.get(j + 1));
					moduleSets.set(j + 1 , temp);
				}
			}
		}
		ModuleSet.moduleSets = moduleSets;
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
