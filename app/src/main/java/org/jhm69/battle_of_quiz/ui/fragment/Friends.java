package org.jhm69.battle_of_quiz.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.jhm69.battle_of_quiz.R;
import org.jhm69.battle_of_quiz.adapters.viewFriends.ViewFriendAdapter;
import org.jhm69.battle_of_quiz.models.ViewFriends;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static org.jhm69.battle_of_quiz.ui.activities.MainActivity.userId;

/**
 * Created by jhm69
 */

public class Friends extends Fragment {

    private List<ViewFriends> usersList;
    private ViewFriendAdapter usersAdapter;
    private FirebaseFirestore firestore;
    private SwipeRefreshLayout refreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_view_friends, container, false);
    }

    public void startListening() {
        usersList.clear();
        usersAdapter.notifyDataSetChanged();
        getView().findViewById(R.id.default_item).setVisibility(View.GONE);
        refreshLayout.setRefreshing(true);
        firestore.collection("Users")
                .document(userId)
                .collection("Friends")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    ViewFriends users = doc.getDocument().toObject(ViewFriends.class);
                                    usersList.add(users);
                                    usersAdapter.notifyDataSetChanged();
                                    refreshLayout.setRefreshing(false);
                                }
                            }

                            if (usersList.isEmpty()) {
                                refreshLayout.setRefreshing(false);
                                getView().findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                            }

                        } else {
                            try {
                                refreshLayout.setRefreshing(false);
                                getView().findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                            } catch (NullPointerException jh) {

                            }
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        refreshLayout.setRefreshing(false);
                        Toasty.error(getView().getContext(), "Some technical error occurred", Toasty.LENGTH_SHORT, true).show();
                        Log.w("Error", "listen:error", e);

                    }
                });
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        RecyclerView mRecyclerView = getView().findViewById(R.id.recyclerView);
        refreshLayout = getView().findViewById(R.id.refreshLayout);

        usersList = new ArrayList<>();
        usersAdapter = new ViewFriendAdapter(usersList, view.getContext());


        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(usersAdapter);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startListening();
            }
        });

        startListening();

    }
}
