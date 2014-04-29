package com.agcy.vkproject.spy.Adapters.CustomItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.R;

/**
 * Created by kiolt_000 on 28-Apr-14.
 */
public class DateItem extends Item {

    private final int time;

    public DateItem(int unix){
        this.time = unix;
    }
    @Override
    public View getView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rootView = inflater.inflate(R.layout.list_item_time, null);

        ((TextView)rootView.findViewById(R.id.time)).setText(""+ Helper.getDate(time));
        return rootView;
    }

    @Override
    public String getContent() {
        return Helper.getDate(time);
    }
}
