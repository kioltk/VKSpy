package com.agcy.vkproject.spy.Adapters.CustomItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.agcy.vkproject.spy.Models.Online;
import com.agcy.vkproject.spy.R;

/**
 * Created by kiolt_000 on 28-Apr-14.
 */
public class OnlineItem extends UpdateItem {

    public OnlineItem(Online online) {
        super(online);
    }

    @Override
    public View getView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = null;

        rootView = inflater.inflate(R.layout.list_item_online, null);





        return reconvert(context,rootView);
    }



    @Override
    public View getViewWithOwner(Context context) {
        return null;
    }

    @Override
    public Online getContent() {
        return (Online) update;
    }

    @Override
    public View reconvert(Context context, View rootView) {
        Online item = getContent();


        TextView tillView = ((TextView) rootView.findViewById(R.id.till));
        TextView sinceView = ((TextView) rootView.findViewById(R.id.since));
        TextView streakView = ((TextView) rootView.findViewById(R.id.streak));
        if(item.isStreak()){

            sinceView.setText(item.getSinceTime());
            tillView.setText(item.getTillTime());
            streakView.setText(item.getStreak());


            sinceView.setVisibility(View.VISIBLE);
            tillView.setVisibility(View.VISIBLE);
            streakView.setVisibility(View.VISIBLE);
        }else{

            TextView timeView = null;
            if(item.getTill()>0) {
                timeView = tillView;
            }else{
                timeView = sinceView;
            }

            timeView.setVisibility(View.VISIBLE);
            timeView.setText(item.getTime());
            if(item.isOnline()){
                streakView.setVisibility(View.VISIBLE);
            }

        }
        return rootView;
    }
}
