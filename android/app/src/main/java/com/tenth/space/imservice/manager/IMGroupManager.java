package com.tenth.space.imservice.manager;


import com.google.protobuf.CodedInputStream;
import com.tenth.space.DB.DBInterface;
import com.tenth.space.DB.entity.GroupEntity;
import com.tenth.space.DB.entity.SessionEntity;
import com.tenth.space.DB.entity.UserEntity;
import com.tenth.space.config.DBConstant;
import com.tenth.space.imservice.callback.Packetlistener;
import com.tenth.space.imservice.event.GroupEvent;
import com.tenth.space.imservice.event.SessionEvent;
import com.tenth.space.protobuf.IMBaseDefine;
import com.tenth.space.protobuf.IMGroup;
import com.tenth.space.protobuf.IMSystem;
import com.tenth.space.protobuf.helper.EntityChangeEngine;
import com.tenth.space.protobuf.helper.ProtoBuf2JavaBean;
import com.tenth.space.utils.IMUIHelper;
import com.tenth.space.utils.Logger;
import com.tenth.space.utils.pinyin.PinYin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.greenrobot.event.EventBus;


public class IMGroupManager extends IMManager {
    private Logger logger = Logger.getLogger(IMGroupManager.class);
    private static IMGroupManager inst = new IMGroupManager();
    private boolean isStrenger;

    public static IMGroupManager instance() {
        return inst;
    }

    // 依赖的服务管理
    private IMSocketManager imSocketManager = IMSocketManager.instance();
    private IMLoginManager imLoginManager=IMLoginManager.instance();
    private DBInterface dbInterface = DBInterface.instance();


    // todo Pinyin的处理
    //正式群,临时群都会有的，存在竞争 如果不同时请求的话
    private Map<String,GroupEntity> groupMap = new ConcurrentHashMap<>();
    // 群组状态
    private boolean isGroupReady = false;

    @Override
    public void doOnStart() {
        groupMap.clear();
    }

    public void onNormalLoginOk(){
        onLocalLoginOk();
        onLocalNetOk();
    }

    /**
     * 1. 加载本地信息
     * 2. 请求正规群信息 ， 与本地进行对比
     * 3. version groupId 请求
     * */
    public void onLocalLoginOk(){
        logger.i("group#loadFromDb");

        if(!EventBus.getDefault().isRegistered(inst)){
            EventBus.getDefault().registerSticky(inst);
        }

        // 加载本地group
        List<GroupEntity> localGroupInfoList = dbInterface.loadAllGroup();
        for(GroupEntity groupInfo:localGroupInfoList){
            groupMap.put(groupInfo.getPeerId(),groupInfo);
        }

        triggerEvent(new GroupEvent(GroupEvent.Event.GROUP_INFO_OK));
    }

    public void onLocalNetOk(){

        GroupEntity publicGroup = IMLoginManager.instance().getPublicGroup();
        if(null != publicGroup && publicGroup.getlistGroupMemberIds().contains(imLoginManager.getPub_key()))
            DBInterface.instance().insertOrUpdateGroup(publicGroup);

        reqGetNormalGroupList();

    }

    @Override
    public void reset() {
        isGroupReady =false;
        groupMap.clear();
        EventBus.getDefault().unregister(inst);
    }

    public void onEvent(SessionEvent event){
        switch (event){
            case RECENT_SESSION_LIST_UPDATE:
                // groupMap 本地已经加载完毕之后才触发
                loadSessionGroupInfo();
                break;
        }
    }

    /**
     * 实现自身的事件驱动
     * @param event
     */
    public  synchronized void triggerEvent(GroupEvent event) {
        switch (event.getEvent()){
            case GROUP_INFO_OK:
                isGroupReady = true;
                break;
            case GROUP_INFO_UPDATED:
                isGroupReady = true;
                break;
        }
        EventBus.getDefault().postSticky(event);
    }

    /**---------------事件驱动end------------------------------*/

    /**
     * 1. 加载本地信息
     * 2. 从session中获取 群组信息，从本地中获取这些群组的version信息
     * 3. 合并上述的merge结果， version groupId 请求
     * */
    private void loadSessionGroupInfo(){
        logger.i("group#loadSessionGroupInfo");

        List<SessionEntity> sessionInfoList =   IMSessionManager.instance().getRecentSessionList();

        List<IMBaseDefine.GroupVersionInfo> needReqList = new ArrayList<>();
        for(SessionEntity sessionInfo:sessionInfoList){
            int version = 0;

            if(sessionInfo.getPeerType() == DBConstant.SESSION_TYPE_GROUP ){
                if(groupMap.containsKey(sessionInfo.getPeerId())){
                    version = groupMap.get(sessionInfo.getPeerId()).getVersion();
                }

                IMBaseDefine.GroupVersionInfo versionInfo = IMBaseDefine.GroupVersionInfo.newBuilder()
                        .setVersion(version)
                        .setGroupId(sessionInfo.getPeerId())
                        .build();
                needReqList.add(versionInfo);
            }

        }
        // 事件触发的时候需要注意
        if(needReqList.size() >0){
            reqGetGroupDetailInfo(needReqList);
            return ;
        }
    }

    /**
     * 联系人页面正式群的请求
     * todo 正式群与临时群逻辑上的分开的，但是底层应该是想通的
     */
    private void reqGetNormalGroupList() {
        logger.i("group#reqGetNormalGroupList");
        String loginId = imLoginManager.getPub_key();
        IMGroup.IMNormalGroupListReq  normalGroupListReq = IMGroup.IMNormalGroupListReq.newBuilder()
                .setUserId(loginId)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_GROUP_VALUE;
        int cid = IMBaseDefine.GroupCmdID.CID_GROUP_NORMAL_LIST_REQUEST_VALUE;
        imSocketManager.sendRequest(normalGroupListReq,sid,cid);
        logger.i("group#send packet to server");
    }

    public void onRepNormalGroupList(IMGroup.IMNormalGroupListRsp normalGroupListRsp) {
        logger.i("group#onRepNormalGroupList");
        int groupSize = normalGroupListRsp.getGroupVersionListCount();
        logger.i("group#onRepNormalGroupList cnt:%d",groupSize);
        List<IMBaseDefine.GroupVersionInfo> versionInfoList =  normalGroupListRsp.getGroupVersionListList();

        /**对比DB中的version字段*/
        // 这块对比的可以抽离出来
        List<IMBaseDefine.GroupVersionInfo> needInfoList = new ArrayList<>();

        for(IMBaseDefine.GroupVersionInfo groupVersionInfo:versionInfoList ){
            String groupId =  groupVersionInfo.getGroupId();
            int version =  groupVersionInfo.getVersion();
            if(groupMap.containsKey(groupId) &&
                    groupMap.get(groupId).getVersion() ==version ){
                continue;
            }
            IMBaseDefine.GroupVersionInfo versionInfo = IMBaseDefine.GroupVersionInfo.newBuilder()
                    .setVersion(0)
                    .setGroupId(groupId)
                    .build();
            needInfoList.add(versionInfo);
        }

        // 事件触发的时候需要注意 todo
        if(needInfoList.size() >0){
            reqGetGroupDetailInfo(needInfoList);
        }
    }

    public void  reqGroupDetailInfo(String groupId,boolean isStrengGroup){
        isStrenger = isStrengGroup;
        IMBaseDefine.GroupVersionInfo groupVersionInfo = IMBaseDefine.GroupVersionInfo.newBuilder()
                .setGroupId(groupId)
                .setVersion(0)
                .build();
        ArrayList<IMBaseDefine.GroupVersionInfo> list = new ArrayList<>();
        list.add(groupVersionInfo);
        reqGetGroupDetailInfo(list);
    }
    public void  reqGroupDetailInfo(String groupId){
        IMBaseDefine.GroupVersionInfo groupVersionInfo = IMBaseDefine.GroupVersionInfo.newBuilder()
                .setGroupId(groupId)
                .setVersion(0)
                .build();
        ArrayList<IMBaseDefine.GroupVersionInfo> list = new ArrayList<>();
        list.add(groupVersionInfo);
        reqGetGroupDetailInfo(list);
    }

    /**
     * 请求群组的详细信息
     */
    public void reqGetGroupDetailInfo(List<IMBaseDefine.GroupVersionInfo> versionInfoList){
        logger.i("group#reqGetGroupDetailInfo");
        if(versionInfoList == null || versionInfoList.size()<=0){
            logger.e("group#reqGetGroupDetailInfo# please check your params,cause by empty/null");
            return ;
        }
        String loginId = imLoginManager.getPub_key();
        IMGroup.IMGroupInfoListReq  groupInfoListReq = IMGroup.IMGroupInfoListReq.newBuilder()
                .setUserId(loginId)
                .addAllGroupVersionList(versionInfoList)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_GROUP_VALUE;
        int cid = IMBaseDefine.GroupCmdID.CID_GROUP_INFO_REQUEST_VALUE;
        imSocketManager.sendRequest(groupInfoListReq,sid,cid);
    }


    public void onRepGroupDetailInfo(IMGroup.IMGroupInfoListRsp groupInfoListRsp){
        logger.i("group#onRepGroupDetailInfo");
        int groupSize = groupInfoListRsp.getGroupInfoListCount();
        String userId = groupInfoListRsp.getUserId();
        String loginId = imLoginManager.getPub_key();
        logger.i("group#onRepGroupDetailInfo cnt:%d",groupSize);
        if(groupSize <=0 || !userId.equals(loginId)){
            logger.i("group#onRepGroupDetailInfo size empty[%d] or userid[%s]≠ loginId[%s]", groupSize,userId,loginId);
            return;
        }
        ArrayList<GroupEntity>  needDb = new ArrayList<>();
        if(isStrenger){
            //按id查找陌生群组只有一条
            IMBaseDefine.GroupInfo groupInfo = groupInfoListRsp.getGroupInfoListList().get(0);
            GroupEntity groupEntity = ProtoBuf2JavaBean.getGroupEntity(groupInfo);
            triggerEvent(new GroupEvent(GroupEvent.Event.STRENGE_GROUP,groupEntity));
            isStrenger=false;
            return;
        }

        for(IMBaseDefine.GroupInfo groupInfo:groupInfoListRsp.getGroupInfoListList()){
            GroupEntity groupEntity = ProtoBuf2JavaBean.getGroupEntity(groupInfo);
            if(groupMap.containsKey(groupEntity.getPeerId()))
                groupMap.remove(groupEntity.getPeerId());
            groupMap.put(groupEntity.getPeerId(),groupEntity);
            needDb.add(groupEntity);
        }

        dbInterface.batchInsertOrUpdateGroup(needDb);
        triggerEvent(new GroupEvent(GroupEvent.Event.GROUP_INFO_UPDATED));
       }


    /**
     * 创建群
     * 默认是创建临时群，且客户端只能创建临时群
     */
    public void reqCreateTempGroup(String groupName, Set<String> memberList, IMBaseDefine.GroupType type){

        logger.i("group#reqCreateTempGroup, tempGroupName = %s", groupName);

        String loginId = imLoginManager.getPub_key();

        IMGroup.IMGroupCreateReq groupCreateReq  = IMGroup.IMGroupCreateReq.newBuilder()
                .setUserId(loginId)
                .setGroupType(type)
                .setGroupName(groupName)
                .setGroupAvatar("")// todo 群头像 现在是四宫格
                .addAllMemberIdList(memberList)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_GROUP_VALUE;
        int cid = IMBaseDefine.GroupCmdID.CID_GROUP_CREATE_REQUEST_VALUE;
        imSocketManager.sendRequest(groupCreateReq, sid, cid,new Packetlistener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    IMGroup.IMGroupCreateRsp groupCreateRsp  = IMGroup.IMGroupCreateRsp.parseFrom((CodedInputStream)response);
                    IMGroupManager.instance().onReqCreateTempGroup(groupCreateRsp);
                } catch (IOException e) {
                    logger.e("reqCreateTempGroup parse error");
                    triggerEvent(new GroupEvent(GroupEvent.Event.CREATE_GROUP_FAIL));
                }
            }

            @Override
            public void onFaild() {
              triggerEvent(new GroupEvent(GroupEvent.Event.CREATE_GROUP_FAIL));
            }

            @Override
            public void onTimeout() {
              triggerEvent(new GroupEvent(GroupEvent.Event.CREATE_GROUP_TIMEOUT));
            }
        });

    }

    public void onReqCreateTempGroup(IMGroup.IMGroupCreateRsp groupCreateRsp){
        logger.i("group#onReqCreateTempGroup");

        int resultCode = groupCreateRsp.getResultCode();
        if(0 != resultCode){
            logger.e("group#createGroup failed");
            triggerEvent(new GroupEvent(GroupEvent.Event.CREATE_GROUP_FAIL));
            return;
        }
        GroupEntity groupEntity = ProtoBuf2JavaBean.getGroupEntity(groupCreateRsp);
        // 更新DB 更新map
        groupMap.put(groupEntity.getPeerId(),groupEntity);

        IMSessionManager.instance().updateSession(groupEntity);
        dbInterface.insertOrUpdateGroup(groupEntity);
        triggerEvent(new GroupEvent(GroupEvent.Event.CREATE_GROUP_OK, groupEntity)); // 接收到之后修改UI
    }

    /**
     * 删除群成员
     * REMOVE_CHANGE_MEMBER_TYPE
     * 可能会触发头像的修改
     */
    public void reqRemoveGroupMember(String groupId,Set<String> removeMemberlist){
        reqChangeGroupMember(groupId,IMBaseDefine.GroupModifyType.GROUP_MODIFY_TYPE_DEL, removeMemberlist);
    }
    /**
     * 新增群成员
     * ADD_CHANGE_MEMBER_TYPE
     * 可能会触发头像的修改
     */
    public void reqAddGroupMember(String groupId,Set<String> addMemberlist){
        reqChangeGroupMember(groupId,IMBaseDefine.GroupModifyType.GROUP_MODIFY_TYPE_ADD, addMemberlist);
    }

    private void reqChangeGroupMember(String groupId,IMBaseDefine.GroupModifyType groupModifyType, Set<String> changeMemberlist) {
        logger.i("group#reqChangeGroupMember, changeGroupMemberType = %s changeMemberlist.size():%d", groupModifyType.toString(), changeMemberlist.size());

        final String loginId = imLoginManager.getPub_key();
        IMGroup.IMGroupChangeMemberReq groupChangeMemberReq = IMGroup.IMGroupChangeMemberReq.newBuilder()
                .setUserId(loginId)
                .setChangeType(groupModifyType)
                .addAllMemberIdList(changeMemberlist)
                .setGroupId(groupId)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_GROUP_VALUE;
        int cid = IMBaseDefine.GroupCmdID.CID_GROUP_CHANGE_MEMBER_REQUEST_VALUE;
        imSocketManager.sendRequest(groupChangeMemberReq, sid, cid,new Packetlistener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    IMGroup.IMGroupChangeMemberRsp groupChangeMemberRsp = IMGroup.IMGroupChangeMemberRsp.parseFrom((CodedInputStream)response);
                    IMGroupManager.instance().onReqChangeGroupMember(groupChangeMemberRsp);
                } catch (IOException e) {
                    logger.e("reqChangeGroupMember parse error!");
                    triggerEvent(new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_FAIL));
                }
            }

            @Override
            public void onFaild() {
                triggerEvent(new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_FAIL));
            }

            @Override
            public void onTimeout() {
                triggerEvent(new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_TIMEOUT));
            }
        });

    }
    public void reqChangeGroupMember(final String groupId, IMBaseDefine.GroupModifyType groupModifyType, Set<String> changeMemberlist, int quit) {
        logger.i("group#reqChangeGroupMember, changeGroupMemberType = %s", groupModifyType.toString());

        final String loginId = imLoginManager.getPub_key();
        IMGroup.IMGroupChangeMemberReq groupChangeMemberReq = IMGroup.IMGroupChangeMemberReq.newBuilder()
                .setUserId(loginId)
                .setChangeType(groupModifyType)
                .addAllMemberIdList(changeMemberlist)
                .setGroupId(groupId)
                .setQuit(quit)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_GROUP_VALUE;
        int cid = IMBaseDefine.GroupCmdID.CID_GROUP_CHANGE_MEMBER_REQUEST_VALUE;
        imSocketManager.sendRequest(groupChangeMemberReq, sid, cid,new Packetlistener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    IMGroup.IMGroupChangeMemberRsp groupChangeMemberRsp = IMGroup.IMGroupChangeMemberRsp.parseFrom((CodedInputStream)response);
                    if(groupChangeMemberRsp.getResultCode()==0){
                        GroupEntity groupEntity = groupMap.get(groupId);
                        triggerEvent(new GroupEvent(GroupEvent.Event.QUIT_GROUP_SUCESS,groupEntity));
                    }
                } catch (IOException e) {
                    logger.e("reqChangeGroupMember parse error!");
                    triggerEvent(new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_FAIL));
                }
            }

            @Override
            public void onFaild() {
                triggerEvent(new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_FAIL));
            }

            @Override
            public void onTimeout() {
                triggerEvent(new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_TIMEOUT));
            }
        });

    }

    public void onReqChangeGroupMember(IMGroup.IMGroupChangeMemberRsp groupChangeMemberRsp){
        int resultCode = groupChangeMemberRsp.getResultCode();
        if (0 != resultCode){
            triggerEvent(new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_FAIL));
            return;
        }

        String groupId = groupChangeMemberRsp.getGroupId();
        List<String> changeUserIdList = groupChangeMemberRsp.getChgUserIdListList();
        IMBaseDefine.GroupModifyType groupModifyType = groupChangeMemberRsp.getChangeType();


        GroupEntity groupEntityRet = groupMap.get(groupId);
        groupEntityRet.setlistGroupMemberIds(groupChangeMemberRsp.getCurUserIdListList());
        groupMap.put(groupId,groupEntityRet);
        dbInterface.insertOrUpdateGroup(groupEntityRet);


        GroupEvent groupEvent = new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_SUCCESS);
        groupEvent.setChangeList(changeUserIdList);
        groupEvent.setChangeType(ProtoBuf2JavaBean.getGroupChangeType(groupModifyType));
        groupEvent.setGroupEntity(groupEntityRet);
        triggerEvent(groupEvent);
    }

    /**
     * 屏蔽群消息
     * IMGroupShieldReq
     * 备注:应为屏蔽之后大部分操作依旧需要客户端做
     * */
    public void reqShieldGroup(final String groupId,final int shieldType){
        final GroupEntity entity =  groupMap.get(groupId);
        if(entity == null){
            logger.i("GroupEntity do not exist!");
            return;
        }
        final String loginId = IMLoginManager.instance().getPub_key();
        IMGroup.IMGroupShieldReq shieldReq = IMGroup.IMGroupShieldReq.newBuilder()
                .setShieldStatus(shieldType)
                .setGroupId(groupId)
                .setUserId(loginId)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_GROUP_VALUE;
        int cid = IMBaseDefine.GroupCmdID.CID_GROUP_SHIELD_GROUP_REQUEST_VALUE;
        imSocketManager.sendRequest(shieldReq,sid,cid,new Packetlistener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    IMGroup.IMGroupShieldRsp groupShieldRsp = IMGroup.IMGroupShieldRsp.parseFrom((CodedInputStream)response);
                    int resCode = groupShieldRsp.getResultCode();
                    if(resCode !=0){
                        triggerEvent(new GroupEvent(GroupEvent.Event.SHIELD_GROUP_FAIL));
                        return;
                    }
                    if(groupShieldRsp.getGroupId() != groupId || !groupShieldRsp.getUserId().equals(loginId)){
                        return;
                    }
                    // 更新DB状态
                    entity.setStatus(shieldType);
                    dbInterface.insertOrUpdateGroup(entity);
                    // 更改未读计数状态
                    boolean isFor = shieldType == DBConstant.GROUP_STATUS_SHIELD;
                    IMUnreadMsgManager.instance().setForbidden(
                            EntityChangeEngine.getSessionKey(groupId,DBConstant.SESSION_TYPE_GROUP),isFor);
                    triggerEvent(new GroupEvent(GroupEvent.Event.SHIELD_GROUP_OK,entity));

                } catch (IOException e) {
                    logger.e("reqChangeGroupMember parse error!");
                    triggerEvent(new GroupEvent(GroupEvent.Event.SHIELD_GROUP_FAIL));
                }
            }
            @Override
            public void onFaild() {
                triggerEvent(new GroupEvent(GroupEvent.Event.SHIELD_GROUP_FAIL));
            }

            @Override
            public void onTimeout() {
                triggerEvent(new GroupEvent(GroupEvent.Event.SHIELD_GROUP_TIMEOUT));
            }
        });
    }

    public void reqRecommendGroup(){
        IMSystem.IMGetSysMsgDataReq msg = IMSystem.IMGetSysMsgDataReq.newBuilder()
                .setUserId(IMLoginManager.instance().getLoginId()).build();
        int sid = IMBaseDefine.ServiceID.SID_GROUP_VALUE;
        int cid = IMBaseDefine.GroupCmdID.CID_GROUP_RECOMMAND_LIST_INFO_REQUEST_VALUE;
        IMSocketManager.instance().sendRequest(msg,sid,cid);
    }

    /**
     * 收到群成员发生变更消息
     * 服务端主动发出
     * DB
     */
    public void receiveGroupChangeMemberNotify(IMGroup.IMGroupChangeMemberNotify notify){
        String groupId =  notify.getGroupId();
       int changeType = ProtoBuf2JavaBean.getGroupChangeType(notify.getChangeType());
       List<String> changeList =  notify.getChgUserIdListList();

       List<String> curMemberList = notify.getCurUserIdListList();
       if(groupMap.containsKey(groupId)){
           GroupEntity entity = groupMap.get(groupId);
           entity.setlistGroupMemberIds(curMemberList);
           dbInterface.insertOrUpdateGroup(entity);
           groupMap.put(groupId,entity);

           GroupEvent groupEvent = new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_SUCCESS);
           groupEvent.setChangeList(changeList);
           groupEvent.setChangeType(changeType);
           groupEvent.setGroupEntity(entity);
           triggerEvent(groupEvent);
       }else{
           //todo 没有就暂时不管了，只要聊过天都会显示在回话里面
       }
    }

    public List<GroupEntity> getGroupList(int groupType) {
        List<GroupEntity> normalGroupList = new ArrayList<>();
        for (Entry<String, GroupEntity> entry : groupMap.entrySet()) {
            GroupEntity group = entry.getValue();
            if (group == null) {
                continue;
            }
            if (group.getGroupType() == groupType) {
                normalGroupList.add(group);
            }
        }
        return normalGroupList;
    }

    // 该方法只有正式群
    // todo eric efficiency
    public  List<GroupEntity> getNormalGroupSortedList() {
        List<GroupEntity> groupList = getGroupList(DBConstant.GROUP_TYPE_NORMAL);
        Collections.sort(groupList, new Comparator<GroupEntity>(){
            @Override
            public int compare(GroupEntity entity1, GroupEntity entity2) {
                if(entity1.getPinyinElement().pinyin==null)
                {
                    PinYin.getPinYin(entity1.getMainName(), entity1.getPinyinElement());
                }
                if(entity2.getPinyinElement().pinyin==null)
                {
                    PinYin.getPinYin(entity2.getMainName(),entity2.getPinyinElement());
                }
                return entity1.getPinyinElement().pinyin.compareToIgnoreCase(entity2.getPinyinElement().pinyin);
            }
        });

        return groupList;
    }

	public GroupEntity findGroup(String groupId) {
		logger.i("group#findGroup groupId:%s", groupId);
        if(groupMap.containsKey(groupId)){
            return groupMap.get(groupId);
        }
        return null;
	}
    public List<GroupEntity>  getSearchAllGroupList(String key){
        List<GroupEntity> searchList = new ArrayList<>();
        for(Map.Entry<String,GroupEntity> entry:groupMap.entrySet()){
            GroupEntity groupEntity = entry.getValue();
            if (IMUIHelper.handleGroupSearch(key, groupEntity)) {
                searchList.add(groupEntity);
            }
        }
        return searchList;
    }

	public List<UserEntity> getGroupMembers(String groupId) {
		logger.i("group#getGroupMembers groupId:%s", groupId);

		GroupEntity group = findGroup(groupId);
		if (group == null) {
			logger.e("group#no such group id:%s", groupId);
			return null;
		}
        Set<String> userList = group.getlistGroupMemberIds();
		ArrayList<UserEntity> memberList = new ArrayList<UserEntity>();
		/*
		for (Integer id : userList) {
			UserEntity contact = IMContactManager.instance().findContact(id);
			if (contact == null) {
				logger.e("group#no such contact id:%s", id);
				continue;
			}
			memberList.add(contact);
		}
	
		 */
		return memberList;
	}

    /**------set/get 的定义*/
    public Map<String, GroupEntity> getGroupMap() {
        return groupMap;
    }

    public boolean isGroupReady() {
        return isGroupReady;
    }

    public void removeGroupMap(String peerId) {
        groupMap.remove(peerId);
    }
}
