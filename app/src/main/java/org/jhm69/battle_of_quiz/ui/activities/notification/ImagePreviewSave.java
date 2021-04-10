package org.jhm69.battle_of_quiz.ui.activities.notification;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

import org.jhm69.battle_of_quiz.R;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class ImagePreviewSave extends AppCompatActivity {


    String intent_URL;
    final ArrayList<Long> list = new ArrayList<>();


    public final BroadcastReceiver onComplete = new BroadcastReceiver() {

        public void onReceive(Context ctxt, Intent intent) {

            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            list.remove(referenceId);
            if (list.isEmpty()) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                        ImagePreviewSave.this, "other_channel");

                android.app.Notification notification;
                notification = mBuilder
                        .setAutoCancel(true)
                        .setContentTitle("Battle of Quiz")
                        .setColorized(true)
                        .setColor(Color.parseColor("#2591FC"))
                        .setSmallIcon(R.drawable.ic_file_download_accent_24dp)
                        .setContentText("Image saved at Downloads/battle_of_quiz/")
                        .build();

                notificationManager.notify(0, notification);
                Toasty.success(ctxt, "Image saved at Downloads/battle_of_quiz/", Toasty.LENGTH_LONG, true).show();
            }
        }

    };
    private long refid;
    private DownloadManager downloadManager;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onComplete);
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransitionExit();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransitionEnter();
    }

    /**
     * Overrides the pending Activity transition by performing the "Enter" animation.
     */
    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_image_preview_save);

        intent_URL = getIntent().getStringExtra("url");

        registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        ImageView photoView = findViewById(R.id.photo_view);
        if (getSharedPreferences("theme", MODE_PRIVATE).getBoolean("dark", false)) {
            Glide.with(this)
                    .setDefaultRequestOptions(new RequestOptions().placeholder(getResources().getDrawable(R.drawable.placeholder)))
                    .load(intent_URL)
                    .into(photoView);
        } else {
            Glide.with(this)
                    .setDefaultRequestOptions(new RequestOptions().placeholder(getResources().getDrawable(R.drawable.placeholder2)))
                    .load(intent_URL)
                    .into(photoView);
        }
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.statusBar));

    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void downloadImage(final String ImageURI) {

        new MaterialDialog.Builder(this)
                .title("Save Image")
                .content("Do you want to save this image?")
                .positiveText("YES")
                .negativeText("NO")
                .onPositive((dialog, which) -> {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(ImageURI));
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                    request.setAllowedOverRoaming(true);
                    request.setTitle("Battle of Quiz");
                    request.setDescription("Downloading image...");
                    request.setVisibleInDownloadsUi(true);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/battle_of_quiz Images/" + "/dt_" + System.currentTimeMillis() + ".jpeg");
                    refid = downloadManager.enqueue(request);
                    list.add(refid);


                }).onNegative((dialog, which) -> dialog.dismiss()).show();

    }

    public void saveImage(View v) {
        Dexter.withActivity(this)
                .withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if (isOnline()) {
                            downloadImage(intent_URL);
                        } else {
                            Toasty.error(ImagePreviewSave.this, "No internet connection", Toasty.LENGTH_SHORT, true).show();
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            DialogOnDeniedPermissionListener.Builder
                                    .withContext(ImagePreviewSave.this)
                                    .withTitle("Storage permission")
                                    .withMessage("Storage permission is needed for downloading images.")
                                    .withButtonText(android.R.string.ok)
                                    .build();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

    }
}
