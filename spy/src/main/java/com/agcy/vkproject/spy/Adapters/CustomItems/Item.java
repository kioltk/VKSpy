package com.agcy.vkproject.spy.Adapters.CustomItems;

import android.content.Context;
import android.view.View;

/**
 * Created by kiolt_000 on 28-Apr-14.
 */
public abstract class Item {

    private boolean isNew = false;

    private boolean isDeleted = false;
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
        return true;
    }

    public boolean isDeleted() {
        return isDeleted;
    }
}
