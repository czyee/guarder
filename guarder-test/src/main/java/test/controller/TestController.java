package test.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("test")
public class TestController {


	public TestController(){
		System.out.println("construct TestController");
	}

	@RequestMapping("aaa.html")
	@ResponseBody
	public String index(){
		return "index.html";
	}

	@RequestMapping("test1.html")
	public String test1(Integer id){
		if (id != null && id == 10){
			return "html/ht1.html";
		}else {
			return "index.html";
		}
	}
}
