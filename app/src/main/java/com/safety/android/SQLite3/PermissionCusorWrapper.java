package com.safety.android.SQLite3;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.UUID;

public class PermissionCusorWrapper extends CursorWrapper {
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public PermissionCusorWrapper(Cursor cursor) {
        super(cursor);
    }

    public PermissionInfo getPermissionInfo(){
        String uuid=getString(getColumnIndex(PermissionDbSchema.PermissionTable.Cols.UUID));
        String resulttype=getString(getColumnIndex(PermissionDbSchema.PermissionTable.Cols.resulttype));
        String action=getString(getColumnIndex(PermissionDbSchema.PermissionTable.Cols.action));
        String describe=getString(getColumnIndex(PermissionDbSchema.PermissionTable.Cols.describe));
        Integer type=getInt(getColumnIndex(PermissionDbSchema.PermissionTable.Cols.type));
        Integer status=getInt(getColumnIndex(PermissionDbSchema.PermissionTable.Cols.status));
        String path=getString(getColumnIndex(PermissionDbSchema.PermissionTable.Cols.path));
        String component=getString(getColumnIndex(PermissionDbSchema.PermissionTable.Cols.component));
        String name=getString(getColumnIndex(PermissionDbSchema.PermissionTable.Cols.name));
        String id=getString(getColumnIndex(PermissionDbSchema.PermissionTable.Cols.id));

        PermissionInfo permissionInfo=new PermissionInfo(UUID.fromString(uuid));
        permissionInfo.setResulttype(resulttype);
        permissionInfo.setAction(action);
        permissionInfo.setDescribe(describe);
        permissionInfo.setType(type);
        permissionInfo.setStatus(status);
        permissionInfo.setPath(path);
        permissionInfo.setComponent(component);
        permissionInfo.setName(name);
        permissionInfo.setId(id);

        return permissionInfo;

    }

}
