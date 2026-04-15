package com.futurecoders.smartexchange.data.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.futurecoders.smartexchange.data.entities.CurrencyRateEntity;

import java.util.List;

@Dao
public interface CurrencyRateDao {

    @Insert
    void insert(CurrencyRateEntity rate);

    @Query("SELECT * FROM currency_rates WHERE rateDate = :date ORDER BY currency ASC")
    List<CurrencyRateEntity> getByDate(String date);

    @Query("DELETE FROM currency_rates")
    void clear();

    @Query("SELECT * FROM currency_rates WHERE currency = :currency  ORDER BY rateDate DESC LIMIT 1")
           CurrencyRateEntity getToday(String currency);

    @Query(" SELECT * FROM currency_rates WHERE currency = :currency AND rateDate = :date LIMIT 1")
    CurrencyRateEntity getRate(String currency, String date);

    @Query("SELECT DISTINCT currency FROM currency_rates ORDER BY currency")
    List<String> getCurrencies();

    @Query("SELECT * FROM currency_rates WHERE currency = :currency AND rateDate = :date LIMIT 1")
    CurrencyRateEntity getByCurrencyAndDate(String currency, String date);
    @Query("SELECT COUNT(*) FROM currency_rates")
    int count();

    // Added: get list of rates for a currency between two dates (inclusive), ordered by date asc
    @Query("SELECT * FROM currency_rates WHERE currency = :currency AND rateDate BETWEEN :fromDate AND :toDate ORDER BY rateDate ASC")
    List<CurrencyRateEntity> getRatesBetween(String currency, String fromDate, String toDate);

}
