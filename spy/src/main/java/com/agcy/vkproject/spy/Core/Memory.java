package com.agcy.vkproject.spy.Core;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.util.Log;

import com.agcy.vkproject.spy.Fragments.UsersListFragment;
import com.agcy.vkproject.spy.Listeners.NewUpdateListener;
import com.agcy.vkproject.spy.Models.DurovOnline;
import com.agcy.vkproject.spy.Models.Online;
import com.agcy.vkproject.spy.Models.Status;
import com.agcy.vkproject.spy.Models.Typing;
import com.bugsense.trace.BugSenseHandler;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKUsersArray;

import java.util.ArrayList;
import java.util.HashMap;


public class Memory {


    private static int filteredUsersCount;
    public static VKUsersArray users = new VKUsersArray();
    private static Context context;
    private static DatabaseConnector databaseConnector;
    private static SQLiteDatabase database;
    public static ArrayList<Integer> downloadingIds = new ArrayList<Integer>();


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
                DatabaseConnector.USER_DATABASE_FIELDS, null, "hint");

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
        close();
        users.addAll(loadedUsers);
        return !users.isEmpty();
    }



    public static VKApiUserFull getUserById(int userid) {
        if (users.isEmpty()) {
            loadUsers();
        }
        VKApiUserFull user = null;
        if (downloadingIds.contains(userid)) {
            Log.w("AGCY SPY", "No such userid in memory: userid = " + userid);
            user = new VKApiUserFull();
            user.id = userid;
        } else {
            user = users.getById(userid);
            if (user == null) {

                Helper.downloadUser(userid);
                downloadingIds.add(userid);

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

        ArrayList<VKApiUserFull> friends = new ArrayList<VKApiUserFull>();
        for(VKApiUserFull user: users){
            if(user.isFriend)
                friends.add(user);
        }

        return friends;
    }


    public static Boolean isTracked(int userid) {

        return getUserById(userid).isTracked();

    }

    public static Boolean isTracked(VKApiUserFull user) {
        return isTracked(user.id);
    }

    //endregion
    //region Getters
    public static Cursor getCursor(String databaseName, String[] fields, String selector, String orderby) {
        open();
        Cursor cursor;
        try {
            cursor = database.query(databaseName, fields, selector, null, null, null, orderby);
        }
        catch(Exception exp) {
            HashMap<String,String> expHashmap = new HashMap<String, String>();
            expHashmap.put("databaseName",databaseName);
            expHashmap.put("selector",selector);
            BugSenseHandler.sendExceptionMap(expHashmap,exp);
            close();
            open();
            return getCursor(databaseName,fields,selector,orderby);
        }
        return cursor;
        // don't forget to close !!1
    }
    public static ArrayList<Online> getTrackedOnlines(){
        Log.i("AGCY SPY SQL", "Loading onlines");
        String selector = "";
        for(VKApiUserFull user:users){
            if(user.isTracked()) {
                if (!selector.equals("")) {
                    selector += " or ";
                }

                selector += "userid = " + user.id;
            }
        }
        if(selector.equals(""))
            return new ArrayList<Online>();
        return getOnlines(selector, "id desc");
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
                order);
        int useridColumnIndex = cursor.getColumnIndex("userid");
        int sinceColumnIndex = cursor.getColumnIndex("since");
        int tillColumnIndex = cursor.getColumnIndex("till");

        ArrayList<Online> onlines = new ArrayList<Online>();

        if (cursor.moveToFirst())
            do {
                int userid = cursor.getInt(useridColumnIndex);
                int since = cursor.getInt(sinceColumnIndex);
                int till = cursor.getInt(tillColumnIndex);
                onlines.add(new Online(userid,since,till));
                } while (cursor.moveToNext());

        close();
        return onlines;

    }



    public static ArrayList<Typing> getTyping() {
        Log.i("AGCY SPY SQL", "Loading typings");
        return getTyping(null, null);
    }

    public static ArrayList<Typing> getTyping(int userid) {
        Log.i("AGCY SPY SQL", "Loading typings by userid:" + userid);
        return getTyping("userid = " + userid, null);
    }

    public static ArrayList<Typing> getTyping(String selector, String order) {

        Cursor cursor = getCursor(DatabaseConnector.TYPING_DATABASE,
                DatabaseConnector.TYPING_DATABASE_FIELDS,
                selector,
                "time desc");
        int useridColumnIndex = cursor.getColumnIndex("userid");
        int timeColumnIndex = cursor.getColumnIndex("time");

        ArrayList<Typing> typings = new ArrayList<Typing>();

        if (cursor.moveToFirst())
            do {
                int userid = cursor.getInt(useridColumnIndex);
                int time = cursor.getInt(timeColumnIndex);
                Typing typing = new Typing(userid, time);
                typings.add(typing);
            } while (cursor.moveToNext());
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
     *
     */
    public static void setStatus(VKApiUserFull user, Boolean online, boolean timeout) {
        setStatus(user,online,Helper.getUnixNow() - (timeout ? 15 * 60 : 0));

    }
    public static void setStatus(final VKApiUserFull user, final Boolean online, final int time){
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {

                if(!forceSetStatus(user,online,time)){
                    return;
                }


                if (user.isTracked())
                    handler.post(new Runnable() {
                        @Override
                        public void run() {Status status = new Status(user.id,  time, online);
                            notifyStatusListeners(status);
                        }
                    });
            }
        }).start();
    }
    public static boolean forceSetStatus(VKApiUserFull user, Boolean online, int time){
        user.online = online;
        if (online) {
            return setOnline(user, time);
        } else {
            return setOffline(user, time);
        }
    }
    public static Boolean setOnline(VKApiUserFull user, int time){
        user.online = true;
        return  saveStatus(user, true, time);
    }
    public static Boolean setOffline(VKApiUserFull user, int time){

        user.last_seen = time;
        return (updateStatus(user, false, time));
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
                "userid = " + userid,null);

        int trackedColumnIndex = cursor.getColumnIndex("tracked");

        Boolean tracked = false;
        if (cursor.moveToFirst()) {
            tracked = cursor.getInt(trackedColumnIndex) > 0;

            ContentValues values = new ContentValues();
            values.put("tracked", !tracked);
            tracked = !tracked;
            forceUpdate(DatabaseConnector.USER_DATABASE, values, "userid", String.valueOf(userid));

        }
        close();
        return tracked;

    }


    public static Boolean updateStatus(VKApiUserFull user, Boolean online, long time) {
        return updateStatus(user,online,time,120);
    }
    /**
     *  Находит последнюю запись по пользователю, и, если это не повторка (+-120секунд), то обновляет запись
     * @param user
     * @param online
     * @param time
     * @param inaccuracy допустимая погрешность, в пределах которой можно смотреть
     * @return true, если обновление прошло успешно, и данные были занесены. Если это повторка,
     * то занесено не будет.
     */
    public static Boolean updateStatus(VKApiUserFull user, Boolean online, long time,int inaccuracy) {
        if (user.id == 1)
            return false;

        Log.i("AGCY SPY SQL", "Updating status.");

        Cursor cursor = getCursor(DatabaseConnector.ONLINE_DATABASE,
                DatabaseConnector.ONLINE_DATABASE_FIELDS,
                "userid = " + user.id,
                "id desc");
        int idColumnIndex = cursor.getColumnIndex("id");
        int useridColumnIndex = cursor.getColumnIndex("userid");
        int sinceColumnIndex = cursor.getColumnIndex("since");
        int tillColumnIndex = cursor.getColumnIndex("till");
        Boolean value = false;
        if (cursor.moveToFirst()) {
            int lastOnline = cursor.getInt(sinceColumnIndex);
            int lastOffline = cursor.getInt(tillColumnIndex);

            if (online) {
               value= saveStatus(user, true, (int) time);
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
                    update(DatabaseConnector.ONLINE_DATABASE, updateValues, "id", String.valueOf(id));
                    value = true;
                } else {
                    // Но если это "повторка" оффлайна, скажем, мы по ошибке пытаемся сохранить
                    // уже схваченный оффлайн.
                    // или, возможно это ошибка лонгпола\апи, и юзер выходит повторно
                    if ((lastOffline - inaccuracy < time && lastOffline + inaccuracy > time) ||
                            lastOffline>time) {
                        close();
                        return false;
                    }
                    value = saveStatus(user, online, (int) time);

                }
            }
        } else {
          value  = saveStatus(user, online, (int) time);
        }

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

        clearDatabase(DatabaseConnector.USER_DATABASE, DatabaseConnector.USER_DATABASE_CREATE);
        save(DatabaseConnector.USER_DATABASE, valuesList);

    }
    public static void saveDurovOnlines(ArrayList<DurovOnline> onlines){

        Cursor cursor = getCursor(DatabaseConnector.ONLINE_DATABASE,
                DatabaseConnector.ONLINE_DATABASE_FIELDS,
                "userid = 1",
                "id desc");
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
            saveOnline(durov,online.from,online.to);
        }
        close();
    }

    private static void removeOnline(int id) {
        database.delete(DatabaseConnector.ONLINE_DATABASE, "id = " + id, null);
    }

    private static void saveOnline(VKApiUserFull user, int since, int till){

        ContentValues values = new ContentValues();
        values.put("userid", user.id);
        values.put("since", since);
        values.put("till", till);
        forceSave(DatabaseConnector.ONLINE_DATABASE, values);
    }

    /**
     * Создаёт новую запись  ONLINES_DATABASE, где один из двух ( till или sine ) будет 0.
     * @param user под кем сохранить
     * @param online online = true, offline = false
     * @param time точное время, когда зашёл
     */
    public static Boolean saveStatus(VKApiUserFull user, boolean online, int time) {
        if(user.id==1){
            return false;
        }
        Cursor cursor = getCursor(DatabaseConnector.ONLINE_DATABASE,
                DatabaseConnector.ONLINE_DATABASE_FIELDS,
                "userid = " + user.id,
                "id desc");
        int idColumnIndex = cursor.getColumnIndex("id");
        int useridColumnIndex = cursor.getColumnIndex("userid");
        int sinceColumnIndex = cursor.getColumnIndex("since");
        int tillColumnIndex = cursor.getColumnIndex("till");

        if (cursor.moveToFirst()) {
            int lastOnline = cursor.getInt(sinceColumnIndex);
            int lastOffline = cursor.getInt(tillColumnIndex);

            if (online) {
                if (lastOnline - 300 < time && lastOnline + 300 > time) {
                    close();
                    return false;
                }
            } else {
                if(lastOffline - 300 < time && lastOffline + 300 > time){
                    close();
                    return false;
                }
            }
        }
        Log.i("AGCY SPY SQL", "Saving online.");
        ContentValues values = new ContentValues();
        values.put("userid", user.id);
        values.put(online ? "since" : "till", time);
        save(DatabaseConnector.ONLINE_DATABASE, values);
        close();
        return true;
    }


    public static void saveTyping(VKApiUserFull user) {

        Log.i("AGCY SPY SQL", "Saving typing.");
        ContentValues values = new ContentValues();
        values.put("userid", user.id);
        values.put("time", Helper.getUnixNow() - 3 * 60);

        save(DatabaseConnector.TYPING_DATABASE, values);



    }
    //endregion
    //region Clearer

    public static void clearUsers() {
        users.clear();
        clearDatabase(DatabaseConnector.USER_DATABASE, DatabaseConnector.USER_DATABASE_CREATE);
    }

    public static void clearTypings() {
        clearDatabase(DatabaseConnector.TYPING_DATABASE, DatabaseConnector.TYPING_DATABASE_CREATE);
    }

    public static void clearOnlines() {
        clearDatabase(DatabaseConnector.ONLINE_DATABASE, DatabaseConnector.ONLINE_DATABASE_CREATE);
    }

    private static void clearDatabase(String databaseName, String createDatabase) {
        open();

        Log.i("AGCY SPY SQL", "Clearing database " + databaseName);
        database.delete(databaseName, null, null);

        close();
    }
    //endregion

    public static int dbOperations = 0;

    public static void open() {
        dbOperations++;
        if (database == null) {
            database = databaseConnector.getWritableDatabase();
        } else {
            if (database.isOpen()) {
                if (dbOperations == 0) {
                    Log.wtf("AGCY SPY SQL", "DB was opened Oo who did leave it opened?");
                    close();
                    database = databaseConnector.getWritableDatabase();
                }
            }

        }
        Log.i("AGCY SPY SQL", "New operation. Count of operation: " + dbOperations);
    }

    public static void close() {
        if (dbOperations == 0) {
            if (database != null) {
                database.close();
                database = null;
            }
            databaseConnector.close();
        }
        dbOperations--;
        Log.i("AGCY SPY SQL", "Operation ended. Count of operation: " + dbOperations);
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

    public static void setTyping(VKApiUserFull user) {

        Typing typing = new Typing(user, Helper.getUnixNow());
        for (NewUpdateListener onlineListener : typingListeners) {
            onlineListener.newItem(typing);
        }
        for (NewUpdateListener onlineListener : typingOnceListeners) {
            onlineListener.newItem(typing);
        }
        typingOnceListeners.clear();

        saveTyping(user);
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
                "id desc");
        int tillColumnIndex = cursor.getColumnIndex("till");
        if (cursor.moveToFirst()) {
            int lastOffline = cursor.getInt(tillColumnIndex);
            close();
            return (lastOffline<=0);

        }
        close();
        return false;
    }

    public static void saveNetwork(boolean connected) {

        ContentValues networkValues = new ContentValues();
        networkValues.put("offline", connected);
        networkValues.put("time", Helper.getUnixNow());

        save(DatabaseConnector.NETWORK_DATABASE,networkValues);
    }


    private static class DatabaseConnector extends SQLiteOpenHelper {

        private static final String DATABASE = "vkspy.db";

        private static final String ONLINE_DATABASE = "online_database";
        private static final String TYPING_DATABASE = "typing_database";
        private static final String USER_DATABASE = "user_database";
        private static final String NETWORK_DATABASE = "network_database";
        private static final String[] ONLINE_DATABASE_FIELDS = {
                "id",
                "userid",
                "since",
                "till"
        };
        private static final String[] TYPING_DATABASE_FIELDS = {
                "id",
                "userid",
                "time"
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
                        "till integer default 0" +
                        " ) ";
        private static final String TYPING_DATABASE_CREATE =
                "create table " +
                        TYPING_DATABASE +
                        " ( " +
                        "id integer primary key autoincrement," +
                        "userid integer not null," +
                        "time integer not null" +
                        " ) ";
        public static final String NETWORK_DATABASE_CREATE =
                "create table " +
                        NETWORK_DATABASE +
                        " ( " +
                        "id integer primary key autoincrement," +
                        "connected integer not null," +
                        "time integer not null" +
                        " ) ";

        public DatabaseConnector(Context context) {
            super(context, DATABASE, null, 9);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            database.execSQL(ONLINE_DATABASE_CREATE);
            database.execSQL(TYPING_DATABASE_CREATE);
            database.execSQL(USER_DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL("drop table if exists "+ NETWORK_DATABASE);
            database.execSQL(NETWORK_DATABASE_CREATE);
        }

    }
}