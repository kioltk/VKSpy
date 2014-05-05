package com.agcy.vkproject.spy.Models;

import com.agcy.vkproject.spy.Core.Helper;

public class Status extends Update {

    private final boolean online;
    private final int unix;

    public Status(int userid, int unix, boolean online) {
        super(userid);
        this.unix = unix;
        this.online = online;
    }

    @Override
    public String getTime() {
        return Helper.getTime(unix);
    }

    @Override
    public String getDate() {
        return Helper.getDate(unix);
    }

    @Override
    public Integer getUnix() {
        return unix;
    }

    public boolean isOnline() {
        return online;
    }
}
