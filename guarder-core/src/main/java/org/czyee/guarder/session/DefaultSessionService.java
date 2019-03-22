package org.czyee.guarder.session;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;

public class DefaultSessionService implements SessionService, DisposableBean, InitializingBean {

	private final Map<String , SessionAttribute> map = Collections.synchronizedMap(new TreeMap<>());

	/**
	 * 默认过期时间1800秒,半小时,不允许修改
	 */
	private final long expires = 1000L * 1800L;

	/**
	 * 用来整理内存的定时器
	 */
	private Timer timer;

	/**
	 * 实例化完成后启动定时器
	 */
	@Override
	public void afterPropertiesSet() {
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
						//置空
						entry.setValue(null);
						iterator.remove();
					}
				}
			}
		};
		timer = new Timer();
		//每分钟执行一次
		timer.schedule(timerTask,0L,1000L * 60L);
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

	@Override
	public List<String> findSessionIdByKeyValue(String key, Object value) {
		if (key == null || value == null){
			return null;
		}
		List<String> sessionIds = null;

		for (Map.Entry<String, SessionAttribute> entry : map.entrySet()) {
			SessionAttribute sessionAttribute = entry.getValue();
			if (sessionAttribute == null || sessionAttribute.data == null){
				continue;
			}
			if (value.equals(sessionAttribute.data.get(key))){
				if (sessionIds == null){
					sessionIds = new ArrayList<>();
				}
				sessionIds.add(entry.getKey());
			}
		}
		return sessionIds;
	}

	@Override
	public void destroy() throws Exception{
		timer.cancel();
	}

	private static class SessionAttribute{
		private Map<String,Object> data;
		private long addTime;
	}
}
