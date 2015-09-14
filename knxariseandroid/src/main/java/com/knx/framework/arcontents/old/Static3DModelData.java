package com.knx.framework.arcontents.old;

import java.io.File;

import android.content.Context;
import android.graphics.Point;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.knx.framework.main.Shared;

public class Static3DModelData {
	
	private Context context;
	
	private String modelName;
	private String modelURL;
	private String modelPath;
	
	private boolean isInApp;
	private Rectangle frame;
	
	private float scale;
	private Vector2 offset;
	
	public Static3DModelData(Context cxt) {
		
		context = cxt;
		
		modelName = "";
		modelURL = "";
		modelPath = "";
		
		isInApp = false;
		
		frame = null;
		scale = 0;
		
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
	
	public void setFrameData(Point topLeftPoint, int width, int height) {
		frame = new Rectangle(topLeftPoint.x, Gdx.graphics.getHeight() - topLeftPoint.y - height, width, height);
		calculateOffset();
	}
	
	public Rectangle getFrame() {
		return frame;
	}
	
	public void setScale(float s) {
		scale = s;
	}
	
	public float getScale() {
		return scale;
	}
	
	public Vector2 getOffset() {
		return offset;
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
    		
        	modelPath = Shared.getAssetDir(context) + "/" + modelName + "/" + modelName + ".g3db";

        	isInApp = false;
        }
	}
	
	private void calculateOffset() {
		offset = new Vector2(frame.x + frame.width / 2 - Gdx.graphics.getWidth() / 2, (frame.y + frame.height / 2) - Gdx.graphics.getHeight() / 2);
	}
}
