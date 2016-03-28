package com.uulookingfor.ics.client.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author suxiong.sx 
 */
public class IcsClientConfigHolder {
	
	@Getter private static IcsClientConfigHolder inst = new IcsClientConfigHolder();
	
	@Getter @Setter private int config ;
	
	@Getter @Setter private String curatorConnectionString = "127.0.0.1:4861";
	
	@Getter @Setter private int curatorBaseSleepTimeMs = 1000;
	
	@Getter @Setter private int curatorMaxRetries = 3;
	
	@Getter @Setter private int lockTimeMs = 500;
	
	
}
