package com.tenth.space.imservice.event;

import com.tenth.space.imservice.entity.UnreadEntity;

/**
 * @author : yingmu on 15-1-6.
 * @email : yingmu@mogujie.com.
 */
public class UnreadEvent {

    public UnreadEntity entity;
    public Event event;

    public UnreadEvent(){}
    public UnreadEvent(Event e){
        this.event = e;
    }

    public enum Event {
        UNREAD_MSG_LIST_OK,
        UNREAD_MSG_RECEIVED,

        SESSION_READED_UNREAD_MSG
    }
}
