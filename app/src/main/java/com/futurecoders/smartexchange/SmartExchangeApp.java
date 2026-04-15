package com.futurecoders.smartexchange;

import android.app.Application;

import com.futurecoders.smartexchange.data.AppDatabase;
import com.futurecoders.smartexchange.data.DAO.CurrencyRateDao;
import com.futurecoders.smartexchange.data.entities.CurrencyRateEntity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SmartExchangeApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        seedExchangeRates();
    }

    private void seedExchangeRates() {
        AppDatabase db = AppDatabase.getDatabase(this);
        CurrencyRateDao dao = db.currencyRateDao();

        new Thread(() -> {

            if (dao.count() > 0) return; // ❗ уже есть — не трогаем

            Calendar today = Calendar.getInstance();

            for (int i = 7; i >= 1; i--) {
                Calendar c = (Calendar) today.clone();
                c.add(Calendar.DAY_OF_MONTH, -i);
                insertRatesForDate(dao, formatDate(c));
            }

            for (int i = 1; i <= 7; i++) {
                Calendar c = (Calendar) today.clone();
                c.add(Calendar.DAY_OF_MONTH, i);
                insertRatesForDate(dao, formatDate(c));
            }

        }).start();
    }

    private void insertRatesForDate(CurrencyRateDao dao, String date) {
        dao.insert(new CurrencyRateEntity("MDL", 1.0, 1.0, date));
        dao.insert(new CurrencyRateEntity("EUR", 19.2, 19.5, date));
        dao.insert(new CurrencyRateEntity("USD", 17.6, 17.9, date));
        dao.insert(new CurrencyRateEntity("RON", 3.8, 3.95, date));
        dao.insert(new CurrencyRateEntity("TRL", 0.55, 0.65, date));
    }

    private String formatDate(Calendar cal) {
        return new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(cal.getTime());
    }
}
