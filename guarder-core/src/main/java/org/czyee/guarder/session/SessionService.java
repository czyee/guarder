package org.czyee.guarder.session;

public interface SessionService {

	void setSessionKeyValue(String sessionId , String key , Object value);

	Object getSessionValue(String sessionId , String key);

	void expire(String sessionId);

	void removeSessionValue(String sessionId, String key);

	void deleteSession(String sessionId);
}
