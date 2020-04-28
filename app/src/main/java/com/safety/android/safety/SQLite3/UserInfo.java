package com.safety.android.safety.SQLite3;

import java.util.Date;
import java.util.UUID;

public class UserInfo {

    private UUID uuid;
    private Date date;
    private String name;
    private String password;
    private String token;

    public UserInfo(){
        this(UUID.randomUUID());
    }

    public UserInfo(UUID uuid){
        this.uuid=uuid;
        date=new Date();
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
