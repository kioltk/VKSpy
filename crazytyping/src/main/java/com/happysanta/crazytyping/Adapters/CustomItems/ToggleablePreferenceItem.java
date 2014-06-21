package com.happysanta.crazytyping.Adapters.CustomItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.happysanta.crazytyping.R;

/**
* Created by kiolt_000 on 04-May-14.
*/
public abstract class ToggleablePreferenceItem extends PreferenceItem {

    Boolean isChecked;
    public ToggleablePreferenceItem(String title, String description, Boolean isChecked) {
        super(title, description);
        this.isChecked = isChecked;
    }

    public ToggleablePreferenceItem(String title, boolean isChecked) {
        super(title);
        this.isChecked= isChecked;
    }


    @Override
    public void onClick() {
    }

    public abstract void onToggle(Boolean isChecked);

    @Override
    public View getView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.list_item_preference_toggleable, null);

        TextView titleView = (TextView) rootView.findViewById(R.id.title);
        titleView.setText(title);

        if (description != null) {
            TextView descriptionView = (TextView) rootView.findViewById(R.id.description);
            descriptionView.setText(description);
            descriptionView.setVisibility(View.VISIBLE);
        }

        final CompoundButton switcher = (CompoundButton) rootView.findViewById(R.id.toggler);
        switcher.setChecked(isChecked);
        switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ToggleablePreferenceItem.this.isChecked = isChecked;
                onToggle(isChecked);
            }
        });

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isChecked = !isChecked;
                switcher.setChecked(isChecked);
                ToggleablePreferenceItem.this.onToggle(isChecked);
            }
        });
        setEnabled(isEnabled());

        return rootView;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

    }
}
