package com.tenth.space.imservice.event;

import com.tenth.space.DB.entity.MessageEntity;

import java.util.List;

/**
 * @author : yingmu on 15-3-26.
 * @email : yingmu@mogujie.com.
 *
 * 异步刷新历史消息
 */
public class RefreshHistoryMsgEvent {
   public int pullTimes;
   public int lastMsgId;
   public int count;
   public List<MessageEntity> listMsg;
   public String peerId;
   public int peerType;
   public String sessionKey;

   public RefreshHistoryMsgEvent(){}

}
