package com.uulookingfor.ics.domain;

public interface IcsListener {
	
	IcsSubscribeInfo subscribe(); 
	
	void reviceConfig(IcsConfig config);
	
	public static final class IcsSubscribeInfo{
		
		private String dataId;
		
		private String groupId;

		public IcsSubscribeInfo(){}
		
		public IcsSubscribeInfo(String dataId, String groupId){
			
			this.dataId = dataId;
			
			this.groupId = groupId;
			
		}
		
		public String getDataId() {
			return dataId;
		}

		public void setDataId(String dataId) {
			this.dataId = dataId;
		}

		public String getGroupId() {
			return groupId;
		}

		public void setGroupId(String groupId) {
			this.groupId = groupId;
		}
		
		public String toString(){
			
			StringBuffer sb = new StringBuffer();
			
			sb.append(dataId).append(IcsCharacter.Slash).append(groupId);
			
			return sb.toString();
		}
	}
	
}
