package it.uniupo.infobooks;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import it.uniupo.infobooks.util.Util;

public class ForgotPasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputLayout mEmailWrapper;
    private EditText mEditEmail;
    private Button mBtnFindYourAccount;
    private ProgressBar mProgressBar;
    private static final String TAG = "ForgotPasswordActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.find_your_account));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mEmailWrapper = findViewById(R.id.email_wrapper);
        mEditEmail = findViewById(R.id.edit_email);
        mBtnFindYourAccount = findViewById(R.id.btn_find_your_account);
        mProgressBar = findViewById(R.id.progress_bar);

        mEditEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Util.hideKeyboard(ForgotPasswordActivity.this, v);
                    sendEmail();
                    return true;
                }
                return false;
            }
        });

        mBtnFindYourAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.hideKeyboard(ForgotPasswordActivity.this, v);
                sendEmail();
            }
        });
    }

    private void sendEmail() {
        mEmailWrapper.setError("");
        if (Util.isNetworkAvailable(this)) {
            String email = mEditEmail.getText().toString().trim();
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                mProgressBar.setVisibility(View.VISIBLE);
                mAuth
                        .sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mProgressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Email sent");
                                    Toast.makeText(ForgotPasswordActivity.this, getString(R.string.sent_email_message), Toast.LENGTH_SHORT).show();
                                    finish();
                                } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                    // Tale eccezione viene generata se l'account corrispondente all'email non esiste o e' stato disabilito
                                    Log.d(TAG, "Account not found", task.getException());
                                    Toast.makeText(ForgotPasswordActivity.this, getString(R.string.account_not_found), Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.d(TAG, "Email sending failed", task.getException());
                                    Toast.makeText(ForgotPasswordActivity.this, getString(R.string.error_message), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                mEmailWrapper.setError(getString(R.string.email_error_message));
                mEditEmail.requestFocus();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.GONE);
    }
}
