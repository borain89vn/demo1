package com.knx.framework.arcontents.overlay;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.knx.framework.R;
import com.knx.framework.utils.ARiseGLUtils;

public class OverlayPlayButton {
	
	private final String TAG = "ARisePlayButton";
	
	private Context context;
	
	private int resourceId;			// id of the resource in class R

	private String vertexShaderCode, fragmentShaderCode;
    
    private FloatBuffer vertexBuffer, textureVerticesBuffer;
    private ShortBuffer drawListBuffer;
    private int mProgram;
    
    private int mPositionHandle;
    private int mMVPMatrixHandle;
    private int mTextureCoordHandle;
    
    private boolean isHidden;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    
    private short[] drawOrder;
    private float[] textureCoords;
    private float[] squareCoords;
    
    
    private final int FAN = 360;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private int mTextureDataHandle;
    
    public OverlayPlayButton(Context cxt, int resId) {
    	
    	context = cxt;
    	
    	int width = 1;
    	int height = 1;
    	resourceId = resId;
    	
    	try {
    		vertexShaderCode = ARiseGLUtils.getShaderFromResources(context, R.raw.per_pixel_vertex_shader);
	    	fragmentShaderCode = ARiseGLUtils.getShaderFromResources(context, R.raw.per_pixel_fragment_shader);
    	} catch (IOException ioe) {
    		Log.e(TAG, "Cannot find shader file");
    	} catch (Exception e) {
    		Log.e(TAG, "Unknown error happens while loading shader code for video display");
    	}
    	
    	drawOrder = new short[FAN*3];
    	for (short i = 0; i < FAN; i++) {
    		drawOrder[3*i]		= 0;
    		drawOrder[3*i+1]	= (short) (i+1);
    		drawOrder[3*i+2]	= (short) (i+2);
    	}
    	drawOrder[3*FAN-3]		= 0;
    	drawOrder[3*FAN-3+1]	= FAN;
    	drawOrder[3*FAN-3+2]	= 1;
    	
    	textureCoords = new float[(FAN+1)*3];
    	textureCoords[0] = 0.5f; textureCoords[1] = 0.5f; textureCoords[2] = 0.0f;
    	for (int i = 1; i < FAN + 1; i++) {
    		textureCoords[3*i]		= (float) -Math.sin(2 * Math.PI * (i-1) / FAN) / 2f + 0.5f;
    		textureCoords[3*i+1]	= (float) Math.cos(2 * Math.PI * (i-1) / FAN) / 2f + 0.5f;
    		textureCoords[3*i+2]	= 0f;
    	}
    	
    	squareCoords = new float[(FAN+1)*3];
    	squareCoords[0] = 0.0f; squareCoords[1] = 0.0f; squareCoords[2] = 0.0f;
    	for (int i = 1; i < FAN + 1; i++) {
    		squareCoords[3*i]	= (float) Math.cos(2 * Math.PI * (i-1) / FAN) * width / 2;
    		squareCoords[3*i+1]	= (float) Math.sin(2 * Math.PI * (i-1) / FAN) * height / 2;
    		squareCoords[3*i+2]	= 0f;
    	}
    	
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer vertextByteBuffer = ByteBuffer.allocateDirect(
        // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        vertextByteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = vertextByteBuffer.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer drawListByteBuffer = ByteBuffer.allocateDirect(
        // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        drawListByteBuffer.order(ByteOrder.nativeOrder());
        drawListBuffer = drawListByteBuffer.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
        
        ByteBuffer textureVertexByteBuffer = ByteBuffer.allocateDirect(textureCoords.length * 4);
		textureVertexByteBuffer.order(ByteOrder.nativeOrder());
		textureVerticesBuffer = textureVertexByteBuffer.asFloatBuffer();
		textureVerticesBuffer.put(textureCoords);
		textureVerticesBuffer.position(0);

		prepareProgram();
    }
    
    public void draw(float[] mvpMatrix) {
    	
    	if (mvpMatrix != null && !isHidden) {
        
	        GLES20.glUseProgram(mProgram);
	        
	        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
	        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");
	        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");
	        
	        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
	        
	        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
	        GLES20.glEnableVertexAttribArray(mPositionHandle);
	
	        GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, textureVerticesBuffer);
			GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
	
	        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
	
	        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
	
	        GLES20.glDisableVertexAttribArray(mPositionHandle);
	        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
	        GLES20.glDisableVertexAttribArray(mMVPMatrixHandle);
        
    	}
    }
    
    private int loadShader(int type, String shaderCode){

        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
    
    private void prepareProgram() {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
        
        mTextureDataHandle = ARiseGLUtils.loadTexture(context, resourceId);
    }
    
    public void show() {
    	isHidden = true;
    }
    
    public void hide() {
    	isHidden = false;
    }
}