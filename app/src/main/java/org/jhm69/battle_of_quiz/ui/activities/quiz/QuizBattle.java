package org.jhm69.battle_of_quiz.ui.activities.quiz;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jhm69.battle_of_quiz.R;
import org.jhm69.battle_of_quiz.notification.APIService;
import org.jhm69.battle_of_quiz.notification.Client;
import org.jhm69.battle_of_quiz.notification.MyResponse;
import org.jhm69.battle_of_quiz.notification.NotificationSender;
import org.jhm69.battle_of_quiz.models.Notification;
import org.jhm69.battle_of_quiz.models.Question;
import org.jhm69.battle_of_quiz.repository.UserRepository;
import org.jhm69.battle_of_quiz.utils.MathView;
import org.jhm69.battle_of_quiz.viewmodel.BattleViewModel;
import org.jhm69.battle_of_quiz.viewmodel.ResultViewModel;
import org.jhm69.battle_of_quiz.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.jhm69.battle_of_quiz.ui.activities.MainActivity.userId;
import static org.jhm69.battle_of_quiz.ui.activities.quiz.StepView.stepAnsList;

public class QuizBattle extends AppCompatActivity {

    public ArrayList<Boolean> SenderAnsList;
    public ArrayList<Boolean> reciverAnsList;
    public int position;

    public final int JUST_STARTED = -1;
    public final int OFFLINE_STARTED = -2;
    public final int JUST_IN = -3;
    public final int IN_STARTED = -4;
    public final int COMPLETED = -5;
    BattleViewModel battleViewModel;
    ImageView otherUserImage, thisUserImage;
    TextView thisUserName, thisUserLevel, otherUserName, otherUserLevel, thisScore, otherScore, topicTV;
    String battleIdNew, offlideID;
    LinearLayout pb;
    String thisUid, otherUid;
    String MyName, MyImage;
    String topic, subtopic;
    int question_number;
    BattleModel realBattle;
    final String timestamp = String.valueOf(System.currentTimeMillis());
    final String battleId = Long.toHexString(Double.doubleToLongBits(Math.random()));
    BattleModel offlineBattleSaving;
    ResultViewModel viewModel;
    UserViewModel userViewModel;
    private LinearLayout optionsContainer;
    private Button next;
    private int count = 0;
    private LinearLayout question;
    private ImageView mcqImg;
    private List<Question> list;
    private int score;
    private DatabaseReference mainDB;
    private StepView stepView;
    private ProgressDialog mDialog;
    private String hisName, hisImage;


    /*  public boolean isPanelShown(View view) {
          return view.getVisibility() == View.VISIBLE;
      }

      public void hideSolution(final View view, Context context) {
          TranslateAnimation animate = new TranslateAnimation(
                  0,                 // fromXDelta
                  0,                   // toXDelta
                  0,                 // fromYDelta
                  view.getHeight());           // toYDelta
          animate.setDuration(500);
          animate.setFillAfter(true);
          view.startAnimation(animate);
          view.setVisibility(View.GONE);
      }

      public void playSolution(final View view, Context context) {
          view.setVisibility(View.VISIBLE);
          TranslateAnimation animate = new TranslateAnimation(
                  0,                // fromXDelta
                  0,                  // toXDelta
                  view.getHeight(),            // fromYDelta
                  0);                // toYDelta
          animate.setDuration(500);
          animate.setFillAfter(true);
          view.startAnimation(animate);
      }
  */
    public void randomiseQuestions(DatabaseReference mainDB) {
        try {
            AsyncTask.execute(() -> mainDB.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                    long max = mutableData.getChildrenCount();
                    for (MutableData data : mutableData.getChildren()) {
                        Question node = data.getValue(Question.class);
                        if (node != null) {
                            node.setIndex((int) (Math.random() * max + 1));
                            data.setValue(node);
                        }
                    }
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                }
            }));

        } catch (Exception ignored) {

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void playAnim(final View view, final int value, final String data) {
        try {
            timeLeft(pb, list.get(position).getTime());
            next.setEnabled(false);
            if (list.get(position).getImg().length() > 10) {
                mcqImg.setVisibility(View.VISIBLE);
                Glide.with(getApplicationContext())
                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_logo_icon))
                        .load(list.get(position).getImg())
                        .into(mcqImg);
            } else {
                mcqImg.setVisibility(View.GONE);
            }
        } catch (NullPointerException ignored) {

        }
        changeColor();
        view.animate().alpha(value).scaleX(value).scaleY(value).setDuration(500).setStartDelay(100)
                .setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (value == 0 && count < 4) {
                    String option = "";
                    if (count == 0) {
                        option = list.get(position).getA();
                    } else if (count == 1) {
                        option = list.get(position).getB();
                    } else if (count == 2) {
                        option = list.get(position).getC();
                    } else if (count == 3) {
                        option = list.get(position).getD();
                    }
                    LinearLayout linearLayout = (LinearLayout) optionsContainer.getChildAt(count);
                    playAnim(linearLayout, 0, option);
                    count++;
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (value == 0) {
                    LinearLayout layout = (LinearLayout) view;
                    ((MathView) layout.getChildAt(0)).setDisplayText(data);
                    view.setTag(data);
                    playAnim(view, 1, data);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ResourceAsColor")
    private void changeColor() {
        LinearLayout op1 = (LinearLayout) optionsContainer.getChildAt(0);
        op1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#575757")));

        LinearLayout op2 = (LinearLayout) optionsContainer.getChildAt(1);
        op2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#575757")));

        LinearLayout op3 = (LinearLayout) optionsContainer.getChildAt(2);
        op3.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#575757")));

        LinearLayout op4 = (LinearLayout) optionsContainer.getChildAt(3);
        op4.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#575757")));

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void elableOption(boolean enable) {
        for (int i = 0; i < 4; i++) {
            LinearLayout linearLayout = (LinearLayout) optionsContainer.getChildAt(i);
            linearLayout.setEnabled(enable);
            if (enable) {
                LinearLayout layout = (LinearLayout) optionsContainer.getChildAt(i);
                layout.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#575757")));
            }
        }
    }

    @SuppressLint("CheckResult")
    private void loadQuestionData() {
        mDialog.show();
        if (battleIdNew != null) {
            try {
                realBattle = battleViewModel.getBattle(battleIdNew);
                topic = realBattle.topic;
                topicTV.setText(topic);
                mDialog.hide();
                Result result = viewModel.getResult(battleIdNew);
                if (result.getAction()==IN_STARTED){
                    Intent intent = new Intent(this, QuizBattle.class);
                    intent.putExtra("ofo", battleIdNew);
                    startActivity(intent);
                }else if(result.getAction()==COMPLETED){
                    Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                    intent.putExtra("resultId", battleIdNew);
                    startActivity(intent);
                }
                list = Objects.requireNonNull(realBattle).questionList;
                question_number = realBattle.questionList.size();
                if (realBattle.getWinner().length() > 4) {
                    Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                    intent.putExtra("resultId", realBattle.battleId);
                    Toasty.warning(getApplicationContext(), "You have already completed this Challenge", Toasty.LENGTH_SHORT, true);
                    startActivity(intent);
                    finish();
                }

                try {
                    playAnim(question, 0, list.get(position).getQuestion());
                } catch (NullPointerException ignored) {
                    Toasty.error(getApplicationContext(), "It seems you have already completed.xml this match", Toasty.LENGTH_SHORT, true);
                    finish();
                }
                try {
                    stepView.getState()
                            .animationType(StepView.ANIMATION_ALL)
                            .nextStepCircleEnabled(true)
                            .stepsNumber(list.size())
                            .commit();
                } catch (Exception ignored) {
                    Toasty.error(getBaseContext(), "It seems you have already completed.xml this match", Toasty.LENGTH_SHORT, true);
                    finish();
                }
                for (int i = 0; i < question_number; i++) {
                    stepAnsList.add(false);
                }
                setUserData(otherUserImage, otherUserName, otherUserLevel, realBattle.senderUid, false);
                SenderAnsList = realBattle.getSenderAnswerList();
                otherUid = realBattle.getSenderUid();
                offlineBattleSaving = realBattle;
                offlineBattleSaving.setWinner("3");

            } catch (NullPointerException h) {
                DatabaseReference mDb = FirebaseDatabase.getInstance().getReference();
                Query query = mDb.child("Play").orderByChild("battleId").equalTo(battleIdNew);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("CheckResult")
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {

                            for (DataSnapshot data : snapshot.getChildren()) {
                                realBattle = data.getValue(BattleModel.class);
                            }
                            list = Objects.requireNonNull(realBattle).questionList;
                            question_number = realBattle.questionList.size();
                            if (!realBattle.getWinner().equals("0")) {
                                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                                intent.putExtra("resultId", realBattle.battleId);
                                Toasty.warning(getApplicationContext(), "You have already completed this Challenge", Toasty.LENGTH_SHORT, true);
                                startActivity(intent);
                                finish();
                            }
                            setUserData(otherUserImage, otherUserName, otherUserLevel, realBattle.senderUid, false);
                            try {
                                playAnim(question, 0, list.get(position).getQuestion());
                            } catch (NullPointerException ignored) {
                                Toasty.error(getApplicationContext(), "It seems you have already completed.xml this match", Toasty.LENGTH_SHORT, true);
                                finish();
                            }
                            try {
                                stepView.getState()
                                        .animationType(StepView.ANIMATION_ALL)
                                        .nextStepCircleEnabled(true)
                                        .stepsNumber(list.size())
                                        .commit();
                            } catch (Exception ignored) {
                                Toasty.error(getBaseContext(), "It seems you have already completed.xml this match", Toasty.LENGTH_SHORT, true);
                                finish();
                            }

                            SenderAnsList = realBattle.senderAnswerList;
                            otherUid = realBattle.getSenderUid();
                            offlineBattleSaving = realBattle;
                            offlineBattleSaving.setWinner("3");
                        }catch (NullPointerException h){
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                for (int i = 0; i < question_number; i++) {
                    stepAnsList.add(false);
                }
            }

        } else if (offlideID != null) {
            try {
                Toast.makeText(this, "Resuming battle...", Toast.LENGTH_SHORT).show();
                realBattle = battleViewModel.getBattle(offlideID);
                mDialog.hide();
                topic = realBattle.topic;
                topicTV.setText(topic);
                if (realBattle.getWinner().equals("3")) {
                    position = realBattle.getReceiverList().size();
                    score = getScoreCount(realBattle.getReceiverList());
                    thisScore.setText(String.valueOf(score));
                    otherScore.setText("-");
                    list = Objects.requireNonNull(realBattle).questionList;
                    question_number = realBattle.questionList.size();
                    setUserData(otherUserImage, otherUserName, otherUserLevel, realBattle.getSenderUid(), false);

                    try {
                        playAnim(question, 0, list.get(position).getQuestion());
                    } catch (NullPointerException ignored) {

                    }
                    try {
                        stepView.getState()
                                .animationType(StepView.ANIMATION_ALL)
                                .nextStepCircleEnabled(true)
                                .stepsNumber(list.size())
                                .commit();
                    } catch (Exception ignored) {

                    }
                    SenderAnsList = realBattle.senderAnswerList;
                    reciverAnsList = realBattle.receiverList;
                    for (int i = 0; i < question_number; i++) {
                        stepAnsList.add(false);
                    }
                    for (int i = 0; i < position; i++) {
                        stepAnsList.add(i, realBattle.getReceiverList().get(i));
                    }
                    stepView.go(position, true, true);
                    otherUid = realBattle.getSenderUid();
                    offlineBattleSaving = realBattle;
                    offlineBattleSaving.setWinner("3");

                } else if (realBattle.getWinner().equals("2")) {

                    position = realBattle.getSenderAnswerList().size();
                    score = getScoreCount(realBattle.getSenderAnswerList());
                    thisScore.setText(String.valueOf(score));
                    list = Objects.requireNonNull(realBattle).questionList;
                    question_number = realBattle.questionList.size();
                    setUserData(otherUserImage, otherUserName, otherUserLevel, realBattle.receiverUid, false);

                    try {
                        playAnim(question, 0, list.get(position).getQuestion());
                    } catch (NullPointerException ignored) {

                    }
                    try {
                        stepView.getState()
                                .animationType(StepView.ANIMATION_ALL)
                                .nextStepCircleEnabled(true)
                                .stepsNumber(list.size())
                                .commit();
                    } catch (Exception ignored) {

                    }
                    SenderAnsList = realBattle.senderAnswerList;
                    for (int i = 0; i < question_number; i++) {
                        stepAnsList.add(false);
                    }
                    for (int i = 0; i < position; i++) {
                        stepAnsList.add(i, realBattle.getSenderAnswerList().get(i));
                    }
                    stepView.go(position, true, true);
                    otherUid = realBattle.getReceiverUid();
                    offlineBattleSaving = realBattle;
                    offlineBattleSaving.setWinner("2");
                }
            } catch (NullPointerException ignored) {
            }
        } else {
            try {
                String type = "bcs";
                type = getIntent().getStringExtra("type");
                Query query = mainDB.child("Topics").child(type).child(topic).child(subtopic)
                        .orderByChild("index")
                        .limitToFirst(question_number);
                query.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        list.add(snapshot.getValue(Question.class));
                        stepAnsList.add(false);
                        mDialog.dismiss();
                        playAnim(question, 0, list.get(position).getQuestion());
                        stepView.getState()
                                .animationType(StepView.ANIMATION_ALL)
                                .nextStepCircleEnabled(true)
                                .stepsNumber(list.size())
                                .commit();
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
                otherUid = getIntent().getStringExtra("otherUid");
                setUserData(otherUserImage, otherUserName, otherUserLevel, otherUid, false);
                offlineBattleSaving = new BattleModel(thisUid, otherUid, list, SenderAnsList, reciverAnsList, timestamp, "0", battleId, topic, false);
            } catch (NullPointerException jj) {
                Log.d("SSSF", jj.getMessage());
                finish();
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void goToNext(int which) {
        try {
            if (battleIdNew == null) {
                stepView.go(which, true, SenderAnsList.get(which - 1));
            } else {
                stepView.go(which, true, reciverAnsList.get(which - 1));
            }
            next.setEnabled(false);
            next.setAlpha(0.5f);
            elableOption(true);
            if (which >= list.size()) {
                mDialog.show();
                addToNotification();
            } else {
                playAnim(question, 0, list.get(which).getQuestion());
            }
            count = 0;
        }catch (IndexOutOfBoundsException d){
            Log.d("Errorrr", d.getMessage());
            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showResult() {
        next.setEnabled(true);
        next.setAlpha(1);
        elableOption(false);
        try {
            LinearLayout CorrectLayout = (LinearLayout) optionsContainer.getChildAt(list.get(position).getAns());
            CorrectLayout.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4BBB4F")));
        } catch (Exception ignored) {

        }

    }

    private void timeLeft(final View view, int time) {
        view.setBackground(getDrawableWithRadius());
        view.setTranslationX(-view.getWidth());
        view.animate()
                .translationXBy(view.getWidth())
                .setDuration(time * 1000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onAnimationStart(Animator animation) {
                        elableOption(true);
                        view.setVisibility(View.VISIBLE);
                    }

                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        showResult();
                    }
                });
    }

    private Drawable getDrawableWithRadius() {
        int[] colors = {Color.parseColor("#8342ED"), Color.parseColor("#5570A0")};
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        gradientDrawable.setCornerRadii(new float[]{20, 20, 20, 20, 20, 20, 20, 20});
        return gradientDrawable;
    }

    private void addScore(String uid) {
        try {
            FirebaseFirestore.getInstance().collection("Users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        int scoreOld = Objects.requireNonNull(documentSnapshot.getLong("score")).intValue();
                        int newScore = scoreOld + (-3);
                        HashMap<String, Object> scoreMap = new HashMap<>();
                        scoreMap.put("score", newScore);
                        FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(uid)
                                .update(scoreMap).addOnSuccessListener(aVoid -> new UserRepository((Application) getApplicationContext()).updateScore(-3));
                    });
        } catch (NullPointerException ignored) {

        }
    }

    public void onPause() {
        super.onPause();
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_battle);
        stepAnsList = new ArrayList<>();
        thisUid = userId;
        offlideID = getIntent().getStringExtra("ofo");
        battleIdNew = getIntent().getStringExtra("battleId");
        topic = getIntent().getStringExtra("topic");
        subtopic = getIntent().getStringExtra("subtopic");
        question_number = getIntent().getIntExtra("question_number", 5);
        mainDB = FirebaseDatabase.getInstance().getReference();
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.statusBar));
        reciverAnsList = new ArrayList<>();
        thisUserName = findViewById(R.id.thisUserName);
        viewModel = ViewModelProviders.of(this).get(ResultViewModel.class);
        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        thisUserLevel = findViewById(R.id.thisUserLevel);
        thisUserImage = findViewById(R.id.thisUserImage);
        thisScore = findViewById(R.id.myScore);
        otherUserName = findViewById(R.id.otherUserName);
        otherUserLevel = findViewById(R.id.otherUserLevel);
        otherUserImage = findViewById(R.id.otherUserImage);
        otherScore = findViewById(R.id.otherScore);
        topicTV = findViewById(R.id.topic);
        question = findViewById(R.id.questionQ);
        stepView = findViewById(R.id.step_view);
        mcqImg = findViewById(R.id.mcq_img);
        list = new ArrayList<>();
        SenderAnsList = new ArrayList<>();
        pb = findViewById(R.id.progressBar);
        next = findViewById(R.id.next);
        optionsContainer = findViewById(R.id.contaner);
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Please wait..");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        setUserData(thisUserImage, thisUserName, thisUserLevel, thisUid, true);

        //mcqAnsList = new ArrayList<>();
        position = 0;
        thisScore.setText(String.valueOf(0));
        topicTV.setText(topic);
        otherScore.setText("-");
        battleViewModel = ViewModelProviders.of(this).get(BattleViewModel.class);
        if (list.size() != 4) {
            loadQuestionData();
        } else {
            try {
                playAnim(question, 0, list.get(position).getQuestion());
            } catch (ClassCastException svs) {
                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
            }
        }
      /*  expBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPanelShown(expCard)) {
                    hideSolution(expCard, getApplicationContext());
                    expBtn.setText("Hint");
                } else {
                    expTV.setDisplayText(list.get(position).getHint());
                    playSolution(expCard, getApplicationContext());
                    expBtn.setText("Hide");
                }
            }
        });*/
        for (int i = 0; i < 4; i++) {
            final int selected = i;
            optionsContainer.getChildAt(i).setOnClickListener(v -> {
                LinearLayout selectedLayout = (LinearLayout) v;
                elableOption(false);
                next.setEnabled(true);
                next.setAlpha(1);
                if (selected == list.get(position).getAns()) {
                    score++;
                    thisScore.setText(String.valueOf(score));
                    Toasty.success(getApplicationContext(), "Correct", Toasty.LENGTH_SHORT, true).show();
                    selectedLayout.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4BBB4F")));
                    stepAnsList.add(position, true);
                    AsyncTask.execute(() -> {
                        if (battleIdNew != null) {
                            reciverAnsList.add(position, true);
                            realBattle.setReceiverList(reciverAnsList);
                            realBattle.setWinner("3");
                            battleViewModel.insert(realBattle);
                            Result myResult = new Result(realBattle.battleId, otherUid, userId, score, -2, topic, timestamp, IN_STARTED);
                            myResult.setBattleId(realBattle.battleId);
                            if (position + 1 == question_number) {
                                myResult.setAction(COMPLETED);
                                myResult.setOtherUid(realBattle.senderUid);
                                myResult.setOtherScore(getScoreCount(realBattle.senderAnswerList));
                            }
                            viewModel.insert(myResult);
                        } else if (offlideID != null) {
                            if (realBattle.getWinner().equals("2")) {
                                SenderAnsList.add(position, true);
                                offlineBattleSaving.setSenderAnswerList(SenderAnsList);
                                battleViewModel.insert(offlineBattleSaving);
                                Result myResult = new Result(offlideID, userId, otherUid, score, -2, topic, timestamp, OFFLINE_STARTED);
                                if (position + 1 == question_number)
                                    myResult.setAction(JUST_STARTED);
                                viewModel.insert(myResult);
                            } else if (realBattle.getWinner().equals("3")) {
                                reciverAnsList.add(position, true);
                                offlineBattleSaving.setSenderAnswerList(reciverAnsList);
                                battleViewModel.insert(offlineBattleSaving);
                                Result myResult = new Result(offlideID, otherUid, userId, score, -2, topic, timestamp, IN_STARTED);
                                if (position + 1 == question_number) {
                                    myResult.setAction(COMPLETED);
                                    myResult.setOtherUid(realBattle.senderUid);
                                    myResult.setOtherScore(getScoreCount(offlineBattleSaving.senderAnswerList));
                                }
                                viewModel.insert(myResult);
                            }
                        } else {
                            SenderAnsList.add(position, true);
                            offlineBattleSaving.setSenderAnswerList(SenderAnsList);
                            offlineBattleSaving.setWinner("2");
                            battleViewModel.insert(offlineBattleSaving);
                            Result myResult = new Result(battleId, userId, otherUid, score, -2, topic, timestamp, OFFLINE_STARTED);
                            if (position + 1 == question_number)
                                myResult.setAction(JUST_STARTED);
                            viewModel.insert(myResult);
                        }

                    });
                } else {
                    Toasty.error(getApplicationContext(), "Wrong", Toasty.LENGTH_SHORT, true).show();
                    selectedLayout.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D32F2F")));
                    LinearLayout CorrectLayout = (LinearLayout) optionsContainer.getChildAt(list.get(position).getAns());
                    CorrectLayout.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4BBB4F")));
                    stepAnsList.add(position, false);
                    AsyncTask.execute(() -> {
                        if (battleIdNew != null) {
                            reciverAnsList.add(position, false);
                            realBattle.setReceiverList(reciverAnsList);
                            realBattle.setWinner("3");
                            battleViewModel.insert(realBattle);
                            Result myResult = new Result(realBattle.battleId, otherUid, userId, score, -2, topic, timestamp, IN_STARTED);
                            myResult.setBattleId(realBattle.battleId);
                            if (position + 1 == question_number) {
                                myResult.setAction(COMPLETED);
                                myResult.setOtherUid(realBattle.senderUid);
                                myResult.setOtherScore(getScoreCount(realBattle.senderAnswerList));
                            }
                            viewModel.insert(myResult);
                        } else if (offlideID != null) {
                            if (realBattle.getWinner().equals("2")) {
                                SenderAnsList.add(position, false);
                                offlineBattleSaving.setSenderAnswerList(SenderAnsList);
                                battleViewModel.insert(offlineBattleSaving);
                                Result myResult = new Result(offlideID, userId, otherUid, score, -2, topic, timestamp, OFFLINE_STARTED);
                                if (position + 1 == question_number)
                                    myResult.setAction(JUST_STARTED);
                                viewModel.insert(myResult);
                            } else if (realBattle.getWinner().equals("3")) {
                                reciverAnsList.add(position, false);
                                offlineBattleSaving.setSenderAnswerList(reciverAnsList);
                                battleViewModel.insert(offlineBattleSaving);
                                Result myResult = new Result(offlideID, otherUid, userId, score, -2, topic, timestamp, IN_STARTED);
                                if (position + 1 == question_number) {
                                    myResult.setAction(COMPLETED);
                                    myResult.setOtherUid(realBattle.senderUid);
                                    myResult.setOtherScore(getScoreCount(offlineBattleSaving.senderAnswerList));
                                }
                                viewModel.insert(myResult);
                            }
                        } else {
                            SenderAnsList.add(position, false);
                            offlineBattleSaving.setSenderAnswerList(SenderAnsList);
                            offlineBattleSaving.setWinner("2");
                            battleViewModel.insert(offlineBattleSaving);
                            Result myResult = new Result(battleId, userId, otherUid, score, -2, topic, timestamp, OFFLINE_STARTED);
                            if (position + 1 == question_number)
                                myResult.setAction(JUST_STARTED);
                            viewModel.insert(myResult);
                        }

                    });
                }
            });
        }
        next.setOnClickListener(v -> {
            if (position < 1) {
                addScore(userId);
                Toast.makeText(getApplicationContext(), "3 point reduced", Toast.LENGTH_SHORT).show();
            }
            position++;
            goToNext(position);
        });
        MobileAds.initialize(this, initializationStatus -> {
        });

        AdView adView = new AdView(this);

        adView.setAdSize(AdSize.BANNER);

        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");

        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void addScore(int score, String uid, String what) {
        if (what.equals("win")) userViewModel.updateWin(score);
        if (what.equals("lose")) userViewModel.updateLose(score);
        if (what.equals("draw")) userViewModel.updateDraw(score);
        if (what.equals("score")) userViewModel.updateScore(score);
        FirebaseFirestore.getInstance().collection("Users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    int scoreOld = Objects.requireNonNull(documentSnapshot.getLong(what)).intValue();
                    int newScore = scoreOld + (score);
                    HashMap<String, Object> scoreMap = new HashMap<>();
                    scoreMap.put(what, newScore);
                    FirebaseFirestore.getInstance()
                            .collection("Users")
                            .document(uid)
                            .update(scoreMap).addOnSuccessListener(aVoid -> {
                            });
                });
    }

    private void setUserData(ImageView proPic, TextView name, TextView level, String uid, boolean me) {
        try {
            if (me) {
                userViewModel.user.observe(this, me1 -> {
                    MyName = me1.getUsername();
                    MyImage = me1.getImage();
                    name.setText(MyName);
                    setLevelByScore(level, (int) me1.getScore());
                    Glide.with(getApplicationContext())
                            .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_logo_icon))
                            .load(MyImage)
                            .into(proPic);
                });
            } else {
                FirebaseFirestore.getInstance().collection("Users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            hisName = documentSnapshot.getString("username");
                            name.setText(hisName);
                            setLevelByScore(level, Integer.parseInt(String.valueOf(documentSnapshot.getLong("score"))));
                            hisImage = documentSnapshot.getString("image");
                            Glide.with(getApplicationContext())
                                    .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_logo_icon))
                                    .load(hisImage)
                                    .into(proPic);
                        });
            }
        } catch (NullPointerException ignored) {

        }
    }

    private void addToNotification() {
        if (battleIdNew != null) {
            Notification notification = new Notification(realBattle.senderUid, MyName, MyImage,
                    "accepted your challenge in " + realBattle.topic,
                    String.valueOf(System.currentTimeMillis()),
                    "play_result"
                    , realBattle.battleId);
            new QuizBattle.SendNotificationAsyncTask(notification).execute();
            //realBattle means data with including second player
            FirebaseFirestore.getInstance().collection("Users")
                    .document(otherUid)
                    .collection("Info_Notifications")
                    .add(notification)
                    .addOnSuccessListener(documentReference -> {
                        //declaring ultimate/final battleResult
                        thisUid = realBattle.senderUid;
                        otherUid = userId;
                        BattleModel battle = new BattleModel(thisUid, otherUid, list, SenderAnsList, reciverAnsList, timestamp, "0", realBattle.battleId, realBattle.getTopic(), true);
                        //here thisUid (who invited to play) and other is player 2 who got invited and playing now
                        //none of SenderAnsList(inviter) and receiverAnsList(this) is null
                        mainDB = FirebaseDatabase.getInstance().getReference();
                        //Result senderResult = new Result(battleId, battle.senderUid, userId, getScoreCount(SenderAnsList), getScoreCount(reciverAnsList), topic, timestamp, COMPLETED);
                        //FirebaseDatabase.getInstance().getReference().child("Result").child(battle.senderUid).child(battle.battleId).setValue(senderResult);
                        mainDB.child("Play").child(realBattle.battleId).setValue(battle).addOnSuccessListener(aVoid -> {
                            Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                            UpdateFinalResult(battle);
                            intent.putExtra("FinalResult", battle);
                            startActivity(intent);
                            mDialog.dismiss();
                            finish();
                        });
                    });

        } else if (offlideID != null) {
            if (offlineBattleSaving.getWinner().equals("2")) {
                Notification notification = new Notification(otherUid, MyName, MyImage,
                        "challenged you in " + topic,
                        String.valueOf(System.currentTimeMillis()),
                        "play"
                        , battleId);
                new QuizBattle.SendNotificationAsyncTask(notification).execute();
                updateXP(-5);
                FirebaseFirestore.getInstance().collection("Users")
                        .document(otherUid)
                        .collection("Info_Notifications")
                        .add(notification)
                        .addOnSuccessListener(documentReference -> {
                            thisUid = userId;
                            BattleModel battleModel;
                            battleModel = offlineBattleSaving;
                            battleModel.setSenderAnswerList(SenderAnsList);
                            battleModel.setWinner("0");
                            battleViewModel.insert(battleModel);
                            Result hisResult = new Result(offlineBattleSaving.battleId, battleModel.getReceiverUid(), userId, getScoreCount(SenderAnsList), -1, topic, timestamp, JUST_IN);
                            FirebaseDatabase.getInstance().getReference().child("Result").child(otherUid).child(battleId).setValue(hisResult);
                            //here thisUid is player who sending invitations and other uid is whom he sending invite
                            //so senderResult is NonNull and reciverAnsList is Null as he still not able to play.
                            mainDB = FirebaseDatabase.getInstance().getReference();
                            mainDB.child("Play").child(battleId).setValue(battleModel).addOnSuccessListener(aVoid -> {
                                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                                intent.putExtra("start", battleModel);
                                startActivity(intent);
                                mDialog.dismiss();
                                finish();
                            });
                        });
            } else if (offlineBattleSaving.getWinner().equals("3")) {
                Notification notification = new Notification(realBattle.senderUid, MyName, MyImage,
                        "accepted your challenge in " + realBattle.topic,
                        String.valueOf(System.currentTimeMillis()),
                        "play_result"
                        , realBattle.battleId);
                new QuizBattle.SendNotificationAsyncTask(notification).execute();
                FirebaseFirestore.getInstance().collection("Users")
                        .document(otherUid)
                        .collection("Info_Notifications")
                        .add(notification)
                        .addOnSuccessListener(documentReference -> {
                            //declaring ultimate/final battleResult
                            thisUid = realBattle.senderUid;
                            otherUid = userId;
                            BattleModel battle = new BattleModel(thisUid, otherUid, list, SenderAnsList, reciverAnsList, timestamp, "0", realBattle.battleId, realBattle.topic, true);
                            //here thisUid (who invited to play) and other is player 2 who got invited and playing now
                            //none of SenderAnsList(inviter) and receiverAnsList(this) is null
                            mainDB = FirebaseDatabase.getInstance().getReference();
                            // Result senderResult = new Result(realBattle.battleId, realBattle.senderUid, userId, getScoreCount(realBattle.getSenderAnswerList()), getScoreCount(reciverAnsList), topic, timestamp, COMPLETED);
                            // FirebaseDatabase.getInstance().getReference().child("Result").child(realBattle.senderUid).child(realBattle.battleId).setValue(senderResult);
                            mainDB.child("Play").child(realBattle.battleId).setValue(battle).addOnSuccessListener(aVoid -> {
                                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                                UpdateFinalResult(battle);
                                intent.putExtra("FinalResult", battle);
                                startActivity(intent);
                                mDialog.dismiss();
                                finish();
                            });
                        })
                        .addOnFailureListener(e -> Log.e("Error", Objects.requireNonNull(e.getLocalizedMessage())));
            }

        } else {
            Notification notification = new Notification(otherUid, MyName, MyImage,
                    "challenged you in " + topic,
                    String.valueOf(System.currentTimeMillis()),
                    "play"
                    , battleId);
            new QuizBattle.SendNotificationAsyncTask(notification).execute();
            updateXP(-5);
            FirebaseFirestore.getInstance().collection("Users")
                    .document(otherUid)
                    .collection("Info_Notifications")
                    .add(notification)
                    .addOnSuccessListener(documentReference -> {
                        Result hisResult = new Result(offlineBattleSaving.battleId, offlineBattleSaving.getReceiverUid(), userId, getScoreCount(SenderAnsList), -1, topic, timestamp, JUST_IN);
                        FirebaseDatabase.getInstance().getReference().child("Result").child(offlineBattleSaving.getReceiverUid()).child(battleId).setValue(hisResult);
                        mainDB.child("Play").child(offlineBattleSaving.battleId).setValue(offlineBattleSaving).addOnSuccessListener(aVoid -> {
                            Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                            intent.putExtra("start", offlineBattleSaving);
                            randomiseQuestions(mainDB.child("Topics").child(topic).child(subtopic));
                            startActivity(intent);
                            mDialog.dismiss();
                            finish();
                        });
                    });
        }
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
    private void UpdateFinalResult(BattleModel battlePlay) {
        int reScore, seScore;
        try {
            topic = battlePlay.topic;
        } catch (NullPointerException ignored) {

        }
        reScore = getScoreCount(battlePlay.receiverList); //me 2nd player
        seScore = getScoreCount(battlePlay.senderAnswerList); //inviter- 1st player
        thisScore.setText(String.valueOf(reScore));
        otherScore.setText(String.valueOf(seScore));
        if (reScore > seScore) {
            updateXP(20);
            Toasty.success(getApplicationContext(), "Congo, you got 5 XP and 5 point", Toasty.LENGTH_LONG);
            addScore(5, battlePlay.receiverUid, "score");
            addScore(1, battlePlay.receiverUid, "win");
            addScore(1, battlePlay.senderUid, "lose");
            Map<String, Object> winner = new HashMap<>();
            winner.put("winner", battlePlay.receiverUid);
            battleViewModel.insert(battlePlay);
            FirebaseDatabase.getInstance().getReference().child("Play").child(battlePlay.battleId).updateChildren(winner);
        } else if (seScore > reScore) {
            Toasty.error(getApplicationContext(), "Damn, you lost 5 point", Toasty.LENGTH_LONG);
            addScore(1, battlePlay.senderUid, "win");
            addScore(5, battlePlay.senderUid, "score");
            addScore(1, battlePlay.receiverUid, "lose");
            Map<String, Object> winner = new HashMap<>();
            winner.put("winner", battlePlay.senderUid);
            battleViewModel.insert(battlePlay);
            FirebaseDatabase.getInstance().getReference().child("Play").child(battlePlay.battleId).updateChildren(winner);
        } else {
            addScore(2, battlePlay.receiverUid, "score");
            addScore(2, battlePlay.senderUid, "score");
            Toasty.info(getApplicationContext(), "Match Drawn", Toasty.LENGTH_LONG);
            addScore(1, battlePlay.receiverUid, "draw");
            addScore(1, battlePlay.senderUid, "draw");
            Map<String, Object> winner = new HashMap<>();
            winner.put("winner", "draw");
            battleViewModel.insert(battlePlay);
            FirebaseDatabase.getInstance().getReference().child("Play").child(battlePlay.battleId).updateChildren(winner);
        }
    }

    @Override
    public void onBackPressed() {
        new MaterialDialog.Builder(this)
                .title("Quite?")
                .content("Are you sure want to quite and go back?")
                .positiveText("Yes")
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .onPositive((dialog, which) -> finish())
                .negativeText("No")
                .show();
    }

    @SuppressLint("SetTextI18n")
    public void setLevelByScore(TextView levelTV, int score) {
        if (score <= 500) {
            levelTV.setText("Level: 1");
        } else if (score <= 1000) {
            levelTV.setText("Level: 2");
        } else if (score <= 1500) {
            levelTV.setText("Level: 3");
        } else if (score <= 2000) {
            levelTV.setText("Level: 4");
        } else if (score <= 2500) {
            levelTV.setText("Level: 5");
        } else if (score <= 3500) {
            levelTV.setText("Level: 6");
        } else if (score <= 5000) {
            levelTV.setText("Level: 7");
        } else {
            levelTV.setText("Level: unknown");
        }
    }

    private void updateXP(int score) {
        FirebaseFirestore.getInstance().collection("Users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    try {
                        int scoreOld = Objects.requireNonNull(documentSnapshot.getLong("reward")).intValue();
                        int newScore = scoreOld + (score);
                        HashMap<String, Object> scoreMap = new HashMap<>();
                        scoreMap.put("reward", newScore);
                        FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(userId)
                                .update(scoreMap).addOnSuccessListener(aVoid -> {
                                    userViewModel.updateXp(score);
                                    //Toast.makeText(, "", Toast.LENGTH_SHORT).show();
                                    //Toasty.success(getApplicationContext(), "Congratulations, You have got 10 reward", Toasty.LENGTH_SHORT, true);
                                });
                    } catch (NullPointerException ignored) {
                    }
                });

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