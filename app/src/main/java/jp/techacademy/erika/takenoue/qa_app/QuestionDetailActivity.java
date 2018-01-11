package jp.techacademy.erika.takenoue.qa_app;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import static jp.techacademy.erika.takenoue.qa_app.MainActivity.FavoriteQidMap;

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;

    private DatabaseReference mAnswerRef;

    private DatabaseReference mFavoriteRef;
    private boolean FavoriteFlag = false;
    private FloatingActionButton mFavoritefab;
    private FirebaseUser mUser;

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

    private ChildEventListener mFavoriteEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            mFavoritefab.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(250, 170, 230)));
            FavoriteFlag = true;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        setTitle(mQuestion.getTitle());

        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        mFavoritefab = (FloatingActionButton) findViewById(R.id.fav);

        if(mUser == null) {
            mFavoritefab.setVisibility(View.GONE);
        } else {
            DatabaseReference mDataBaseReference = FirebaseDatabase.getInstance().getReference();
            mFavoriteRef = mDataBaseReference.child(Const.FavoritePATH).child(mUser.getUid()).child(mQuestion.getQuestionUid());
            mFavoriteRef.addChildEventListener(mFavoriteEventListener);

            mFavoritefab.setVisibility(View.VISIBLE);
        }

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

        mFavoritefab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FavoriteFlag == true) {
                    DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference favoriteRef = dataBaseReference.child(Const.FavoritePATH).child(mUser.getUid()).child(mQuestion.getQuestionUid());
                    favoriteRef.removeValue();
                    Snackbar.make(findViewById(android.R.id.content), "お気に入り解除しました。", Snackbar.LENGTH_LONG).show();
                    FavoriteQidMap.remove(mQuestion.getQuestionUid());
                    mFavoritefab.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(130, 130, 130)));
                    FavoriteFlag = false;
                } else {
                    DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference favoriteRef = dataBaseReference.child(Const.FavoritePATH).child(mUser.getUid()).child(mQuestion.getQuestionUid());
                    Map<String, String> data = new HashMap<String, String>();
                    String genre = String.valueOf(mQuestion.getGenre());
                    data.put("genre", genre);
                    favoriteRef.setValue(data);
                    Snackbar.make(findViewById(android.R.id.content), "お気に入りに登録しました。", Snackbar.LENGTH_LONG).show();
                    FavoriteQidMap.put(mQuestion.getQuestionUid(), genre);
                    mFavoritefab.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(250, 170, 230)));
                    FavoriteFlag = true;

                }
            }
        });

        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);
    }
}
