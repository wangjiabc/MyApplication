package com.safety.android.SQLite3;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class PermissionBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION=1;
    private static final String DATABASE_NAME="permission.db";

    public PermissionBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+PermissionDbSchema.PermissionTable.NAME+"("+
                "_id integer primary key autoincrement, "+
                PermissionDbSchema.PermissionTable.Cols.action+","+
                PermissionDbSchema.PermissionTable.Cols.component+","+
                PermissionDbSchema.PermissionTable.Cols.describe+","+
                PermissionDbSchema.PermissionTable.Cols.id+","+
                PermissionDbSchema.PermissionTable.Cols.name+","+
                PermissionDbSchema.PermissionTable.Cols.path+","+
                PermissionDbSchema.PermissionTable.Cols.resulttype+","+
                PermissionDbSchema.PermissionTable.Cols.status+","+
                PermissionDbSchema.PermissionTable.Cols.type+","+
                PermissionDbSchema.PermissionTable.Cols.UUID+
                ")"
        );

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
