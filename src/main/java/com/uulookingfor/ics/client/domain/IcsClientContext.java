package com.uulookingfor.ics.client.domain;

import com.uulookingfor.ics.client.config.IcsClientConfig;
import com.uulookingfor.ics.client.curator.IcsCuratorClient;
import com.uulookingfor.ics.client.curator.impl.DefaultIcsCuratorClient;
import com.uulookingfor.ics.serialize.Serializer;
import com.uulookingfor.ics.serialize.impl.ProtoStuffSerializer;

public interface IcsClientContext {

	IcsClientConfig icsClinetConfig = new IcsClientConfig();
	
	IcsCuratorClient icsCuratorClient = new DefaultIcsCuratorClient();
	
	Serializer icsSerializer  = new ProtoStuffSerializer();
	
}
