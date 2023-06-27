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
    //Load処理中断Flag
    private static int mInterruptionFlag;
    private static final int NOT_INTERRUPTING = 0;
    private static final int INTERRUPTING = 1;

    /**
     * コンストラクター
     */
    public DataLoader() {
        //必要な時にThreadを立ち上げるようにする為、ここではThread生成せず、load()で生成する

        //フラグ初期化
        mInterruptionFlag = NOT_INTERRUPTING;
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

    public void cancel() {
        if (mDataLoadHandler != null) {
            //中断フラグ変更
            setInterruptionFlag(INTERRUPTING);

            //cancelのMessageを投げる
            Message message = Message.obtain();
            message.what = DataLoadHandler.MSG_CANCEL;
            mDataLoadHandler.sendMessage(message);
        }
    }

    /**
     * 別スレッドとHandler経由でやりとりを行う為のHandler
     */
    class DataLoadHandler extends Handler {
        private static final int MSG_LOAD = 1;
        private static final int MSG_CANCEL = 2;

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
                case MSG_CANCEL:
                    //読込み停止
                    Log.i(TAG, "cancel load.");
                    notifyCancel();
                default:
                    break;
            }
        }
    }

    private boolean loadInternal() {
        for (int rate = 0; rate < 100; rate++) {
            if (mInterruptionFlag == INTERRUPTING) {
                setInterruptionFlag(NOT_INTERRUPTING);
                return true;
            }
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
        return false;
    }

    private void notifyProgress(final int progress) {
        synchronized (mCallback) {
            final Handler handler = new Handler(mLooper);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "DataLoading :" + progress + "%");
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
                    Log.i(TAG, "onCompleted");
                    mCallback.onCompleted();
                }
            });
        }
    }

    private void notifyCancel() {
        synchronized (mCallback) {
            final Handler handler = new Handler(mLooper);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "notifyCancel");
                    int cancelRate = 0;
                    mCallback.onCancel(cancelRate);
                }
            });
        }
    }

    /**
     * 別スレッドから値を変更する為synchronizedにする
     * @param state
     */
    public synchronized void setInterruptionFlag(int state) {
        mInterruptionFlag = state;
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

        /**
         * 読込みキャンセルを通知
         * @param cancelRate 中断の割合。中断でも対応できるように引数で割合を返す
         */
        void onCancel(final int cancelRate);
    }
}
