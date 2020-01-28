package it.uniupo.infobooks.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import it.uniupo.infobooks.R;

public class Util {

    // Metodo che permette di verificare se il dispositivo e' connesso ad una rete internet
    // In caso non sia connesso viene mostrato un Toast contenente un messaggio di errore
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        } else {
            Toast.makeText(context, context.getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // Metodo che permette di nascondere la tastiera
    public static void hideKeyboard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // Metodo che permette di verificare se l'applicazione dispone dell'autorizzazione passata come parametro
    public static boolean checkPermissions(Context context, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }
}
