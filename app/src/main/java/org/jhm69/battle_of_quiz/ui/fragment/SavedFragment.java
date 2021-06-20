package org.jhm69.battle_of_quiz.ui.fragment;import android.annotation.SuppressLint;import android.os.Bundle;import android.view.LayoutInflater;import android.view.View;import android.view.ViewGroup;import android.widget.ImageView;import android.widget.TextView;import androidx.annotation.NonNull;import androidx.fragment.app.Fragment;import androidx.lifecycle.ViewModelProviders;import androidx.recyclerview.widget.DefaultItemAnimator;import androidx.recyclerview.widget.LinearLayoutManager;import androidx.recyclerview.widget.RecyclerView;import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;import com.google.android.material.bottomsheet.BottomSheetDialog;import com.google.firebase.database.annotations.Nullable;import org.jhm69.battle_of_quiz.R;import org.jhm69.battle_of_quiz.adapters.PostViewHolder;import org.jhm69.battle_of_quiz.models.Post;import org.jhm69.battle_of_quiz.viewmodel.PostViewModel;import java.util.ArrayList;import java.util.List;import java.util.Objects;public class SavedFragment extends Fragment {    List<Post> postList;    PostViewModel postViewModel;    private RecyclerView mRecyclerView;    private View statsheetView;    private BottomSheetDialog mmBottomSheetDialog;    private SwipeRefreshLayout refreshLayout;    private View rootView;    public SavedFragment() {    }    @Override    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,                             Bundle savedInstanceState) {        rootView = inflater.inflate(R.layout.fragment_saved, container, false);        return rootView;    }    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables", "InflateParams"})    @Override    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {        super.onViewCreated(view, savedInstanceState);        postList = new ArrayList<>();        postViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(PostViewModel.class);        statsheetView = Objects.requireNonNull(getActivity()).getLayoutInflater().inflate(R.layout.stat_bottom_sheet_dialog, null);        mmBottomSheetDialog = new BottomSheetDialog(rootView.getContext());        mmBottomSheetDialog.setContentView(statsheetView);        mmBottomSheetDialog.setCanceledOnTouchOutside(true);        refreshLayout = rootView.findViewById(R.id.refreshLayout);        TextView title = rootView.findViewById(R.id.default_title);        TextView text = rootView.findViewById(R.id.default_text);        ImageView image = rootView.findViewById(R.id.imageview);        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_logo));        title.setText("No saved posts found");        text.setText("Save some post from Timeline to view here.");        PostPhotosAdapter postPhotosAdapter = new PostPhotosAdapter(postList);        mRecyclerView = rootView.findViewById(R.id.recyclerView);        mRecyclerView.setItemAnimator(new DefaultItemAnimator());        mRecyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext(), LinearLayoutManager.VERTICAL, false));        mRecyclerView.setHasFixedSize(true);        postViewModel.posts.observe(requireActivity(), posts -> {            postList = posts;            postPhotosAdapter.postListNow = postList;            mRecyclerView.setAdapter(postPhotosAdapter);            postPhotosAdapter.notifyDataSetChanged();        });        //mAdapter_v19 = new PostsAdapter(postList, rootView.getContext(), getActivity(), mmBottomSheetDialog, statsheetView, false);//        mRecyclerView.addItemDecoration(new DividerItemDecoration(rootView.getContext(), DividerItemDecoration.VERTICAL));        //mRecyclerView.setAdapter(mAdapter_v19);        refreshLayout.setOnRefreshListener(() -> refreshLayout.setVisibility(View.GONE));    }    class PostPhotosAdapter extends RecyclerView.Adapter<PostViewHolder> {        private List<Post> postListNow;        public PostPhotosAdapter(List<Post> post) {            this.postListNow = post;        }        @NonNull        @Override        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {            View view = getLayoutInflater().inflate(R.layout.item_feed_post, parent, false);            return new PostViewHolder(view);        }        @Override        public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {            try {                Post post1 = postListNow.get(position);                holder.bind(post1, holder, position, mmBottomSheetDialog, statsheetView, true);            } catch (IndexOutOfBoundsException ignored) {            }        }        @Override        public int getItemCount() {            return postList.size();        }    }}