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

    /**
     * 处理媒体按钮事件。如果媒体事件已被处理，则返回 true，否则返回 false。
     *
     * @param context  Context 对象。
     * @param intent   要处理的媒体事件。
     * @param listener 媒体事件监听器。
     * @return 如果媒体事件已被处理，则返回 true，否则返回 false（如果 Intent 中不包含媒体事件，也会返回 false）。
     */
    public static boolean handleMediaButton(Context context, Intent intent, OnMediaButtonActionListener listener) {
        if (notMediaButtonAction(intent)) {
            return false;
        }

        if (listener == null) {
            return false;
        }

        listener.onMediaButtonAction(context, intent);

        return true;
    }

    /**
     * 将 {@link MediaButtonHelper} 注册为媒体按钮事件接收器。
     */
    public static void registerMediaButtonReceiver(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            throw new IllegalStateException("AudioManager is null");
        }

        audioManager.registerMediaButtonEventReceiver(new ComponentName(context, MediaButtonHelper.class));
    }

    /**
     * 取消将 {@link MediaButtonHelper} 注册为媒体按钮事件接收器。
     */
    public static void unregisterMediaButtonReceiver(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            throw new IllegalStateException("AudioManager is null");
        }

        audioManager.unregisterMediaButtonEventReceiver(new ComponentName(context, MediaButtonHelper.class));
    }

    /**
     * 回调接口。当检测到某个媒体按钮被触发时，该回调接口中的方法会被调用。
     */
    public interface OnMediaButtonActionListener {
        void onMediaButtonAction(Context context, Intent intent);
    }

    /**
     * 多媒体回调接口。该类是个抽象类，它实现了 {@link OnMediaButtonActionListener} 接口，并对多媒体相关的媒体按钮事件进行了封装。
     */
    public abstract static class MediaListener implements OnMediaButtonActionListener {
        private ClickCounter mClickCounter;

        public MediaListener() {
            mClickCounter = new ClickCounter(new ClickCounter.Callback() {
                @Override
                public void onClick(int clickCount) {
                    onHeadsetHookClicked(clickCount);
                }
            });
        }

        @Override
        public void onMediaButtonAction(Context context, Intent intent) {
            KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (keyEvent == null) {
                return;
            }

            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK) {
                mClickCounter.putEvent();
                return;
            }

            mClickCounter.reset();

            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    onPlay();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    onPause();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    onPlayPause();
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    onStop();
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    onNext();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    onPrevious();
                    break;
            }
        }

        /**
         * "播放" 键被触发。
         */
        public abstract void onPlay();

        /**
         * "暂停" 键被触发。
         */
        public abstract void onPause();

        /**
         * "播放/暂停" 键被触发。
         */
        public abstract void onPlayPause();

        /**
         * "停止" 键被触发。
         */
        public abstract void onStop();

        /**
         * "下一曲" 键被触发。
         */
        public abstract void onNext();

        /**
         * "上一曲" 键被触发。
         */
        public abstract void onPrevious();

        /**
         * 耳机上的按钮被点击（可用于实现 "线控播放" 功能）。
         *
         * @param clickCount 按钮被点击的次数。
         */
        public abstract void onHeadsetHookClicked(int clickCount);
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
