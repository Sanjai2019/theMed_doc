package com.example.ehospital;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "PhoneAuth";
    private String phoneVerificationId;
    private FirebaseAuth fbAuth;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private ProgressDialog progressdialog;
    EditText phoneText,codeText;
    CountryCodePicker ccp;
    String number;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            verificationCallbacks;
    private Button verifyButton;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference,databaseReference1;
    private Button sendButton;
    private Button resendButton;
    FirebaseUser user;
    SharedPreferences sharedPreferences;
    String ch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        phoneText=findViewById(R.id.phoneText);
        verifyButton = (Button) findViewById(R.id.verifyButton);
        sendButton = (Button) findViewById(R.id.sendButton);
        resendButton = (Button) findViewById(R.id.resendButton);
        codeText = (EditText) findViewById(R.id.codeText);
        ch=phoneText.getText().toString();
        ccp = (CountryCodePicker)findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phoneText);
        fbAuth = FirebaseAuth.getInstance();
        user= fbAuth.getCurrentUser();
        /*firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference().child("Doctor database");
        databaseReference1=firebaseDatabase.getReference().child("LaboratoryRegistrations");
        SharedPreferences sharedPreferences1 = getSharedPreferences("labordoc",MODE_PRIVATE);*/
        //String checker = sharedPreferences1.getString("prefs","");
        /*if(user!=null && checker.equals("doctor"))
        {
            startActivity(new Intent(MainActivity.this,doctor_registration.class));
        }
        else if(user!=null && checker.equals("lab"))
            startActivity(new Intent(MainActivity.this,LabRegistration.class));*/
        if(user!=null)
        {
            startActivity(new Intent(MainActivity.this,DoctorOrLabTechnician.class));
        }
        sharedPreferences=getSharedPreferences("MyPrefs",MODE_PRIVATE);

    }
    public void sendCode(View view) {
        number = ccp.getFullNumberWithPlus();
        if(number.length()<=3) {
            phoneText.setError("Can't be empty");
        }
        else {
            progressdialog = new ProgressDialog(MainActivity.this);
            progressdialog.setMessage("Please Wait....");
            setUpVerificatonCallbacks();
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    number,        // Phone number to verify
                    60,                 // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    this,               // Activity (for callback binding)
                    verificationCallbacks);
        }
    }
    private void setUpVerificatonCallbacks() {

        verificationCallbacks =
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(
                            PhoneAuthCredential credential) {

                        resendButton.setEnabled(false);
                        verifyButton.setEnabled(false);
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {

                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                            Log.d(TAG, "Invalid credential: "
                                    + e.getLocalizedMessage());
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            // SMS quota exceeded
                            Log.d(TAG, "SMS Quota exceeded.");
                        }
                    }

                    @Override
                    public void onCodeSent(String verificationId,PhoneAuthProvider.ForceResendingToken token)
                    {
                        progressdialog.show();
                        phoneVerificationId = verificationId;
                        resendToken = token;

                        verifyButton.setEnabled(true);
                        sendButton.setEnabled(false);
                        resendButton.setEnabled(true);
                    }
                };

    }
    public void verifyCode(View view) {

        String code = codeText.getText().toString();
        if(code.isEmpty())
            codeText.setError("Can't be Empty");
        else {
            PhoneAuthCredential credential =
                    PhoneAuthProvider.getCredential(phoneVerificationId,code);
            String checkcode=credential.getSmsCode();
            if(checkcode.equals(code))
                signInWithPhoneAuthCredential(credential);
            else
                Toast.makeText(getApplicationContext(),"Invalid code",Toast.LENGTH_LONG).show();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        fbAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            resendButton.setEnabled(false);
                            verifyButton.setEnabled(false);
                            FirebaseUser user = task.getResult().getUser();
                            String phoneNumber = user.getPhoneNumber();
                            // try {
                            //}
                            //catch (Exception e)
                            //{
                            //Toast.makeText(policereg.this,"ces",Toast.LENGTH_LONG).show();
                            //}
                            progressdialog.dismiss();
                            Intent intent = new Intent(MainActivity.this, DoctorOrLabTechnician.class);
                            //intent.putExtra("phone", phoneNumber);
                            startActivity(intent);
                            finish();
                        } else {
                            if (task.getException() instanceof
                                    FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(MainActivity.this,"The verification code entered was invalid",Toast.LENGTH_SHORT).show();

                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    public void resendCode(View view) {

        number = ccp.getFullNumberWithPlus();
            setUpVerificatonCallbacks();

            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    number,
                    60,
                    TimeUnit.SECONDS,
                    this,
                    verificationCallbacks,
                    resendToken);
        }

}

