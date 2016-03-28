package com.uulookingfor.ics.client.impl;

import static com.uulookingfor.ics.client.domain.IcsClientErrCode.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.data.Stat;

import lombok.NonNull;

import com.uulookingfor.ics.client.IcsClient;
import com.uulookingfor.ics.client.config.IcsClientConfigHolder;
import com.uulookingfor.ics.client.domain.IcsClientConstants;
import com.uulookingfor.ics.client.domain.IcsClientContext;
import com.uulookingfor.ics.client.log.IcsLogger;
import com.uulookingfor.ics.domain.IcsCharacter;
import com.uulookingfor.ics.domain.IcsConfig;
import com.uulookingfor.ics.domain.IcsConfigMeta;
import com.uulookingfor.ics.domain.IcsListener;
import com.uulookingfor.ics.domain.IcsListener.IcsSubscribeInfo;
import com.uulookingfor.ics.domain.IcsResult;
import com.uulookingfor.ics.util.Md5Util;

public class DefaultIcsClient implements IcsClient, IcsClientContext, IcsClientConstants{

	public synchronized void init() throws Exception{
		
		icsClinetConfig.init();	
		
		icsCuratorClient.init();
		
		
		//close when shut down
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			@Override
			public void run() {
				icsCuratorClient.stop();
			}
			
		}));
		
	}

	@Override
	public IcsResult<Boolean> createNewConfig(@NonNull String dataId, @NonNull String groupId, boolean isAggr) {

		IcsResult<Boolean> ret = new IcsResult<Boolean>();
		
		IcsResult<Boolean> paramRet = validParam(dataId, groupId);
		if(!paramRet.isSuccess()){
			
			return ret.fail(
					paramRet.getErrCode(), 
					paramRet.getErrMsg());
			
		}
		
		IcsConfigMeta configMeta = new IcsConfigMeta();
		configMeta.setAggr(isAggr);
		configMeta.setAggrCount(firstDatumId);
		
		return createNewConfig0(dataId, groupId, configMeta);
		
	}

	@Override
	public IcsResult<Boolean> removeExistsConfig(@NonNull String dataId, @NonNull String groupId) {
		
		IcsResult<Boolean> ret = new IcsResult<Boolean>();
		
		IcsResult<Boolean> paramRet = validParam(dataId, groupId);
		if(!paramRet.isSuccess()){
			
			return ret.fail(
					paramRet.getErrCode(), 
					paramRet.getErrMsg());
			
		}
		
		return removeExistsConfig0(dataId, groupId);
	}

	@Override
	public IcsResult<IcsConfig> publishConfig(@NonNull String dataId, @NonNull String groupId,
			@NonNull String content) {
		
		IcsResult<IcsConfig> ret = new IcsResult<IcsConfig>();
		
		IcsResult<Boolean> paramRet = validParam(dataId, groupId);
		if(!paramRet.isSuccess()){
			
			return ret.fail(
					paramRet.getErrCode(), 
					paramRet.getErrMsg());
			
		}
		
		return publisConfig0(dataId, groupId, buildIcsConfig(dataId, groupId, content));
		
	}

	@Override
	public IcsResult<IcsConfig> getConfig(@NonNull String dataId, @NonNull String groupId) {
		
		IcsResult<IcsConfig> ret = new IcsResult<IcsConfig>();
		
		IcsResult<IcsConfigMeta> configMetaRet = getConfigMeta0(dataId, groupId);
		
		if(!configMetaRet.isSuccess()){
			
			return ret.fail(
					configMetaRet.getErrCode(), 
					configMetaRet.getErrMsg());
		}
		
		if(configMetaRet.getModel().isAggr()){
			
			return getAggrConfig0(dataId, groupId);
			
		}else{
			
			return getDatumConfig0(dataId, groupId, String.valueOf(firstDatumId));
			
		}
		
	}

	@Override
	public IcsResult<Boolean> addListener(@NonNull final IcsListener listener) {
		
		IcsResult<Boolean> ret = new IcsResult<Boolean>();
		
		final IcsSubscribeInfo subscribeInfo = listener.subscribe();
		
		if(subscribeInfo == null){
			
			return ret.fail(
					L_UNKNOW_SUBSCRIBE_INFO.getErrCode(), 
					String.format(L_UNKNOW_SUBSCRIBE_INFO.getErrMsg(), "null"));
			
		}
		
		IcsResult<Boolean> validRet = validParam(subscribeInfo.getDataId(), subscribeInfo.getGroupId());
		
		if(!validRet.isSuccess()){
			
			return ret.fail(
					validRet.getErrCode(), 
					String.format(validRet.getErrMsg(), subscribeInfo.getDataId(), subscribeInfo.getGroupId()));
			
		}
		
		return addListenerOnce0(subscribeInfo, 
				watcher(subscribeInfo, listener));
		
	}
	
	private IcsResult<Boolean> createNewConfig0(String dataId, String groupId, IcsConfigMeta configMeta){
		
		IcsResult<Boolean> ret = new IcsResult<Boolean>();
		
		IcsResult<byte[]> serialRet = serialize(configMeta);
		
		if(!serialRet.isSuccess()){
			
			return ret.fail(
					serialRet.getErrCode(), 
					serialRet.getErrMsg());
			
		}
		
		try {
			
			String paramPath = path(dataId, groupId);
			
			icsCuratorClient
					.getCuratorClient()
					.create()
					.creatingParentsIfNeeded()
					.forPath(paramPath, serialRet.getModel());
			
		}catch(NodeExistsException existsException){
			
			return ret.fail(O_EXISTS_ALREADY.getErrCode(), 
						String.format(O_EXISTS_ALREADY.getErrMsg(), dataId, groupId));
			
		}catch (Exception e) {
			
			IcsLogger.clientLogger.error(O_ZOOKEEPER_EXCEPTION.getErrCode(), e);
			
			return ret.fail(O_ZOOKEEPER_EXCEPTION.getErrCode(), 
					String.format(O_ZOOKEEPER_EXCEPTION.getErrMsg(), e.getMessage()));
		}
		
		IcsResult<Boolean> notiferRet = createNotifier(dataId, groupId);
		
		if(!notiferRet.isSuccess()){
			
			removeExistsConfig(dataId, groupId);
			
			return ret.fail(
					notiferRet.getErrCode(), 
					notiferRet.getErrMsg());
			
		}
		
		return ret.success(true);
		
	}
	
	private IcsResult<Boolean> createNotifier(String dataId, String groupId){
		
		IcsResult<Boolean> ret = new IcsResult<Boolean>();
		
		String paramPath = path(dataId, groupId, notifier);
		
		String data = "";
		
		try {
			
			icsCuratorClient.getCuratorClient().create().creatingParentsIfNeeded().forPath(paramPath, data.getBytes());
			
			return ret.success(true);
			
		} catch(NodeExistsException existsException){
			
			return ret.success(true);
			
		}catch (Exception e) {
			
			IcsLogger.clientLogger.error(O_ZOOKEEPER_EXCEPTION.getErrCode(), e);
			
			return ret.fail(O_ZOOKEEPER_EXCEPTION.getErrCode(), 
					String.format(O_ZOOKEEPER_EXCEPTION.getErrMsg(), e.getMessage()));
		}
	}
	
	private IcsResult<Boolean> removeExistsConfig0(String dataId, String groupId){
		
		IcsResult<Boolean> ret = new IcsResult<Boolean>();
		
		try {
			
			icsCuratorClient.getCuratorClient().delete().deletingChildrenIfNeeded().forPath(path(dataId, groupId));
			
		}catch(NoNodeException noNodeException){
			
			return ret.fail(O_NOT_EXISTS.getErrCode(), 
						String.format(O_NOT_EXISTS.getErrMsg(), dataId, groupId));
			
		}catch (Exception e) {
			
			IcsLogger.clientLogger.error(O_ZOOKEEPER_EXCEPTION.getErrCode(), e);
			
			return ret.fail(O_ZOOKEEPER_EXCEPTION.getErrCode(), 
					String.format(O_ZOOKEEPER_EXCEPTION.getErrMsg(), e.getMessage()));
		}
		
		return ret.success(true);
	}
	
	private IcsResult<IcsConfigMeta> getConfigMeta0(String dataId, String groupId){
		
		IcsResult<IcsConfigMeta> ret = new IcsResult<IcsConfigMeta>();
		
		byte[] bytes = null;
		
		try {
			
			String paramPath = path(dataId, groupId);
			
			bytes = icsCuratorClient
					.getCuratorClient()
					.getData()
					.forPath(paramPath);
			
		} catch (Exception e) {
			
			IcsLogger.clientLogger.error(O_ZOOKEEPER_EXCEPTION.getErrCode(), e);
			
			return ret.fail(O_ZOOKEEPER_EXCEPTION.getErrCode(), 
					String.format(O_ZOOKEEPER_EXCEPTION.getErrMsg(), e.getMessage()));
		}
		
		return deSerialize(bytes, IcsConfigMeta.class);
	}
	
	private IcsResult<Boolean> updateConfigMeta0(String dataId, String groupId, IcsConfigMeta configMeta){
		
		IcsResult<Boolean> ret = new IcsResult<Boolean>();
		
		Long srcAggrCount = configMeta.getAggrCount();
		
		Long dstAggrCount = srcAggrCount + 1;
		
		configMeta.setAggrCount(dstAggrCount);
		
		IcsResult<byte[]> serialRet = serialize(configMeta);
		
		if(!serialRet.isSuccess()){
			
			return ret.fail(
					serialRet.getErrCode(), 
					serialRet.getErrMsg());
			
		}
		
		try {
			
			String paramPath = path(dataId, groupId);
			
			Stat stat = icsCuratorClient
					.getCuratorClient()
					.setData()
					.forPath(paramPath, serialRet.getModel());
			
			if(stat != null){
				
				return ret.success(true);
				
			}
			
			else{
				
				IcsLogger.clientLogger.error(O_CANT_UPDATE_CONFIGMETA.getErrCode() + " " 
						+ String.format(O_CANT_UPDATE_CONFIGMETA.getErrMsg(), dataId, groupId));
				
				return ret.fail(O_CANT_UPDATE_CONFIGMETA.getErrCode(), 
						String.format(O_CANT_UPDATE_CONFIGMETA.getErrMsg(), dataId, groupId));
				
			}
			
		}catch (Exception e) {
			
			IcsLogger.clientLogger.error(O_ZOOKEEPER_EXCEPTION.getErrCode(), e);
			
			return ret.fail(O_ZOOKEEPER_EXCEPTION.getErrCode(), 
					String.format(O_ZOOKEEPER_EXCEPTION.getErrMsg(), e.getMessage()));
		}
		
	}
	
	private IcsResult<IcsConfig> publisConfig0(String dataId, String groupId, IcsConfig config){
		
		IcsResult<IcsConfig> ret = new IcsResult<IcsConfig>();
		
		IcsResult<byte[]> serialRet = serialize(config);
		
		if(!serialRet.isSuccess()){
			
			return ret.fail(
					serialRet.getErrCode(), 
					serialRet.getErrMsg());
			
		}
		
		IcsResult<Long> aggrCountRet = getAggrCount(dataId, groupId);
			
		if(!aggrCountRet.isSuccess()){
			
			return ret.fail(
					aggrCountRet.getErrCode(), 
					aggrCountRet.getErrMsg());
		}
			
		String paramPath = path(dataId, groupId, String.valueOf(aggrCountRet.getModel()));
		
		try {
			
			icsCuratorClient.getCuratorClient().create().forPath(paramPath, serialRet.getModel());
			
			notify(dataId, groupId);

			return getConfig(dataId, groupId);
			
		} catch(NodeExistsException existsException){
			
			Stat stat = null;
			
			try {
				stat = icsCuratorClient.getCuratorClient().setData().forPath(paramPath, serialRet.getModel());
				
				if(stat != null){
					
					notify(dataId, groupId);
					
					return getConfig(dataId, groupId);
					
				}
				
				else{
					
					IcsLogger.clientLogger.error(U_NULLSTAT_AFTER_PUT.getErrCode() + " " 
							+ String.format(U_NULLSTAT_AFTER_PUT.getErrMsg(), dataId, groupId));
					
					return ret.fail(U_NULLSTAT_AFTER_PUT.getErrCode(), 
							String.format(U_NULLSTAT_AFTER_PUT.getErrMsg(), dataId, groupId));
					
				}
				
			} catch (Exception e) {
				
				IcsLogger.clientLogger.error(O_ZOOKEEPER_EXCEPTION.getErrCode(), e);
			
				return ret.fail(O_ZOOKEEPER_EXCEPTION.getErrCode(), 
						String.format(O_ZOOKEEPER_EXCEPTION.getErrMsg(), e.getMessage()));
				
			}
			
		} catch (Exception e) {
			
			IcsLogger.clientLogger.error(O_ZOOKEEPER_EXCEPTION.getErrCode(), e);
			
			return ret.fail(O_ZOOKEEPER_EXCEPTION.getErrCode(), 
					String.format(O_ZOOKEEPER_EXCEPTION.getErrMsg(), e.getMessage()));
			
		}
	}
	
	private IcsResult<IcsConfig> getDatumConfig0(String dataId, String groupId, String datumId) {
		
		IcsResult<IcsConfig> ret = new IcsResult<IcsConfig>();
		
		byte[] bytes = null;
		
		try {
			
			String paramPath = path(dataId, groupId, datumId);
			
			bytes = icsCuratorClient
					.getCuratorClient()
					.getData()
					.forPath(paramPath);
			
		} catch (Exception e) {
			
			IcsLogger.clientLogger.error(O_ZOOKEEPER_EXCEPTION.getErrCode(), e);
			
			return ret.fail(O_ZOOKEEPER_EXCEPTION.getErrCode(), 
					String.format(O_ZOOKEEPER_EXCEPTION.getErrMsg(), e.getMessage()));
		}
		
		return deSerialize(bytes, IcsConfig.class);
		
	}
	
	/**
	 * we don't lock here, than means : <br/>
	 * 
	 *  if some client add datum when invoke this method, may be miss it <br/>
	 * 
	 */
	private IcsResult<IcsConfig> getAggrConfig0(String dataId, String groupId) {
		
		IcsResult<IcsConfig> ret = new IcsResult<IcsConfig>();
		
		IcsResult<List<String>> datumListRet = getDatumList0(dataId, groupId);
		
		if(!datumListRet.isSuccess()){
			
			return ret.fail(
					datumListRet.getErrCode(), 
					datumListRet.getErrMsg());
			
		}
		
		List<IcsConfig> datumConfigs = new ArrayList<IcsConfig>();
		
		for(String datum : datumListRet.getModel()){
			
			if(!validDatum(datum)){
				continue;
			}
			
			IcsResult<IcsConfig> datumConfigRet = getDatumConfig0(dataId, groupId, datum);
			
			if(!datumConfigRet.isSuccess()){
				
				return ret.fail(
						datumConfigRet.getErrCode(), 
						datumConfigRet.getErrMsg());
			}
			
			datumConfigs.add(datumConfigRet.getModel());
		}
		
		return combineConfig(datumConfigs);
		
	}
	
	private IcsResult<List<String>> getDatumList0(String dataId, String groupId){
		
		IcsResult<List<String>> ret = new IcsResult<List<String>>();
		
		try {
			
			List<String> children = icsCuratorClient.getCuratorClient().getChildren().forPath(path(dataId, groupId));
			
			return ret.success(children);
					
		} catch (Exception e) {
			
			IcsLogger.clientLogger.error(O_ZOOKEEPER_EXCEPTION.getErrCode(), e);
			
			return ret.fail(O_ZOOKEEPER_EXCEPTION.getErrCode(), 
					String.format(O_ZOOKEEPER_EXCEPTION.getErrMsg(), e.getMessage()));
		}
		
	}
	
	private IcsResult<Boolean> addListenerOnce0(IcsSubscribeInfo subscribeInfo, CuratorWatcher watcher){
		
		IcsResult<Boolean> ret = new IcsResult<Boolean>();
		
		try {
			
			icsCuratorClient.getCuratorClient().getData().usingWatcher(watcher).forPath(path(subscribeInfo.getDataId(), subscribeInfo.getGroupId(), notifier));
			
		} catch (Exception e) {
			
			IcsLogger.clientLogger.error(O_ZOOKEEPER_EXCEPTION.getErrCode(), e);
			
			return ret.fail(O_ZOOKEEPER_EXCEPTION.getErrCode(), 
					String.format(O_ZOOKEEPER_EXCEPTION.getErrMsg(), e.getMessage()));
		}
		
		return ret.success(true);
		
	}
	private <T> IcsResult<byte[]> serialize(T model){
		
		IcsResult<byte[]> ret = new IcsResult<byte[]>();
		
		byte[] serialModel = null;
		
		try{
			
			serialModel = icsSerializer.serialize(model);
			
		}catch(Exception e){
			
			IcsLogger.clientLogger.error(O_SERIALIZE_EXCEPTION.getErrCode(), e);
			
			return ret.fail(O_SERIALIZE_EXCEPTION.getErrCode(), 
					String.format(O_SERIALIZE_EXCEPTION.getErrMsg(), e.getMessage()));
			
		}
		
		return ret.success(serialModel);
	}
	
	private <T> IcsResult<T> deSerialize(byte[] bytes, Class<T> clazz){
		
		IcsResult<T> ret = new IcsResult<T>();
		
		T model = null;
		
		try{
			
			model = icsSerializer.deSerialize(bytes, clazz);
			
		}catch(Exception e){
			
			IcsLogger.clientLogger.error(O_SERIALIZE_EXCEPTION.getErrCode(), e);
			
			return ret.fail(O_SERIALIZE_EXCEPTION.getErrCode(), 
					String.format(O_SERIALIZE_EXCEPTION.getErrMsg(), e.getMessage()));
			
		}
		
		return ret.success(model);
	}
	
	private String path(String dataId, String groupId){
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(IcsCharacter.Slash).append(dataId).append(IcsCharacter.Slash).append(groupId);
		
		return sb.toString();
		
		
	}
	
	private String path(String dataId, String groupId, String datumId){
		
		StringBuffer sb = new StringBuffer();
		
		sb
		.append(IcsCharacter.Slash).append(dataId)
		.append(IcsCharacter.Slash).append(groupId)
		.append(IcsCharacter.Slash).append(datumId);
		
		return sb.toString();
		
	}
	
	private IcsResult<Boolean> validParam(String dataId, String groupId){
		
		IcsResult<Boolean> ret = new IcsResult<Boolean>();
		
		if("".equals(dataId) || "".equals(dataId.trim())){
			
			return ret.fail(O_INVALID_PARAM.getErrCode(), 
					String.format(O_INVALID_PARAM.getErrMsg(), "dataId can't be empty"));
			
		}
		
		if("".equals(groupId) || "".equals(groupId.trim())){
			
			return ret.fail(O_INVALID_PARAM.getErrCode(), 
					String.format(O_INVALID_PARAM.getErrMsg(), "groupId can't be empty"));
			
		}
		
		if(dataId.contains(IcsCharacter.Slash)){
			
			return ret.fail(O_INVALID_PARAM.getErrCode(), 
					String.format(O_INVALID_PARAM.getErrMsg(), "dataId can't contain special charater /"));
			
		}
		
		if(groupId.contains(IcsCharacter.Slash)){
			
			return ret.fail(O_INVALID_PARAM.getErrCode(), 
					String.format(O_INVALID_PARAM.getErrMsg(), "groupId can't contain special charater /"));
			
		}
		
		return ret.success(true);
	}
	
	private IcsConfig buildIcsConfig(String dataId, 
			String groupId,
			String content){
		
		IcsConfig config = new IcsConfig();
		
		config.setDataId(dataId);
		config.setGroupId(groupId);
		config.setContent(content);
		config.setTimestamp(new Date().getTime());
		config.setMd5(Md5Util.getMd5String(content));
		
		return config;
		
	}
	
	private IcsResult<Long> getAggrCount(String dataId, String groupId){
		
		IcsResult<Long> ret = new IcsResult<Long>();
		
		Long aggrCount = -1L;
				
		InterProcessMutex lock = new InterProcessMutex(icsCuratorClient.getCuratorClient(), path(dataId, groupId, aggrCountLock));
		
		try{
			
			if(!lock.acquire(IcsClientConfigHolder.getInst().getLockTimeMs(), TimeUnit.MILLISECONDS)){
				
				return ret.fail(O_NOT_ACQUIRE_LOCK.getErrCode(), 
						String.format(O_NOT_ACQUIRE_LOCK.getErrMsg(), dataId, groupId));
				
			}
			
			IcsResult<IcsConfigMeta> configMetaRet = getConfigMeta0(dataId, groupId);
			
			if(!configMetaRet.isSuccess()){
				
				return ret.fail(O_CANT_GET_CONFIGMETA.getErrCode(), 
						String.format(O_CANT_GET_CONFIGMETA.getErrMsg(), dataId, groupId));
				
			}
			
			aggrCount = configMetaRet.getModel().getAggrCount();
			
			if(!configMetaRet.getModel().isAggr()){
				//just do nothing
			}else{
				
				IcsResult<Boolean> updateConfigMetaRet = updateConfigMeta0(dataId, groupId, configMetaRet.getModel());
				
				if(!updateConfigMetaRet.isSuccess()){
					
					return ret.fail(O_CANT_UPDATE_CONFIGMETA.getErrCode(), 
							String.format(O_CANT_UPDATE_CONFIGMETA.getErrMsg(), dataId, groupId, aggrCount + 1));
					
				}
			}
			
		}catch(Exception e){
			
			IcsLogger.clientLogger.error(O_ZOOKEEPER_EXCEPTION.getErrCode(), e);
			
			return ret.fail(O_ZOOKEEPER_EXCEPTION.getErrCode(), 
					String.format(O_ZOOKEEPER_EXCEPTION.getErrMsg(), e.getMessage()));
			
		}finally{
			
			try {
				
				lock.release();
				
			} catch (Exception e) {
				
				IcsLogger.clientLogger.error(O_ZOOKEEPER_EXCEPTION.getErrCode(), e);
			
				return ret.fail(O_ZOOKEEPER_EXCEPTION.getErrCode(), 
						String.format(O_ZOOKEEPER_EXCEPTION.getErrMsg(), e.getMessage()));
				
			}
			
		}
		
		return ret.success(aggrCount);
		
	}
	
	private boolean validDatum(String datum){
		
		if(aggrCountLock.equals(datum)){
			return false;
		}
		
		if(notifier.equals(datum)){
			return false;
		}
		
		return true;
	}
	
	private IcsResult<IcsConfig> combineConfig(List<IcsConfig> datumConfigs){
		
		IcsResult<IcsConfig> ret = new IcsResult<IcsConfig>();
		
		IcsConfig model = new IcsConfig();
		
		StringBuffer sb = new StringBuffer();
		
		model.setTimestamp(0);
		
		for(IcsConfig datumConfig : datumConfigs){
			
			model.setDataId(datumConfig.getDataId());
			
			model.setGroupId(datumConfig.getGroupId());
			
			sb.append(datumConfig.getContent());
			
			if(datumConfig.getTimestamp() > model.getTimestamp()){
				
				model.setTimestamp(datumConfig.getTimestamp());
				
			}

		}
		
		model.setContent(sb.toString());
		
		model.setMd5(Md5Util.getMd5String(model.getContent()));
		
		return ret.success(model);
		
	}
	
	private IcsResult<Boolean> notify(String dataId, String groupId){
		
		IcsResult<Boolean> ret = new IcsResult<Boolean>();
		
		String paramPath = path(dataId, groupId, notifier);
		
		String data = String.valueOf(System.nanoTime());
		
		try {
			
			icsCuratorClient.getCuratorClient().setData().forPath(paramPath, data.getBytes());
			
			return ret.success(true);
			
		}  catch (Exception e) {
			
			IcsLogger.clientLogger.error(O_ZOOKEEPER_EXCEPTION.getErrCode(), e);
			
			return ret.fail(O_ZOOKEEPER_EXCEPTION.getErrCode(), 
					String.format(O_ZOOKEEPER_EXCEPTION.getErrMsg(), e.getMessage()));
			
		}
		
	}
	
	private CuratorWatcher watcher(final IcsSubscribeInfo subscribeInfo, final IcsListener listener){
		
		return new CuratorWatcher(){

			@Override
			public void process(WatchedEvent event) throws Exception {
				
				IcsResult<Boolean> addListenerRet = addListenerOnce0(subscribeInfo, watcher(subscribeInfo, listener));
				
				if(!addListenerRet.isSuccess()){
					
					throw new RuntimeException(addListenerRet.getErrCode() + "-" + addListenerRet.getErrMsg());
					
				}
				
				if(event.getType().getIntValue() != EventType.NodeDataChanged.getIntValue()
						&& event.getType().getIntValue() != EventType.NodeChildrenChanged.getIntValue()){
					return;
				}
				
				IcsResult<IcsConfig> configRet = getConfig(subscribeInfo.getDataId(), subscribeInfo.getGroupId());
				
				if(configRet.isSuccess()){
					
					listener.reviceConfig(configRet.getModel());
					
				}else{
					
					IcsLogger.clientLogger.error("listener can't get config dataId=%s groupId=%s", subscribeInfo.getDataId(), subscribeInfo.getGroupId());
					
				}
				
			}
			
		};
	}
	
}
