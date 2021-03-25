package com.tenth.space.protobuf.helper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.tenth.space.DB.entity.BlogEntity;
import com.tenth.space.DB.entity.CommentEntity;
import com.tenth.space.DB.entity.DepartmentEntity;
import com.tenth.space.DB.entity.GroupEntity;
import com.tenth.space.DB.entity.MessageEntity;
import com.tenth.space.DB.entity.SessionEntity;
import com.tenth.space.DB.entity.UserEntity;
import com.tenth.space.config.DBConstant;
import com.tenth.space.config.MessageConstant;
import com.tenth.space.config.SysConstant;
import com.tenth.space.imservice.entity.AudioMessage;
import com.tenth.space.imservice.entity.MsgAnalyzeEngine;
import com.tenth.space.imservice.entity.UnreadEntity;
import com.tenth.space.protobuf.IMBaseDefine;
import com.tenth.space.protobuf.IMGroup;
import com.tenth.space.protobuf.IMMessage;
import com.tenth.space.utils.CommonUtil;
import com.tenth.space.utils.pinyin.PinYin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import fastdfs.FdfsUtil;

/**
 * @author : yingmu on 15-1-5.
 * @email : yingmu@mogujie.com.
 */
public class ProtoBuf2JavaBean {

    public static DepartmentEntity getDepartEntity(IMBaseDefine.DepartInfo departInfo) {
        DepartmentEntity departmentEntity = new DepartmentEntity();

        int timeNow = (int) (System.currentTimeMillis() / 1000);

        departmentEntity.setDepartId(departInfo.getDeptId());
        departmentEntity.setDepartName(departInfo.getDeptName());
        departmentEntity.setPriority(departInfo.getPriority());
        departmentEntity.setStatus(getDepartStatus(departInfo.getDeptStatus()));

        departmentEntity.setCreated(timeNow);
        departmentEntity.setUpdated(timeNow);

        // 设定pinyin 相关
        PinYin.getPinYin(departInfo.getDeptName(), departmentEntity.getPinyinElement());

        return departmentEntity;
    }

    public static UserEntity getUserEntity(IMBaseDefine.UserInfo userInfo) {
        UserEntity userEntity = new UserEntity();
        int timeNow = (int) (System.currentTimeMillis() / 1000);
        userEntity.setId(userInfo.getUserId());
        userEntity.setRelation(userInfo.getRelation().name());
        userEntity.setStatus(userInfo.getStatus());
        userEntity.setAvatar(userInfo.getAvatarUrl());//==""?userInfo.getUserId():userInfo.getAvatarUrl());
        userEntity.setCreated(timeNow);
        userEntity.setDepartmentId(userInfo.getDepartmentId());
        userEntity.setEmail(userInfo.getEmail());//==""?userInfo.getUserId():userInfo.getEmail());
        userEntity.setGender(userInfo.getUserGender());
        userEntity.setMainName(userInfo.getUserNickName()==""?userInfo.getUserId().substring(userInfo.getUserId().length()-4):userInfo.getUserNickName());//==""?userInfo.getUserId():userInfo.getUserNickName());
        userEntity.setPhone(userInfo.getUserTel());//==""?userInfo.getUserId():userInfo.getUserTel());
        userEntity.setPinyinName(userInfo.getUserDomain());//==""?userInfo.getUserId():userInfo.getUserDomain());
        userEntity.setRealName(userInfo.getReferralCode()==""?"wystan":userInfo.getReferralCode());//==""?userInfo.getUserId():userInfo.getUserRealName());
        userEntity.setUpdated(timeNow);
        userEntity.setFansCnt(userInfo.getFansCnt());
        userEntity.setSignature(userInfo.getSignInfo());
        userEntity.setPub_key(userInfo.getUserId());
        userEntity.setAddress(userInfo.getUserId());


       // if("" != userInfo.getUserId()) {//wystan disable for user_id
            //userEntity.setPeerId(Integer.parseInt(userInfo.getUserId().substring(4, 5), 16) * 100 +
            //        Integer.parseInt(userInfo.getUserId().substring(7, 8), 16) * 10 +
             //       Integer.parseInt(userInfo.getUserId().substring(12, 13), 16));//
      //  }


       userEntity.setPeerId(userInfo.getUserId()); //wystan modify for peerid 0


        PinYin.getPinYin(userEntity.getMainName(), userEntity.getPinyinElement());
        return userEntity;
    }

    public static SessionEntity getSessionEntity(IMBaseDefine.ContactSessionInfo sessionInfo) {
        SessionEntity sessionEntity = new SessionEntity();

        int msgType = getJavaMsgType(sessionInfo.getLatestMsgType());
        sessionEntity.setLatestMsgType(msgType);
        sessionEntity.setPeerType(getJavaSessionType(sessionInfo.getSessionType()));
        sessionEntity.setPeerId(sessionInfo.getSessionId());
        sessionEntity.buildSessionKey();
        sessionEntity.setTalkId(sessionInfo.getLatestMsgFromUserId());
        sessionEntity.setLatestMsgId(sessionInfo.getLatestMsgId());
        sessionEntity.setCreated(sessionInfo.getUpdatedTime());

        String content = sessionInfo.getLatestMsgData().toStringUtf8();
       // String desMessage = new String(com.mogujie.tt.Security.getInstance().DecryptMsg(content));
        // 消息加密 neil
       //String desMessage=AESUtils.decrypt(content, Config.getKA());
        // 判断具体的类型是什么
        /*if (msgType == DBConstant.MSG_TYPE_GROUP_TEXT ||
                msgType == DBConstant.MSG_TYPE_SINGLE_TEXT) {
            desMessage = MsgAnalyzeEngine.analyzeMessageDisplay(desMessage);
        }
        sessionEntity.setLatestMsgData(desMessage);
        */

        sessionEntity.setLatestMsgData(content);
        sessionEntity.setUpdated(sessionInfo.getUpdatedTime());

        return sessionEntity;
    }


    public static GroupEntity getGroupEntity(IMBaseDefine.GroupInfo groupInfo) {
        GroupEntity groupEntity = new GroupEntity();
        int timeNow = (int) (System.currentTimeMillis() / 1000);
        groupEntity.setUpdated(timeNow);
        groupEntity.setCreated(timeNow);
        groupEntity.setMainName(groupInfo.getGroupName());
        groupEntity.setAvatar(groupInfo.getGroupAvatar());
        groupEntity.setCreatorId(groupInfo.getGroupCreatorId());
        groupEntity.setPeerId(groupInfo.getGroupId());
        groupEntity.setGroupType(getJavaGroupType(groupInfo.getGroupType()));
        groupEntity.setStatus(groupInfo.getShieldStatus());
        groupEntity.setUserCnt(groupInfo.getGroupMemberListCount());
        groupEntity.setVersion(groupInfo.getVersion());
        groupEntity.setlistGroupMemberIds(groupInfo.getGroupMemberListList());

        List<UserEntity> userEntityList = new ArrayList<>();
        for(IMBaseDefine.UserInfo userInfo:groupInfo.getGroupMemberUsersList()) {
            userEntityList.add(getUserEntity(userInfo));
        }

        groupEntity.setUserEntityList(userEntityList);
        // may be not good place
        PinYin.getPinYin(groupEntity.getMainName(), groupEntity.getPinyinElement());

        return groupEntity;
    }


    /**
     * 创建群时候的转化
     *
     * @param groupCreateRsp
     *
     * @return
     */
    public static GroupEntity getGroupEntity(IMGroup.IMGroupCreateRsp groupCreateRsp) {
        GroupEntity groupEntity = new GroupEntity();
        int timeNow = (int) (System.currentTimeMillis() / 1000);
        groupEntity.setMainName(groupCreateRsp.getGroupName());
        groupEntity.setlistGroupMemberIds(groupCreateRsp.getUserIdListList());
        groupEntity.setCreatorId(groupCreateRsp.getUserId());
        groupEntity.setPeerId(groupCreateRsp.getGroupId());

        groupEntity.setUpdated(timeNow);
        groupEntity.setCreated(timeNow);
        groupEntity.setAvatar("");
        groupEntity.setGroupType(DBConstant.GROUP_TYPE_TEMP);
        groupEntity.setStatus(DBConstant.GROUP_STATUS_ONLINE);
        groupEntity.setUserCnt(groupCreateRsp.getUserIdListCount());
        groupEntity.setVersion(1);

        PinYin.getPinYin(groupEntity.getMainName(), groupEntity.getPinyinElement());
        return groupEntity;
    }


    /**
     * 拆分消息在上层做掉 图文混排
     * 在这判断
     */
    public static MessageEntity getMessageEntity(IMBaseDefine.MsgInfo msgInfo) {
        MessageEntity messageEntity = null;
        IMBaseDefine.MsgType msgType = msgInfo.getMsgType();
        switch (msgType) {
            case MSG_TYPE_SINGLE_AUDIO:
            case MSG_TYPE_GROUP_AUDIO:
                try {
                    /**语音的解析不能转自 string再返回来*/
                    messageEntity = analyzeAudio(msgInfo);
                } catch (JSONException e) {
                    return null;
                } catch (UnsupportedEncodingException e) {
                    return null;
                }
                break;

            case MSG_TYPE_GROUP_TEXT:
            case MSG_TYPE_SINGLE_TEXT:
                messageEntity = analyzeText(msgInfo);
                break;
            default:
                throw new RuntimeException("ProtoBuf2JavaBean#getMessageEntity wrong type!");
        }
        return messageEntity;
    }

    public static CommentEntity getCommentEntity(IMBaseDefine.BlogInfo msgInfo) {
        CommentEntity commentEntity = analyzeComment(msgInfo);
        return commentEntity;
    }

    public static BlogEntity getBlogEntity(IMBaseDefine.BlogInfo blogInfo) {
        BlogEntity blogEntity = analyzeBlog(blogInfo);
//        switch (blogType) {
//            case BLOG_TYPE_FRIEND:
//                blogEntity = analyzeBlog(blogInfo);
//                break;
//
//            case BLOG_TYPE_RCOMMEND:
//                break;
//
//            case BLOG_TYPE_FOLLOWUSER:
//                break;
//
//            default:
//                throw new RuntimeException("ProtoBuf2JavaBean#getBlogMessage wrong type!");
//        }
        return blogEntity;
    }

    public static MessageEntity analyzeText(IMBaseDefine.MsgInfo msgInfo) {
        return MsgAnalyzeEngine.analyzeMessage(msgInfo);
    }

    public static BlogEntity analyzeBlog(IMBaseDefine.BlogInfo blogInfo) {
        return MsgAnalyzeEngine.analyzeBlog(blogInfo);
    }

    private static CommentEntity analyzeComment(IMBaseDefine.BlogInfo msgInfo) {
        return MsgAnalyzeEngine.analyzeComment(msgInfo);
    }


    public static AudioMessage analyzeAudio(IMBaseDefine.MsgInfo msgInfo) throws JSONException, UnsupportedEncodingException {
        AudioMessage audioMessage = new AudioMessage();
        audioMessage.setFromId(msgInfo.getFromSessionId());
        audioMessage.setMsgId(msgInfo.getMsgId());
        audioMessage.setMsgType(getJavaMsgType(msgInfo.getMsgType()));
        audioMessage.setStatus(MessageConstant.MSG_SUCCESS);
        audioMessage.setReadStatus(MessageConstant.AUDIO_UNREAD);
        audioMessage.setDisplayType(DBConstant.SHOW_AUDIO_TYPE);
        audioMessage.setCreated(msgInfo.getCreateTime());
        audioMessage.setUpdated(msgInfo.getCreateTime());

        /*
        ByteString bytes = msgInfo.getMsgData();

        byte[] audioStream = bytes.toByteArray();
        if (audioStream.length < 4) {
            audioMessage.setReadStatus(MessageConstant.AUDIO_READED);
            audioMessage.setAudioPath("");
            audioMessage.setAudiolength(0);
        } else {
            int msgLen = audioStream.length;
            byte[] playTimeByte = new byte[4];
            byte[] audioContent = new byte[msgLen - 4];

            System.arraycopy(audioStream, 0, playTimeByte, 0, 4);
            System.arraycopy(audioStream, 4, audioContent, 0, msgLen - 4);
            int playTime = CommonUtil.byteArray2int(playTimeByte);
            String audioSavePath = FileUtil.saveAudioResourceToFile(audioContent, audioMessage.getFromId());
            audioMessage.setAudiolength(playTime);
            audioMessage.setAudioPath(audioSavePath);
        }
         */
        //wystan modify audio file download from fastdfs  210129
        String originContent = msgInfo.getMsgData().toStringUtf8();
        JSONObject extraContent0;
        try {
            extraContent0 = new JSONObject(originContent);
            String audioRemoteUrl = extraContent0.getString("url");
            audioMessage.setAudiolength(extraContent0.getInt("audiolength"));
            audioMessage.setUrl(audioRemoteUrl);

            if(audioRemoteUrl.contains(FdfsUtil.FDFS_PROTOL)) {
                audioMessage.setAudioPath(new FdfsUtil(CommonUtil.getSavePath(SysConstant.FILE_SAVE_TYPE_AUDIO)).downloadSingle(audioRemoteUrl));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        /**抽离出来 或者用gson*/
        JSONObject extraContent = new JSONObject();
        extraContent.put("audioPath", audioMessage.getAudioPath());
        extraContent.put("audiolength", audioMessage.getAudiolength());
        extraContent.put("readStatus", audioMessage.getReadStatus());
        String audioContent = extraContent.toString();
        audioMessage.setContent(audioContent);

        return audioMessage;
    }


    public static MessageEntity getMessageEntity(IMMessage.IMMsgData msgData) {

        MessageEntity messageEntity = null;
        IMBaseDefine.MsgType msgType = msgData.getMsgType();
        IMBaseDefine.MsgInfo msgInfo = IMBaseDefine.MsgInfo.newBuilder()
                .setMsgData(msgData.getMsgData())
                .setMsgId(msgData.getMsgId())
                .setMsgType(msgType)
                .setCreateTime(msgData.getCreateTime())
                .setFromSessionId(msgData.getFromUserId())

                .build();


        switch (msgType) {
            case MSG_TYPE_SINGLE_AUDIO:
            case MSG_TYPE_GROUP_AUDIO:
                try {
                    messageEntity = analyzeAudio(msgInfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case MSG_TYPE_GROUP_TEXT:
            case MSG_TYPE_SINGLE_TEXT:
                messageEntity = analyzeText(msgInfo);
                break;
            default:
                throw new RuntimeException("ProtoBuf2JavaBean#getMessageEntity wrong type!");
        }
        if (messageEntity != null) {
            messageEntity.setToId(msgData.getToSessionId());
        }

        /**
         消息的发送状态与 展示类型需要在上层做掉
         messageEntity.setStatus();
         messageEntity.setDisplayType();
         */
        return messageEntity;
    }

    public static UnreadEntity getUnreadEntity(IMBaseDefine.UnreadInfo pbInfo) {
        UnreadEntity unreadEntity = new UnreadEntity();
        unreadEntity.setSessionType(getJavaSessionType(pbInfo.getSessionType()));
        unreadEntity.setLatestMsgData(pbInfo.getLatestMsgData().toString());
        unreadEntity.setPeerId(pbInfo.getSessionId());
        unreadEntity.setLaststMsgId(pbInfo.getLatestMsgId());
        unreadEntity.setUnReadCnt(pbInfo.getUnreadCnt());
        unreadEntity.buildSessionKey();
        return unreadEntity;
    }

    /** ----enum 转化接口-- */
    public static int getJavaMsgType(IMBaseDefine.MsgType msgType) {
        switch (msgType) {
            case MSG_TYPE_GROUP_TEXT:
                return DBConstant.MSG_TYPE_GROUP_TEXT;
            case MSG_TYPE_GROUP_AUDIO:
                return DBConstant.MSG_TYPE_GROUP_AUDIO;
            case MSG_TYPE_SINGLE_AUDIO:
                return DBConstant.MSG_TYPE_SINGLE_AUDIO;
            case MSG_TYPE_SINGLE_TEXT:
                return DBConstant.MSG_TYPE_SINGLE_TEXT;
//            case MSG_TYPE_ADD_FRIEND:
//                return MSG_TYPE_ADD_FRIEND;
            default:
                throw new IllegalArgumentException("msgType is illegal,cause by #getProtoMsgType#" + msgType);
        }
    }

    public static int getJavaSessionType(IMBaseDefine.SessionType sessionType) {
        switch (sessionType) {
            case SESSION_TYPE_SINGLE:
                return DBConstant.SESSION_TYPE_SINGLE;
            case SESSION_TYPE_GROUP:
                return DBConstant.SESSION_TYPE_GROUP;
            default:
                throw new IllegalArgumentException("sessionType is illegal,cause by #getProtoSessionType#" + sessionType);
        }
    }

    public static int getJavaGroupType(IMBaseDefine.GroupType groupType) {
        switch (groupType) {
            case GROUP_TYPE_NORMAL:
                return DBConstant.GROUP_TYPE_NORMAL;
            case GROUP_TYPE_TMP:
                return DBConstant.GROUP_TYPE_TEMP;
            case GROUP_TYPE_ACTIVE:
                return DBConstant.GROUP_TYPE_ACTIVE;
            default:
                throw new IllegalArgumentException("sessionType is illegal,cause by #getProtoSessionType#" + groupType);
        }
    }

    public static int getGroupChangeType(IMBaseDefine.GroupModifyType modifyType) {
        switch (modifyType) {
            case GROUP_MODIFY_TYPE_ADD:
                return DBConstant.GROUP_MODIFY_TYPE_ADD;
            case GROUP_MODIFY_TYPE_DEL:
                return DBConstant.GROUP_MODIFY_TYPE_DEL;
            default:
                throw new IllegalArgumentException("GroupModifyType is illegal,cause by " + modifyType);
        }
    }

    public static int getDepartStatus(IMBaseDefine.DepartmentStatusType statusType) {
        switch (statusType) {
            case DEPT_STATUS_OK:
                return DBConstant.DEPT_STATUS_OK;
            case DEPT_STATUS_DELETE:
                return DBConstant.DEPT_STATUS_DELETE;
            default:
                throw new IllegalArgumentException("getDepartStatus is illegal,cause by " + statusType);
        }

    }
}
