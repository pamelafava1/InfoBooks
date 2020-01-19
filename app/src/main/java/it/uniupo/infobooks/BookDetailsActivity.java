package it.uniupo.infobooks;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import it.uniupo.infobooks.model.Book;
import it.uniupo.infobooks.util.LocationTracker;
import it.uniupo.infobooks.util.Util;

public class BookDetailsActivity extends AppCompatActivity {

    private TextView mTitle, mAuthors, mPublishedDate, mDescription, mPageCount;
    private RatingBar mAverageRating;
    private ImageView mThumbnail;
    private Book mBook;
    private String mSelfLink;
    private LocationTracker mLocationTracker;
    private static final int REQUEST_LOCATION = 101;
    private static final String TAG = "BookDetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        if (savedInstanceState != null) {
            mBook = savedInstanceState.getParcelable("book");
        } else {
            mBook = getIntent().getParcelableExtra("book");
            mSelfLink = mBook.getSelfLink();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mTitle = findViewById(R.id.book_title);
        mAuthors = findViewById(R.id.book_authors);
        mPublishedDate = findViewById(R.id.book_published_date);
        mDescription = findViewById(R.id.book_description);
        mPageCount = findViewById(R.id.book_page_count);
        mAverageRating = findViewById(R.id.book_average_rating);
        mThumbnail = findViewById(R.id.book_thumbnail);

        updateUI();
    }

    private void updateUI() {
        mTitle.setText(mBook.getTitle());
        mAuthors.setText(mBook.getAuthors());
        mPublishedDate.setText(mBook.getPublishedDate());
        mDescription.setText(mBook.getDescription());
        mPageCount.setText(String.valueOf(mBook.getPageCount()));
        mAverageRating.setRating((float) mBook.getAverageRating());
        Glide
                .with(this)
                .load(mBook.getThumbnail())
                .placeholder(getDrawable(R.drawable.placeholder))
                .into(mThumbnail);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.book_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_on_map:
                showAlertDialog();
                return true;
            case android.R.id.home:
                BookDetailsActivity.this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setMessage(getString(R.string.save_on_the_map))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Util.isNetworkAvailable(BookDetailsActivity.this)) {
                            getDeviceLocation();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getDeviceLocation() {
        if (!Util.checkPermissions(this, Manifest.permission.ACCESS_COARSE_LOCATION) && !Util.checkPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            mLocationTracker = new LocationTracker(this);
            if (mLocationTracker.canGetLocaion()) {
                double latitude = mLocationTracker.getLatitude();
                double longitude = mLocationTracker.getLongitude();
                // Con il LocationManager puo' capitare di avere latitudine e longitudine pari a 0.0
                // In tal caso si riprova a recuperare di nuovo la posizione
                if (String.valueOf(latitude).equals("0.0") && String.valueOf(longitude).equals("0.0")) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            double latitude = mLocationTracker.getLatitude();
                            double longitude = mLocationTracker.getLongitude();
                            // Nel caso la latitudine e longitudine siano pari a 0.0 viene mostrato un Toast con un messaggio di errore
                            // In caso contrario si salva il libro sulla mappa
                            if (String.valueOf(latitude).equals("0.0") && String.valueOf(longitude).equals("0.0")) {
                                Toast.makeText(BookDetailsActivity.this, getString(R.string.location_not_found), Toast.LENGTH_SHORT).show();
                            } else {
                                saveOnMap(latitude, longitude);
                            }
                        }
                    }, 1000);
                } else {
                    saveOnMap(latitude, longitude);
                }
            } else {
                mLocationTracker.showSettingsAlertDialog();
            }
        }
    }

    private void saveOnMap(double latitude, double longitude) {
        mBook.setLatitude(latitude);
        mBook.setLongitude(longitude);
        mBook.setEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        DocumentReference documentReference = FirebaseFirestore
                .getInstance()
                .collection("maps")
                .document(mSelfLink.substring(44) + latitude + longitude);
        documentReference.set(mBook)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(BookDetailsActivity.this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Book added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding book", e);
                        Toast.makeText(BookDetailsActivity.this, getString(R.string.error_message), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getDeviceLocation();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("book", mBook);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLocationTracker != null) {
            mLocationTracker.stopListener();
        }
    }
}
