package com.futurecoders.smartexchange.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.futurecoders.smartexchange.R;
import com.futurecoders.smartexchange.data.AppDatabase;
import com.futurecoders.smartexchange.data.AppExecutors;
import com.futurecoders.smartexchange.data.DAO.AccountDao;
import com.futurecoders.smartexchange.data.DAO.CurrencyRateDao;
import com.futurecoders.smartexchange.data.entities.AccountEntity;
import com.futurecoders.smartexchange.data.entities.CurrencyRateEntity;
import com.futurecoders.smartexchange.data.entities.TransactionEntity;
import com.futurecoders.smartexchange.data.session.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BuySellActivity extends AppCompatActivity {

    private EditText etAmount;
    private RadioButton rbBuy, rbSell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_sell);

        etAmount = findViewById(R.id.etAmount);
        rbBuy = findViewById(R.id.rbBuy);
        rbSell = findViewById(R.id.rbSell);
        Button btnOk = findViewById(R.id.btnOk);

        String currency = getIntent().getStringExtra("currency");

        btnOk.setOnClickListener(v -> executeTransaction(currency));
    }

    private void executeTransaction(String currency) {

        String amountStr = etAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            showError("Enter amount");
            return;
        }

        double amount = Double.parseDouble(amountStr);
        boolean isBuy = rbBuy.isChecked();

        SessionManager sm = new SessionManager(this);
        int userId = sm.getUserId();

        AppDatabase db = AppDatabase.getDatabase(this);
        AccountDao accountDao = db.accountDao();
        CurrencyRateDao rateDao = db.currencyRateDao();

        AppExecutors.getDatabaseExecutor().execute(() -> {

            AccountEntity mdlAccount = accountDao.getAccount(userId, "MDL");
            AccountEntity currencyAccount = accountDao.getAccount(userId, currency);

            if (mdlAccount == null || currencyAccount == null) {
                showError("Accounts not found");
                return;
            }

            CurrencyRateEntity rate = rateDao.getToday(currency);

            if (rate == null) {
                showError("Rate not available");
                return;
            }

            double totalMDL;
            double usedRate;

            if (isBuy) {
                usedRate = rate.getBuyRate();
                totalMDL = amount * usedRate;

                if (mdlAccount.getBalance() < totalMDL) {
                    showError("Not enough MDL");
                    return;
                }

                mdlAccount.setBalance(mdlAccount.getBalance() - totalMDL);
                currencyAccount.setBalance(currencyAccount.getBalance() + amount);

                totalMDL = -totalMDL; // MDL ушли
            } else {
                usedRate = rate.getSellRate();
                totalMDL = amount * usedRate;

                if (currencyAccount.getBalance() < amount) {
                    showError("Not enough currency");
                    return;
                }

                currencyAccount.setBalance(currencyAccount.getBalance() - amount);
                mdlAccount.setBalance(mdlAccount.getBalance() + totalMDL);
            }

            String now = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm",
                    Locale.getDefault()
            ).format(new Date());

            TransactionEntity tx = new TransactionEntity(
                    userId,
                    currency,
                    amount,
                    usedRate,
                    isBuy ? "BUY" : "SELL",
                    totalMDL,
                    now
            );

            db.transactionDao().insert(tx);
            accountDao.update(mdlAccount);
            accountDao.update(currencyAccount);

            runOnUiThread(this::finish);
        });
    }


    private void showError(String msg) {
        runOnUiThread(() ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        );
    }
}
