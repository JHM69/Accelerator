package org.jhm69.battle_of_quiz.ui.fragment;

import static org.jhm69.battle_of_quiz.ui.activities.MainActivity.userId;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.jhm69.battle_of_quiz.R;
import org.jhm69.battle_of_quiz.adapters.UsersAdapter;
import org.jhm69.battle_of_quiz.models.Users;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

/**
 * Created by jhm69
 */

public class SendMessage extends Fragment {

    private List<Users> usersList;
    private UsersAdapter usersAdapter;
    private FirebaseFirestore firestore;
    private SwipeRefreshLayout refreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.send_message_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        RecyclerView mRecyclerView = view.findViewById(R.id.messageList);
        refreshLayout = view.findViewById(R.id.refreshLayout);

        usersList = new ArrayList<>();
        usersAdapter = new UsersAdapter(usersList, view.getContext());
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(usersAdapter);

        refreshLayout.setOnRefreshListener(() -> {
            usersList.clear();
            usersAdapter.notifyDataSetChanged();
            startListening();
        });
        usersList.clear();
        usersAdapter.notifyDataSetChanged();
        startListening();

    }

    public void startListening() {
        Objects.requireNonNull(getView()).findViewById(R.id.default_item).setVisibility(View.GONE);
        refreshLayout.setRefreshing(true);
        firestore.collection("Users")
                .document(userId)
                .collection("Friends")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                Users users = doc.getDocument().toObject(Users.class);
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
                        getView().findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                        refreshLayout.setRefreshing(false);
                    }

                })
                .addOnFailureListener(e -> {
                    Toasty.error(getView().getContext(), "Some technical error occurred", Toasty.LENGTH_SHORT, true).show();
                    refreshLayout.setRefreshing(false);
                    Log.w("Error", "listen:error", e);
                });
    }

}
