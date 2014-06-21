package com.happysanta.crazytyping.Adapters.CustomItems;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.happysanta.crazytyping.Adapters.CustomItems.Interfaces.LoadableImage;
import com.happysanta.crazytyping.Adapters.CustomItems.Interfaces.Reconvertable;
import com.happysanta.crazytyping.Models.Update;
import com.happysanta.crazytyping.R;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by kiolt_000 on 28-Apr-14.
 */
public abstract class UpdateItem extends Item implements LoadableImage, Reconvertable {

    protected final Update update;
    protected boolean imageLoaded = false;
    private boolean shouldLoadImage;

    public UpdateItem(Update update){
        this.update = update;
    }
    @Override
    public abstract View getView(Context context);
    protected boolean shouldLoadImage(){
        return shouldLoadImage;
    }
    public void loadImage(){
        shouldLoadImage = true;
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


    public abstract View getViewWithOwner(Context context);

    @Override
    public Update getContent() {
        return update;
    }


}
