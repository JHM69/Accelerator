package org.jhm69.battle_of_quiz.ui.activities.post;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import org.apache.commons.lang3.StringUtils;
import org.jhm69.battle_of_quiz.R;
import org.jhm69.battle_of_quiz.SendNotificationPack.APIService;
import org.jhm69.battle_of_quiz.SendNotificationPack.Client;
import org.jhm69.battle_of_quiz.SendNotificationPack.MyResponse;
import org.jhm69.battle_of_quiz.SendNotificationPack.NotificationSender;
import org.jhm69.battle_of_quiz.adapters.CommentsAdapter;
import org.jhm69.battle_of_quiz.adapters.PostPhotosAdapter;
import org.jhm69.battle_of_quiz.models.Comment;
import org.jhm69.battle_of_quiz.models.MultipleImage;
import org.jhm69.battle_of_quiz.models.Notification;
import org.jhm69.battle_of_quiz.models.Post;
import org.jhm69.battle_of_quiz.models.Users;
import org.jhm69.battle_of_quiz.repository.UserRepository;
import org.jhm69.battle_of_quiz.ui.fragment.Home;
import org.jhm69.battle_of_quiz.utils.AnimationUtil;
import org.jhm69.battle_of_quiz.utils.MathView;
import org.jhm69.battle_of_quiz.utils.RichEditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import static org.jhm69.battle_of_quiz.adapters.PostViewHolder.updateLike;
import static org.jhm69.battle_of_quiz.ui.activities.MainActivity.userId;

@RequiresApi(api = Build.VERSION_CODES.R)
public class CommentsActivity extends AppCompatActivity {
    @SuppressLint("NewApi")
    private final Set<String> ADMIN_UID_LIST = Set.of(
            "zjQh2f1tn7O8tKDolnhBq9AqtcH3"
    );
    String user_id, post_id;
    MathView post_desc;
    TextView likeCount, saveCount;
    int likCo, saveCo, comCo;
    ProgressBar mProgress;
    boolean owner;
    Post post;
    Users me;
    ImageView myImage;
    List<Comment> commentList = new ArrayList<>();
    TextView CommentCount;
    RecyclerView mCommentsRecycler;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mCurrentUser;
    private CircleImageView user_image;
    private MaterialFavoriteButton sav_button, like_btn;
    private FrameLayout pager_layout;
    private RelativeLayout indicator_holder;
    private ViewPager pager;
    private RichEditor mCommentText;
    private DotsIndicator indicator2;
    private CommentsAdapter mAdapter;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_comments);
        mFirestore = FirebaseFirestore.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.statusBar));
        user_image = findViewById(R.id.comment_admin);
        post_desc = findViewById(R.id.comment_post_desc);
        TextView p_nameTV = findViewById(R.id.post_username);
        TextView p_instTV = findViewById(R.id.dept_institute);
        TextView timestampTV = findViewById(R.id.post_timestamp);
        likeCount = findViewById(R.id.like_count);
        saveCount = findViewById(R.id.save_count);
        CommentCount = findViewById(R.id.textView8);
        mCommentsRecycler = findViewById(R.id.coments);
        LinearLayout adminActivity = findViewById(R.id.adminActivity);
        like_btn = findViewById(R.id.like_button);
        View vBgLike = findViewById(R.id.vBgLike);
        ImageView ivLike = findViewById(R.id.ivLike);
        pager = findViewById(R.id.pager);
        pager_layout = findViewById(R.id.pager_layout);
        sav_button = findViewById(R.id.save_button);
        me = new UserRepository(getApplication()).getStaticUser();
        FrameLayout mImageholder = findViewById(R.id.image_holder);
        indicator2 = findViewById(R.id.indicator);
        indicator_holder = findViewById(R.id.indicator_holder);


        boolean approved = getIntent().getBooleanExtra("approveStatus", true);

        post = (Post) getIntent().getSerializableExtra("post");

        if (!approved && ADMIN_UID_LIST.contains(userId)) {
            Toast.makeText(this, "Approve or Delete this Post", Toast.LENGTH_SHORT).show();
            adminActivity.setVisibility(View.VISIBLE);
            findViewById(R.id.approvePost).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    approvePost(post);
                }
            });
            findViewById(R.id.deletePost).setOnClickListener(view -> deletePost(post.getPostId()));
        } else {
            adminActivity.setVisibility(View.GONE);
        }

        if (post.getImage_count() > 0) {
            mImageholder.setVisibility(View.VISIBLE);
        } else {
            mImageholder.setVisibility(View.GONE);
        }


        user_id = post.getUserId();
        post_desc.setDisplayText(post.getDescription());
        p_instTV.setText(post.getDept() + ", " + post.getInstitute());
        p_nameTV.setText(post.getName());
        timestampTV.setText(TimeAgo.using(Long.parseLong(Objects.requireNonNull(post.getTimestamp()))));
        setupCommentView();
        getLikeandFav(post);
        setStatData(post);


        mCommentText = findViewById(R.id.text);
        mCommentText.setPlaceholder("Type your comment here...");
        ImageView mCommentsSend = findViewById(R.id.send);
        mProgress = findViewById(R.id.progressBar5);

        myImage = findViewById(R.id.imageView7);

        commentList = new ArrayList<>();
        mAdapter = new CommentsAdapter(commentList, this, owner);
        mCommentsSend.setOnClickListener(view -> {
            String comment = mCommentText.getHtml();
            if (!TextUtils.isEmpty(comment))
                sendComment(comment, mProgress);
            else
                AnimationUtil.shakeView(mCommentText, CommentsActivity.this);
        });

        mCommentsRecycler.setItemAnimator(new DefaultItemAnimator());
        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mCommentsRecycler.setHasFixedSize(true);
        mCommentsRecycler.setAdapter(mAdapter);

        getComments(mProgress);

        Glide.with(getApplicationContext())
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_logo))
                .load(me.getImage())
                .into(myImage);

    }

    private void approvePost(Post post) {
        post.setLike_count(0);
        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setMessage("Approving...");
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        mFirestore.collection("Posts")
                .document(post.getPostId())
                .set(post).addOnSuccessListener(aVoid -> {
            addToNotification("An Admin Approved Your Post", "post");
            mFirestore.collection("PendingPosts")
                    .document(post.getPostId()).delete();
            mDialog.dismiss();
            Toasty.success(getApplicationContext(), "Approved", Toasty.LENGTH_SHORT, true).show();
            finish();
        });

    }

    private void deletePost(String Id) {
        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setMessage("Removing from Approval List...");
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        mFirestore.collection("PendingPosts")
                .document(Id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                addToNotification("An Admin Deleted Your Post, Try posting good contents only.", "");
                mDialog.dismiss();
                Toasty.success(getApplicationContext(), "Done", Toasty.LENGTH_SHORT, true).show();
                finish();
            }
        });
    }


    @SuppressLint("SetTextI18n")
    private void setupCommentView() {
        if (post.getImage_count() == 0) {
            pager_layout.setVisibility(View.GONE);
        } else if (post.getImage_count() == 1) {
            pager_layout.setVisibility(View.VISIBLE);
            ArrayList<MultipleImage> multipleImages = new ArrayList<>();
            PostPhotosAdapter photosAdapter = new PostPhotosAdapter(Home.context, this, multipleImages, false, post.getPostId(), like_btn, post.getUserId(), true);
            setUrls(multipleImages, photosAdapter, post);
            pager.setAdapter(photosAdapter);
            indicator_holder.setVisibility(View.GONE);
            photosAdapter.notifyDataSetChanged();
            pager_layout.setVisibility(View.VISIBLE);
            post_desc.setVisibility(View.VISIBLE);
            String desc = post.getDescription();
            post_desc.setDisplayText(StringUtils.left(desc, 200));
        } else {
            ArrayList<MultipleImage> multipleImages = new ArrayList<>();
            PostPhotosAdapter photosAdapter = new PostPhotosAdapter(Home.context, this, multipleImages, false, post.getPostId(), like_btn, post.getUserId(), true);
            setUrls(multipleImages, photosAdapter, post);

            pager.setAdapter(photosAdapter);
            photosAdapter.notifyDataSetChanged();
            indicator2.setDotsClickable(true);
            indicator2.setViewPager(pager);

            final Handler handler = new Handler();
            final Runnable slide = new Runnable() {
                @Override
                public void run() {
                    if (pager.getCurrentItem() == multipleImages.size()) {
                        pager.setCurrentItem(0, true);
                        return;
                    }
                    pager.setCurrentItem(pager.getCurrentItem() + 1, true);
                }
            };
            Timer slideTimer = new Timer();
            slideTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(slide);
                }
            }, 3000, 3000);


            pager_layout.setVisibility(View.VISIBLE);
            indicator_holder.setVisibility(View.VISIBLE);

        }

        mFirestore.collection("Users")
                .document(user_id)
                .get()
                .addOnSuccessListener(documentSnapshot -> Glide.with(getApplicationContext())
                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_logo_icon))
                        .load(documentSnapshot.getString("image"))
                        .into(user_image))
                .addOnFailureListener(e -> Log.e("error", e.getLocalizedMessage()));

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

    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

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

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) Home.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
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
                        } else {
                            Log.e("Like", "No document found");

                        }

                        if (isOnline()) {
                            like_btn.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
                                @Override
                                public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                                    Map<String, Object> likeMap = new HashMap<>();
                                    if (favorite) {
                                        likeMap.put("liked", true);

                                        try {
                                            updateLike(true, post.getPostId());
                                            mFirestore.collection("Posts")
                                                    .document(post.getPostId())
                                                    .collection("Liked_Users")
                                                    .document(mCurrentUser.getUid())
                                                    .set(likeMap)
                                                    .addOnSuccessListener(aVoid -> {
                                                        likCo++;
                                                        likeCount.setText(String.valueOf(likCo));
                                                        addToNotification("Liked in your Post", "like");


                                                    })
                                                    .addOnFailureListener(e -> Log.e("Error like", e.getMessage()));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        likeMap.put("liked", false);

                                        try {
                                            updateLike(false, post.getPostId());
                                            mFirestore.collection("Posts")
                                                    .document(post.getPostId())
                                                    .collection("Liked_Users")
                                                    .document(mCurrentUser.getUid())
                                                    //.set(likeMap)
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            likCo--;
                                                            likeCount.setText(String.valueOf(likCo));
                                                            //holder.like_count.setText(String.valueOf(Integer.parseInt(holder.like_count.getText().toString())-1));
                                                            //Toast.makeText(context, "Unliked post '" + post.postId, Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.e("Error unlike", e.getMessage());
                                                        }
                                                    });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        }


                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Error Like", e.getMessage());
                        }
                    });
        } catch (NullPointerException j) {

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

                        if (isOnline()) {
                            sav_button.setOnFavoriteChangeListener((buttonView, favorite) -> {
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
                                                    saveCo++;
                                                    saveCount.setText(String.valueOf(saveCo));

                                                    Map<String, Object> postMap = new HashMap<>();
                                                    postMap.put("postId", post.getPostId());
                                                    postMap.put("userId", post.getUserId());
                                                    postMap.put("name", post.getName());
                                                    postMap.put("username", post.getUsername());
                                                    postMap.put("institute", post.getInstitute());
                                                    postMap.put("dept", post.getDept());
                                                    postMap.put("timestamp", post.getTimestamp());
                                                    postMap.put("image_count", post.getImage_count());
                                                    postMap.put("description", post.getDescription());

                                                    try {
                                                        postMap.put("image_url_0", post.getImage_url_0());
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    try {
                                                        postMap.put("image_url_1", post.getImage_url_1());
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    try {
                                                        postMap.put("image_url_2", post.getImage_url_2());
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    try {
                                                        postMap.put("image_url_3", post.getImage_url_3());
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    try {
                                                        postMap.put("image_url_4", post.getImage_url_4());
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    try {
                                                        postMap.put("image_url_5", post.getImage_url_5());
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    try {
                                                        postMap.put("image_url_6", post.getImage_url_6());
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }

                                                    mFirestore.collection("Users")
                                                            .document(mCurrentUser.getUid())
                                                            .collection("Saved_Posts")
                                                            .document(post.getPostId())
                                                            .set(postMap)
                                                            .addOnSuccessListener(aVoid12 -> {
                                                                // Toast.makeText(context, "Added to Saved_Posts, post '" + post.postId, Toast.LENGTH_SHORT).show();
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.e("Error add fav", e.getMessage());
                                                        }
                                                    });
                                                })
                                                .addOnFailureListener(e -> Log.e("Error fav", e.getMessage()));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
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
                                                    saveCo--;
                                                    saveCount.setText(String.valueOf(saveCo));
                                                    mFirestore.collection("Users")
                                                            .document(mCurrentUser.getUid())
                                                            .collection("Saved_Posts")
                                                            .document(post.getPostId())
                                                            .delete()
                                                            .addOnSuccessListener(aVoid1 -> {
                                                                // Toast.makeText(context, "Removed from Saved_Posts, post '" + post.postId, Toast.LENGTH_SHORT).show();
                                                            })
                                                            .addOnFailureListener(e -> Log.e("Error remove fav", e.getMessage()));

                                                })
                                                .addOnFailureListener(e -> Log.e("Error fav", e.getMessage()));

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Error Fav", e.getMessage()));
        } catch (NullPointerException j) {
        }
    }

    @SuppressLint("SetTextI18n")
    void setStatData(Post post) {
        FirebaseFirestore.getInstance().collection("Posts")
                .document(post.getPostId())
                .collection("Liked_Users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    likeCount.setText(String.valueOf(queryDocumentSnapshots.size()));
                    likCo = queryDocumentSnapshots.size();
                    FirebaseFirestore.getInstance().collection("Posts")
                            .document(post.getPostId())
                            .collection("Saved_Users")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                saveCount.setText(String.valueOf(queryDocumentSnapshots1.size()));
                                saveCo = queryDocumentSnapshots1.size();
                                FirebaseFirestore.getInstance().collection("Posts")
                                        .document(post.getPostId())
                                        .collection("Comments")
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots3 -> {
                                            comCo = queryDocumentSnapshots3.size();
                                        })
                                        .addOnFailureListener(Throwable::printStackTrace);
                            })
                            .addOnFailureListener(Throwable::printStackTrace);
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }


    private void addToNotification(String message, String type) {

        if (!post.getUserId().equals(me.getId())) {
            Notification notification = new Notification(post.getUserId(), me.getName(), me.getImage(), message, String.valueOf(System.currentTimeMillis()), type, post.getPostId());


            mFirestore.collection("Users")
                    .document(post.getUserId())
                    .collection("Info_Notifications")
                    .whereEqualTo("id", user_id)
                    .whereEqualTo("action_id", post_id)
                    .whereEqualTo("type", type)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            mFirestore.collection("Users")
                                    .document(post.getUserId())
                                    .collection("Info_Notifications")
                                    .document(notification.getId()).set(notification)
                                    .addOnSuccessListener(documentReference -> {
                                        new SendNotificationAsyncTask(notification).execute();

                                    })
                                    .addOnFailureListener(e -> Log.e("Error", e.getLocalizedMessage()));
                        }

                    })
                    .addOnFailureListener(Throwable::printStackTrace);
        }

    }

    public void finishThis(View v) {
        finish();
        overridePendingTransitionExit();
    }

    private void sendComment(final String comment, final ProgressBar mProgress) {
        mProgress.setVisibility(View.VISIBLE);
        Comment comment1 = new Comment(me.getId(), me.getName(), me.getImage(), post.getPostId(), comment, String.valueOf(System.currentTimeMillis()));
        mCommentText.setHtml("");
        mFirestore.collection("Posts")
                .document(post.getPostId())
                .collection("Comments")
                .add(comment1)
                .addOnSuccessListener(documentReference -> {
                    mProgress.setVisibility(View.GONE);
                    addToNotification("Commented on your post", "comment");
                    Toasty.success(CommentsActivity.this, "Comment added", Toasty.LENGTH_SHORT, true).show();
                    mAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    mCommentText.setHtml(comment1.getComment());
                    mProgress.setVisibility(View.GONE);
                    Toasty.error(CommentsActivity.this, "Error adding comment: " + e.getMessage(), Toasty.LENGTH_SHORT, true).show();
                    Log.e("Error sending comment", Objects.requireNonNull(e.getMessage()));
                });

    }


    @SuppressLint("SetTextI18n")
    private void getComments(final ProgressBar mProgress) {
        mProgress.setVisibility(View.VISIBLE);
        mFirestore.collection("Posts")
                .document(post.getPostId())
                .collection("Comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(this, (querySnapshot, e) -> {
                    if (e != null) {
                        mProgress.setVisibility(View.GONE);
                        e.printStackTrace();
                        return;
                    }
                    if (!querySnapshot.isEmpty()) {
                        for (DocumentChange doc : querySnapshot.getDocumentChanges()) {
                            if (doc.getDocument().exists()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    mProgress.setVisibility(View.GONE);
                                    Comment comment = doc.getDocument().toObject(Comment.class).withId(doc.getDocument().getId());
                                    commentList.add(comment);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                        if (commentList.isEmpty()) {
                            mProgress.setVisibility(View.GONE);
                        }
                        CommentCount.setText("   " + commentList.size());

                    } else {
                        mProgress.setVisibility(View.GONE);
                    }


                });
    }

    private static class SendNotificationAsyncTask extends AsyncTask<Void, Void, Void> {
        final APIService apiService;
        Notification notification;

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
                            if (response.code() == 200) {
                                if (response.body().success != 1) {
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<MyResponse> call, Throwable t) {

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
