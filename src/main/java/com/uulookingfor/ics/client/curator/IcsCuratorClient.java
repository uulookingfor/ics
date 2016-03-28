package com.uulookingfor.ics.client.curator;

import org.apache.curator.framework.CuratorFramework;

public interface IcsCuratorClient {

	void init();
	
	void stop();
	
	CuratorFramework getCuratorClient();
	
}
