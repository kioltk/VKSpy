package com.agcy.vkproject.spy.Adapters.CustomItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import com.agcy.vkproject.spy.Models.Typing;
import com.agcy.vkproject.spy.R;

/**
 * Created by kiolt_000 on 28-Apr-14.
 */
public class TypingItem extends UpdateItem {

    public TypingItem(Typing typing){
        super(typing);
    }
    @Override
    public View getView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rootView = inflater.inflate(R.layout.list_item_time, null);

        AlphaAnimation alphaAnimation = new AlphaAnimation(1,0);
        alphaAnimation.setInterpolator(new AccelerateInterpolator());
        alphaAnimation.setDuration(700);
        rootView.startAnimation(alphaAnimation);
        ((TextView)rootView.findViewById(R.id.time)).setText(""+ getContent().getTime());
        return rootView;
    }

    @Override
    public Typing getContent() {
        return (Typing) update;
    }
}
