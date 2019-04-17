package com.barmej.driverapllication.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.barmej.driverapllication.R;
import com.barmej.driverapllication.callbacks.CallBack;
import com.barmej.driverapllication.domain.TripManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText passwordEt;
    private EditText emailEt;
    private ProgressBar progressBar;
    private Button loginBt;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        passwordEt = findViewById(R.id.edit_text_password);
        emailEt = findViewById(R.id.edit_text_email);
        progressBar = findViewById(R.id.progress_bar);
        loginBt = findViewById(R.id.button_login);
        loginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginClicked();
            }
        });

    }

    private void loginClicked() {
        if (!isValidEmail(emailEt.getText())) {
            emailEt.setError(getString(R.string.invalid_email));
            return;
        }
        if (passwordEt.getText().length() < 6) {
            passwordEt.setError(getString(R.string.invalid_password_length));
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(emailEt.getText().toString(), passwordEt.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String driverId = task.getResult().getUser().getUid();
                    fetchDriverProfileAndLogin(driverId);
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, R.string.login_error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private void fetchDriverProfileAndLogin(String driverId) {
        TripManager.getInstance().getDriverProfileAndMakeAvailableIfOffline(driverId, new CallBack() {

            @Override
            public void onComplete(boolean isSuccessful) {
                if (isSuccessful) {
                    finish();
                    startActivity(HomeActivity.getStartIntent(LoginActivity.this));
                } else {
                    Toast.makeText(LoginActivity.this, R.string.login_error, Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);

                }

            }
        });
    }
}
