package com.uulookingfor.ics.client.domain;

import lombok.Getter;

/**
 * @author suxiong.sx 
 */
public enum IcsClientErrCode {
	
	U_NULLSTAT_AFTER_PUT("U_NULLSTAT_AFTER_PUT", "config exists already dataId=%s  groupId=%s"),
	
	R_NOT_EXISTS("R_NOT_EXISTS", "config not exists dataId=%s  groupId=%s"),
	
	L_UNKNOW_SUBSCRIBE_INFO("L_UNKNOW_SUBSCRIBE_INFO", "config recognize subscribe info %s"),
	
	O_INVALID_PARAM("O_INVALID_PARAM", "invalid param, %s"),
	
	O_ZOOKEEPER_EXCEPTION("O_ZOOKEEPER_EXCEPTION", "zookeeper exception %s"),
	
	O_SERIALIZE_EXCEPTION("O_SERIALIZE_EXCEPTION", "serialize exception %s"),
	
	O_EXISTS_ALREADY("O_EXISTS_ALREADY", "config exists already dataId=%s  groupId=%s"),
	
	O_NOT_EXISTS("O_NOT_EXISTS", "config not exists dataId=%s  groupId=%s"),
	
	O_NOT_ACQUIRE_LOCK("O_NOT_ACIURE_LOCK", "can't acquire lock dataId=%s  groupId=%s"),
	
	O_CANT_GET_CONFIGMETA("O_CANT_GET_CONFIGMETA", "can't get config meta dataId=%s  groupId=%s"),
	
	O_CANT_UPDATE_CONFIGMETA("O_CANT_UPDATE_CONFIGMETA", "can't update config meta dataId=%s  groupId=%s aggrCount=%d"),
	;
	
	@Getter private String errCode ;
	
	@Getter private String errMsg ;
	
	private IcsClientErrCode(String errCode, String errMsg){
		
		this.errCode = errCode;
		
		this.errMsg = errMsg;
		
	}
	
	
}
