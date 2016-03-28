package com.uulookingfor.ics.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.uulookingfor.ics.client.domain.IcsClientContext;
import com.uulookingfor.ics.client.impl.DefaultIcsClient;
import com.uulookingfor.ics.domain.IcsConfig;
import com.uulookingfor.ics.domain.IcsListener;

public class Main implements IcsClientContext{
	
	public static void main(String[] args) throws Exception{
		
		Gson gson = new Gson();
		
		IcsClient icsClient = new DefaultIcsClient();
		
		icsClient.init();
		
//		System.out.println(gson.toJson(icsClient.createNewConfig("dataId3", "groupId1", false)));
		
//		System.out.println(gson.toJson(icsClient.createNewConfig("dataId3", "groupId2", true)));
		
//		System.out.println(gson.toJson(icsClient.removeExistsConfig("dataId3", "groupId2")));
		
//		System.out.println(gson.toJson(icsClient.publishConfig("dataId3", "groupId1", "ics hello world 31 again")));
		
//		System.out.println(gson.toJson(icsClient.publishConfig("dataId3", "groupId2", "ip5")));
		
//		System.out.println(gson.toJson(icsClient.getConfig("dataId3", "groupId2")));
		
//		testListener(icsClient, "dataId3", "groupId2");
	}
	
	private static void testListener(IcsClient icsClient, final String dataId, final String groupId){
		
		final Gson gson = new Gson();
		
		System.out.println(gson.toJson(icsClient.addListener(new IcsListener(){

			@Override
			public IcsSubscribeInfo subscribe() {
				
				return new IcsSubscribeInfo(dataId, groupId);
				
			}

			@Override
			public void reviceConfig(IcsConfig config) {
				
				System.out.println("from listener : " + gson.toJson(config));
			}
			
			
		})));
		
		BufferedReader buf = new BufferedReader(new InputStreamReader(System.in)); 
		
		for(;;){
			try {
				
				String line = buf.readLine();
				
				if("quit".equals(line)){
					break;
				}
				
				System.out.println(gson.toJson(icsClient.publishConfig(dataId, groupId, line)));
				
			} catch (IOException e) {
				
				e.printStackTrace();
				
			}
		}
	}
		
	
}
