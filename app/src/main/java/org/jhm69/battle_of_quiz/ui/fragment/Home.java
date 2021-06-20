package org.jhm69.battle_of_quiz.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;

import org.jhm69.battle_of_quiz.R;
import org.jhm69.battle_of_quiz.adapters.PostViewHolder;
import org.jhm69.battle_of_quiz.models.Images;
import org.jhm69.battle_of_quiz.models.Post;
import org.jhm69.battle_of_quiz.ui.activities.post.PostImage;
import org.jhm69.battle_of_quiz.ui.activities.post.PostText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Home extends Fragment {

    @SuppressLint("StaticFieldLeak")
    public static Context context;
    private final List<Images> imagesList = new ArrayList<>();
    String tag = "All";
    int lastPosition = -1;
    private RecyclerView mPostsRecyclerView;
    private FirebaseFirestore mFirestore;
    private View statsheetView;
    private BottomSheetDialog mmBottomSheetDialog;
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_home, container, false);
    }

    @SuppressLint("InflateParams")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        context = getContext();
        ExtendedFloatingActionButton button = view.findViewById(R.id.soihefawiw);
        mSwipeRefreshLayout = view.findViewById(R.id.swf);
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        mFirestore = FirebaseFirestore.getInstance();
        mFirestore.setFirestoreSettings(settings);
        statsheetView = Objects.requireNonNull(getActivity()).getLayoutInflater().inflate(R.layout.stat_bottom_sheet_dialog, null);
        mmBottomSheetDialog = new BottomSheetDialog(view.getContext());
        mmBottomSheetDialog.setContentView(statsheetView);
        mmBottomSheetDialog.setCanceledOnTouchOutside(true);
        mPostsRecyclerView = view.findViewById(R.id.posts_recyclerview);
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(context);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);
        mBottomSheetDialog.setContentView(sheetView);

        LinearLayout text_post = sheetView.findViewById(R.id.text_post);
        LinearLayout photo_post = sheetView.findViewById(R.id.image_post);

        text_post.setOnClickListener(V -> {
            mBottomSheetDialog.dismiss();
            PostText.startActivity(context);
        });

        photo_post.setOnClickListener(view1 -> {
            mBottomSheetDialog.dismiss();
            startPickImage();
        });

        mPostsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPostsRecyclerView.setHasFixedSize(true);
        setupAdapter(tag);
        mSwipeRefreshLayout.setOnRefreshListener(() -> setupAdapter(tag));
        ChipGroup chipGroup = view.findViewById(R.id.filter_chip_SS_group);
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            try {
                Chip c = view.findViewById(checkedId);
                tag = c.getText().toString();
                setupAdapter(tag);
            } catch (NullPointerException ignored) {

            }
        });

        button.setOnClickListener(view12 -> mBottomSheetDialog.show());
    }

    private void setupAdapter(String tag) {
        mSwipeRefreshLayout.setRefreshing(true);
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(6)
                .setPageSize(15)
                .build();
        Query mQuery;
        if (tag.equals("All")) {
            mQuery = mFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING);
        } else {
            mQuery = mFirestore.collection("Posts").orderBy("liked_count", Query.Direction.DESCENDING).whereEqualTo("tag", tag);
        }

        FirestorePagingOptions<Post> options = new FirestorePagingOptions.Builder<Post>()
                .setLifecycleOwner(this)
                .setQuery(mQuery, config, Post.class)
                .build();

        FirestorePagingAdapter<Post, PostViewHolder> mAdapter = new FirestorePagingAdapter<Post, PostViewHolder>(options) {
            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = getLayoutInflater().inflate(R.layout.item_feed_post, parent, false);
                return new PostViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull Post post) {
                Animation animation = AnimationUtils.loadAnimation(getActivity(),
                        (position > lastPosition) ? R.anim.up_from_bottom
                                : R.anim.down_from_top);
                holder.itemView.startAnimation(animation);
                lastPosition = position;
                mSwipeRefreshLayout.setRefreshing(false);
                holder.bind(post, holder, position, mmBottomSheetDialog, statsheetView, true);
            }

            @Override
            protected void onError(@NonNull Exception e) {
                super.onError(e);
                Log.e("MainActivity", e.getMessage());
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                switch (state) {
                    case LOADING_INITIAL:
                    case LOADING_MORE:
                        // refreshLayout.setRefreshing(true);
                        break;

                    case LOADED:

                    case FINISHED:
                        //refreshLayout.setRefreshing(false);
                        break;

                    case ERROR:
                        Toast.makeText(
                                getActivity(),
                                "Error Occurred!",
                                Toast.LENGTH_SHORT
                        ).show();

                        //refreshLayout.setRefreshing(false);
                        break;
                }
            }

        };
        mPostsRecyclerView.setAdapter(mAdapter);

    }

    public void startPickImage() {
        ImagePicker.create(this)
                .folderMode(true)
                .toolbarFolderTitle("Folder")
                .toolbarImageTitle("Tap to select")
                .includeVideo(false)
                .multi()
                .limit(7)
                .showCamera(true)
                .enableLog(true)
                .imageDirectory("ত্বারক")
                .start(123);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == 123) {
                imagesList.clear();
                List<Image> pickedImages = ImagePicker.getImages(data);
                for (Image image : pickedImages) {
                    imagesList.add(new Images(image.getName(), image.getPath(), image.getPath(), image.getId()));
                }
                Intent intent = new Intent(getActivity(), PostImage.class);
                intent.putParcelableArrayListExtra("imagesList", (ArrayList<? extends Parcelable>) imagesList);
                startActivity(intent);
            }
        }
    }
}
