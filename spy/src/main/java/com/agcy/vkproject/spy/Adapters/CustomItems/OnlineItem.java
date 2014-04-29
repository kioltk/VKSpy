package com.agcy.vkproject.spy.Adapters.CustomItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.agcy.vkproject.spy.Models.Online;
import com.agcy.vkproject.spy.R;

/**
 * Created by kiolt_000 on 28-Apr-14.
 */
public class OnlineItem extends UpdateItem {

    public OnlineItem(Online online) {
        super(online);
    }

    @Override
    public View getView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.list_item_online, null);
        ((TextView)rootView.findViewById(R.id.since)).setText(""+getContent().getSinceShort());
        ((TextView)rootView.findViewById(R.id.till)).setText(""+getContent().getTillShort());
        return rootView;
    }

    @Override
    public Online getContent() {
        return (Online) update;
    }
}
