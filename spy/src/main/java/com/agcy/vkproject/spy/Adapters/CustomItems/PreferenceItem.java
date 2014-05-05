package com.agcy.vkproject.spy.Adapters.CustomItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.agcy.vkproject.spy.R;

/**
* Created by kiolt_000 on 04-May-14.
*/
public abstract class PreferenceItem {

    public String title;
    public String description;
    public PreferenceItem(String title, String description){
        this.title = title;
        this.description = description;
    }
    public abstract void onClick();

    public View getView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.list_item_preference, null);

        TextView titleView = (TextView) rootView.findViewById(R.id.title);
        titleView.setText(title);

        if(description!=null) {
            TextView descriptionView = (TextView) rootView.findViewById(R.id.description);
            descriptionView.setText(description);
            descriptionView.setVisibility(View.VISIBLE);
        }
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceItem.this.onClick();
            }
        });

        return rootView;
    }

}
