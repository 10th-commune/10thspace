package com.tenth.space.DB.entity;

/**
 * Created by Administrator on 2017/1/13.
 */

public class HandlerEntity {
    int counts;
    int position;

    public HandlerEntity(int counts, int position) {
        this.counts = counts;
        this.position = position;

    }

    public int getCounts() {
        return counts;
    }

    public int getPosition() {
        return position;
    }
}
