package com.example.bookrate.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bookrate.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.regex.Pattern;

public class AuthorRegisterActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, phoneEditText, nameEditText;
    private Button registerButton;

    private FirebaseAuth mAuth;
    private DatabaseReference pendingAuthorsRef;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // we reuse the same layout

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/");
        pendingAuthorsRef = database.getReference("pending_authors");

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();

            if (!validateInputs(name, email, password, phone)) return;

            registerAuthor(email, password, name, phone);
        });
    }

    private boolean validateInputs(String name, String email, String password, String phone) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            Toast.makeText(this, "Password must have at least 8 characters, one uppercase letter, and one special character", Toast.LENGTH_LONG).show();
            return false;
        }

        if (!phone.matches("\\d{10}")) {
            Toast.makeText(this, "Phone number must be exactly 10 digits", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void registerAuthor(String email, String password, String name, String phone) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            savePendingAuthorToDatabase(user.getUid(), name, email, phone);
                        }
                    } else {
                        Toast.makeText(this, "Registration failed", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void savePendingAuthorToDatabase(String userId, String name, String email, String phone) {
        HashMap<String, String> authorMap = new HashMap<>();
        authorMap.put("name", name);
        authorMap.put("email", email);
        authorMap.put("phone", phone);
        authorMap.put("role", "author");

        pendingAuthorsRef.child(userId).setValue(authorMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Request sent to admin. Please wait!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Database error", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
