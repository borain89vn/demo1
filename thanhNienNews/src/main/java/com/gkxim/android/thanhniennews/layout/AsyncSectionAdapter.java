package com.gkxim.android.thanhniennews.layout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.EventFullWebViewWorldCupActivity;
import com.gkxim.android.thanhniennews.SectionActivity;
import com.gkxim.android.thanhniennews.models.BoxElement;
import com.gkxim.android.thanhniennews.models.IGenericPage;
import com.gkxim.android.thanhniennews.models.SectionPage;
import com.gkxim.android.thanhniennews.tracking.Tracking;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.UIUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public class AsyncSectionAdapter extends BaseAdapter {

	private static final String TAG = "AsyncSectionAdapter";
	private static final boolean DEBUG = GKIMLog.DEBUG_ON;
	private LayoutInflater mInflater;
	private Activity mContext;
	private boolean mNotification = false;
	private ArrayList<IGenericPage> mPagesArray = null;
	private String mTAG = TAG;
	private String[] mArrVnDays = null;
	private String[] mArrEnDays = null;
	private boolean mTabletVersion = false;

	public class ViewHolder {
		public BoxLayout boxLayout;
		protected TextView title;
		protected TextView date;
		public LinearLayout llseparator;
		// Reserved, check not changed.
		public String sectionId = null;
		public ImageView webevent;
		public AdView adview;
		// Spring
		public ImageView springGreetings;
		public ImageView tetOfYou;

		public ImageView imgWeather;
		public TextView txtWeather;
	}

	public AsyncSectionAdapter(Context context) {
		if (context instanceof Activity) {
			mContext = (Activity) context;
		}
		mArrVnDays = mContext.getResources().getStringArray(
				R.array.string_vn_days);
		mArrEnDays = mContext.getResources().getStringArray(
				R.array.string_en_days);
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPagesArray = new ArrayList<IGenericPage>();
		mTAG = TAG + "(" + this.hashCode() + ")";
		mTabletVersion = UIUtils.isTablet(context);
	}

	@Override
	public IGenericPage getItem(int position) {
		if (position < 0 || position >= mPagesArray.size()) {
			return null;
		}
		return mPagesArray.get(position);
	}

	public int getPageIndexById(String secId) {
		if (secId == null || secId.length() <= 0) {
			return -1;
		}

		int length = mPagesArray.size();
		for (int i = 0; i < length; i++) {
			if (secId.equalsIgnoreCase(mPagesArray.get(i).getSectionId())) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		GKIMLog.lf(null, 1, mTAG + "=>getView (" + position + "): "
				+ convertView);
		return drawView(position, convertView);
	}

	private View drawView(int position, View view) {

		ViewHolder holder = null;
		long startTime = System.currentTimeMillis();
		if (view == null) {
			holder = new ViewHolder();
			view = mInflater.inflate(R.layout.sectionview, null);
			holder.boxLayout = (BoxLayout) view.findViewById(R.id.blSection);
			holder.llseparator = (LinearLayout) view
					.findViewById(R.id.ll_section_separator);
			holder.title = (TextView) view
					.findViewById(R.id.tv_section_category);
			holder.title.setTypeface(TNPreferenceManager.getTNTypefaceBOLD());
			holder.date = (TextView) view
					.findViewById(R.id.tv_section_separator_date);
			holder.date.setTypeface(TNPreferenceManager.getTNTypefaceBOLD());
			holder.webevent = (ImageView) view
					.findViewById(R.id.iv_section_headevent);
			holder.adview = (AdView) view.findViewById(R.id.adView);

			AdRequest adRequest = new AdRequest.Builder().build();
			holder.adview.loadAd(adRequest);

			// Spring
			holder.springGreetings = (ImageView) view
					.findViewById(R.id.imgv_spring_greetings);
			holder.tetOfYou = (ImageView) view
					.findViewById(R.id.imgv_tet_of_you);
			holder.imgWeather = (ImageView) view
					.findViewById(R.id.boxview_img_weather_title);
			holder.txtWeather = (TextView) view
					.findViewById(R.id.boxview_weather_title);

			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		IGenericPage gp = getItem(position);
		holder.boxLayout.setPage(gp);
		if (mContext instanceof SectionActivity) {
			SectionActivity act = (SectionActivity) mContext;
			holder.boxLayout.setOnClickListener(act.getOnClick());
		}
		holder.sectionId = gp.getSectionId();
		GKIMLog.l(1, TAG + " drawView holder.sectionId:" + holder.sectionId);
		// boolean bLoggedIn = TNPreferenceManager.checkLoggedIn();
		if (mNotification
				&& !TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_SAVED
						.equals(holder.sectionId) /* && bLoggedIn */) {
			holder.boxLayout.refreshBoxStories(
					TNPreferenceManager.getReadStories(),
					TNPreferenceManager.getIdSavedStories());

		}

		if (TNPreferenceManager.EXTRAVALUE_SECTION_HOME
				.equals(holder.sectionId)
				|| TNPreferenceManager.EXTRAVALUE_SECTION_SEARCH_PAGE
						.equals(holder.sectionId)) {
			holder.title.setVisibility(View.GONE);
		} else {
			holder.title.setVisibility(View.VISIBLE);
			if (TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE
					.equals(holder.sectionId)) {
				holder.title
						.setText(TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_NAME);
			} else if (TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_SAVED
					.equals(holder.sectionId)) {
				holder.title
						.setText(TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_SAVED_NAME);
			} else {
				holder.title.setText(gp.getSectionTitle());
			}
		}

		// Always reset webview back to GONE
		holder.adview.setVisibility(View.VISIBLE);
		holder.webevent.setVisibility(View.GONE);
		if (position == 0) {
			holder.llseparator.setVisibility(View.GONE);

			// NOTE: specified for World Cup 2014 campaign.
			if (TNPreferenceManager.EXTRAVALUE_SECTION_HOME
					.equals(holder.sectionId)) {
				if (GUIListMenuAdapter.HAS_ENABLE_WEBEVENT) {
					holder.adview.setVisibility(View.GONE);
					holder.webevent.setVisibility(View.VISIBLE);
					applyFullMargins(holder.webevent);
					holder.webevent
							.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									// NOTE: handling click on webEvent - WC2014
									// open a browser with link from resource
									String url = v.getResources().getString(
											R.string.webevent_worldcup2014_url);
									GKIMLog.lf(v.getContext(), 1, TAG
											+ "=>Open Worldcup 2014 campaign: "
											+ url);
									if (!TextUtils.isEmpty(url)
											&& mContext != null
											&& mContext instanceof SectionActivity) {
										try {
											// Intent i = new
											// Intent(Intent.ACTION_VIEW,
											// Uri.parse(url));
											Intent i = new Intent(
													mContext,
													EventFullWebViewWorldCupActivity.class);
											i.putExtra(
													TNPreferenceManager.EXTRAKEY_EVENTWEBVIEW_URL,
													url);
											((SectionActivity) mContext)
													.startActivity(i);
											Tracking.sendEvent(
													TNPreferenceManager.EVENT_WORLDCUP_HOME_OPENED,
													null);
										} catch (Exception e) {
											GKIMLog.l(
													4,
													TAG
															+ "Open Worldcup 2014 campaign exception: "
															+ url);
										}
									}
								}
							});
				}
			}else if (holder.sectionId
					.equals(SectionActivity.LOCATION_SPECIAL_STORY_BOX_ID)) {
				holder.imgWeather.setVisibility(View.VISIBLE);
				holder.txtWeather.setVisibility(View.VISIBLE);
				if (SectionActivity.mBoxLocationStory != null) {
					BoxElement[] titleElms = SectionActivity.mBoxLocationStory
							.getBoxElementbyType(BoxElement.BOXELEMENT_TYPE_TITLE);
					if (titleElms != null) {
						BoxElement elm = titleElms[0];
						ImageLoader.getInstance().displayImage(
								elm.getWeatherimg(), holder.imgWeather);

						String titleWeather = elm.getWeathercontent();
						holder.txtWeather.setText(titleWeather + (char) 0x00B0
								+ "C");
					}
				}
			}else{
				holder.imgWeather.setVisibility(View.GONE);
				holder.txtWeather.setVisibility(View.GONE);
			}
		} else {
			if (holder.sectionId
					.equals(SectionActivity.LOCATION_SPECIAL_STORY_BOX_ID)) {
				holder.llseparator.setVisibility(View.GONE);
				holder.adview.setVisibility(View.GONE);
			} else {
				holder.llseparator.setVisibility(View.VISIBLE);
			}
			holder.title.setVisibility(View.GONE);
			holder.date.setText(getDate(gp));
		}
		if (TNPreferenceManager.getXuanSectionId().equals(holder.sectionId)
				&& position == 0) {
			holder.springGreetings.setVisibility(View.VISIBLE);
			holder.tetOfYou.setVisibility(View.VISIBLE);
			holder.springGreetings
					.setOnClickListener(((SectionActivity) mContext)
							.getOnClick());
			holder.tetOfYou.setOnClickListener(((SectionActivity) mContext)
					.getOnClick());
			resizeImageSpring(holder.springGreetings, holder.tetOfYou);
		} else {
			holder.springGreetings.setVisibility(View.GONE);
			holder.tetOfYou.setVisibility(View.GONE);
		}

		if (DEBUG) {
			long duration = System.currentTimeMillis() - startTime;
			GKIMLog.lf(mContext, 1, mTAG + "=>drawView in : " + duration);
		}
		if (position == (getCount() - 1)) {
			mNotification = false;
		}
		return view;
	}

	/**
	 * Apply Box's margins for the view
	 * 
	 * @param view
	 *            The View that is needed to apply the margin.
	 */
	public void applyFullMargins(View view) {
		int cell = TNPreferenceManager.getBoxSize();
		int onecellmargin = TNPreferenceManager.getGapWidth();

		int nCells = TNPreferenceManager.EXTRAVALUE_SECTION_COLUMN_PHONE;
		int nGaps = TNPreferenceManager.EXTRAVALUE_SECTION_COLUMN_PHONE - 1;
		if (mTabletVersion) {
			nCells = TNPreferenceManager.EXTRAVALUE_SECTION_COLUMN_TABLET;
			nGaps = TNPreferenceManager.EXTRAVALUE_SECTION_COLUMN_TABLET - 1;
		}
		int bannerWidth = cell * nCells + onecellmargin * nGaps;
		GKIMLog.l(1, TAG + "applyMargins, bannerWidth:" + bannerWidth
				+ ", marginLR:" + onecellmargin);
		if (view != null) {
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					bannerWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(onecellmargin, onecellmargin,
					onecellmargin, onecellmargin);
			view.setLayoutParams(layoutParams);
		}
	}

	public void resizeImageSpring(ImageView spring, ImageView gallery) {
		int cell = TNPreferenceManager.getBoxSize();
		int padding = TNPreferenceManager.getGapWidth();
		GKIMLog.l(1, TAG + "resizeImageSpring cell:" + cell + " padding:"
				+ padding);
		if (!mTabletVersion) {
			if (spring != null && cell > 0) {
				GKIMLog.l(1, TAG + "resizeImageSpring  spring cell:" + cell
						+ " padding:" + padding);
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						(int) (cell * 1));
				layoutParams.setMargins(padding, padding, padding, padding);
				spring.setLayoutParams(layoutParams);

			}
			if (gallery != null && cell > 0) {
				GKIMLog.l(1, TAG + "resizeImageSpring  spring cell:" + cell
						+ " padding:" + padding);
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						(int) (cell * 1));
				layoutParams.setMargins(padding, 0, padding, 0);
				gallery.setLayoutParams(layoutParams);

			}
		} else {
			if (spring != null && cell > 0) {
				GKIMLog.l(1, TAG + "resizeImageSpring  spring cell:" + cell
						+ " padding:" + padding);
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
						cell * 2 + padding, (int) (cell * 1));
				layoutParams.setMargins(padding, padding, padding, 0);
				spring.setLayoutParams(layoutParams);

			}
			if (gallery != null && cell > 0) {
				GKIMLog.l(1, TAG + "resizeImageSpring  spring cell:" + cell
						+ " padding:" + padding);
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
						cell * 2 + padding, (int) (cell * 1));
				layoutParams.setMargins(0, padding, padding, 0);
				gallery.setLayoutParams(layoutParams);

			}
		}
	}

	@Override
	public int getCount() {
		return mPagesArray.size();
	}

	public void addPage(IGenericPage page) {
		String currentSectionId = null;
		if (mPagesArray.size() > 0) {
			currentSectionId = mPagesArray.get(0).getSectionId();
		}
		if (page != null && page.getBoxStoryCount() > 0) {
			String addSectionId = page.getSectionId();
			if (TNPreferenceManager.isSectionAddBoxes()
					|| !addSectionId.equalsIgnoreCase(currentSectionId)) {
				// clear as changing the section.
				mPagesArray.clear();
			}
			mPagesArray.add(page);
		}
	}

	public void addPages(IGenericPage[] pages) {
		if (pages != null && pages.length > 0) {
			for (IGenericPage p : pages) {
				addPage(p);
			}
		}
	}

	public boolean removePage(IGenericPage page) {
		if (page != null && mPagesArray.contains(page)) {
			return mPagesArray.remove(page);
		}
		return false;
	}

	public void clear() {
		mPagesArray.clear();
	}

	public String getDate(IGenericPage page) {
		String theday = "";
		if (page instanceof SectionPage) {
			theday = ((SectionPage) page).getIssueDateString();
		}
		return theday;
	}

	@Override
	public void notifyDataSetChanged() {
		mNotification = true;
		super.notifyDataSetChanged();
	}
}
