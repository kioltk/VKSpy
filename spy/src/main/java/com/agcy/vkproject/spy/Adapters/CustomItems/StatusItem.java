package com.agcy.vkproject.spy.Adapters.CustomItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.agcy.vkproject.spy.Garbage.MagicBounceInterpolator;
import com.agcy.vkproject.spy.Models.Status;
import com.agcy.vkproject.spy.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vk.sdk.api.model.VKApiUserFull;

/**
 * Created by kiolt_000 on 04-May-14.
 */
public class StatusItem extends UpdateItem {
    private boolean imageLoaded = false;

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
    public void loadImage(View rootView) {
        if(rootView!=null) {
            imageLoaded = true;
            ImageView photo = (ImageView) rootView.findViewById(R.id.photo);

            ImageLoader.getInstance().displayImage(update.getOwner().getBiggestPhoto(), photo);
        }
    }
    @Override
     public void placeHolder(View rootView) {
        if(rootView!=null) {
            imageLoaded = true;
            ImageView photo = (ImageView) rootView.findViewById(R.id.photo);
            photo.setImageResource(R.drawable.user_placeholder);
        }
    }

    @Override
    public View getViewWithOwner(Context context) {

        View rootView;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.list_item_update_with_owner, null);

        fillView(context, rootView);

        return rootView;
    }

    @Override
    public View reconvert(Context context,View rootView) {
        fillView(context, rootView);
        return rootView;
    }

    public void fillView(Context context,View rootView) {
        ImageView photo = (ImageView) rootView.findViewById(R.id.photo);
        TextView name = (TextView) rootView.findViewById(R.id.name);
        TextView time = (TextView) rootView.findViewById(R.id.time);
        VKApiUserFull user = update.getOwner();
        name.setText(user.first_name+" "+user.last_name);


        if(isNew()) {

            loadImage(rootView);

            ScaleAnimation blinkAnimation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, (float) 0.5, Animation.RELATIVE_TO_SELF, 0.5f);
            blinkAnimation.setInterpolator(new MagicBounceInterpolator());
            blinkAnimation.setDuration(1200);
            blinkAnimation.setStartOffset(400);
            photo.startAnimation(blinkAnimation);

            AlphaAnimation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new AccelerateInterpolator());
            fadeIn.setDuration(750);
            fadeIn.setStartOffset(700);
            name.startAnimation(fadeIn);
            time.startAnimation(fadeIn);

        }else{
            if(imageLoaded)
                loadImage(rootView);
            else
                placeHolder(rootView);

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
    }

}
