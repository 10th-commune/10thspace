package com.tenth.space.DB.entity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class IdCardEntity {
    private String address;
    private String encryptPrivakey;
    private String pubKey;
    private String keyParameter;
    private int created;

    public IdCardEntity(String IdCardEntityString){

    }

    public IdCardEntity(String address, String encryptPrivakey, String pubKey, String keyParameter, int created){
        setAddress(address);
        setEncryptPrivakey(encryptPrivakey);
        setPubKey(pubKey);
        setKeyParameter(keyParameter);
        setCreated(created);
    }

    public IdCardEntity(String address, String encryptPrivakey, String pubKey, String keyParameter){
        setAddress(address);
        setEncryptPrivakey(encryptPrivakey);
        setPubKey(pubKey);
        setKeyParameter(keyParameter);
        setCreated((int) (System.currentTimeMillis() / 1000));
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setEncryptPrivakey(String encryptPrivakey) {
        this.encryptPrivakey = encryptPrivakey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public void setKeyParameter(String keyParameter) {
        this.keyParameter = keyParameter;
    }

    public String getAddress() {
        return address;
    }

    public String getEncryptPrivakey() {
        return encryptPrivakey;
    }

    public String getPubKey() {
        return pubKey;
    }

    public String getKeyParameter() {
        return keyParameter;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public int getCreated() {
        return created;
    }

    public String toJsonString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("encryptPrivakey", encryptPrivakey);
            jsonObject.put("keyParameter", keyParameter);
            jsonObject.put("created", created);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            return jsonObject.toString();
        }

        /*
        return "IdCardEntity{" +
                "encryptPrivakey='" + encryptPrivakey + '\'' +
                ", keyParameter='" + keyParameter + '\'' +
                ", created=" + created +
                '}';

         */
    }

    @Override
    public String toString() {
        return "IdCardEntity{" +
                "address='" + address + '\'' +
                ", encryptPrivakey='" + encryptPrivakey + '\'' +
                ", pubKey='" + pubKey + '\'' +
                ", keyParameter='" + keyParameter + '\'' +
                ", created=" + created +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdCardEntity that = (IdCardEntity) o;
        return created == that.created &&
                Objects.equals(address, that.address) &&
                Objects.equals(encryptPrivakey, that.encryptPrivakey) &&
                Objects.equals(pubKey, that.pubKey) &&
                Objects.equals(keyParameter, that.keyParameter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, encryptPrivakey, pubKey, keyParameter, created);
    }
}
