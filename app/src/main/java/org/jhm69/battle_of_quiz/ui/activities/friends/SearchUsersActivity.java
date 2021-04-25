package org.jhm69.battle_of_quiz.ui.activities.friends;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import org.jhm69.battle_of_quiz.R;
import org.jhm69.battle_of_quiz.adapters.PostViewHolder;
import org.jhm69.battle_of_quiz.adapters.searchFriends.SearchFriendAdapter;
import org.jhm69.battle_of_quiz.models.Friends;
import org.jhm69.battle_of_quiz.models.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class SearchUsersActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private ArrayList<Friends> usersList;
    private SearchFriendAdapter usersAdapter;
    private ListenerRegistration mRegistration;
    private ProgressDialog dialog;
    private String searchTextData;
    String tag="User";
    RecyclerView mRecyclerView;
    SearchUsersActivity.PostPhotosAdapter postPhotosAdapter;
    private View statsheetView;
    private BottomSheetDialog mmBottomSheetDialog;
    private List<Post> posts;
    private PostViewHolder postViewHolder;
    public static void startActivity(Activity activity, Context context, View view) {
        Intent intent = new Intent(context, SearchUsersActivity.class);

        if (Build.VERSION.SDK_INT >= 21) {
            String transitionName = "search";
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                            view,   // Starting view
                            transitionName    // The String
                    );
            ActivityCompat.startActivity(context, intent, options.toBundle());

        } else {
            context.startActivity(intent);
        }

    }

    public void stopListening() {
        if (mRegistration != null) {
            mRegistration.remove();
            mRegistration = null;
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopListening();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopListening();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void startListeningUser(String searchQuery) {
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Searching....");
        dialog.show();
        usersList.clear();
        mRecyclerView.setAdapter(usersAdapter);
        Query mQuery = mFirestore.collection("Users");
        mQuery.get().addOnSuccessListener(documentSnapshots -> {
            try {
                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                    final String docId = doc.getDocument().getId();
                    if (!docId.equals(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())) {
                        mFirestore.collection("Users").document(docId).get().addOnSuccessListener(documentSnapshot -> {
                            if (Objects.requireNonNull(documentSnapshot.getString("name")).toLowerCase().contains(searchQuery.toLowerCase())) {
                                Friends friends = documentSnapshot.toObject(Friends.class);
                                usersList.add(friends);
                                usersAdapter.notifyDataSetChanged();
                            }
                            dialog.dismiss();
                        });

                    }
                }
                dialog.dismiss();
            }catch (Exception d){
                Toasty.error(getApplicationContext(), "No result found", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }
    private void startListeningPost(String searchQuery) {
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Searching Post....");
        dialog.show();
        posts.clear();
        mRecyclerView.setAdapter(postPhotosAdapter);
        Query mQuery = mFirestore.collection("Posts");
        mQuery.get().addOnSuccessListener(documentSnapshots -> {
            try {
                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                    final String docId = doc.getDocument().getId();
                    if (!docId.equals(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())) {
                        mFirestore.collection("Posts").document(docId).get().addOnSuccessListener(documentSnapshot -> {
                            if (Objects.requireNonNull(documentSnapshot.getString("description")).toLowerCase().contains(searchQuery.toLowerCase())) {
                                Post post = documentSnapshot.toObject(Post.class);
                                posts.add(post);
                                postPhotosAdapter.postListNow = posts;
                                mRecyclerView.setAdapter(postPhotosAdapter);
                                postPhotosAdapter.notifyDataSetChanged();
                            }
                            dialog.dismiss();
                        });

                    }
                }

                dialog.dismiss();
            }catch (Exception h){
                dialog.dismiss();
                Toasty.error(getApplicationContext(), "Error", R.drawable.ic_error_outline_white_24dp).show();
            }
        });
    }

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccentt));
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.statusBar));

        // getActionBar().setHomeButtonEnabled(true);
        // getActionBar().setDisplayHomeAsUpEnabled(true);

        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Color.parseColor("#212121"));
        }

        mRecyclerView = findViewById(R.id.usersList);
        //  searchText = findViewById(R.id.searchText);
        postPhotosAdapter = new SearchUsersActivity.PostPhotosAdapter(posts);
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        // fab = findViewById(R.id.search_button);
        usersList = new ArrayList<>();
        posts = new ArrayList<>();
        usersAdapter = new SearchFriendAdapter(usersList, this);
        statsheetView = SearchUsersActivity.this.getLayoutInflater().inflate(R.layout.stat_bottom_sheet_dialog, null);
        mmBottomSheetDialog = new BottomSheetDialog(getApplicationContext());
        mmBottomSheetDialog.setContentView(statsheetView);
        mmBottomSheetDialog.setCanceledOnTouchOutside(true);

        ChipGroup chipGroup = findViewById(R.id.filter_chip_SS_group);
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            try {
                Chip c = findViewById(checkedId);
                tag = c.getText().toString();
                if(tag.equals("Post")){
                    startListeningPost(searchTextData);
                }else if(tag.equals("User")){
                    startListeningUser(searchTextData);
                }
            } catch (NullPointerException ignored) {

            }
        });

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(usersAdapter);

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


    @SuppressLint("NewApi")
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notifications, menu);
        MenuItem search_data = menu.findItem(R.id.action_search);
        search_data.expandActionView();

        SearchView searchView = (SearchView) search_data.getActionView();
        searchView.setIconifiedByDefault(true);
        searchView.setFocusable(true);
        searchView.setIconified(false);

        searchView.requestFocusFromTouch();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.white));
        searchEditText.setHintTextColor(getResources().getColor(R.color.white));
        searchView.setVisibility(View.VISIBLE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchTextData = query;
                if(tag.equals("Post")){
                    startListeningPost(searchTextData);
                }else if(tag.equals("User")){
                    startListeningUser(searchTextData);
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
    class PostPhotosAdapter extends RecyclerView.Adapter<PostViewHolder> {
        private List<Post> postListNow;
        public PostPhotosAdapter(List<Post> post) {
            this.postListNow = post;
        }

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_feed_post, parent, false);
            return new PostViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
            try {
                Post post1 = postListNow.get(position);
                holder.bind(post1, holder, position, mmBottomSheetDialog, statsheetView, true);
            } catch (IndexOutOfBoundsException ignored) {

            }
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }
    }
}
