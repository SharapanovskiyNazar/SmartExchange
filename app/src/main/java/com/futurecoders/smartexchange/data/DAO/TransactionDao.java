package com.futurecoders.smartexchange.data.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.futurecoders.smartexchange.data.entities.TransactionEntity;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    void insert(TransactionEntity transaction);

    @Query("SELECT * FROM transactions WHERE userId = :userId")
    List<TransactionEntity> getByUser(int userId);
}
