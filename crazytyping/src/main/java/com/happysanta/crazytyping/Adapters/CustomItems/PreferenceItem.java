package com.happysanta.crazytyping.Adapters.CustomItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.happysanta.crazytyping.R;

/**
* Created by kiolt_000 on 04-May-14.
*/
public abstract class PreferenceItem extends Item {

    public String title;
    public String description;
    public PreferenceItem(String title){
        this(title, null);
    }
    public PreferenceItem(String title, String description){
        this.title = title;
        this.description = description;
    }
    public abstract void onClick();
    View rootView;
    @Override
    public Object getContent() {
        return title;
    }
    @Override
    public View getView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        rootView = inflater.inflate(R.layout.list_item_preference, null);

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

        setEnabled(isEnabled());
        return rootView;
    }

    public void setDescription(String description) {
        this.description = description;
        TextView descriptionView = (TextView) rootView.findViewById(R.id.description);
        if(description!=null) {
            descriptionView.setText(description);
            descriptionView.setVisibility(View.VISIBLE);
        }else{
            descriptionView.setVisibility(View.GONE);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (rootView != null) {
            rootView.setEnabled(enabled);
            rootView.setClickable(enabled);
            rootView.setFocusable(enabled);
        }
    }
}
