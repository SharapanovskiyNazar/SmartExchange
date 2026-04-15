package com.futurecoders.smartexchange.data.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.futurecoders.smartexchange.data.entities.AccountEntity;

import java.util.List;

@Dao
public interface AccountDao {

    @Insert
    void insert(AccountEntity account);

    @Update
    void update(AccountEntity account);

    @Query("SELECT * FROM accounts WHERE userId = :userId")
    List<AccountEntity> getByUser(int userId);

    @Query("SELECT * FROM accounts WHERE userId = :userId AND currency = :currency LIMIT 1")
    AccountEntity getAccount(int userId, String currency);

    @Query("SELECT currency FROM accounts WHERE userId = :userId")
    List<String> getCurrenciesByUser(int userId);

    @Query("SELECT * FROM accounts WHERE userId = :userId ORDER BY " +
        " CASE WHEN currency = 'MDL' THEN 0 ELSE 1 END, currency")
    List<AccountEntity> getByUserOrdered(int userId);



}
