package org.czyee.guarder.annotated.interceptor;

import javax.servlet.http.HttpServletRequest;

public interface LoginChecker {

	boolean checkLogin(HttpServletRequest request);

	String getLoginTimeOutTip();
}
