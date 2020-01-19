package it.uniupo.infobooks.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.uniupo.infobooks.model.Book;

public class QueryUtil {

    private static final String TAG = "QueryUtil";

    // La risposta ottenuta e' in formato JSON e il seguente metodo permette di effettuare il parsing
    public static List<Book> JSONBookParser(String JSONResponse) {
        List<Book> dataset = new ArrayList<>();
        if (JSONResponse != null) {
            try {
                if (JSONResponse.length() > 0) {
                    JSONObject jsonObject = new JSONObject(JSONResponse);
                    JSONArray itemsArray = jsonObject.getJSONArray("items");

                    for (int i=0; i<itemsArray.length(); i++) {
                        JSONObject bookJSON = itemsArray.getJSONObject(i);
                        JSONObject volumeInfo = bookJSON.getJSONObject("volumeInfo");

                        String selfLink = "No self link";
                        String title = "No title";
                        String authors = "No authors";
                        String publishedDate = "No published date";
                        String description = "No description";
                        int pageCount = 0;
                        double averageRating = 0.0;
                        String thumbnail = null;

                        try {
                            selfLink = bookJSON.getString("selfLink");
                        } catch (JSONException e) {
                            Log.e(TAG, "No self link");
                        }

                        try {
                            title = volumeInfo.getString("title");
                        } catch (JSONException e) {
                            Log.e(TAG, "No title");
                        }

                        try {
                            JSONArray authorsArray = volumeInfo.getJSONArray("authors");
                            StringBuilder builder = new StringBuilder();
                            builder.append(authorsArray.getString(0));
                            for (int j=1; j<authorsArray.length(); j++) {
                                builder
                                        .append(", ")
                                        .append(authorsArray.getString(j));
                            }
                            authors = builder.toString();
                        } catch (JSONException e) {
                            Log.e(TAG, "No authors");
                        }

                        try {
                            publishedDate = volumeInfo.getString("publishedDate");
                        } catch (JSONException e) {
                            Log.e(TAG, "No published date");
                        }

                        try {
                            description = volumeInfo.getString("description");
                        } catch (JSONException e) {
                            Log.e(TAG, "No description");
                        }

                        try {
                            pageCount = volumeInfo.getInt("pageCount");
                        } catch (JSONException e) {
                            Log.e(TAG, "No page count");
                        }

                        try {
                            averageRating = volumeInfo.getDouble("averageRating");
                        } catch (JSONException e) {
                            Log.e(TAG, "No average rating");
                        }

                        try {
                            thumbnail = volumeInfo.getJSONObject("imageLinks").getString("thumbnail");
                        } catch (JSONException e) {
                            Log.e(TAG, "No thumbnail");
                        }

                        Book book = new Book(selfLink, title, authors, publishedDate, description, pageCount, averageRating, thumbnail);
                        dataset.add(book);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return dataset;
    }
}
