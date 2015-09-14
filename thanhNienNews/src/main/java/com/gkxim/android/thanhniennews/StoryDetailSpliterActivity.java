package com.gkxim.android.thanhniennews;

import android.text.TextUtils;
import com.gkxim.android.thanhniennews.tracking.Tracking;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Timon <br>
 *         This is only a middle-activity to invoke a dedicated Story Detail
 *         activity base on current SDK version. It's also be used in action
 *         "com.gkxim.android.thanhniennews.ACTION_VIEW_STORY" which will be
 *         sent from PNS. If SDK > 11 (HoneyCome) then invoke
 *         StoryDetailFragmentActivity, otherwise invoke simple
 *         StoryDetailActivity
 * 
 */
public class StoryDetailSpliterActivity extends Activity {
	private static final String TAG = StoryDetailSpliterActivity.class
			.getSimpleName();
	private static final String ACTION_STORY_VIEW = "com.gkxim.android.thanhniennews.ACTION_VIEW_STORY";
	public static final String DATA_KEY_STORY_JSON = "data.data";
	public static final String DATA_KEY_STORY_ID = "story_id";
	public static final String DATA_KEY_SECTION_ID ="section_id";
    private static final String DATA_KEY_PUSH_TYPE = "type";
    private static final String DATA_KEY_PUSH_URL = "url";
    private static final String DATA_VALUE_PUSH_TYPE_WORLDCUP = "wc";
    private Intent storyIntent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		GKIMLog.lf(this, 1, TAG + "=>onCreate.");
		super.onCreate(savedInstanceState);
		Intent intent = this.getIntent();
		if (intent != null) {
			GKIMLog.lf(this, 0, TAG + "=>intent: " + intent);
			String action = intent.getAction();
			if (ACTION_STORY_VIEW.equalsIgnoreCase(action)) {
				Bundle extra = intent.getExtras();
				// start story from PNS
				if (extra != null && extra.containsKey(DATA_KEY_STORY_JSON)) {
					GKIMLog.lf(this, 0, TAG + "=>bundle: " + extra);
					// Get data from Intent's action
					String strjson = extra.getString(DATA_KEY_STORY_JSON);
					String storyId = "";
					String sectionId ="";
                    //Specified keywords for WorldCup2014's pushes
                    String type=null;
                    String wcurl="";
					try {
						JSONObject jo = new JSONObject(strjson);
						if (jo.has(DATA_KEY_STORY_ID)) {
							storyId = jo.getString(DATA_KEY_STORY_ID);
						}
						if (jo.has(DATA_KEY_SECTION_ID)) {
							sectionId = jo.getString(DATA_KEY_SECTION_ID);
						}
                        if (jo.has(DATA_KEY_PUSH_TYPE)) {
							type = jo.getString(DATA_KEY_PUSH_TYPE);
						}
                        if (jo.has(DATA_KEY_PUSH_URL)) {
							wcurl = jo.getString(DATA_KEY_PUSH_URL);
						}

					} catch (Exception e) {
						GKIMLog.lf(null, 4, TAG
								+ "=>Exception from parsing json string: "
								+ strjson + ": " + e.getMessage());
					}
					
					GKIMLog.log(TAG + "=>Get data PNS: sectionId:"+ sectionId + " storyId:"+storyId);
					// sending intent
					storyIntent = new Intent();
                    storyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    if (!TextUtils.isEmpty(type)) {
                        if (DATA_VALUE_PUSH_TYPE_WORLDCUP.equalsIgnoreCase(type)) {
                            //Push is a WC's link
                            storyIntent.setClass(this, EventFullWebViewWorldCupActivity.class);
                            storyIntent.putExtra(TNPreferenceManager.EXTRAKEY_EVENTWEBVIEW_URL, wcurl);
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put(TNPreferenceManager.EXTRAKEY_EVENTWEBVIEW_URL, wcurl);
                            Tracking.sendEvent(TNPreferenceManager.EVENT_WORLDCUP_PUSH_OPENED, map);
                        }
                    }else {
                        //Push is a Story
                        GKIMLog.lf(this, 0, TAG + "=>open fragment activity.");
                        //FIXME: fix pns
                        storyIntent.setClass(this,
                                SplashActivity.class);
                        // if (UIUtils.hasHoneycomb()) {
                        // GKIMLog.lf(this, 0, TAG + "=>open fragment activity.");
                        // storyIntent.setClass(this,
                        // StoryDetailFragmentActivity.class);
                        // } else {
                        // GKIMLog.lf(this, 0, TAG + "=>open simple activity.");
                        // storyIntent.setClass(this, StoryDetailActivity.class);
                        // }

                        storyIntent.putExtra(TNPreferenceManager.EXTRAKEY_IS_STORY,
                                true);
                        storyIntent.putExtra(TNPreferenceManager.EXTRAKEY_STORYID,
                                storyId);
                        storyIntent.putExtra(
                                TNPreferenceManager.EXTRAKEY_IS_STORY_CHECKED,
                                false);
                        storyIntent
                                .putExtra(
                                        TNPreferenceManager.EXTRAKEY_OPEN_STORY_FROM_SECTION,
                                        TNPreferenceManager.EXTRAVALUE_SECTION_HOME);
                        storyIntent
                                .putExtra(
                                        TNPreferenceManager.EXTRAKEY_OPEN_STORY_FROM_PNS,
                                        true);
                        // NOTE: wont have list of story's ids since this is
                        // triggers from PNS
                        // if (mStoryIds != null) {
                        // storyIntent
                        // .putExtra(
                        // TNPreferenceManager.EXTRAKEY_STORYIDS_FROM_SECTION,
                        // mStoryIds);
                        // }
                    }
					startActivity(storyIntent);
					finish();
				}
			}
		}
		// NOTE: no need to invoke super.onCreate(savedInstanceState) since this
		// activity will never be shown or live it life
	}

}
