package com.safety.android.safety.SQLite3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class UserLab {

    private static UserLab userLab;

    private Context context;
    private SQLiteDatabase database;

    public static UserLab get(Context context){
        if(userLab==null){
            userLab=new UserLab(context);
        }
        return userLab;
    }

    private UserLab(Context context){
        this.context=context;
        database=new UserBaseHelper(context).getWritableDatabase();
    }

    public List<UserInfo> getUserInfo(){
        List<UserInfo> userInfos=new ArrayList<>();
        UserInfoCusorWrapper cusorWrapper=queryUsers(null,null);

        try {
            cusorWrapper.moveToFirst();
            while (!cusorWrapper.isAfterLast()){
                userInfos.add(cusorWrapper.getUserInfo());
                cusorWrapper.moveToNext();
            }
        }finally {
            cusorWrapper.close();
        }

        return  userInfos;

    }

    public void addUserInfo(UserInfo userInfo){
        ContentValues values=getContentValues(userInfo);

        database.insert(UserDbSchema.UserTable.NAME,null,values);
    }

    public int updateUserInfo(UserInfo userInfo){
        String name=userInfo.getName();
        ContentValues values=getContentValues(userInfo);

        int i=database.update(UserDbSchema.UserTable.NAME,values,
                UserDbSchema.UserTable.Cols.name+"=?",
                new String[]{name});

        return i;
    }

    private static ContentValues getContentValues(UserInfo userInfo){
        ContentValues values=new ContentValues();
        values.put(UserDbSchema.UserTable.Cols.UUID,userInfo.getUuid().toString());
        values.put(UserDbSchema.UserTable.Cols.name,userInfo.getName());
        values.put(UserDbSchema.UserTable.Cols.password,userInfo.getPassword());
        values.put(UserDbSchema.UserTable.Cols.date,userInfo.getDate().getTime());
        values.put(UserDbSchema.UserTable.Cols.token,userInfo.getToken());

        return values;
    }

    private UserInfoCusorWrapper queryUsers(String whereClause,String[] whereArgs){
        Cursor cursor=database.query(UserDbSchema.UserTable.NAME,
                null,whereClause,
                whereArgs,null,null,
                "date");
        return new UserInfoCusorWrapper(cursor);
    }

}
