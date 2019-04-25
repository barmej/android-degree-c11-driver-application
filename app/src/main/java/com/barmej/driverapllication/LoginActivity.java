package com.barmej.driverapllication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.barmej.driverapllication.callback.CallBack;
import com.barmej.driverapllication.domain.TripManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseUser;

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
        FirebaseUser currentFireBaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentFireBaseUser!=null ){
            hideForm(true);
            fetchDriverProfileAndLogin(currentFireBaseUser.getUid());
        }

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

        hideForm(true);

        FirebaseAuth.getInstance().signInWithEmailAndPassword(emailEt.getText().toString(), passwordEt.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String driverId = task.getResult().getUser().getUid();
                    fetchDriverProfileAndLogin(driverId);
                } else {

                    hideForm(false);

                    Toast.makeText(LoginActivity.this, R.string.login_error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private void hideForm(boolean hide) {
        if (hide) {
            progressBar.setVisibility(View.VISIBLE);
            passwordEt.setVisibility(View.INVISIBLE);
            emailEt.setVisibility(View.INVISIBLE);
            loginBt.setVisibility(View.INVISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            passwordEt.setVisibility(View.VISIBLE);
            emailEt.setVisibility(View.VISIBLE);
            loginBt.setVisibility(View.VISIBLE);
        }
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
                    hideForm(false);
                }

            }
        });
    }
}
