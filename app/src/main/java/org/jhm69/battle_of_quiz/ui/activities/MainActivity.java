package org.jhm69.battle_of_quiz.ui.activities;

import static org.jhm69.battle_of_quiz.R.id.search;

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
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import org.jhm69.battle_of_quiz.R;
import org.jhm69.battle_of_quiz.adapters.DrawerAdapter;
import org.jhm69.battle_of_quiz.models.DrawerItem;
import org.jhm69.battle_of_quiz.models.SimpleItem;
import org.jhm69.battle_of_quiz.notification.Token;
import org.jhm69.battle_of_quiz.ui.activities.account.EditProfile;
import org.jhm69.battle_of_quiz.ui.activities.account.LoginActivity;
import org.jhm69.battle_of_quiz.ui.activities.friends.SearchUsersActivity;
import org.jhm69.battle_of_quiz.ui.activities.post.SinglePostView;
import org.jhm69.battle_of_quiz.ui.activities.quiz.Ranking;
import org.jhm69.battle_of_quiz.ui.fragment.Dashboard;
import org.jhm69.battle_of_quiz.ui.fragment.FriendsFragment;
import org.jhm69.battle_of_quiz.ui.fragment.ProfileFragment;
import org.jhm69.battle_of_quiz.ui.fragment.SavedFragment;
import org.jhm69.battle_of_quiz.ui.fragment.ThemeBottomSheetDialog;
import org.jhm69.battle_of_quiz.ui.fragment.admin.AdminFragment;
import org.jhm69.battle_of_quiz.viewmodel.UserViewModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

/**
 * Created by jhm69
 */
public class MainActivity extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener {
    private static final int POS_DASHBOARD = 0;
    private static final int RANKING = 1;
    private static final int POS_FRIENDS = 2;
    private static final int SAVED_POST = 3;
    private static final int WEB = 4;
    private static final int ABOUT_APP = 5;
    private static final int THEME = 6;
    private static final int POS_LOGOUT = 7;
    private static final int ADMIN = 8;
    public static String userId;
    public static boolean inHome=true;
    @SuppressLint({"StaticFieldLeak", "NewApi"})
    private final Set<String> ADMIN_UID_LIST = Set.of(
            "0h9MvJiFvFWRBiOoHzUcGlqJe2m2", "eSW24hxmW6YmbaInd2OlrsWx0Rw1"
    );
    public TextView username;
    public TextView rewardTv;
    private long rewardCount;
    public Fragment mCurrentFragment;
    private Toolbar mainToolbar;
    private DrawerAdapter adapter;
    private String edit = "d";
    private UserViewModel userViewModel;
    private FirebaseUser currentuser;
    private CircleImageView imageView;
    private String[] screenTitles;
    private Drawable[] screenIcons;
    private SlidingRootNav slidingRootNav;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private RewardedAd rewardedAd;


    private void updateStatus() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        wait(10000);
                        try {
                            String refreshToken = FirebaseInstanceId.getInstance().getToken();
                            Token token = new Token(refreshToken);
                            FirebaseDatabase.getInstance().getReference("Tokens").child(userId).setValue(token);
                            long timestamp = System.currentTimeMillis();
                            HashMap<String, Object> scoreMap = new HashMap<>();
                            scoreMap.put("lastTimestamp", timestamp);
                            firestore.collection("Users")
                                    .document(userId)
                                    .update(scoreMap).addOnSuccessListener(aVoid -> {
                            });
                        } catch (NullPointerException ignored) {

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
        long newScore = rewardCount + (50);
        HashMap<String, Object> scoreMap = new HashMap<>();
        scoreMap.put("reward", newScore);
        firestore.collection("Users")
                .document(userId)
                .update(scoreMap).addOnSuccessListener(aVoid -> userViewModel.setReward((int)newScore));
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
            } catch (Exception ignored) {

            }
            if (currentuser == null) {
                LoginActivity.startActivityy(this);
                finish();
            }
        } catch (Exception ignored) {

        }
    }

    @Override
    public void onBackPressed() {
        if(inHome){
            new MaterialDialog.Builder(this)
                    .title("Exit app?")
                    .content("Are you sure want to exit?")
                    .positiveText("Yes")
                    .canceledOnTouchOutside(false)
                    .cancelable(false)
                    .onPositive((dialog, which) -> finishAffinity())
                    .negativeText("No")
                    .show();
        }else{
             showFragment(new Dashboard());
        if (slidingRootNav.isMenuOpened()) {
            slidingRootNav.closeMenu(true);
        }
        adapter.setSelected(POS_DASHBOARD);
        }

    }

    @SuppressLint("PackageManagerGetSignatures")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("Theme", Context.MODE_PRIVATE);
        String themeName = sharedPreferences.getString("ThemeName", "Default");
        if (themeName.equalsIgnoreCase("TealTheme")) {
            setTheme(R.style.TealTheme);
        } else if (themeName.equalsIgnoreCase("VioleteTheme")) {
            setTheme(R.style.VioleteTheme);
        } else if (themeName.equalsIgnoreCase("PinkTheme")) {
            setTheme(R.style.PinkTheme);
        } else if (themeName.equalsIgnoreCase("DelRio")) {
            setTheme(R.style.DelRio);
        } else if (themeName.equalsIgnoreCase("DarkTheme")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            setTheme(R.style.Dark);
        } else if (themeName.equalsIgnoreCase("Lynch")) {
            setTheme(R.style.Lynch);
        } else {
            setTheme(R.style.AppTheme);
        }




        setContentView(R.layout.activity_main);


        edit = getIntent().getStringExtra("sss");
        mainToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(mainToolbar);
        userViewModel = ViewModelProviders.of(this)
                .get(UserViewModel.class);
        updateStatus();


        firestore = FirebaseFirestore.getInstance();


        mAuth = FirebaseAuth.getInstance();
        currentuser = mAuth.getCurrentUser();


        mCurrentFragment = new Dashboard();
        if (currentuser == null) {
            LoginActivity.startActivityy(this);
            finish();
        } else {
            setUserProfile();
            MobileAds.initialize(this, initializationStatus -> {
            });
            rewardedAd = new RewardedAd(getApplicationContext(),
                    "ca-app-pub-1812307912459750/3167786256");

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
                    .addOnCompleteListener(Task::isSuccessful);
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
                        createItemFor(WEB),
                        createItemFor(ABOUT_APP),
                        createItemFor(THEME),
                        createItemFor(POS_LOGOUT),
                        createItemFor(ADMIN)));
            } else {
                adapter = new DrawerAdapter(Arrays.asList(
                        createItemFor(POS_DASHBOARD).setChecked(true),
                        createItemFor(RANKING),
                        createItemFor(POS_FRIENDS),
                        createItemFor(SAVED_POST),
                        createItemFor(WEB),
                        createItemFor(ABOUT_APP),
                        createItemFor(THEME),
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
                        // Toasty.info(MainActivity.this, "You have denied some permissions permanently, if the app force close try granting permission from settings.", Toasty.LENGTH_LONG,true).show();
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
        firestore.collection("Users")
                .document(userId)
                .update(scoreMap).addOnSuccessListener(aVoid -> {
        });
        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setIndeterminate(true);
        mDialog.setMessage("Logging you out...");
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        Map<String, Object> tokenRemove = new HashMap<>();
        try {

            firestore.collection("Users").document(userId).update(tokenRemove).addOnSuccessListener(aVoid -> {
                userViewModel.delete();
                Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).delete();
                mAuth.signOut();
                mDialog.dismiss();
                LoginActivity.startActivityy(MainActivity.this);
                finish();
            }).addOnFailureListener(e -> {
                Toasty.error(MainActivity.this, "Error logging out", Toasty.LENGTH_SHORT, true).show();
                mDialog.dismiss();
            });
        }catch (Exception f){
            userViewModel.delete();
            mAuth.signOut();
            LoginActivity.startActivityy(MainActivity.this);
            finish();
        }

    }

    @SuppressLint("SetTextI18n")
    private void setUserProfile() {
        try {
            userViewModel.user.observe(this, me -> {
                if (me == null) {
                    Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().signOut();
                    finish();
                }
                Log.d("iddr", userId);
                username = findViewById(R.id.username);
                imageView = findViewById(R.id.profile_image);
                rewardTv = findViewById(R.id.reaward);
                String nam = Objects.requireNonNull(me).getName();
                String imag = me.getImage();
                Chip add = findViewById(R.id.button4);
                add.setOnClickListener(view -> loadAd());
                username.setText(nam);
                rewardCount = me.getReward();
                Glide.with(MainActivity.this)
                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_logo))
                        .load(imag)
                        .into(imageView);
                rewardTv.setText(me.getReward() + " XP");

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

            });


        } catch (Exception ignored) {

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

    @SuppressLint("CheckResult")
    @Override
    public void onItemSelected(int position) {
        Fragment selectedScreen;
        switch (position) {

            case POS_DASHBOARD:
                inHome = true;
                mainToolbar.setSubtitle("Dashboard");
                selectedScreen = new Dashboard();
                showFragment(selectedScreen);
                slidingRootNav.closeMenu(true);
                return;

            case ADMIN:
                inHome = false;
                mainToolbar.setSubtitle("Admin");
                selectedScreen = new AdminFragment();
                showFragment(selectedScreen);
                slidingRootNav.closeMenu(true);
                return;

            case POS_FRIENDS:
                inHome = false;
                mainToolbar.setSubtitle("Friends");
                selectedScreen = new FriendsFragment();
                showFragment(selectedScreen);
                slidingRootNav.closeMenu(true);
                return;

            case RANKING:
                inHome = false;
                mainToolbar.setSubtitle("Ranking");
                selectedScreen = new Ranking();
                showFragment(selectedScreen);
                slidingRootNav.closeMenu(true);
                return;

            case SAVED_POST:
                inHome = false;
                mainToolbar.setSubtitle("Offlined Post");
                selectedScreen = new SavedFragment();
                showFragment(selectedScreen);
                slidingRootNav.closeMenu(true);
                return;

            case ABOUT_APP:
                inHome = false;
                startActivity(new Intent(getApplicationContext(), SinglePostView.class).putExtra("post_id", "about_app"));
                return;

            case WEB:
                inHome = true;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://tarok.tech"));
                startActivity(browserIntent);
                return;

            case THEME:
                inHome = true;
                new ThemeBottomSheetDialog().show(getSupportFragmentManager(), "");
                return;

          /*  case POS_SHARE:
                try {
                    doSocialShare("Share to get 20 XP", "Check out ত্বারক! Prove your skills by taking part in one to one battle of knowledge!", "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
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
                            .onPositive((dialog, which) -> {

                                logout();
                                dialog.dismiss();
                            }).negativeText("No")
                            .onNegative((dialog, which) -> dialog.dismiss()).show();
                } else {

                    new MaterialDialog.Builder(this)
                            .title("Logout")
                            .content("A error occurred while logging you out, Check your network connection and try again.")
                            .positiveText("Done")
                            .onPositive((dialog, which) -> dialog.dismiss()).show();

                }

                return;

            default:
                inHome = true;
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
        inHome = false;
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
}