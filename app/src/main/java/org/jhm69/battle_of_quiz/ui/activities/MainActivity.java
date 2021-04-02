package org.jhm69.battle_of_quiz.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.yalantis.ucrop.UCrop;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import org.jhm69.battle_of_quiz.R;
import org.jhm69.battle_of_quiz.SendNotificationPack.Token;
import org.jhm69.battle_of_quiz.adapters.DrawerAdapter;
import org.jhm69.battle_of_quiz.models.DrawerItem;
import org.jhm69.battle_of_quiz.models.SimpleItem;
import org.jhm69.battle_of_quiz.models.Users;
import org.jhm69.battle_of_quiz.ui.activities.account.EditProfile;
import org.jhm69.battle_of_quiz.ui.activities.account.LoginActivity;
import org.jhm69.battle_of_quiz.ui.activities.friends.SearchUsersActivity;
import org.jhm69.battle_of_quiz.ui.activities.quiz.Ranking;
import org.jhm69.battle_of_quiz.ui.fragment.Dashboard;
import org.jhm69.battle_of_quiz.ui.fragment.FriendsFragment;
import org.jhm69.battle_of_quiz.ui.fragment.ProfileFragment;
import org.jhm69.battle_of_quiz.ui.fragment.SavedFragment;
import org.jhm69.battle_of_quiz.ui.fragment.admin.AdminFragment;
import org.jhm69.battle_of_quiz.utils.Config;
import org.jhm69.battle_of_quiz.viewmodel.ResultViewModel;
import org.jhm69.battle_of_quiz.viewmodel.UserViewModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

import static org.jhm69.battle_of_quiz.R.id.search;

/**
 * Created by jhm69
 */
public class MainActivity extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener {
    private static final int POS_DASHBOARD = 0;
    private static final int RANKING = 1;
    private static final int POS_FRIENDS = 2;
    private static final int SAVED_POST = 3;
    //private static final int POS_SHARE = 4;
    private static final int POS_LOGOUT = 4;
    private static final int ADMIN = 5;
    public static String userId;
    @SuppressLint({"StaticFieldLeak", "NewApi"})
    private final Set<String> ADMIN_UID_LIST = Set.of(
            "zjQh2f1tn7O8tKDolnhBq9AqtcH3"
    );
    public TextView username;
    public TextView rewardTv;
    public Fragment mCurrentFragment;
    Toolbar mainToolbar;
    DrawerAdapter adapter;
    String edit = "d";
    ResultViewModel resultViewModel;
    UserViewModel userViewModel;
    private FirebaseUser currentuser;
    private CircleImageView imageView;
    private String[] screenTitles;
    private Drawable[] screenIcons;
    private SlidingRootNav slidingRootNav;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private RewardedAd rewardedAd;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, boolean validate) {
        Intent intent = new Intent(context, MainActivity.class).putExtra("validate", validate);
        context.startActivity(intent);
    }

    public static void updateStatus() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        wait(10000);
                        try {
                            String refreshToken = FirebaseInstanceId.getInstance().getToken();
                            Token token = new Token(refreshToken);
                            FirebaseDatabase.getInstance().getReference("Tokens").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
                            long timestamp = System.currentTimeMillis();
                            HashMap<String, Object> scoreMap = new HashMap<>();
                            scoreMap.put("lastTimestamp", timestamp);
                            FirebaseFirestore.getInstance()
                                    .collection("Users")
                                    .document(userId)
                                    .update(scoreMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @SuppressLint("CheckResult")
                                @Override
                                public void onSuccess(Void aVoid) {
                                }
                            });
                        } catch (NullPointerException j) {

                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();

    }

    private void updateXP() {
        FirebaseFirestore.getInstance().collection("Users")
                .document(currentuser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    int scoreOld = Objects.requireNonNull(documentSnapshot.getLong("reward")).intValue();
                    int newScore = scoreOld + (20);
                    HashMap<String, Object> scoreMap = new HashMap<>();
                    scoreMap.put("reward", newScore);
                    FirebaseFirestore.getInstance()
                            .collection("Users")
                            .document(currentuser.getUid())
                            .update(scoreMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @SuppressLint({"CheckResult", "DefaultLocale"})
                        @Override
                        public void onSuccess(Void aVoid) {
                            userViewModel.updateXp(5);
                        }
                    });
                });
    }

    @SuppressLint("CheckResult")
    private void loadAd() {
        if (rewardedAd.isLoaded()) {
            RewardedAdCallback adCallback = new RewardedAdCallback() {
                @Override
                public void onRewardedAdOpened() {
                    // Ad opened.
                }

                @Override
                public void onRewardedAdClosed() {
                    // Ad closed.
                }

                @Override
                public void onUserEarnedReward(@NonNull RewardItem reward) {
                    // User earned reward.
                    updateXP();

                }

                @Override
                public void onRewardedAdFailedToShow(AdError adError) {
                    // Ad failed to display.
                }
            };
            rewardedAd.show(this, adCallback);
        } else {
            Toasty.error(this, "The video ad wasn't loaded yet. Please try again after few min.", Toasty.LENGTH_SHORT, true);
            Log.d("TAG", "");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        getSharedPreferences("fcm_activity", MODE_PRIVATE).edit().putBoolean("active", false).apply();
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onStart() {
        try {
            super.onStart();

            //getSharedPreferences("fcm_activity", MODE_PRIVATE).edit().putBoolean("active", true).apply();
            //boolean validate = getIntent().getBooleanExtra("validate", false);
            try {
                if (edit.equals("sss")) {
                    startActivity(new Intent(getApplicationContext(), EditProfile.class));
                }
            } catch (NullPointerException h) {
            }
            if (currentuser != null) {

            } else {
                LoginActivity.startActivityy(this);
                finish();
            }
        } catch (IllegalArgumentException hg) {

        }
    }

    @Override
    public void onBackPressed() {
        showFragment(new Dashboard());
        if (slidingRootNav.isMenuOpened()) {
            slidingRootNav.closeMenu(true);
        }
        adapter.setSelected(POS_DASHBOARD);
    }

    @SuppressLint("PackageManagerGetSignatures")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edit = getIntent().getStringExtra("sss");
        mainToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(mainToolbar);
        resultViewModel = ViewModelProviders.of(this)
                .get(ResultViewModel.class);
        userViewModel = ViewModelProviders.of(this)
                .get(UserViewModel.class);
        updateStatus();
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.statusBar));


        firestore = FirebaseFirestore.getInstance();


        mAuth = FirebaseAuth.getInstance();
        currentuser = mAuth.getCurrentUser();

        MobileAds.initialize(this, initializationStatus -> {
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Config.createNotificationChannels(this);
        }
        mCurrentFragment = new Dashboard();
        if (currentuser == null) {
            LoginActivity.startActivityy(this);
            finish();
        } else {
            setUserProfile();
            MobileAds.initialize(getApplicationContext(),
                    "ca-app-pub-3940256099942544~3347511713");

            rewardedAd = new RewardedAd(getApplicationContext(),
                    "ca-app-pub-3940256099942544/5224354917");

            RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
                @Override
                public void onRewardedAdLoaded() {
                    // Ad successfully loaded.
                }

                @Override
                public void onRewardedAdFailedToLoad(LoadAdError adError) {
                    // Ad failed to load.
                }
            };
            rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                        }
                    });
        }
        askPermission();
        userId = currentuser.getUid();
        runOnUiThread(() -> {
            slidingRootNav = new SlidingRootNavBuilder(MainActivity.this)
                    .withToolbarMenuToggle(mainToolbar)
                    .withMenuOpened(false)
                    .withContentClickableWhenMenuOpened(false)
                    .withSavedState(savedInstanceState)
                    .withMenuLayout(R.layout.activity_main_drawer)
                    .inject();
            screenIcons = loadScreenIcons();
            screenTitles = loadScreenTitles();

            if (ADMIN_UID_LIST.contains(userId)) {
                adapter = new DrawerAdapter(Arrays.asList(
                        createItemFor(POS_DASHBOARD).setChecked(true),
                        createItemFor(RANKING),
                        createItemFor(POS_FRIENDS),
                        createItemFor(SAVED_POST),
                        createItemFor(POS_LOGOUT),
                        createItemFor(ADMIN)));
            } else {
                adapter = new DrawerAdapter(Arrays.asList(
                        createItemFor(POS_DASHBOARD).setChecked(true),
                        createItemFor(RANKING),
                        createItemFor(POS_FRIENDS),
                        createItemFor(SAVED_POST),
                        createItemFor(POS_LOGOUT)));
            }
            adapter.setListener(MainActivity.this);
            RecyclerView list = findViewById(R.id.list);
            list.setNestedScrollingEnabled(false);
            list.setLayoutManager(new LinearLayoutManager(this));
            list.setAdapter(adapter);
            adapter.setSelected(POS_DASHBOARD);


        });

    }


    private void askPermission() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        //Toasty.info(MainActivity.this, "You have denied some permissions permanently, if the app force close try granting permission from settings.", Toasty.LENGTH_LONG,true).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    public void logout() {
        HashMap<String, Object> scoreMap = new HashMap<>();
        scoreMap.put("lastTimestamp", System.currentTimeMillis());
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentuser.getUid())
                .update(scoreMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @SuppressLint("CheckResult")
            @Override
            public void onSuccess(Void aVoid) {
            }
        });
        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setIndeterminate(true);
        mDialog.setMessage("Logging you out...");
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, MODE_PRIVATE);

        Map<String, Object> tokenRemove = new HashMap<>();
        tokenRemove.put("token_ids", FieldValue.arrayRemove(pref.getString("regId", "")));

        firestore.collection("Users").document(userId).update(tokenRemove).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                userViewModel.delete();
                Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).delete();
                mAuth.signOut();
                LoginActivity.startActivityy(MainActivity.this);
                mDialog.dismiss();
                finish();
            }
        }).addOnFailureListener(e -> {
            Toasty.error(MainActivity.this, "Error logging out", Toasty.LENGTH_SHORT, true).show();
            mDialog.dismiss();

        });

    }

    @SuppressLint("SetTextI18n")
    private void setUserProfile() {
        try {
            userViewModel.user.observe(this, new Observer<Users>() {
                @Override
                public void onChanged(Users me) {
                    if (me == null) {
                        Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                        finish();
                    }
                    Log.d("iddr", userId);
                    username = findViewById(R.id.username);
                    imageView = findViewById(R.id.profile_image);
                    rewardTv = findViewById(R.id.reaward);
                    String nam = Objects.requireNonNull(me).getName(), imag = me.getImage();
                    Button add = findViewById(R.id.button4);
                    add.setOnClickListener(view -> loadAd());
                    username.setText(nam);
                    Glide.with(MainActivity.this)
                            .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_logo_icon))
                            .load(imag)
                            .into(imageView);
                    Log.d("Reward", String.valueOf(me.getReward()));
                    rewardTv.setText(me.getReward() + " xp");



       /* darkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    Intent intent = getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    finish();
                    startActivity(intent);

                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    Intent intent = getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    finish();
                    startActivity(intent);
                }
            }
        });*/

                }
            });


        } catch (Exception g) {

        }
    }


    private DrawerItem createItemFor(int position) {
        return new SimpleItem(screenIcons[position], screenTitles[position])
                .withIconTint(Color.parseColor("#989898"))
                .withTextTint(Color.parseColor("#989898"))
                .withSelectedIconTint(color())
                .withSelectedTextTint(color());
    }

    @NonNull
    private String[] loadScreenTitles() {
        return getResources().getStringArray(R.array.ld_activityScreenTitles);
    }

    private Drawable[] loadScreenIcons() {
        TypedArray ta = getResources().obtainTypedArray(R.array.ld_activityScreenIcons);
        Drawable[] icons = new Drawable[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            int id = ta.getResourceId(i, 0);
            if (id != 0) {
                icons[i] = ContextCompat.getDrawable(this, id);
            }
        }
        ta.recycle();
        return icons;
    }

    @ColorInt
    private int color() {
        return ContextCompat.getColor(this, R.color.colorAccentt);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @SuppressLint("CheckResult")
    @Override
    public void onItemSelected(int position) {
        Fragment selectedScreen;
        switch (position) {

            case POS_DASHBOARD:
                mainToolbar.setSubtitle("Dashboard");
                selectedScreen = new Dashboard();
                showFragment(selectedScreen);
                slidingRootNav.closeMenu(true);
                return;

            case ADMIN:
                mainToolbar.setSubtitle("Admin");
                selectedScreen = new AdminFragment();
                showFragment(selectedScreen);
                slidingRootNav.closeMenu(true);
                return;

            case POS_FRIENDS:
                mainToolbar.setSubtitle("Friends");
                selectedScreen = new FriendsFragment();
                showFragment(selectedScreen);
                slidingRootNav.closeMenu(true);
                return;

            case RANKING:
                mainToolbar.setSubtitle("Ranking");
                selectedScreen = new Ranking();
                showFragment(selectedScreen);
                slidingRootNav.closeMenu(true);
                return;

            case SAVED_POST:
                mainToolbar.setSubtitle("Offlined Post");
                selectedScreen = new SavedFragment();
                showFragment(selectedScreen);
                slidingRootNav.closeMenu(true);
                return;

          /*  case POS_SHARE:
                try {
                    doSocialShare("Share to get 20 XP", "Check out Battle of Quiz! Prove your skills by taking part in one to one battle of knowledge!", "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                    slidingRootNav.closeMenu(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
*/
            case POS_LOGOUT:
                if (currentuser != null && isOnline()) {
                    new MaterialDialog.Builder(this)
                            .title("Logout")
                            .content("Are you sure do you want to logout from this account?")
                            .positiveText("Yes")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                    logout();
                                    dialog.dismiss();
                                }
                            }).negativeText("No")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            }).show();
                } else {

                    new MaterialDialog.Builder(this)
                            .title("Logout")
                            .content("A error occurred while logging you out, Check your network connection and try again.")
                            .positiveText("Done")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            }).show();

                }

                return;

            default:
                selectedScreen = new Dashboard();
                showFragment(selectedScreen);

        }

        slidingRootNav.closeMenu(true);

    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void onViewProfileClicked(View view) {
        mainToolbar.setSubtitle("My Profile");
        showFragment(new ProfileFragment());
        slidingRootNav.closeMenu(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_posts, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == 23) {
                Uri imageUri = UCrop.getOutput(data);
                Toasty.info(getApplicationContext(), "Updating...", Toasty.LENGTH_LONG, true).show();
                final StorageReference user_profile = FirebaseStorage.getInstance().getReference().child("images").child(userId + ".png");
                user_profile.putFile(Objects.requireNonNull(imageUri)).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        final DocumentReference userDocument = FirebaseFirestore.getInstance().collection("Users").document(userId);
                        user_profile.getDownloadUrl().addOnSuccessListener(uri -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("image", uri.toString());
                            userDocument.update(map)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            userViewModel.updateUserImage(uri.toString());
                                            // userHelper.updateContactImage(1, uri.toString());
                                            Toasty.success(getApplicationContext(), "Successfully changed Profile image", Toasty.LENGTH_LONG, true).show();
                                            Log.i("Update", "success");
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.i("Update", "failed: " + e.getMessage()));
                        }).addOnFailureListener(e -> Log.e("Error", "listen", e));

                    } else {
                        Log.e("Error", "listen", task.getException());
                    }

                });
            } else if (requestCode == 667) {
                Toasty.success(this, "Success", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toasty.error(getApplicationContext(), "Error", Toasty.LENGTH_SHORT).show();
        }
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == search) {
            startActivity(new Intent(MainActivity.this, SearchUsersActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        mCurrentFragment = fragment;
    }


    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        // handleIntent();
    }
}