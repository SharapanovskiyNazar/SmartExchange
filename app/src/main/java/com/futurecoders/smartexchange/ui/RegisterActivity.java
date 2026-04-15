package com.futurecoders.smartexchange.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.futurecoders.smartexchange.R;
import com.futurecoders.smartexchange.data.AppDatabase;
import com.futurecoders.smartexchange.data.AppExecutors;
import com.futurecoders.smartexchange.data.DAO.AccountDao;
import com.futurecoders.smartexchange.data.DAO.UserDao;
import com.futurecoders.smartexchange.data.entities.AccountEntity;
import com.futurecoders.smartexchange.data.entities.UserEntity;
import com.futurecoders.smartexchange.data.session.SessionManager;

public class RegisterActivity extends AppCompatActivity {

    private EditText etIdnp, etFirstName, etLastName, etPhone, etPassword;
    private TextView tvMessage;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // views
        etIdnp = findViewById(R.id.etIdnp);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        tvMessage = findViewById(R.id.tvMessage);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> register());
    }

   private void register() {

       String idnp = etIdnp.getText().toString().trim();
       String firstName = etFirstName.getText().toString().trim();
       String lastName = etLastName.getText().toString().trim();
       String phone = etPhone.getText().toString().trim();
       String password = etPassword.getText().toString().trim();

       if (idnp.isEmpty() || firstName.isEmpty() || lastName.isEmpty()
               || phone.isEmpty() || password.isEmpty()) {
           showError("All fields are required");
           return;
       }

       btnRegister.setEnabled(false);

       AppDatabase db = AppDatabase.getDatabase(this);
       UserDao userDao = db.userDao();
       AccountDao accountDao = db.accountDao();

       AppExecutors.getDatabaseExecutor().execute(() -> {

           // 1️⃣ проверка телефона
           UserEntity existing = userDao.findByPhone(phone);
           if (existing != null) {
               runOnUiThread(() -> {
                   btnRegister.setEnabled(true);
                   showError("User with this phone already exists");
               });
               return;
           }

           // 2️⃣ создаём пользователя
           UserEntity user = new UserEntity(
                   idnp, firstName, lastName, phone, password
           );

           long userId = userDao.insert(user); // 🔥 ВАЖНО

           // 3️⃣ создаём дефолтный счёт MDL = 100
           accountDao.insert(
                   new AccountEntity((int) userId, "MDL", 100.0)
           );

           runOnUiThread(() -> {
               btnRegister.setEnabled(true);
               startActivity(new Intent(this, LoginActivity.class));
               finish();
           });
       });
   }

    private void showError(String msg) {
        tvMessage.setText(msg);
        tvMessage.setVisibility(View.VISIBLE);
    }
}
