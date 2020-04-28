package com.safety.android.safety.SQLite3;

public class UserDbSchema {
    public static final class UserTable{
        public static final String NAME="user";

        public static final class Cols{
            public static final String UUID="uuid";
            public static final String name="name";
            public static final String password="password";
            public static final String date="date";
            public static final String token="token";
        }
    }
}
