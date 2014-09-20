package com.happysanta.vkspy.Adapters.CustomItems;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.happysanta.vkspy.Models.ChatTyping;
import com.happysanta.vkspy.R;


public class ChatTypingItem extends TypingItem {
    public ChatTypingItem(ChatTyping typing) {
        super(typing);
    }

    @Override
    public View getView(Context context) {
        ChatTyping content = (ChatTyping) getContent();
        View view = super.getView(context);
        TextView additional = (TextView) view.findViewById(R.id.additional);
        additional.setVisibility(View.VISIBLE);
        additional.setText(content.getChat().title);
        return view;
    }

    @Override
    public View getViewWithOwner(Context context) {
        View view = super.getViewWithOwner(context);
        return view;

    }

    @Override
    public View reconvert(Context context, View rootView) {
        rootView = super.reconvert(context, rootView);
        ChatTyping content = (ChatTyping) getContent();
        TextView additional = (TextView) rootView.findViewById(R.id.additional);
        additional.setVisibility(View.VISIBLE);
        additional.setText(content.getChat().title);
        return rootView;
    }
}
