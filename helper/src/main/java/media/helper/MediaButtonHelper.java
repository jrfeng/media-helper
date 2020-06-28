package media.helper;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 用于帮助监听系统的 <b>Intent.ACTION_MEDIA_BUTTON</b> 媒体按钮事件。
 * <p>
 * 该帮助类提供了 2 种注册 MediaButton 事件监听器的方法：
 * <p>
 * <b>第一种方法</b>：通过调用 {@link #registerMediaButtonReceiver()} 来注册一个媒体按钮广播监听器。
 * <p>
 * <b>例 1：</b><br>
 * <code>
 * <pre>
 * // 注册 MediaButton 事件监听器
 * mediaButtonHelper.registerMediaButtonReceiver()
 *
 * // 取消注册 MediaButton 事件监听器
 * mediaButtonHelper.unregisterMediaButtonReceiver()
 * </pre>
 * </code>
 * <p>
 * <b>第二张方法</b>：通过 AndroidManifest.xml 文件进行注册。如果你使用了 MediaSession 框架，推荐使用这种方
 * 法进行注册。
 * <p>
 * <p><b>例 2：</b></p>
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
 * </code>
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
    private AudioManager mAudioManager;
    private OnMediaButtonActionListener mListener;

    private BroadcastReceiver mMediaButtonReceiver;
    private ComponentName mReceiverComponentName;
    private boolean mRegistered;

    public MediaButtonHelper(@NonNull Context context, @NonNull OnMediaButtonActionListener listener) {
        ObjectUtil.requireNonNull(listener);
        ObjectUtil.requireNonNull(listener);

        mContext = context.getApplicationContext();
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mListener = listener;

        mMediaButtonReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleMediaButton(context, intent);
            }
        };

        mReceiverComponentName = new ComponentName(context, mMediaButtonReceiver.getClass());
    }

    private void handleMediaButton(Context context, Intent intent) {
        if (notMediaButtonAction(intent)) {
            return;
        }

        mListener.onMediaButtonAction(context, intent);
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
        mAudioManager.registerMediaButtonEventReceiver(mReceiverComponentName);

        mRegistered = true;
    }

    /**
     * 取消注册 <b>Intent.ACTION_MEDIA_BUTTON</b> 媒体按钮监听器。
     */
    public void unregisterMediaButtonReceiver() {
        if (mRegistered) {
            mAudioManager.unregisterMediaButtonEventReceiver(mReceiverComponentName);
            mContext.unregisterReceiver(mMediaButtonReceiver);
            mRegistered = false;
        }
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
