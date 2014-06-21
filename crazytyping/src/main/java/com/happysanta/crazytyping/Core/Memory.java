package com.happysanta.crazytyping.Core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.happysanta.crazytyping.Helper.Time;
import com.happysanta.crazytyping.Listeners.NewUpdateListener;
import com.happysanta.crazytyping.Models.ChatDialog;
import com.happysanta.crazytyping.Models.Dialog;
import com.happysanta.crazytyping.Models.Online;
import com.happysanta.crazytyping.Models.Status;
import com.happysanta.crazytyping.Models.Typing;
import com.happysanta.crazytyping.R;
import com.vk.sdk.api.model.VKApiChat;
import com.vk.sdk.api.model.VKApiMessage;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.api.model.VKUsersArray;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;


public class Memory {


    private static int filteredUsersCount;
    public static VKUsersArray users = new VKUsersArray();
    public static ArrayList<Dialog> dialogs = new ArrayList<Dialog>();
    private static Context context;
    private static DatabaseConnector databaseConnector;
    private static SQLiteDatabase database;
    public static ArrayList<Integer> downloadingUsersIds = new ArrayList<Integer>();
    public static ArrayList<Integer> downloadingChatsIds = new ArrayList<Integer>();
    public static boolean targetChanged = false;
    private static HashMap<Long,View> typingUpdateListeners = new HashMap<Long, View>();


    public static void initialize(Context context) {

        Log.i("AGCY SPY SQL","Initialization");
        Memory.context = context;
        databaseConnector = new DatabaseConnector(context);
        open();
        close();

    }

    public static void DESTROY() {
        context.deleteDatabase(DatabaseConnector.DATABASE);
        context = null;
    }
    public static void clearAll(){

        Log.i("AGCY SPY SQL","Clearing databases.");
        clearUsers();
        clearTypings();
        clearOnlines();
        clearNetworks();
        clearChats();
    }

    private static ArrayList<NewUpdateListener> typingListeners = new ArrayList<NewUpdateListener>();
    private static ArrayList<NewUpdateListener> typingOnceListeners = new ArrayList<NewUpdateListener>();

    private static ArrayList<NewUpdateListener> onlineListeners = new ArrayList<NewUpdateListener>();
    private static ArrayList<NewUpdateListener> onlineOnceListeners = new ArrayList<NewUpdateListener>();

    public static void addTypingOnceListener(NewUpdateListener typingOnceListener) {
        if(!typingOnceListeners.contains(typingOnceListener))
            typingOnceListeners.add(typingOnceListener);
    }
    public static void addTypingListener(NewUpdateListener typingListener) {
        if(!typingListeners.contains(typingListener))
            typingListeners.add(typingListener);
    }
    public static void removeTypingListener(NewUpdateListener typingListener){
        typingListeners.remove(typingListener);
    }

    public static void addOnlineOnceListener(NewUpdateListener onlineOnceListener) {
        if(!onlineOnceListeners.contains(onlineOnceListener))
            onlineOnceListeners.add(onlineOnceListener);
    }
    public static void addOnlineListener(NewUpdateListener onlineListener) {
        if(!onlineListeners.contains(onlineListener))
            onlineListeners.add(onlineListener);
    }
    public static void removeOnlineListener(NewUpdateListener onlineListener){
        onlineListeners.remove(onlineListener);
    }

    //region Interface
    public static Boolean loadUsers() {

        ArrayList<VKApiUserFull> loadedUsers = new ArrayList<VKApiUserFull>();
        final Cursor cursor = getCursor(DatabaseConnector.USER_DATABASE,
                DatabaseConnector.USER_DATABASE_FIELDS, null, null, null);

        final int useridColumnIndex = cursor.getColumnIndex("userid");
        final int photoColumnIndex = cursor.getColumnIndex("photo");
        final int first_nameColumnIndex = cursor.getColumnIndex("first_name");
        final int last_nameColumnIndex = cursor.getColumnIndex("last_name");
        if (cursor.moveToFirst())
            do {
                VKApiUserFull user = new VKApiUserFull() {{
                    this.id = cursor.getInt(useridColumnIndex);
                    this.photo_200 = cursor.getString(photoColumnIndex);
                    this.first_name = cursor.getString(first_nameColumnIndex);
                    this.last_name = cursor.getString(last_nameColumnIndex);
                }};
                if (users.getById(user.id) == null)
                    loadedUsers.add(user);

            } while (cursor.moveToNext());
        cursor.close();
        close();
        users.addAll(loadedUsers);
        return !users.isEmpty();
    }
    public static Boolean loadChats(){
/*
        ArrayList<VKApiChat> loadedChats = new ArrayList<VKApiChat>();
        final Cursor cursor = getCursor(DatabaseConnector.CHAT_DATABASE,
                DatabaseConnector.CHAT_DATABASE_FIELDS, null, null, null);

        final int idColumnIndex = cursor.getColumnIndex("id");
        final int titleColumnIndex = cursor.getColumnIndex("title");
        final int photoColumnIndex = cursor.getColumnIndex("photo");
        final int usersColumnIndex = cursor.getColumnIndex("users");

        if (cursor.moveToFirst())
            do {
                VKApiChat chat = new VKApiChat() {{
                    this.id = cursor.getInt(idColumnIndex);
                    this.photo_200 = cursor.getString(photoColumnIndex);
                    this.title = cursor.getString(titleColumnIndex);
                    this.users = parseIds(cursor.getString(usersColumnIndex).split(","));
                }};
                if (chats.getById(chat.id) == null)
                    loadedChats.add(chat);

            } while (cursor.moveToNext());
        cursor.close();
        close();
        chats.addAll(loadedChats);
        return !users.isEmpty();
        */
        return false;
    }

    public static VKApiUserFull getUserById(int userid) {
        if (users.isEmpty()) {
            loadUsers();
        }
        VKApiUserFull user = null;
        if (downloadingUsersIds.contains(userid)) {
            Log.w("AGCY SPY", "No such userid in memory: userid = " + userid);
            user = new VKApiUserFull();
            user.id = userid;
        } else {
            user = users.getById(userid);
            if (user == null) {

                Helper.downloadUser(userid);
                downloadingUsersIds.add(userid);

                user = new VKApiUserFull();
                user.id = userid;
            }
        }


        return user;
    }

    public static ArrayList<VKApiUserFull> getUsers() {
        ArrayList<VKApiUserFull> arrayUsers = new ArrayList<VKApiUserFull>(users);
        return arrayUsers;
    }
    public static ArrayList<VKApiUserFull> getFriends(){
        return getUsers(true);
    }
    public static ArrayList<VKApiUserFull> getUnfriends(){
        return getUsers(false);
    }
    public static ArrayList<VKApiUserFull> getExternals() {
        ArrayList<VKApiUserFull> outputUsers = new ArrayList<VKApiUserFull>();
        for(VKApiUserFull user: users){
            if(user.isFriend == false && user.isTargeted())
                outputUsers.add(user);
        }

        return outputUsers;
    }

    public static ArrayList<VKApiUserFull> getUsers(boolean friends){


        ArrayList<VKApiUserFull> outputUsers = new ArrayList<VKApiUserFull>();
        for(VKApiUserFull user: users){
            if(user.isFriend == friends)
                outputUsers.add(user);
        }

        return outputUsers;
    }


    public static Boolean isTracked(int userid) {

        return getUserById(userid).isTargeted();

    }

    public static Boolean isTracked(VKApiUserFull user) {
        return isTracked(user.id);
    }

    //endregion
    //region Getters
    public synchronized static Cursor getCursor(String databaseName, String[] fields, String selector, String orderby, String limit) {
        open();
        Cursor cursor = null;
        try {
            cursor = database.query(databaseName, fields, selector, null, null, null, orderby, limit);
        }
        catch(Exception exp) {
            HashMap<String,String> expHashmap = new HashMap<String, String>();
            expHashmap.put("databaseName",databaseName);
            expHashmap.put("selector",selector);
            BugSenseHandler.sendExceptionMap(expHashmap,exp);
            //if(cursor!=null){
            //    cursor.close();
            //}
            forceClose();
            //open();
            Log.e("AGCY SPY SQL","Lets recursion start!",exp);
            return getCursor(databaseName,fields,selector,orderby, limit);
        }
        return cursor;
        // don't forget to close !!1
    }
    public static ArrayList<Online> getTrackedOnlines(){
        ArrayList<Online> trackedOnlines = new ArrayList<Online>();

        Log.i("AGCY SPY SQL", "Loading onlines");
        String selector = "";
        for(int i = 0 ; i < users.size()/1000 + 1; i ++) {

            for (int j = i; j < i * 1000 + users.size() % 1000; j++) {
                VKApiUserFull user = users.get(j);
                if (user.isTargeted()) {
                    if (!selector.equals("")) {
                        selector += " or ";
                    }

                    selector += "userid = " + user.id;
                }
            }

            if (selector.equals(""))
                return new ArrayList<Online>();
            trackedOnlines.addAll(getOnlines(selector, "id desc"));

        }
        Collections.sort(trackedOnlines);
        return trackedOnlines;
    }
    public static ArrayList<Online> getOnlines() {

        Log.i("AGCY SPY SQL", "Loading onlines");
        return getOnlines(null, "id desc");
    }

    public static ArrayList<Online> getOnlines(final int userid) {

        Log.i("AGCY SPY SQL", "Loading onlines by userid:" + userid);
        return getOnlines("userid = " + userid, "id desc");
    }

    public static ArrayList<Online> getOnlines(String selector, String order) {

        Cursor cursor = getCursor(DatabaseConnector.DIALOG_DATABASE,
                DatabaseConnector.DIALOG_DATABASE_FIELDS,
                selector,
                order, null);
        int idColumnIndex = cursor.getColumnIndex("id");
        int useridColumnIndex = cursor.getColumnIndex("userid");
        int sinceColumnIndex = cursor.getColumnIndex("since");
        int tillColumnIndex = cursor.getColumnIndex("till");
        int platformColumnIndex = cursor.getColumnIndex("platform");
        ArrayList<Online> onlines = new ArrayList<Online>();

        if (cursor.moveToFirst())
            do {
                int id = cursor.getInt(idColumnIndex);
                int userid = cursor.getInt(useridColumnIndex);
                int since = cursor.getInt(sinceColumnIndex);
                int till = cursor.getInt(tillColumnIndex);
                int platform = cursor.getInt(platformColumnIndex);
                onlines.add(new Online(userid,since,till, id,platform));
                } while (cursor.moveToNext());

        cursor.close();
        close();
        return onlines;

    }



    public static ArrayList<Typing> getTypings() {
        Log.i("AGCY SPY SQL", "Loading typings");
        return getTypings(null);
    }

    public static ArrayList<Typing> getTypingsByUserId(int userid) {
        Log.i("AGCY SPY SQL", "Loading typings by userid:" + userid);
        return getTypings("userid = " + userid);
    }

    public static ArrayList<Typing> getTypings(String selector) {
        ArrayList<Typing> typings = new ArrayList<Typing>();
/*
        Cursor cursor = getCursor(DatabaseConnector.TYPING_DATABASE,
                DatabaseConnector.TYPING_DATABASE_FIELDS,
                selector,
                "time desc", null);
        int useridColumnIndex = cursor.getColumnIndex("userid");
        int timeColumnIndex = cursor.getColumnIndex("time");
        int chatidColumnIndex = cursor.getColumnIndex("chatid");


        if (cursor.moveToFirst())
            do {
                int userid = cursor.getInt(useridColumnIndex);
                int time = cursor.getInt(timeColumnIndex);
                int chatid = cursor.getInt(chatidColumnIndex);
                if (chatid==0) {
                    Typing typing = new Typing(userid, time);
                    typings.add(typing);
                }else{
                    typings.add(new ChatTyping((userid),time,(chatid)));
                }
            } while (cursor.moveToNext());
        cursor.close();
        close();
        */
        return typings;
    }
    //endregion
    //region Setters



    /**
     * Уведомляет слушалки
     * !!! Используется только в главном потоке.
     * @param status
     */
    public static void notifyStatusListeners(Status status){
        VKApiUserFull user = status.getOwner();
        user.last_seen = status.getUnix();
        user.online = status.isOnline();
        user.platform = status.getPlatform();
        for (NewUpdateListener onlineListener : onlineListeners) {
            onlineListener.newItem(status);
        }
        for (NewUpdateListener onlineListener : onlineOnceListeners) {
            onlineListener.newItem(status);
        }
        onlineOnceListeners.clear();
    }


    //endregion
    //region Updaters
    private static void forceUpdate(String databaseName, ContentValues values, String primaryKeyName, String primaryKeyValue) {
        database.update(databaseName, values, primaryKeyName + " = " + primaryKeyValue, null);
    }
    private static void update(String databaseName, ContentValues values, String primaryKeyName, String primaryKeyValue) {
        open();
        forceUpdate(databaseName,values,primaryKeyName,primaryKeyValue);
        close();
    }

    //endregion
    //region Savers
    private static void save(String databaseName, ContentValues values) {
        open();

        forceSave(databaseName, values);

        close();
    }

    private static void forceSave(String databaseName, ContentValues values) {
        database.insert(databaseName, null, values);
    }

    private static void save(String databaseName, ArrayList<ContentValues> valuesList) {

        open();
        for (ContentValues contentValues : valuesList) {
            database.insert(databaseName, null, contentValues);
        }
        close();
    }

    public static void saveUser(VKApiUserFull user) {

        Log.i("AGCY SPY SQL", "Saving user.");
        ContentValues values = new ContentValues();
        values.put("userid", user.id);
        values.put("photo", user.getBiggestPhoto());
        values.put("first_name", user.first_name);
        values.put("last_name", user.last_name);
        save(DatabaseConnector.USER_DATABASE, values);
        users.add(user);
    }

    public static void saveFriends(VKUsersArray friends){

        if(users==null || users.isEmpty()){
            for(int i = 0 ; i < 5 && i < friends.size();i++){
                friends.get(i).targeted = true;
            }
        }

        for(VKApiUserFull friend : friends){
            friend.isFriend = true;
        }
        saveUsers(friends);
    }
    public static ArrayList<Dialog> getDialogs(){
        ArrayList<Dialog> dialogs = new ArrayList<Dialog>();
        open();
        Cursor cursor = getCursor(DatabaseConnector.DIALOG_DATABASE,
                DatabaseConnector.DIALOG_DATABASE_FIELDS,
                null,
                null,
                null);
        int useridColumnIndex = cursor.getColumnIndex("userid");
        int titleColumnIndex = cursor.getColumnIndex("title");
        int chatidColumnIndex = cursor.getColumnIndex("chatid");
        int photoColumnIndex = cursor.getColumnIndex("photo");
        int targetedColumnIndex = cursor.getColumnIndex("targeted");


        if (cursor.moveToFirst())
            do {

                int userid = cursor.getInt(useridColumnIndex);
                int chatid = cursor.getInt(chatidColumnIndex);
                String title = cursor.getString(titleColumnIndex);
                String photo = cursor.getString(photoColumnIndex);
                Boolean targeted = cursor.getInt(targetedColumnIndex)>0;

                if (chatid==0) {
                    Dialog typing = new Dialog(userid, targeted);
                    dialogs.add(typing);
                }else{
                    dialogs.add(new ChatDialog(chatid, title, photo, targeted));
                }

            } while (cursor.moveToNext());
        cursor.close();
        close();
        return dialogs;
    }
    public static void saveDialogs(VKList<VKApiMessage> dialogs){
        ArrayList<ContentValues> valuesList = new ArrayList<ContentValues>();
        ArrayList<Dialog> tempDialogs = new ArrayList<Dialog>();
        for (VKApiMessage dialog : dialogs) {
            Boolean targeted = false;
            ContentValues values = new ContentValues();
            if(dialog.chat_id==0) {
                Dialog savedDialog = getDialog(dialog.user_id);
                if(savedDialog!=null)
                    targeted = savedDialog.targeted;

                tempDialogs.add(new Dialog(dialog.user_id, targeted));
                values.put("userid", dialog.user_id);
            }else{
                ChatDialog savedDialog = getChatDialog(dialog.chat_id);
                if(savedDialog!=null)
                    targeted =  savedDialog.targeted;
                tempDialogs.add(new ChatDialog(dialog.chat_id, dialog.title, dialog.photo, targeted));
                values.put("title", dialog.title);
                values.put("chatid", dialog.chat_id);
                values.put("photo", dialog.photo);
            }
            values.put("targeted",targeted);
            valuesList.add(values);

        }

        clearDatabase(DatabaseConnector.DIALOG_DATABASE);
        save(DatabaseConnector.DIALOG_DATABASE, valuesList);
        Memory.dialogs = tempDialogs;
    }

    private static Dialog getDialog(int user_id) {
        for (Dialog dialog : dialogs) {
            if(dialog.getUserid()==user_id){
                return dialog;
            }
        }
        return null;
    }

    private static ChatDialog getChatDialog(int chat_id) {
        for (Dialog dialog : dialogs) {
            if(dialog instanceof ChatDialog && ((ChatDialog)dialog).getChatId()==chat_id){
                return (ChatDialog) dialog;
            }
        }
        return null;
    }

    public static void saveUsers(VKUsersArray users) {
        Log.i("AGCY SPY SQL", "Saving new users. Count: " + users.size());
        ArrayList<ContentValues> valuesList = new ArrayList<ContentValues>();
        for (VKApiUserFull user : users) {
            ContentValues values = new ContentValues();
            values.put("userid", user.id);
            values.put("photo", user.getBiggestPhoto());
            values.put("first_name", user.first_name);
            values.put("last_name", user.last_name);
            valuesList.add(values);
            Memory.users.add(user);
        }

        clearDatabase(DatabaseConnector.USER_DATABASE);
        save(DatabaseConnector.USER_DATABASE, valuesList);

    }


    //endregion
    //region Clearer

    public static void clearUsers() {
        users.clear();
        clearDatabase(DatabaseConnector.USER_DATABASE);
    }

    public static void clearTypings() {
        //clearDatabase(DatabaseConnector.TYPING_DATABASE);
    }

    public static void clearOnlines() {
        clearDatabase(DatabaseConnector.DIALOG_DATABASE);
    }
    public static void clearChats() {
  //      clearDatabase(DatabaseConnector.CHAT_DATABASE);
    }
    public static void clearNetworks() {
  //      clearDatabase(DatabaseConnector.NETWORK_DATABASE);
    }

    private static void clearDatabase(String databaseName) {
        open();
        Log.i("AGCY SPY SQL", "Clearing database " + databaseName);
        database.delete(databaseName, null, null);
        close();
    }
    //endregion

    public static int dbOperations = 0;

    public synchronized static void open() {
        dbOperations++;
        if (database == null) {
            database = databaseConnector.getWritableDatabase();
        } else {
            if (database.isOpen()) {
                if (dbOperations == 0) {
                    Log.wtf("AGCY SPY SQL", "DB was opened Oo who did leave it opened?");
                    forceClose();
                    database = databaseConnector.getWritableDatabase();
                }
            }

        }
        //Log.i("AGCY SPY SQL", "New operation. Count of operation: " + dbOperations);
    }

    public static void close() {
        dbOperations--;
        if (dbOperations == 0) {
            forceClose();
        }
        //Log.i("AGCY SPY SQL", "Operation ended. Count of operation: " + dbOperations);
    }

    public static void forceClose() {
        if (database != null) {

            database.close();
            database = null;
        }
        databaseConnector.close();
    }

    public static int getCountOfTracked() {
        filteredUsersCount = 0;
        for (VKApiUserFull user : users) {
            if (user.isTargeted()) {
                filteredUsersCount++;
            }
        }
        return filteredUsersCount;
    }

    public static void saveNetwork(boolean connected) {

        ContentValues networkValues = new ContentValues();
        networkValues.put("connected", connected);
        networkValues.put("time", Time.getUnixNow());

        //save(DatabaseConnector.NETWORK_DATABASE, networkValues);
    }


    public static void loadDialogs() {
        dialogs = getDialogs();

    }

    public static Boolean targetDialog(long id) {
        targetChanged = true;
        for (Dialog dialog : dialogs) {
            if(dialog.getId()==id){
                dialog.targeted = !dialog.targeted;
                if(dialog instanceof ChatDialog)
                    saveChatDialogTargeted(id - 2000000000, dialog.targeted);
                else
                    saveDialogTargeted(id, dialog.targeted);
                return dialog.targeted;
            }
        }
        return false;
    }

    private static void saveChatDialogTargeted(long id, boolean targeted) {
        ContentValues values = new ContentValues();
        values.put("targeted",targeted);
        update(DatabaseConnector.DIALOG_DATABASE, values, "chatid", "" + id);
    }

    private static void saveDialogTargeted(long id, boolean targeted) {
        ContentValues values = new ContentValues();
        values.put("targeted",targeted);
        update(DatabaseConnector.DIALOG_DATABASE, values, "userid", "" + id);
    }

    public static ArrayList<Dialog> getTargetedDialogs() {
        ArrayList<Dialog> tempDialogs = new ArrayList<Dialog>();
        for (Dialog dialog : dialogs) {
            if(dialog.targeted)
                tempDialogs.add(dialog);
        }

        return tempDialogs;
    }
    static Handler handler = new Handler();
    public static void notifyTypingUpdate(String[] split) {
        Log.i("crazy","typing notify: " + Arrays.toString(split));
        for (String typingUpdate : split) {
            long idDialog = Long.parseLong(typingUpdate);
            if(typingUpdateListeners.containsKey(idDialog)){


                final AlphaAnimation normalTypingHiding = new AlphaAnimation(0.5f,1);
                normalTypingHiding.setInterpolator(new DecelerateInterpolator());
                normalTypingHiding.setStartOffset(10000);
                normalTypingHiding.setDuration(5000);


                final View dialogView = typingUpdateListeners.get(idDialog);
                final View typingView = dialogView.findViewById(R.id.typing_active);
                final View normalTypingView = dialogView.findViewById(R.id.typing);
                final AlphaAnimation blinkAnimation = new AlphaAnimation(1, 0);
                blinkAnimation.setInterpolator(new DecelerateInterpolator());
                blinkAnimation.setDuration(5 * 1000);
                blinkAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                    typingView.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                typingView.setVisibility(View.INVISIBLE);
                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        typingView.startAnimation(blinkAnimation);
                        normalTypingView.startAnimation(normalTypingHiding);
                    }
                });
            }
        }
    }

    public static void setTypingListeners(ArrayList<View> typingListeners) {
        Memory.typingUpdateListeners.clear();
        for (View typingListener : typingListeners) {
            typingUpdateListeners.put((Long) typingListener.getTag(), typingListener);
        }
    }
    public static void addTypingListener(View listener){
        typingUpdateListeners.put((Long) listener.getTag(),listener);
    }

    public static void removeTypingListener(View view) {
        typingUpdateListeners.remove(view.getTag());
    }


    private static class DatabaseConnector extends SQLiteOpenHelper {

        private static final String DATABASE = "crazytyping.db";

        private static final String DIALOG_DATABASE = "dialog_database";
        private static final String USER_DATABASE = "user_database";
        private static final String[] DIALOG_DATABASE_FIELDS = {
                "id",
                "userid",
                "chatid",
                "title",
                "photo",
                "targeted"
        };
        private static final String[] USER_DATABASE_FIELDS = {
                "userid",
                "photo",
                "first_name",
                "last_name"
        };
        private static final String DIALOG_DATABASE_CREATE =
                "create table " +
                        DIALOG_DATABASE +
                        " ( " +
                            "id integer primary key autoincrement," +
                            "userid integer default 0," +
                            "chatid integer default 0," +
                            "title TEXT," +
                            "photo TEXT," +
                            "targeted INTEGER DEFAULT 0" +
                        " ) ";
        private static final String USER_DATABASE_CREATE =
                "create table " +
                        USER_DATABASE +
                        " ( " +
                            "userid integer primary key," +
                            "photo text not null," +
                            "first_name text not null," +
                            "last_name text not null" +
                        " ) ";

        public DatabaseConnector(Context context) {
            super(context, DATABASE, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            database.execSQL(DIALOG_DATABASE_CREATE);
            database.execSQL(USER_DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

        }

    }
}