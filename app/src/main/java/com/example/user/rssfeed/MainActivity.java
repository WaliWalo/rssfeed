package com.example.user.rssfeed;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    ListView lvRss;
    ArrayList<String> titles;
    ArrayList<String> links;
    ArrayList<String> image;
    ArrayList<ListModel> arrayOfList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        JobManager.create(this).addJobCreator(new jobCreator());
        lvRss = findViewById(R.id.list);
        titles = new ArrayList<>();
        links = new ArrayList<>();
        image = new ArrayList<>();
        arrayOfList = new ArrayList<ListModel>();

        new BackgroundProcess().execute();
        syncJob.scheduleJob();
    }

    public class jobCreator implements JobCreator {

        @Override
        @Nullable
        public Job create(@NonNull String tag) {
            switch (tag) {
                case syncJob.TAG:
                    return new syncJob();
                default:
                    return null;
            }
        }
    }

    public static class syncJob extends Job {

        public static final String TAG = "job_note_sync";

        @Override
        @NonNull
        protected Result onRunJob(@NonNull Params params) {
            Log.d("test","test");
            return Result.SUCCESS;
        }

        public static void scheduleJob() {
            Set<JobRequest> jobRequests = JobManager.instance().getAllJobRequestsForTag(syncJob.TAG);
            if (!jobRequests.isEmpty()) {
                return;
            }
            new JobRequest.Builder(syncJob.TAG)
                    .setPeriodic(TimeUnit.MINUTES.toMillis(15), TimeUnit.MINUTES.toMillis(7))
                    .setUpdateCurrent(true) // calls cancelAllForTag(NoteSyncJob.TAG) for you
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setRequirementsEnforced(true)
                    .build()
                    .schedule();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                Adapter adapter = new Adapter(MainActivity.this,arrayOfList);
                adapter.notifyDataSetChanged();
                Toast msg = Toast.makeText(MainActivity.this,"Reloaded",Toast.LENGTH_SHORT);
                msg.show();
                lvRss.setAdapter(adapter);
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this,Settings.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public InputStream getInputStream(URL url){
        try{
            return url.openConnection().getInputStream();
        }catch (IOException e){
            return null;
        }
    }

    public class BackgroundProcess extends AsyncTask<Integer, Void, Exception>{

        Exception exception = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Exception doInBackground(Integer... integers) {

            try{

                Intent intent = getIntent();
                String feed = intent.getStringExtra("feed");

                URL url = new URL(feed);

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

                factory.setNamespaceAware(false);
                XmlPullParser pullParser = factory.newPullParser();

                pullParser.setInput(getInputStream(url),"UTF_8");
                boolean isInsideItem = false;
                int eventType = pullParser.getEventType();

                while(eventType != XmlPullParser.END_DOCUMENT){
                    if(eventType == XmlPullParser.START_TAG){
                        if(pullParser.getName().equalsIgnoreCase("item")){
                            isInsideItem = true;
                        }else if(pullParser.getName().equalsIgnoreCase("title")){
                            if(isInsideItem){
                                titles.add(pullParser.nextText());
                            }
                        }else if(pullParser.getName().equalsIgnoreCase("enclosure")){
                            if(isInsideItem){
                                String link = pullParser.getAttributeValue(null,"url");
                                Log.d(link,"link");
                                image.add(link);
                            }
                        }else if(pullParser.getName().equalsIgnoreCase("link")){
                            if(isInsideItem){
                                links.add(pullParser.nextText());
                            }
                        }
                    }else if(eventType == XmlPullParser.END_TAG&&pullParser.getName().equalsIgnoreCase("item")){
                        isInsideItem = false;
                    }
                    //increment
                    eventType = pullParser.next();
                }


            }catch (MalformedURLException e){
                exception = e;
            }catch (XmlPullParserException e){
                exception = e;
            }catch (IOException e){
                exception = e;
            }

            return exception;
        }

        @Override
        protected void onPostExecute(Exception s) {
            super.onPostExecute(s);
            Adapter adapter = new Adapter(MainActivity.this,arrayOfList);

            for(int i=0;i<titles.size();i++){
                ListModel newFeed = new ListModel(titles.get(i),image.get(i),links.get(i));
                adapter.add(newFeed);
            }

            lvRss.setAdapter(adapter);
        }
    }
}
