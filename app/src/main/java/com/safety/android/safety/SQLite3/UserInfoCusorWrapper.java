package com.safety.android.safety.SQLite3;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.Date;
import java.util.UUID;

public class UserInfoCusorWrapper extends CursorWrapper {
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public UserInfoCusorWrapper(Cursor cursor) {
        super(cursor);
    }

    public UserInfo getUserInfo(){
        String uuid=getString(getColumnIndex(UserDbSchema.UserTable.Cols.UUID));
        String name=getString(getColumnIndex(UserDbSchema.UserTable.Cols.name));
        String password=getString(getColumnIndex(UserDbSchema.UserTable.Cols.password));
        long date=getInt(getColumnIndex(UserDbSchema.UserTable.Cols.date));
        String token=getString(getColumnIndex(UserDbSchema.UserTable.Cols.token));

        UserInfo userInfo=new UserInfo(UUID.fromString(uuid));
        userInfo.setName(name);
        userInfo.setPassword(password);
        userInfo.setDate(new Date(date));
        userInfo.setToken(token);

        return userInfo;
    }
}
