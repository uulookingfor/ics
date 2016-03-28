package com.uulookingfor.ics.serialize;

import com.uulookingfor.ics.serialize.domain.IcsSerializeException;

/**
 * @author suxiong.sx 
 */
public interface Serializer {
	
	<T> byte[] serialize(T obj);
	
	<T> T deSerialize(byte[] para, Class<T> clazz) throws IcsSerializeException;
}
