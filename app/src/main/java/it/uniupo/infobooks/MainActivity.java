package it.uniupo.infobooks;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import it.uniupo.infobooks.adapter.BookAdapter;
import it.uniupo.infobooks.model.Book;
import it.uniupo.infobooks.util.Constants;
import it.uniupo.infobooks.util.Util;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private List<Book> mDataset;
    private ListView mListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mTextView;
    private ProgressBar mProgressBar;
    private BookAdapter mAdapter;
    private boolean isUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int visibility;
        if (savedInstanceState != null) {
            mDataset = savedInstanceState.getParcelableArrayList(Constants.DATASET);
            visibility = savedInstanceState.getInt(Constants.VISIBILITY);
            isUpdated = savedInstanceState.getBoolean(Constants.IS_UPDATE);
        } else {
            mDataset = new ArrayList<>();
            isUpdated = false;
            visibility = View.VISIBLE;
        }

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (mAuth.getCurrentUser() == null) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh);
        mListView = findViewById(R.id.list_view);
        mTextView = findViewById(R.id.text_view);
        mProgressBar = findViewById(R.id.progress_bar);

        mTextView.setVisibility(visibility);

        mAdapter = new BookAdapter(this, R.layout.book_item, mDataset);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Book book = mDataset.get(position);
                Intent intent = new Intent(MainActivity.this, BookDetailsActivity.class);
                intent.putExtra(Constants.BOOK, book);
                startActivity(intent);
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Util.isNetworkAvailable(MainActivity.this) && !isUpdated) {
                    retrieveBook();
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    // Permette di recuperare la cronologia dell'utente dal Cloud Firestore
    private void retrieveBook() {
        isUpdated = true;

        mTextView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        String uid = mAuth.getCurrentUser().getUid();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore
                .collection(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        mProgressBar.setVisibility(View.GONE);

                        if (task.getResult() != null) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                Book book = doc.toObject(Book.class);
                                mDataset.add(book);
                                mAdapter.notifyDataSetChanged();
                            }
                            if (mDataset.size() == 0) {
                                // Se la cronologia e' vuota viene mostrata una TextView
                                // Per indicare all'utente come iniziare a cercare libri
                                mTextView.setVisibility(View.VISIBLE);
                            } else {
                                // Altrimenti viene nascosta la TextView
                                mTextView.setVisibility(View.GONE);
                            }
                        } else {
                            isUpdated = false;
                        }
                    }
                });
    }

    // Aggiunge elementi alla barra delle azioni
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // Gestisce i click degli elementi presenti nella barra delle azioni
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
                return true;
            case R.id.action_map:
                startActivity(new Intent(this, MapsActivity.class));
                return true;
            case R.id.action_logout:
                mAuth.signOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setMessage(getString(R.string.exit_message))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(Constants.DATASET, (ArrayList<? extends Parcelable>) mDataset);
        outState.putInt(Constants.VISIBILITY, mTextView.getVisibility());
        outState.putBoolean(Constants.IS_UPDATE, isUpdated);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        // Se i libri non sono ancora stati caricati e c'e' una connessione ad una rete internet si recupera la cronologia dal Cloud
        if (!isUpdated && Util.isNetworkAvailable(this)) {
            retrieveBook();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
