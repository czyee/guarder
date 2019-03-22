package org.czyee.guarder.annotated.interceptor;

import org.czyee.guarder.annotated.annotation.Perm;
import org.czyee.guarder.annotated.controller.ResourceController;
import org.czyee.guarder.annotated.permission.PermissionHandler;
import org.czyee.guarder.session.SessionIdGenerator;
import org.czyee.guarder.session.SessionUtil;
import org.czyee.guarder.util.CookieUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class GuarderInterceptor implements HandlerInterceptor, InitializingBean {

	@Autowired
	private ResourceController resourceController;

	private String cookieName = "GSESSIONID";

	private SessionIdGenerator sessionIdGenerator;

	private String projectDomain;

	private PermissionHandler permissionHandler;

	private String denyTip;

	private String denyPage;

	private LoginChecker loginChecker;

	private ReqChecker reqChecker;

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

	public void setDenyPage(String denyPage) {
		this.denyPage = denyPage;
	}

	public void setLoginChecker(LoginChecker loginChecker) {
		this.loginChecker = loginChecker;
	}

	public void setReqChecker(ReqChecker reqChecker) {
		this.reqChecker = reqChecker;
	}

	@Override
	public void afterPropertiesSet() {
		if (reqChecker == null){
			reqChecker = ReqChecker.DEFAULT_REQ_CHECKER;
		}
	}

	private void initSession(HttpServletRequest request, HttpServletResponse response){
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
		//没有perm注解视为不要登录即可访问,更不需要权限
		if (perm == null){
			return true;
		}
		//有perm注解即视为需要登录,在有登录校验器的情况下需要验登录状态
		if (loginChecker != null){
			boolean login = loginChecker.checkLogin(request);
			if (!login){
				//未登录,返回登录超时提示
				String loginTimeOutTip = loginChecker.getLoginTimeOutTip();
				String loginUrl = loginChecker.getLoginUrl();
				if (reqChecker.ajax(request)){
					responseText(response, loginTimeOutTip);
				}else {
					try {
						response.sendRedirect(loginUrl);
					} catch (IOException e) {
						//do nothing
					}
				}
				return false;
			}
		}
		//没有登录校验器或者登录校验通过的情况下,需要校验会话权限
		boolean canAccess = permissionHandler.canAccess(perm);
		if (canAccess){
			return true;
		}
		if (denyPage != null){
			if (reqChecker.ajax(request)){
				responseText(response,denyTip);
			}else {
				try {
					response.sendRedirect(denyPage);
				} catch (IOException e) {
					//do nothing
				}
			}
		}else {
			responseText(response,denyTip);
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

	private void responseText(HttpServletResponse response , String text){
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
			writer.print(text);
			writer.flush();
		} catch (Exception e){
			//do nothing
		}finally {
			if (writer != null){
				writer.close();
			}
		}
	}
}
