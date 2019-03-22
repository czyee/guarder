package org.czyee.guarder.annotated.interceptor;

import javax.servlet.http.HttpServletRequest;

public interface LoginChecker {

	/**
	 * 根据请求获取登录状态
	 * @param request 请求
	 * @return 是否已登录
	 */
	boolean checkLogin(HttpServletRequest request);

	/**
	 *
	 * @return ajax响应字符串
	 */
	String getLoginTimeOutTip();

	/**
	 * @return 普通请求重定向地址
	 */
	String getLoginUrl();
}
