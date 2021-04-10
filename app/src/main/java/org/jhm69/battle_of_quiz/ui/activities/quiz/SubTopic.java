package org.jhm69.battle_of_quiz.ui.activities.quiz;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import org.jhm69.battle_of_quiz.R;
import org.jhm69.battle_of_quiz.adapters.SubTopicAdapter;
import org.jhm69.battle_of_quiz.models.SubTopicModel;

import java.util.ArrayList;
import java.util.List;

public class SubTopic extends AppCompatActivity {
    final List<SubTopicModel> subTopics = new ArrayList<>();
    String topic, otherUid, type;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private SubTopicAdapter capterAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_parent_topic);
        ProgressBar loading = findViewById(R.id.progressBar2);
        topic = getIntent().getStringExtra("topic");
        type = getIntent().getStringExtra("type");
        otherUid = getIntent().getStringExtra("otherUid");
        recyclerView = findViewById(R.id.rcv);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.statusBar));
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(capterAdapter);
        recyclerView.setHasFixedSize(true);
        Log.d("Typee-SubTopic", type);
        FirebaseDatabase.getInstance().getReference().child("Topics").child(type).child(topic).child("all_totpics").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                SubTopicModel subTopic = snapshot.getValue(SubTopicModel.class);
                subTopics.add(subTopic);
                capterAdapter = new SubTopicAdapter(subTopics, topic, SubTopic.this, otherUid, type);
                recyclerView.setAdapter(capterAdapter);
                capterAdapter.notifyDataSetChanged();
                loading.setVisibility(View.GONE);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}
