package org.jhm69.battle_of_quiz.ui.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.jhm69.battle_of_quiz.R;
import org.jhm69.battle_of_quiz.models.Notification;
import org.jhm69.battle_of_quiz.ui.activities.notification.NotificationFragment;
import org.jhm69.battle_of_quiz.ui.activities.quiz.BattleModel;
import org.jhm69.battle_of_quiz.ui.activities.quiz.Result;
import org.jhm69.battle_of_quiz.viewmodel.BattleViewModel;
import org.jhm69.battle_of_quiz.viewmodel.ResultViewModel;
import org.jhm69.battle_of_quiz.viewmodel.UserViewModel;

import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
import static org.jhm69.battle_of_quiz.adapters.ResultAdapter.COMPLETED;
import static org.jhm69.battle_of_quiz.ui.activities.MainActivity.userId;

/**
 * Created by jhm69
 */

public class Dashboard extends Fragment {
    private final int[] tabIcons = {
            R.drawable.ic_flash_on_black_24dp,
            R.drawable.ic_notes_black_24dp,
            R.drawable.ic_notifications_black_24dp
    };
    private TabAdapter adapter;
    private TabLayout tabLayout;
    private BattleViewModel battleViewModel;
    private ResultViewModel viewModel;
    private int nSize;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_dashboard, container, false);
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabLayout = Objects.requireNonNull(getView()).findViewById(R.id.tabLayout);
        ViewPager viewPager = getView().findViewById(R.id.view_pager);
        adapter = new TabAdapter(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), getActivity());
        adapter.addFragment(new Quiz(), "Quiz", tabIcons[0]);
        adapter.addFragment(new Home(), "Posts", tabIcons[1]);
        adapter.addFragment(new NotificationFragment(), "Notification", tabIcons[2]);

        viewPager.setAdapter(adapter);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            Objects.requireNonNull(tab).setCustomView(null);
            tab.setCustomView(adapter.getTabView(i));
        }
        tabLayout.setupWithViewPager(viewPager);

        highLightCurrentTab(0);
        try {
            viewPager.setOffscreenPageLimit(adapter.getCount());
        } catch (IllegalStateException ignored) {

        }


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                highLightCurrentTab(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        viewModel = ViewModelProviders.of(getActivity()).get(ResultViewModel.class);
        battleViewModel = ViewModelProviders.of(getActivity()).get(BattleViewModel.class);

        int count = Objects.requireNonNull(getActivity()).getSharedPreferences("Notifications", MODE_PRIVATE).getInt("count", 0);

        AsyncTask.execute(() -> FirebaseFirestore.getInstance().collection("Users")
                .document(userId)
                .collection("Info_Notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING).limit(7)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    nSize = queryDocumentSnapshots.size();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                            if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                Notification notification = documentChange.getDocument().toObject(Notification.class).withId(documentChange.getDocument().getId());
                                if (notification.getType().equals("play")) {
                                    if (!viewModel.resultExistsForSecondPlayer(notification.getAction_id())) {
                                        new PlayAsyncTask(viewModel, battleViewModel, notification.getAction_id()).execute("play");
                                    }
                                } else if (notification.getType().equals("play_result")) {
                                    if (!viewModel.resultExists(notification.getAction_id())) {
                                        new PlayResultAsyncTask(viewModel, battleViewModel, notification.getAction_id()).execute("play_result");
                                    }
                                }
                            }
                        }
                    }
                }));

        if (nSize>count) {
            Log.d("nCount", nSize+"  "+count);
            TabLayout.Tab tab = tabLayout.getTabAt(2);
            Objects.requireNonNull(tab).setCustomView(null);
            tab.setCustomView(adapter.setNotifications(nSize-count));
        }  // badge_count.setVisibility(View.GONE);

    }

    private void highLightCurrentTab(int position) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            assert tab != null;
            tab.setCustomView(null);
            tab.setCustomView(adapter.getTabView(i));
        }
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#BCA9DA"));
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        assert tab != null;
        tab.setCustomView(null);
        tab.setCustomView(adapter.getSelectedTabView(position));
    }

    private int getScoreCount(List<Boolean> scoreList) {
        int score = 0;
        for (int i = 0; i < scoreList.size(); i++) {
            if (scoreList.get(i)) {
                score++;
            }
        }
        return score;
    }

    @SuppressLint("CheckResult")
    private void updateScore() {
        try {
            UserViewModel userViewModel = ViewModelProviders.of(requireActivity()).get(UserViewModel.class);
            FirebaseFirestore.getInstance().collection("Users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        int score = Objects.requireNonNull(documentSnapshot.getLong("score")).intValue();
                        int reward = Objects.requireNonNull(documentSnapshot.getLong("reward")).intValue();

                        int win = Integer.parseInt(Objects.requireNonNull(documentSnapshot.get("win")).toString());
                        int lose = Integer.parseInt(Objects.requireNonNull(documentSnapshot.get("lose")).toString());
                        int draw = Integer.parseInt(Objects.requireNonNull(documentSnapshot.get("draw")).toString());

                        userViewModel.setWin(win);
                        userViewModel.setLose(lose);
                        userViewModel.setDraw(draw);
                        userViewModel.setScore(score);
                        userViewModel.setXp(reward);
                    });
        }catch (Exception ignored){

        }
    }

    @SuppressLint("StaticFieldLeak")
    private static class PlayAsyncTask extends AsyncTask<String, Void, Void> {
        final ResultViewModel viewModel;
        final BattleViewModel battleViewModel;
        final String id;

        public PlayAsyncTask(ResultViewModel viewModel, BattleViewModel battleViewModel, String id) {
            this.viewModel = viewModel;
            this.battleViewModel = battleViewModel;
            this.id = id;
        }

        @Override
        protected Void doInBackground(String... type) {
            final BattleModel[] battlep = {null};
            DatabaseReference mDb = FirebaseDatabase.getInstance().getReference();
            com.google.firebase.database.Query query = mDb.child("Play").orderByChild("battleId").equalTo(id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        battlep[0] = data.getValue(BattleModel.class);
                    }
                    if (battlep[0] != null) {
                        com.google.firebase.database.Query query = mDb.child("Result").child(userId).orderByChild("battleId").equalTo(id);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Result result = null;
                                for (DataSnapshot data : snapshot.getChildren()) {
                                    result = data.getValue(Result.class);
                                }
                                battleViewModel.insert(battlep[0]);
                                viewModel.insert(result);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
            return null;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class PlayResultAsyncTask extends AsyncTask<String, Void, Void> {
        final ResultViewModel viewModel;
        final BattleViewModel battleViewModel;
        final String id;

        public PlayResultAsyncTask(ResultViewModel viewModel, BattleViewModel battleViewModel, String id) {
            this.viewModel = viewModel;
            this.battleViewModel = battleViewModel;
            this.id = id;
        }

        @Override
        protected Void doInBackground(String... type) {
            final BattleModel[] battlep = {null};
            DatabaseReference mDb = FirebaseDatabase.getInstance().getReference();
            com.google.firebase.database.Query query = mDb.child("Play").orderByChild("battleId").equalTo(id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        battlep[0] = data.getValue(BattleModel.class);
                    }
                    Log.d("Reesult: ", "Downloaded Battle");
                    if (battlep[0] != null) {
                        try {
                            Result result = new Result(battlep[0].getBattleId(), battlep[0].getSenderUid(), battlep[0].getReceiverUid(), getScoreCount(battlep[0].getSenderAnswerList()), getScoreCount(battlep[0].getReceiverList()), battlep[0].topic, battlep[0].getTimestamp(), COMPLETED);
                            Log.d("Reesult: ", "Downloaded Result");
                            battleViewModel.insert(battlep[0]);
                            Log.d("Reesult: ", "Inserted Battle and Inserting Result also updating score");
                            Log.d("Reesult: ", ReflectionToStringBuilder.toString(result));
                            viewModel.insert(result);
                            updateScore();
                            Log.d("Reesult: ", "Score updated and inserting Result");
                        }catch (Exception ignored){

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
            return null;
        }
    }

 /*   private void checkFriendRequest() {
        request_alert = getView().findViewById(R.id.friend_req_alert);
        request_alert_text = getView().findViewById(R.id.friend_req_alert_text);
        FirebaseFirestore.getInstance().collection("Users")
                .document(userId)
                .collection("Friend_Requests")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }

                    if (!queryDocumentSnapshots.isEmpty()) {
                        try {
                            request_alert.setVisibility(View.VISIBLE);
                            request_alert_text.setText(String.format("You have new Friend Request", queryDocumentSnapshots.size()));
                            request_alert.animate()
                                    .setDuration(300)
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .alpha(1.0f)
                                    .start();

                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }

                });
    }*/
}

