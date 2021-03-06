package com.happysanta.vkspy.Adapters.CustomItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import com.happysanta.vkspy.Core.Helper;
import com.happysanta.vkspy.Garbage.MagicBounceInterpolator;
import com.happysanta.vkspy.Models.Status;
import com.happysanta.vkspy.R;
import com.vk.sdk.api.model.VKApiUserFull;

/**
 * Created by kiolt_000 on 04-May-14.
 */
public class StatusItem extends UpdateItem {

    public StatusItem(Status status) {
        super(status);
    }

    @Override
    public Status getContent() {
        return (Status) super.getContent();
    }

    @Override
    public View getView(Context context) {
        return null;
    }


    @Override
    public View getViewWithOwner(Context context) {

        View rootView;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.list_item_update_with_owner, null);

        reconvert(context, rootView);

        return rootView;
    }

    @Override
    public View reconvert(Context context,View rootView) {
        TextView name = (TextView) rootView.findViewById(R.id.name);
        TextView time = (TextView) rootView.findViewById(R.id.time);
        View textHolder = rootView.findViewById(R.id.text_holder);
        TextView additional = (TextView) rootView.findViewById(R.id.additional);
        VKApiUserFull user = update.getOwner();
        name.setText(user.first_name+" "+user.last_name);

        placeHolder(rootView);
        String platform = Helper.getPlatform(getContent().getPlatform());
        if(platform==null){
            additional.setVisibility(View.GONE);
        }else{
            additional.setVisibility(View.VISIBLE);
            additional.setText(platform);
        }


        if(isNew()) {

            loadImage(rootView);
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

        }else{
            if(imageLoaded || shouldLoadImage())
                loadImage(rootView);

        }

        time.setText("" + getContent().getTime());
        time.setPadding(0,0,0,0);
        if(getContent().isOnline()){
            time.setTextColor(context.getResources().getColor(R.color.green));
            time.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_arrow_green, 0);
        }else{
            time.setTextColor(context.getResources().getColor(R.color.red));
            time.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_arrow_red, 0);
        }
        return rootView;
    }

}
