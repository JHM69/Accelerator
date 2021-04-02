package org.jhm69.battle_of_quiz.ui.activities.post;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.commons.lang3.StringUtils;
import org.jhm69.battle_of_quiz.R;
import org.jhm69.battle_of_quiz.repository.UserRepository;
import org.jhm69.battle_of_quiz.utils.MathView;
import org.jhm69.battle_of_quiz.utils.RichEditor;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import es.dmoral.toasty.Toasty;

import static org.jhm69.battle_of_quiz.ui.activities.MainActivity.userId;

public class PostText extends AppCompatActivity {
    FirebaseFirestore mFirestore;
    FirebaseUser mCurrentUser;
    DatabaseReference databaseReference;
    EditText latexText;
    RichEditor mRichEd;
    Spinner type;
    String tag;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, PostText.class);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, String preText) {
        Intent intent = new Intent(context, PostText.class).putExtra("preText", preText);
        context.startActivity(intent);
    }

    public static String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 14) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();

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
                .title("Discard Post")
                .content("Are you sure do you want to go back?")
                .positiveText("Yes")
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .onPositive((dialog, which) -> finish())
                .negativeText("No")
                .show();
    }

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post_text);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        Toolbar toolbar = findViewById(R.id.toolbar4);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Text Post (3 xp)");
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.statusBar));
        latexText = findViewById(R.id.latex_equation);
        type = findViewById(R.id.spinner_type);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.item_type_x));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_expandable_list_item_1);
        type.setAdapter(arrayAdapter);
        type.setOnItemSelectedListener(new TypeXSpinnerClass());

        try {
            getSupportActionBar().setTitle("Text Post (3 xp)");
            toolbar.setTitleTextColor(Color.WHITE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mFirestore = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        FrameLayout mImageholder = findViewById(R.id.image_holder);
        mRichEd = findViewById(R.id.editPost);
        setUpLaTexEditor(mRichEd, getApplicationContext(), null, 200);

        if (StringUtils.isNotEmpty(getIntent().getStringExtra("preText"))) {

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_text_post, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_post) {
            sendPost();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendPost() {
        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setMessage("Posting...");
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        mFirestore.collection("Users").document(mCurrentUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (tag != null) {
                    try {
                        String postID = getSaltString();
                        Map<String, Object> postMap = new HashMap<>();
                        postMap.put("userId", documentSnapshot.getString("id"));
                        postMap.put("username", documentSnapshot.getString("username"));
                        postMap.put("name", documentSnapshot.getString("name"));
                        postMap.put("institute", documentSnapshot.getString("institute"));
                        postMap.put("dept", documentSnapshot.getString("dept"));
                        postMap.put("userimage", documentSnapshot.getString("image"));
                        postMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
                        postMap.put("image_count", 0);
                        postMap.put("description", mRichEd.getHtml());
                        postMap.put("postId", postID);
                        postMap.put("tag", tag);
                        postMap.put("liked_count", 0);
                        Map<String, Object> postMapFinal = new HashMap<>();
                        postMapFinal.put(postID, postMap);

                        mFirestore.collection("PendingPosts")
                                .document(postID)
                                .set(postMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                updateXP();
                                mDialog.dismiss();
                                Toasty.success(PostText.this, "Post is sent to admin for review", Toasty.LENGTH_SHORT, true).show();
                                finish();
                            }
                        });
                    } catch (NullPointerException j) {
                    }
                } else {
                    Toasty.error(getApplicationContext(), "Select a tag", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mDialog.dismiss();
            }
        });
    }

    public void setUpLaTexEditor(RichEditor mEditor, Context context, MathView mathView, int height) {
        String data;
        mEditor.setEditorHeight(height);
        mEditor.setEditorFontSize(18);
        TypedValue typedValue = new TypedValue();
        mEditor.setPadding(10, 10, 10, 10);
        mEditor.setPlaceholder("Start Witting your post here...");
        //mEditor.setInputEnabled(false);

        mEditor.setOnTextChangeListener(text -> {
            try {
                mathView.setDisplayText(text);
            } catch (NullPointerException ignored) {

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
                            //Toast.makeText(PostText.this, data, Toast.LENGTH_SHORT).show();
                            insertData(data, mEditor);
                            latexText.setText("");
                            data = "";
                            latexView.setVisibility(View.GONE);
                        }
                    });
                    findViewById(R.id.action_frac).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex("\\frac{a}{b}", latexText);
                        }
                    });
                    findViewById(R.id.action_power).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex("a^2", latexText);
                        }
                    });
                    findViewById(R.id.action_sub).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex("a_2", latexText);
                        }
                    });
                    findViewById(R.id.action_root).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex("\\sqrt{a}", latexText);
                        }
                    });
                    findViewById(R.id.action_alpha).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex("\\alpha", latexText);
                        }
                    });
                    findViewById(R.id.action_diff).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex("\\frac{d}{dx}(y)", latexText);
                        }
                    });
                    findViewById(R.id.action_int).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex("\\int_{a}^{b}", latexText);
                        }
                    });

                    findViewById(R.id.action_therefore).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex("\\therefore", latexText);
                        }
                    });
                    findViewById(R.id.action_theta).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex("\\theta", latexText);
                        }
                    });
                    findViewById(R.id.action_mu).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex("\\mu", latexText);
                        }
                    });
                    findViewById(R.id.action_pi).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex("\\pi", latexText);
                        }
                    });
                    findViewById(R.id.action_lanbda).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex("\\lambda", latexText);
                        }
                    });
                    findViewById(R.id.action_ohm).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex("\\ohm", latexText);
                        }
                    });
                    findViewById(R.id.action_omega).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex("\\omega", latexText);
                        }
                    });

                    findViewById(R.id.action_hat).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex("\\hat{A}", latexText);
                        }
                    });
                    findViewById(R.id.action_over).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex("\\Overthrow{A}", latexText);
                        }
                    });
                    findViewById(R.id.action_enter).setOnClickListener(new View.OnClickListener() {
                        @Override

                        public void onClick(View view) {
                            addExtraLatex("\\\\", latexText);
                        }
                    });
                    findViewById(R.id.action_space).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addExtraLatex("\\;", latexText);
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

    private void updateXP() {
        FirebaseFirestore.getInstance().collection("Users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    int scoreOld = documentSnapshot.getLong("reward").intValue();
                    int newScore = scoreOld + (-3);
                    HashMap<String, Object> scoreMap = new HashMap<>();
                    scoreMap.put("reward", newScore);
                    FirebaseFirestore.getInstance()
                            .collection("Users")
                            .document(userId)
                            .update(scoreMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @SuppressLint({"CheckResult", "DefaultLocale"})
                        @Override
                        public void onSuccess(Void aVoid) {
                            new UserRepository((Application) getApplicationContext()).updateXp(-3);
                            //Toast.makeText(, "", Toast.LENGTH_SHORT).show();
                            //Toasty.success(getApplicationContext(), "Congratulations, You have got 10 reward", Toasty.LENGTH_SHORT, true);
                        }
                    });
                });

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
