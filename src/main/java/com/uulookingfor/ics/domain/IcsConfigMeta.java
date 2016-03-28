package com.uulookingfor.ics.domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class IcsConfigMeta implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Getter @Setter private boolean isAggr = false;
	
	@Getter @Setter private long aggrCount = 0;
	
}
