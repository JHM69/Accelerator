package org.jhm69.battle_of_quiz.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jhm69.battle_of_quiz.R;
import org.jhm69.battle_of_quiz.ui.activities.MainActivity;

import java.util.Objects;

@SuppressWarnings("RedundantSuppression")
@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class MyFireBaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        int icon = R.drawable.ic_logo_icon;
        String name = "Battle of Quiz";
        String type = remoteMessage.getData().get("type");
        String title = remoteMessage.getData().get("username");
        String message = remoteMessage.getData().get("message");

        switch (Objects.requireNonNull(type)) {
            case "like":
                name = "Lights";
                icon = (R.drawable.ic_batti);
                break;
            case "comment":
                name = "Comments";
                icon = (R.drawable.ic_comment_blue);
                break;
            case "friend_req":
                name = "Friend Requests";
                icon = (R.drawable.ic_person_add_yellow_24dp);
                break;
            case "accept_friend_req":
                name = "Accepted Notification";
                icon = (R.drawable.ic_person_green_24dp);
                break;
            case "play":
                name = "Invites to Play";
                break;
            case "play_result":
                name = "Battle Results";
                icon = (R.drawable.ic_logo_icon);
                break;
            case "post":
                icon = (R.drawable.ic_image_post_black);
                break;
            default:
                icon = (R.drawable.ic_logo_icon);
                break;
        }

        showNotification(icon, title, message, type, name);
    }


    void showNotification(int icon, String title, String message, String id, String name) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(id,
                    name,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Battle of Quiz notifications. Add friends, Lighting posts, Playing battle quiz etc");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), id)
                .setVibrate(new long[]{0, 100})
                .setPriority(Notification.PRIORITY_MAX)
                .setLights(Color.BLUE, 3000, 3000)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(icon)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_logo_icon));
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
    }


}
