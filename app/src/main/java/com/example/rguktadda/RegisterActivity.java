package com.example.rguktadda;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText cnfrmPasswordEditText;
    private Button registerSubmitBtn;
    private Button loginBttn;
    private ProgressBar regProgressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.reg_email);
        passwordEditText = findViewById(R.id.reg_password);
        cnfrmPasswordEditText = findViewById(R.id.reg_pass_cnfrm);
        registerSubmitBtn = findViewById(R.id.reg_submit);
        loginBttn = findViewById(R.id.reg_login);
        regProgressBar = findViewById(R.id.reg_progressBar);


        loginBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        registerSubmitBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String cnfrmPasssword = cnfrmPasswordEditText.getText().toString().trim();

                if(!email.isEmpty()&& !password.isEmpty() && !cnfrmPasssword.isEmpty()){
                    if(password.equals(cnfrmPasssword)){

                        regProgressBar.setVisibility(View.VISIBLE);

                        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()){

                                    //sendToMain();
                                    Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
                                    startActivity(setupIntent);
                                    finish();
                                }else{

                                    String errMsg = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this, errMsg,Toast.LENGTH_LONG).show();
                                }
                                regProgressBar.setVisibility(View.INVISIBLE);
                            }
                        });


                    }else {
                        Toast.makeText(RegisterActivity.this,"confirm password and password dont match",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            sendToMain();
        }
    }

    public void sendToMain(){
        Intent loginIntent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(loginIntent);
        finish();
    }
}
