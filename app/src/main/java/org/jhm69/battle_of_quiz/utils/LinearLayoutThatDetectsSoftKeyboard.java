package org.jhm69.battle_of_quiz.utils;import android.annotation.SuppressLint;import android.app.Activity;import android.content.Context;import android.graphics.Rect;import android.util.AttributeSet;import android.widget.LinearLayout;/* * LinearLayoutThatDetectsSoftKeyboard - a variant of LinearLayout that can detect when * the soft keyboard is shown and hidden (something Android can't tell you, weirdly). */public class LinearLayoutThatDetectsSoftKeyboard extends LinearLayout {    private Listener listener;    public LinearLayoutThatDetectsSoftKeyboard(Context context, AttributeSet attrs) {        super(context, attrs);    }    public void setListener(Listener listener) {        this.listener = listener;    }    @Override    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {        int height = MeasureSpec.getSize(heightMeasureSpec);        Activity activity = (Activity) getContext();        @SuppressLint("DrawAllocation") Rect rect = new Rect();        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);        int statusBarHeight = rect.top;        int screenHeight = activity.getWindowManager().getDefaultDisplay().getHeight();        int diff = (screenHeight - statusBarHeight) - height;        if (listener != null) {            listener.onSoftKeyboardShown(diff > 128); // assume all soft keyboards are at least 128 pixels high        }        super.onMeasure(widthMeasureSpec, heightMeasureSpec);    }    public interface Listener {        void onSoftKeyboardShown(boolean isShowing);    }}