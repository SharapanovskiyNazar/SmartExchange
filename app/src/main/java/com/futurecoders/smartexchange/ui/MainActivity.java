package com.futurecoders.smartexchange.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.futurecoders.smartexchange.R;
import com.futurecoders.smartexchange.data.AppDatabase;
import com.futurecoders.smartexchange.data.AppExecutors;
import com.futurecoders.smartexchange.data.DAO.AccountDao;
import com.futurecoders.smartexchange.data.DAO.CurrencyRateDao;
import com.futurecoders.smartexchange.data.DAO.UserDao;
import com.futurecoders.smartexchange.data.entities.AccountEntity;
import com.futurecoders.smartexchange.data.entities.CurrencyRateEntity;
import com.futurecoders.smartexchange.data.entities.UserEntity;
import com.futurecoders.smartexchange.data.session.SessionManager;
import com.futurecoders.smartexchange.ui.adapters.AccountAdapter;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private RecyclerView rvAccounts;

    private AccountAdapter accountAdapter;
    private AccountDao accountDao;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // ===== UI =====
        tvWelcome = findViewById(R.id.tvWelcome);
        rvAccounts = findViewById(R.id.rvAccounts);

        rvAccounts.setLayoutManager(new LinearLayoutManager(this));
        accountAdapter = new AccountAdapter();
        rvAccounts.setAdapter(accountAdapter);

        // ===== DB =====
        AppDatabase db = AppDatabase.getDatabase(this);
        accountDao = db.accountDao();
        UserDao userDao = db.userDao();

        int userId = sessionManager.getUserId();

        // Load user name
        AppExecutors.getDatabaseExecutor().execute(() -> {
            UserEntity user = userDao.getById(userId);
            runOnUiThread(() -> {
                if (user != null) {
                    tvWelcome.setText("Hello, " + user.getFirstName());
                }
            });
        });

        // ===== Bottom buttons =====
        TextView btnOpenAccount = findViewById(R.id.btnOpenAccount);
        TextView btnRates = findViewById(R.id.btnRates);
        TextView btnCharts = findViewById(R.id.btnCharts);

        btnOpenAccount.setOnClickListener(v ->
                startActivity(new Intent(this, OpenAccountActivity.class)));

        btnRates.setOnClickListener(v ->
                startActivity(new Intent(this, RatesActivity.class)));

        btnCharts.setOnClickListener(v ->
                startActivity(new Intent(this, ChartActivity.class)));

    }

    @Override
    protected void onResume() {
        super.onResume();

        int userId = sessionManager.getUserId();

        AppExecutors.getDatabaseExecutor().execute(() -> {
            List<AccountEntity> accounts = accountDao.getByUserOrdered(userId);

            runOnUiThread(() -> accountAdapter.setData(accounts));
        });
    }
}
