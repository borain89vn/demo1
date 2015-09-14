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

public class OverlayVideo {
	
	private final String TAG = "ARiseOverlayVideo";
	
	private Context context;

	private String vertexShaderCode, fragmentShaderCode;
    
    private final FloatBuffer vertexBuffer, textureVerticesBuffer;
    private final ShortBuffer drawListBuffer;
    private final int mProgram;
    
    private int mPositionHandle;
    private int mMVPMatrixHandle;
    private int mTextureCoordHandle;

    private static final int COORDS_PER_VERTEX = 3;				// number of coordinates per vertex in this array
    private static float[] squareCoords;						// Coordinates of the square
    private static float[][] verticesList;
    
    private float[] latestMVPMatrix;

    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices
    
    // Coordinates of the video texture
    private static float textureCoords[] = {
    	0f, 1f, 0.0f,   // top left
    	0f, 0f, 0.0f,   // bottom left
        1f, 0f, 0.0f,   // bottom right
        1f, 1f, 0.0f	// top right
    };
    
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    
    public OverlayVideo(Context cxt) {
    	
    	context = cxt;
    	
    	int width = 1;
    	int height = 1;
    	
    	try {
	    	vertexShaderCode = ARiseGLUtils.getShaderFromResources(context, R.raw.video_display_vertex_shader);
	    	fragmentShaderCode = ARiseGLUtils.getShaderFromResources(context, R.raw.video_display_fragment_shader);
    	} catch (IOException ioe) {
    		Log.e(TAG, "Cannot find shader file");
    	} catch (Exception e) {
    		Log.e(TAG, "Unknown error happens while loading shader code for video display", e);
    		e.printStackTrace();
    	}
    	
    	squareCoords = new float[12];

    	squareCoords[0] = - ((float) width) / 2.0f;	squareCoords[1] = ((float) height) / 2.0f;		squareCoords[2] = 0.0f;
    	squareCoords[3] = ((float) width) / 2.0f;	squareCoords[4] = ((float) height) / 2.0f;		squareCoords[5] = 0.0f;
    	squareCoords[6] = ((float) width) / 2.0f;	squareCoords[7] = - ((float) height) / 2.0f;	squareCoords[8] = 0.0f;
    	squareCoords[9] = - ((float) width) / 2.0f;	squareCoords[10] = - ((float) height) / 2.0f;	squareCoords[11] = 0.0f;
    	
    	verticesList = new float[4][4];
    	for (int i = 0; i < 4; i++) {
    		verticesList[i][0] = squareCoords[3*i];
    		verticesList[i][1] = squareCoords[3*i+1];
    		verticesList[i][2] = squareCoords[3*i+2];
    		verticesList[i][3] = 1.0f;
    	}
    	
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(
        // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        vertexByteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = vertexByteBuffer.asFloatBuffer();
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

        // prepare shaders and OpenGL program
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }
    
    public void draw(float[] mvpMatrix) {
    	
    	if (mvpMatrix != null) { // only draw when mvpMatrix is not null
    		
    		
    		latestMVPMatrix = mvpMatrix;
        
    		// Add program to OpenGL environment
	        GLES20.glUseProgram(mProgram);
	        
	        // get handle to vertex shader's vPosition member
	        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
	
	        // Enable a handle to the triangle vertices
	        GLES20.glEnableVertexAttribArray(mPositionHandle);
	
	        // Prepare the triangle coordinate data
	        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
	        		GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
	        
	        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram,
					"inputTextureCoordinate");
			GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
			GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX,
					GLES20.GL_FLOAT, false, vertexStride, textureVerticesBuffer);
	
	        // get handle to shape's transformation matrix
	        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
	
	        // Apply the projection and view transformation
	        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
	
	        // Draw the square
	        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
	
	        // Disable vertex array
	        GLES20.glDisableVertexAttribArray(mPositionHandle);
	        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
	        GLES20.glDisableVertexAttribArray(mMVPMatrixHandle);
    	}
    }
    
    private int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
    
    public float[][] getVerticesCoords() {
    	return verticesList;
    }
    
    public float[] getLatestMVPMatrix() {
    	return latestMVPMatrix;
    }
}