package com.safety.android.safety.SQLite3;

/**
 * Created by WangJing on 2017/6/10.
 */

public class SafeDbSchema {
    public static final class CrimeTable{
        public static final String NAME="safety";

        public static final class Cols{
            public static final String UUID="uuid";
            public static final String TITLE="title";
            public static final String DATE="date";
            public static final String SOLVED="solved";
            public static final String SUSPECT="suspect";
        }
    }
}
