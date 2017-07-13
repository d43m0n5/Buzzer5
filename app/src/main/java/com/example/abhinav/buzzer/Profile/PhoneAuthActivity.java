package com.example.abhinav.buzzer.Profile;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.abhinav.buzzer.R;
import com.example.abhinav.buzzer.Timeline.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends AppCompatActivity {

    private EditText etxtPhone;
    private FirebaseAuth mAuth;
 //   private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText etxtPhoneCode;
    private String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        etxtPhone = (EditText)findViewById(R.id.etxtPhone);
        etxtPhoneCode = (EditText)findViewById(R.id.etxtPhoneCode);
        mAuth =FirebaseAuth.getInstance();
       /* mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null){
                    Toast.makeText(PhoneAuthActivity.this , "Now you are verified..", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(PhoneAuthActivity.this , MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }; */

    }

    public void requestCode(View view){
        String phoneNumber = etxtPhone.getText().toString();
        if (TextUtils.isEmpty(phoneNumber))
            return;
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber , 60 , TimeUnit.SECONDS , PhoneAuthActivity.this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        //called if it is not needed to enter verification code
                        signInWithCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        //incorrect phone number , verification code
                        Toast.makeText(PhoneAuthActivity.this , "Verification Failed" ,Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        mVerificationId = s;
                    }

                    @Override
                    public void onCodeAutoRetrievalTimeOut(String s) {
                        super.onCodeAutoRetrievalTimeOut(s);
                        //Called if timeout or code isnt retrieved
                        Toast.makeText(PhoneAuthActivity.this , "Verification Timed Out" ,Toast.LENGTH_LONG).show();

                    }
                }
        );
    }

    private void signInWithCredential(PhoneAuthCredential phoneAuthCredential){

        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                Toast.makeText(PhoneAuthActivity.this , "Now you are verified..", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(PhoneAuthActivity.this , SetupActivity.class) ;
                startActivity(intent);
                finish();
            }
            }
        });

    }

    public void signIn (View view){
        String code = etxtPhoneCode.getText().toString();
        if (TextUtils.isEmpty(code))
            return;

        signInWithCredential(PhoneAuthProvider.getCredential(mVerificationId , code));
    }

}
