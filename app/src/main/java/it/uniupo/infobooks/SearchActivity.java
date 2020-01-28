package it.uniupo.infobooks;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import it.uniupo.infobooks.adapter.BookAdapter;
import it.uniupo.infobooks.model.Book;
import it.uniupo.infobooks.util.Constants;
import it.uniupo.infobooks.util.QueryUtil;
import it.uniupo.infobooks.util.Util;

public class SearchActivity extends AppCompatActivity {

    private List<Book> mDataset;
    private ListView mListView;
    private BookAdapter mAdapter;
    private TextView mEmptyState;
    private ProgressBar mProgressBar;
    private EditText mEditBook;
    private ImageButton mBtnScanCode;
    private static final String TAG = "SearchActivity";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        int visibility;
        if (savedInstanceState == null) {
            mDataset = new ArrayList<>();
            visibility = View.GONE;
        } else {
            mDataset = savedInstanceState.getParcelableArrayList(Constants.DATASET);
            visibility = savedInstanceState.getInt(Constants.VISIBILITY);
        }

        Toolbar toolbar = findViewById(R.id.search_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mListView = findViewById(R.id.list_view);
        mEmptyState = findViewById(R.id.empty_state);
        mProgressBar = findViewById(R.id.progress_bar);
        mEditBook = findViewById(R.id.edit_book);
        mBtnScanCode = findViewById(R.id.btn_scan_code);

        // Tale TextView e' visibile solo quando la ricerca non ha dato alcun risultato
        mEmptyState.setVisibility(visibility);

        mAdapter = new BookAdapter(this, R.layout.book_item, mDataset);
        mListView.setAdapter(mAdapter);

        mEditBook.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String isbn = mEditBook.getText().toString().trim();
                if (actionId == EditorInfo.IME_ACTION_SEARCH && !TextUtils.isEmpty(isbn)) {
                    Util.hideKeyboard(SearchActivity.this, v);
                    searchOnlineBooks(isbn);
                    return true;
                }
                return false;
            }
        });

        mBtnScanCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCode();
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Controlla se c'e' una connessione ad una rete internet
                if (Util.isNetworkAvailable(SearchActivity.this)) {
                    Book book = mDataset.get(position);
                    // Viene salvato il libro nel Cloud Firestore
                    saveBook(book);
                    Intent intent = new Intent(SearchActivity.this, BookDetailsActivity.class);
                    // Viene passato il libro selezionato a BookDetailsActivity
                    intent.putExtra(Constants.BOOK, book);
                    startActivity(intent);
                }
            }
        });
    }

    // Permette di salvare il libro selezionato dall'utente nel Cloud Firestore
    private void saveBook(Book book) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference docRef = database.collection(uid)
                // Notando che esistono piu' libri con lo stesso isbn si e' scelto di utilizzare il self link (che e' univoco) come id del libro
                .document(book.getSelfLink().substring(44));
        docRef
                .set(book)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Document added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    // Metodo che permette di cercare online i libri con un determinato isbn
    private void searchOnlineBooks(String isbn) {
        // Controlla se c'e' una connessione ad internet, nel caso non ci sia viene mostrato un Toast
        if (Util.isNetworkAvailable(this)) {
            // Avvia l'AsyncTask per effettuare la ricerca dei libri in background
            // Prima dell'AsyncTask viene pulita la lista di libri contenente la ricerca precedente
            mDataset.clear();
            new BookAsyncTask(this).execute(isbn);
        }
    }

    // Permette di scansionare un codice a barre utilizzato per ottenere l'isbn
    private void scanCode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator
                .setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES)
                .setBeepEnabled(false)
                .setBarcodeImageEnabled(true)
                .setOrientationLocked(false)
                .setCaptureActivity(com.journeyapps.barcodescanner.CaptureActivity.class)
                .initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            String isbn = result.getContents();
            if (isbn != null) {
                // Imposta l'isbn ottenuto dalla scansione nel EditText
                mEditBook.setText(isbn);
                // Cerca il libro online
                searchOnlineBooks(isbn);
            } else {
                Toast.makeText(this, getString(R.string.cancelled), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class BookAsyncTask extends AsyncTask<String, Void, List<Book>> {

        private WeakReference<SearchActivity> activityWeakReference;
        private static final String BOOK_BASE_URL = "https://www.googleapis.com/books/v1/volumes?q=isbn:";

        BookAsyncTask(SearchActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SearchActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            activity.mListView.setVisibility(View.GONE);
            activity.mEmptyState.setVisibility(View.GONE);
            activity.mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Book> doInBackground(String... strings) {
            String isbn = strings[0];
            String JSONResponse = makeHttpRequest(isbn);
            return QueryUtil.JSONBookParser(JSONResponse);
        }

        @Override
        protected void onPostExecute(List<Book> books) {
            super.onPostExecute(books);
            SearchActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            activity.mProgressBar.setVisibility(View.GONE);
            activity.mListView.setVisibility(View.VISIBLE);
            if (books.size() == 0) {
                activity.mEmptyState.setVisibility(View.VISIBLE);
            } else {
                activity.mEmptyState.setVisibility(View.GONE);
            }
            activity.mDataset.addAll(books);
            activity.mAdapter.notifyDataSetChanged();
        }

        private String makeHttpRequest(String isbn) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String JSONResponse = null;
            try {
                // Creazione connessione
                String url = BOOK_BASE_URL + isbn;
                URL requestURL = new URL(url);
                urlConnection = (HttpURLConnection) requestURL.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Legge i dati ottenuti dalla risposta
                StringBuilder builder = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                JSONResponse = builder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return JSONResponse;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(Constants.DATASET, (ArrayList<? extends Parcelable>) mDataset);
        outState.putInt(Constants.VISIBILITY, mEmptyState.getVisibility());
    }
}
