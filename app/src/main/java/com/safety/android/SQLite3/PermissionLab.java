package com.safety.android.SQLite3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class PermissionLab {
    private static PermissionLab mpermissionLab;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    private PermissionLab(Context context){
        mContext=context.getApplicationContext();
        mDatabase=new PermissionBaseHelper(context).getWritableDatabase();
    }

    public static PermissionLab get(Context context){

        if(mpermissionLab==null){
            mpermissionLab=new PermissionLab(context);
        }
        return mpermissionLab;
    }

    private static ContentValues getContentValues(PermissionInfo permissionInfo){
        ContentValues values=new ContentValues();
        values.put(PermissionDbSchema.PermissionTable.Cols.UUID,permissionInfo.getUuid().toString());
        values.put(PermissionDbSchema.PermissionTable.Cols.resulttype,permissionInfo.getResulttype());
        values.put(PermissionDbSchema.PermissionTable.Cols.action,permissionInfo.getAction());
        values.put(PermissionDbSchema.PermissionTable.Cols.describe,permissionInfo.getDescribe());
        values.put(PermissionDbSchema.PermissionTable.Cols.type,permissionInfo.getType());
        values.put(PermissionDbSchema.PermissionTable.Cols.status,permissionInfo.getStatus());
        values.put(PermissionDbSchema.PermissionTable.Cols.path,permissionInfo.getPath());
        values.put(PermissionDbSchema.PermissionTable.Cols.component,permissionInfo.getComponent());
        values.put(PermissionDbSchema.PermissionTable.Cols.name,permissionInfo.getName());
        values.put(PermissionDbSchema.PermissionTable.Cols.id,permissionInfo.getId());

        return  values;
    }

    public void addPermission(PermissionInfo permissionInfo){
        ContentValues values=getContentValues(permissionInfo);

        mDatabase.insert(PermissionDbSchema.PermissionTable.NAME,null,values);
    }

    public void delPermission(){
        mDatabase.delete(PermissionDbSchema.PermissionTable.NAME,null,null);
    }

    public List<PermissionInfo> getPermissionInfo(){
        List<PermissionInfo> permissionInfos=new ArrayList<>();
        PermissionCusorWrapper cusorWrapper=queryPermissions(null,null);

        try {
            cusorWrapper.moveToFirst();
            while (!cusorWrapper.isAfterLast()){
                permissionInfos.add(cusorWrapper.getPermissionInfo());
                cusorWrapper.moveToNext();
            }
        }finally {
            cusorWrapper.close();
        }

        return  permissionInfos;
    }

    private PermissionCusorWrapper queryPermissions(String whereClause,String[] whereArgs){
        Cursor cursor=mDatabase.query(PermissionDbSchema.PermissionTable.NAME,null,whereClause,
                whereArgs,null,null,
                null);

        return new PermissionCusorWrapper(cursor);
    }

}
