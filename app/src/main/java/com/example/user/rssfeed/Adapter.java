package com.example.user.rssfeed;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.LoginFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Adapter extends ArrayAdapter<ListModel> {
    public Adapter(Context context, ArrayList<ListModel> listModel){
        super(context,0, listModel);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        final ListModel list = getItem(position);
        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.items,parent, false);
        }

        TextView tvTitle = convertView.findViewById(R.id.tvTitle);
        ImageButton ibImage = convertView.findViewById(R.id.imageButton);

        tvTitle.setText(list.title);
        try {
            Bitmap image = new getImage().execute(list.image).get();
            ibImage.setImageBitmap(image);
            ibImage.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    Uri uri = Uri.parse(list.links);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                   getContext().startActivity(intent);
                }
            });
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        return convertView;
    }

    public class getImage extends AsyncTask<String, Void, Bitmap>{

        Exception exception = null;
        Bitmap bitmap;
        @Override
        protected Bitmap doInBackground(String...urls) {

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
                return bitmap;
            }catch (MalformedURLException e){
                exception = e;

                Log.d(e.toString(),"asd");
            }catch (IOException e){
                exception = e;
                Log.d(e.toString(),"asd");
            }
            return bitmap;
        }
    }
}
