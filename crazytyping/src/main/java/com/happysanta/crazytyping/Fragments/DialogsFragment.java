package com.happysanta.crazytyping.Fragments;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Checkable;

import com.happysanta.crazytyping.Adapters.DialogsAdapter;
import com.happysanta.crazytyping.Core.Memory;
import com.happysanta.crazytyping.Fragments.Interfaces.LoadImagesOnScrollListener;
import com.happysanta.crazytyping.MainActivity;
import com.happysanta.crazytyping.Models.Dialog;
import com.happysanta.crazytyping.R;

import java.util.ArrayList;


public class DialogsFragment extends ListFragment {

    @Override
    public AbsListView.OnScrollListener getScrollListener() {
        return new LoadImagesOnScrollListener(getListView()){
            @Override
            public int getOffset() {
                int offset = 0;
                if (firstVisibleItem == 0 && hasHeader()) {
                    visibleItemCount--;
                } else {
                    if (firstVisibleItem != 0 && hasHeader()) {
                        offset = 1;
                    }
                }
                return offset;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                ArrayList<View> childs = new ArrayList<View>();
                for(int i = 0 ; i <visibleItemCount; i++){
                    childs.add(view.getChildAt(i));
                }
                Memory.setTypingListeners(childs);
            }
        };
    }


    @Override
    public BaseAdapter adapter() {

        ArrayList<Dialog> items;

        items = Memory.dialogs;
        if (context != null)
            return new DialogsAdapter(items, context);
        return null;
    }

    @Override
    protected void onContentBinded() {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        boolean targeted = targetDialog(id);

        final Checkable typingView = (Checkable) view.findViewById(R.id.checked);
        if(targeted != typingView.isChecked())
            typingView.toggle();
    }

    private Boolean targetDialog(long id) {
        Boolean resp = Memory.targetDialog(id);
        MainActivity activity = (MainActivity) getActivity();
        activity.checkTogglerReady();

        return resp;
    }

    @Override
    public int getAdapterEmptyText() {
        return R.string.no_typings;
    }


}
