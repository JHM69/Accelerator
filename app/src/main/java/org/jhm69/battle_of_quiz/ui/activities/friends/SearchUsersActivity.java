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
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.jhm69.battle_of_quiz.R;
import org.jhm69.battle_of_quiz.adapters.searchFriends.SearchFriendAdapter;
import org.jhm69.battle_of_quiz.models.Friends;

import java.util.ArrayList;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class SearchUsersActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private ArrayList<Friends> usersList;
    private SearchFriendAdapter usersAdapter;
    private ListenerRegistration mRegistration;
    private ProgressDialog dialog;

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

    private void startListening(String searchQuery) {
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Searching....");
        dialog.show();
        usersList.clear();
        Query mQuery = mFirestore.collection("Users");
        mQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                    final String docId = doc.getDocument().getId();
                    if (!docId.equals(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())) {
                        mFirestore.collection("Users").document(docId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (Objects.requireNonNull(documentSnapshot.getString("name")).toLowerCase().contains(searchQuery.toLowerCase())) {
                                    Friends friends = documentSnapshot.toObject(Friends.class);
                                    usersList.add(friends);
                                    usersAdapter.notifyDataSetChanged();
                                } else {
                                    Toasty.error(getApplicationContext(), "No Result Found", R.drawable.ic_error_outline_white_24dp).show();
                                }
                                dialog.dismiss();
                            }
                        });

                    }
                }
                dialog.dismiss();
            }
        });
    }


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

        RecyclerView mRecyclerView = findViewById(R.id.usersList);
        //  searchText = findViewById(R.id.searchText);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        // fab = findViewById(R.id.search_button);
        usersList = new ArrayList<>();
        usersAdapter = new SearchFriendAdapter(usersList, this, findViewById(R.id.layout));


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
//        search_data.expandActionView();
//        search_data.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        SearchView searchView = (SearchView) search_data.getActionView();
        searchView.setSubmitButtonEnabled(true);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setIconifiedByDefault(false);
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.white));
        searchEditText.setHintTextColor(getResources().getColor(R.color.white));
        searchView.setVisibility(View.VISIBLE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startListening(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

}
