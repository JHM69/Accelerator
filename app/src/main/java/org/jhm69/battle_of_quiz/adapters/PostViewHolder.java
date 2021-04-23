package org.jhm69.battle_of_quiz.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import org.jhm69.battle_of_quiz.R;
import org.jhm69.battle_of_quiz.notification.APIService;
import org.jhm69.battle_of_quiz.notification.Client;
import org.jhm69.battle_of_quiz.notification.MyResponse;
import org.jhm69.battle_of_quiz.notification.NotificationSender;
import org.jhm69.battle_of_quiz.models.MultipleImage;
import org.jhm69.battle_of_quiz.models.Notification;
import org.jhm69.battle_of_quiz.models.Post;
import org.jhm69.battle_of_quiz.ui.activities.friends.FriendProfile;
import org.jhm69.battle_of_quiz.ui.activities.post.CommentsActivity;
import org.jhm69.battle_of_quiz.ui.activities.post.WhoLikedActivity;
import org.jhm69.battle_of_quiz.ui.fragment.Home;
import org.jhm69.battle_of_quiz.utils.MathView;
import org.jhm69.battle_of_quiz.viewmodel.PostViewModel;
import org.jhm69.battle_of_quiz.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.jhm69.battle_of_quiz.ui.activities.MainActivity.userId;

@SuppressWarnings("ConstantConditions")
public class PostViewHolder extends RecyclerView.ViewHolder {
    private final FirebaseFirestore mFirestore;
    private final FirebaseUser mCurrentUser;
    private final CircleImageView user_image;
    private final TextView user_name;
    private final TextView timestamp;
    private final TextView institute_dept;
    private final MathView post_desc;
    @SuppressLint("NewApi")
    private final Set<String> ADMIN_UID_LIST = Set.of(
            "0h9MvJiFvFWRBiOoHzUcGlqJe2m2", "eSW24hxmW6YmbaInd2OlrsWx0Rw1"
    );
    private final MaterialFavoriteButton sav_button;
    private final MaterialFavoriteButton like_btn;
    private final MaterialFavoriteButton stat_btn;
    private final FrameLayout pager_layout;
    private final RelativeLayout indicator_holder;
    private final ImageView delete;
    private final ViewPager pager;
    private final DotsIndicator indicator2;
    int position;
    final Button see_more;
    PostViewModel postViewModel;
    private Context context;
    private boolean isOwner;
    private Activity activity;
    private boolean forComment;
    private View mView;

    public PostViewHolder(@NonNull View holder) {
        super(holder);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        user_image = holder.findViewById(R.id.post_user_image);
        like_btn = holder.findViewById(R.id.like_button);
        stat_btn = holder.findViewById(R.id.stat_button);
        user_name = holder.findViewById(R.id.post_username);
        institute_dept = holder.findViewById(R.id.dept_institute);
        timestamp = holder.findViewById(R.id.post_timestamp);
        post_desc = holder.findViewById(R.id.post_desc);
        pager = holder.findViewById(R.id.pager);
        pager_layout = holder.findViewById(R.id.pager_layout);
        see_more = holder.findViewById(R.id.share_button);
        delete = holder.findViewById(R.id.delete_button);
        sav_button = holder.findViewById(R.id.save_button);
        indicator2 = holder.findViewById(R.id.indicator);
        indicator_holder = holder.findViewById(R.id.indicator_holder);
    }

    public static void updateLike(boolean like, String postId) {
        try {
            FirebaseFirestore.getInstance().collection("Posts")
                    .document(postId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        try {
                            int likeNew = 0;
                            int likeOld = Objects.requireNonNull(documentSnapshot.getLong("like_count")).intValue();
                            if (like) likeNew = likeOld++;
                            else likeNew = likeOld--;
                            HashMap<String, Object> scoreMap = new HashMap<>();
                            scoreMap.put("liked_count", likeNew);
                            FirebaseFirestore.getInstance()
                                    .collection("Posts")
                                    .document(postId)
                                    .update(scoreMap).addOnSuccessListener(aVoid -> {

                                    });
                        } catch (NullPointerException ignored) {

                        }
                    });
        } catch (NullPointerException ignored) {

        }
    }

    @SuppressLint({"ResourceType", "SetTextI18n"})
    public void bind(Post post, final PostViewHolder holder, int position, BottomSheetDialog mmBottomSheetDialog, View statsheetView, boolean approved) {
        Log.d("Approved", approved+" ");
        context = Home.context;
        this.position = position;
        getLikeandFav(post);
        user_name.setText(post.getName());
        user_name.setOnClickListener(v -> FriendProfile.startActivity(context, post.getUserId()));
        String dept = post.getDept();
        String institute = post.getInstitute();
        if (!dept.equals("") && !institute.equals("")) {
            holder.institute_dept.setText(dept + ", " + institute);
        } else if (institute.equals("")) {
            holder.institute_dept.setText(dept);
        } else if (dept.equals("")) {
            holder.institute_dept.setText(institute);
        }


        //post_desc.setDisplayText(post.getDescription());
        stat_btn.setOnFavoriteChangeListener((buttonView, favorite) -> {

            mmBottomSheetDialog.show();

            final ProgressBar pbar = statsheetView.findViewById(R.id.pbar);
            final LinearLayout main = statsheetView.findViewById(R.id.main);
            final LinearLayout loveL = statsheetView.findViewById(R.id.love);
            final LinearLayout saveL = statsheetView.findViewById(R.id.save);
            final TextView smile = statsheetView.findViewById(R.id.smiles);
            final TextView comments = statsheetView.findViewById(R.id.comments);
            final TextView save = statsheetView.findViewById(R.id.saves);

            pbar.setVisibility(View.VISIBLE);
            main.setVisibility(View.GONE);
            FirebaseFirestore.getInstance().collection("Posts")
                    .document(post.getPostId())
                    .collection("Liked_Users")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {

                        smile.setText(String.format(Home.context.getString(R.string.s_peopl_have_smiled_for_this_post), queryDocumentSnapshots.size()));
                        loveL.setOnClickListener(view -> view.getContext().startActivity(new Intent(view.getContext(), WhoLikedActivity.class).putExtra("postId", post.getPostId()).putExtra("type", "Liked_Users")));

                        FirebaseFirestore.getInstance().collection("Posts")
                                .document(post.getPostId())
                                .collection("Saved_Users")
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    save.setText(String.format(Home.context.getString(R.string.s_peopl_have_saved_this_post), queryDocumentSnapshots1.size()));
                                    saveL.setOnClickListener(view -> view.getContext().startActivity(new Intent(view.getContext(), WhoLikedActivity.class).putExtra("postId", post.getPostId()).putExtra("type", "Saved_Users")));
                                    pbar.setVisibility(View.GONE);
                                    main.setVisibility(View.VISIBLE);

                                })
                                .addOnFailureListener(Throwable::printStackTrace);

                        FirebaseFirestore.getInstance().collection("Posts")
                                .document(post.getPostId())
                                .collection("Comments")
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    comments.setText(String.format(Home.context.getString(R.string.s_peopl_have_commented_this_post), queryDocumentSnapshots1.size()));
                                    pbar.setVisibility(View.GONE);
                                    main.setVisibility(View.VISIBLE);

                                })
                                .addOnFailureListener(Throwable::printStackTrace);

                    })
                    .addOnFailureListener(Throwable::printStackTrace);

        });
        Glide.with(Home.context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.placeholder))
                .load(post.getUserimage())
                .into(holder.user_image);

        String timeAgo = TimeAgo.using(Long.parseLong(post.getTimestamp()));
        timestamp.setText(timeAgo);
        try {
            if (post.getImage_count() == 0) {
                pager_layout.setVisibility(View.GONE);
                String descc = post.getDescription();
                post_desc.setDisplayText((descc.length() > 297) ? descc.substring(0, 300) + "..." : descc);
            }  else if (post.getImage_count() == 1) {
                pager_layout.setVisibility(View.VISIBLE);
                ArrayList<MultipleImage> multipleImages = new ArrayList<>();
                PostPhotosAdapter photosAdapter = new PostPhotosAdapter(Home.context, activity, multipleImages, false, post.getPostId(), like_btn, post.getUserId(), approved);
                setUrls(multipleImages, photosAdapter, post);
                pager.setAdapter(photosAdapter);
                indicator_holder.setVisibility(View.GONE);
                photosAdapter.notifyDataSetChanged();
                pager_layout.setVisibility(View.VISIBLE);
                post_desc.setVisibility(View.VISIBLE);
                String desc = post.getDescription();
                post_desc.setDisplayText((desc.length() > 297) ? desc.substring(0, 300) + "..." : desc);
            } else {
                ArrayList<MultipleImage> multipleImages = new ArrayList<>();
                PostPhotosAdapter photosAdapter = new PostPhotosAdapter(Home.context, activity, multipleImages, false, post.getPostId(), like_btn, post.getUserId(), approved);
                setUrls(multipleImages, photosAdapter, post);

                pager.setAdapter(photosAdapter);
                photosAdapter.notifyDataSetChanged();
                indicator2.setDotsClickable(true);
                indicator2.setViewPager(pager);

                final Handler handler = new Handler();
                final Runnable slide = () -> {
                    if (pager.getCurrentItem() == multipleImages.size()) {
                        pager.setCurrentItem(0, true);
                        return;
                    }
                    pager.setCurrentItem(pager.getCurrentItem() + 1, true);
                };
                Timer slideTimer = new Timer();
                slideTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(slide);
                    }
                }, 100000, 100000);


                pager_layout.setVisibility(View.VISIBLE);
                indicator_holder.setVisibility(View.VISIBLE);
                // holder.post_text.setVisibility(View.GONE);

            }
        } catch (NullPointerException ignored) {

        }
        try {
            setupViews(post);
        } catch (Exception ignored) {
        }
        if (post.getUserId().equals(mCurrentUser.getUid()) || ADMIN_UID_LIST.contains(mCurrentUser.getUid())) {
            isOwner = true;
            delete.setVisibility(View.VISIBLE);
            delete.setOnClickListener(v -> new MaterialDialog.Builder(Home.context)
                    .title("Delete post")
                    .content("Are you sure do you want to delete this post?")
                    .positiveText("Yes")
                    .negativeText("No")
                    .onPositive((dialog, which) -> {

                        final ProgressDialog pdialog = new ProgressDialog(Home.context);
                        pdialog.setMessage("Please wait...");
                        pdialog.setIndeterminate(true);
                        pdialog.setCancelable(false);
                        pdialog.setCanceledOnTouchOutside(false);
                        pdialog.show();

                        dialog.dismiss();
                        FirebaseFirestore.getInstance().collection("Posts")
                                .document(post.getPostId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {


                                    if (!TextUtils.isEmpty(post.getImage_url_0())) {
                                        StorageReference img = FirebaseStorage.getInstance()
                                                .getReferenceFromUrl(post.getImage_url_0());
                                        img.delete().addOnSuccessListener(aVoid1 ->
                                        {
                                            pdialog.dismiss();
                                            Log.i("Post Image", "deleted");
                                        })
                                                .addOnFailureListener(e -> Log.e("Post Image", e.getLocalizedMessage()));
                                    }

                                    pdialog.show();
                                    if (!TextUtils.isEmpty(post.getImage_url_1())) {
                                        StorageReference img = FirebaseStorage.getInstance()
                                                .getReferenceFromUrl(post.getImage_url_1());
                                        img.delete().addOnSuccessListener(aVoid12 -> {
                                            pdialog.dismiss();
                                            Log.i("Post Image", "deleted");
                                        })
                                                .addOnFailureListener(e -> Log.e("Post Image", e.getLocalizedMessage()));
                                    }

                                    pdialog.show();
                                    if (!TextUtils.isEmpty(post.getImage_url_2())) {
                                        StorageReference img = FirebaseStorage.getInstance()
                                                .getReferenceFromUrl(post.getImage_url_2());
                                        img.delete().addOnSuccessListener(aVoid13 -> {
                                            pdialog.dismiss();
                                            Log.i("Post Image", "deleted");
                                        })
                                                .addOnFailureListener(e -> Log.e("Post Image", e.getLocalizedMessage()));
                                    }

                                    pdialog.show();
                                    if (!TextUtils.isEmpty(post.getImage_url_3())) {
                                        StorageReference img = FirebaseStorage.getInstance()
                                                .getReferenceFromUrl(post.getImage_url_3());
                                        img.delete().addOnSuccessListener(aVoid14 -> {
                                            pdialog.dismiss();
                                            Log.i("Post Image", "deleted");
                                        })
                                                .addOnFailureListener(e -> Log.e("Post Image", e.getLocalizedMessage()));
                                    }

                                    pdialog.show();
                                    if (!TextUtils.isEmpty(post.getImage_url_4())) {
                                        StorageReference img = FirebaseStorage.getInstance()
                                                .getReferenceFromUrl(post.getImage_url_4());
                                        img.delete().addOnSuccessListener(aVoid15 -> {
                                            pdialog.dismiss();
                                            Log.i("Post Image", "deleted");
                                        })
                                                .addOnFailureListener(e -> Log.e("Post Image", e.getLocalizedMessage()));
                                    }

                                    pdialog.show();
                                    if (!TextUtils.isEmpty(post.getImage_url_5())) {
                                        StorageReference img = FirebaseStorage.getInstance()
                                                .getReferenceFromUrl(post.getImage_url_5());
                                        img.delete().addOnSuccessListener(aVoid16 -> {
                                            pdialog.dismiss();
                                            Log.i("Post Image", "deleted");
                                        })
                                                .addOnFailureListener(e -> Log.e("Post Image", e.getLocalizedMessage()));
                                    }

                                    pdialog.show();
                                    if (!TextUtils.isEmpty(post.getImage_url_6())) {
                                        StorageReference img = FirebaseStorage.getInstance()
                                                .getReferenceFromUrl(post.getImage_url_6());
                                        img.delete().addOnSuccessListener(aVoid17 -> {
                                            pdialog.dismiss();
                                            Log.i("Post Image", "deleted");
                                        })
                                                .addOnFailureListener(e -> Log.e("Post Image", e.getLocalizedMessage()));
                                    }

                                    Toasty.success(context, "Post deleted", Toasty.LENGTH_SHORT, true).show();
                                    // postList.remove(holder.getAdapterPosition());
                                    // notifyItemRemoved(holder.getAdapterPosition());
                                    // notifyDataSetChanged();
                                    pdialog.dismiss();


                                })
                                .addOnFailureListener(e -> {
                                    pdialog.dismiss();
                                    Log.e("error", e.getLocalizedMessage());
                                });

                    })
                    .onNegative((dialog, which) -> dialog.dismiss()).show());

        } else {
            isOwner = false;
            holder.delete.setVisibility(View.GONE);
        }
        try {
            mFirestore.collection("Users")
                    .document(post.getUserId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        try {
                            if (!documentSnapshot.getString("image").equals(post.getUserimage())) {
                                Map<String, Object> postMap = new HashMap<>();
                                postMap.put("name", documentSnapshot.getString("name"));
                                postMap.put("userimage", documentSnapshot.getString("image"));

                                mFirestore.collection("Posts")
                                        .document(post.getPostId())
                                        .update(postMap)
                                        .addOnSuccessListener(aVoid -> Log.i("post_update", "success"))
                                        .addOnFailureListener(e -> Log.i("post_update", "failure"));

                                user_name.setText(documentSnapshot.getString("name"));

                                Glide.with(Home.context)
                                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_logo_icon))
                                        .load(documentSnapshot.getString("image"))
                                        .into(user_image);
                            } else if (!Objects.equals(documentSnapshot.getString("userId"), post.getUserId())) {
                                Map<String, Object> postMap = new HashMap<>();
                                postMap.put("name", documentSnapshot.getString("name"));
                                mFirestore.collection("Posts")
                                        .document(post.getPostId())
                                        .update(postMap)
                                        .addOnSuccessListener(aVoid -> Log.i("post_update", "success"))
                                        .addOnFailureListener(e -> Log.i("post_update", "failure"));

                                user_name.setText(documentSnapshot.getString("name"));

                            } else if (!Objects.equals(documentSnapshot.getString("image"), post.getUserimage())) {

                                Map<String, Object> postMap = new HashMap<>();
                                postMap.put("userimage", documentSnapshot.getString("image"));

                                mFirestore.collection("Posts")
                                        .document(post.getPostId())
                                        .update(postMap)
                                        .addOnSuccessListener(aVoid -> Log.i("post_update", "success"))
                                        .addOnFailureListener(e -> Log.i("post_update", "failure"));

                                Glide.with(Home.context)
                                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_logo_icon))
                                        .load(documentSnapshot.getString("image"))
                                        .into(user_image);

                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Error", e.getMessage()));
        } catch (Exception ex) {
            Log.w("error", "fastscrolled", ex);
        }

        user_name.setOnClickListener(v -> FriendProfile.startActivity(context, post.getUserId()));

        see_more.setOnClickListener(view -> context.startActivity(new Intent(Home.context, CommentsActivity.class).putExtra("post", post).putExtra("owner", isOwner).putExtra("approveStatus", approved)));

        holder.itemView.setOnClickListener(view -> context.startActivity(new Intent(Home.context, CommentsActivity.class).putExtra("post", post).putExtra("owner", isOwner).putExtra("approveStatus", approved)));

        if (forComment) {
            context.startActivity(new Intent(Home.context, CommentsActivity.class).putExtra("post", post).putExtra("owner", isOwner).putExtra("approveStatus", approved));
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) Home.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @SuppressLint("SetTextI18n")
    private void setupViews(Post post) {

        try {
            getLikeandFav(post);
        } catch (NullPointerException ignored) {

        }
        user_name.setText(post.getName());

        institute_dept.setText(post.getDept() + ", " + post.getInstitute());

        Glide.with(Home.context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.placeholder))
                .load(post.getUserimage())
                .into(user_image);
        String timeAgo = TimeAgo.using(Long.parseLong(post.getTimestamp()));
        timestamp.setText(timeAgo);
    }

    @SuppressLint("ClickableViewAccessibility")


    private void setUrls(ArrayList<MultipleImage> multipleImages, PostPhotosAdapter photosAdapter, Post post) {
        String url0, url1, url2, url3, url4, url5, url6;
        url0 = post.getImage_url_0();
        url1 = post.getImage_url_1();
        url2 = post.getImage_url_2();
        url3 = post.getImage_url_3();
        url4 = post.getImage_url_4();
        url5 = post.getImage_url_5();
        url6 = post.getImage_url_6();

        if (!TextUtils.isEmpty(url0)) {
            MultipleImage image = new MultipleImage(url0);
            multipleImages.add(image);
            photosAdapter.notifyDataSetChanged();
            Log.i("url0", url0);
        }

        if (!TextUtils.isEmpty(url1)) {
            MultipleImage image = new MultipleImage(url1);
            multipleImages.add(image);
            photosAdapter.notifyDataSetChanged();
            Log.i("url1", url1);
        }

        if (!TextUtils.isEmpty(url2)) {
            MultipleImage image = new MultipleImage(url2);
            multipleImages.add(image);
            photosAdapter.notifyDataSetChanged();
            Log.i("url2", url2);
        }

        if (!TextUtils.isEmpty(url3)) {
            MultipleImage image = new MultipleImage(url3);
            multipleImages.add(image);
            photosAdapter.notifyDataSetChanged();
            Log.i("url3", url3);
        }

        if (!TextUtils.isEmpty(url4)) {
            MultipleImage image = new MultipleImage(url4);
            multipleImages.add(image);
            photosAdapter.notifyDataSetChanged();
            Log.i("url4", url4);
        }

        if (!TextUtils.isEmpty(url5)) {
            MultipleImage image = new MultipleImage(url5);
            multipleImages.add(image);
            photosAdapter.notifyDataSetChanged();
            Log.i("url5", url5);
        }

        if (!TextUtils.isEmpty(url6)) {
            MultipleImage image = new MultipleImage(url6);
            multipleImages.add(image);
            photosAdapter.notifyDataSetChanged();
            Log.i("ur6", url6);
        }


    }

    private void addToNotification(String admin_id, Notification notification) {
        if (!admin_id.equals(userId)) {
            mFirestore.collection("Users")
                    .document(admin_id)
                    .collection("Info_Notifications")
                    .add(notification)
                    .addOnSuccessListener(documentReference -> new SendNotificationAsyncTask(notification).execute())
                    .addOnFailureListener(e -> Log.e("Error", e.getLocalizedMessage()));
        }
    }

    private void getLikeandFav(Post post) {
        try {
            mFirestore.collection("Posts")
                    .document(post.getPostId())
                    .collection("Liked_Users")
                    .document(mCurrentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            boolean liked = documentSnapshot.getBoolean("liked");
                            like_btn.setFavorite(liked, false);
                        }

                        if (isOnline()) {
                            like_btn.setOnFavoriteChangeListener((buttonView, favorite) -> {
                                Map<String, Object> likeMap = new HashMap<>();
                                if (favorite) {
                                    likeMap.put("liked", true);
                                    updateLike(true, post.getPostId());
                                    try {
                                        mFirestore.collection("Posts")
                                                .document(post.getPostId())
                                                .collection("Liked_Users")
                                                .document(mCurrentUser.getUid())
                                                .set(likeMap)
                                                .addOnSuccessListener(aVoid -> {
                                                    UserViewModel userViewModel = ViewModelProviders.of((FragmentActivity) context).get(UserViewModel.class);
                                                    userViewModel.user.observe((LifecycleOwner) context, users -> {
                                                        Notification notification = new Notification(post.getPostId(),
                                                                users.getUsername(),
                                                                users.getImage(),
                                                                "Liked your post",
                                                                String.valueOf(System.currentTimeMillis())
                                                                , "like"
                                                                , post.getPostId()
                                                        );
                                                        addToNotification(post.getUserId(), notification);
                                                    });
                                                })
                                                .addOnFailureListener(e -> Log.e("Error like", e.getMessage()));
                                    } catch (Exception ignored) {

                                    }
                                } else {
                                    likeMap.put("liked", false);
                                    updateLike(false, post.getPostId());
                                    try {
                                        mFirestore.collection("Posts")
                                                .document(post.getPostId())
                                                .collection("Liked_Users")
                                                .document(mCurrentUser.getUid())
                                                //.set(likeMap)
                                                .delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    //holder.like_count.setText(String.valueOf(Integer.parseInt(holder.like_count.getText().toString())-1));
                                                    //Toast.makeText(context, "Unliked post '" + post.postId, Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> Log.e("Error unlike", e.getMessage()));
                                    } catch (Exception ignored) {

                                    }
                                }
                            });
                        }


                    })
                    .addOnFailureListener(e -> Log.e("Error Like", e.getMessage()));
        } catch (NullPointerException ignored) {

        }

        try {
            mFirestore.collection("Posts")
                    .document(post.getPostId())
                    .collection("Saved_Users")
                    .document(mCurrentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {

                        if (documentSnapshot.exists()) {
                            boolean fav = documentSnapshot.getBoolean("Saved");

                            sav_button.setFavorite(fav, false);
                        } else {
                            Log.e("Fav", "No document found");

                        }

                        sav_button.setOnFavoriteChangeListener((buttonView, favorite) -> {
                            postViewModel = ViewModelProviders.of((FragmentActivity) context).get(PostViewModel.class);
                            postViewModel.insert(post);
                            if (favorite) {
                                Map<String, Object> favMap = new HashMap<>();
                                favMap.put("Saved", true);
                                try {
                                    mFirestore.collection("Posts")
                                            .document(post.getPostId())
                                            .collection("Saved_Users")
                                            .document(mCurrentUser.getUid())
                                            .set(favMap)
                                            .addOnSuccessListener(aVoid -> {

                                            })
                                            .addOnFailureListener(e -> Log.e("Error fav", e.getMessage()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } else {
                                postViewModel.delete(post);
                                Map<String, Object> favMap = new HashMap<>();
                                favMap.put("Saved", false);

                                try {

                                    mFirestore.collection("Posts")
                                            .document(post.getPostId())
                                            .collection("Saved_Users")
                                            .document(mCurrentUser.getUid())
                                            //.set(favMap)
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {

                                            })
                                            .addOnFailureListener(e -> Log.e("Error fav", e.getMessage()));

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    })
                    .addOnFailureListener(e -> Log.e("Error Fav", e.getMessage()));
        } catch (NullPointerException ignored) {
        }

    }

    private static class SendNotificationAsyncTask extends AsyncTask<Void, Void, Void> {
        final APIService apiService;
        final Notification notification;

        private SendNotificationAsyncTask(Notification notification) {
            this.notification = notification;
            apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        }

        @Override
        protected Void doInBackground(Void... jk) {
            FirebaseDatabase.getInstance().getReference().child("Tokens").child(notification.getId()).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String usertoken = dataSnapshot.getValue(String.class);
                    NotificationSender sender = new NotificationSender(notification, usertoken);
                    apiService.sendNotifcation(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                        }

                        @Override
                        public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return null;
        }
    }

}