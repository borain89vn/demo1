package com.knx.framework.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class ARiseGLUtils {
	
	private static final String TAG = "ARiseGLUtils";
	
	/********************************************************************************
	 * Sets up OpenGL environment used in this app
	 */
	public static void setOpenGLEnvironment() {
		try {
			Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
			Gdx.gl20.glEnable(GL20.GL_TEXTURE);                  
			Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
			Gdx.gl20.glDepthFunc(GL20.GL_LEQUAL);
			Gdx.gl20.glClearDepthf(1.0F);
	
			Gdx.gl20.glEnable(GL20.GL_BLEND);
			Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);
		} catch (Exception e) {
			Log.e(TAG, "Error setting up openGL environment.", e);
			e.printStackTrace();
		}
	}
	
	/********************************************************************************
	 * Gets shader from a raw resource.
	 * @param context		: the context uses this function
	 * @param id			: the resource id of the shader
	 * @return The content of the shader file
	 * @throws IOException
	 */
	public static String getShaderFromResources(Context context, int id) throws IOException {
		InputStream inputStream = context.getResources().openRawResource(id);
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		String line;
		StringBuilder fileContent = new StringBuilder();
		
		try {
			while ((line = bufferedReader.readLine()) != null) {
				fileContent.append(line);
				fileContent.append("\n");
			}
		} catch (IOException e) {
			Log.e(TAG, "Error reading resources id " + id);
		}
		return fileContent.toString();
	}
	
	public static int createTexture() {
		int[] texture = new int[1];
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
	    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
	    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
	    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
	    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
		return texture[0];
	}
	
	public static int loadTexture(Context context, int res) {
		int[] texture = new int[1];
	    GLES20.glGenTextures(1, texture, 0);
	    BitmapFactory.Options bo = new BitmapFactory.Options();
	    bo.inScaled = false;
	    Bitmap tex = BitmapFactory.decodeResource(context.getResources(), res, bo);
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
	    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
	    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
	    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
	    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
	    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, tex, 0);
	    tex.recycle();
	    if (texture[0] == 0) {
	        // Displays error
	    }

	    return texture[0];
	}
}
