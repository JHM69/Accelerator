package org.jhm69.battle_of_quiz.ui.fragment;import android.annotation.SuppressLint;import android.content.Context;import android.graphics.PorterDuff;import android.view.LayoutInflater;import android.view.View;import android.widget.ImageView;import android.widget.TextView;import androidx.annotation.NonNull;import androidx.annotation.Nullable;import androidx.core.content.ContextCompat;import androidx.fragment.app.Fragment;import androidx.fragment.app.FragmentManager;import androidx.fragment.app.FragmentStatePagerAdapter;import org.jhm69.battle_of_quiz.R;import java.util.ArrayList;import java.util.List;public class TabAdapter extends FragmentStatePagerAdapter {    private final List<Fragment> mFragmentList = new ArrayList<>();    private final List<String> mFragmentTitleList = new ArrayList<>();    private final List<Integer> mFragmentIconList = new ArrayList<>();    private final Context context;    int notificationBadge;    TabAdapter(FragmentManager fm, Context context) {        super(fm);        this.context = context;    }    public void addFragment(Fragment fragment, String title, int tabIcon) {        mFragmentList.add(fragment);        mFragmentTitleList.add(title);        mFragmentIconList.add(tabIcon);    }    public void setNotificationBadge(int b) {        notificationBadge = b;    }    @Nullable    @Override    public CharSequence getPageTitle(int position) {//return mFragmentTitleList.get(position);        return null;    }    @Override    public int getCount() {        return mFragmentList.size();    }    @NonNull    @Override    public Fragment getItem(int position) {        return mFragmentList.get(position);    }    public View getTabView(int position) {        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.custom_tab, null);        TextView tabTextView = view.findViewById(R.id.tabTextView);        tabTextView.setText(mFragmentTitleList.get(position));        ImageView tabImageView = view.findViewById(R.id.tabImageView);        tabImageView.setImageResource(mFragmentIconList.get(position));        return view;    }    public View setNotifications(int b) {        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.custom_tab, null);        TextView tabTextView = view.findViewById(R.id.tabTextView);        tabTextView.setText(mFragmentTitleList.get(2));        ImageView tabImageView = view.findViewById(R.id.tabImageView);        tabImageView.setImageResource(mFragmentIconList.get(2));        TextView badge = view.findViewById(R.id.badge);        badge.setVisibility(View.VISIBLE);        badge.setText(String.valueOf(b));        return view;    }    public View getSelectedTabView(int position) {        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.custom_tab, null);        TextView tabTextView = view.findViewById(R.id.tabTextView);        tabTextView.setText(mFragmentTitleList.get(position));        tabTextView.setTextColor(ContextCompat.getColor(context, R.color.white));        ImageView tabImageView = view.findViewById(R.id.tabImageView);        tabImageView.setImageResource(mFragmentIconList.get(position));        TextView badge = view.findViewById(R.id.badge);        badge.setVisibility(View.GONE);        tabImageView.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_ATOP);        return view;    }}