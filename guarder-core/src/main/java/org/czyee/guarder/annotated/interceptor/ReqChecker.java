package org.czyee.guarder.annotated.interceptor;

import javax.servlet.http.HttpServletRequest;

public interface ReqChecker {

	ReqChecker DEFAULT_REQ_CHECKER = new ReqChecker() {
		/**
		 * 默认所有post请求均视为ajax请求
		 * @param request
		 * @return
		 */
		@Override
		public boolean ajax(HttpServletRequest request) {
			String method = request.getMethod();
			return method.equalsIgnoreCase("post");
		}
	};

	/**
	 * 确定请求是否是ajax请求,ajax响应消息为字符串,非ajax会重定向
	 * @param request
	 * @return
	 */
	boolean ajax(HttpServletRequest request);
}
