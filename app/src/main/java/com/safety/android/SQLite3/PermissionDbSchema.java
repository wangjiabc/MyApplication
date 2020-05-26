package com.safety.android.SQLite3;

public class PermissionDbSchema {
    public static final class PermissionTable{
        public static final String NAME="permission";

        public static final class Cols{
            public static final String UUID="uuid";
            public static final String resulttype="resulttype";
            public static final String action="action";
            public static final String describe="describe";
            public static final String type="type";
            public static final String status="status";
            public static final String path="path";
            public static final String component="component";
            public static final String name="name";
            public static final String id="id";
        }
    }
}
