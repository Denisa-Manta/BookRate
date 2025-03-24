package com.example.bookrate.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.bookrate.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inițializare Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Inițializare UI
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        // Click pe butonul de login
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            if (user.isEmailVerified()) {
                                // Login reușit și email verificat
                                checkUserRole(user.getUid());
                            } else {
                                // Email-ul nu este verificat
                                Toast.makeText(LoginActivity.this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                                mAuth.signOut(); // Deconectează utilizatorul dacă email-ul nu e verificat
                            }
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed. Please check your credentials.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkUserRole(String uid) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference userRef = database.getReference("users").child(uid);

        userRef.child("role").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String role = String.valueOf(task.getResult().getValue());
                if ("admin".equals(role)) {
                    // Go to Admin Activity
                    Toast.makeText(LoginActivity.this, "Welcome, Admin!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                } else {
                    // Go to Reader Activity
                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, ReaderActivity.class));
                }
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Failed to fetch role", Toast.LENGTH_SHORT).show();
            }
        });
    }

}


