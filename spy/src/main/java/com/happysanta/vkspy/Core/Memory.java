package com.happysanta.vkspy.Core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.util.Log;

import com.happysanta.vkspy.Fragments.UsersListFragment;
import com.happysanta.vkspy.Helper.Time;
import com.happysanta.vkspy.Listeners.NewUpdateListener;
import com.happysanta.vkspy.Models.ChatTyping;
import com.happysanta.vkspy.Models.DurovOnline;
import com.happysanta.vkspy.Models.Online;
import com.happysanta.vkspy.Models.Status;
import com.happysanta.vkspy.Models.Typing;
import com.bugsense.trace.BugSenseHandler;
import com.vk.sdk.api.model.VKApiChat;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.api.model.VKUsersArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;


public class Memory {


    private static int filteredUsersCount;
    public static VKUsersArray users = new VKUsersArray();
    public static VKList<VKApiChat> chats = new VKList<VKApiChat>();
    private static Context context;
    private static DatabaseConnector databaseConnector;
    private static SQLiteDatabase database;
    public static ArrayList<Integer> downloadingUsersIds = new ArrayList<Integer>();
    public static ArrayList<Integer> downloadingChatsIds = new ArrayList<Integer>();


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
                DatabaseConnector.USER_DATABASE_FIELDS, null, "hint", null);

        final int useridColumnIndex = cursor.getColumnIndex("userid");
        final int sexColumnIndex = cursor.getColumnIndex("sex");
        final int photoColumnIndex = cursor.getColumnIndex("photo");
        final int first_nameColumnIndex = cursor.getColumnIndex("first_name");
        final int last_nameColumnIndex = cursor.getColumnIndex("last_name");
        final int isFriend_ColumnIndex = cursor.getColumnIndex("isFriend");
        final int tracked_ColumnIndex = cursor.getColumnIndex("tracked");
        if (cursor.moveToFirst())
            do {
                VKApiUserFull user = new VKApiUserFull() {{
                    this.id = cursor.getInt(useridColumnIndex);
                    this.photo_200 = cursor.getString(photoColumnIndex);
                    this.first_name = cursor.getString(first_nameColumnIndex);
                    this.last_name = cursor.getString(last_nameColumnIndex);
                    this.sex = cursor.getInt(sexColumnIndex);
                    this.isFriend = cursor.getInt(isFriend_ColumnIndex)>0;
                    this.tracked = cursor.getInt(tracked_ColumnIndex)>0;
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
    }

    public static VKApiChat getChatById(int chatId){
        if(chats.isEmpty()){
            loadChats();
        }
        VKApiChat chat = null;
        if(downloadingChatsIds.contains(chatId)){
            Log.w("AGCY SPY", "No such chatid in memory: chatid = " + chatId);
            chat = new VKApiChat();
            chat.id = chatId;
        }else{
            for (VKApiChat vkApiChat : chats) {
                if(vkApiChat.id == chatId){
                    chat = vkApiChat;
                    break;
                }
            }
            if (chat == null) {

                Helper.downloadChat(chatId);
                downloadingChatsIds.add(chatId);

                chat = new VKApiChat();
                chat.id = chatId;
            }
        }
        return chat;
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
            if(user.isFriend == false && user.isTracked())
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

        return getUserById(userid).isTracked();

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
                if (user.isTracked()) {
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

        Cursor cursor = getCursor(DatabaseConnector.ONLINE_DATABASE,
                DatabaseConnector.ONLINE_DATABASE_FIELDS,
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

        Cursor cursor = getCursor(DatabaseConnector.TYPING_DATABASE,
                DatabaseConnector.TYPING_DATABASE_FIELDS,
                selector,
                "time desc", null);
        int useridColumnIndex = cursor.getColumnIndex("userid");
        int timeColumnIndex = cursor.getColumnIndex("time");
        int chatidColumnIndex = cursor.getColumnIndex("chatid");

        ArrayList<Typing> typings = new ArrayList<Typing>();

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
        return typings;
    }
    //endregion
    //region Setters

    public static Boolean setTracked(VKApiUserFull user) {
        user.tracked = updateTrack(user.id);
        Helper.trackedUpdated();
        return user.tracked;

    }
    public static void setTracked(ArrayList<VKApiUserFull> tracked) {
        Log.i("AGCY SPY SQL","Saving tracks: " + tracked.size());
        open();
        database.execSQL("update " + DatabaseConnector.USER_DATABASE + " set tracked = 0");
        for (VKApiUserFull user : users) {
            user.tracked= false;

        };
        for (VKApiUserFull user : tracked) {
            int id = user.id;
            user.tracked = true;
            ContentValues updateValues = new ContentValues();
            updateValues.put("userid", id);
            updateValues.put("tracked", true);
            forceUpdate(DatabaseConnector.USER_DATABASE, updateValues, "userid", String.valueOf(id));
        }
        close();
    }


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
    /**
     * Метод сохраняет\обновляет запись в бд, но и уведомляет все listeners, которым нужен этот статус
     * !!! Не забываем главный поток
     * @param user
     * @param online
     * @param platform
     *
     */

    public static void setStatus(VKApiUserFull user, Boolean online, boolean timeout, Integer platform) {
        setStatus(user,online, Time.getUnixNow() - (timeout ? 15 * 60 : 0), platform);

    }

    public static void setStatus(final VKApiUserFull user, final Boolean online, final int time, final Integer platform){
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {

                if(!forceSetStatus(user,online,time, platform)){
                    return;
                }


                if (user.isTracked())
                    handler.post(new Runnable() {
                        @Override
                        public void run() {Status status = new Status(user.id,  time, online, platform);
                            notifyStatusListeners(status);
                        }
                    });
            }
        }).start();
    }
    public static boolean forceSetStatus(VKApiUserFull user, Boolean online, int time, Integer platform){
        user.online = online;
        if (online) {
            return setOnline(user, time, platform);
        } else {
            return setOffline(user, time, platform);
        }
    }
    public static Boolean setOnline(VKApiUserFull user, int time, Integer platform){
        user.online = true;
        return  saveStatus(user, true, time, platform);
    }
    public static Boolean setOffline(VKApiUserFull user, int time, Integer platform){

        user.last_seen = time;
        return (updateStatus(user, false, time, platform));
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

    public static Boolean updateTrack(int userid) {


        Log.i("AGCY SPY SQL", "Updating track.");

        Cursor cursor = getCursor(DatabaseConnector.USER_DATABASE,
                DatabaseConnector.USER_DATABASE_FIELDS,
                "userid = " + userid,null, null);

        int trackedColumnIndex = cursor.getColumnIndex("tracked");

        Boolean tracked = false;
        if (cursor.moveToFirst()) {
            tracked = cursor.getInt(trackedColumnIndex) > 0;

            ContentValues values = new ContentValues();
            values.put("tracked", !tracked);
            tracked = !tracked;
            forceUpdate(DatabaseConnector.USER_DATABASE, values, "userid", String.valueOf(userid));

        }
        cursor.close();
        close();
        return tracked;

    }


    public static Boolean updateStatus(VKApiUserFull user, Boolean online, long time, Integer platform) {
        return updateStatus(user, online, time, 120, platform);
    }
    /**
     *  Находит последнюю запись по пользователю, и, если это не повторка (+-120секунд), то обновляет запись
     * @param user
     * @param online
     * @param time
     * @param inaccuracy допустимая погрешность, в пределах которой можно смотреть
     * @param platform айди платформы
     * @return true, если обновление прошло успешно, и данные были занесены. Если это повторка,
     * то занесено не будет.
     */
    public static Boolean updateStatus(VKApiUserFull user, Boolean online, long time, int inaccuracy, Integer platform) {
        if (user.id == 1)
            return false;

        Log.i("AGCY SPY SQL", "Updating status.");

        Cursor cursor = getCursor(DatabaseConnector.ONLINE_DATABASE,
                DatabaseConnector.ONLINE_DATABASE_FIELDS,
                "userid = " + user.id,
                "id desc", null);
        int idColumnIndex = cursor.getColumnIndex("id");
        int useridColumnIndex = cursor.getColumnIndex("userid");
        int sinceColumnIndex = cursor.getColumnIndex("since");
        int tillColumnIndex = cursor.getColumnIndex("till");
        int platformColumnIndex = cursor.getColumnIndex("platform");
        Boolean value = false;
        if (cursor.moveToFirst()) {
            int lastOnline = cursor.getInt(sinceColumnIndex);
            int lastOffline = cursor.getInt(tillColumnIndex);

            if (online) {
               value= saveStatus(user, true, (int) time, platform);
            } else {
                if (lastOffline == 0) {
                    if(lastOnline>time){
                        time = lastOnline+1;
                    }
                    // Если в конце открытый онлайн
                    int id = cursor.getInt(idColumnIndex);
                    ContentValues updateValues = new ContentValues();
                    updateValues.put("userid", cursor.getInt(useridColumnIndex));
                    updateValues.put("since", lastOnline);
                    updateValues.put("till", time);
                    //updateValues.put("platform",platform);
                    update(DatabaseConnector.ONLINE_DATABASE, updateValues, "id", String.valueOf(id));
                    value = true;
                } else {
                    // Но если это "повторка" оффлайна, скажем, мы по ошибке пытаемся сохранить
                    // уже схваченный оффлайн.
                    // или, возможно это ошибка лонгпола\апи, и юзер выходит повторно
                    if ((lastOffline - inaccuracy < time && lastOffline + inaccuracy > time) ||
                            lastOffline>time) {
                        cursor.close();
                        close();
                        return false;
                    }
                    value = saveStatus(user, online, (int) time, platform);

                }
            }
        } else {
          value  = saveStatus(user, online, (int) time, platform);
        }

        cursor.close();
        close();
        return value;
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
        values.put("sex", user.sex);
        values.put("photo", user.getBiggestPhoto());
        values.put("first_name", user.first_name);
        values.put("last_name", user.last_name);
        values.put("isFriend", user.isFriend);
        values.put("tracked", user.tracked);
        save(DatabaseConnector.USER_DATABASE, values);
        users.add(user);
    }

    public static void saveFriends(VKUsersArray friends){

        if(users==null || users.isEmpty()){
            for(int i = 0 ; i < 5 && i < friends.size();i++){
                friends.get(i).tracked = true;
            }
        }

        for(VKApiUserFull friend : friends){
            friend.isFriend = true;
        }
        saveUsers(friends);
    }

    public static void saveUsers(VKUsersArray newUsers) {

        VKUsersArray alreadyStoredUsers = new VKUsersArray(Memory.users);
        for (VKApiUserFull alreadyStoredUser : alreadyStoredUsers) {
            VKApiUserFull newUser = newUsers.getById(alreadyStoredUser.id);
            if (newUser == null) {
                alreadyStoredUser.isFriend = false;
                newUsers.add(alreadyStoredUser);
            }else{
                newUser.tracked = alreadyStoredUser.tracked;
            }
        }
        Memory.users = newUsers;

        Log.i("AGCY SPY SQL", "Saving new users. Count: " + newUsers.size());
        ArrayList<ContentValues> valuesList = new ArrayList<ContentValues>();
        for (int i = 0; i < Memory.users.size(); i++) {
            VKApiUserFull user = Memory.users.get(i);
            ContentValues values = new ContentValues();
            values.put("userid", user.id);
            values.put("sex", user.sex);
            values.put("photo", user.getBiggestPhoto());
            values.put("first_name", user.first_name);
            values.put("last_name", user.last_name);
            values.put("isFriend",user.isFriend);
            values.put("tracked",user.tracked);
            values.put("hint", i);
            valuesList.add(values);
        }

        clearDatabase(DatabaseConnector.USER_DATABASE);
        save(DatabaseConnector.USER_DATABASE, valuesList);

    }
    public static void saveDurovOnlines(ArrayList<DurovOnline> onlines){

        Cursor cursor = getCursor(DatabaseConnector.ONLINE_DATABASE,
                DatabaseConnector.ONLINE_DATABASE_FIELDS,
                "userid = 1",
                "id desc", null);
        int idColumnIndex = cursor.getColumnIndex("id");
        int tillColumnIndex = cursor.getColumnIndex("till");


        if (cursor.moveToFirst()) {
            int id = cursor.getInt(idColumnIndex);
            int till = cursor.getInt(tillColumnIndex);
            if(till==0){
                removeOnline(id);
            }
        }
        VKApiUserFull durov = new VKApiUserFull();
        durov.id = 1;
        for (DurovOnline online : onlines) {
            saveOnline(durov,online.from,online.to, 7);
        }
        cursor.close();
        close();
    }

    private static void removeOnline(int id) {
        database.delete(DatabaseConnector.ONLINE_DATABASE, "id = " + id, null);
    }

    private static void saveOnline(VKApiUserFull user, int since, int till, Integer platform){

        ContentValues values = new ContentValues();
        values.put("userid", user.id);
        values.put("since", since);
        values.put("till", till);
        values.put("platform", platform);
        forceSave(DatabaseConnector.ONLINE_DATABASE, values);
    }

    /**
     * Создаёт новую запись  ONLINES_DATABASE, где один из двух ( till или sine ) будет 0.
     * @param user под кем сохранить
     * @param online online = true, offline = false
     * @param time точное время, когда зашёл
     * @param platform
     */
    public static Boolean saveStatus(VKApiUserFull user, boolean online, int time, Integer platform) {
        if(user.id==1){
            return false;
        }
        Cursor cursor = getCursor(DatabaseConnector.ONLINE_DATABASE,
                DatabaseConnector.ONLINE_DATABASE_FIELDS,
                "userid = " + user.id,
                "id desc", null);
        int idColumnIndex = cursor.getColumnIndex("id");
        int useridColumnIndex = cursor.getColumnIndex("userid");
        int sinceColumnIndex = cursor.getColumnIndex("since");
        int tillColumnIndex = cursor.getColumnIndex("till");
        int platformColumnIndex = cursor.getColumnIndex("platform");

        if (cursor.moveToFirst()) {
            int lastOnline = cursor.getInt(sinceColumnIndex);
            int lastOffline = cursor.getInt(tillColumnIndex);
            int lastPlatform = cursor.getInt(platformColumnIndex);

            if (online) {
                if (lastOnline - 300 < time && lastOnline + 300 > time && lastPlatform == platform) {
                    cursor.close();
                    close();
                    return false;
                }
            } else {
                if(lastOffline - 300 < time && lastOffline + 300 > time){
                    cursor.close();
                    close();
                    return false;
                }
            }
        }
        Log.i("AGCY SPY SQL", "Saving online.");
        ContentValues values = new ContentValues();
        values.put("userid", user.id);
        values.put(online ? "since" : "till", time);
        if(platform!=null)
            values.put("platform", platform);
        forceSave(DatabaseConnector.ONLINE_DATABASE, values);
        cursor.close();
        close();
        return true;
    }


    public static void saveTyping(int userid) {
        saveTyping((userid), 0);
    }
    public static void saveTyping(int userid, int chatid){

        Log.i("AGCY SPY SQL", "Saving typing.");
        ContentValues values = new ContentValues();
        values.put("userid", userid);
        values.put("chatid", chatid);
        values.put("time", Time.getUnixNow() - 3 * 60);

        save(DatabaseConnector.TYPING_DATABASE, values);
    }
    //endregion
    //region Clearer

    public static void clearUsers() {
        users.clear();
        clearDatabase(DatabaseConnector.USER_DATABASE);
    }

    public static void clearTypings() {
        clearDatabase(DatabaseConnector.TYPING_DATABASE);
    }

    public static void clearOnlines() {
        clearDatabase(DatabaseConnector.ONLINE_DATABASE);
    }
    public static void clearChats() {
        clearDatabase(DatabaseConnector.CHAT_DATABASE);
    }
    public static void clearNetworks() {
        clearDatabase(DatabaseConnector.NETWORK_DATABASE);
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
            if (user.isTracked()) {
                filteredUsersCount++;
            }
        }
        return filteredUsersCount;
    }
    public static void setChatTyping(VKApiUserFull user, VKApiChat chat){
        ChatTyping typing = new ChatTyping(user, Time.getUnixNow()- 180, chat);
        for (NewUpdateListener typingListener : typingListeners) {
            typingListener.newItem(typing);
        }
        for (NewUpdateListener typingOnceListener : typingOnceListeners) {
            typingOnceListener.newItem(typing);
        }
        typingOnceListeners.clear();

        saveTyping(user.id,chat.id);
    }
    public static void setTyping(VKApiUserFull user) {

        Typing typing = new Typing(user, Time.getUnixNow() - 180, 0);
        for (NewUpdateListener typingListener : typingListeners) {
            typingListener.newItem(typing);
        }
        for (NewUpdateListener typingOnceListener : typingOnceListeners) {
            typingOnceListener.newItem(typing);
        }
        typingOnceListeners.clear();

        saveTyping(user.id);
    }
    private static UsersListFragment.UsersListener usersListener;
    public static void addUsersListener(UsersListFragment.UsersListener usersListener){
        Memory.usersListener = usersListener;
    }
    public static void reloadFriends() {
        if(usersListener!=null)
            usersListener.reload();
    }

    public static boolean getLastOnline(int id) {
        Cursor cursor = getCursor(DatabaseConnector.ONLINE_DATABASE,
                DatabaseConnector.ONLINE_DATABASE_FIELDS,
                "userid = " + id,
                "id desc", "1");
        int tillColumnIndex = cursor.getColumnIndex("till");
        if (cursor.moveToFirst()) {
            int lastOffline = cursor.getInt(tillColumnIndex);
            cursor.close();
            close();
            return (lastOffline<=0);

        }
        cursor.close();
        close();
        return false;
    }

    public static void saveNetwork(boolean connected) {

        ContentValues networkValues = new ContentValues();
        networkValues.put("connected", connected);
        networkValues.put("time", Time.getUnixNow());

        save(DatabaseConnector.NETWORK_DATABASE, networkValues);
    }

    public static void saveChat(VKApiChat chat) {

        Log.i("AGCY SPY SQL", "Saving chat.");
        ContentValues values = new ContentValues();
        values.put("id", chat.id);
        values.put("title", chat.title);
        values.put("photo", chat.getBiggestPhoto());
        values.put("users", Arrays.toString(chat.users).replace("[","").replace("]",""));
        save(DatabaseConnector.CHAT_DATABASE, values);
        chats.add(chat);

    }


    private static class DatabaseConnector extends SQLiteOpenHelper {

        private static final String DATABASE = "vkspy.db";

        private static final String ONLINE_DATABASE = "online_database";
        private static final String TYPING_DATABASE = "typing_database";
        private static final String USER_DATABASE = "user_database";
        private static final String NETWORK_DATABASE = "network_database";
        public static final String CHAT_DATABASE = "chat_database";
        private static final String[] ONLINE_DATABASE_FIELDS = {
                "id",
                "userid",
                "since",
                "till",
                "platform"
        };
        private static final String[] TYPING_DATABASE_FIELDS = {
                "id",
                "userid",
                "time",
                "chatid"
        };
        private static final String[] USER_DATABASE_FIELDS = {
                "userid",
                "sex",
                "photo",
                "first_name",
                "last_name",
                "isFriend",
                "tracked"
        };
        public static final String[] CHAT_DATABASE_FIELDS = {
                "id",
                "title",
                "photo",
                "users"
        };
        private static final String USER_DATABASE_CREATE =
                "create table " +
                        USER_DATABASE +
                        " ( " +
                        "userid integer primary key," +
                        "sex integer not null," +
                        "photo text not null," +
                        "first_name text not null," +
                        "last_name text not null," +
                        "hint int DEFAULT 99999," +
                        "isFriend int default 0,"+
                        "tracked int default 0" +
                        " ) ";
        private static final String ONLINE_DATABASE_CREATE =
                "create table " +
                        ONLINE_DATABASE +
                        " ( " +
                        "id integer primary key autoincrement," +
                        "userid integer not null," +
                        "since integer default 0," +
                        "till integer default 0," +
                        "platform integer default 7"+
                        " ) ";
        private static final String TYPING_DATABASE_CREATE =
                "create table " +
                        TYPING_DATABASE +
                        " ( " +
                        "id integer primary key autoincrement," +
                        "userid integer not null," +
                        "time integer not null," +
                        "chatid integer default 0" +
                        " ) ";
        public static final String NETWORK_DATABASE_CREATE =
                "create table " +
                        NETWORK_DATABASE +
                        " ( " +
                        "id integer primary key autoincrement," +
                        "connected integer not null," +
                        "time integer not null" +
                        " ) ";
        public static final String CHAT_DATABASE_CREATE =
                " create table " +
                        CHAT_DATABASE +
                        " ( " +
                        " id integer primary key, " +
                        " title text, " +
                        " photo text, " +
                        " users text "+
                        " ) "
                ;

        public DatabaseConnector(Context context) {
            super(context, DATABASE, null, 14);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            database.execSQL(ONLINE_DATABASE_CREATE);
            database.execSQL(TYPING_DATABASE_CREATE);
            database.execSQL(USER_DATABASE_CREATE);
            database.execSQL(CHAT_DATABASE_CREATE);
            database.execSQL(NETWORK_DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

            if(oldVersion<11){
                database.execSQL(CHAT_DATABASE_CREATE);
            }
            if(oldVersion<12){
                database.execSQL("alter table "+ TYPING_DATABASE+" add chatid default 0");
            }
            if(oldVersion<13) {
                database.execSQL("drop table if exists " + NETWORK_DATABASE);
                database.execSQL(NETWORK_DATABASE_CREATE);
            }
            if(oldVersion<14){

                database.execSQL("alter table "+ ONLINE_DATABASE+" add platform default 7");
            }
        }

    }
}