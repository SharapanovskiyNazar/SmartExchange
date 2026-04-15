package com.futurecoders.smartexchange.data;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutors {

    private static final ExecutorService databaseExecutor =
            Executors.newSingleThreadExecutor();

    public static ExecutorService getDatabaseExecutor() {
        return databaseExecutor;
    }
}
