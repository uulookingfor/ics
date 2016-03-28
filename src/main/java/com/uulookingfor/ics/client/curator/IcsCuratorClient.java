package com.uulookingfor.ics.client.curator;

import org.apache.curator.framework.CuratorFramework;

/**
 * @author suxiong.sx 
 */
public interface IcsCuratorClient {

	void init();
	
	void stop();
	
	CuratorFramework getCuratorClient();
	
}
