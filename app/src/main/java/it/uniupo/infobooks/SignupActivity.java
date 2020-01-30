package it.uniupo.infobooks;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import it.uniupo.infobooks.util.Util;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputLayout mEmailWrapper;
    private EditText mEditEmail, mEditPassword;
    private Button mBtnSignup, mBtnLogin;
    private ProgressBar mProgressBar;
    private String mEmail, mPassword;
    private static final String TAG = "SignupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        mEmailWrapper = findViewById(R.id.email_wrapper);
        mEditEmail = findViewById(R.id.edit_email);
        mEditPassword = findViewById(R.id.edit_password);
        mBtnSignup = findViewById(R.id.btn_signup);
        mBtnLogin = findViewById(R.id.btn_login);
        mProgressBar = findViewById(R.id.progress_bar);

        mEditEmail.addTextChangedListener(signupTextWatcher);
        mEditPassword.addTextChangedListener(signupTextWatcher);

        mEditPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE && mBtnSignup.isEnabled()) {
                    Util.hideKeyboard(SignupActivity.this, v);
                    signup();
                    return true;
                }
                return false;
            }
        });

        mBtnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.hideKeyboard(SignupActivity.this, v);
                signup();
            }
        });

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private TextWatcher signupTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        // Metodo che viene richiamato quando il testo dell'email o della password viene modificato
        // Finche' l'email e' vuota e la password e' minore di sei caratteri il bottone per registrarsi rimane disabilitato, in caso contrario viene abilitato
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mEmailWrapper.setError("");
            mEmail = mEditEmail.getText().toString().trim();
            mPassword = mEditPassword.getText().toString().trim();
            mBtnSignup.setEnabled(!TextUtils.isEmpty(mEmail) && mPassword.length() >= 6);
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    private void signup() {
        // Controlla che ci sia una connessione ad una rete internet
        // Nel caso ci fosse si prosegue con la registrazione dell'account, in caso contrario viene visualizzato un Toast con un messaggio di errore
        if (Util.isNetworkAvailable(this)) {

            // Controlla che il formato dell'email sia corretto
            if (Patterns.EMAIL_ADDRESS.matcher(mEmail).matches()) {

                mProgressBar.setVisibility(View.VISIBLE);

                // Metodo che permette ad un utente di creare un nuovo account inserendo le proprie credenziali, composte da email e password
                mAuth
                        .createUserWithEmailAndPassword(mEmail, mPassword)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                mProgressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    // La registrazione e' andata a buon fine quindi viene fatta partire l'Activity principale
                                    Log.d(TAG, "Signup successful");
                                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                    finish();

                                    // La registrazione non e' andata a buon fine quindi viene mostrato un Toast contente un messaggio di errore
                                } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    // Tale eccezione viene generata se l'account corrispondente all'email e' gia' esistente
                                    Log.w(TAG, "Account already exits", task.getException());
                                    Toast.makeText(SignupActivity.this, getString(R.string.account_already_exists), Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.w(TAG, "Signup failed", task.getException());
                                    Toast.makeText(SignupActivity.this, getString(R.string.error_message), Toast.LENGTH_SHORT).show();
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
