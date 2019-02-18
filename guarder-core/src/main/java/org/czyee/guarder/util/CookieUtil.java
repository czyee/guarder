package org.czyee.guarder.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {

	public static Cookie getCookie(HttpServletRequest request, String key){
		if (request == null){
			return null;
		}
		Cookie[] cookies = request.getCookies();
		if (cookies == null){
			return null;
		}
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(key)){
				return cookie;
			}
		}
		return null;
	}

	public static String getCookieValue(HttpServletRequest request , String key){
		Cookie cookie = getCookie(request,key);
		if (cookie == null){
			return null;
		}
		return cookie.getValue();
	}
}
