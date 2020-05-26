package com.safety.android.SQLite3;

import java.util.UUID;

public class PermissionInfo {

    private UUID uuid;
    private String resulttype;
    private String action;
    private String describe;
    private Integer type;
    private Integer status;
    private String path;
    private String component;
    private String name;
    private String id;

    public PermissionInfo() {
        this(UUID.randomUUID());
    }

    public PermissionInfo(UUID uuid){
        this.uuid=uuid;
    }

    public void setAction(String action) {
        this.action = action;
    }



    public String getAction() {
        return action;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }



    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }



    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }


    public String getResulttype() {
        return resulttype;
    }

    public void setResulttype(String resulttype) {
        this.resulttype = resulttype;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
