package media.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

/**
 * 用于帮助监听 <b>AudioManager.ACTION_AUDIO_BECOMING_NOISY</b> 广播。
 * <p>
 * 当多媒体应用程序接收到该广播时，应该暂停播放。
 */
public class BecomeNoiseHelper {
    private Context mContext;
    private OnBecomeNoiseListener mListener;

    private BroadcastReceiver mBecomeNoiseReceiver;
    private boolean mRegistered;

    /**
     * 创建一个 {@link BecomeNoiseHelper} 对象。
     *
     * @param context  Context 对象，不能为 null
     * @param listener 事件监听器，不能为 null
     */
    public BecomeNoiseHelper(@NonNull Context context, @NonNull final OnBecomeNoiseListener listener) {
        ObjectUtil.requireNonNull(context);
        ObjectUtil.requireNonNull(listener);

        mContext = context.getApplicationContext();
        mListener = listener;

        mBecomeNoiseReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mListener.onBecomeNoise();
            }
        };
    }

    /**
     * 注册 <b>AudioManager.ACTION_AUDIO_BECOMING_NOISY</b> 广播监听器。
     */
    public void registerBecomeNoiseReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mContext.registerReceiver(mBecomeNoiseReceiver, intentFilter);

        mRegistered = true;
    }

    /**
     * 取消注册 <b>AudioManager.ACTION_AUDIO_BECOMING_NOISY</b> 广播监听器。
     */
    public void unregisterBecomeNoiseReceiver() {
        if (mRegistered) {
            mContext.unregisterReceiver(mBecomeNoiseReceiver);
            mRegistered = false;
        }
    }

    /**
     * 用于监听 become noise 事件。
     */
    public interface OnBecomeNoiseListener {
        /**
         * 当监听到 become noise 事件时会调用该方法，此时应暂停播放。
         */
        void onBecomeNoise();
    }
}
