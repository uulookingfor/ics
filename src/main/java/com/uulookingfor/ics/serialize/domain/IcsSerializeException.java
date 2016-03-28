package com.uulookingfor.ics.serialize.domain;

/**
 * @author suxiong.sx 
 */
public class IcsSerializeException extends Exception{

	private static final long serialVersionUID = -3404582893017334314L;
	
	public IcsSerializeException() {
        super();
    }
	
	public IcsSerializeException(String message) {
        super(message);
    }
	
	public IcsSerializeException(Throwable cause) {
        super(cause);
    }
	
	public IcsSerializeException(String message, Throwable cause) {
        super(message, cause);
    }
}
