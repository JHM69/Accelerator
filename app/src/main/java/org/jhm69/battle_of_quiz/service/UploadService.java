package org.jhm69.battle_of_quiz.service;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jhm69.battle_of_quiz.models.Images;
import org.jhm69.battle_of_quiz.repository.UserRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;

import static org.jhm69.battle_of_quiz.ui.activities.MainActivity.userId;
import static org.jhm69.battle_of_quiz.ui.activities.post.PostText.getSaltString;

public class UploadService extends Service {

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
    private static final String TAG_FOREGROUND_SERVICE = UploadService.class.getSimpleName();
    private int count;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (Objects.equals(action, ACTION_START_FOREGROUND_SERVICE)) {
                List<Images> imagesList = intent.getParcelableArrayListExtra("imagesList");
                int notification_id = intent.getIntExtra("notification_id", 2);
                String current_id = intent.getStringExtra("current_id");
                String description = intent.getStringExtra("description");
                String tag = intent.getStringExtra("tag");
                ArrayList<String> uploadedImagesUrl = intent.getStringArrayListExtra("uploadedImagesUrl");
                count = intent.getIntExtra("count", 0);


                Uri myUri = intent.getParcelableExtra("image");
                String userid = intent.getStringExtra("userid");
                String fuserId = intent.getStringExtra("fuser");
                Log.d("========", String.valueOf(myUri));

                if (fuserId != null && userid != null) {
                    Toast.makeText(this, "Sending Images", Toast.LENGTH_SHORT).show();
                    // sendMessage((int) System.currentTimeMillis(), fuserId, userid, myUri, userid, fuserId);
                } else if (tag != null && uploadedImagesUrl != null) {
                    Toast.makeText(this, "posting", Toast.LENGTH_SHORT).show();
                    uploadImages(notification_id, 0, imagesList, current_id, description, uploadedImagesUrl, tag);
                } else {
                    Toast.makeText(this, "not sending", Toast.LENGTH_SHORT).show();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void stopForegroundService() {
        Log.d(TAG_FOREGROUND_SERVICE, "Stop foreground service.");
        stopForeground(true);
        stopSelf();
    }

    private void notifyProgress(int id, String title, String message, Context context, int progress, boolean indeterminate) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "other_channel");
        // Create notification default intent.
        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        builder.setSmallIcon(android.R.drawable.stat_sys_upload)
                .setContentTitle(title)
                .setContentText(message)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setTicker(message)
                .setChannelId("play")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(100, progress, indeterminate)
                .setVibrate(new long[100]);
        startForeground(id, builder.build());
    }

    private void uploadImages(final int notification_id, final int index, final List<Images> imagesList, String currentUser_id, String description, ArrayList<String> uploadedImagesUrl, String tag) {

        int img_count = index + 1;
        Uri imageUri;
        try {
            File compressedFile = new Compressor(this)
                    .setQuality(60)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .compressToFile(new File(imagesList.get(index).getPath()));
            imageUri = Uri.fromFile(compressedFile);
        } catch (Exception e) {
            e.printStackTrace();
            imageUri = Uri.fromFile(new File(imagesList.get(index).getPath()));
        }

        final StorageReference fileToUpload = FirebaseStorage.getInstance().getReference().child("post_images").child("boq" + System.currentTimeMillis() + "_" + imagesList.get(index).getName());
        fileToUpload.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileToUpload.getDownloadUrl()
                        .addOnSuccessListener(uri -> {

                            uploadedImagesUrl.add(uri.toString());
                            int next_index = index + 1;
                            try {
                                if (!TextUtils.isEmpty(imagesList.get(index + 1).getOg_path())) {
                                    uploadImages(notification_id, next_index, imagesList, currentUser_id, description, uploadedImagesUrl, tag);
                                } else {
                                    uploadPost(notification_id, currentUser_id, description, uploadedImagesUrl, tag);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                uploadPost(notification_id, currentUser_id, description, uploadedImagesUrl, tag);
                            }

                        })
                        .addOnFailureListener(Throwable::printStackTrace))
                .addOnFailureListener(Throwable::printStackTrace)
                .addOnProgressListener(taskSnapshot -> {

                    if (count == 1) {
                        String title = "Uploading " + img_count + "/" + imagesList.size() + " images...";
                        int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                        notifyProgress(notification_id
                                ,
                                title
                                , progress + "%"
                                , getApplicationContext()
                                ,
                                progress
                                , false);
                    } else if (count > 1) {

                        notifyProgress(notification_id
                                ,
                                "ত্বারক"
                                , "Uploading " + count + " posts"
                                , getApplicationContext()
                                ,
                                0
                                , true);

                    }

                });

    }

    private void uploadPost(int notification_id, String currentUser_id, String description, ArrayList<String> uploadedImagesUrl, String tag) {

        if (!uploadedImagesUrl.isEmpty()) {

            if (count == 1) {
                notifyProgress(notification_id
                        ,
                        "ত্বারক"
                        , "Uploading post.."
                        , getApplicationContext()
                        ,
                        0
                        , true);
            }


            FirebaseFirestore.getInstance().collection("Users")
                    .document(currentUser_id)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {

                        Map<String, Object> postMap = new HashMap<>();

                        postMap.put("userId", documentSnapshot.getString("id"));
                        postMap.put("username", documentSnapshot.getString("username"));
                        postMap.put("institute", documentSnapshot.getString("institute"));
                        postMap.put("dept", documentSnapshot.getString("dept"));
                        postMap.put("name", documentSnapshot.getString("name"));
                        postMap.put("userimage", documentSnapshot.getString("image"));
                        postMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
                        postMap.put("image_count", uploadedImagesUrl.size());
                        try {
                            postMap.put("image_url_0", uploadedImagesUrl.get(0));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_1", uploadedImagesUrl.get(1));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_2", uploadedImagesUrl.get(2));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_3", uploadedImagesUrl.get(3));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_4", uploadedImagesUrl.get(4));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_5", uploadedImagesUrl.get(5));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_6", uploadedImagesUrl.get(6));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String idll = getSaltString();
                        postMap.put("description", description);
                        postMap.put("tag", tag);
                        postMap.put("liked_count", 0);
                        postMap.put("comment_count", 0);
                        postMap.put("color", "0");
                        postMap.put("postId", idll);
                        Map<String, Object> postMapFinal = new HashMap<>();
                        postMapFinal.put(getSaltString(), postMap);

                        FirebaseFirestore.getInstance().collection("PendingPosts")
                                .document(idll)
                                .set(postMap)
                                .addOnSuccessListener(documentReference -> {
                                    getSharedPreferences("uploadservice", MODE_PRIVATE)
                                            .edit()
                                            .putInt("count", --count).apply();
                                    // Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.study_forum))
                                    Toasty.success(getApplicationContext(), "Post added for Review.", Toasty.LENGTH_SHORT, true).show();
                                    stopForegroundService();

                                })
                                .addOnFailureListener(e -> {
                                    Toasty.error(getApplicationContext(), "Error :" + e.getMessage(), Toasty.LENGTH_SHORT, true).show();
                                    stopForegroundService();
                                    e.printStackTrace();
                                });

                    }).addOnFailureListener(e -> {
                Toasty.error(getApplicationContext(), "Error :" + e.getMessage(), Toasty.LENGTH_SHORT, true).show();
                stopForegroundService();
                e.printStackTrace();
            });

        } else {
            Toasty.info(this, "No image has been uploaded, Please wait or try again", Toasty.LENGTH_SHORT, true).show();
            stopForegroundService();
        }
    }

    /*  public void sendMessage(int notification_id, String sender, final String receiver, Uri image, String userid, String fuser) {
          notifyProgress(notification_id
                  ,
                  "ত্বারক"
                  , "Sending attachment.."
                  , getApplicationContext()
                  , 0
                  , true);

          final StorageReference fileToUpload = FirebaseStorage.getInstance().getReference().child("message_images").child("battle_of_quiz_" + System.currentTimeMillis() + ".png");
          fileToUpload.putFile(image).addOnSuccessListener(taskSnapshot -> fileToUpload.getDownloadUrl()
                  .addOnSuccessListener(uri -> {
                      String imsge = uri.toString();
                      DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

                      HashMap<String, Object> hashMap = new HashMap<>();
                      hashMap.put("sender", sender);
                      hashMap.put("receiver", receiver);
                      hashMap.put("message", "attachment");
                      hashMap.put("image", imsge);
                      hashMap.put("isseen", false);
                      hashMap.put("timestamp", System.currentTimeMillis());

                      reference.child("Chats").push().setValue(hashMap);


                      // add user to chat fragment
                      final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                              .child(fuser)
                              .child(userid);

                      chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                          @Override
                          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                              if (!dataSnapshot.exists()) {
                                  chatRef.child("id").setValue(userid);
                              }
                          }

                          @Override
                          public void onCancelled(@NonNull DatabaseError databaseError) {

                          }
                      });

                      final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                              .child(userid)
                              .child(fuser);
                      chatRefReceiver.child("id").setValue(fuser);

                      reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser);
                      reference.addValueEventListener(new ValueEventListener() {
                          @Override
                          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                              User user = dataSnapshot.getValue(User.class);
                              sendNotifiaction(receiver, Objects.requireNonNull(user).getName(), fuser, userid);
                          }

                          @Override
                          public void onCancelled(@NonNull DatabaseError databaseError) {

                          }
                      });
                  }));

      }

  */
    private void updateXP() {
        FirebaseFirestore.getInstance().collection("Users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    int scoreOld = documentSnapshot.getLong("reward").intValue();
                    int newScore = scoreOld + (-3);
                    HashMap<String, Object> scoreMap = new HashMap<>();
                    scoreMap.put("reward", newScore);
                    FirebaseFirestore.getInstance()
                            .collection("Users")
                            .document(userId)
                            .update(scoreMap).addOnSuccessListener(aVoid -> {
                                new UserRepository((Application) getApplicationContext()).updateXp(-3);
                                //Toast.makeText(, "", Toast.LENGTH_SHORT).show();
                                //Toasty.success(getApplicationContext(), "Congratulations, You have got 10 reward", Toasty.LENGTH_SHORT, true);
                            });
                });

    }

}
