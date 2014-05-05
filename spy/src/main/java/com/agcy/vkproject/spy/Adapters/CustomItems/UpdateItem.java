package com.agcy.vkproject.spy.Adapters.CustomItems;

import android.content.Context;
import android.view.View;

import com.agcy.vkproject.spy.Models.Update;

/**
 * Created by kiolt_000 on 28-Apr-14.
 */
public abstract class UpdateItem extends Item {

    protected final Update update;

    public UpdateItem(Update update){
        this.update = update;
    }
    @Override
    public abstract View getView(Context context);
    public abstract View getViewWithOwner(Context context);

    @Override
    public Update getContent() {
        return update;
    }
}
