package com.happysanta.vkspy.Adapters.CustomItems;

import android.content.Context;
import android.view.View;

/**
 * Created by kiolt_000 on 28-Apr-14.
 */
public abstract class Item {

    private boolean isNew = false;

    protected boolean alreadyDeleted = false;
    protected boolean isDeleted = false;
    private boolean enabled = true;

    public abstract View getView(Context context);

    public void setDeleted(){
        isDeleted = true;
    }
    public boolean isNew(){
        return isNew;
    }
    public void setNew(Boolean value){
        isNew = value;
    }
    public abstract Object getContent();

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

}
