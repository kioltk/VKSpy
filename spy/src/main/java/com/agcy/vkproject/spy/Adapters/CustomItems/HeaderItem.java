package com.agcy.vkproject.spy.Adapters.CustomItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.agcy.vkproject.spy.R;

/**
 * Created by kiolt_000 on 05-May-14.
 */
public class HeaderItem extends Item {

    private String text;

    public HeaderItem(String text){
        this.text = text;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public View getView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView =  inflater.inflate(R.layout.list_item_header, null);

        TextView textView = (TextView) rootView.findViewById(R.id.text);
        textView.setText(getContent());
        return rootView;
    }

    @Override
    public String getContent() {
        
        return text;
    }
}