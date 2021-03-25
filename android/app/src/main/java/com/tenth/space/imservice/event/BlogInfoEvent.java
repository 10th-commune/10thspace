package com.tenth.space.imservice.event;

import com.tenth.space.DB.entity.CommentEntity;
import com.tenth.space.imservice.entity.BlogMessage;
import com.tenth.space.protobuf.IMSystem;

import java.util.List;

/**
 * Created by neil.yi on 2016/9/23.
 */

public class BlogInfoEvent {

    private Event event;
    private BlogMessage blogMessage = null;
    public int position;
    public CommentEntity mCommentEntity = null;

    public List<IMSystem.IMSysMsgData> getStudytimeListList() {
        return studytimeListList;
    }


    List<IMSystem.IMSysMsgData> studytimeListList;

    public BlogInfoEvent(Event event) {
        //默认值 初始化使用
        this.event = event;
    }

    public BlogInfoEvent(Event event, BlogMessage blog) {
        //默认值 初始化使用
        this.event = event;
        blogMessage = blog;
    }
    public BlogInfoEvent (Event event,List<IMSystem.IMSysMsgData> studytimeListList){
        this.event=event;
        this.studytimeListList=studytimeListList;
    }

    public BlogInfoEvent(Event followSuccess, int position) {
        this.event = followSuccess;
        this.position = position;
    }

    public BlogMessage getBlogMessage() {
        return blogMessage;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void setCommentEntity(CommentEntity commentEntity) {
        mCommentEntity = commentEntity;
    }

    public enum Event {
        NONE,
        ADD_BLOG_UPDATE_OK,
        GET_BLOG_OK,
        ACK_SEND_BLOG_OK,
        ACK_SEND_BLOG_FAILURE,
        ACK_SEND_BLOG_TIME_OUT,
        IMAGE_UPLOAD_FAILD,
        IMAGE_UPLOAD_SUCCESS,
        HANDLER_IMAGE_UPLOAD_SUCCESS,
        //关注
        FOLLOW_SUCCESS,
        DEL_FOLLOW_SUCCESS,
        //评论
        GET_COMMENT_LIST_OK,
        ADD_COMMENT_OK,
        //三个状态
//        RCOMMEND,
//        FRIEND,
//        FOLLOWUSER
        RANKINGBACK_OK,//请求的排行好友返回值
    }
}
