package com.example.quiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.INVISIBLE;

public class QuestionsActivity extends AppCompatActivity {


    public static final  String FILE_NAME = "Quiz";
    public static final  String KEY_NAME  = "Questions";
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    //DatabaseReference qRef = database.getReference();
    private TextView question,noIndicator;
    private FloatingActionButton bookmarkBtn;
    private LinearLayout optionsContainer;
    private Button shareBtn, nextBtn;
    private int count = 0;
    private List<QuestionModel> list;
    private int position = 0;
    private int score = 0;
    private String courseID,level = "L1";
    private Dialog loadingdialog;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Gson gson;
    private int matchedQuestionPosition;
    private List<QuestionModel> bookmarkslist;
    private List<QuestionDetails> questionDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        question = findViewById(R.id.question);
        noIndicator = findViewById(R.id.no_indicator);
        bookmarkBtn = findViewById(R.id.bookmark_btn);
        optionsContainer = findViewById(R.id.options_container);
        shareBtn = findViewById(R.id.share_btn);
        nextBtn = findViewById(R.id.next_btn);
        preferences = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        gson = new Gson();
        getBookmarks();
        courseID = getIntent().getStringExtra("CourseID");
        loadingdialog = new Dialog(this);
        loadingdialog.setContentView(R.layout.loading);
        loadingdialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));
        loadingdialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingdialog.setCancelable(false);
        list = new ArrayList<>();
        final List<QuestionDetails> questionDetails = new ArrayList<>();

        bookmarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(modelmatch()){
                    bookmarkslist.remove(matchedQuestionPosition);
                    bookmarkBtn.setImageDrawable(getDrawable(R.drawable.bookmark_border));
                }else{
                    bookmarkslist.add(list.get(position));
                    bookmarkBtn.setImageDrawable(getDrawable(R.drawable.bookmark));
                }
            }
        });

        loadingdialog.show();
        myRef.child("Test").child(courseID).child("Levels").child(level).child("ques").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    questionDetails.add(snapshot.getValue(QuestionDetails.class));
                }
                for(int i = 0; i < questionDetails.size(); i++) {
                    String tid = String.valueOf(questionDetails.get(i).getTypeId());
                    //Toast.makeText(QuestionsActivity.this, tid+" "+questionDetails.get(i).getqID(), Toast.LENGTH_SHORT).show();
                    myRef.child("Types").child(tid).child(questionDetails.get(i).getqID()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                list.add(snapshot.getValue(QuestionModel.class));
                            }
                            if (list.size() > 0) {
                                for (int i = 0; i < 4; i++) {
                                    optionsContainer.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            checkAnswer((Button) v);
                                        }
                                    });
                                }
                                playAnime(question, 0, list.get(position).getQuestion());
                                nextBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        nextBtn.setEnabled(false);
                                        nextBtn.setAlpha(0.7f);
                                        enableoption(true);
                                        position++;
                                        if (position == list.size()) {
                                            //score Activity
                                            Intent scoreIntent = new Intent(QuestionsActivity.this, ScoreActivity.class);
                                            scoreIntent.putExtra("score", score);
                                            scoreIntent.putExtra("total", list.size());
                                            startActivity(scoreIntent);
                                            finish();
                                            return;
                                        }
                                        count = 0;
                                        playAnime(question, 0, list.get(position).getQuestion());
                                    }
                                });

                                shareBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String body = list.get(position).getQuestion() + "\n" +
                                                list.get(position).getOptionA() + "\n" +
                                                list.get(position).getOptionB() + "\n" +
                                                list.get(position).getOptionC() + "\n" +
                                                list.get(position).getOptionD();
                                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                        shareIntent.setType("text/plain");
                                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Quiz Challange");
                                        shareIntent.putExtra(Intent.EXTRA_TEXT, body);
                                        startActivity(Intent.createChooser(shareIntent, "Share Via"));
                                    }
                                });
                            } else {
                                finish();
                                Toast.makeText(QuestionsActivity.this, "No Questions Available", Toast.LENGTH_SHORT).show();
                                loadingdialog.dismiss();
                                finish();
                            }
                            loadingdialog.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(QuestionsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(QuestionsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        storeBookmarks();
    }

    private void playAnime(final View view, final int value, final String data){
        view.animate().alpha(value).scaleX(value).scaleY(value).setDuration(500).setStartDelay(100)
                .setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if(value == 0 && count < 4){
                    String option = "";
                    if(count == 0){
                        option = list.get(position).getOptionA();
                    }else if(count == 1){
                        option = list.get(position).getOptionB();
                    }else if(count == 2){
                        if(list.get(position).getNumberOfOptions() == 3) {
                            option = list.get(position).getOptionC();
                        }else{
                            optionsContainer.getChildAt(count).setVisibility(INVISIBLE);
                        }
                    }else if(count == 3){
                        if(list.get(position).getNumberOfOptions() == 4) {
                            option = list.get(position).getOptionD();
                        }else{
                            optionsContainer.getChildAt(count).setVisibility(INVISIBLE);
                        }
                    }
                    playAnime(optionsContainer.getChildAt(count),0,option);
                    count++;
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //data change
                if(value == 0){
                    try {
                        ((TextView)view).setText(data);
                        noIndicator.setText(position+1+"/"+list.size());
                        if(modelmatch()){
                            bookmarkBtn.setImageDrawable(getDrawable(R.drawable.bookmark));
                        }else{
                            bookmarkBtn.setImageDrawable(getDrawable(R.drawable.bookmark_border));
                        }
                    }catch (ClassCastException e){
                        ((Button)view).setText(data);
                    }
                    view.setTag(data);
                    playAnime(view,1,data);
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

    private void checkAnswer(Button selectedoption){
        enableoption(false);
        nextBtn.setEnabled(true);
        nextBtn.setAlpha(1);
        if(selectedoption.getText().toString().equals(list.get(position).getcorrect())){
            //correct
            score++;
            selectedoption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        }else{
            //incorrect
            selectedoption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            Button correctoption = (Button) optionsContainer.findViewWithTag(list.get(position).getcorrect());
            correctoption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        }
    }

    private void enableoption(boolean enable){
        for(int i = 0; i < 4; i++){
            optionsContainer.getChildAt(i).setEnabled(enable);
            if(enable){
                optionsContainer.getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#989898")));
            }
        }
    }

    private void getBookmarks(){
        String json = preferences.getString(KEY_NAME,"");
        Type type = new TypeToken<List<QuestionModel>>(){}.getType();
        bookmarkslist = gson.fromJson(json,type);
        if(bookmarkslist == null){
            bookmarkslist = new ArrayList<>();
        }
    }

    private boolean modelmatch(){
        boolean matched = false;
        int i = 0;
        for(QuestionModel model :bookmarkslist){
            if(model.getQuestion().equals(list.get(position).getQuestion())
            && model.getcorrect().equals(list.get(position).getcorrect())){
                matched = true;
                matchedQuestionPosition = i;
            }
            i++;
        }
        return matched;
    }

    private void storeBookmarks(){
        String json = gson.toJson(bookmarkslist);
        editor.putString(KEY_NAME,json);
        editor.commit();
    }
}
