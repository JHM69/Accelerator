package org.jhm69.battle_of_quiz.ui.activities.post;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.marcoscg.dialogsheet.DialogSheet;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;
import com.yalantis.ucrop.UCrop;

import org.jhm69.battle_of_quiz.R;
import org.jhm69.battle_of_quiz.adapters.PagerPhotosAdapter;
import org.jhm69.battle_of_quiz.models.Images;
import org.jhm69.battle_of_quiz.service.UploadService;
import org.jhm69.battle_of_quiz.utils.MathView;
import org.jhm69.battle_of_quiz.utils.RichEditor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import es.dmoral.toasty.Toasty;

import static android.view.View.GONE;

public class PostImage extends AppCompatActivity {

    public static boolean canUpload = false;
    List<Images> imagesList;
    ViewPager pager;
    Spinner type;
    PagerPhotosAdapter adapter;
    ArrayList<String> uploadedImagesUrl = new ArrayList<>();
    EditText latexText;
    String tag;
    RichEditor mRichEd;
    String forInsert;
    private FirebaseUser mCurrentUser;
    private DotsIndicator indicator;
    private RelativeLayout indicator_holder;
    private int selectedIndex;
    private SharedPreferences sharedPreferences;
    private int serviceCount;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, PostImage.class);
        context.startActivity(intent);
    }

    @NonNull
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    public static void insertData(String latexData, RichEditor mEditor) {
        mEditor.insertLatex(latexData);
    }

    public static void addExtraLatex(String data, EditText latexText) {
        int start = Math.max(latexText.getSelectionStart(), 0);
        int end = Math.max(latexText.getSelectionEnd(), 0);
        latexText.getText().replace(Math.min(start, end), Math.max(start, end),
                data, 0, data.length());
    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();

        return true;
    }

    @Override
    public void onBackPressed() {
        new MaterialDialog.Builder(this)
                .title("Discard")
                .content("Are you sure do you want to go back?")
                .positiveText("Yes")
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .onPositive((dialog, which) -> finish())
                .negativeText("No")
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post_image);
        imagesList = getIntent().getParcelableArrayListExtra("imagesList");
        if (imagesList.isEmpty()) {
            finish();
        }
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.statusBar));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("New Image Post");
        latexText = findViewById(R.id.latex_equation);
        type = findViewById(R.id.spinner_type);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.item_type_x));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_expandable_list_item_1);
        type.setAdapter(arrayAdapter);
        type.setOnItemSelectedListener(new TypeXSpinnerClass());

        try {
            Objects.requireNonNull(getSupportActionBar()).setTitle("New Image Post");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Map<String, Object> postMap = new HashMap<>();

        pager = findViewById(R.id.pager);
        indicator = findViewById(R.id.indicator);
        indicator_holder = findViewById(R.id.indicator_holder);

        indicator.setDotsClickable(true);
        adapter = new PagerPhotosAdapter(this, imagesList);
        pager.setAdapter(adapter);
        mRichEd = findViewById(R.id.editPost);
        setUpLaTexEditor(mRichEd, getApplicationContext(), null, 200);
        if (imagesList.size() > 1) {
            indicator_holder.setVisibility(View.VISIBLE);
            indicator.setViewPager(pager);
        } else {
            indicator_holder.setVisibility(GONE);
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        sharedPreferences = getSharedPreferences("uploadservice", MODE_PRIVATE);
        serviceCount = sharedPreferences.getInt("count", 0);

        EditText mEditText = findViewById(R.id.text);


        ProgressDialog mDialog = new ProgressDialog(this);
        StorageReference mStorage = FirebaseStorage.getInstance().getReference();
    }

    public void setUpLaTexEditor(RichEditor mEditor, Context context, MathView mathView, int height) {
        String data;
        mEditor.setEditorHeight(height);
        mEditor.setEditorFontSize(18);
        TypedValue typedValue = new TypedValue();
        mEditor.setPadding(10, 10, 10, 10);
        //mEditor.setBackground("https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg");
        mEditor.setPlaceholder("Start Witting your post here...");
        //mEditor.setInputEnabled(false);

        mEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                try {
                    mathView.setDisplayText(text);
                } catch (NullPointerException ignored) {

                }
            }
        });

        findViewById(R.id.action_undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.undo();
            }
        });

        findViewById(R.id.action_redo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mEditor.redo();
            }
        });
        findViewById(R.id.action_bold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBold();
            }
        });

        findViewById(R.id.action_italic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setItalic();
            }
        });

        findViewById(R.id.action_subscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setSubscript();
            }
        });

        findViewById(R.id.action_superscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setSuperscript();
            }
        });

        findViewById(R.id.action_strikethrough).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setStrikeThrough();
            }
        });

        findViewById(R.id.action_underline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setUnderline();
            }
        });

        findViewById(R.id.action_heading1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(1);
            }
        });

        findViewById(R.id.action_heading2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(2);
            }
        });

        findViewById(R.id.action_heading3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(3);
            }
        });

        findViewById(R.id.action_heading4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(4);
            }
        });

        findViewById(R.id.action_heading5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(5);
            }
        });

        findViewById(R.id.action_heading6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(6);
            }
        });

        findViewById(R.id.action_txt_color).setOnClickListener(new View.OnClickListener() {
            private boolean isChanged;

            @Override
            public void onClick(View v) {
                mEditor.setTextColor(isChanged ? Color.BLACK : Color.RED);
                isChanged = !isChanged;
            }
        });

        findViewById(R.id.action_bg_color).setOnClickListener(new View.OnClickListener() {
            private boolean isChanged;

            @Override
            public void onClick(View v) {
                mEditor.setTextBackgroundColor(isChanged ? Color.TRANSPARENT : Color.YELLOW);
                isChanged = !isChanged;
            }
        });

        findViewById(R.id.action_indent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setIndent();
            }
        });

        findViewById(R.id.action_outdent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setOutdent();
            }
        });

        findViewById(R.id.action_align_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignLeft();
            }
        });

        findViewById(R.id.action_align_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignCenter();
            }
        });

        findViewById(R.id.action_align_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignRight();
            }
        });

        findViewById(R.id.action_blockquote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBlockquote();
            }
        });

        findViewById(R.id.action_insert_bullets).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBullets();
            }
        });

        findViewById(R.id.action_insert_numbers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setNumbers();
            }
        });
        View latexView = findViewById(R.id.latext_editor);
        findViewById(R.id.insert_latex).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (latexView.getVisibility() == View.GONE) {
                    latexView.setVisibility(View.VISIBLE);

                    MathView mathView = findViewById(R.id.mathView);
                    latexText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            mathView.setDisplayText("\\(" + charSequence.toString() + "\\)");
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });
                    findViewById(R.id.submit_latex).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String data = latexText.getText().toString();
                            data = data.replace(" ", "");
                            insertData(data, mEditor);
                            latexText.setText("");
                            data = "";
                            latexView.setVisibility(View.GONE);
                        }
                    });
                    findViewById(R.id.action_frac).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex(" \\frac{a}{b} ", latexText);
                        }
                    });
                    findViewById(R.id.action_power).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex(" a^2 ", latexText);
                        }
                    });
                    findViewById(R.id.action_sub).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex(" a_2 ", latexText);
                        }
                    });
                    findViewById(R.id.action_root).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex("\\sqrt{ a }", latexText);
                        }
                    });
                    findViewById(R.id.action_alpha).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex(" \\alpha ", latexText);
                        }
                    });
                    findViewById(R.id.action_diff).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex(" \\frac{d}{dx}(y) ", latexText);
                        }
                    });
                    findViewById(R.id.action_int).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex(" \\int_{a}^{b} ", latexText);
                        }
                    });

                    findViewById(R.id.action_therefore).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex(" \\therefore ", latexText);
                        }
                    });
                    findViewById(R.id.action_theta).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex(" \\theta ", latexText);
                        }
                    });
                    findViewById(R.id.action_mu).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex(" \\mu ", latexText);
                        }
                    });
                    findViewById(R.id.action_pi).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex(" \\pi ", latexText);
                        }
                    });
                    findViewById(R.id.action_lanbda).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex(" \\lambda ", latexText);
                        }
                    });
                    findViewById(R.id.action_ohm).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex(" \\ohm  ", latexText);
                        }
                    });
                    findViewById(R.id.action_omega).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex(" \\omega  ", latexText);
                        }
                    });

                    findViewById(R.id.action_hat).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex(" \\hat{A}  ", latexText);
                        }
                    });
                    findViewById(R.id.action_over).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex(" \\Overthrow{A}  ", latexText);
                        }
                    });
                    findViewById(R.id.action_enter).setOnClickListener(new View.OnClickListener() {
                        @Override


                        public void onClick(View view) {
                            addExtraLatex(" \\\\ ", latexText);
                        }
                    });
                    findViewById(R.id.action_space).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex(" \\; ", latexText);
                        }
                    });


                } else {
                    latexView.setVisibility(View.GONE);
                }
            }
        });

        findViewById(R.id.insert_code).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setCode();
            }
        });
        findViewById(R.id.action_insert_checkbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertTodo();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_image_post, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_post) {
            if (tag != null) {
                new DialogSheet(this)
                        .setRoundedCorners(true)
                        .setColoredNavigationBar(true)
                        .setPositiveButton("Yes", v -> {
                            sharedPreferences.edit().putInt("count", ++serviceCount).apply();
                            Intent intent = new Intent(PostImage.this, UploadService.class);
                            intent.putExtra("count", serviceCount);
                            intent.putStringArrayListExtra("uploadedImagesUrl", uploadedImagesUrl);
                            intent.putParcelableArrayListExtra("imagesList", (ArrayList<? extends Parcelable>) imagesList);
                            intent.putExtra("notification_id", (int) System.currentTimeMillis());
                            intent.putExtra("current_id", mCurrentUser.getUid());
                            try {
                                intent.putExtra("description", mRichEd.getHtml());
                            } catch (NullPointerException h) {
                                intent.putExtra("description", " ");
                            }
                            intent.putExtra("tag", tag);
                            intent.setAction(UploadService.ACTION_START_FOREGROUND_SERVICE);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(intent);
                            } else {
                                startService(intent);
                            }
                            Toasty.info(PostImage.this, "Uploading images..", Toasty.LENGTH_SHORT, true).show();
                            finish();
                        })
                        .setNegativeButton("No", v -> {
                        })
                        .setTitle("Upload")
                        .setMessage("Are you sure is this the content you want to upload? Upload post with image will cost 5 xp!")
                        .show();
            } else {
                Toasty.error(this, "Select a tag", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void deleteItem(View view) {

        new MaterialDialog.Builder(this)
                .title("Remove")
                .content("Are you sure do you want to remove this image?")
                .positiveText("Yes")
                .onPositive((dialog, which) -> {

                    if (imagesList.size() == 1) {
                        finish();
                        return;
                    }

                    imagesList.remove(pager.getCurrentItem());

                    adapter = new PagerPhotosAdapter(PostImage.this, imagesList);
                    pager.setAdapter(adapter);
                    indicator.setViewPager(pager);

                    if (imagesList.size() > 1) {
                        indicator_holder.setVisibility(View.VISIBLE);
                        indicator.setViewPager(pager);
                    } else {
                        indicator_holder.setVisibility(GONE);
                    }

                })
                .negativeText("No")
                .show();
    }

    public void openCropItem(View view) {

        selectedIndex = pager.getCurrentItem();
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setCompressionQuality(90);
        options.setShowCropGrid(true);


        UCrop.of(Uri.fromFile(new File(imagesList.get(selectedIndex).getOg_path())), Uri.fromFile(new File(getCacheDir(), imagesList.get(selectedIndex).getName() + "_" + random() + "_edit.png")))
                .withAspectRatio(1, 1)
                .withOptions(options)
                .start(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {

            long old_id = imagesList.get(selectedIndex).getId();
            String old_name = imagesList.get(selectedIndex).getName();
            String old_path = imagesList.get(selectedIndex).getOg_path();

            imagesList.remove(selectedIndex);
            imagesList.add(selectedIndex, new Images(old_name, old_path, UCrop.getOutput(data).getPath(), old_id));
            adapter = new PagerPhotosAdapter(this, imagesList);
            pager.setAdapter(adapter);
            indicator.setViewPager(pager);
            adapter.notifyDataSetChanged();
            pager.setCurrentItem(selectedIndex, true);

        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable throwable = UCrop.getError(Objects.requireNonNull(data));
            Objects.requireNonNull(throwable).printStackTrace();
            Toasty.error(this, "Error cropping : " + throwable.getMessage(), Toasty.LENGTH_SHORT, true).show();
        }


    }

    class TypeXSpinnerClass implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
            tag = parent.getItemAtPosition(position).toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }
}
