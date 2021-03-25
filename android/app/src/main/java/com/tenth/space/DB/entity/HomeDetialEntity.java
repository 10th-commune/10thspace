package com.tenth.space.DB.entity;



import java.io.Serializable;

/**
 * Created by Administrator on 2017/1/4.
 */

public class HomeDetialEntity implements Serializable {
    private   Object state;
    private int ONlineCount;
    private  String NameClass;
    private int position;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getNameClass() {
        return NameClass;
    }
    public int getONlineCount() {
        return ONlineCount;
    }

    public Object  getState() {
        return state;
    }

    public void setState(String  state) {
        this.state = state;
    }

    public HomeDetialEntity(Object state, int ONlineCount,String NameClass) {
        this.state = state;
        this.ONlineCount = ONlineCount;
        this.NameClass=NameClass;
    }
    public HomeDetialEntity(Object state, String NameClass,int position) {
        this.state = state;
        this.NameClass=NameClass;
        this.position=position;
    }

    public void setONlineCount(int ONlineCount) {
        this.ONlineCount = ONlineCount;
    }

}
