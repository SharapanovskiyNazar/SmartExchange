package com.futurecoders.smartexchange.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.futurecoders.smartexchange.R;
import com.futurecoders.smartexchange.data.AppDatabase;
import com.futurecoders.smartexchange.data.AppExecutors;
import com.futurecoders.smartexchange.data.DAO.UserDao;
import com.futurecoders.smartexchange.data.entities.UserEntity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.futurecoders.smartexchange.data.session.SessionManager;


public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etPhone, etPassword;
    private TextView tvMessage;
    private Button btnLogin;

    private UserDao userDao;

    private TextView tvRegister;

    private SessionManager sessionManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        tvMessage = findViewById(R.id.tvMessage);
        btnLogin = findViewById(R.id.btnLogin);

        userDao = AppDatabase.getDatabase(this).userDao();

        btnLogin.setOnClickListener(v -> login());

        tvRegister = findViewById(R.id.tvRegister);

        if (tvRegister == null) {
            throw new RuntimeException("tvRegister is NULL – check activity_login.xml");
        }

        tvRegister.setOnClickListener(v -> {
            android.widget.Toast.makeText(
                    LoginActivity.this,
                    "REGISTER CLICK",
                    android.widget.Toast.LENGTH_LONG
            ).show();

            startActivity(new Intent(this, RegisterActivity.class));
        });


        sessionManager = new SessionManager(this);

// если пользователь уже вошёл — сразу MainActivity
      //  if (sessionManager.isLoggedIn()) {
      //      openMainScreen();
      //  }


    }

    private void login() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (phone.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        AppExecutors.getDatabaseExecutor().execute(() -> {
            UserEntity user = userDao.login(phone, password);

            runOnUiThread(() -> {
                if (user == null) {
                    showError("Invalid phone or password");
                } else {
                    sessionManager.saveUser(user.getUserId());
                    openMainScreen();
                }
            });
        });

    }

    private void showError(String message) {
        tvMessage.setText(message);
        tvMessage.setVisibility(View.VISIBLE);
    }

    private void openMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_about) {
            startActivity(
                    new Intent(this, DeveloperActivity.class)
            );
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
