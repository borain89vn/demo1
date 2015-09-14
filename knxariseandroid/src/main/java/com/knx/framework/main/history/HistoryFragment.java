package com.knx.framework.main.history;

/**
 * Created with IntelliJ IDEA.
 * User: huy_phan
 * Date: 26/12/12
 * Time: 4:58 PM
 * To change this template use File | Settings | File Templates.
 */

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.knx.framework.R;
import com.knx.framework.helper.ARiseConfigs;
import com.knx.framework.helper.LangPref;
import com.knx.framework.task.AddHistory;
import com.knx.framework.task.DbHelper;
import com.knx.framework.ui.ARiseDialogTwoButton;

public class HistoryFragment extends ListFragment {

	static final int FRAGMENT_HISTORY = 10;
	static final int FRAGMENT_BOOKMARK = 20;

	static final int CLICK_MODE_SINGLE = 10;
	static final int CLICK_MODE_MULTIPLE = 20;

	private int clickMode;
	private LinkedHashSet<String> selectedItems;

	public static final String TAG = "ARiseHistoryFragment";
	private List<Map<String, String>> items;
	private String section = null;
	
	private SharedPreferences pref;
	private Editor editor;

	public static HistoryFragment newInstance() {
		HistoryFragment f = new HistoryFragment();
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		section = args.getString("section");
		selectedItems = new LinkedHashSet<String>();
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		pref = getActivity().getApplicationContext().getSharedPreferences("KnorexPref", 0); // 0 - for private mode
        editor = pref.edit();
		View view =  inflater.inflate(R.layout.history_fragment, container, false);
		
		if (section.equals("History")) {
			items = DbHelper.getInstance(getActivity()).getHistory();
		} else {
			items = DbHelper.getInstance(getActivity()).getBookmarks();
		}

		ListAdapter adapter = new ListAdapter(getActivity(), items);
		setListAdapter(adapter);
		setHasOptionsMenu(true);

		this.clickMode = CLICK_MODE_SINGLE;

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.history_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.clear_all) {

		}

		return true;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				editor.putBoolean("taken", false);
				editor.clear();
				editor.commit(); // commit changes
				if (clickMode == CLICK_MODE_SINGLE) {
					// Update time for entry in database
		            String isnap_id = items.get(position).get("id");
		            String posterURL = items.get(position).get("posterURL");
		            String arContent = items.get(position).get("arContent");
		            String title = items.get(position).get("title");
					
					HashMap<String, String> item = new HashMap<String, String>();
		            item.put("id", isnap_id);
		            item.put("posterURL", posterURL);
		            item.put("arContent", arContent);
		            item.put("title", title);
		            if ((posterURL != null) && (title != null)) {
		                (new AddHistory(getActivity())).execute(item);
		            }
					
					// edit to use libgdx
					Intent i = new Intent(getActivity(), HistoryStaticLayer.class);
					Bundle b = new Bundle();
					b.putString("json", arContent);
					i.putExtras(b);
		            startActivity(i);
		            
				} else {
					String ariseID = items.get(position).get("id");
					if (selectedItems.contains(ariseID)) {
						view.findViewById(R.id.selected).setVisibility(View.INVISIBLE);
						selectedItems.remove(ariseID);
						if (selectedItems.size() == 0) {
							clickMode = CLICK_MODE_SINGLE;
						}
					} else {
						view.findViewById(R.id.selected).setVisibility(View.VISIBLE);
						selectedItems.add(ariseID);
					}

					ViewGroup viewGroup = (ViewGroup) getView().getParent().getParent();
					Button deleteButton = (Button) viewGroup.findViewById(R.id.delete_button);

					if (clickMode == CLICK_MODE_MULTIPLE) {
						deleteButton.setText(LangPref.TXTDELETE);
					} else {
						updateStatusButton();
					}
				}
			}
		});
		getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> av, View view, int position, long id) {
				if (clickMode == CLICK_MODE_SINGLE) {
					clickMode = CLICK_MODE_MULTIPLE;
				}
				String ariseID = items.get(position).get("id");
				if (selectedItems.contains(ariseID)) {
					view.findViewById(R.id.selected).setVisibility(View.INVISIBLE);
					selectedItems.remove(ariseID);
					if (selectedItems.size() == 0) {
						clickMode = CLICK_MODE_SINGLE;
					}
				} else {
					view.findViewById(R.id.selected).setVisibility(View.VISIBLE);
					selectedItems.add(ariseID);
				}

				ViewGroup viewGroup = (ViewGroup) getView().getParent().getParent();
				Button deleteButton = (Button) viewGroup.findViewById(R.id.delete_button);

				if (clickMode == CLICK_MODE_MULTIPLE) {
					deleteButton.setText(LangPref.TXTDELETE);
					getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
				} else {
					updateStatusButton();
					getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
				}
				return true;
			}
		});
		getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
        if (section.equals("History")) {
		    updateStatusButton();
        }
	}

	private void updateStatusButton() {
		ViewGroup viewGroup = (ViewGroup) getView().getParent().getParent();
		Button deleteButton = (Button) viewGroup.findViewById(R.id.delete_button);
		if (items.size() <= 1) {
			deleteButton.setText(LangPref.TXTSHOWING + " " + items.size() + " " + LangPref.TXTITEM);
		} else {
			deleteButton.setText(LangPref.TXTSHOWING + " " + items.size() + " " + LangPref.TXTITEMS);
		}
	}

	public void resetSelectionStatus() {
		selectedItems.clear();
		clickMode = CLICK_MODE_SINGLE;
		updateStatusButton();
	}

	public void reloadData() {
		if (section.equals("History")) {
			items = DbHelper.getInstance(getActivity()).getHistory();
		} else {
			items = DbHelper.getInstance(getActivity()).getBookmarks();
		}
		ListAdapter adapter = new ListAdapter(getActivity(), items);
		setListAdapter(adapter);
		adapter.notifyDataSetChanged();
        updateStatusButton();
	}

	public void removeItems() {
		if (selectedItems.size() > 0) {
			String message = "Are you sure you want to delete selected items?";
			if (section.equals("History")) {
				message = LangPref.TXTCONFIRMCLEARHISTORY;
			} else {
				message = LangPref.TXTCONFIRMCLEARBOOKMARK;
			}
			
			if (getActivity() != null && !getActivity().isFinishing()) {				
				final ARiseDialogTwoButton dialog = new ARiseDialogTwoButton(getActivity());
				dialog.setThemeColor(ARiseConfigs.THEME_COLOR);
				dialog.setMessageText(message);
				dialog.setButtonNo(LangPref.TXTNO, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// dismiss the dialog
						dialog.dismiss();
					}
				});
				dialog.setButtonYes(LangPref.TXTYES, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						for (String item : selectedItems) {
							if (section.equals("History")) {
								DbHelper.getInstance(getActivity()).removeFromHistory(item);
							} else {
								DbHelper.getInstance(getActivity()).removeFromBookmark(item);
							}
						}
						reloadData();
						
						// dismiss the dialog
						dialog.dismiss();
					}
				});
				
				dialog.show();
			}
		}
	}

	private class ListAdapter extends BaseAdapter {

		private List<Map<String, String>> items;
		private Context context;

		public ListAdapter(Context context, List<Map<String, String>> items) {
			super();
			this.context = context;
			this.items = items;
		}

		public int getCount() {
			return items.size();
		}

		public Map<String, String> getItem(int position) {
			return (null == items) ? null : items.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			
			if (v == null) {
				v = LayoutInflater.from(context).inflate(R.layout.history_row, null);
			}
			
			Map<String, String> o = items.get(position);
			if (o != null) {
				TextView titleTextView = (TextView) v.findViewById(R.id.title);
				titleTextView.setText(o.get("title"));

				TextView viewtimeTextView = (TextView) v.findViewById(R.id.viewtime);
				long viewtime = Long.parseLong(o.get("viewtime"));
				String s = new SimpleDateFormat("dd MMM yyyy kk:mm").format(new Timestamp(viewtime));
				viewtimeTextView.setText(s);

				String posterURL = o.get("posterURL");
				
				if (posterURL != null && posterURL.length() > 0) {
					ImageView posterImageView = (ImageView) v.findViewById(R.id.thumbnail);
					HistoryThumbnailLoader imageLoader = new HistoryThumbnailLoader(getActivity());
					imageLoader.displayImage(posterURL, posterImageView);
				}
				
//				Log.i(TAG, "------------ Row " + (position + 1) + " ------------");
//				Log.i(TAG, "\t iSnapID\t: " + o.get("id"));
//				Log.i(TAG, "\t title\t\t: " + o.get("title"));
//				Log.i(TAG, "\t viewtime\t: " + o.get("viewtime"));
//				Log.i(TAG, "\t PosterURL\t: " + o.get("posterURL"));
			}

			return v;
		}
	}
}