package com.olaapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.olaapp.MainActivity.showList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.MyViewHolder> implements ActivityCompat.OnRequestPermissionsResultCallback{
    private Context context;
    private static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;
    private String name,song;
    private ArrayList<Album> albumList;
    private ImageButton buttonViewOption;

    class MyViewHolder extends RecyclerView.ViewHolder  {
          TextView gl,g2;
          ImageView imageView;
         LinearLayout cardView;

          MyViewHolder(View itemView) {
          super(itemView);

          cardView = itemView.findViewById(R.id.lyt_parent);
          gl = itemView.findViewById(R.id.goi);
          g2 = itemView.findViewById(R.id.goi2);
          buttonViewOption = itemView.findViewById(R.id.textViewOptions);
         imageView = itemView.findViewById(R.id.image);
      }

  }

    AlbumAdapter(Context context, ArrayList<Album> albumList){
        this.context=context;
        this.albumList=albumList;
    }

    @NonNull
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewtype){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_card,parent,false);

        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final AlbumAdapter.MyViewHolder holder, final int position) {
       final Album album = albumList.get(position);
       ImageLoader imageLoader = AppController.getInstance().getImageLoader();
        permissionStatus = context.getSharedPreferences("permissionStatus",MODE_PRIVATE);
        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        holder.gl.setText(album.getSong());
        holder.g2.setText(album.getArtists());
        name=album.getUrl();
        song=album.getSong();
        Glide.with(context).load(album.getCover_image()).into(holder.imageView);


        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Album album2 = albumList.get(position);
                Intent intent=new Intent(context,Main2Activity.class);
                intent.putExtra("url",album2.getUrl());
                intent.putExtra("image",album2.getCover_image());
                intent.putExtra("song",album2.getSong());
                intent.putExtra("artists",album2.getArtists());
                context.startActivity(intent);
            }
        });

        buttonViewOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //creating a popup menu
                PopupMenu popup = new PopupMenu(context, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.options_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {

                            case R.id.menu2:
                                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                        //Show Information about why you need the permission
                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                        builder.setTitle("Need Storage Permission");
                                        builder.setMessage("This app needs storage permission.");
                                        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                                            }
                                        });
                                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                        builder.show();
                                    } else if (permissionStatus.getBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE,false)) {
                                        //Previously Permission Request was cancelled with 'Dont Ask Again',
                                        // Redirect to Settings after showing Information about why you need the permission
                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                        builder.setTitle("Need Storage Permission");
                                        builder.setMessage("This app needs storage permission.");
                                        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                                sentToSettings = true;
                                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                                                intent.setData(uri);
                                                ((Activity)context).startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                                                Toast.makeText(context, "Go to Permissions to Grant Storage", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                        builder.show();
                                    }
                                    else {
                                        //just request the permission
                                        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                                    }


                                    SharedPreferences.Editor editor = permissionStatus.edit();
                                    editor.putBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE,true);
                                    editor.commit();




                                } else {
                                    //You already have the permission, just go ahead.
                                    proceedAfterPermission();
                                }

                              break;

                            case R.id.menu3:
                                try {
                                    Intent i = new Intent(Intent.ACTION_SEND);
                                    i.setType("text/plain");

                                    i.putExtra(Intent.EXTRA_TEXT, album.getUrl());
                                    context.startActivity(Intent.createChooser(i, "Choose one"));
                                } catch(Exception e) {
                                    //e.toString();
                                }

                                break;
                    }

                        return false;
                    }
                });
                //displaying the popup
                popup.show();

            }
        });



    }

    private void proceedAfterPermission() {
        //We've got the permission, now we can proceed further
        Toast.makeText(context, "Downloading", Toast.LENGTH_SHORT).show();
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(name);
        String nameoffile= URLUtil.guessFileName(name,null, MimeTypeMap.getFileExtensionFromUrl(name));
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(song);
        request.setDescription("Thanks for Downloading");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,nameoffile);
        DownloadManager manager=(DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == EXTERNAL_STORAGE_PERMISSION_CONSTANT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //The External Storage Write Permission is granted to you... Continue your left job...
                proceedAfterPermission();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //Show Information about why you need the permission
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Need Storage Permission");
                    builder.setMessage("This app needs storage permission");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();


                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);


                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else {
                    Toast.makeText(context,"Unable to get Permission",Toast.LENGTH_LONG).show();
                }
            }
        }
    }




    @Override
    public int getItemCount() {
        return albumList.size();
    }

    void setfilter(ArrayList<Album> newlist){
    albumList=new ArrayList<>();
    albumList.addAll(newlist);
    notifyDataSetChanged();
}

}

