package com.uulookingfor.ics.client.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.beanutils.BeanUtils;

import com.uulookingfor.ics.client.domain.IcsClientConstants;

public class IcsClientConfig implements IcsClientConstants{

	private boolean inited = false;
	
	public synchronized void init() throws Exception{
		
		if(inited){
			return;
		}
		
		//init here...
		initFrom(configFilePath, IcsClientConfigHolder.getInst());
		
		inited = true;
		
	}

	
	private IcsClientConfigHolder initFrom(String configFilePath, IcsClientConfigHolder inst) throws Exception{
		
		Properties props = new Properties();
		
		InputStream inStream = getClass().getResourceAsStream(configFilePath);
		
		try {
			
			props.load(inStream);
			
		} catch (IOException e) {
			
			throw e;
			
		};
		
		
		inStream.close();
		
		BeanUtils.populate(inst, toMap(props));
		
		return inst;
	}
	
	private Map<String, Object> toMap(Properties props){
		
		Map<String, Object> ret = new HashMap<String, Object>();
		
		for(Entry<Object, Object> entry : props.entrySet()){
			
			ret.put(String.valueOf(entry.getKey()), entry.getValue());
			
		}
		
		return ret;
	}
	
}
