package com.example.vehidocs.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.example.vehidocs.R;
import com.example.vehidocs.features.models.UserHelperClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class SignUpOTPConfirmation extends AppCompatActivity {

    Button verify_btn;
    EditText mobileNumberEnteredByTheUser;
    ProgressBar progressBar;
    String verificationCodeBySystem;
    PinView codeEnteredByUser;
    FirebaseDatabase rootNode;
    DatabaseReference reference;
    public static final String fullNameParameter = "fullName";
    public static final String mobileNoParameter = "mobileNo";
    public static final String emailParameter = "email";
    public static final String licenseParameter = "license";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_otpconfirmation);

        // Back Button
        Button back_button = findViewById(R.id.back_Btn);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpOTPConfirmation.this, RequestActivity.class);
                startActivity(intent);
                finish();
            }
        });

        verify_btn = findViewById(R.id.verify_btn);
        mobileNumberEnteredByTheUser = findViewById(R.id.verification_code_entered_by_user);
//        progressBar = findViewById(R.id.progress_bar);
        codeEnteredByUser = findViewById(R.id.verification_code_entered_by_user);

        String mobileNo = getIntent().getStringExtra("mobileNo");

        sendVerificationCodeToUser(mobileNo);

        verify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredCode = codeEnteredByUser.getText().toString();
                if (enteredCode.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter a value", Toast.LENGTH_SHORT).show();
                } else if (enteredCode.length() == 6) {
                    verifyCode(enteredCode);
                } else {
                    Toast.makeText(getApplicationContext(), "Invalid OTP pattern", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendVerificationCodeToUser(String mobileNo) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber("+977" + mobileNo, 60, TimeUnit.SECONDS, this, mCallbacks);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            verificationCodeBySystem = s;
            Log.d("code", verificationCodeBySystem);
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                verificationCodeBySystem = code;
//                progressBar.setVisibility(View.VISIBLE);
//                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(SignUpOTPConfirmation.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private void verifyCode(String verificationCodeByUser) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCodeBySystem, verificationCodeByUser);
        signInTheUserByCredentials(credential);
    }

    private void signInTheUserByCredentials(PhoneAuthCredential credential) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(SignUpOTPConfirmation.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            rootNode = FirebaseDatabase.getInstance("https://digitalvehicle-5fc1b-default-rtdb.firebaseio.com");
                            reference = rootNode.getReference("Users");
                            String fullName = getIntent().getStringExtra(fullNameParameter);
                            String email = getIntent().getStringExtra(emailParameter);
                            String mobileNo = getIntent().getStringExtra(mobileNoParameter);
                            String license = getIntent().getStringExtra(licenseParameter);
                            UserHelperClass user = new UserHelperClass(fullName, email, mobileNo, license);
                            //reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(helperClass);
                            reference.child(mobileNo).setValue(user);
                            Log.d("Phone Auth", "successful");
                            Intent intent = new Intent(getApplicationContext(), MainPageActivity.class);
                            intent.putExtra(MainPageActivity.mobileNoParameter, mobileNo);
                            intent.putExtra(MainPageActivity.fullNameParameter, fullName);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(SignUpOTPConfirmation.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Back Pressed", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), RequestActivity.class);
        startActivity(intent);
    }
}