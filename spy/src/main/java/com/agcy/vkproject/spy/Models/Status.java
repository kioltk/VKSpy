package com.agcy.vkproject.spy.Models;

public class Status extends Update {

    private final boolean online;

    public Status(int userid, int unix, boolean online) {
        super(userid,unix);
        this.online = online;

        convertedTime = convertTime();
    }
    public boolean isOnline() {
        return online;
    }
}
