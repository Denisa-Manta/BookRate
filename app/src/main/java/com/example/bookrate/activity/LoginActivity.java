package com.example.bookrate.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.bookrate.R;

public class LoginActivity extends AppCompatActivity {

    // Definirea câmpurilor și butonului
    private EditText emailEditText, passwordEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inițializarea câmpurilor și a butonului
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        // Setăm un listener pentru butonul de login
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preluăm datele din câmpuri
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // Validăm datele introduse
                if (email.isEmpty() || password.isEmpty()) {
                    // Dacă sunt câmpuri goale, afișăm un mesaj de eroare
                    Toast.makeText(LoginActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
                } else {
                    // Logica de login (verificare date, trimitere către server, etc.)
                    // Exemplu simplu de validare (poți înlocui cu validarea reală)
                    if (email.equals("user@example.com") && password.equals("password123")) {
                        // Dacă loginul este valid, afișăm un mesaj de succes
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        // Aici poți adăuga logica pentru a trece la o altă activitate
                    } else {
                        // Dacă loginul nu este valid, afișăm un mesaj de eroare
                        Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
