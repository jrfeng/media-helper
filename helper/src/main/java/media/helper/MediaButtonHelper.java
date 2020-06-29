package media.helper;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 用于帮助监听系统的 <b>Intent.ACTION_MEDIA_BUTTON</b> 媒体按钮事件。
 */
public class MediaButtonHelper extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (notMediaButtonAction(intent)) {
            return;
        }

        Intent serviceIntent = new Intent("android.intent.action.MEDIA_BUTTON");
        Bundle extras = intent.getExtras();
        if (extras != null) {
            serviceIntent.putExtras(extras);
        }

        context.startService(serviceIntent);
    }

    private static boolean notMediaButtonAction(Intent intent) {
        return !Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction());
    }

    public static boolean handleMediaButton(Context context, Intent intent,
                                            MediaButtonEventCallback callback) {
        if (notMediaButtonAction(intent)) {
            return false;
        }

        if (callback == null) {
            return false;
        }

        return callback.handleMediaButtonEvent(context, intent);
    }

    /**
     * 将 {@link MediaButtonHelper} 注册为媒体按钮事件接收器。
     */
    public static void registerMediaButtonEventReceiver(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            throw new IllegalStateException("AudioManager is null");
        }

        audioManager.registerMediaButtonEventReceiver(new ComponentName(context, MediaButtonHelper.class));
    }

    /**
     * 取消将 {@link MediaButtonHelper} 注册为媒体按钮事件接收器。
     */
    public static void unregisterMediaButtonEventReceiver(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            throw new IllegalStateException("AudioManager is null");
        }

        audioManager.unregisterMediaButtonEventReceiver(new ComponentName(context, MediaButtonHelper.class));
    }

    public abstract static class MediaButtonEventCallback {
        private ClickCounter mClickCounter;

        public MediaButtonEventCallback() {
            mClickCounter = new ClickCounter(new ClickCounter.Callback() {
                @Override
                public void onClick(int clickCount) {
                    onHeadsetHookClicked(clickCount);
                }
            });
        }

        boolean handleMediaButtonEvent(Context context, Intent intent) {
            if (onMediaButtonEvent(context, intent)) {
                return true;
            }

            KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (keyEvent == null) {
                return false;
            }

            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK) {
                mClickCounter.putEvent();
                return false;
            }

            mClickCounter.reset();

            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    onPlay();
                    return true;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    onPause();
                    return true;
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    onPlayPause();
                    return true;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    onStop();
                    return true;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    onNext();
                    return true;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    onPrevious();
                    return true;
            }

            return false;
        }

        public boolean onMediaButtonEvent(Context context, Intent intent) {
            return false;
        }

        /**
         * "播放" 键被触发。
         */
        public void onPlay() {
        }

        /**
         * "暂停" 键被触发。
         */
        public void onPause() {
        }

        /**
         * "播放/暂停" 键被触发。
         */
        public void onPlayPause() {
        }

        /**
         * "停止" 键被触发。
         */
        public void onStop() {
        }

        /**
         * "下一曲" 键被触发。
         */
        public void onNext() {
        }

        /**
         * "上一曲" 键被触发。
         */
        public void onPrevious() {
        }

        /**
         * 耳机上的按钮被点击（可用于实现 "线控播放" 功能）。
         *
         * @param clickCount 按钮被点击的次数。
         */
        public void onHeadsetHookClicked(int clickCount) {
        }
    }

    private static class ClickCounter {
        private static final int TIME_DELAY = 300;  // 单位: 毫秒 ms
        private int mCount;
        private Timer mTimer;
        private Callback mCallback;

        ClickCounter(Callback callback) {
            mCallback = callback;
        }

        void reset() {
            cancelTimer();
            mCount = 0;
        }

        void putEvent() {
            mCount += 1;
            startTimer();
        }

        private void startTimer() {
            cancelTimer();

            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mCallback.onClick(mCount);
                    mCount = 0;
                }
            }, TIME_DELAY);
        }

        private void cancelTimer() {
            if (mTimer != null) {
                mTimer.cancel();
            }
        }

        interface Callback {
            void onClick(int clickCount);
        }
    }
}
