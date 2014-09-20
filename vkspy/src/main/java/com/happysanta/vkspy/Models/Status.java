package com.happysanta.vkspy.Models;

public class Status extends Update {

    private final boolean online;
    private int platform = 7;

    public Status(int userid, int unix, boolean online) {
        super(userid,unix, 0);
        this.online = online;

        convertedTime = convertTime();
    }
    public Status(int userid, int unix, boolean online, Integer platform){
        this(userid,unix,online);
        if(platform!=null)
        this.platform = platform;
    }
    public boolean isOnline() {
        return online;
    }

    public int getPlatform() {
        return platform;
    }
}
