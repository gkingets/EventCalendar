package com.magic.eventcalendar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MypageLogin extends AppCompatActivity {

    FirebaseAuth mAuth;

    TextInputEditText loginMail;
    TextInputEditText loginPassword;
    Button loginBtn;
    TextView loginToRegister;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mypage_login);

        loginMail = findViewById(R.id.login_mail);
        loginPassword = findViewById(R.id.login_password);
        loginBtn = findViewById(R.id.login_button);
        loginToRegister = findViewById(R.id.login_to_register);

        mAuth = FirebaseAuth.getInstance();

        // return button
        ImageView returnButton = findViewById(R.id.login_back);
        returnButton.setOnClickListener(v -> finish());

        loginBtn.setOnClickListener(view -> {
            loginUser();
        });

        loginToRegister.setOnClickListener(view -> {
            startActivity(new Intent(MypageLogin.this, MypageRegister.class));
        });

        

    }

    private void loginUser() {
        String mail = loginMail.getText().toString();
        String password = loginPassword.getText().toString();

        if (TextUtils.isEmpty(mail)) {
            loginMail.setError("Email cannot be empty");
            loginMail.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            loginPassword.setError("Password cannot be empty");
            loginPassword.requestFocus();
        } else {
            mAuth.signInWithEmailAndPassword(mail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(MypageLogin.this, "Sing in successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MypageLogin.this, MainActivity.class));
                    } else {
                        Toast.makeText(MypageLogin.this, "Sing in Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


}
