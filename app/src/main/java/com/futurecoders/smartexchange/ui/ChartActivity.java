package com.futurecoders.smartexchange.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.futurecoders.smartexchange.R;
import com.futurecoders.smartexchange.data.AppDatabase;
import com.futurecoders.smartexchange.data.AppExecutors;
import com.futurecoders.smartexchange.data.DAO.CurrencyRateDao;
import com.futurecoders.smartexchange.data.entities.CurrencyRateEntity;
import com.futurecoders.smartexchange.ui.views.SimpleLineChart;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChartActivity extends AppCompatActivity {

    private Spinner spinnerCurrency;

    private Button btnFromDate;
    private Button btnToDate;
    private SimpleLineChart lineChart;

    private Calendar fromCal = Calendar.getInstance();
    private Calendar toCal = Calendar.getInstance();

    private CurrencyRateDao rateDao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        spinnerCurrency = findViewById(R.id.spinnerCurrency);
        btnFromDate = findViewById(R.id.btnFromDate);
        btnToDate = findViewById(R.id.btnToDate);
        Button btnShowChart = findViewById(R.id.btnShowChart);
        lineChart = findViewById(R.id.lineChart);

        AppDatabase db = AppDatabase.getDatabase(this);
        rateDao = db.currencyRateDao();

        setupDefaults();
        setupRateTypeSpinner();
        loadCurrencies();

        btnFromDate.setOnClickListener(v -> showDatePicker(fromCal, date -> btnFromDate.setText(date)));
        btnToDate.setOnClickListener(v -> showDatePicker(toCal, date -> btnToDate.setText(date)));

        btnShowChart.setOnClickListener(v -> showChart());
    }

    private void setupRateTypeSpinner() {

            ArrayList<String> types = new ArrayList<>();
             types.add("Sell");
            types.add("Buy");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_spinner_currency,
                types
        );

        adapter.setDropDownViewResource(
                R.layout.item_spinner_currency
        );

        spinnerCurrency.setAdapter(adapter);
    }

    private void setupDefaults() {
        // default: last 7 days
        toCal = Calendar.getInstance();
        fromCal = Calendar.getInstance();
        fromCal.add(Calendar.DAY_OF_MONTH, -7);

        btnFromDate.setText(formatDate(fromCal));
        btnToDate.setText(formatDate(toCal));
    }

    private interface DateCallback { void onDate(String date); }

    private void showDatePicker(Calendar cal, DateCallback cb) {
        new DatePickerDialog(this, R.style.MaibDatePicker, (DatePicker view, int year, int month, int dayOfMonth) -> {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            cb.onDate(formatDate(cal));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String formatDate(Calendar cal) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
    }

    private void loadCurrencies() {
        AppExecutors.getDatabaseExecutor().execute(() -> {

            List<String> currencies = rateDao.getCurrencies();

            runOnUiThread(() -> {
                if (currencies == null || currencies.isEmpty()) {
                    Toast.makeText(this,
                            R.string.no_currencies,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        R.layout.item_spinner_currency,
                        currencies
                );

                adapter.setDropDownViewResource(
                        R.layout.item_spinner_currency
                );

                spinnerCurrency.setAdapter(adapter);
            });
        });
    }


    private void showChart() {
        String currency = (String) spinnerCurrency.getSelectedItem();
        if (currency == null) {
            Toast.makeText(this, R.string.choose_currency, Toast.LENGTH_SHORT).show();
            return;
        }

        String fromDate = formatDate(fromCal);
        String toDate = formatDate(toCal);

        // validate dates: from <= to
        if (!isFromBeforeOrEqualToTo(fromDate, toDate)) {
            Toast.makeText(this, R.string.invalid_date_order, Toast.LENGTH_SHORT).show();
            return;
        }

        AppExecutors.getDatabaseExecutor().execute(() -> {
            List<CurrencyRateEntity> rates = rateDao.getRatesBetween(currency, fromDate, toDate);
            runOnUiThread(() -> drawChartBoth(rates));
        });
    }

    private boolean isFromBeforeOrEqualToTo(String from, String to) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date d1 = fmt.parse(from);
            Date d2 = fmt.parse(to);
            return d1 != null && d2 != null && !d1.after(d2);
        } catch (ParseException e) {
            return false;
        }
    }

    private void drawChartBoth(List<CurrencyRateEntity> rates) {
        if (rates == null || rates.isEmpty()) {
            Toast.makeText(this, R.string.no_rates_found, Toast.LENGTH_SHORT).show();
            lineChart.clear();
            return;
        }

        List<Float> sellValues = new ArrayList<>();
        List<Float> buyValues = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();

        for (CurrencyRateEntity r : rates) {
            sellValues.add((float) r.getSellRate());
            buyValues.add((float) r.getBuyRate());
            xLabels.add(r.getRateDate());
        }

        lineChart.setData(sellValues, buyValues, xLabels);
    }
}
