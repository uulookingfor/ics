package com.uulookingfor.ics.domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * @author suxiong.sx 
 */
public class IcsConfig implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@Getter @Setter private String dataId;
	
	@Getter @Setter private String groupId;
	
	@Getter @Setter private String content;
	
	@Getter @Setter private String md5;
	
	@Getter @Setter private long timestamp;
	
}
