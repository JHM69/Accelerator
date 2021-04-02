package org.jhm69.battle_of_quiz.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jhm69.battle_of_quiz.R;
import org.jhm69.battle_of_quiz.models.Notification;
import org.jhm69.battle_of_quiz.ui.activities.friends.FriendProfile;
import org.jhm69.battle_of_quiz.ui.activities.post.SinglePostView;
import org.jhm69.battle_of_quiz.ui.activities.quiz.QuizBattle;
import org.jhm69.battle_of_quiz.ui.activities.quiz.ResultActivity;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

import static org.jhm69.battle_of_quiz.ui.activities.MainActivity.userId;

/**
 * Created by Jahangir .
 */

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
    private final List<Notification> notificationsList;
    private final Context context;


    //     *  -1 -> JUST_STARTED -> Result
//   *  -2 -> OFFLINE_STARTED -> Play
//   *  -3 ->  -> Play
//   *  -4 -> IN_STARTED ->/**/ Play
//   *  -5 -5 COMPLETED -> Result
//
    int lastPosition = -1;

    public NotificationsAdapter(List<Notification> notificationsList, Context context) {
        this.notificationsList = notificationsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Animation animation = AnimationUtils.loadAnimation(context,
                (position > lastPosition) ? R.anim.up_from_bottom
                        : R.anim.down_from_top);
        holder.itemView.startAnimation(animation);
        lastPosition = position;

        Notification notification = notificationsList.get(position);

        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_logo_icon))
                .load(notification.getImage())
                .into(holder.image);

        holder.title.setText(notification.getUsername());
        holder.body.setText(notification.getMessage());
        holder.timestamp.setText(TimeAgo.using(Long.parseLong(notification.getTimestamp())));

        if (notification.getType().equals("like")) {
            holder.type_image.setImageResource(R.drawable.ic_batti);
        } else if (notification.getType().equals("comment")) {
            holder.type_image.setImageResource(R.drawable.ic_comment_blue);
        } else if (notification.getType().equals("friend_req")) {
            holder.type_image.setImageResource(R.drawable.ic_person_add_yellow_24dp);
        } else if (notification.getType().equals("accept_friend_req")) {
            holder.type_image.setImageResource(R.drawable.ic_person_green_24dp);
        } else if (notification.getType().equals("play")) {
            holder.type_image.setImageResource(R.drawable.ic_flash_on_black_24dp);
        } else if (notification.getType().equals("play_result")) {
            holder.type_image.setImageResource(R.drawable.ic_baseline_multiline_chart_24);
        } else if (notification.getType().equals("post")) {
            holder.type_image.setImageResource(R.drawable.ic_image_post_black);
        } else {
            holder.type_image.setImageResource(R.drawable.ic_logo_icon);
        }

        holder.itemView.setOnClickListener(v -> {
            switch (notification.getType()) {
                case "like":
                case "comment":
                case "post":
                    context.startActivity(new Intent(context, SinglePostView.class).putExtra("post_id", notification.getAction_id()));
                    break;
                case "friend_req":
                case "accept_friend_req":
                    FriendProfile.startActivity(context, notification.getAction_id());
                    break;
                case "play":
                    context.startActivity(new Intent(context, QuizBattle.class).putExtra("battleId", notification.getAction_id()));
                    break;
                case "play_result":
                    context.startActivity(new Intent(context, ResultActivity.class).putExtra("resultId", notification.getAction_id()));
                    break;
            }
        });

        holder.itemView.setOnLongClickListener(v -> {

            new MaterialDialog.Builder(context)
                    .title("Delete notification")
                    .content("Are you sure do you want to delete this notification?")
                    .positiveText("Yes")
                    .negativeText("No")
                    .onPositive((dialog, which) -> {
                        FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(userId)
                                .collection("Info_Notifications")
                                .document(notificationsList.get(holder.getAdapterPosition()).documentId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                })
                                .addOnFailureListener(Throwable::printStackTrace);

                        notificationsList.remove(holder.getAdapterPosition());
                        Toasty.success(context, "Notification removed", Toasty.LENGTH_SHORT, true).show();
                        notifyDataSetChanged();

                    })
                    .show();

            return true;
        });

    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final CircleImageView image;
        private final ImageView type_image;
        private final TextView title;
        private final TextView body;
        private final TextView timestamp;

        public ViewHolder(View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image);
            type_image = itemView.findViewById(R.id.type_image);
            title = itemView.findViewById(R.id.title);
            body = itemView.findViewById(R.id.body);
            timestamp = itemView.findViewById(R.id.timestamp);

        }
    }


}
