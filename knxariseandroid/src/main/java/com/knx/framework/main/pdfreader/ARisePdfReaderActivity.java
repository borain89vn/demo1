package com.knx.framework.main.pdfreader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.artifex.mupdflib.MuPDFCore;
import com.artifex.mupdflib.MuPDFPageAdapter;
import com.knx.framework.R;
import com.knx.framework.helper.ARiseConfigs;
import com.knx.framework.main.Shared;

public class ARisePdfReaderActivity extends Activity {

	private static final String TAG = "ARiseCustomizedPdfReader";

	public static final String PDF_URL_KEY = "PDF_URL_KEY";
	
	private ProgressDialog loadingDialog;

	private File tmpPdfFile;

	private MuPDFCore core;
	private ARisePdfReaderView mDocView;
	private RelativeLayout pdfReaderViewContainer;

	private HorizontalScrollView scrollView;
	
	private RelativeLayout bottomPanel;
	private ImageButton pageSliderButton;
	private String mFileName;
	private TextView filenameTextView;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.arise_pdf_reader_activity);
		
		loadingDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
		loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		loadingDialog.setCancelable(false);
		loadingDialog.setTitle(null);
		loadingDialog.setMessage(getResources().getString(R.string.txtLoading));
		loadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				Uri uri = Uri.parse(tmpPdfFile.getAbsolutePath());
				core = ARisePdfReaderActivity.this.openFile(Uri.decode(uri.getEncodedPath()));
				createUI();
			}
		});
		loadingDialog.show();
		
		bottomPanel = (RelativeLayout) findViewById(R.id.pdf_reader_view_bottom_panel);
		filenameTextView = (TextView) findViewById(R.id.pdf_reader_view_bottom_panel_file_name);
		pageSliderButton = (ImageButton) findViewById(R.id.pdf_reader_view_bottom_panel_button);
		
		bottomPanel.setBackgroundColor(ARiseConfigs.THEME_COLOR);
		pageSliderButton.setEnabled(false);

		// get the pdf url
		String pdfUrl = getIntent().getExtras().getString(PDF_URL_KEY);
		Log.i(TAG, "Passed pdf url: " + pdfUrl);

		if (pdfUrl != null && pdfUrl.length() > 0
				&& pdfUrl.toLowerCase().endsWith(".pdf")) {
			mFileName = (new File(pdfUrl)).getName();
			filenameTextView.setText(mFileName);
		} else {

		}

		// init tmpPdfFile
		tmpPdfFile = new File(Shared.getAssetDir(ARisePdfReaderActivity.this)
				+ "/pdf/temp_pdf.pdf");
		if (tmpPdfFile.exists()) {
			if (tmpPdfFile.delete()) {
				Log.i(TAG, "Tmp pdf file is deleted!");
			} else {
				Log.i(TAG, "Tmp pdf file is not deleted!");
			}
		} else {
			Log.i(TAG, "Tmp pdf file does not exist. Safe to download!");
		}
		
		(new DownloadPdfTask(ARisePdfReaderActivity.this)).execute(pdfUrl);
	}

	private void createUI() {
		if (core == null)
			return;
		
		pdfReaderViewContainer = (RelativeLayout) findViewById(R.id.pdf_reader_view_container);

		// Now create the UI.
		// First create the document view
		mDocView = new ARisePdfReaderView(this);
		mDocView.setListener(new ARisePdfReaderView.ARisePdfReaderViewListener() {

			@Override
			public void onTap() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						scrollView.setVisibility(View.GONE);
					}
				});
			}

			@Override
			public void onMove() {

			}

			@Override
			public void onFling() {

			}
		});
		mDocView.setAdapter(new MuPDFPageAdapter(this, null, core));

		pdfReaderViewContainer.removeAllViews();
		pdfReaderViewContainer.addView(mDocView,
				new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.MATCH_PARENT,
						RelativeLayout.LayoutParams.MATCH_PARENT));

		pageSliderButton.setEnabled(true);
		pageSliderButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (scrollView.getVisibility() == View.GONE) {
					scrollView.setVisibility(View.VISIBLE);
				} else {
					scrollView.setVisibility(View.GONE);
				}
			}
		});

		initScrollView();
	}

	private void initScrollView() {
		if (scrollView == null) {
			scrollView = (HorizontalScrollView) findViewById(R.id.pdf_reader_view_seek_bar);
			scrollView.setAlpha(0.8f);
			scrollView.setBackgroundColor(ARiseConfigs.THEME_COLOR);

			LinearLayout mainLL = (LinearLayout) findViewById(R.id.pdf_reader_view_scroll_view_linear_layout);
			mainLL.removeAllViews();

			for (int i = 0; i < core.countPages(); i++) {

				RelativeLayout previewRelativeLayout = (RelativeLayout) getLayoutInflater()
						.inflate(R.layout.arise_pdf_reader_preview_row, null);
				ImageView previewImgView = (ImageView) previewRelativeLayout
						.findViewById(R.id.arise_pdf_reader_preview_thumbnail);
				TextView pageNumber = (TextView) previewRelativeLayout
						.findViewById(R.id.arise_pdf_reader_preview_page_number);

				int imgViewWidth = (int) getResources().getDimension(
						R.dimen.arise_pdf_reader_preview_width);
				int imgViewHeight = (int) getResources().getDimension(
						R.dimen.arise_pdf_reader_preview_thumbnail_height);

				int previewBmpWidth = -1;
				int previewBmpHeight = -1;

				if ((core.getPageSize(i).y > 0) && (core.getPageSize(i).x > 0)) {
					PointF pageSize = core.getPageSize(i);
					float pdfRatio = (float) pageSize.y / pageSize.x;
					float imgViewRatio = (float) imgViewHeight / imgViewWidth;

					if (pdfRatio > imgViewRatio) {
						previewBmpHeight = imgViewHeight;
						previewBmpWidth = (int) (imgViewHeight / pdfRatio);
					} else {
						previewBmpWidth = imgViewWidth;
						previewBmpHeight = (int) (imgViewWidth * pdfRatio);
					}
				}

				if (previewBmpWidth > 0 && previewBmpHeight > 0) {
					Bitmap previewBmp = Bitmap.createBitmap(previewBmpWidth,
							previewBmpHeight, Bitmap.Config.ARGB_8888);
					core.drawPageSynchrinized(i, previewBmp, previewBmpWidth,
							previewBmpHeight, 0, 0, previewBmpWidth,
							previewBmpHeight);
//					core.drawPage(previewBmp, i, previewBmpWidth,
//							previewBmpHeight, 0, 0, previewBmpWidth,
//							previewBmpHeight);
					previewImgView.setImageBitmap(previewBmp);
				}

				pageNumber.setText("" + (i + 1));

				final int constantI = i;
				previewRelativeLayout
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								// highlightView.setVisibility(View.VISIBLE);
								mDocView.setDisplayedViewIndex(constantI);
							}
						});

				LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
						(int) getResources().getDimension(
								R.dimen.arise_pdf_reader_preview_width),
						(int) getResources().getDimension(
								R.dimen.arise_pdf_reader_preview_height));
				llParams.setMargins(10, 10, 10, 10);
				llParams.weight = 1;

				mainLL.addView(previewRelativeLayout, llParams);
			}
		}
	}

	private MuPDFCore openFile(String path) {
		System.out.println("Trying to open " + path);
		try {
			core = new MuPDFCore(this, path);
			// New file: drop the old outline data
//			OutlineActivityData.set(null);
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
		return core;
	}

	private class DownloadPdfTask extends AsyncTask<String, Void, Void> {

		private final String TAG = "CustomizedPdfReader.DownloadPdfTask";

		private long startTime;

		public DownloadPdfTask(Context cxt) { }

		@Override
		protected Void doInBackground(String... data) {
			
			startTime = System.currentTimeMillis();
			
			String posterURL = data[0];
			downloadPdf(posterURL);
			return null;
		}

		protected void onPostExecute(Void unused) {
			long leftTime = 3000 - (System.currentTimeMillis() - startTime); 
			if (leftTime > 0) {
				Timer t = new Timer();
				t.schedule(new TimerTask() {
					@Override
					public void run() {
						if (loadingDialog != null) {
							loadingDialog.dismiss();
						}
					}
				}, leftTime);
			} else {
				if (loadingDialog != null) {
					loadingDialog.dismiss();
				}
			}
		}

		private void downloadPdf(String posterURL) {
			try {
				URL url = new URL(posterURL);
				HttpURLConnection urlConnection = (HttpURLConnection) url
						.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.setUseCaches(false);
				urlConnection.setAllowUserInteraction(false);
				urlConnection.connect();

				InputStream inputStream = urlConnection.getInputStream();

				if (!tmpPdfFile.getParentFile().exists()) {
					tmpPdfFile.getParentFile().mkdirs();
				}

				if (tmpPdfFile.createNewFile()) {
					FileOutputStream fileOutput = new FileOutputStream(
							tmpPdfFile);
					byte[] buffer = new byte[1024];
					int bufferLength = 0;
					while ((bufferLength = inputStream.read(buffer)) > 0) {
						fileOutput.write(buffer, 0, bufferLength);
					}
					fileOutput.close();
				}
			} catch (MalformedURLException e) {
				Log.e(TAG, String.format(
						"Error while downloading poster. Type: %s",
						"MalformedURLException"));
			} catch (IOException e) {
				Log.e(TAG, String.format(
						"Error while downloading poster. Type: %s",
						"IOException"));
			} catch (Exception e) {
				Log.e(TAG, String.format(
						"Error while downloading poster. Type: %s", "Unknown"));
				e.printStackTrace();
			}
		}
	}
}
