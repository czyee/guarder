package org.czyee.guarder.session;

import java.util.UUID;

public class DefautSessionIdGenerator implements SessionIdGenerator {

	/**
	 * 默认实现是uuid
	 * @return
	 */
	@Override
	public String createSessionId() {

		return UUID.randomUUID().toString();
	}
}
