package media.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 用于帮助监听系统的 <b>Intent.ACTION_MEDIA_BUTTON</b> 媒体按钮事件。
 * <p>
 * 你可以通过调用 {@link #registerMediaButtonReceiver()} 来注册一个媒体按钮广播监听器。
 * <p>
 * 如果你使用了 MediaSession 框架，那么可以不调用 {@link #registerMediaButtonReceiver()} 来注册媒体按钮广
 * 播监听器，而是在 AndroidManifest.xml 文件中进行注册。
 * <p>
 * <p><b>例：</b></p>
 * 在 AndroidManifest.xml 文件中注册媒体按钮广播监听器：
 * <p>
 * <code>
 * <pre>
 * &lt;receiver android:name="androidx.media.session.MediaButtonReceiver" &gt;
 *     &lt;intent-filter&gt;
 *         &lt;action android:name="android.intent.action.MEDIA_BUTTON" /&gt;
 *     &lt;/intent-filter&gt;
 * &lt;/receiver&gt;
 * </pre>
 * <code/>
 * </p>
 * 然后像下面这样配置你的 Service：
 * <p>
 * <code>
 * <pre>
 * &lt;service android:name="com.example.android.MediaPlaybackService" &gt;
 *     &lt;intent-filter&gt;
 *         &lt;action android:name="android.intent.action.MEDIA_BUTTON" /&gt;
 *     &lt;/intent-filter&gt;
 * &lt;/service&gt;
 * </pre>
 * </code>
 * </p>
 * 最后，在 Service 的 onStartCommand() 方法中调用静态方法
 * {@link #handleMediaButton(Context, Intent, OnMediaButtonActionListener)} 来处理媒体按钮事件：
 * <p>
 * <code>
 * <pre>
 * &#64;Override
 * public int onStartCommand(Intent intent, int flags, int startId) {
 *     MediaButtonHelper.handleMediaButton(getApplicationContext(), intent, myOnMediaButtonActionListener);
 *
 *     return super.onStartCommand(intent, flags, startId);
 * }
 * </pre>
 * </code>
 * </p>
 */
public class MediaButtonHelper {
    private Context mContext;
    private WeakReference<OnMediaButtonActionListener> mListenerWeakReference;

    private BroadcastReceiver mMediaButtonReceiver;
    private boolean mRegistered;

    public MediaButtonHelper(@NonNull Context context, @NonNull OnMediaButtonActionListener listener) {
        ObjectUtil.requireNonNull(listener);
        ObjectUtil.requireNonNull(listener);

        mContext = context.getApplicationContext();
        mListenerWeakReference = new WeakReference<>(listener);

        mMediaButtonReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleMediaButton(context, intent);
            }
        };
    }

    private void handleMediaButton(Context context, Intent intent) {
        if (notMediaButtonAction(intent)) {
            return;
        }

        OnMediaButtonActionListener listener = mListenerWeakReference.get();
        if (listener == null) {
            unregisterMediaButtonReceiver();
            return;
        }

        listener.onMediaButtonAction(context, intent);
    }

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

    private static boolean notMediaButtonAction(Intent intent) {
        return !Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction());
    }

    /**
     * 注册 <b>Intent.ACTION_MEDIA_BUTTON</b> 媒体按钮监听器。
     */
    public void registerMediaButtonReceiver() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        mContext.registerReceiver(mMediaButtonReceiver, intentFilter);

        mRegistered = true;
    }

    /**
     * 取消注册 <b>Intent.ACTION_MEDIA_BUTTON</b> 媒体按钮监听器。
     */
    public void unregisterMediaButtonReceiver() {
        if (mRegistered) {
            mContext.unregisterReceiver(mMediaButtonReceiver);
            mRegistered = false;
        }
    }

    /**
     * 回调接口。但检测到某个媒体按钮被触发时，该回调接口中的方法会被调用。
     */
    public interface OnMediaButtonActionListener {
        void onMediaButtonAction(Context context, Intent intent);
    }

    /**
     * 多媒体回调接口。该类是个抽象类，它实现了 {@link OnMediaButtonActionListener} 接口，并对多媒体相关的媒体按钮事件进行了封装。
     */
    public abstract class MediaListener implements OnMediaButtonActionListener {
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
