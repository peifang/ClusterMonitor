package com.intel.fangpei.network;

import java.io.IOException;

public interface INIOHandler {
	public void processConnect() throws IOException;

	public void processRead() throws IOException;

	public void processWrite() throws IOException;

	public void processError(Exception e);
}
