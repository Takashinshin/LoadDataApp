package com.takashi.study.samplethreadapp;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * データを読み込むクラス(時間がかかる)
 */
public class DataLoader {
    private static final String TAG = "DataLoader";
    private HandlerThread mHandlerThread;
    private DataLoadHandler mDataLoadHandler;
    private Callback mCallback;
    //Callback通知先のLooper
    private Looper mLooper;

    /**
     * コンストラクター
     */
    public DataLoader() {
        //必要な時にThreadを立ち上げるようにする為、ここではThread生成せず、load()で生成する
    }

    /**
     * 読込み開始
     * @param callback
     */
    public void load(DataLoader.Callback callback, Looper looper) {
        mCallback = callback;
        mLooper = looper;

        //ロード処理を行うスレッド作成
        mHandlerThread = new HandlerThread("DataLoader");
        mHandlerThread.start();

        //上記スレッドをやりとりを行う為のHandlerインスタンス生成
        mDataLoadHandler = new DataLoadHandler(mHandlerThread.getLooper());

        //メッセージを通知
        Message message = Message.obtain();
        message.what = DataLoadHandler.MSG_LOAD;
        mDataLoadHandler.sendMessage(message);
    }

    /**
     * 別スレッドとHandler経由でやりとりを行う為のHandler
     */
    class DataLoadHandler extends Handler {
        private static final int MSG_LOAD = 1;

        public DataLoadHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_LOAD:
                    //読込み処理
                    Log.i(TAG, "start loadInternal() ");
                    loadInternal();
                    break;
                default:
                    break;
            }
        }
    }

    private void loadInternal() {
        for (int rate = 0; rate < 100; rate++) {
            try {
                //100ms毎に進捗率を通知
                Thread.sleep(100);
                notifyProgress(rate);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        //100%で読込み完了を通知
        notifyCompleted();
    }

    private void notifyProgress(final int progress) {
        synchronized (mCallback) {
            final Handler handler = new Handler(mLooper);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.onProgress(progress);
                }
            });
        }
    }

    private void notifyCompleted() {
        synchronized (mCallback) {
            final Handler handler = new Handler(mLooper);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.onCompleted();
                }
            });
        }
    }

    /**
     * データ読込み状態を通知するCallback
     */
    interface Callback {
        /**
         * データ読込み経過を通知
         */
        void onProgress(final int progress);

        /**
         * データ読込み完了を筒通知
         */
        void onCompleted();

    }
}
