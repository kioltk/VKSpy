package com.agcy.vkproject.spy.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.agcy.vkproject.spy.Models.Typing;

import java.util.ArrayList;

/**
 * Created by kiolt_000 on 27-Apr-14.
 */
public class TypingWithOwnerAdapter extends TypingAdapter {
    public TypingWithOwnerAdapter(ArrayList<Typing> typings, Context context) {
        super(typings, context);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        View ownerView = OwnerAdapterHelper.getOwnerView(((Typing)getItem(position)).userId,context);
        View infoView = super.getView(position, convertView, parent);

        linearLayout.addView(ownerView);
        linearLayout.addView(infoView);
        return linearLayout;
    }
}
