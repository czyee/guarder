package org.czyee.guarder.annotated.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class ResourceController {

	private String index = "index.html";

	public void setIndex(String index) {
		this.index = index;
	}

	private String contextPath;
	private int contextPathLength = 0;

	@Autowired
	public void setServletContext(ServletContext servletContext){
		String contextPath = servletContext.getContextPath();
		if (!"/".equals(contextPath) && !"".equals(contextPath)){
			this.contextPath = contextPath;
			this.contextPathLength = contextPath.length();
		}
	}

	/**
	 * 静态资源
	 * @param request
	 * @return
	 */
	public String source(HttpServletRequest request){
		String requestURI = request.getRequestURI();
		if (contextPath != null){
			requestURI = new String(requestURI.toCharArray(),contextPathLength,requestURI.length() - contextPathLength);
		}
		System.out.println(requestURI);
		return requestURI;
	}

	/**
	 * 返回首页
	 * @return
	 */
	public String index(){
		return index;
	}

}
