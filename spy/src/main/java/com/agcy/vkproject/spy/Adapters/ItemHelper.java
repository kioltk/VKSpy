package com.agcy.vkproject.spy.Adapters;

import android.util.Log;

import com.agcy.vkproject.spy.Adapters.CustomItems.DateItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.HeaderItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.Item;
import com.agcy.vkproject.spy.Adapters.CustomItems.OnlineItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.StatusItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.TypingItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.UserItem;
import com.agcy.vkproject.spy.Models.Online;
import com.agcy.vkproject.spy.Models.Status;
import com.agcy.vkproject.spy.Models.Typing;
import com.agcy.vkproject.spy.Models.Update;
import com.vk.sdk.api.model.VKApiUserFull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by kiolt_000 on 28-Apr-14.
 */
public class ItemHelper {
    private static ArrayList<Item> convertUpdates (List<? extends Update> updates,boolean first) {
        ArrayList<Item> items = new ArrayList<Item>();
        Update lastUpdate = null;
        if(!first){
            lastUpdate = updates.get(0);
        }
        for(Update update:updates) {
            if (!update.compareDays(lastUpdate))
                items.add(new DateItem(update.getUnix()));
            Item item = null;
            if (update instanceof Online)
                item = new OnlineItem((Online) update);
            if(update instanceof Status)
                item = new StatusItem((Status) update);
            if (update instanceof Typing)
                item = new TypingItem((Typing) update);
            items.add(item);
            lastUpdate = update;
        }
        return items;
    }
    private static ArrayList<Item> convertUsers (List<? extends VKApiUserFull> users,boolean first) {
        ArrayList<Item> convertedUsers = new ArrayList<Item>();
        if(first) {
            convertedUsers.add(new HeaderItem("Important"));

            for (VKApiUserFull user : users.subList(0, 5)) {
                convertedUsers.add(new UserItem(user));
            }
        }
        ArrayList<VKApiUserFull> tempUsers = new ArrayList<VKApiUserFull>();
        for(VKApiUserFull user: users) {
            tempUsers.add(user);
        }

        Collections.sort(tempUsers, new Comparator<VKApiUserFull>() {
            @Override
            public int compare(VKApiUserFull lhs, VKApiUserFull rhs) {
                return lhs.first_name.compareTo(rhs.first_name);
            }
        });


        VKApiUserFull lastUser = null;
        for(VKApiUserFull user:tempUsers) {
            if (!theSameLastNameLetter(user,lastUser))
                convertedUsers.add(new HeaderItem(user.first_name.substring(0,1)));
            Item item = new UserItem(user);
            convertedUsers.add(item);
            lastUser = user;
        }
        return convertedUsers;
    }

    private static boolean theSameLastNameLetter(VKApiUserFull user1, VKApiUserFull user2){
        if(user1 == null || user2 == null){
            return false;
        }
        return user1.first_name.substring(0,1).equals(user2.first_name.substring(0,1));
    }

    public static class ObservableUpdatesArray extends ObservableArray<Update>{

        private boolean addedNew = false;

        public ObservableUpdatesArray(ArrayList<? extends Update> items) {
            super(items,20);
        }

        @Override
        public List<Item> convert(List<? extends Update> source,boolean first) {
            return convertUpdates(source,first);
        }

        @Override
        public void newItem(final Update newUpdate) {

            for(int i = 0; i < items.size() ; i++){
                Object itemTemp = items.get(i);
                int unix = 0;

                Update updateTemp = (Update) itemTemp;
                unix = updateTemp.getUnix();
                if(unix<newUpdate.getUnix()){
                    items.add(i,newUpdate);
                    break;
                }
            }

            ArrayList<Item> newConvertedItems = (ArrayList<Item>) convert(new ArrayList<Update>() {{
                add(newUpdate);
            }}, false);
            Item convertedItem = newConvertedItems.get(0);
            convertedItem.setNew(true);
            //convertedItems.add(addedNew? 1: 0,convertedItem );

            for(int i = 0; i < convertedItems.size() ; i++){
                Object convertedItemTemp = get(i).getContent();
                int unix = 0;
                if(!(convertedItemTemp instanceof Update))
                    continue;

                Update updateTemp = (Update) convertedItemTemp;
                unix = updateTemp.getUnix();
                if(unix<newUpdate.getUnix()){
                    if(i==1)
                        if(!addedNew){
                            Item nowDivider = new DateItem(0);
                            nowDivider.setNew(true);
                            convertedItems.add(0, nowDivider);
                            addedNew = true;
                        }

                    convertedItems.add(i,convertedItem);
                    break;
                }
            }
            offset++;// потому что ебать
        }

        public boolean recreateHeaders(){
            if( !convertedItems.isEmpty()) {
                addedNew= false;
                Item topItem = convertedItems.get(0);
                if (topItem instanceof DateItem) {

                    DateItem nowDateItem = (DateItem) topItem;

                    if (nowDateItem.getContent() == 0) {

                        Update update = items.get(1);
                        nowDateItem.recreate(update.getUnix());

                        return true;


                    }

                }
            }
            return false;
        }
    }

    public static class ObservableUsersArray extends ObservableArray<VKApiUserFull>{
        public ObservableUsersArray(ArrayList<? extends VKApiUserFull> items) {
            super(items,items.size());
        }

        @Override
        public List<Item> convert(List<? extends VKApiUserFull> source,boolean first) {
            return convertUsers(source,first);
        }

        @Override
        public void newItem(VKApiUserFull item) {

        }
    }

    public static abstract class ObservableArray<T>{

        protected int offset = 0;
        protected final ArrayList<Item> convertedItems = new ArrayList<Item>();
        protected final ArrayList<T> items = new ArrayList<T>();

        public ObservableArray(ArrayList<? extends T> items, int startCount){
            for(T item : items) {
                this.items.add(item);
            }

            if (items.size() != 0) {

                convertMore(startCount,true);
            }
        }
        public ObservableArray(ArrayList<? extends T> items) {
            this(items,200);
        }

        protected Boolean convertMore(int count,boolean first){
            //Log.i("AGCY SPY", "Converting. Items: " + adapter.size()+", offset: "+offset+", count: "+count);
            try {
                int itemsCount = items.size();
                if(offset == itemsCount)
                    return false;

                int least = items.size() - offset;
                if (count > least){
                    count = least;
                }
                convertedItems.addAll(convert(items.subList(offset, offset + count),first));
                offset = offset+ count;
            }
            catch(Exception exp){
                Log.wtf("AGCY SPY", "WOWOW chill out guy");
                return false;
            }
            return true;
        }
        public Boolean convertMore(int count){
            return convertMore(count, false);
        }
        public abstract List<Item> convert(List<? extends T> source,boolean first);

        public Item get(int position){
            return convertedItems.get(position);
        }
        public int size(){
            return convertedItems.size();
        }

        public boolean isLast(int position){
            return offset == items.size() && position+1 == convertedItems.size();

        }

        public abstract void newItem(T item);
        public void removeConverted(int position){
            convertedItems.remove(position);
        }
        public void removeConverted(Item item){
            convertedItems.remove(item);
        }
    }
}
