package com.example.bookrate.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.bookrate.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // 🔙 closes current activity and goes back

        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

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
                            checkUserRole(user.getUid(), user);
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed. Please check your credentials.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkUserRole(String uid, FirebaseUser user) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference userRef = database.getReference("users").child(uid);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DataSnapshot snapshot = task.getResult();
                String role = String.valueOf(snapshot.child("role").getValue());

                switch (role) {
                    case "admin":
                        Toast.makeText(this, "Welcome, Admin!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, AdminActivity.class));
                        break;

                    case "author":
                        Toast.makeText(this, "Welcome, Author!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, AuthorActivity.class));
                        break;

                    case "pending_author":
                        Toast.makeText(this, "Your author account is pending approval.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        break;

                    default:
                        if (user.isEmailVerified()) {
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, ReaderActivity.class));
                        } else {
                            Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                        break;
                }

                finish();
            } else {
                Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
            }
        });
    }
}
