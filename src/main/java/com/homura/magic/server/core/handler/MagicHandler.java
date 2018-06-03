package com.homura.magic.server.core.handler;

import com.homura.magic.server.core.MagicRequest;
import com.homura.magic.server.core.MagicResponse;

public interface MagicHandler {

	void handle(MagicRequest request, MagicResponse response) throws Exception;

	public static class NotFoundHandler implements MagicHandler {

		@Override
		public void handle(MagicRequest request, MagicResponse response) throws Exception {
			// TODO Auto-generated method stub

		}

	}

	public static class InternalErrorHandler implements MagicHandler {

		@Override
		public void handle(MagicRequest request, MagicResponse response) throws Exception {
			// TODO Auto-generated method stub

		}

	}
}
