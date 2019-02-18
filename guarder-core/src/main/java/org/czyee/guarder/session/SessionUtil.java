package org.czyee.guarder.session;

import org.springframework.context.ApplicationContext;

public class SessionUtil {

	private static final ThreadLocal<String> THREAD_SESSION = new ThreadLocal<>();

	private static SessionService sessionService;

	private static boolean init = false;


	public static void init(ApplicationContext applicationContext){
		if (!init){
			sessionService = applicationContext.getBean(SessionService.class);
		}
		init = true;
	}

	public static void setSession(String sessionId){
		if (sessionId == null){
			throw new RuntimeException("");
		}
		sessionService.expire(sessionId);
		THREAD_SESSION.set(sessionId);
	}

	public static boolean hasSession(){
		return THREAD_SESSION.get() != null;
	}

	public static Object getAttribute(String key){
		return sessionService.getSessionValue(THREAD_SESSION.get(),key);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getAttribute(String key , Class<T> clazz){
		Object sessionValue = getAttribute(key);
		T t = null;
		try {
			t = (T) sessionValue;
		}catch (Exception e){
			//do nothing
		}
		return t;
	}

	public static void logout(){
		sessionService.deleteSession(THREAD_SESSION.get());
	}

	public static void setAttribute(String key , Object value){
		sessionService.setSessionKeyValue(THREAD_SESSION.get(),key,value);
	}

	public static void removeAttribute(String key) {
		sessionService.removeSessionValue(THREAD_SESSION.get(),key);
	}

	public static void clear(){
		THREAD_SESSION.remove();
	}
}
