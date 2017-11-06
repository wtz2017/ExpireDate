package com.wtz.expiredate.data;

/**
 * Created by WTZ on 2017/10/28.
 */

public class GoodsItem {

    private int id;
    private String name;
    private int count;
    private long expireDate;
    private String iconPath;

    public boolean isExpired() {
        return System.currentTimeMillis() >= expireDate;
    }

    public boolean isWillExpired() {
        int remain = (int) ((expireDate - System.currentTimeMillis()) / 86400000);
        return (remain > 0) && (remain < 7);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(long expireDate) {
        this.expireDate = expireDate;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

}
