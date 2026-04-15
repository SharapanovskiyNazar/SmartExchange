package com.futurecoders.smartexchange;

import android.app.Application;

import com.futurecoders.smartexchange.data.AppDatabase;
import com.futurecoders.smartexchange.data.DAO.CurrencyRateDao;
import com.futurecoders.smartexchange.data.entities.CurrencyRateEntity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// NBM rates
import java.text.ParseException;
import java.util.Date;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;
import java.util.concurrent.ThreadLocalRandom;

public class SmartExchangeApp extends Application {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        seedExchangeRates();
    }

    private void seedExchangeRates() {
        AppDatabase db = AppDatabase.getDatabase(this);
        CurrencyRateDao dao = db.currencyRateDao();

        executor.execute(() -> {
            String lastLoadedDate = dao.getLastLoadedDate();

            Calendar start = Calendar.getInstance();
            if (lastLoadedDate == null) {
                start.set(2026, Calendar.JANUARY, 1, 0, 0, 0);
            } else {
                Date parsed = convertStringToDate(lastLoadedDate);
                start.setTime(parsed);
                start.add(Calendar.DAY_OF_MONTH, 1);
            }
            start.set(Calendar.MILLISECOND, 0);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            while (!start.after(today)) {
                String date = formatDate(start);

                if (dao.countByDate(date) == 0) {
                    insertRatesForDateSync(dao, date);
                }

                start.add(Calendar.DAY_OF_MONTH, 1);
            }
        });
    }

    private void insertRatesForDateSync(CurrencyRateDao dao, String date) {
        try {
            BnmExchangeParser parser = new BnmExchangeParser();

            Date d = convertStringToDate(date);
            if (d == null) return;

            String dateStr = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(d);
            Map<String, Double> allRates = parser.getAllExchangeRates(dateStr);

            List<CurrencyRateEntity> entities = new ArrayList<>();

            for (Map.Entry<String, Double> entry : allRates.entrySet()) {
                String currencyCode = entry.getKey().toUpperCase();

                if (currencyCode.equals("EUR") ||
                        currencyCode.equals("CHF") ||
                        currencyCode.equals("USD") ||
                        currencyCode.equals("RON") ||
                        currencyCode.equals("RUB") ||
                        currencyCode.equals("UAH") ||
                        currencyCode.equals("TRY") ||
                        currencyCode.equals("GBP")) {

                    if (currencyCode.equals("TRY")) {
                        currencyCode = "TRL";
                    }

                    double bias = getRateBias();

                    entities.add(new CurrencyRateEntity(
                            currencyCode,
                            entry.getValue() - bias,
                            entry.getValue() + bias,
                            date
                    ));
                }
            }

            for (CurrencyRateEntity entity : entities) {
                dao.insert(entity);
            }

            Log.d("insertRatesForDateSync", "Inserted rates for date: " + date);

        } catch (IOException | XmlPullParserException e) {
            Log.e("insertRatesForDateSync", "Error fetching rates for date: " + date, e);
        }
    }

    private void insertRatesForDate(CurrencyRateDao dao, String date) {
        /* Test data
        dao.insert(new CurrencyRateEntity("MDL", 1.0, 1.0, date));
        dao.insert(new CurrencyRateEntity("EUR", 19.2, 19.5, date));
        dao.insert(new CurrencyRateEntity("USD", 17.6, 17.9, date));
        dao.insert(new CurrencyRateEntity("RON", 3.8, 3.95, date));
        dao.insert(new CurrencyRateEntity("TRL", 0.55, 0.65, date));
         */

        // NBM rates
        // Асинхронный вызов для получения всех курсов (и кодов) за один запрос
        executor.execute(() -> {
            try {
                BnmExchangeParser parser = new BnmExchangeParser();

                Date d = convertStringToDate(date);
                if (d != null) {
                    //"08.01.2026" Пример даты в формате dd.mm.yyyy
                    String dateStr = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(d);
                    Map<String, Double> allRates = parser.getAllExchangeRates(dateStr);

                    // Извлекаем список кодов
                    List<String> codes = new ArrayList<>(allRates.keySet());

                    // Сохраняем курсы в базу данных
                    for (Map.Entry<String, Double> entry : allRates.entrySet()) {
                        String currencyCode = entry.getKey().toUpperCase();
                        if ((currencyCode.equals("EUR")) || (currencyCode.equals("CHF")) || (currencyCode.equals("USD")) ||
                            (currencyCode.equals("RON")) || (currencyCode.equals("RUB")) || (currencyCode.equals("UAH")) ||
                            (currencyCode.equals("TRY")) || (currencyCode.equals("GBP"))) {
                            if (currencyCode.equals("TRY"))
                                currencyCode = "TRL";
                            dao.insert(new CurrencyRateEntity(currencyCode, entry.getValue() - getRateBias(), entry.getValue() + getRateBias(), date));
                        }
                    }
                }
            } catch (IOException | XmlPullParserException e) {
                handler.post(() -> {
                    Log.e("insertRatesForDate", "Error fetching data", e);
                });
                e.printStackTrace();
            }
        });
    }

    private String formatDate(Calendar cal) {
        return new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(cal.getTime());
    }
    private Date convertStringToDate(String dateString) {
        try {
            // 1. Создаем форматтер с тем же шаблоном, в котором записана строка
            // Важно: "MM" — месяц, "mm" — минуты
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            // 2. Преобразуем строку в объект Date
            return formatter.parse(dateString);
        } catch (ParseException e) {
            // В случае ошибки парсинга (неверный формат) выводим лог и возвращаем null
            handler.post(() -> {
                Log.e("convertStringToDate", "Error parsing date string", e);
            });
            e.printStackTrace();
            return null;
        }
    }

    public Double getRateBias() {
        // Включает 0.01 и идет до 0.10 (не включая верхнюю границу)
        // Чтобы включить 0.10 ровно, можно поставить 0.101
        return ThreadLocalRandom.current().nextDouble(0.01, 0.11);
    }
}
