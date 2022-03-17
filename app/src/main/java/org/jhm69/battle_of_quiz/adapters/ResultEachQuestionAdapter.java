package org.jhm69.battle_of_quiz.adapters;import android.animation.Animator;import android.annotation.SuppressLint;import android.content.Context;import android.content.res.ColorStateList;import android.graphics.Color;import android.view.LayoutInflater;import android.view.View;import android.view.ViewGroup;import android.view.animation.DecelerateInterpolator;import android.widget.ImageView;import android.widget.LinearLayout;import android.widget.TextView;import androidx.annotation.NonNull;import androidx.recyclerview.widget.RecyclerView;import com.skydoves.expandablelayout.ExpandableLayout;import org.jhm69.battle_of_quiz.R;import org.jhm69.battle_of_quiz.models.Question;import org.jhm69.battle_of_quiz.models.QuestionEachResult;import org.jhm69.battle_of_quiz.utils.MathView;import java.util.List;public class ResultEachQuestionAdapter extends RecyclerView.Adapter<ResultEachQuestionAdapter.MyViewHolder> {    final List<QuestionEachResult> questionEachResults;    //final BottomSheetDialog mBottomSheetDialog;    int count;    final Context context;    int lastPosition = -1;    boolean expanded = true;    @SuppressLint("InflateParams")    public ResultEachQuestionAdapter(List<QuestionEachResult> questionEachResults, Context context) {        this.questionEachResults = questionEachResults;       // mBottomSheetDialog = new BottomSheetDialog(context);        this.context = context;    }    @NonNull    @Override    public ResultEachQuestionAdapter.MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.question_top_layout, viewGroup, false);        return new MyViewHolder(view);    }    @Override    public void onBindViewHolder(@NonNull ResultEachQuestionAdapter.MyViewHolder viewHolder, @SuppressLint("RecyclerView") int i) {        QuestionEachResult q = questionEachResults.get(i);        try {            if (q.getLeftSide()) {                viewHolder.thisLeftUser.setBackgroundResource(R.drawable.ic_baseline_check_24);            } else {                viewHolder.thisLeftUser.setBackgroundResource(R.drawable.ic_baseline_clear_24);            }        } catch (NullPointerException ignored) {        }        try {            if (q.getRightSide()) {                viewHolder.thatRightUser.setBackgroundResource(R.drawable.ic_baseline_check_24);            } else {                viewHolder.thatRightUser.setBackgroundResource(R.drawable.ic_baseline_clear_24);            }        } catch (NullPointerException ignored) {        }        viewHolder.questionText.setText(String.valueOf(i + 1));        viewHolder.expandableLayout.setOnExpandListener(b -> {            count = 0;            playAnim(viewHolder.question, 0, q.getQuestion().getQuestion(), q.getQuestion(), viewHolder.sheetView, viewHolder.sol);        });        viewHolder.expandableLayout.expand();        viewHolder.itemView.setOnClickListener(view -> {            if(!expanded) {                viewHolder.expandableLayout.expand();                viewHolder.questionq.setVisibility(View.VISIBLE);            }else{                viewHolder.questionq.setVisibility(View.GONE);                viewHolder.expandableLayout.collapse();            }            expanded = !expanded;        });    }    @Override    public int getItemCount() {        return questionEachResults.size();    }    private void playAnim(final View questionView, final int value, final String data, Question question, LinearLayout optionsContainer, LinearLayout sol) {        questionView.animate().alpha(value).scaleX(value).scaleY(value).setDuration(500).setStartDelay(100)                .setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {            @Override            public void onAnimationStart(Animator animation) {                if (value == 0 && count < 4) {                    String option = "";                    optionsContainer.getChildAt(count).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#848280")));                    if (count == 0) {                        option = question.getA();                    } else if (count == 1) {                        option = question.getB();                    } else if (count == 2) {                        option = question.getC();                    } else if (count == 3) {                        option = question.getD();                    }                    LinearLayout CorrectLayout = (LinearLayout) optionsContainer.getChildAt(question.getAns());                    CorrectLayout.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4BBB4F")));                    LinearLayout linearLayout = (LinearLayout) optionsContainer.getChildAt(count);                    playAnim(linearLayout, 0, option, question, optionsContainer, sol);                    count++;                }            }            @Override            public void onAnimationEnd(Animator animation) {                if (value == 0) {                    LinearLayout layout = (LinearLayout) questionView;                    ((MathView) layout.getChildAt(0)).setDisplayText(data);                    questionView.setTag(data);                    ((MathView) sol.getChildAt(0)).setDisplayText(question.getHint());                    playAnim(questionView, 1, data, question, optionsContainer, sol);                }            }            @Override            public void onAnimationCancel(Animator animation) {            }            @Override            public void onAnimationRepeat(Animator animation) {            }        });    }    public static class MyViewHolder extends RecyclerView.ViewHolder {        final ImageView thisLeftUser;        final ImageView thatRightUser;        final TextView questionText;        final ExpandableLayout expandableLayout;        final LinearLayout question;        final LinearLayout sol;        final View questionq;        final LinearLayout sheetView;        public MyViewHolder(View itemView) {            super(itemView);            thisLeftUser = itemView.findViewById(R.id.thisUserImage);            thatRightUser = itemView.findViewById(R.id.otherUserImage);            questionText = itemView.findViewById(R.id.textView10);            question = itemView.findViewById(R.id.question);            sol = itemView.findViewById(R.id.expCard);            expandableLayout = itemView.findViewById(R.id.expandable);            sheetView = itemView.findViewById(R.id.contaner);            questionq = itemView.findViewById(R.id.questionq);        }    }}