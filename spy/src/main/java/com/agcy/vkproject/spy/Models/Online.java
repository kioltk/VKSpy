package com.agcy.vkproject.spy.Models;

/**
 * Created by kiolt_000 on 26-Apr-14.
 */
public class Online {
    public final int userId;
    public final int since;
    public final int till;

    public Online(int userId, int since, int till) {
        this.userId = userId;
        this.since = since;
        this.till = till;
    }
}
