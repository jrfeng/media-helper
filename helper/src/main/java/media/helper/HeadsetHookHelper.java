package media.helper;

import android.content.Intent;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 用于帮助处理耳机上的按钮点击事件（用于实现 “线控播放”）。
 */
public class HeadsetHookHelper {
    public static final int DEFAULT_CLICK_INTERVAL = 300;  // 单位: 毫秒 ms
    private ClickCounter mClickCounter;

    /**
     * 使用默认的点击事件间隔 {@link #DEFAULT_CLICK_INTERVAL} 构造一个 HeadsetHookHelper 对象。
     * <p>
     * 默认的点击时间间隔为 300 毫秒。
     *
     * @param listener 点击事件监听器
     */
    public HeadsetHookHelper(@NonNull OnHeadsetHookClickListener listener) {
        this(DEFAULT_CLICK_INTERVAL, listener);
    }

    /**
     * 使用 {@code clickInterval} 参数指定的点击事件间隔构造一个 HeadsetHookHelper 对象。
     *
     * @param clickInterval 两次点击事件的最大时间间隔（单位：毫秒）
     * @param listener      点击事件监听器
     */
    public HeadsetHookHelper(int clickInterval, @NonNull OnHeadsetHookClickListener listener) {
        mClickCounter = new ClickCounter(clickInterval, listener);
    }

    /**
     * 处理媒体按钮事件。
     *
     * @param intent 包含媒体按钮事件的 Intent 对象
     * @return 返回 true 表示当前 HeadsetHookHelper 消耗了这个媒体按钮事件，否则会返回 false
     */
    public boolean handleMediaButton(Intent intent) {
        KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (keyEvent == null) {
            return false;
        }

        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK) {
            mClickCounter.putEvent();
            return true;
        }

        mClickCounter.reset();

        return false;
    }

    public interface OnHeadsetHookClickListener {
        /**
         * @param clickCount 按钮被点击的次数
         */
        void onHeadsetHookClicked(int clickCount);
    }

    private static class ClickCounter {
        private int mClickInterval;
        private int mCount;
        private Timer mTimer;
        private OnHeadsetHookClickListener mHeadsetHookClickListener;

        ClickCounter(int clickInterval, OnHeadsetHookClickListener listener) {
            mClickInterval = clickInterval;
            mHeadsetHookClickListener = listener;
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
                    mHeadsetHookClickListener.onHeadsetHookClicked(mCount);
                    mCount = 0;
                }
            }, mClickInterval);
        }

        private void cancelTimer() {
            if (mTimer != null) {
                mTimer.cancel();
            }
        }
    }
}
