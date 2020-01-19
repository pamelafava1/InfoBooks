package it.uniupo.infobooks.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import it.uniupo.infobooks.R;
import it.uniupo.infobooks.model.Book;

public class BookAdapter extends ArrayAdapter<Book> {

    private Context mContext;
    private List<Book> mDataset;

    public BookAdapter(Context context, int resource, List<Book> dataset) {
        super(context, resource);
        mContext = context;
        mDataset = dataset;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.book_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Book book = mDataset.get(position);
        holder.title.setText(book.getTitle());
        holder.authors.setText(book.getAuthors());
        holder.averageRating.setRating((float) book.getAverageRating());
        Glide
                .with(mContext)
                .load(book.getThumbnail())
                .placeholder(R.drawable.placeholder)
                .into(holder.thumbnail);
        return convertView;
    }

    @Override
    public int getCount() {
        return mDataset.size();
    }

    public class ViewHolder {
        TextView title;
        TextView authors;
        RatingBar averageRating;
        ImageView thumbnail;

        ViewHolder(View view) {
            title = view.findViewById(R.id.book_title);
            authors = view.findViewById(R.id.book_authors);
            averageRating = view.findViewById(R.id.book_average_rating);
            thumbnail = view.findViewById(R.id.book_thumbanil);
        }
    }
}
