package com.magic.eventcalendar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
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

import java.text.SimpleDateFormat;

public class MypageRegister extends AppCompatActivity {

    FirebaseAuth mAuth;

    TextInputEditText regMail;
    TextInputEditText regPassword;
    Button regBtn;
    TextView regToRegister;
    String uid;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mypage_register);

        regMail = findViewById(R.id.register_mail);
        regPassword = findViewById(R.id.register_password);
        regBtn = findViewById(R.id.register_button);
        regToRegister = findViewById(R.id.register_to_login);
        mAuth = FirebaseAuth.getInstance();

        // Database用に変換
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user != null) {
//            String uid = user.getUid();
//        } else {
//            String uid = "GUEST";
//        }
//        String mail = regMail.getText().toString();
//
        regBtn.setOnClickListener(view -> {
            createUser();
        });

        regToRegister.setOnClickListener(view -> {
            startActivity(new Intent(MypageRegister.this, MypageLogin.class));
        });

    }

    private void createUser() {
        String mail = regMail.getText().toString();
        String password = regPassword.getText().toString();

        if (TextUtils.isEmpty(mail)) {
            regMail.setError("Email cannot be empty");
            regMail.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            regPassword.setError("Password cannot be empty");
            regPassword.requestFocus();
        } else {
            mAuth.createUserWithEmailAndPassword(mail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(MypageRegister.this, "User Registered successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MypageRegister.this, MypageLogin.class));
                    } else {
                        Toast.makeText(MypageRegister.this, "Registration Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

//    private void writeNewUser() {
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        String mail = regMail.getText().toString();
//        String userID = uid;
//
////        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
////        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//
//        // Get current timestamp as a ID
//        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
//        String time = "LI" + timeStamp;
//
//        if (TextUtils.isEmpty(mail)) {
//            regMail.setError("Email cannot be empty");
//            regMail.requestFocus();
//        } else {
//            DatabaseReference refUserID = database.getReference("user");
//            refUserID.child(time).child("userID").setValue(userID);
//
//            DatabaseReference refEmail = database.getReference("user");
//            refEmail.child(time).child("mail").setValue(mail);
//        }
//
//    }

}
