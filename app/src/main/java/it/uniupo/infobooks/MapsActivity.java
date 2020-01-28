package it.uniupo.infobooks;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import it.uniupo.infobooks.model.Book;
import it.uniupo.infobooks.util.Constants;
import it.uniupo.infobooks.util.LocationTracker;
import it.uniupo.infobooks.util.Util;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Book> mDataset = new ArrayList<>();
    private LocationTracker mLocationTracker;
    private static final float DEFAULT_ZOOM = 15f;
    private static final int REQUEST_LOCATION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (savedInstanceState == null) {
            mapFragment.setRetainInstance(true);
        }
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null) {
            if (Util.checkPermissions(this, Manifest.permission.ACCESS_COARSE_LOCATION) && Util.checkPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                mLocationTracker = new LocationTracker(this);
                if (mLocationTracker.canGetLocaion()) {
                    if (!String.valueOf(mLocationTracker.getLatitude()).equals("0.0") && !String.valueOf(mLocationTracker.getLongitude()).equals("0.0")) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(mLocationTracker.getLatitude(), mLocationTracker.getLongitude()), DEFAULT_ZOOM));
                    }
                } else {
                    mLocationTracker.showSettingsAlertDialog();
                }

                mMap.setMyLocationEnabled(true);

                if (Util.isNetworkAvailable(this)) {
                    fetchBooks();
                }

                mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {
                        if (mMap.getCameraPosition().zoom >= DEFAULT_ZOOM) {
                            addItemsToMaps();
                        } else {
                            mMap.clear();
                        }
                    }
                });

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        marker.showInfoWindow();
                        return true;
                    }
                });
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            }
        }
    }

    private void addItemsToMaps() {
        final MarkerOptions markerOptions = new MarkerOptions();
        if (mMap != null) {
            LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            for (Book book : mDataset) {
                LatLng latLng = new LatLng(book.getLatitude(), book.getLongitude());
                if (bounds.contains(latLng)) {
                    markerOptions.title(book.getTitle());
                    markerOptions.snippet(getString(R.string.tagged_by) + " " + book.getEmail());
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    mMap.addMarker(markerOptions.position(latLng));
                }
            }
        }
    }

    // Metodo che permette di recuperare i libri dal Cloud Firestore
    private void fetchBooks() {
        FirebaseFirestore
                .getInstance()
                .collection(Constants.MAPS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                for (QueryDocumentSnapshot q : task.getResult()) {
                                    mDataset.add(q.toObject(Book.class));
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.setRetainInstance(true);
                mapFragment.getMapAsync(this);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLocationTracker != null) {
            mLocationTracker.stopListener();
        }
    }
}