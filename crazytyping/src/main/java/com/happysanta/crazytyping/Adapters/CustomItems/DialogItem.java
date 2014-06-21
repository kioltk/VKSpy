package com.happysanta.crazytyping.Adapters.CustomItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.happysanta.crazytyping.Adapters.CustomItems.Interfaces.LoadableImage;
import com.happysanta.crazytyping.Adapters.CustomItems.Interfaces.Reconvertable;
import com.happysanta.crazytyping.Models.Dialog;
import com.happysanta.crazytyping.R;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by kioltk on 6/19/14.
 */
public class DialogItem extends Item implements Reconvertable, LoadableImage {

    private final Dialog dialog;
    private boolean imageLoaded = false;

    public DialogItem(Dialog dialog){
        this.dialog = dialog;
    }


    @Override
    public Dialog getContent() {
        return dialog;
    }

    @Override
    public View getView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.list_item_update_with_owner, null);


        return reconvert(context,rootView);


    }

    @Override
    public View reconvert(Context context, View rootView) {
        TextView name = (TextView) rootView.findViewById(R.id.name);
        View typing = rootView.findViewById(R.id.typing);
        View typingActive = rootView.findViewById(R.id.typing_active);
        typing.clearAnimation();
        typingActive.clearAnimation();
        if(getContent().targeted)
            typing.setVisibility(View.VISIBLE);
        else
            typing.setVisibility(View.INVISIBLE);
        name.setText(getContent().getTitle());


        TextView additional = (TextView) rootView.findViewById(R.id.additional);
        additional.setVisibility(View.GONE);
        if(imageLoaded)
            loadImage(rootView);
        else
            placeHolder(rootView);


        return rootView;
    }

    @Override
    public void loadImage(View rootView) {
        if(rootView!=null) {
            imageLoaded = true;
            ImageView photo = (ImageView) rootView.findViewById(R.id.photo);
            ImageLoader.getInstance().displayImage(getContent().getPhoto(), photo);
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


}
