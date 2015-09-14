package com.knx.framework.main.pdfreader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.artifex.mupdflib.Hit;
import com.artifex.mupdflib.LinkInfo;
import com.artifex.mupdflib.LinkInfoExternal;
import com.artifex.mupdflib.LinkInfoInternal;
import com.artifex.mupdflib.LinkInfoRemote;
import com.artifex.mupdflib.LinkInfoVisitor;
import com.artifex.mupdflib.MuPDFView;
import com.artifex.mupdflib.ReaderView;

public class ARisePdfReaderView extends ReaderView {
	
	private static final String TAG = "ARisePdfReaderView";
	
	public interface ARisePdfReaderViewListener {
		public void onTap();
		public void onMove();
		public void onFling();
	}
	
	private Context mContext;
	
	private ARisePdfReaderViewListener listener;
	
	// use this flag to disable tap gesture
	private boolean tapDisabled;
	
	public ARisePdfReaderView(Context act) {
		super(act);
		mContext = act;
	}
	
	public void setListener(ARisePdfReaderViewListener l) {
		listener = l;
	}
	
	public ARisePdfReaderViewListener getListener() {
		return listener;
	}
	
	public boolean onSingleTapUp(MotionEvent e) {
		
		Log.d(TAG, "onSingleTapUp");
		LinkInfo link = null;

		if (!tapDisabled) {
			MuPDFView pageView = (MuPDFView) getDisplayedView();
			Hit item = pageView.passClickEvent(e.getX(), e.getY());
			
			if (item == Hit.Nothing) {
				if (pageView != null && (link = pageView.hitLink(e.getX(), e.getY())) != null) {
					link.acceptVisitor(new LinkInfoVisitor() {
						@Override
						public void visitInternal(LinkInfoInternal li) {
							// Clicked on an internal (GoTo) link
							setDisplayedViewIndex(li.pageNumber);
						}
	
						@Override
						public void visitExternal(LinkInfoExternal li) {
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(li.url));
							mContext.startActivity(intent);
						}
	
						@Override
						public void visitRemote(LinkInfoRemote li) {
							// Clicked on a remote (GoToR) link
						}
					});
				} else {
					if (listener != null) {
						listener.onTap();
					}
				}
			}
		}

		return super.onSingleTapUp(e);
	}
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		if (listener != null) {
			listener.onMove();
		}
		return super.onScroll(e1, e2, distanceX, distanceY);
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		return super.onDown(e);
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (listener != null) {
			listener.onFling();
		}
		return super.onFling(e1, e2, velocityX, velocityY);
	}
	
	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		super.onScale(detector);
		return true;
	}
	
	@Override
	public boolean onScaleBegin(ScaleGestureDetector sgd) {
		tapDisabled = true;
		return super.onScaleBegin(sgd);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if ((event.getAction() & event.getActionMasked()) == MotionEvent.ACTION_DOWN) {
			tapDisabled = false;
		}

		return super.onTouchEvent(event);
	}
	
	/**
	 * This method is called when main contents of docs is tapped
	 */
	protected void onTapMainDocArea() {
		
	}
	
	/**
	 * This method is called when docs is being scrolled
	 */
	protected void onDocMotion() {
		
	}
	
	protected void onChildSetup(int i, View v) {

		((MuPDFView) v).setLinkHighlighting(true);

//		((MuPDFView) v).setChangeReporter(new Runnable() {
//			public void run() {
//				applyToChildren(new ReaderView.ViewMapper() {
//					@Override
//					void applyToView(View view) {
//						((MuPDFView) view).update();
//					}
//				});
//			}
//		});
	}

	protected void onMoveToChild(int i) {
		
	}

	@Override
	protected void onMoveOffChild(int i) {
		View v = getView(i);
		if (v != null)
			((MuPDFView) v).deselectAnnotation();
	}

	protected void onSettle(View v) {
		// When the layout has settled ask the page to render
		// in HQ
		((MuPDFView) v).updateHq(false);
	}

	protected void onUnsettle(View v) {
		// When something changes making the previous settled view
		// no longer appropriate, tell the page to remove HQ
		((MuPDFView) v).removeHq();
	}

	@Override
	protected void onNotInUse(View v) {
		((MuPDFView) v).releaseResources();
	}

	@Override
	protected void onScaleChild(View v, Float scale) {
		((MuPDFView) v).setScale(scale);
		Log.d(VIEW_LOG_TAG, "onScaleChild " + scale);
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		return super.onSingleTapUp(e);
	}
	
	@Override
	public View getView(int i) {
		return super.getView(i);
	}
	
	@Override
	public void setDisplayedViewIndex(int i) {
		super.setDisplayedViewIndex(i);
	}
	
	@Override
	protected View getViewAtPositionForARise(int i) {
		return super.getViewAtPositionForARise(i);
	}
}
