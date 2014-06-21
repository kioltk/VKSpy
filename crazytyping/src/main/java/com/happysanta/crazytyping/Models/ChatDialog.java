package com.happysanta.crazytyping.Models;

/**
 * Created by kioltk on 6/19/14.
 */
public class ChatDialog extends Dialog {
    private final String title;
    private final String photo;

    public ChatDialog(int chatid, String title, String photo, Boolean targeted) {
        super(chatid, targeted);
        this.title = title;
        this.photo = photo;

    }
    @Override
    public int getUserid(){
        return 0;
    }
    public int getChatId(){
        return userid;
    }
    @Override
    public String getTitle(){
        return title;
    }
    @Override
    public String getPhoto(){
        return photo;
    }

    @Override
    public long getId() {
        return getChatId()+2000000000;
    }
}
