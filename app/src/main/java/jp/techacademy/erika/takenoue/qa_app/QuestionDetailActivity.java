package jp.techacademy.erika.takenoue.qa_app;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity implements View.OnClickListener, DatabaseReference.CompletionListener {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;

    private DatabaseReference mFavoriteRef;

    private DatabaseReference mAnswerRef;

    private DatabaseReference mGenreRef;

    private FloatingActionButton mFloatingActionButton;

    private ArrayList<Question> mQuestionArrayList;

    private int mGenre;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()) {
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


            @Override
            protected void onCreate (Bundle savedInstanceState){
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_question_detail);

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    findViewById(R.id.fav).setVisibility(View.INVISIBLE);
                } else {


                }

                Bundle extras = getIntent().getExtras();

                mQuestion = (Question) extras.get("question");


                setTitle(mQuestion.getTitle());

                mListView = (ListView) findViewById(R.id.listView);
                mAdapter = new QuestionDetailListAdapter(this, mQuestion);
                mListView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();

                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        if (user == null) {
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                            intent.putExtra("question", mQuestion);
                            startActivity(intent);
                        }
                    }
                });

                DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
                mAnswerRef.addChildEventListener(mEventListener);
                mFloatingActionButton  = (FloatingActionButton) findViewById(R.id.fav);
                mFloatingActionButton.setOnClickListener(this);
            }

            @Override
            public void onClick (View view){
                if (view.getId() == R.id.fav) {
                    if (mFavoriteRef == null) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                        mFavoriteRef = dataBaseReference.child(Const.FavoritePATH).child(user.getUid()).child(mQuestion.getQuestionUid());
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("genre", String.valueOf(mQuestion.getGenre()));
                        mFavoriteRef.setValue(data);
                        mFloatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(250,170,230)));
                        Snackbar.make(view, "お気に入りに登録しました。", Snackbar.LENGTH_LONG).show();
                        return;
                    } else if (mFavoriteRef != null) {
                        mFavoriteRef.removeValue();
                        mFloatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(130,130,130)));
                        Snackbar.make(view, "お気に入りから削除しました。", Snackbar.LENGTH_LONG).show();
                        return;
                    }
                    mFavoriteRef.addChildEventListener(mEventListener);
                }
            }


            @Override
            public void onComplete (DatabaseError databaseError, DatabaseReference databaseReference)
            {

            }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFavoriteRef == null) {
            mFloatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(130,130,130)));
        } else if (mFavoriteRef != null) {
            mFloatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(250,170,230)));

        }
    }
}




