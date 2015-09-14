package com.knx.framework.utils;

import java.util.Arrays;

import android.opengl.Matrix;

import com.badlogic.gdx.math.Matrix4;
import com.knx.framework.helper.ARiseConfigs;

public class ARiseMathUtils {
	
	public final static float[] LOST_TRACK_MATRIX = {
		0f, 1f, 0f, 0f,
		-1f, 0f, 0f, 0f,
		0f, 0f, 1f, 0f,
		0f, 0f, -1.5f * getFocalLength(ARiseConfigs.FOV, 320f), 1f};
	
	public static float[] convertIntrinsicToCameraProjectionMatrix4(double[] intrinsic, int sceneWidth, int sceneHeight, int prevWidth, int prevHeight) {
		float[] result = new float[16];

		// Calculate the Projection Matrix by multiplying Normalized Device Coordinates (NDC) Matrix with the Projective Transform Matrix
		// Projection = NDC x Perspective
		float[] orthoMat = Arrays.copyOf(getOrthoMatrix(sceneWidth, sceneHeight), 16);
		intrinsic[0] *= (double) sceneWidth / 240;
		intrinsic[1] *= (double) sceneHeight / 320;
		float[] intrinsicMat = getIntrinsicMatrix(intrinsic);
		
		Matrix.multiplyMM(result, 0, orthoMat, 0, intrinsicMat, 0);

		return result;
	}

	private static float[] getIntrinsicMatrix(double[] camParams) {
		float[] intMat = new float[16];

		float alpha = (float)camParams[0], beta = (float)camParams[1], skew = 0f;
		float far = 1e6f, near = 1f;
		
		intMat[0] = alpha;	intMat[4] = skew;	intMat[8] = 0f;				intMat[12] = 0f;
		intMat[1] = 0f;		intMat[5] = beta;	intMat[9] = 0f;				intMat[13] = 0f;
		intMat[2] = 0f;		intMat[6] = 0f;		intMat[10] = near + far;	intMat[14] = near * far;
		intMat[3] = 0f;		intMat[7] = 0f;		intMat[11] = -1f;			intMat[15] = 0f;

		return intMat;
	}

	private static float[] getOrthoMatrix(int sceneWidth, int sceneHeight){
		float[] result = new float[16];

		float left = -sceneWidth/2, bottom = -sceneHeight/2, right = sceneWidth/2, top = sceneHeight/2;
		float far = 1e6f, near = 1f;

		// Ortho projection
		Matrix.orthoM(result, 0, left, right, bottom, top, near, far);

		return result;
	}
	
	/********************************************************************************
	 * Calculates the angles in 3 dimensions from a matrix.
	 * 
	 * <table>
	 * <tr><td>cosCcosB</td><td>-sinCcosA + cosCsinBsinA</td><td>sinCsinA+cosCsinBcosA</td><td>tx</td></tr>
	 * <tr><td>sinCcosB</td><td>cosCcosA + sinCsinBsinA</td><td>-cosCsinA+sinCsinBcosA</td><td>ty</td></tr>
	 * <tr><td>-sinB</td><td>cosBsinA</td><td>cosBcosA</td><td>tz</td></tr>
	 * <tr><td>0</td><td>0</td><td>0</td><td>1</td></tr>
	 * </table>
	 * 
	 * @param r	: the 16 entry values of matrix
	 * @return	The angles in 3 dimensions
	 */
	public static float[] extractAngleFromMatrix(float[] r) {
		
		if (r[3] != 1 && r[3] != -1) {

			float yAngle1 = (float) -Math.asin((double)r[2]);
			float xAngle1 = (float) Math.atan2((double)r[6]/Math.cos(yAngle1), (double)r[10]/Math.cos(yAngle1));
			float zAngle1 = (float) Math.atan2((double)r[1]/Math.cos(yAngle1), (double)r[0]/Math.cos(yAngle1));
			float[] angles_sol1 = {xAngle1, yAngle1, zAngle1};
			
			return angles_sol1;
			
		} else { // gimbal lock
			float xAngle, yAngle;
			float zAngle = 0.0f; // can set randomly, but by convention, is 0.
			if (r[3] == -1) {
				yAngle = (float) Math.PI / 2.0f;
				xAngle = zAngle + (float) Math.atan2((double) r[4], (double) r[8]);
			} else {
				yAngle = (float) -Math.PI / 2.0f;
				xAngle = -zAngle + (float) Math.atan2((double) -r[4], (double) -r[8]);
			}
			float[] angles = {xAngle, yAngle, zAngle};
			return angles;
		}
	}
	
	/********************************************************************************
	 * Converts values from pose matrix (RT matrix) of MIMAS engine to extrinsic matrix.
	 * @param rtMatrix	: the values of rotation-translation matrix of MIMAS engine.
	 * @return	the extrinsic matrix converted from RT matrix.
	 */
	public static Matrix4 convertRTMatrixToExtrinsicMatrix(double[] rtMatrix) {
		
		float[] extrinsicMatrix = new float[16];
		
		Matrix.setIdentityM(extrinsicMatrix, 0);
		extrinsicMatrix[0] = (float) rtMatrix[0]; extrinsicMatrix[4] = (float) rtMatrix[1]; extrinsicMatrix[8] = (float) rtMatrix[2];
		extrinsicMatrix[1] = (float) rtMatrix[3]; extrinsicMatrix[5] = (float) rtMatrix[4]; extrinsicMatrix[9] = (float) rtMatrix[5];
		extrinsicMatrix[2] = (float) rtMatrix[6]; extrinsicMatrix[6] = (float) rtMatrix[7]; extrinsicMatrix[10] = (float) rtMatrix[8];
		
		float det = extrinsicMatrix[0]*extrinsicMatrix[5]*extrinsicMatrix[10]
				+ extrinsicMatrix[4]*extrinsicMatrix[9]*extrinsicMatrix[2]
				+ extrinsicMatrix[8]*extrinsicMatrix[1]*extrinsicMatrix[6]
				- extrinsicMatrix[8]*extrinsicMatrix[5]*extrinsicMatrix[2]
				- extrinsicMatrix[1]*extrinsicMatrix[4]*extrinsicMatrix[10]
				- extrinsicMatrix[6]*extrinsicMatrix[9]*extrinsicMatrix[0];
		
		// convert into RHS
		float[] angles = extractAngleFromMatrix(extrinsicMatrix);
		Matrix.setIdentityM(extrinsicMatrix, 0);
		Matrix.rotateM(extrinsicMatrix, 0, angles[0]*180f/(float)Math.PI, 0.0f, 1.0f, 0.0f);
		Matrix.rotateM(extrinsicMatrix, 0, angles[1]*180f/(float)Math.PI, 1.0f, 0.0f, 0.0f);
		Matrix.rotateM(extrinsicMatrix, 0, angles[2]*180f/(float)Math.PI, 0.0f, 0.0f, 1.0f);
		extrinsicMatrix[12] = (float) -rtMatrix[10];
		extrinsicMatrix[13] = (float) -rtMatrix[9];
		extrinsicMatrix[14] = (float) -rtMatrix[11];
		
		if (det > 0) {
			return new Matrix4(extrinsicMatrix);
		} else {
			return null;
		}
	}
	
	/********************************************************************************
	 * Converts an angle from radian to degree.
	 * @param rad	: the angle in radian 
	 * @return	The converted angle in degree
	 */
	public static float radToDeg(float rad) {
		return rad * 180f / (float) Math.PI;
	}
	
	/********************************************************************************
	 * Converts an angle from degree to radian.
	 * @param rad	: the angle in degree
	 * @return	The converted angle in radian
	 */
	public static float degToRad(float deg) {
		return deg * (float) Math.PI / 180f;
	}
	
	/********************************************************************************
	 * Calculates the focal length from field of view and preview width.
	 * @param fov_in_deg
	 * @param prevWidth
	 * @return	the focal length from field of view and preview width.
	 */
	public static float getFocalLength(float fov_in_deg, float prevWidth) {
		float fov_in_rad = (float) Math.PI * fov_in_deg / 180f; // convert to rad
		float focalLength = prevWidth / (2f * (float) Math.tan((double) (fov_in_rad) / 2f));
		return focalLength;
	}
	
	/********************************************************************************
	 * Checks if a point lies inside a polygon constructed by a list of vertices.
	 * @param p					: the point to be checked
	 * @param listOfVertices	: the list of vertices to form a polygon
	 * @return true if the point lies inside those vertices, false if it lies outside.
	 */
	public static boolean isInside(float[] p, float[][] listOfVertices) {
		// listOfVertices order: 0-1-2-3 is clockwise
		boolean check01 = signOfCrossProduct(listOfVertices[0], listOfVertices[1], p);
		boolean check12 = signOfCrossProduct(listOfVertices[1], listOfVertices[2], p);
		boolean check23 = signOfCrossProduct(listOfVertices[2], listOfVertices[3], p);
		boolean check30 = signOfCrossProduct(listOfVertices[3], listOfVertices[0], p);
		return (check01 && check12 && check23 && check30);
	}

	private static boolean signOfCrossProduct(float[] a, float[] b, float[] p) {
		float det = (b[0]-a[0]) * (p[1]-a[1]) - (b[1]-a[1]) * (p[0]-a[0]);
		if (det > 0) { // vector ap is on ccw direction of ab
			return false;
		} else {
			return true;
		}
	}
}
