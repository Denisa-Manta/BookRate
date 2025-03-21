package com.example.bookrate.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.bookrate.R;

public class RegisterActivity extends AppCompatActivity {

    // Definirea variabilelor pentru câmpuri și buton
    private EditText emailEditText, passwordEditText, phoneEditText, nameEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inițializarea câmpurilor de editare și a butonului
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        registerButton = findViewById(R.id.registerButton);

        // Setăm un click listener pentru butonul de înregistrare
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preluăm datele din câmpurile de editare
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String phone = phoneEditText.getText().toString();
                String name = nameEditText.getText().toString();

                // Validăm dacă toate câmpurile sunt completate
                if (email.isEmpty() || password.isEmpty() || phone.isEmpty() || name.isEmpty()) {
                    // Dacă nu sunt completate, afișăm un mesaj de eroare
                    Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Dacă toate câmpurile sunt completate, afișăm un mesaj de succes (de exemplu)
                    Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                    // Aici poți adăuga logica pentru a trimite datele la un server,
                    // să le salvezi într-o bază de date locală, etc.
                }
            }
        });
    }
}
