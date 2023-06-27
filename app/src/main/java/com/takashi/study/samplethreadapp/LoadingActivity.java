package com.takashi.study.samplethreadapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class LoadingActivity extends AppCompatActivity {
    private static final String TAG = "LoadingActivity";
    private DataLoader mDataLoader;
    private Button mStartLoadBtn, mCancelBtn;
    private ProgressBar mProgressBar;
    private DataLoaderCallbackImpl mDataLoaderCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        mStartLoadBtn = findViewById(R.id.startLoadBtn);
        mCancelBtn = findViewById(R.id.cancelBtn);
        mProgressBar = findViewById(R.id.progressBar);


        mDataLoader = new DataLoader();
        mDataLoaderCallback = new DataLoaderCallbackImpl();

        setEnableBtn(true, false);

        mStartLoadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //データ読込み開始
                mDataLoader.load(mDataLoaderCallback, Looper.getMainLooper());

                //ロード中は釦無効化
                setEnableBtn(false, true);
            }
        });

        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //データ読込み中止
                mDataLoader.cancel();

                //キャンセル中の釦(start:有効, cancel:無効)
                setEnableBtn(true, false);
            }
        });
    }

    private void setEnableBtn(boolean startBtn, boolean cancelBtn) {
        mStartLoadBtn.setEnabled(startBtn);
        mCancelBtn.setEnabled(cancelBtn);
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

        @Override
        public void onCancel(int cancelRate) {
            //初期状態に戻す
            Log.i(TAG, "onCancel rate:" + cancelRate);
            mProgressBar.setProgress(cancelRate);
        }
    }
}