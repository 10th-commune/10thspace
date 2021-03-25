package com.tenth.space.DB.entity;

import com.tenth.space.protobuf.helper.EntityChangeEngine;

/**
 * @author : yingmu on 15-3-25.
 * @email : yingmu@mogujie.com.
 *
 * 聊天对象抽象类  may be user/group
 */
public abstract class PeerEntity{
    protected String id;
    protected String peerId;
    /** Not-null value.
     * userEntity --> nickName
     * groupEntity --> groupName
     * */
    protected String address;
    protected String mainName;
    /** Not-null value.*/
    protected String avatar;
    protected int created;
    protected int updated;

    protected String pub_key;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public String getMainName() {
        return mainName;
    }

    public void setMainName(String mainName) {
        this.mainName = mainName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }

    // peer就能生成sessionKey
    public String getSessionKey(){
       return EntityChangeEngine.getSessionKey(getPeerId(),getType());
    }

    public abstract int getType();

    public void setPub_key(String pub_key) {
        this.pub_key = pub_key;
    }

    public String getPub_key() {
        if(null == pub_key )
            pub_key = peerId;
        return pub_key;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
}
