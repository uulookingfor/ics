package com.uulookingfor.ics.domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * @author suxiong.sx 
 */
@ToString
public class IcsResult<T> implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@Getter @Setter private boolean success = false;
	
	@Getter @Setter private T model;
	
	@Getter @Setter private String errCode;
	
	@Getter @Setter private String errMsg;
	
	public IcsResult<T> fail(String errCode, String errMsg){
		
		this.success = false;
		this.errCode = errCode;
		this.errMsg = errMsg;
		
		return this;
	}
	
	public IcsResult<T> success(T model){
		
		this.success = true;
		this.model = model;
		
		return this;
	}
	
}

