package com.gkxim.android.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import junit.framework.Assert;

import org.apache.http.NameValuePair;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.UIUtils;

public final class UrlImageViewHelper {

	private static final String TAG = "UrlImageViewHelper";
	private static final boolean DEBUG = GKIMLog.DEBUG_ON;

	public static int copyStream(InputStream input, OutputStream output)
			throws IOException {
		byte[] stuff = new byte[1024];
		int read = 0;
		int total = 0;
		while ((read = input.read(stuff)) != -1) {
			output.write(stuff, 0, read);
			total += read;
		}
		return total;
	}

	static Resources mResources;
	static DisplayMetrics mMetrics;

	private static void prepareResources(Context context) {
		if (mMetrics != null)
			return;
		mMetrics = new DisplayMetrics();
//		WindowManager wm = null;
//		if (context instanceof Activity) {
//			wm = ((Activity) context).getWindowManager();
//		}else {
//			wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//		}
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(mMetrics);
		AssetManager mgr = context.getAssets();
		mResources = new Resources(mgr, mMetrics, context.getResources()
				.getConfiguration());
	}

	private static Drawable loadDrawableFromStream(Context context, String url,
			String filename, int targetWidth, int targetHeight,
			boolean isscale, int scaleW, int scaleH) {
		prepareResources(context);
		try { 

			//Duy fix load OutOfmemory
			FileInputStream stream;
			// BitmapFactory.Options o = new BitmapFactory.Options();
			// o.inJustDecodeBounds = true;
			// = new FileInputStream(filename);
			// BitmapFactory.decodeStream(stream, null, o);
			// stream.close();
			stream = new FileInputStream(filename);
			// int scale = 0;
			// while ((o.outWidth >> scale) > targetWidth
			// || (o.outHeight >> scale) > targetHeight) {
			// GKIMLog.lf(null, 0, TAG + "downsampling");
			// scale++;
			// }
			// o = new Options();
			// o.inSampleSize = 1 << scale;
			// final Bitmap bitmap = BitmapFactory.decodeStream(stream, null,
			// o);
			BitmapFactory.Options bfOptions = new BitmapFactory.Options();
		    bfOptions.inDither = false;                     //Disable Dithering mode
		    bfOptions.inPurgeable = true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared\
		    bfOptions.inInputShareable = true; 
		    bfOptions.inTempStorage = new byte[8 * 1024]; 
		    bfOptions.inPreferredConfig = Config.RGB_565;
	        
	        
			BitmapDrawable bd;
			if (isscale) {
				final Bitmap bitmap = BitmapFactory.decodeStream(stream, null, bfOptions);
				Bitmap bitmapResult = null;
				if (bitmap != null) {
//					if (scaleH == -1) {
//						//Scale by width and keep aspect ratio
//						scaleH = scaleW * (bitmap.getHeight()/bitmap.getWidth());
//					}
					bitmapResult = Bitmap.createScaledBitmap(bitmap, scaleW,
							scaleH, false);
				}
				if (DEBUG)
					GKIMLog.lf(
							null,
							0,
							TAG
									+ String.format(
											"=>Loaded bitmap (%dx%d): %s.",
											bitmap.getWidth(),
											bitmap.getHeight(), url));
				bd = new BitmapDrawable(mResources, bitmapResult);
			} else {
				
				final Bitmap bitmap = BitmapFactory.decodeStream(stream, null, bfOptions);
				if (DEBUG)
					GKIMLog.lf(
							null,
							0,
							TAG
									+ String.format(
											"=>Loaded bitmap (%dx%d): %s.",
											bitmap.getWidth(),
											bitmap.getHeight(), url));
				bd = new BitmapDrawable(mResources, bitmap);
			}
			stream.close();
			return new ZombieDrawable(url, bd);
		} catch (IOException e) {
			return null;
		}
	}

	public static final int CACHE_DURATION_INFINITE = Integer.MAX_VALUE;
	public static final int CACHE_DURATION_ONE_DAY = 1000 * 60 * 60 * 24;
	public static final int CACHE_DURATION_TWO_DAYS = CACHE_DURATION_ONE_DAY * 2;
	public static final int CACHE_DURATION_THREE_DAYS = CACHE_DURATION_ONE_DAY * 3;
	public static final int CACHE_DURATION_FOUR_DAYS = CACHE_DURATION_ONE_DAY * 4;
	public static final int CACHE_DURATION_FIVE_DAYS = CACHE_DURATION_ONE_DAY * 5;
	public static final int CACHE_DURATION_SIX_DAYS = CACHE_DURATION_ONE_DAY * 6;
	public static final int CACHE_DURATION_ONE_WEEK = CACHE_DURATION_ONE_DAY * 7;

	public static void setUrlDrawable(final ImageView imageView,
			final String url, int defaultResource, boolean isscale, int scaleW,
			int scaleH) {
		setUrlDrawable(imageView.getContext(), imageView, url, defaultResource,
				CACHE_DURATION_ONE_WEEK, isscale, scaleW, scaleH);
	}

	public static void setUrlDrawable(final ImageView imageView,
			final String url, boolean isscale, int scaleW, int scaleH) {
		setUrlDrawable(imageView.getContext(), imageView, url, null,
				CACHE_DURATION_ONE_WEEK, null, isscale, scaleW, scaleH);
	}

	public static void loadUrlDrawable(final Context context, final String url,
			boolean isscale, int scaleW, int scaleH) {
		setUrlDrawable(context, null, url, null, CACHE_DURATION_ONE_WEEK, null,
				isscale, scaleW, scaleH);
	}

	public static void setUrlDrawable(final ImageView imageView,
			final String url, Drawable defaultDrawable, boolean isscale,
			int scaleW, int scaleH) {
		setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable,
				CACHE_DURATION_ONE_WEEK, null, isscale, scaleW, scaleH);
	}

	public static void setUrlDrawable(final ImageView imageView,
			final String url, int defaultResource, long cacheDurationMs,
			boolean isscale, int scaleW, int scaleH) {
		setUrlDrawable(imageView.getContext(), imageView, url, defaultResource,
				cacheDurationMs, isscale, scaleW, scaleH);
	}

	public static void loadUrlDrawable(final Context context, final String url,
			long cacheDurationMs, boolean isscale, int scaleW, int scaleH) {
		setUrlDrawable(context, null, url, null, cacheDurationMs, null,
				isscale, scaleW, scaleH);
	}

	public static void setUrlDrawable(final ImageView imageView,
			final String url, Drawable defaultDrawable, long cacheDurationMs,
			boolean isscale, int scaleW, int scaleH) {
		setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable,
				cacheDurationMs, null, isscale, scaleW, scaleH);
	}

	private static void setUrlDrawable(final Context context,
			final ImageView imageView, final String url, int defaultResource,
			long cacheDurationMs, boolean isscale, int scaleW, int scaleH) {
		Drawable d = null;
		if (defaultResource != 0)
			d = imageView.getResources().getDrawable(defaultResource);
		setUrlDrawable(context, imageView, url, d, cacheDurationMs, null,
				isscale, scaleW, scaleH);
	}

	public static void setUrlDrawable(final ImageView imageView,
			final String url, int defaultResource,
			UrlImageViewCallback callback, boolean isscale, int scaleW,
			int scaleH) {
		setUrlDrawable(imageView.getContext(), imageView, url, defaultResource,
				CACHE_DURATION_ONE_WEEK, callback, isscale, scaleW, scaleH);
	}

	public static void setUrlDrawable(final ImageView imageView,
			final String url, UrlImageViewCallback callback, boolean isscale,
			int scaleW, int scaleH) {
		setUrlDrawable(imageView.getContext(), imageView, url, null,
				CACHE_DURATION_ONE_WEEK, callback, isscale, scaleW, scaleH);
	}

	public static void loadUrlDrawable(final Context context, final String url,
			UrlImageViewCallback callback, boolean isscale, int scaleW,
			int scaleH) {
		setUrlDrawable(context, null, url, null, CACHE_DURATION_ONE_WEEK,
				callback, isscale, scaleW, scaleH);
	}

	public static void loadUrlDrawableWithId(final Context context,
			final String url, UrlImageViewCallback callback, boolean isscale,
			int scaleW, int scaleH, String id) {
		setUrlDrawableWithId(context, null, url, null, CACHE_DURATION_ONE_WEEK,
				callback, isscale, scaleW, scaleH, id);
	}

	public static void setUrlDrawable(final ImageView imageView,
			final String url, Drawable defaultDrawable,
			UrlImageViewCallback callback, boolean isscale, int scaleW,
			int scaleH) {
		setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable,
				CACHE_DURATION_ONE_WEEK, callback, isscale, scaleW, scaleH);
	}

	public static void setUrlDrawable(final ImageView imageView,
			final String url, int defaultResource, long cacheDurationMs,
			UrlImageViewCallback callback, boolean isscale, int scaleW,
			int scaleH) {
		setUrlDrawable(imageView.getContext(), imageView, url, defaultResource,
				cacheDurationMs, callback, isscale, scaleW, scaleH);
	}

	public static void loadUrlDrawable(final Context context, final String url,
			long cacheDurationMs, UrlImageViewCallback callback,
			boolean isscale, int scaleW, int scaleH) {
		setUrlDrawable(context, null, url, null, cacheDurationMs, callback,
				isscale, scaleW, scaleH);
	}

	public static void setUrlDrawable(final ImageView imageView,
			final String url, Drawable defaultDrawable, long cacheDurationMs,
			UrlImageViewCallback callback, boolean isscale, int scaleW,
			int scaleH) {
		setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable,
				cacheDurationMs, callback, isscale, scaleW, scaleH);
	}

	private static void setUrlDrawable(final Context context,
			final ImageView imageView, final String url, int defaultResource,
			long cacheDurationMs, UrlImageViewCallback callback,
			boolean isscale, int scaleW, int scaleH) {
		Drawable d = null;
		if (defaultResource != 0)
			d = imageView.getResources().getDrawable(defaultResource);
		setUrlDrawable(context, imageView, url, d, cacheDurationMs, callback,
				isscale, scaleW, scaleH);
	}

	private static boolean isNullOrEmpty(CharSequence s) {
		return (s == null || s.equals("") || s.equals("null") || s
				.equals("NULL"));
	}

	private static boolean mHasCleaned = false;

	public static String getFilenameForUrl(String url) {
		return "" + url.hashCode() + ".urlimage";
	}

	private static void cleanup(Context context) {
		if (mHasCleaned)
			return;
		mHasCleaned = true;
		try {
			// purge any *.urlimage files over a week old
			String[] files = context.getFilesDir().list();
			if (files == null)
				return;
			for (String file : files) {
				if (!file.endsWith(".urlimage"))
					continue;

				File f = new File(context.getFilesDir().getAbsolutePath() + '/'
						+ file);
				if (System.currentTimeMillis() > f.lastModified()
						+ CACHE_DURATION_ONE_WEEK)
					f.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void setUrlDrawable(final Context context,
			final ImageView imageView, final String url,
			final Drawable defaultDrawable, long cacheDurationMs,
			final UrlImageViewCallback callback, final boolean isscale,
			final int scaleW, final int scaleH) {
		setUrlDrawableWithId(context, imageView, url, defaultDrawable,
				cacheDurationMs, callback, isscale, scaleW, scaleH, "");
	}

	private static void setUrlDrawableWithId(final Context context,
			final ImageView imageView, final String url,
			final Drawable defaultDrawable, long cacheDurationMs,
			final UrlImageViewCallback callback, final boolean isscale,
			final int scaleW, final int scaleH, final String id) {
		cleanup(context);
		// disassociate this ImageView from any pending downloads
		if (isNullOrEmpty(url)) {
			if (imageView != null)
				imageView.setImageDrawable(defaultDrawable);
			return;
		}

		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		final int tw = display.getWidth();
		final int th = display.getHeight();

		if (mDeadCache == null)
			mDeadCache = new UrlLruCache(getHeapSize(context) / 8);
		Drawable drawable;
		BitmapDrawable zd = mDeadCache.remove(url);
		if (zd != null) {
			// this drawable was resurrected, it should not be in the live cache
			if (DEBUG)
				GKIMLog.lf(null, 0, TAG + "zombie load");
			Assert.assertTrue(!mAllCache.contains(zd));
			drawable = new ZombieDrawable(url, zd);
		} else {
			drawable = mLiveCache.get(url);
		}

		if (drawable != null) {
			if (DEBUG)
				GKIMLog.lf(null, 0, TAG + " Cache hit on: " + url);
			if (imageView != null) {
				imageView.setImageDrawable(drawable);
			}
			if (callback != null)
				callback.onLoaded(imageView, drawable, url, true, id);
			return;
		}

		// oh noes, at this point we definitely do not have the file available
		// in memory
		// let's prepare for an asynchronous load of the image.

		final String filename = context.getFileStreamPath(
				getFilenameForUrl(url)).getAbsolutePath();

		// null it while it is downloading
		if (imageView != null) {
			imageView.setImageDrawable(defaultDrawable);
		}

		// since listviews reuse their views, we need to
		// take note of which url this view is waiting for.
		// This may change rapidly as the list scrolls or is filtered, etc.
		if (DEBUG)
			GKIMLog.lf(null, 0, TAG + "Waiting for " + url);
		if (imageView != null)
			mPendingViews.put(imageView, url);

		ArrayList<ImageView> currentDownload = mPendingDownloads.get(url);
		if (currentDownload != null) {
			// Also, multiple vies may be waiting for this url.
			// So, let's maintain a list of these views.
			// When the url is downloaded, it sets the imagedrawable for
			// every view in the list. It needs to also validate that
			// the imageview is still waiting for this url.
			if (imageView != null)
				currentDownload.add(imageView);
			return;
		}

		final ArrayList<ImageView> downloads = new ArrayList<ImageView>();
		if (imageView != null)
			downloads.add(imageView);
		mPendingDownloads.put(url, downloads);

		final int targetWidth = tw <= 0 ? Integer.MAX_VALUE : tw;
		final int targetHeight = th <= 0 ? Integer.MAX_VALUE : th;
		final Loader loader = new Loader() {
			@Override
			public void run() {
				try {
					result = loadDrawableFromStream(context, url, filename,
							targetWidth, targetHeight, isscale, scaleW, scaleH);
				} catch (Exception ex) {
					GKIMLog.lf(null, 0, TAG
							+ "=>loadDrawableFromStream has failed on: " + url + " with exception: " + ex.getMessage());
				}
			}
		};

		final Runnable completion = new Runnable() {
			@Override
			public void run() {
				Assert.assertEquals(Looper.myLooper(), Looper.getMainLooper());
				Drawable usableResult = loader.result;
				if (usableResult == null)
					usableResult = defaultDrawable;
				mPendingDownloads.remove(url);
				mLiveCache.put(url, usableResult);
				if (downloads.size() > 0) {
					for (ImageView iv : downloads) {
						// validate the url it is waiting for
						String pendingUrl = mPendingViews.get(iv);
						if (!url.equals(pendingUrl)) {
							if (DEBUG)
								GKIMLog.lf(
										null,
										0,
										TAG
												+ "Ignoring out of date request to update view for "
												+ url);
							continue;
						}
						mPendingViews.remove(iv);
						if (usableResult != null) {
							GKIMLog.lf(iv.getContext(), 0, TAG + "=>complete set image: " + url);
							iv.setImageDrawable(usableResult);
							if (callback != null)
								callback.onLoaded(iv, loader.result, url, false, id);

						}
					}
				}
				if (loader.result != null && callback != null)
					callback.onLoaded(null, loader.result, url, false, id);
			}
		};

		File file = new File(filename);
		if (file.exists()) {
			try {
				if (cacheDurationMs == CACHE_DURATION_INFINITE
						|| System.currentTimeMillis() < file.lastModified()
								+ cacheDurationMs) {
					GKIMLog.lf(
							null,
							0,
							TAG
									+ " File Cache hit on: "
									+ url
									+ ". "
									+ (System.currentTimeMillis() - file
											.lastModified()) + "ms old.");

					AsyncTask<Void, Void, Void> fileloader = new AsyncTask<Void, Void, Void>() {
						protected Void doInBackground(Void[] params) {
							loader.run();
							return null;
						}

						protected void onPostExecute(Void result) {
							completion.run();
						}
					};
					executeTask(fileloader);
					return;
				} else {
					GKIMLog.lf(null, 0, TAG
							+ " File cache has expired. Refreshing.");
				}
			} catch (Exception ex) {
			}
		}

		mDownloader.download(context, url, filename, loader, completion);
	}

	private static abstract class Loader implements Runnable {
		public Drawable result;
	}

	public static interface UrlDownloader {
		public void download(Context context, String url, String filename,
				Runnable loader, Runnable completion);
	}

	private static UrlDownloader mDefaultDownloader = new UrlDownloader() {
		@Override
		public void download(final Context context, final String url,
				final String filename, final Runnable loader,
				final Runnable completion) {
			AsyncTask<Void, Void, Void> downloader = new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					try {
						InputStream is = null;
						if (url.startsWith(ContactsContract.Contacts.CONTENT_URI
								.toString())) {
							ContentResolver cr = context.getContentResolver();
							is = ContactsContract.Contacts
									.openContactPhotoInputStream(cr,
											Uri.parse(url));
						} else {
							URL u = new URL(url);
							HttpURLConnection urlConnection = (HttpURLConnection) u
									.openConnection();

							if (mRequestPropertiesCallback != null) {
								ArrayList<NameValuePair> props = mRequestPropertiesCallback
										.getHeadersForRequest(context, url);
								if (props != null) {
									for (NameValuePair pair : props) {
										urlConnection
												.addRequestProperty(
														pair.getName(),
														pair.getValue());
									}
								}
							}

							if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK)
								return null;
							is = urlConnection.getInputStream();
						}

						if (is != null) {
							FileOutputStream fos = new FileOutputStream(
									filename);
							copyStream(is, fos);
							fos.close();
							is.close();
						}
						loader.run();
						return null;
					} catch (Throwable e) {
						e.printStackTrace();
						return null;
					}
				}

				protected void onPostExecute(Void result) {
					completion.run();
				}
			};

			executeTask(downloader);
		}
	};

	/**
	 * Scale the image in its view. 
	 * Reference from Argillander - Scale image into ImageView
	 * @param view
	 * @param boxBoundary
	 * @param drawing
	 */
	public static void scaleImage(ImageView view, int boxBoundary, Drawable drawing)
	{
	    // Get the ImageView and its bitmap
//	    Drawable drawing = view.getDrawable();
	    Bitmap bitmap = ((ZombieDrawable)drawing).getBitmap();

	    if (bitmap == null) return;
	    
	    // Get current dimensions
	    int width = bitmap.getWidth();
	    int height = bitmap.getHeight();

	    // Determine how much to scale: the dimension requiring less scaling is
	    // closer to the its side. This way the image always stays inside your
	    // bounding box AND either x/y axis touches it.
	    float xScale = ((float) boxBoundary) / width;
	    float yScale = ((float) boxBoundary) / height;
	    float scale = (xScale <= yScale) ? xScale : yScale;

	    // Create a matrix for the scaling and add the scaling data
	    Matrix matrix = new Matrix();
//	    matrix.postScale(scale, scale);
	    matrix.postScale(xScale, xScale);

	    // Create a new bitmap and convert it to a format understood by the ImageView
	    //FIXME: there still have an issue on outofmemory here
	    Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
	    BitmapDrawable result = new BitmapDrawable(mResources, scaledBitmap);
	    width = scaledBitmap.getWidth();
	    height = scaledBitmap.getHeight();

	    // Apply the scaled bitmap
	    view.setImageDrawable(result);
	    // Now change ImageView's dimensions to match the scaled image
	    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
	    params.width = width;
	    params.height = height;
	    view.setLayoutParams(params);
	    bitmap.recycle();
	}
	
	static public interface RequestPropertiesCallback {
		public ArrayList<NameValuePair> getHeadersForRequest(Context context,
				String url);
	}

	static private RequestPropertiesCallback mRequestPropertiesCallback;

	static public RequestPropertiesCallback getRequestPropertiesCallback() {
		return mRequestPropertiesCallback;
	}

	static public void setRequestPropertiesCallback(
			RequestPropertiesCallback callback) {
		mRequestPropertiesCallback = callback;
	}

	public static void useDownloader(UrlDownloader downloader) {
		mDownloader = downloader;
	}

	public static void useDefaultDownloader() {
		mDownloader = mDefaultDownloader;
	}

	public static UrlDownloader getDefaultDownloader() {
		return mDownloader;
	}

	private static UrlImageCache mLiveCache = UrlImageCache.getInstance();

	private static UrlLruCache mDeadCache;
	private static HashSet<BitmapDrawable> mAllCache = new HashSet<BitmapDrawable>();

	private static int getHeapSize(Context context) {
		return ((ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass() * 1024 * 1024;
	}

	private static class ZombieDrawable extends WrapperDrawable {
		public ZombieDrawable(String url, BitmapDrawable drawable) {
			super(drawable);
			mUrl = url;

			mAllCache.add(drawable);
			mDeadCache.remove(url);
			mLiveCache.put(url, this);
		}

		String mUrl;

		@Override
		protected void finalize() throws Throwable {
			super.finalize();

			mDeadCache.put(mUrl, mDrawable);
			mAllCache.remove(mDrawable);
			mLiveCache.remove(mUrl);
			if (DEBUG)
				GKIMLog.lf(null, 0, TAG + "Zombie GC event");
		}
	}

	private static UrlDownloader mDownloader = mDefaultDownloader;

	private static void executeTask(AsyncTask<Void, Void, Void> task) {
		if (!UIUtils.hasHoneycomb()) {
			task.execute();
		} else {
			executeTaskHoneycomb(task);
		}
	}

	@TargetApi(11)
	private static void executeTaskHoneycomb(AsyncTask<Void, Void, Void> task) {
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private static Hashtable<ImageView, String> mPendingViews = new Hashtable<ImageView, String>();
	private static Hashtable<String, ArrayList<ImageView>> mPendingDownloads = new Hashtable<String, ArrayList<ImageView>>();
}
