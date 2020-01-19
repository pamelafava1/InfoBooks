package it.uniupo.infobooks.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {

    private String selfLink;
    private String title;
    private String authors;
    private String publishedDate;
    private String description;
    private int pageCount;
    private double averageRating;
    private String thumbnail;
    private double latitude;
    private double longitude;
    private String email;

    public Book() {}

    public Book(String selfLink, String title, String authors, String publishedDate, String description, int pageCount, double averageRating, String thumbnail) {
        this.selfLink = selfLink;
        this.title = title;
        this.authors = authors;
        this.publishedDate = publishedDate;
        this.description = description;
        this.pageCount = pageCount;
        this.averageRating = averageRating;
        this.thumbnail = thumbnail;
    }

    private Book(Parcel in) {
        selfLink = in.readString();
        title = in.readString();
        authors = in.readString();
        publishedDate = in.readString();
        description = in.readString();
        pageCount = in.readInt();
        averageRating = in.readDouble();
        thumbnail = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        email = in.readString();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public String getSelfLink() {
        return selfLink;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthors() {
        return authors;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public String getDescription() {
        return description;
    }

    public int getPageCount() {
        return pageCount;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getEmail() {
        return email;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(selfLink);
        dest.writeString(title);
        dest.writeString(authors);
        dest.writeString(publishedDate);
        dest.writeString(description);
        dest.writeInt(pageCount);
        dest.writeDouble(averageRating);
        dest.writeString(thumbnail);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(email);
    }
}
