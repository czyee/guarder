package org.czyee.guarder.annotated.permission;

import org.czyee.guarder.annotated.annotation.Module;
import org.czyee.guarder.annotated.annotation.Perm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Permission implements Serializable {

	private static List<Permission> permissions;

	public static void initPermissions(List<Permission> permissions){
		Permission.permissions = permissions;
	}

	/**
	 * 获取所有权限,用来给超级管理员授权
	 * @return
	 */
	public static List<Permission> getAllPermissions(){
		return permissions;
	}

	public static List<Permission> findByModule(Module module){
		List<Permission> list = new ArrayList<>();
		for (Permission permission : permissions) {
			if (module.id().equals(permission.perm.module())){
				list.add(permission);
			}
		}
		return list;
	}

	/**
	 * 根据路径获取权限
	 * @param path
	 * @return
	 */
	public static Permission findByPath(String path){
		for (Permission permission : permissions) {
			if (permission.getPath().equals(path)){
				return permission;
			}
		}
		return null;
	}

	private String path;//解析出来的路径
	private Perm perm;//对应的权限
	public void setPath(String path) {
		this.path = path;
	}

	public void setPerm(Perm perm) {
		this.perm = perm;
	}

	public String getPath() {
		return path;
	}

	public Perm getPerm() {
		return perm;
	}
}
