package jp.techacademy.erika.takenoue.qa_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import static jp.techacademy.erika.takenoue.qa_app.MainActivity.FavoriteQidMap;


public class FavoriteActivity extends AppCompatActivity {

    private int mGenre = 0;

    private DatabaseReference mDatabaseReference;;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
    private QuestionsListAdapter mAdapter;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if (FavoriteQidMap.containsKey(dataSnapshot.getKey())) {
                HashMap map = (HashMap) dataSnapshot.getValue();
                String title = (String) map.get("title");
                String body = (String) map.get("body");
                String name = (String) map.get("name");
                String uid = (String) map.get("uid");
                String imageString = (String) map.get("image");
                byte[] bytes;
                if (imageString != null) {
                    bytes = Base64.decode(imageString, Base64.DEFAULT);
                } else {
                    bytes = new byte[0];
                }

                ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
                HashMap answerMap = (HashMap) map.get("answers");
                if (answerMap != null) {
                    for (Object key : answerMap.keySet()) {
                        HashMap temp = (HashMap) answerMap.get((String) key);
                        String answerBody = (String) temp.get("body");
                        String answerName = (String) temp.get("name");
                        String answerUid = (String) temp.get("uid");
                        Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                        answerArrayList.add(answer);
                    }
                }

                int mGenre = Integer.parseInt(FavoriteQidMap.get(dataSnapshot.getKey()));
                Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenre, bytes, answerArrayList);
                mQuestionArrayList.add(question);
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            for (Question question: mQuestionArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {
                    question.getAnswers().clear();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            question.getAnswers().add(answer);
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }
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
        setContentView(R.layout.activity_main);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        mAdapter.setQuestionArrayList(mQuestionArrayList);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                intent.putExtra("question", mQuestionArrayList.get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        mQuestionArrayList.clear();
        for (int i = 1;i <= 4;i++) {
            DatabaseReference mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(i));
            mGenreRef.addChildEventListener(mEventListener);
        }
    }

};

