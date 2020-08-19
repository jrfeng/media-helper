package media.helper;

import android.content.Intent;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 用于帮助处理耳机上的 Headset Hook 按钮点击事件（用于实现 “线控播放”）。
 * <p>
 * 使用步骤：<br>
 * <ol>
 *     <li>创建一个 HeadsetHookHelper 对象；</li>
 *     <li>在媒体按钮监听器中调用 {@link #handleMediaButton(Intent)} 处理媒体按钮事件即可。</li>
 * </ol>
 * <p>
 * <b>例：</b><br>
 * <code>
 * <pre>
 * HeadsetHookHelper headsetHookHelper = new HeadsetHookHelper(new OnHeadsetHookClickListener() {
 *     &#64;Override
 *     public void onHeadsetHookClicked(int clickCount) {
 *         // do something
 *     }
 * });
 * ...
 *
 * // MediaSessionCompat.Callback
 * &#64;Override
 * public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
 *     return headsetHookHelper.handleMediaButton(mediaButtonEvent);
 * }
 * </pre>
 * </code>
 */
public class HeadsetHookHelper {
    public static final int DEFAULT_CLICK_INTERVAL = 300;  // 单位: 毫秒 ms
    private ClickCounter mClickCounter;

    /**
     * 使用默认的点击事件时间间隔 {@link #DEFAULT_CLICK_INTERVAL} 构造一个 HeadsetHookHelper 对象。
     * <p>
     * 默认的点击事件时间间隔为 300 毫秒。
     *
     * @param listener 点击事件监听器，不能为 null
     */
    public HeadsetHookHelper(@NonNull OnHeadsetHookClickListener listener) {
        this(DEFAULT_CLICK_INTERVAL, listener);
    }

    /**
     * 使用 {@code clickInterval} 参数指定的点击事件时间间隔构造一个 HeadsetHookHelper 对象。
     *
     * @param clickInterval 两次点击事件的最大时间间隔（单位：毫秒）
     * @param listener      点击事件监听器，不能为 null
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
            consumeMediaButtonEvent(keyEvent);
            return true;
        }

        mClickCounter.reset();

        return false;
    }

    private void consumeMediaButtonEvent(KeyEvent keyEvent) {
        if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
            mClickCounter.putEvent();
        }
    }

    /**
     * 用于监听 Headset Hook 按钮的点击次数。
     */
    public interface OnHeadsetHookClickListener {
        /**
         * 当检测到 Headset Hook 按钮被单次或连续点击时，会调用该方法。
         *
         * @param clickCount Headset Hook 按钮被点击的次数
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
