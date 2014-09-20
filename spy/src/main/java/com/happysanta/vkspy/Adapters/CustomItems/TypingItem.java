package com.happysanta.vkspy.Adapters.CustomItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.happysanta.vkspy.Garbage.MagicBounceInterpolator;
import com.happysanta.vkspy.Models.Typing;
import com.happysanta.vkspy.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vk.sdk.api.model.VKApiUserFull;

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

        View rootView = inflater.inflate(R.layout.list_item_typing, null);

        TextView additional = (TextView) rootView.findViewById(R.id.additional);
        additional.setVisibility(View.GONE);

        ((TextView)rootView.findViewById(R.id.time)).setText(""+ getContent().getSmartTime());
        return rootView;
    }

    @Override
    public View getViewWithOwner(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.list_item_update_with_owner, null);


        return reconvert(context,rootView);


    }

    @Override
    public View reconvert(Context context, View rootView) {
        ImageView photo = (ImageView) rootView.findViewById(R.id.photo);
        TextView name = (TextView) rootView.findViewById(R.id.name);
        TextView time = (TextView) rootView.findViewById(R.id.time);
        View textHolder = rootView.findViewById(R.id.text_holder);
        VKApiUserFull user = update.getOwner();
        name.setText(user.first_name+" "+user.last_name);

        if(isNew()){
            View photoHolder = rootView.findViewById(R.id.photo_holder);
            ScaleAnimation blinkAnimation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, (float) 0.5, Animation.RELATIVE_TO_SELF, 0.5f);
            blinkAnimation.setInterpolator(new MagicBounceInterpolator());
            blinkAnimation.setDuration(1200);
            blinkAnimation.setStartOffset(400);
            photoHolder.startAnimation(blinkAnimation);

            AlphaAnimation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new AccelerateInterpolator());
            fadeIn.setDuration(750);
            fadeIn.setStartOffset(700);
            textHolder.startAnimation(fadeIn);
            time.startAnimation(fadeIn);
        }

        TextView additional = (TextView) rootView.findViewById(R.id.additional);
        additional.setVisibility(View.GONE);

        ImageLoader.getInstance().displayImage(user.getBiggestPhoto(),photo);
        rootView.setTag("typing");
        time.setText("" + getContent().getSmartTime());

        time.setTag(getContent());

        return rootView;
    }

    @Override
    public Typing getContent() {
        return (Typing) update;
    }
}
