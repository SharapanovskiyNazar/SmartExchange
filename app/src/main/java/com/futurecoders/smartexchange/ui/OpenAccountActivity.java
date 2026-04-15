package com.futurecoders.smartexchange.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.futurecoders.smartexchange.R;
import com.futurecoders.smartexchange.data.AppDatabase;
import com.futurecoders.smartexchange.data.AppExecutors;
import com.futurecoders.smartexchange.data.DAO.AccountDao;
import com.futurecoders.smartexchange.data.DAO.CurrencyRateDao;
import com.futurecoders.smartexchange.data.entities.AccountEntity;
import com.futurecoders.smartexchange.data.session.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class OpenAccountActivity extends AppCompatActivity {

    private Spinner spCurrency;
    private TextView tvMessage;
    private Button btnOpen;

    private ArrayAdapter<String> adapter;
    private List<String> currencies = new ArrayList<>(); // ✅ НЕ null

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_account);

        spCurrency = findViewById(R.id.spCurrency);
        tvMessage = findViewById(R.id.tvMessage);
        btnOpen = findViewById(R.id.btnOpen);

        // 🔹 Adapter создаём СРАЗУ с пустым списком
        adapter = new ArrayAdapter<>(
                this,
                R.layout.item_spinner_currency,   // ← ТВОЙ layout
                currencies
        );
        adapter.setDropDownViewResource(R.layout.item_spinner_currency);
        spCurrency.setAdapter(adapter);

        spCurrency.setAdapter(adapter);

        loadAvailableCurrencies();

        btnOpen.setOnClickListener(v -> openAccount());
    }

    private void loadAvailableCurrencies() {

        SessionManager sm = new SessionManager(this);
        int userId = sm.getUserId();

        AppDatabase db = AppDatabase.getDatabase(this);
        CurrencyRateDao rateDao = db.currencyRateDao();
        AccountDao accountDao = db.accountDao();

        AppExecutors.getDatabaseExecutor().execute(() -> {

            // 1️⃣ Все валюты из курсов
            List<String> allCurrencies = rateDao.getCurrencies();

            // ❗ защита
            if (allCurrencies == null) {
                allCurrencies = new ArrayList<>();
            }

            // 2️⃣ Уже открытые счета
            List<String> opened = accountDao.getCurrenciesByUser(userId);

            List<String> result = new ArrayList<>();
            for (String c : allCurrencies) {
                if (!"MDL".equals(c) && !opened.contains(c)) {
                    result.add(c);
                }
            }

            runOnUiThread(() -> {
                currencies.clear();
                currencies.addAll(result);
                adapter.notifyDataSetChanged();

                if (currencies.isEmpty()) {
                    tvMessage.setText("All accounts already opened");
                    tvMessage.setVisibility(View.VISIBLE);
                   // btnOpen.setEnabled(false);
                    btnOpen.setVisibility(View.GONE);
                    spCurrency.setVisibility(View.GONE);
                }
            });
        });
    }

    private void openAccount() {

        if (spCurrency.getSelectedItem() == null) {
            Toast.makeText(this, "No currency available", Toast.LENGTH_SHORT).show();
            return;
        }

        String currency = spCurrency.getSelectedItem().toString();

        SessionManager sm = new SessionManager(this);
        int userId = sm.getUserId();

        AppDatabase db = AppDatabase.getDatabase(this);
        AccountDao accountDao = db.accountDao();

        AppExecutors.getDatabaseExecutor().execute(() -> {
            accountDao.insert(new AccountEntity(userId, currency, 0.0));
            runOnUiThread(this::finish);
        });
    }
}
