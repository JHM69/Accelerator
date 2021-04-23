package org.jhm69.battle_of_quiz.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import org.jhm69.battle_of_quiz.R;
import org.jhm69.battle_of_quiz.adapters.PostViewHolder;
import org.jhm69.battle_of_quiz.models.Post;
import org.jhm69.battle_of_quiz.ui.activities.account.EditProfile;
import org.jhm69.battle_of_quiz.ui.activities.notification.ImagePreviewSave;
import org.jhm69.battle_of_quiz.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static org.jhm69.battle_of_quiz.R.id.action_edit;
import static org.jhm69.battle_of_quiz.ui.activities.MainActivity.userId;

/**
 * Created by jhm69
 */

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_profile_view, container, false);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadFragment(new AboutFragment());
        BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == action_edit) {
                startActivity(new Intent(getActivity(), EditProfile.class));
            } else {
                loadFragment(new AboutFragment());
            }
            return true;
        });

        bottomNavigationView.setOnNavigationItemReselectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_profile:
                case action_edit:
                    break;
            }
        });


    }

    private void loadFragment(Fragment fragment) {
        assert getFragmentManager() != null;
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }


    @SuppressWarnings("StatementWithEmptyBody")
    public static class AboutFragment extends Fragment {
        private TextView post, play;
        private TextView friend;
        private TextView scoreTv;
        private PieChart pieChart;
        private View rootView;
        private RecyclerView rcv;
        private View statsheetView;
        private BottomSheetDialog mmBottomSheetDialog;
        float total;
        ImageView playMatchs;
        @SuppressLint({"SetTextI18n", "InflateParams"})
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragmengt_about, container, false);
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
            UserViewModel profileView = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(UserViewModel.class);
            CircleImageView profile_pic = rootView.findViewById(R.id.profile_pic);
            TextView name = rootView.findViewById(R.id.name);
            TextView instituteTV = rootView.findViewById(R.id.institute_about);
            TextView email = rootView.findViewById(R.id.email);
            TextView location = rootView.findViewById(R.id.location);
            post = rootView.findViewById(R.id.posts);
            play = rootView.findViewById(R.id.win);
            friend = rootView.findViewById(R.id.friends);
            TextView bio = rootView.findViewById(R.id.bio);
            scoreTv = rootView.findViewById(R.id.scoreJ);
            statsheetView = getActivity().getLayoutInflater().inflate(R.layout.stat_bottom_sheet_dialog, null);
            mmBottomSheetDialog = new BottomSheetDialog(Objects.requireNonNull(getContext()));
            mmBottomSheetDialog.setContentView(statsheetView);
            mmBottomSheetDialog.setCanceledOnTouchOutside(true);
            playMatchs = rootView.findViewById(R.id.playBtn);
            rcv = rootView.findViewById(R.id.hdrh);
            rcv.setVisibility(View.VISIBLE);
            LinearLayoutManager layoutManager = new LinearLayoutManager(rootView.getContext());
            rcv.setHasFixedSize(true);
            rcv.setLayoutManager(layoutManager);
            loadPosts();

            playMatchs.setVisibility(View.GONE);

            pieChart = rootView.findViewById(R.id.pieChart);
            pieChart.setNoDataText("");
            mFirestore.collection("Users")
                    .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                    .collection("Friends")
                    .get()
                    .addOnSuccessListener(documentSnapshots -> {
                        friend.setText(String.valueOf(documentSnapshots.size()));
                    });
            try {
                profileView.user.observe(getViewLifecycleOwner(), users -> {
                    name.setText(users.getName());

                    instituteTV.setText(users.getInstitute());

                    email.setText(users.getEmail());
                    location.setText(users.getLocation());
                    bio.setText(users.getBio());
                    scoreTv.setText(String.valueOf(users.getScore()));
                    setUpChartData(pieChart, users.getWin(), users.getLose(), users.getDraw());
                    Glide.with(rootView.getContext())
                            .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_logo_icon))
                            .load(users.getImage())
                            .into(profile_pic);
                    profile_pic.setOnClickListener(v -> {
                        rootView.getContext().startActivity(new Intent(rootView.getContext(), ImagePreviewSave.class)
                                .putExtra("url", users.getImage()));
                    });
                });
                FirebaseFirestore.getInstance().collection("Posts")
                        .whereEqualTo("userId", mAuth.getCurrentUser().getUid())
                        .get()
                        .addOnSuccessListener(querySnapshot -> post.setText(String.format(Locale.ENGLISH, "%d", querySnapshot.size())));
            } catch (NullPointerException ignored) {

            }

            return rootView;
        }


        private void loadPosts() {
            PagedList.Config config = new PagedList.Config.Builder()
                    .setEnablePlaceholders(false)
                    .setPrefetchDistance(4)
                    .setPageSize(6)
                    .build();
            Query mQuery;
            mQuery = FirebaseFirestore.getInstance().collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).whereEqualTo("userId", userId);
            FirestorePagingOptions<Post> options = new FirestorePagingOptions.Builder<Post>()
                    .setLifecycleOwner(this)
                    .setQuery(mQuery, config, Post.class)
                    .build();

            FirestorePagingAdapter<Post, PostViewHolder> mAdapter;
            mAdapter = new FirestorePagingAdapter<Post, PostViewHolder>(options) {
                @NonNull
                @Override
                public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = getLayoutInflater().inflate(R.layout.item_feed_post, parent, false);
                    return new PostViewHolder(view);
                }

                @Override
                protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull Post post) {
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
                            break;

                        case ERROR:
                            Toast.makeText(
                                    getActivity(),
                                    "Error Occurred!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            //refreshLayout.setRefreshing(false);
                            break;

                        case FINISHED:
                            // refreshLayout.setRefreshing(false);
                            break;
                    }
                }

            };
            rcv.setAdapter(mAdapter);
        }

        void setUpChartData(PieChart pieChart, float win, float lose, float draw) {
            float total = win + draw + lose;
            if (total == 0) {
                pieChart.setVisibility(View.INVISIBLE);
            } else {
                pieChart.setVisibility(View.VISIBLE);
                Description description = new Description();
                description.setText("");
                pieChart.setDescription(description);
                Map<String, Float> scoreData = new HashMap<>();
                scoreData.put("win", win);
                scoreData.put("draw:", draw);
                scoreData.put("lose:", lose);
                ArrayList<PieEntry> entries = new ArrayList<>();
                if (win == 0) {
                    //entries.add(new PieEntry(win, "Win: " + win));
                } else {
                    entries.add(new PieEntry(win, "win"));
                }
                if (draw == 0) {
                    //entries.add(new PieEntry(win, "Win: " + win));
                } else {
                    entries.add(new PieEntry(draw, "draw"));
                }
                if (lose == 0) {
                    //entries.add(new PieEntry(win, "Win: " + win));
                } else {
                    entries.add(new PieEntry(lose, "lose"));
                }
                PieDataSet pieDataSet = new PieDataSet(entries, " | won:" + (win == 0 ? "0" : (int) win) + " | drawn:" + (draw == 0 ? "0" : (int) draw) + " | lost:" + (lose == 0 ? "0" : (int) lose) + " | total:" + ((win + draw + lose) == 0 ? "0" : (int) (win + draw + lose)));
                pieDataSet.setColors(Color.parseColor("#41B843"), Color.parseColor("#AA6CEF"), Color.parseColor("#F45656"));
                PieData pieData = new PieData(pieDataSet);
                pieChart.setData(pieData);
                pieData.setValueTextColor(Color.parseColor("#ffffff"));
                pieData.setValueTextSize(10);
                pieChart.animateXY(1500, 1500);
                pieChart.invalidate();
                play.setText(String.valueOf((int)total));

            }
        }
    }

   /* public static class EditFragment extends Fragment {
        UserViewModel userViewModel;
        private static final int PICK_IMAGE = 100;
        private FirebaseAuth mAuth;
        private FirebaseFirestore mFirestore;
        //private UserHelper userHelper;
        private TextInputEditText name, email, bio, location, institute, dept;
        private AuthCredential credential;
        private View rootView;

        public EditFragment() {
        }

        @Override
        public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.frag_edit_profile, container, false);

            mAuth = FirebaseAuth.getInstance();
            mFirestore = FirebaseFirestore.getInstance();
            userViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(UserViewModel.class);
            name = rootView.findViewById(R.id.name);
            email = rootView.findViewById(R.id.email);
            bio = rootView.findViewById(R.id.bio);
            institute = rootView.findViewById(R.id.institute);
            dept = rootView.findViewById(R.id.dept);
            location = rootView.findViewById(R.id.location);
            CircleImageView profile_pic = rootView.findViewById(R.id.profile_pic);
            Button updatebtn = rootView.findViewById(R.id.update);
            ImageView updatepicture = rootView.findViewById(R.id.imageView12);

            updatepicture.setOnClickListener(v -> {
                if (isOnline()) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE);

                } else {
                    Toasty.error(rootView.getContext(), "Some technical error occurred", Toasty.LENGTH_SHORT, true).show();
                }

            });
            if (!isOnline()) {
                rootView.findViewById(R.id.h_username).animate()
                        .alpha(0.0f)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                rootView.findViewById(R.id.h_username).setVisibility(View.GONE);
                            }
                        }).start();

                rootView.findViewById(R.id.h_email).animate()
                        .alpha(0.0f)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                rootView.findViewById(R.id.h_email).setVisibility(View.GONE);
                            }
                        }).start();

            }
            userViewModel.user.observe(getViewLifecycleOwner(), users -> {
                name.setText(users.getName());
                institute.setText(users.getInstitute());
                email.setText(users.getEmail());
                location.setText(users.getLocation());
                bio.setText(users.getBio());

                Glide.with(rootView.getContext())
                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_logo_icon))
                        .load(users.getImage())
                        .into(profile_pic);
                profile_pic.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        rootView.getContext().startActivity(new Intent(rootView.getContext(), ImagePreviewSave.class)
                                .putExtra("url", users.getImage()));
                        return false;
                    }
                });
            });


            updatebtn.setOnClickListener(v -> {
                Users users = userViewModel.user.getValue();
                final ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                final DocumentReference userDocument = mFirestore.collection("Users").document(userId);
                if (!Objects.requireNonNull(email.getText()).toString().equals(users.getEmail())) {
                    dialog.setMessage("Updating Details....");
                    new MaterialDialog.Builder(rootView.getContext())
                            .title("Email changed")
                            .content("It seems that you have changed your email, re-enter your password to change.")
                            .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                            .input("Password", "", new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull MaterialDialog mdialog, CharSequence input) {
                                    if (!input.toString().equals("pass")) {
                                        dialog.dismiss();
                                        mdialog.show();
                                        Toasty.error(rootView.getContext(), "Invalid password", Toasty.LENGTH_SHORT, true).show();
                                    } else {

                                        mdialog.dismiss();
                                        final FirebaseUser currentuser = mAuth.getCurrentUser();

                                        credential = EmailAuthProvider
                                                .getCredential(currentuser.getEmail(), input.toString());

                                        currentuser.reauthenticate(credential)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        currentuser.updateEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()) {

                                                                    currentuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {

                                                                            Map<String, Object> userMap = new HashMap<>();
                                                                            userMap.put("email", email.getText().toString());

                                                                            FirebaseFirestore.getInstance().collection("Users")
                                                                                    .document(mAuth.getCurrentUser().getUid())
                                                                                    .update(userMap)
                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {
                                                                                            dialog.dismiss();
                                                                                            users.setEmail(email.getText().toString());
                                                                                            userViewModel.insert(users);
                                                                                            Toasty.success(rootView.getContext(), "Verification email sent.", Toasty.LENGTH_SHORT, true).show();
                                                                                            dialog.dismiss();
                                                                                        }
                                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    dialog.dismiss();
                                                                                    Log.e("Update", "failed: " + e.getLocalizedMessage());
                                                                                }
                                                                            });

                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            dialog.dismiss();
                                                                            Log.e("Error", e.getLocalizedMessage());
                                                                            dialog.dismiss();
                                                                        }
                                                                    });

                                                                } else {

                                                                    Log.e("Update email error", task.getException().getMessage() + "..");
                                                                    dialog.dismiss();

                                                                }

                                                            }
                                                        });

                                                    }
                                                });

                                    }
                                }
                            })
                            .positiveText("Done")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog mdialog, @NonNull DialogAction which) {
                                    dialog.show();
                                    mdialog.dismiss();
                                }
                            })
                            .negativeText("Don't change my email")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog mdialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                    mdialog.dismiss();
                                }
                            })
                            .cancelable(false)
                            .canceledOnTouchOutside(false)
                            .show();


                }

                if (!name.getText().toString().equals(users.getName())) {

                    dialog.setMessage("Updating Details....");
                    dialog.show();

                    Map<String, Object> map = new HashMap<>();
                    map.put("name", name.getText().toString());

                    userDocument.update(map)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    users.setName(name.getText().toString());
                                    userViewModel.insert(users);
                                    dialog.dismiss();
                                    Log.i("Update", "success");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i("Update", "failed: " + e.getMessage());
                                    dialog.dismiss();
                                }
                            });

                }

                if (!bio.getText().toString().equals(users.getBio())) {
                    dialog.setMessage("Updating Details....");
                    dialog.show();
                    Map<String, Object> map = new HashMap<>();
                    map.put("bio", bio.getText().toString());
                    userDocument.update(map)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dialog.dismiss();
                                    users.setBio(bio.getText().toString());
                                    userViewModel.insert(users);
                                    //userHelper.updateContactBio(1, bio.getText().toString());
                                    Log.i("Update", "success");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                    Log.i("Update", "failed: " + e.getMessage());
                                }
                            });

                }

                if (!location.getText().toString().equals(users.getLocation())) {
                    dialog.setMessage("Updating Details....");
                    dialog.show();
                    Map<String, Object> map = new HashMap<>();
                    map.put("location", location.getText().toString());
                    userDocument.update(map)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dialog.dismiss();
                                    users.setLocation(location.getText().toString());
                                    userViewModel.insert(users);
                                    Log.i("Update", "success");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                    Log.i("Update", "failed: " + e.getMessage());

                                }
                            });

                }
                if (!institute.getText().toString().equals(users.getInstitute())) {

                    dialog.setMessage("Updating Details....");
                    dialog.show();

                    Map<String, Object> map = new HashMap<>();
                    map.put("institute", institute.getText().toString());

                    userDocument.update(map)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dialog.dismiss();
                                    users.setInstitute(institute.getText().toString());
                                    userViewModel.insert(users);
                                    Log.i("Update", "success");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                    Log.i("Update", "failed: " + e.getMessage());

                                }
                            });

                }
                if (!dept.getText().toString().equals(users.getDept())) {

                    dialog.setMessage("Updating Details....");
                    dialog.show();

                    Map<String, Object> map = new HashMap<>();
                    map.put("dept", dept.getText().toString());

                    userDocument.update(map)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dialog.dismiss();
                                    users.setDept(dept.getText().toString());
                                    userViewModel.insert(users);
                                    //userHelper.updateContactDept(1, dept.getText().toString());
                                    Log.i("Update", "success");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                    Log.i("Update", "failed: " + e.getMessage());

                                }
                            });

                }
            });

            return rootView;
        }

        public boolean isOnline() {
            ConnectivityManager cm =
                    (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (data != null) {
                Uri imageUri = null;
                if (requestCode == PICK_IMAGE) {
                    imageUri = data.getData();
                    UCrop.Options options = new UCrop.Options();
                    options.setCompressionFormat(Bitmap.CompressFormat.PNG);
                    options.setCompressionQuality(50);
                    options.withAspectRatio(1, 1);
                    options.setShowCropGrid(true);
                    UCrop.of(Objects.requireNonNull(imageUri), Uri.fromFile(new File(getActivity().getCacheDir(), userId + System.currentTimeMillis() + ".png")))
                            .withOptions(options)
                            .start(getActivity(), 23);
                } else if (requestCode == 23) {
                    try {
                        imageUri = UCrop.getOutput(Objects.requireNonNull(data));
                        Toasty.info(rootView.getContext(), "Profile picture uploaded, click Save details button to apply changes", Toasty.LENGTH_LONG, true).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toasty.info(rootView.getContext(), "error " + e.getLocalizedMessage(), Toasty.LENGTH_SHORT, true).show();
                    }
                }
            }

        }
    }
*/

}


