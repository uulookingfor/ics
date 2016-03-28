package com.uulookingfor.ics.client;

import com.uulookingfor.ics.domain.IcsConfig;
import com.uulookingfor.ics.domain.IcsListener;
import com.uulookingfor.ics.domain.IcsResult;

/**
 * @author suxiong.sx 
 */
public interface IcsClient{
	
	void init() throws Exception;
	
	IcsResult<Boolean> createNewConfig(String dataId, String groupId, boolean isAggr);
	
	IcsResult<Boolean> removeExistsConfig(String dataId, String groupId);
	
	IcsResult<IcsConfig> publishConfig(String dataId, String groupId, String content);
	
	IcsResult<IcsConfig> getConfig(String dataId, String groupId);
	
	IcsResult<Boolean> addListener(IcsListener listener);
	
}
