package com.happysanta.spy.Adapters.CustomItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.happysanta.spy.R;

/**
 * Created by kiolt_000 on 05-May-14.
 */
public class HeaderItem extends Item {

    private int resId;
    private String text = null;
    public HeaderItem(int resId){
        this.resId = resId;
    }
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
        if(text !=null)
            textView.setText(getContent());
        else
        textView.setText(resId);
        return rootView;
    }

    @Override
    public String getContent() {
        
        return text;
    }
}
