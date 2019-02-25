package org.czyee.guarder.annotated.interceptor;

import org.czyee.guarder.annotated.annotation.Perm;
import org.czyee.guarder.annotated.controller.ResourceController;
import org.czyee.guarder.annotated.permission.PermissionHandler;
import org.czyee.guarder.session.SessionIdGenerator;
import org.czyee.guarder.session.SessionUtil;
import org.czyee.guarder.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public class GuarderInterceptor implements HandlerInterceptor {

	@Autowired
	private ResourceController resourceController;

	private String cookieName = "GSESSIONID";

	private SessionIdGenerator sessionIdGenerator;

	private String projectDomain;

	private PermissionHandler permissionHandler;

	private String denyTip;

	public void setPermissionHandler(PermissionHandler permissionHandler) {
		this.permissionHandler = permissionHandler;
	}

	public void setDenyTip(String denyTip) {
		this.denyTip = denyTip;
	}

	public void setSessionIdGenerator(SessionIdGenerator sessionIdGenerator) {
		if (sessionIdGenerator == null){
			throw new IllegalArgumentException("sessionIdGenerator can't be null");
		}
		this.sessionIdGenerator = sessionIdGenerator;
	}

	public void setProjectDomain(String projectDomain) {
		if (projectDomain != null){
			this.projectDomain = projectDomain.trim();
		}
	}

	public void setCookieName(String cookieName) {
		if (cookieName == null){
			throw new IllegalArgumentException("cookieName can't be null");
		}
		this.cookieName = cookieName;
	}

	private void initSession(HttpServletRequest request,HttpServletResponse response){
		if (SessionUtil.hasSession()){
			return;
		}
		String sessionId = CookieUtil.getCookieValue(request, cookieName);
		if (sessionId == null){
			sessionId = sessionIdGenerator.createSessionId();
			Cookie cookie = new Cookie(cookieName, sessionId);
			if (projectDomain != null){
				cookie.setDomain(projectDomain);
			}else {
				cookie.setPath("/");
			}
			response.addCookie(cookie);
		}
		SessionUtil.setSession(sessionId);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		//初始化会话
		initSession(request,response);
		if (!(handler instanceof HandlerMethod)){
			return true;
		}
		HandlerMethod handlerMethod = (HandlerMethod) handler;
		Object bean = handlerMethod.getBean();
		//resourceController直接放行
		if (bean == resourceController){
			return true;
		}
		Perm perm = handlerMethod.getMethodAnnotation(Perm.class);
		if (perm == null){
			return true;
		}
		boolean canAccess = permissionHandler.canAccess(perm);
		if (canAccess){
			return true;
		}
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
			writer.print(denyTip);
			writer.flush();
		} catch (Exception e){
			//do nothing
		}finally {
			if (writer != null){
				writer.close();
			}
		}
		return false;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {

	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		//结束后清空会话
		SessionUtil.clear();
	}
}
