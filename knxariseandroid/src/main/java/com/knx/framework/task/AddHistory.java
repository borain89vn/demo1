package com.knx.framework.task;

import java.util.HashMap;

import android.content.Context;
import android.os.AsyncTask;


public class AddHistory extends AsyncTask<HashMap<String, String>, Void, Void> {
	
	private Context context;
	
	public AddHistory(Context cxt) {
		context = cxt;
	}

    protected Void doInBackground(HashMap<String, String>... data) {
        HashMap<String, String> item = data[0];
        String id = item.get("id");
        String title = item.get("title");
        String posterURL = item.get("posterURL");
        String arContent = item.get("arContent");
        DbHelper.getInstance(context).addToHistory(id, title, posterURL, arContent);
        return null;
    }
}