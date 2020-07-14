package media.helper;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class MediaStoreHelper {
    private static Executor mExecutor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors() * 2,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setDaemon(true);
                    return thread;
                }
            });

    private MediaStoreHelper() {
        throw new AssertionError();
    }

    public static <T> Scanner<T> scanAudio(@NonNull ContentResolver resolver, @NonNull Decoder<T> decoder) {
        ObjectUtil.requireNonNull(resolver);
        ObjectUtil.requireNonNull(decoder);

        return new AudioScanner<>(resolver, decoder);
    }

    public static <T> Scanner<T> scanVideo(@NonNull ContentResolver resolver, @NonNull Decoder<T> decoder) {
        ObjectUtil.requireNonNull(resolver);
        ObjectUtil.requireNonNull(decoder);

        return new VideoScanner<>(resolver, decoder);
    }

    public static <T> Scanner<T> scanImages(@NonNull ContentResolver resolver, @NonNull Decoder<T> decoder) {
        ObjectUtil.requireNonNull(resolver);
        ObjectUtil.requireNonNull(decoder);

        return new ImagesScanner<>(resolver, decoder);
    }

    public interface Scanner<T> {
        int MIN_UPDATE_THRESHOLD = 200;

        Scanner<T> projection(String[] projection);

        Scanner<T> selection(String selection);

        Scanner<T> selectionArgs(String[] args);

        Scanner<T> sortOrder(String sortOrder);

        /**
         * 设置更新 UI 刷新的阈值时间（单位：毫秒），避免 UI 刷新速度跟不上数据流的速度。如果两个数据的发送时间
         * 间隔小于 threshold 值，本次 UI 刷新将被忽略。
         *
         * @param threshold UI 刷新的阈值，不能小于 {@link #MIN_UPDATE_THRESHOLD}
         */
        Scanner<T> updateThreshold(int threshold);

        void cancel();

        void scan(@NonNull OnScanCallback<T> callback);
    }

    public interface OnScanCallback<T> {
        void onStartScan();

        void onUpdateProgress(int progress, int max, T item);

        void onFinished(List<T> items);
    }

    public static abstract class Decoder<T> {
        public abstract T decode(Cursor cursor);

        public static int getDateAdded(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED));
        }

        public static int getDateModified(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
        }

        public static String getDisplayName(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
        }

        public static String getMimeType(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE));
        }

        public static int getSize(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE));
        }

        public static String getTitle(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE));
        }

        public static int getId(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
        }

        public static String getAudioArtist(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        }

        public static int getAudioArtistId(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST_ID));
        }

        public static String getAudioAlbum(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM));
        }

        public static int getAudioAlbumId(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID));
        }

        public static boolean audioIsAlarm(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.IS_ALARM)) != 0;
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        public static boolean audioIsAudioBook(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.IS_AUDIOBOOK)) != 0;
        }

        public static boolean audioIsMusic(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.IS_MUSIC)) != 0;
        }

        public static boolean audioIsNotification(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.IS_NOTIFICATION)) != 0;
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        public static boolean audioIsPending(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.IS_PENDING)) != 0;
        }

        public static boolean audioIsPodcast(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.IS_PODCAST)) != 0;
        }

        public static boolean audioIsRingtone(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.IS_RINGTONE)) != 0;
        }

        public static int getAudioTrack(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TRACK));
        }

        public static int getAudioYear(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.YEAR));
        }

        public static Uri getAudioUri(Cursor cursor) {
            return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, getId(cursor));
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        public static int getVideoWidth(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.WIDTH));
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        public static int getVideoHeight(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.HEIGHT));
        }

        public static String getVideoCategory(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.CATEGORY));
        }

        public static String getVideoColorRange(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DESCRIPTION));
        }

        public static boolean videoIsPrivate(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.IS_PRIVATE)) != 0;
        }

        public static String getVideoLanguage(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.LANGUAGE));
        }

        public static float getVideoLatitude(Cursor cursor) {
            return cursor.getFloat(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.LATITUDE));
        }

        public static float getVideoLongitude(Cursor cursor) {
            return cursor.getFloat(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.LONGITUDE));
        }

        public static int getVideoMiniThumbMagic(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.MINI_THUMB_MAGIC));
        }

        public static String getVideoTags(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.TAGS));
        }

        public static Uri getVideoUri(Cursor cursor) {
            return ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, getId(cursor));
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        public static int getImageWidth(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.WIDTH));
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        public static int getImageHeight(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.HEIGHT));
        }

        public static String getImageDescription(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DESCRIPTION));
        }

        public static boolean imageIsPrivate(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.IS_PRIVATE)) != 0;
        }

        public static float getImageLatitude(Cursor cursor) {
            return cursor.getFloat(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.LATITUDE));
        }

        public static float getImageLongitude(Cursor cursor) {
            return cursor.getFloat(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.LONGITUDE));
        }

        public static int getImageMiniThumbMagic(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC));
        }

        public static String getImagePicasaId(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.PICASA_ID));
        }

        public static Uri getImageUri(Cursor cursor) {
            return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, getId(cursor));
        }
    }

    private static abstract class BaseScanner<T> implements Scanner<T> {
        private String[] mProjection;
        private String mSelection;
        private String[] mSelectionArgs;
        private String mSortOrder;

        private Uri mUri;
        private ContentResolver mResolver;
        private Decoder<T> mDecoder;

        private OnScanCallback<T> mCallback;
        private Handler mMainHandler;
        private int mThreshold;

        private boolean mRunning;
        private boolean mCancelled;
        private boolean mFinished;

        private long mLastUpdateTime;

        public BaseScanner(Uri uri, ContentResolver resolver, Decoder<T> decoder) {
            mUri = uri;
            mResolver = resolver;
            mDecoder = decoder;

            mThreshold = MIN_UPDATE_THRESHOLD;
            mMainHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public Scanner<T> projection(String[] projection) {
            mProjection = projection;
            return this;
        }

        @Override
        public Scanner<T> selection(String selection) {
            mSelection = selection;
            return this;
        }

        @Override
        public Scanner<T> selectionArgs(String[] args) {
            mSelectionArgs = args;
            return this;
        }

        @Override
        public Scanner<T> sortOrder(String sortOrder) {
            mSortOrder = sortOrder;
            return this;
        }

        @Override
        public Scanner<T> updateThreshold(int threshold) {
            mThreshold = threshold;

            if (mThreshold < MIN_UPDATE_THRESHOLD) {
                mThreshold = MIN_UPDATE_THRESHOLD;
            }

            return this;
        }

        protected synchronized final boolean isRunning() {
            return mRunning;
        }

        protected synchronized final void setRunning(boolean running) {
            mRunning = running;
        }

        protected synchronized final boolean isFinished() {
            return mFinished;
        }

        protected synchronized final void setFinished(boolean finished) {
            mFinished = finished;
        }

        protected synchronized final boolean isCancelled() {
            return mCancelled;
        }

        @Override
        public synchronized final void cancel() {
            mCancelled = true;
            setRunning(false);
            setFinished(true);
        }

        @Override
        public void scan(@NonNull final OnScanCallback<T> callback) throws IllegalStateException {
            ObjectUtil.requireNonNull(callback);

            if (isRunning()) {
                throw new IllegalStateException("scanner is running.");
            }

            mCallback = callback;

            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (isCancelled() || isFinished()) {
                        return;
                    }

                    setRunning(true);
                    setFinished(false);

                    notifyStartScan();

                    Cursor cursor = mResolver.query(mUri, mProjection, mSelection, mSelectionArgs, mSortOrder);
                    if (cursor != null && cursor.moveToFirst()) {
                        forEachCursor(cursor);
                        cursor.close();
                        return;
                    }

                    notifyFinished(new ArrayList<T>());
                }
            });
        }

        private void forEachCursor(Cursor cursor) {
            int progress = 0;
            int max = cursor.getCount();
            List<T> items = new ArrayList<>(max);

            do {
                progress++;
                items.add(decode(cursor, progress, max));
            } while (cursor.moveToNext() && !isCancelled());

            notifyFinished(items);
        }

        private T decode(Cursor cursor, final int progress, final int max) {
            T item = mDecoder.decode(cursor);
            notifyProgressUpdate(progress, max, item);
            return item;
        }

        private void notifyStartScan() {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.onStartScan();
                }
            });
        }

        private void notifyProgressUpdate(final int progress, final int max, final T item) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - mLastUpdateTime < mThreshold) {
                return;
            }

            if (isCancelled() || isFinished()) {
                return;
            }

            mLastUpdateTime = currentTime;
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.onUpdateProgress(progress, max, item);
                }
            });
        }

        private void notifyFinished(final List<T> items) {
            setFinished(true);
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.onFinished(items);
                }
            });
        }
    }

    private static class AudioScanner<T> extends BaseScanner<T> {
        public AudioScanner(ContentResolver resolver, Decoder<T> decoder) {
            super(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, resolver, decoder);
        }
    }

    private static class VideoScanner<T> extends BaseScanner<T> {
        public VideoScanner(ContentResolver resolver, Decoder<T> decoder) {
            super(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, resolver, decoder);
        }
    }

    private static class ImagesScanner<T> extends BaseScanner<T> {
        public ImagesScanner(ContentResolver resolver, Decoder<T> decoder) {
            super(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, resolver, decoder);
        }
    }
}
