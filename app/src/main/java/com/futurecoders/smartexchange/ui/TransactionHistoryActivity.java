package com.futurecoders.smartexchange.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.futurecoders.smartexchange.R;
import com.futurecoders.smartexchange.data.AppDatabase;
import com.futurecoders.smartexchange.data.AppExecutors;
import com.futurecoders.smartexchange.data.DAO.TransactionDao;
import com.futurecoders.smartexchange.data.entities.TransactionEntity;
import com.futurecoders.smartexchange.data.session.SessionManager;
import com.futurecoders.smartexchange.ui.adapters.TransactionAdapter;

import java.util.List;

public class TransactionHistoryActivity extends AppCompatActivity {

    private TransactionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        RecyclerView rv = findViewById(R.id.rvTransactions);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TransactionAdapter();
        rv.setAdapter(adapter);

        SessionManager sessionManager = new SessionManager(this);
        int userId = sessionManager.getUserId();

        TransactionDao dao = AppDatabase.getDatabase(this).transactionDao();

        AppExecutors.getDatabaseExecutor().execute(() -> {
            List<TransactionEntity> list = dao.getByUser(userId);
            runOnUiThread(() -> adapter.setData(list));
        });
    }
}
