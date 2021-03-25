package com.tenth.space.DB.entity;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END

import com.tenth.space.config.DBConstant;
import com.tenth.space.imservice.entity.SearchElement;
import com.tenth.space.utils.pinyin.PinYin;
import com.tenth.space.utils.qrcode.QRCodeUtil;

import org.apache.http.util.TextUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * Entity mapped to table UserInfo.
 */
class IdCard{
    private String address;
    protected String encryptPrivakey;
    protected byte[] pubKey;
    protected byte[] keyParameter;

    public IdCard(){

    }

    public IdCard(String address, String encryptPrivakey, byte[] pubKey, byte[] keyParameter){
        setAddress(address);
        setEncryptPrivakey(encryptPrivakey);
        setPubKey(pubKey);
        setKeyParameter(keyParameter);
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setEncryptPrivakey(String encryptPrivakey) {
        this.encryptPrivakey = encryptPrivakey;
    }

    public void setPubKey(byte[] pubKey) {
        this.pubKey = pubKey;
    }

    public void setKeyParameter(byte[] keyParameter) {
        this.keyParameter = keyParameter;
    }

    public String getAddress() {
        return address;
    }

    public String getEncryptPrivakey() {
        return encryptPrivakey;
    }

    public byte[] getPubKey() {
        return pubKey;
    }

    public byte[] getKeyParameter() {
        return keyParameter;
    }
}

public class UserEntity extends PeerEntity{

    private IdCard idCard;
    private String relation;
   // private String id;
    //private String peerId;
    private int gender;
    /** Not-null value. */
    private String mainName;
    /** Not-null value. */
    private String pinyinName;
    /** Not-null value. */
    private String realName;
    /** Not-null value. */
    private String avatar;
    /** Not-null value. */
    private String phone;
    /** Not-null value. */
    private String email;
    private String signature;
    private int departmentId;
    private int status;
    private int created;
    private int updated;
    private int fansCnt;

    public long getLastmodifTime() {
        return lastmodifTime;
    }

    public void setLastmodifTime(long lastmodifTime) {
        this.lastmodifTime = lastmodifTime;
    }

    private long lastmodifTime;

    // KEEP FIELDS - put your custom fields here
    private PinYin.PinYinElement pinyinElement = new PinYin.PinYinElement();
    private SearchElement searchElement = new SearchElement();
    // KEEP FIELDS END

    public UserEntity() {
    }

   // public UserEntity(String id) {
  //      this.id = id;
  //  }
    public UserEntity(String  peerId) {
        this.peerId = peerId;
    }
    public UserEntity(String relation, String id, String peerId, int gender, String mainName, String pinyinName, String realName, String avatar, String phone, String email, String signature, int departmentId, int status, int created, int updated, int fansCnt) {
        this.relation = relation;
        this.id = id;
        this.peerId = peerId;
        this.gender = gender;
        this.mainName = mainName;
        this.pinyinName = pinyinName;
        this.realName = realName;
        this.avatar = avatar;
        this.phone = phone;
        this.email = email;
        this.signature = signature;
        this.departmentId = departmentId;
        this.status = status;
        this.created = created;
        this.updated = updated;
        this.fansCnt = fansCnt;
        this.idCard = null;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

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

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    /** Not-null value. */
    public String getMainName() {
        return mainName;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setMainName(String mainName) {
        this.mainName = mainName;
    }

    /** Not-null value. */
    public String getPinyinName() {
        return pinyinName;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setPinyinName(String pinyinName) {
        this.pinyinName = pinyinName;
    }

    /** Not-null value. */
    public String getRealName() {
        return realName;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setRealName(String realName) {
        this.realName = realName;
    }

    /** Not-null value. */
    public String getAvatar() {
        return avatar;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    /** Not-null value. */
    public String getPhone() {
        return phone;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /** Not-null value. */
    public String getEmail() {
        return email;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setEmail(String email) {
        this.email = email;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    // KEEP METHODS - put your custom methods here
    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", peerId=" + peerId +
                ", gender=" + gender +
                ", mainName='" + mainName + '\'' +
                ", pinyinName='" + pinyinName + '\'' +
                ", realName='" + realName + '\'' +
                ", avatar='" + avatar + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", departmentId=" + departmentId +
                ", status=" + status +
                ", created=" + created +
                ", updated=" + updated +
                ", pinyinElement=" + pinyinElement +
                ", searchElement=" + searchElement +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity)) return false;

        UserEntity entity = (UserEntity) o;

        if (id != null ? !id.equals(entity.id) : entity.id != null) return false;//if (peerId != != entity.peerId) return false;
        if (departmentId != entity.departmentId) return false;
        if (gender != entity.gender) return false;
        if (peerId != null ? !peerId.equals(entity.peerId) : entity.peerId != null) return false;//if (peerId != != entity.peerId) return false;
        if (status != entity.status) return false;
        if (avatar != null ? !avatar.equals(entity.avatar) : entity.avatar != null) return false;
        if (email != null ? !email.equals(entity.email) : entity.email != null) return false;
        if (mainName != null ? !mainName.equals(entity.mainName) : entity.mainName != null)
            return false;
        if (phone != null ? !phone.equals(entity.phone) : entity.phone != null) return false;
        if (pinyinName != null ? !pinyinName.equals(entity.pinyinName) : entity.pinyinName != null)
            return false;
        if (realName != null ? !realName.equals(entity.realName) : entity.realName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCard, relation, id, peerId, gender, mainName, pinyinName, realName, avatar, phone, email, signature, departmentId, status, created, updated, fansCnt, lastmodifTime, pinyinElement, searchElement);
    }
    /*
    @Override
    public int hashCode() {
        int result = peerId;
        result = 31 * result + gender;
        result = 31 * result + (mainName != null ? mainName.hashCode() : 0);
        result = 31 * result + (pinyinName != null ? pinyinName.hashCode() : 0);
        result = 31 * result + (realName != null ? realName.hashCode() : 0);
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + departmentId;
        result = 31 * result + status;
        return result;
    }

     */

    public PinYin.PinYinElement getPinyinElement() {
        return pinyinElement;
    }


    public SearchElement getSearchElement() {
        return searchElement;
    }

    public String getSectionName() {
        if (TextUtils.isEmpty(pinyinElement.pinyin)) {
            return "";
        }
        return pinyinElement.pinyin.substring(0, 1);
    }

    @Override
    public int getType() {
        return DBConstant.SESSION_TYPE_SINGLE;
    }

    public int getFansCnt() {
        return fansCnt;
    }

    public void setFansCnt(int fansCnt) {
        this.fansCnt = fansCnt;
    }

    public void setIdCard(IdCard idCard) {
        this.idCard = idCard;
    }

    public void setIdCard(String address, String encryptPrivakey, byte[] pubKey, byte[] keyParameter) {
        this.idCard = new IdCard(address,encryptPrivakey,pubKey,keyParameter);
    }

    public IdCard getIdCard() {
        return idCard;
    }

   // public String getAddress() {
   //     return idCard!=null ? idCard.getAddress() : null;
   // }

    public String getEncryptPrivakey() {
        return idCard!=null ? idCard.getEncryptPrivakey(): null;
    }

    public byte[] getKeyParameter() {
        return idCard!=null ? idCard.keyParameter: null;
    }

    public byte[] getPubKey() {
        return idCard!=null ? idCard.getPubKey(): null;
    }



    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}