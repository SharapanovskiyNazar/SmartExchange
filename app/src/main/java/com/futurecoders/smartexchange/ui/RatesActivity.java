package com.futurecoders.smartexchange.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.futurecoders.smartexchange.R;
import com.futurecoders.smartexchange.data.AppDatabase;
import com.futurecoders.smartexchange.data.AppExecutors;
import com.futurecoders.smartexchange.data.entities.CurrencyRateEntity;
import com.futurecoders.smartexchange.ui.adapters.RateAdapter;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RatesActivity extends AppCompatActivity {

    private RateAdapter adapter;
    private MaterialButton btnDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rates);

        btnDate = findViewById(R.id.btnDate);

        RecyclerView rv = findViewById(R.id.rvRates);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RateAdapter();
        rv.setAdapter(adapter);

        // ✅ текущая дата
        Calendar today = Calendar.getInstance();
        String todayStr = formatDate(today);

        btnDate.setText(todayStr);   // ← вот здесь
        loadRates(todayStr);

        btnDate.setOnClickListener(v -> showDatePicker());
    }


    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();

        new DatePickerDialog(
                this,
                R.style.MaibDatePicker, // 👈 ВАЖНО
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(Calendar.YEAR, year);
                    selected.set(Calendar.MONTH, month);
                    selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    String date = formatDate(selected);
                    btnDate.setText(date);
                    loadRates(date);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }


    private void loadRates(String date) {
        btnDate.setText(date);

        AppExecutors.getDatabaseExecutor().execute(() -> {
            List<CurrencyRateEntity> rates = AppDatabase.getDatabase(this)
                    .currencyRateDao()
                    .getByDate(date);

            runOnUiThread(() -> adapter.setData(rates));
        });
    }

    private String formatDate(Calendar cal) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(cal.getTime());
    }
}
