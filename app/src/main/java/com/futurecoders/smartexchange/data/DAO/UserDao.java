package com.futurecoders.smartexchange.data.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.futurecoders.smartexchange.data.entities.UserEntity;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(UserEntity user);

    @Query("SELECT * FROM users")
    List<UserEntity> getAll();

    @Query("SELECT * FROM users WHERE phoneNumber = :phone AND password = :password LIMIT 1")
    UserEntity login(String phone, String password);

    @Query("SELECT * FROM users WHERE phoneNumber = :phone LIMIT 1")
    UserEntity findByPhone(String phone);

    @Query("SELECT * FROM users WHERE userId = :id LIMIT 1")
    UserEntity getById(int id);

}
