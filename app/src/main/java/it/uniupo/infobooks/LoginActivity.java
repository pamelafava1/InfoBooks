package it.uniupo.infobooks;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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

import it.uniupo.infobooks.util.Util;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText mEditEmail, mEditPassword;
    private Button mBtnLogin, mBtnForgotPassword, mBtnSignup;
    private ProgressBar mProgressBar;
    private String mEmail, mPassword;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Permette di capire se un utente e' gia' loggato
        // Nel caso in cui si sia loggato viene fatta partire l'Activity principale
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        // Se l'utente non e' loggato viene mostrato il layout del login
        setContentView(R.layout.activity_login);

        mEditEmail = findViewById(R.id.edit_email);
        mEditPassword = findViewById(R.id.edit_password);
        mBtnLogin = findViewById(R.id.btn_login);
        mBtnForgotPassword = findViewById(R.id.btn_forgot_password);
        mBtnSignup = findViewById(R.id.btn_signup);
        mProgressBar = findViewById(R.id.progress_bar);

        mEditEmail.addTextChangedListener(loginTextWatcher);
        mEditPassword.addTextChangedListener(loginTextWatcher);

        mEditPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE && mBtnLogin.isEnabled()) {
                    Util.hideKeyboard(LoginActivity.this, v);
                    login();
                    return true;
                }
                return false;
            }
        });

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.hideKeyboard(LoginActivity.this, v);
                login();
            }
        });

        mBtnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });

        mBtnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });
    }

    private TextWatcher loginTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        // Metodo che viene richiamato quando il testo dell'email o della password viene modificato
        // Finche' la password e/o l'email sono vuote il bottone per loggarsi rimane disabilitato, in caso contrario viene abilitato
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mEmail = mEditEmail.getText().toString().trim();
            mPassword = mEditPassword.getText().toString().trim();
            mBtnLogin.setEnabled(!TextUtils.isEmpty(mEmail) && !TextUtils.isEmpty(mPassword));
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    private void login() {
        // Controlla se c'e' una connessione ad una rete internet
        // Nel caso ci fosse si prosegue con il login, in caso contrario viene visualizzato un Toast con un messaggio di errore
        if (Util.isNetworkAvailable(this)) {

            // Viene reso visibile l'indicatore di progresso, che rimarra' visibile fino al completamento dell'operazione
            mProgressBar.setVisibility(View.VISIBLE);

            // Metodo che permette ad un utente di loggarsi inserendo le proprie credenziali, composte da email e password
            mAuth
                    .signInWithEmailAndPassword(mEmail, mPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            // Una volta completata l'operazione viene nascosto l'indicatore di progresso
                            mProgressBar.setVisibility(View.GONE);

                            if (task.isSuccessful()) {
                                // Il login e' andato a buon fine quindi viene fatta partire l'Activity principale
                                Log.d(TAG, "Login successful");
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                // Il login non e' andato a buon fine quindi viene mostrato un Toast contente un messaggio di errore
                                Log.e(TAG, "Login failed", task.getException());
                                Toast.makeText(LoginActivity.this, getString(R.string.log_in_error_message), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.GONE);
    }
}
