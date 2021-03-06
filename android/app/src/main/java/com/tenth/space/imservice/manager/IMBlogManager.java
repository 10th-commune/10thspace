package com.tenth.space.imservice.manager;

import android.content.Intent;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.tenth.space.DB.DBInterface;
import com.tenth.space.DB.entity.BlogEntity;
import com.tenth.space.DB.entity.CommentEntity;
import com.tenth.space.config.SysConstant;
import com.tenth.space.imservice.callback.Packetlistener;
import com.tenth.space.imservice.entity.BlogMessage;
import com.tenth.space.imservice.event.BlogInfoEvent;
import com.tenth.space.imservice.service.LoadImageService2;
import com.tenth.space.protobuf.IMBaseDefine;
import com.tenth.space.protobuf.IMBlog;
import com.tenth.space.protobuf.IMSystem;
import com.tenth.space.protobuf.helper.ProtoBuf2JavaBean;
import com.tenth.space.utils.LogUtils;
import com.tenth.space.utils.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.tenth.space.imservice.event.BlogInfoEvent.Event.ADD_COMMENT_OK;
import static com.tenth.space.imservice.event.BlogInfoEvent.Event.GET_COMMENT_LIST_OK;
import static com.tenth.space.imservice.event.BlogInfoEvent.Event.RANKINGBACK_OK;

/**
 * Created by wing on 2016/7/11.
 */
public class IMBlogManager extends IMManager {

    private Logger logger = Logger.getLogger(IMMessageManager.class);
    IMSocketManager imSocketManager = IMSocketManager.instance();
    private DBInterface dbInterface = DBInterface.instance();
    private final long TIMEOUT_MILLISECONDS = 6 * 1000;
    private final long IMAGE_TIMEOUT_MILLISECONDS = 4 * 60 * 1000;
    //private Map<Integer,BlogEntity> blogMap = new ConcurrentHashMap<>();
    private List<BlogEntity> temprecommendBlogList = new ArrayList<>();
    private List<BlogEntity> tempfridendBlogList = new ArrayList<>();
    private List<BlogEntity> tempfollowBlogList = new ArrayList<>();

    ArrayList<CommentEntity> commentEntities = new ArrayList<>();

    public ArrayList<CommentEntity> getCommentEntities() {
        return commentEntities;
    }

    public List<BlogEntity> getRecommendBlogList() {
        return temprecommendBlogList;
    }

    public List<BlogEntity> getFridendBlogList() {
        return tempfridendBlogList;
    }

    public List<BlogEntity> getFollowBlogList() {
        return tempfollowBlogList;
    }


    public IMBlogManager() {
        //??????????????????????????????????????????
        EventBus.getDefault().register(this);
    }

    @Override
    public void doOnStart() {
    }

    @Override
    public void reset() {
        //userDataReady = false;
        //userMap.clear();
    }

    // ??????
    private static IMBlogManager inst = new IMBlogManager();

    public static IMBlogManager instance() {
        return inst;
    }

    //??????app??????????????????????????????????????????????????????
    public void reqBlogList(IMBaseDefine.BlogType type,int pager) {
        String userId = IMLoginManager.instance().getPub_key();
        IMBlog.IMBlogGetListReq imGetBlogListReq = IMBlog.IMBlogGetListReq
                .newBuilder()
                .setBlogType(type)
                .setPageSize(8)
                .setPage(pager)
                .setUserId(userId)
                .setUpdateTime(0)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_BLOG_VALUE;
        int cid = IMBaseDefine.BlogCmdID.CID_BLOG_GET_LIST_REQUEST_VALUE;
        imSocketManager.sendRequest(imGetBlogListReq, sid, cid);
    }

    public void onReqBlogList(IMBlog.IMBlogGetListRsp rsp) {
switch (rsp.getBlogType()){
    case BLOG_TYPE_RCOMMEND:
        temprecommendBlogList.clear();
        break;
    case BLOG_TYPE_FRIEND:
        tempfridendBlogList.clear();
        break;
    case BLOG_TYPE_FOLLOWUSER:
        tempfollowBlogList.clear();
        break;
}


        List<IMBaseDefine.BlogInfo> blogInfos = rsp.getBlogListList();
//        if (blogInfos.size() <= 0) {
//            return;
//        }
        //????????????????????????????????????

        for (IMBaseDefine.BlogInfo info : blogInfos) {
            BlogEntity blogEntity = null;
           // IMBaseDefine.BlogType blogType = info.getBlogType();
            IMBaseDefine.BlogType blogType = rsp.getBlogType();
            switch (blogType) {
                case BLOG_TYPE_RCOMMEND:
                    blogEntity = ProtoBuf2JavaBean.getBlogEntity(info);
                    temprecommendBlogList.add(blogEntity);//????????????????????????????????????????????????????????????????????????
                    break;

                case BLOG_TYPE_FRIEND:
                    blogEntity = ProtoBuf2JavaBean.getBlogEntity(info);
                    tempfridendBlogList.add(blogEntity);
                    break;

                case BLOG_TYPE_FOLLOWUSER:
                    blogEntity = ProtoBuf2JavaBean.getBlogEntity(info);
                    tempfollowBlogList.add(blogEntity);
                    break;

                default:
                    break;
            }
        }

//        ???????????????
        LogUtils.d("????????????-IMBlogManager---------??????????????????????????????????????????");
        if (temprecommendBlogList.size()>0){
            dbInterface.batchInsertOrUpdateBlog(temprecommendBlogList);
        }
        if (tempfridendBlogList.size()>0){
            dbInterface.batchInsertOrUpdateBlog(tempfridendBlogList);
        }
        if (tempfollowBlogList.size()>0){
            dbInterface.batchInsertOrUpdateBlog(tempfollowBlogList);
        }

//?????????
        LogUtils.d("????????????-IMBlogManager---------GET_BLOG_OK???????????????????????????????????????");
        switch (rsp.getBlogType()){
            case BLOG_TYPE_RCOMMEND:
                EventBus.getDefault().post(new BlogInfoEvent(BlogInfoEvent.Event.GET_BLOG_OK,-1));
                break;
            case BLOG_TYPE_FRIEND:
                EventBus.getDefault().post(new BlogInfoEvent(BlogInfoEvent.Event.GET_BLOG_OK,-2));
                break;
            case BLOG_TYPE_FOLLOWUSER:
               EventBus.getDefault().post(new BlogInfoEvent(BlogInfoEvent.Event.GET_BLOG_OK,-3));
                break;
        }


    }

    public void sendBlog(final String blogContent, final BlogMessage blogMessage) {
        final String userId = IMLoginManager.instance().getPub_key();
        byte[] sendContent = null;
        try {
//            LogUtils.d("??????????????????????????????" + blogContent);
//            String content = new String(com.mogujie.tt.Security.getInstance().EncryptMsg(blogContent));
            String content = blogContent;
//            LogUtils.d("??????????????????????????????" + content);

            sendContent = content.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        IMBlog.IMBlogSend msgData = IMBlog.IMBlogSend
                .newBuilder()
                .setUserId(userId)
                .setBlogData(ByteString.copyFrom(sendContent))  // ???????????????????????? todo ByteString.copyFrom
                .build();
        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
        int cid = IMBaseDefine.BlogCmdID.CID_BLOG_SEND_VALUE;

        LogUtils.d("IMBlogManager------sendRequest ?????????IM?????????????????????");
        //final BlogEntity messageEntity  = msgEntity;
        imSocketManager.sendRequest(msgData, sid, cid, new Packetlistener(IMAGE_TIMEOUT_MILLISECONDS) {
            @Override
            public void onSuccess(Object response) {
                try {
                    IMBlog.IMBlogSendAck blogSendAck = IMBlog.IMBlogSendAck.parseFrom((CodedInputStream) response);
                    logger.i("blog#onAckSendedMsg");
                    if (blogSendAck.getUserId() == "") {
                        throw new RuntimeException("Msg ack error,cause by msgId <=0");
                    }

                    //messageEntity.setStatus(MessageConstant.MSG_SUCCESS);
                    //messageEntity.setMsgId(imMsgDataAck.getMsgId());
                    /**??????ID???????????????????????????*/
                    //dbInterface.insertOrUpdateMessage(messageEntity);
                    /**??????sessionEntity lastMsgId??????*/

                    //??????AddBlogActivity
                    EventBus.getDefault().postSticky(new BlogInfoEvent(BlogInfoEvent.Event.ACK_SEND_BLOG_OK));
                    LogUtils.d("IMBlogManager------sendRequest:onSuccess:?????????IM???????????????" +
                            "???BlogId=" + blogSendAck.getBlogId() + ", UserId=" + blogSendAck.getUserId() + "???");

                    //??????InternalFragment????????????????????????
                    blogMessage.setLikeCnt(0);
                    blogMessage.setCommentCnt(0);
                    blogMessage.setCreated(System.currentTimeMillis());//???????????????
                    blogMessage.setAvatarUrl(IMLoginManager.instance().getLoginInfo().getAvatar());//?????????
                   // blogMessage.setWriterUserId(userId);//??????,??????????????? ????????????
                    blogMessage.setBlogId(blogSendAck.getBlogId());
                    String mainName = IMLoginManager.instance().getLoginInfo().getMainName();
                    blogMessage.setNickName(mainName);
                    EventBus.getDefault().postSticky(new BlogInfoEvent(BlogInfoEvent.Event.ADD_BLOG_UPDATE_OK, blogMessage));
//                    reqBlogList();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFaild() {
                //messageEntity.setStatus(MessageConstant.MSG_FAILURE);
                //dbInterface.insertOrUpdateMessage(messageEntity);

                //??????AddBlogActivity???????????????????????????
                EventBus.getDefault().postSticky(new BlogInfoEvent(BlogInfoEvent.Event.ACK_SEND_BLOG_FAILURE));
            }

            @Override
            public void onTimeout() {
                //messageEntity.setStatus(MessageConstant.MSG_FAILURE);
                //dbInterface.insertOrUpdateMessage(messageEntity);

                //??????AddBlogActivity?????????????????????????????????
                EventBus.getDefault().postSticky(new BlogInfoEvent(BlogInfoEvent.Event.ACK_SEND_BLOG_TIME_OUT));
            }
        });
    }

    public void reqAddComment(final int blogId, final String data) {
        ByteString bytes = null;
        try {
            bytes = ByteString.copyFrom(data.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (bytes == null) {
            LogUtils.d("???????????????????????????reqAddComment----data.getBytes(\"utf-8\")");
            return;
        }
//        message IMBlogAddCommentReq{
//            //cmd id:		0x0A05
//            required uint32 user_id = 1;
//            required uint32 blog_id = 2;
//            required bytes blog_data = 3;
//            optional bytes attach_data = 20;
//        }
        String userId = IMLoginManager.instance().getPub_key();
        final String mainName = IMLoginManager.instance().getLoginInfo().getMainName();
        final String avatar = IMLoginManager.instance().getLoginInfo().getAvatar();
        IMBlog.IMBlogAddCommentReq addCommentReq = IMBlog.IMBlogAddCommentReq
                .newBuilder()
                .setUserId(userId)
                .setBlogId(blogId)
                .setBlogData(bytes)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_BLOG_VALUE;
        int cid = IMBaseDefine.BlogCmdID.CID_BLOG_ADD_COMMENT_REQUEST_VALUE;
        imSocketManager.sendRequest(addCommentReq, sid, cid, new Packetlistener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    IMBlog.IMBlogAddCommentRsp addCommentRsp = IMBlog.IMBlogAddCommentRsp.parseFrom((CodedInputStream) response);
                    int resultCode = addCommentRsp.getResultCode();
                    if (resultCode == 0) {
                        CommentEntity commentEntity = new CommentEntity();
                        commentEntity.setMsgData(data);
                        commentEntity.setNickName(mainName);
                        commentEntity.setAvatarUrl(avatar);

                        BlogInfoEvent blogInfoEvent = new BlogInfoEvent(ADD_COMMENT_OK);
                        blogInfoEvent.setCommentEntity(commentEntity);
                        EventBus.getDefault().postSticky(blogInfoEvent);
                        LogUtils.d("????????????????????????:resultCode:" + resultCode);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFaild() {

            }

            @Override
            public void onTimeout() {

            }
        });
        LogUtils.d("IMBlogManager-----reqAddComment(??????????????????)");
    }

    public void reqCommentList(int blogId) {
        String userId = IMLoginManager.instance().getPub_key();
        IMBlog.IMBlogGetCommentReq getCommentReq = IMBlog.IMBlogGetCommentReq
                .newBuilder()
                .setUserId(userId)
                .setBlogId(blogId)
                .setUpdateTime(0)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_BLOG_VALUE;
        int cid = IMBaseDefine.BlogCmdID.CID_BLOG_GET_COMMENT_REQUEST_VALUE;
        imSocketManager.sendRequest(getCommentReq, sid, cid);
        LogUtils.d("IMBlogManager-----reqCommentList(??????????????????)");
    }
/*
??????????????????2016.12.1xubo********************************************************************************************************************
 */
    public void onRspGetComment(IMBlog.IMBlogGetCommentRsp rsp) {
        commentEntities.clear();

        String userId = rsp.getUserId();
        List<IMBaseDefine.BlogInfo> commentList = rsp.getCommentListList();
        LogUtils.d("???????????????????????????msgInfoList:size=" + commentList.size());
        if (commentList.size() <= 0) {
            logger.i("onRspGetComment# have no msgList");
            EventBus.getDefault().postSticky(new BlogInfoEvent(GET_COMMENT_LIST_OK));
            return;
        }
        /**
         * comment???????????????
         * blogId_
         * msgList_
         *      createTime_
         *      fromSessionId_
         *      msgData_
         *      msgId
         *      msgType_(MSG_TYPE_COMMENT)
         * updateTime_
         * userId
         */

        //????????????????????????
        for (IMBaseDefine.BlogInfo info : commentList) {
           // IMBaseDefine.BlogType blogType = info.getBlogType();
            CommentEntity commentEntity = ProtoBuf2JavaBean.getCommentEntity(info);
            commentEntities.add(commentEntity);
        }
//
//        //???????????????
////        LogUtils.d("????????????-IMBlogManager---------dbInterface.batchInsertOrUpdateBlog(blogList)");
////        dbInterface.batchInsertOrUpdateBlog(blogList);
//
        LogUtils.d("????????????-IMBlogManager---------GET_COMMENT_LIST_OK????????????");
        EventBus.getDefault().postSticky(new BlogInfoEvent(GET_COMMENT_LIST_OK));
    }

    public void onEvent(BlogInfoEvent event) {
        BlogInfoEvent.Event type = event.getEvent();
        switch (type) {
            case IMAGE_UPLOAD_FAILD:
                /*logger.i("pic#onUploadImageFaild");
                ImageMessage imageMessage = (ImageMessage)event.getMessageEntity();
                imageMessage.setLoadStatus(MessageConstant.IMAGE_LOADED_FAILURE);
                imageMessage.setStatus(MessageConstant.MSG_FAILURE);
                dbInterface.insertOrUpdateMessage(imageMessage);*/

                /**??????Activity??? ??????*/
                /*event.setEvent(MessageEvent.Event.HANDLER_IMAGE_UPLOAD_FAILD);
                event.setMessageEntity(imageMessage);
                triggerEvent(event);*/
                break;

            case IMAGE_UPLOAD_SUCCESS:
                onImageUploadSuccess(event);
//                EventBus.getDefault().unregister(this);
                break;
        }
    }

    public void sendBlogCmd(BlogMessage blog) {
        logger.i("chat#text#textMessage");

        if (blog.getPathList().size() != 0) {//??????????????????????????????(???????????????)

            //DBInterface.instance().batchInsertOrUpdateBlog(blogList);

            Intent inent = new Intent(ctx, LoadImageService2.class);
            inent.putExtra(SysConstant.UPLOAD_IMAGE_INTENT_PARAMS, blog);
            ctx.startService(inent);
        } else {//??????????????????????????????(???????????????)
            LogUtils.d("?????????????????????????????????????????????????????????:" + blog.getBlogContent());
            sendBlog(blog.getBlogContent(), blog);
        }
    }

    private void onImageUploadSuccess(BlogInfoEvent blogInfoEvent) {
        BlogMessage blogMessage = (BlogMessage) blogInfoEvent.getBlogMessage();

        List<String> urlList = blogMessage.getUrlList();
        for (int i = 0; i < urlList.size(); i++) {
            try {
                //??????url http:\/\/maomaojiang.oss-cn-shenzhen.aliyuncs.com\/IM\/2016\/10\/1477887043564.png
                // => http://maomaojiang.oss-cn-shenzhen.aliyuncs.com/IM/2016/10/1477887043564.png
                urlList.set(i, URLDecoder.decode(urlList.get(i), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
//        for (String url : blogMessage.getUrlList()) {
//            String realImageURL = "";
//            try {
//                realImageURL = URLDecoder.decode(url, "utf-8");
//                LogUtils.d("URLDecoder:" + realImageURL);
//                logger.i("pic#realImageUrl:%s", realImageURL);
//            } catch (UnsupportedEncodingException e) {
//                logger.e(e.toString());
//            }

        //?????????sqlite
            /*imageMessage.setUrl(realImageURL);
            imageMessage.setStatus(MessageConstant.MSG_SUCCESS);
            imageMessage.setLoadStatus(MessageConstant.IMAGE_LOADED_SUCCESS);
            dbInterface.insertOrUpdateMessage(imageMessage);*/
//    }

//        /**??????Activity??? ?????? ??? ????????????*/

        //?????????????????????????????????
        blogInfoEvent.setEvent(BlogInfoEvent.Event.HANDLER_IMAGE_UPLOAD_SUCCESS);
        //imageEvent.setMessageEntity(imageMessage);

        //?????????????????????AddBlogActivity????????????????????????(??????????????????????????????)
        EventBus.getDefault().postSticky(blogInfoEvent);

        //????????????IMAGE_MSG_START,???json??????
        /*imageMessage.setContent(MessageConstant.IMAGE_MSG_START
                + realImageURL + MessageConstant.IMAGE_MSG_END);*/

        //??????????????????????????????(???????????????)
        LogUtils.d("?????????????????????????????????????????????????????????:" + blogMessage.getBlogContent());
        sendBlog(blogMessage.getBlogContent(), blogMessage);
    }
    public void sendStudyTimeRankingReq( IMSystem.StatisticsType statisticsType, String timeInterval, int pageIndex,List<Integer>userIds,int pageSize){
        IMSystem.IMSysGetStudyTimeReq build = IMSystem.IMSysGetStudyTimeReq.newBuilder()
                .setUserId(IMLoginManager.instance().getPub_key())
                .addAllTargetUserIdList(userIds)
                .setStatisticsType(statisticsType)
                .setStatisticsParam(timeInterval)
                .setPage(pageIndex)
                .setPageSize(pageSize)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_SYS_MSG_VALUE;
        int cid = IMBaseDefine.SysMsgCmdID.CID_SYS_GET_STUDY_TIME_REQUEST_VALUE;
        IMSocketManager.instance().sendRequest(build,sid,cid);
    }
    public void onstudyTimeRsp(IMSystem.IMGetSysMsgDataRsp imSysGetStudyTimeRsp) {
        List<IMSystem.IMSysMsgData> studytimeListList = imSysGetStudyTimeRsp.getMsgListList();
        BlogInfoEvent event=new BlogInfoEvent(RANKINGBACK_OK,studytimeListList);
        EventBus.getDefault().postSticky(event);
    }

    /*public void sendVoice(AudioMessage audioMessage) {
        logger.i("chat#audio#sendVoice");
        audioMessage.setStatus(MessageConstant.MSG_SENDING);
        long pkId =  DBInterface.instance().insertOrUpdateMessage(audioMessage);
        sessionManager.updateSession(audioMessage);
        sendMessage(audioMessage);
    }*/


    /*public void sendSingleImage(ImageMessage msg){
        logger.i("ImMessageManager#sendImage ");
        ArrayList<ImageMessage> msgList = new ArrayList<>();
        msgList.add(msg);
        sendImages(msgList);
    }*/

//    public void sendImages(BlogMessage blog) {
//        logger.i("blog#sendImages");
//
//        //DBInterface.instance().batchInsertOrUpdateBlog(blogList);
//
//        Intent inent = new Intent(ctx, LoadImageService2.class);
//        inent.putExtra(SysConstant.UPLOAD_IMAGE_INTENT_PARAMS, blog);
//        ctx.startService(inent);
//
//    }
}