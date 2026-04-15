package com.futurecoders.smartexchange.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.futurecoders.smartexchange.data.DAO.AccountDao;
import com.futurecoders.smartexchange.data.DAO.UserDao;
import com.futurecoders.smartexchange.data.DAO.CurrencyRateDao;
import com.futurecoders.smartexchange.data.DAO.TransactionDao;
import com.futurecoders.smartexchange.data.entities.UserEntity;
import com.futurecoders.smartexchange.data.entities.CurrencyRateEntity;
import com.futurecoders.smartexchange.data.entities.TransactionEntity;
import com.futurecoders.smartexchange.data.entities.AccountEntity;
@Database(entities = {
        UserEntity.class,
        CurrencyRateEntity.class,
        TransactionEntity.class,
        AccountEntity.class
}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract CurrencyRateDao currencyRateDao();
    public abstract TransactionDao transactionDao();
    public abstract AccountDao accountDao();


    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                INSTANCE = Room.databaseBuilder(context, AppDatabase.class, "smart_exchange_db")
                        .fallbackToDestructiveMigration()
                        .build();

            }
        }
        return INSTANCE;
    }
}
