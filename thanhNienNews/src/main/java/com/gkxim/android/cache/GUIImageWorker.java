/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gkxim.android.cache;

import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.ImageView;

import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.UIUtils;

/**
 * This class wraps up completing some arbitrary long running work when loading
 * a bitmap to an ImageView. It handles things like using a memory and disk
 * cache, running the work in a background thread and setting a placeholder
 * image.
 */
@Deprecated
public abstract class GUIImageWorker {
	private static final String TAG = "ImageWorker";
	private static final int FADE_IN_TIME = 300;
	private static final boolean DEBUG = GKIMLog.DEBUG_ON;

	protected GUIImageCache mImageCache;
	protected GUIImageCache.ImageCacheParams mImageCacheParams;
	protected Bitmap mLoadingBitmap;
	protected boolean mFadeInBitmap = true;
	private boolean mExitTasksEarly = false;
	protected boolean mPauseWork = false;
	private final Object mPauseWorkLock = new Object();
	private final Hashtable<Integer, Bitmap> loadingBitmaps = new Hashtable<Integer, Bitmap>(
			2);

	protected Resources mResources;

	private static final int MESSAGE_CLEAR = 0;
	private static final int MESSAGE_INIT_DISK_CACHE = 1;
	private static final int MESSAGE_FLUSH = 2;
	private static final int MESSAGE_CLOSE = 3;

	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		public Thread newThread(Runnable r) {
			return new Thread(r, "AsyncTask_ImageWorker#"
					+ mCount.getAndIncrement());
		}
	};

	// Dual thread executor for main AsyncTask
	public static final Executor DUAL_THREAD_EXECUTOR = Executors
			.newFixedThreadPool(2, sThreadFactory);

	protected GUIImageWorker(Context context) {
		mResources = context.getResources();
	}

	/**
	 * Load an image specified by the data parameter into an ImageView (override
	 * {@link GUIImageWorker#processBitmap(Object)} to define the processing
	 * logic). A memory and disk cache will be used if an {@link GUIImageCache} has
	 * been set using {@link GUIImageWorker#addImageCache}. If the image is found
	 * in the memory cache, it is set immediately, otherwise an
	 * {@link AsyncTask} will be created to asynchronously load the bitmap.
	 * 
	 * @param data
	 *            The URL of the image to download.
	 * @param imageView
	 *            The ImageView to bind the downloaded image to.
	 */
	protected void loadImage(Object data, ImageView imageView) {
		loadImage(data, imageView, mLoadingBitmap);
	}

	/**
	 * Load an image specified by the data parameter into an ImageView (override
	 * {@link GUIImageWorker#processBitmap(Object)} to define the processing
	 * logic). A memory and disk cache will be used if an {@link GUIImageCache} has
	 * been set using {@link GUIImageWorker#addImageCache}. If the image is found
	 * in the memory cache, it is set immediately, otherwise an
	 * {@link AsyncTask} will be created to asynchronously load the bitmap.
	 * 
	 * @param data
	 *            The URL of the image to download.
	 * @param imageView
	 *            The ImageView to bind the downloaded image to.
	 * @param resId
	 *            Resource of placeholder bitmap while the image loads.
	 */
	protected void loadImage(Object data, ImageView imageView, int resId) {
		if (!loadingBitmaps.containsKey(resId)) {
			// Store loading bitmap in a hash table to prevent continual
			// decoding
			loadingBitmaps.put(resId,
					BitmapFactory.decodeResource(mResources, resId));
		}
		loadImage(data, imageView, loadingBitmaps.get(resId));
	}

	/**
	 * Load an image specified by the data parameter into an ImageView (override
	 * {@link GUIImageWorker#processBitmap(Object)} to define the processing
	 * logic). A memory and disk cache will be used if an {@link GUIImageCache} has
	 * been set using {@link GUIImageWorker#addImageCache}. If the image is found
	 * in the memory cache, it is set immediately, otherwise an
	 * {@link AsyncTask} will be created to asynchronously load the bitmap.
	 * 
	 * @param data
	 *            The URL of the image to download.
	 * @param imageView
	 *            The ImageView to bind the downloaded image to.
	 */
	public void loadImage(Object data, ImageView imageView, Bitmap loadingBitmap) {
		if (data == null) {
			return;
		}

		Bitmap bitmap = null;

		if (mImageCache != null) {
			bitmap = mImageCache.getBitmapFromMemCache(String.valueOf(data));
		}

		if (bitmap != null) {
			// Bitmap found in memory cache
			// imageView.setImageBitmap(bitmap);
			setImageBitmap(imageView, bitmap, data);
		} else if (cancelPotentialWork(data, imageView)) {
			final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(mResources,
					loadingBitmap, task);
			imageView.setImageDrawable(asyncDrawable);

			if (UIUtils.hasHoneycomb()) {
				// On HC+ we execute on a dual thread executor. There really
				// isn't much extra
				// benefit to having a really large pool of threads. Having more
				// than one will
				// likely benefit network bottlenecks though.
				task.executeOnExecutor(DUAL_THREAD_EXECUTOR, data);
			} else {
				// Otherwise pre-HC the default is a thread pool executor (not
				// ideal, serial
				// execution or a smaller number of threads would be better).
				task.execute(data);
			}
		}
	}

	public void loadImage(Object data, AsynTaskCallback callback) {
		if (data == null) {
			return;
		}
		Bitmap bitmap = null;

		if (mImageCache != null) {
			bitmap = mImageCache.getBitmapFromMemCache(String.valueOf(data));
		}

		if (bitmap != null) {
			// Bitmap found in memory cache
			if (callback != null) {
				callback.run(String.valueOf(data), bitmap);
			}
		} else {
			final BitmapWorkerTask task = new BitmapWorkerTask(callback);
			if (UIUtils.hasHoneycomb()) {
				// On HC+ we execute on a dual thread executor. There really
				// isn't much extra
				// benefit to having a really large pool of threads. Having more
				// than one will
				// likely benefit network bottlenecks though.
				task.executeOnExecutor(DUAL_THREAD_EXECUTOR, data);
			} else {
				// Otherwise pre-HC the default is a thread pool executor (not
				// ideal, serial
				// execution or a smaller number of threads would be better).
				task.execute(data);
			}
		}
	}

	/**
	 * Set placeholder bitmap that shows when the the background thread is
	 * running.
	 * 
	 * @param bitmap
	 */
	public void setLoadingImage(Bitmap bitmap) {
		mLoadingBitmap = bitmap;
	}

	/**
	 * Set placeholder bitmap that shows when the the background thread is
	 * running.
	 * 
	 * @param resId
	 */
	public void setLoadingImage(int resId) {
		mLoadingBitmap = BitmapFactory.decodeResource(mResources, resId);
	}

	/**
	 * Adds an {@link GUIImageCache} to this worker in the background (to prevent
	 * disk access on UI thread).
	 * 
	 * @param fragmentManager
	 *            The FragmentManager to initialize and add the cache
	 * @param cacheParams
	 *            The cache parameters to use
	 */
	public void addImageCache(FragmentManager fragmentManager,
			GUIImageCache.ImageCacheParams cacheParams) {
		mImageCacheParams = cacheParams;
		setImageCache(GUIImageCache.findOrCreateCache(fragmentManager,
				mImageCacheParams));
		new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
	}

	/**
	 * Adds an {@link GUIImageCache} to this worker in the background (to prevent
	 * disk access on UI thread) using default cache parameters.
	 * 
	 * @param fragmentActivity
	 *            The FragmentActivity to initialize and add the cache
	 */
	public void addImageCache(FragmentActivity fragmentActivity) {
		addImageCache(fragmentActivity.getSupportFragmentManager(),
				new GUIImageCache.ImageCacheParams(fragmentActivity));
	}

	/**
	 * Sets the {@link GUIImageCache} object to use with this ImageWorker. Usually
	 * you will not need to call this directly, instead use
	 * {@link GUIImageWorker#addImageCache} which will create and add the
	 * {@link GUIImageCache} object in a background thread (to ensure no disk
	 * access on the main/UI thread).
	 * 
	 * @param imageCache
	 */
	public void setImageCache(GUIImageCache imageCache) {
		mImageCache = imageCache;
	}

	/**
	 * If set to true, the image will fade-in once it has been loaded by the
	 * background thread.
	 */
	public void setImageFadeIn(boolean fadeIn) {
		mFadeInBitmap = fadeIn;
	}

	/**
	 * Setting this to true will signal the working tasks to exit processing at
	 * the next chance. This helps finish up pending work when the activity is
	 * no longer in the foreground and completing the tasks is no longer useful.
	 * 
	 * @param exitTasksEarly
	 */
	public void setExitTasksEarly(boolean exitTasksEarly) {
		mExitTasksEarly = exitTasksEarly;
	}

	/**
	 * Subclasses should override this to define any processing or work that
	 * must happen to produce the final bitmap. This will be executed in a
	 * background thread and be long running. For example, you could resize a
	 * large bitmap here, or pull down an image from the network.
	 * 
	 * @param data
	 *            The data to identify which image to process, as provided by
	 *            {@link GUIImageWorker#loadImage(Object, ImageView)}
	 * @return The processed bitmap
	 */
	protected abstract Bitmap processBitmap(Object data);

	/**
	 * Cancels any pending work attached to the provided ImageView.
	 * 
	 * @param imageView
	 */
	public static void cancelWork(ImageView imageView) {
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
		if (bitmapWorkerTask != null) {
			bitmapWorkerTask.cancel(true);
			if (DEBUG) {
				GKIMLog.l(0, TAG + "=> cancelWork - cancelled work for "
						+ bitmapWorkerTask.data);
			}
		}
	}

	/**
	 * Returns true if the current work has been canceled or if there was no
	 * work in progress on this image view. Returns false if the work in
	 * progress deals with the same data. The work is not stopped in that case.
	 */
	public static boolean cancelPotentialWork(Object data, ImageView imageView) {
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {
			final Object bitmapData = bitmapWorkerTask.data;
			if (bitmapData == null || !bitmapData.equals(data)) {
				bitmapWorkerTask.cancel(true);
				GKIMLog.l(0, TAG
						+ "=> cancelPotentialWork - cancelled work for " + data);
			} else {
				// The same work is already in progress.
				return false;
			}
		}
		return true;
	}

	/**
	 * @param imageView
	 *            Any imageView
	 * @return Retrieve the currently active work task (if any) associated with
	 *         this imageView. null if there is no such task.
	 */
	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}

	/**
	 * The actual AsyncTask that will asynchronously process the image.
	 */
	private class BitmapWorkerTask extends AsyncTask<Object, Void, Bitmap> {
		private Object data;
		private final WeakReference<ImageView> imageViewReference;
		private AsynTaskCallback asynTaskCallback;

		public BitmapWorkerTask(ImageView imageView) {
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		public BitmapWorkerTask(AsynTaskCallback callback) {
			imageViewReference = null;
			asynTaskCallback = callback;
		}
		
//		public BitmapWorkerTask(ImageView imageView, AsynTaskCallback callback) {
//			imageViewReference = new WeakReference<ImageView>(imageView);
//			asynTaskCallback = callback;
//		} 
		
		/**
		 * Background processing.
		 */
		@Override
		protected Bitmap doInBackground(Object... params) {
			GKIMLog.l(0, TAG + "=> doInBackground - starting work");

			data = params[0];
			final String dataString = String.valueOf(data);
			Bitmap bitmap = null;

			// Wait here if work is paused and the task is not cancelled
			synchronized (mPauseWorkLock) {
				while (mPauseWork && !isCancelled()) {
					try {
						mPauseWorkLock.wait();
					} catch (InterruptedException e) {
					}
				}
			}

			// If the image cache is available and this task has not been
			// cancelled by another
			// thread and the ImageView that was originally bound to this task
			// is still bound back
			// to this task and our "exit early" flag is not set then try and
			// fetch the bitmap from
			// the cache
			if (mImageCache != null && !isCancelled()
					&& !mExitTasksEarly) {
				bitmap = mImageCache.getBitmapFromDiskCache(dataString);
//				if (getAttachedImageView() != null) {
//				itmap = mImageCache.getBitmapFromDiskCache(dataString);
//				}
			}

			// If the bitmap was not found in the cache and this task has not
			// been cancelled by
			// another thread and the ImageView that was originally bound to
			// this task is still
			// bound back to this task and our "exit early" flag is not set,
			// then call the main
			// process method (as implemented by a subclass)
			if (bitmap == null && !isCancelled()
					&& getAttachedImageView() != null && !mExitTasksEarly) {
				bitmap = processBitmap(params[0]);
			}

			// If the bitmap was processed and the image cache is available,
			// then add the processed
			// bitmap to the cache for future use. Note we don't check if the
			// task was cancelled
			// here, if it was, and the thread is still running, we may as well
			// add the processed
			// bitmap to our cache as it might be used again in the future
			if (bitmap != null && mImageCache != null) {
				mImageCache.addBitmapToCache(dataString, bitmap);
			}

			GKIMLog.l(0, TAG + "=> doInBackground - finished work");
			return bitmap;
		}

		/**
		 * Once the image is processed, associates it to the imageView
		 */
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			// if cancel was called on this task or the "exit early" flag is set
			// then we're done
			if (isCancelled() || mExitTasksEarly) {
				bitmap = null;
			}

			final ImageView imageView = getAttachedImageView();
			if (bitmap != null) {
				if (imageView != null) {
					GKIMLog.l(0, TAG + "=> onPostExecute - setting bitmap");
					// setImageBitmap(imageView, bitmap);
					setImageBitmap(imageView, bitmap, data);
				}
				if (asynTaskCallback != null) {
					GKIMLog.l(0, TAG + "=> onPostExecute - invoke callback");
					asynTaskCallback.run(String.valueOf(data), bitmap);
				}
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			synchronized (mPauseWorkLock) {
				mPauseWorkLock.notifyAll();
			}
		}

		/**
		 * Returns the ImageView associated with this task as long as the
		 * ImageView's task still points to this task as well. Returns null
		 * otherwise.
		 */
		private ImageView getAttachedImageView() {
			if (imageViewReference == null) {
				return null;
			}
			final ImageView imageView = imageViewReference.get();
			final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

			if (this == bitmapWorkerTask) {
				return imageView;
			}

			return null;
		}
	}

	/**
	 * A custom Drawable that will be attached to the imageView while the work
	 * is in progress. Contains a reference to the actual worker task, so that
	 * it can be stopped if a new binding is required, and makes sure that only
	 * the last started worker process can bind its result, independently of the
	 * finish order.
	 */
	private static class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

		public AsyncDrawable(Resources res, Bitmap bitmap,
				BitmapWorkerTask bitmapWorkerTask) {
			super(res, bitmap);
			bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(
					bitmapWorkerTask);
		}

		public BitmapWorkerTask getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

	public interface AsynTaskCallback {
		void run(String key, Bitmap result);
	}

	/**
	 * Called when the processing is complete and the final bitmap should be set
	 * on the ImageView.
	 * 
	 * @param imageView
	 * @param bitmap
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(16)
	private void setImageBitmap(ImageView imageView, Bitmap bitmap,
			boolean bBackground) {

		if (bBackground) {
			if (UIUtils.hasJellyBean()) {
				imageView.setBackground(new BitmapDrawable(mResources, bitmap));
			} else {
				imageView.setBackgroundDrawable(new BitmapDrawable(mResources,
						bitmap));
			}
			imageView.setImageBitmap(null);
		} else {
			Bitmap scaledBefore = Bitmap.createScaledBitmap(bitmap,
					imageView.getWidth(), imageView.getHeight(), false);
			if (mFadeInBitmap) {
				TransitionDrawable td = new TransitionDrawable(new Drawable[] {
						new ColorDrawable(android.R.color.transparent),
						new BitmapDrawable(mResources, scaledBefore) });
				td.setCrossFadeEnabled(true);
				imageView.setImageDrawable(td);
				if (DEBUG) {
					td.startTransition(FADE_IN_TIME * 10);
				} else {
					td.startTransition(FADE_IN_TIME);
				}
			} else {
				imageView.setImageBitmap(scaledBefore);
			}
		}

		// if (mFadeInBitmap) {
		// // Use TransitionDrawable to fade in
		// // noinspection deprecation
		// // imageView.setBackgroundDrawable(imageView.getDrawable());
		// if (bBackground) {
		// if (UIUtils.hasJellyBean()) {
		// imageView.setBackground(new BitmapDrawable(mResources, bitmap));
		// }else {
		// imageView.setBackgroundDrawable(new BitmapDrawable(mResources,
		// bitmap));
		// }
		// }else {
		// Bitmap scaledBefore = Bitmap.createScaledBitmap(bitmap,
		// imageView.getWidth(), imageView.getHeight(), false);
		// final TransitionDrawable td = new TransitionDrawable(
		// new Drawable[] {
		// new ColorDrawable(android.R.color.transparent),
		// new BitmapDrawable(mResources, scaledBefore) });
		// imageView.setImageDrawable(td);
		// td.startTransition(FADE_IN_TIME);
		// }
		// } else {
		// // if (bBackground) {
		// // if (UIUtils.hasJellyBean()) {
		// // imageView.setBackground(new BitmapDrawable(mResources,
		// // scaledBefore));
		// // }else {
		// // imageView.setBackgroundDrawable(new BitmapDrawable(mResources,
		// bitmap));
		// // }
		// // }else {
		// // imageView.setImageBitmap(scaledBefore);
		// // }
		// }
		GKIMLog.lf(imageView.getContext(), 5, TAG
				+ "=>setImageBitmap: image size = " + imageView.getWidth()
				+ ", " + imageView.getHeight());
	}

	private void setImageBitmap(ImageView imageView, Bitmap bitmap, Object data) {
		boolean bBackground = false;
		if (imageView.getWidth() == 0 || imageView.getHeight() == 0) {
			String imagelink = "";
			if (data instanceof String) {
				imagelink = (String) data;
			} else {
				imagelink = data.toString();
			}
			bBackground = true;
			GKIMLog.lf(null, 4, TAG + "=>setImageBitmap: failed if size = 0."
					+ imagelink);
		}
		setImageBitmap(imageView, bitmap, bBackground);

	}

	public void setPauseWork(boolean pauseWork) {
		synchronized (mPauseWorkLock) {
			mPauseWork = pauseWork;
			if (!mPauseWork) {
				mPauseWorkLock.notifyAll();
			}
		}
	}

	protected class CacheAsyncTask extends AsyncTask<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... params) {
			switch ((Integer) params[0]) {
			case MESSAGE_CLEAR:
				clearCacheInternal();
				break;
			case MESSAGE_INIT_DISK_CACHE:
				initDiskCacheInternal();
				break;
			case MESSAGE_FLUSH:
				flushCacheInternal();
				break;
			case MESSAGE_CLOSE:
				closeCacheInternal();
				break;
			}
			return null;
		}
	}

	protected void initDiskCacheInternal() {
		if (mImageCache != null) {
			mImageCache.initDiskCache();
		}
	}

	protected void clearCacheInternal() {
		if (mImageCache != null) {
			mImageCache.clearCache();
		}
	}

	protected void flushCacheInternal() {
		if (mImageCache != null) {
			mImageCache.flush();
		}
	}

	protected void closeCacheInternal() {
		if (mImageCache != null) {
			mImageCache.close();
			mImageCache = null;
		}
	}

	public void clearCache() {
		new CacheAsyncTask().execute(MESSAGE_CLEAR);
	}

	public void flushCache() {
		new CacheAsyncTask().execute(MESSAGE_FLUSH);
	}

	public void closeCache() {
		new CacheAsyncTask().execute(MESSAGE_CLOSE);
	}
}
