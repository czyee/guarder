package org.czyee.guarder.session;

import java.util.*;

public class DefaultSessionService implements SessionService{

	private static final Map<String , SessionAttribute> map = new HashMap<>();

	private static final long expires = 1800000L;

	static {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				long currentTimeMillis = System.currentTimeMillis();
				Set<Map.Entry<String, SessionAttribute>> entries = map.entrySet();
				Iterator<Map.Entry<String, SessionAttribute>> iterator = entries.iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, SessionAttribute> entry = iterator.next();
					SessionAttribute value = entry.getValue();
					long addTime = value.addTime;
					if (addTime + expires < currentTimeMillis){
						iterator.remove();
					}
				}
			}
		};
		final Timer timer = new Timer();
		timer.schedule(timerTask,0L,60000L);
		Thread shutdownHook = new Thread(new Runnable() {
			@Override
			public void run() {
				timer.cancel();
			}
		});
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

	@Override
	public void setSessionKeyValue(String sessionId, String key, Object value) {
		SessionAttribute sessionAttribute = map.get(sessionId);
		if (sessionAttribute == null){
			sessionAttribute = new SessionAttribute();
			map.put(sessionId,sessionAttribute);
		}
		if (sessionAttribute.data == null){
			sessionAttribute.data = new HashMap<>();
		}
		sessionAttribute.data.put(key,value);
		sessionAttribute.addTime = System.currentTimeMillis();
	}

	@Override
	public Object getSessionValue(String sessionId, String key) {
		if (sessionId == null){
			return null;
		}
		SessionAttribute sessionAttribute = map.get(sessionId);
		if (sessionAttribute == null){
			return null;
		}
		long addTime = sessionAttribute.addTime;
		if (addTime + expires < System.currentTimeMillis()){
			return null;
		}
		if (sessionAttribute.data == null){
			return null;
		}
		return sessionAttribute.data.get(key);
	}

	@Override
	public void expire(String sessionId) {
		SessionAttribute sessionAttribute = map.get(sessionId);
		if (sessionAttribute != null){
			sessionAttribute.addTime = System.currentTimeMillis();
		}
	}

	@Override
	public void removeSessionValue(String sessionId, String key) {
		SessionAttribute sessionAttribute = map.get(sessionId);
		if (sessionAttribute == null){
			return;
		}
		if (sessionAttribute.data == null){
			return;
		}
		sessionAttribute.data.remove(key);
	}

	@Override
	public void deleteSession(String sessionId) {
		map.remove(sessionId);
	}

	private static class SessionAttribute{
		private Map<String,Object> data;
		private long addTime;
	}
}
