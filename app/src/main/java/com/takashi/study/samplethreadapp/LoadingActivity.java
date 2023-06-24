package com.takashi.study.samplethreadapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class LoadingActivity extends AppCompatActivity {
    private static final String TAG = "LoadingActivity";
    private DataLoader mDataLoader;
    private Button mStartLoadBtn;
    private ProgressBar mProgressBar;
    private DataLoaderCallbackImpl mDataLoaderCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        mStartLoadBtn = findViewById(R.id.startLoadBtn);
        mProgressBar = findViewById(R.id.progressBar);

        mDataLoader = new DataLoader();
        mDataLoaderCallback = new DataLoaderCallbackImpl();

        mStartLoadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //データ読込み開始
                mDataLoader.load(mDataLoaderCallback, Looper.getMainLooper());

                //ロード中は釦無効化
                mStartLoadBtn.setEnabled(false);
            }
        });
    }

    class DataLoaderCallbackImpl implements DataLoader.Callback {

        @Override
        public void onProgress(int progress) {
            //進捗率を画面へ反映
            mProgressBar.setProgress(progress);
        }

        @Override
        public void onCompleted() {
            //読込み完了で画面遷移
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    }
}