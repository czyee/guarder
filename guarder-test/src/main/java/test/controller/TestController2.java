package test.controller;

import com.google.gson.Gson;
import org.czyee.guarder.annotated.annotation.Module;
import org.czyee.guarder.annotated.annotation.Perm;
import org.czyee.guarder.annotated.permission.PermissionHandler;
import org.czyee.guarder.node.PermNode;
import org.czyee.guarder.node.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
public class TestController2 {

	@Autowired
	private PermissionHandler permissionHandler;

	@RequestMapping("html/ht1.html")
	@Module(id = "ht1" , name = "后台1" , node = "aaa")
	public String ht1(Integer id) {
		if (id != null && id == 10){
			System.out.println("pass");
		}
		return "html/ht1.html";
	}

	@RequestMapping("html/ht2.html")
	@ResponseBody
	@Perm(name = "ht2" , module = "ht1")
	public String ht2(){
		return "123";
	}

	@RequestMapping("html/ht3.html")
	@ResponseBody
	public String ht3(){
		List<String> list = new ArrayList<>();
		list.add("/html/ht1.html");
		permissionHandler.auth(list);
		return "auth!";
	}

	private Gson gson = new Gson();

	@RequestMapping("html/ht4.html")
	@ResponseBody
	public String ht4(){
		List<TreeNode> sessionTreeNode = permissionHandler.getSessionTreeNode();
		return gson.toJson(sessionTreeNode);
	}

	@RequestMapping("html/ht5.html")
	@ResponseBody
	public String ht5(){
		List<String> list = new ArrayList<>();
		list.add("/html/ht1.html");
		List<PermNode> simpleTreeNode = permissionHandler.getSimpleTreeNode(list);
		String s = gson.toJson(simpleTreeNode);
		System.out.println(s);
		return s;
	}
}
