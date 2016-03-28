package com.uulookingfor.ics.domain;

import lombok.Getter;
import lombok.Setter;

public class IcsZnodeData {
	
	@Getter @Setter private IcsConfig config;
	
	@Getter @Setter private IcsConfigMeta configMeta;
	
	public IcsZnodeData(){}
	
	public IcsZnodeData(IcsConfig config, IcsConfigMeta configMeta){
		
		this.config = config;
		this.configMeta = configMeta;
		
	}
}