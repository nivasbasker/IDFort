package com.zio.idfort.ui;

import static android.app.PendingIntent.getActivity;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;
import static androidx.biometric.BiometricPrompt.ERROR_HW_NOT_PRESENT;
import static androidx.biometric.BiometricPrompt.ERROR_HW_UNAVAILABLE;
import static androidx.biometric.BiometricPrompt.ERROR_NO_BIOMETRICS;
import static androidx.biometric.BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL;
import static androidx.biometric.BiometricPrompt.ERROR_VENDOR;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.zio.idfort.R;
import com.zio.idfort.databinding.ActivityLoginBinding;
import com.zio.idfort.ui.DashboardActivity;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {


    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                switch (errorCode) {
                    case ERROR_HW_NOT_PRESENT:
                    case ERROR_HW_UNAVAILABLE:
                    case ERROR_NO_DEVICE_CREDENTIAL:
                    case ERROR_NO_BIOMETRICS:
                    case ERROR_VENDOR:
                        Toast.makeText(getApplicationContext(), "Device doesn't have credentials, your files might be less secure", Toast.LENGTH_LONG).show();
                        preferences = LoginActivity.this.getSharedPreferences("main", Context.MODE_PRIVATE);
                        preferences.edit().putBoolean("secure", false).apply();

                        Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    default:
                        break;
                }


            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                //Toast.makeText(getApplicationContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                //Toast.makeText(getApplicationContext(), "Authentication failed",Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("ID Fort needs you authentication")
                .setSubtitle("Log in using your biometric/device credential")
                .setAllowedAuthenticators(BIOMETRIC_WEAK | DEVICE_CREDENTIAL)
                .setConfirmationRequired(false)
                .build();

        // Prompt appears when user clicks "Log in".
        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.

        biometricPrompt.authenticate(promptInfo);
    }

    public void unlock(View v) {
        biometricPrompt.authenticate(promptInfo);
    }
}