package com.example.saber.scrollconficttest;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SecondActivity";
    private HorizontalScrollViewEx2 mListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"onCreate");
        initView();
    }

    private void initView() {
        LayoutInflater inflater = getLayoutInflater();
        mListContainer = (HorizontalScrollViewEx2) findViewById(R.id.container);
        final int screenWidth = MyUtils.getScreenMetrics(this).widthPixels;
        final int screenHeight = MyUtils.getScreenMetrics(this).heightPixels;

        for (int i=0;i<3;i++){
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.content_layout,mListContainer,false);
            layout.getLayoutParams().width = screenWidth;
            TextView textView = (TextView) layout.findViewById(R.id.title);
            textView.setText("page "+(i+1));
            layout.setBackgroundColor(Color.rgb(255/(i+1),255/(i+1),0));
            createList(layout);
            mListContainer.addView(layout);
        }

    }

    private void createList(ViewGroup layout) {
        ListViewEx listView = (ListViewEx) layout.findViewById(R.id.list);
        listView.setHorizontalScrollViewEx2(mListContainer);//内部拦截
        ArrayList<String> datas = new ArrayList<>();
        for(int i=0;i<50;i++){
            datas.add("name "+i);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.content_list_item,R.id.name,datas);
        listView.setAdapter(adapter);
    }
}
