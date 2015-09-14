package com.knx.framework.arcontents.overlay;

import java.io.File;

import android.content.Context;

import com.badlogic.gdx.math.Vector2;
import com.knx.framework.main.Shared;

public class Overlay3DModelData {
	
	private Context context;
	
	private String modelName;
	private String modelURL;
	private String modelPath;
	
	private boolean isInApp;
	
	private float scale;
	private Vector2 offset;
	
	public Overlay3DModelData(Context cxt) {
		
		context = cxt;
		
		modelName = "";
		modelURL = "";
		modelPath = "";
		
		isInApp = false;
		
		scale = 0;
		offset = new Vector2();
	}
	
	public String getModelName() {
		return modelName;
	}
	
	public void setURL(String url) {
		modelURL = url;
		calculateModelPath();
	}
	
	public String getURL() {
		return modelURL;
	}

	public String getModelPath() {
		return modelPath;
	}
	
	public boolean isInApp() {
		return isInApp;
	}
	
	public void setOffset(Vector2 os) {
		offset = os.cpy();
	}
	
	public Vector2 getOffset() {
		return offset;
	}
	
	public void setScale(float s) {
		scale = s;
	}
	
	public float getScale() {
		return scale;
	}
	
	private void calculateModelPath() {
        if (modelURL.startsWith("file:///")) {
        	
        	// get filename and get rid of ".g3db"
        	modelName = (new File(modelURL)).getName();
        	modelName = modelName.substring(0, modelName.length() - 5);
        	
        	modelPath = modelURL.substring(8);
        	isInApp = true;
        	
        } else {
        	
        	// get filename and get rid of ".tar.gz"
    		modelName = (new File(modelURL)).getName();
    		modelName = modelName.substring(0, modelName.length() - 7);
    		
        	modelPath = Shared.getAssetDir(context) + "/" + modelName + "/" + modelName + "/" + modelName + ".g3db";
        	isInApp = false;
        }
	}
}
