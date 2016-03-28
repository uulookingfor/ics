package com.uulookingfor.ics.client.curator.impl;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.uulookingfor.ics.client.config.IcsClientConfigHolder;
import com.uulookingfor.ics.client.curator.IcsCuratorClient;

public class DefaultIcsCuratorClient implements IcsCuratorClient{
	
	private static CuratorFramework curatorClient;
	
	private static boolean inited;
	
	public synchronized void init(){
		
		if(inited){
			return;
		}
		
		curatorClient = createSimple();  
		
		curatorClient.start();
		
		inited = true;
	}
	
	public synchronized void stop(){
		
		if(inited || curatorClient != null){
			
			curatorClient.close();
			
			inited = false;
			
			curatorClient = null;
			
		}
	}
	
	private CuratorFramework createSimple(){
		
		ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(
				IcsClientConfigHolder.getInst().getCuratorBaseSleepTimeMs(), 
				IcsClientConfigHolder.getInst().getCuratorMaxRetries()
				);

        return CuratorFrameworkFactory.newClient(IcsClientConfigHolder.getInst().getCuratorConnectionString(), retryPolicy);
        
    }

	@Override
	public CuratorFramework getCuratorClient() {
		
		return curatorClient;
		
	}
}
