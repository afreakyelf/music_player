package com.olaapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.olaapp.payment.CHECKSUM;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private AlbumAdapter albumAdapter;
    RequestQueue queue;
    LinearLayoutManager lLayout;
    private ArrayList<Album> list=new ArrayList<>();
    static Boolean premiumBought = false;
    Button buyPre;
    static int showList = 0 ;
    Group group;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = findViewById(R.id.progressBar);

        group = findViewById(R.id.group);
        TextView noin = findViewById(R.id.noInternet);
        if(!isOnline()){
            noin.setVisibility(View.VISIBLE);
            group.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }

        buyPre = findViewById(R.id.unlockPremium);


        if(getIntent().getExtras()!=null) {
            boolean prem = getIntent().getExtras().getBoolean("premium");
            if(prem){
                premiumBought = true;
            }
        }

            if(premiumBought){
                buyPre.setVisibility(View.GONE);
            }else {
                buyPre.setVisibility(View.VISIBLE);
            }

        buyPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               makePayment();
            }
        });

        recyclerView= findViewById(R.id.recyclerview);
        list=new ArrayList<>();
        albumAdapter=new AlbumAdapter(this,list);
        lLayout = new LinearLayoutManager(MainActivity.this
        );
        recyclerView.setLayoutManager(lLayout);
        recyclerView.setAdapter(albumAdapter);

        queue= AppController.getInstance().getRequestQueue();

        preparealbums();

    }

    private void makePayment() {

            Intent intent = new Intent(MainActivity.this, CHECKSUM.class);
            intent.putExtra("orderid", String.valueOf(System.currentTimeMillis()));
            intent.putExtra("custid", String.valueOf(12523));
            startActivity(intent);

        if (ContextCompat.checkSelfPermission(
                MainActivity.this,
        Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
        }

    }

    public  void preparealbums()
    {
        String url="http://starlord.hackerearth.com/studio";


        JsonArrayRequest movieReq = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>()
                {
                    @Override
                    public void onResponse(JSONArray response)
                    {
                        Log.d(TAG, response.toString());

                        int sizeee ;
                        if(!premiumBought){
                            sizeee=7;
                        }else {
                            sizeee=response.length();
                        }

                        for (int i = 0; i < sizeee; i++) {
                            try
                            {
                                JSONObject obj = response.getJSONObject(i);
                                Album movie = new Album(obj.getString("song"),obj.getString("url"),
                                        obj.getString("artists"),obj.getString("cover_image"));
                                list.add(movie);

                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        recyclerView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        // notifying list adapter about data changes
                        // so that it renders the list view with updated data
                        albumAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());


            }
        });

        // Adding request to request queue
        queue.add(movieReq);

    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        MenuItem menuItem=menu.findItem(R.id.action_search);
        SearchView searchView= (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setQueryHint("Search Song name");
        searchView.setOnQueryTextListener(this);



        return true;
    }




    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        if(premiumBought){
            buyPre.setVisibility(View.GONE);
        }else {
            buyPre.setVisibility(View.VISIBLE);
        }
        
        newText=newText.toLowerCase();
        ArrayList<Album> arrayListp=new ArrayList<>();
        for(Album album:list){
            String name=album.getSong().toLowerCase();
            if(name.contains(newText))
                arrayListp.add(album);
        }
        albumAdapter.setfilter(arrayListp);
        return true;
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }


}
