package com.knx.framework.arcontents.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class Overlay3DModel extends ModelInstance {
	
	private final int TARGET_IMAGE_WIDTH = 320;
	private final int TARGET_IMAGE_HEIGHT = 240;
	
	private Vector2 offset;
	private float autoScaleFactor;
	private float scaleFactor;

	private Matrix4 originalMatrix;
	private Matrix4 gestureRotationMatrix;
	private float currentScale;
	
	private AnimationController animation;

	/********************************************************************************
	 * Constructor method. Create a model instance from the a Model model.
	 * @param model	: the Model model to be used to create the model instance
	 */
	public Overlay3DModel(Model model) {
		super(model);
		
		offset = new Vector2();
		scaleFactor = 1f;
		if (this.animations.size > 0) {
			animation = new AnimationController(this);
		}
		reset();
	}
	
	/********************************************************************************
	 * Resets the 3D model.
	 */
	public void reset() {
		gestureRotationMatrix = (new Matrix4()).idt();
		currentScale = 1f;
		
		calculateAutoScaleFactor();
	}
	
	/********************************************************************************
	 * Calculates the scale factor to scale the model instance to the target image size.
	 */
	private void calculateAutoScaleFactor() {
		Vector3 dimensions = new Vector3(2f, 2f, 2f);
		
		// not used?
//		float xzPlaneOffset = Float.MAX_VALUE;

		try {
			BoundingBox boundingBox = new BoundingBox();
			calculateBoundingBox(boundingBox);
			boundingBox.mul(new Matrix4().idt().rotate(Vector3.Z, -90));
			dimensions = boundingBox.getDimensions();
//			xzPlaneOffset = boundingBox.getMin().y;
		} catch (Exception e1) {
			try { // another method
				float minX = Float.MAX_VALUE;
				float maxX = Float.MIN_VALUE;
				float minY = Float.MAX_VALUE;
				float maxY = Float.MIN_VALUE;
				float minZ = Float.MAX_VALUE;
				float maxZ = Float.MIN_VALUE;
				for (Mesh mMesh : this.model.meshes) {
					int numOfVertices = mMesh.getNumVertices();
					float[] vertArr = new float[numOfVertices];
					mMesh.getVertices(vertArr);
					for (int i = 0; i < vertArr.length; i++) {
						switch (i % 3) {
							case 0:	// x
								if (vertArr[i] > maxX) maxX = vertArr[i];
								if (vertArr[i] < minX) minX = vertArr[i];
								break;
							case 1:	// y
								if (vertArr[i] > maxY) maxY = vertArr[i];
								if (vertArr[i] < minY) minY = vertArr[i];
								break;
							case 2:	// z
								if (vertArr[i] > maxZ) maxZ = vertArr[i];
								if (vertArr[i] < minZ) minZ = vertArr[i];
								break;
						}
					}
				}
				dimensions.set(maxX - minX, maxY - minY, maxZ - minZ);
//				xzPlaneOffset = minY;
			} catch (Exception e2) {
				
			}
		}
		
		// call this to move the model above xz plane
//		for (Mesh mMesh : this.model.meshes) {
//			mMesh.transform(new Matrix4().setToTranslation(0f, -xzPlaneOffset, 0f));
//		}
		
		autoScaleFactor = (float) TARGET_IMAGE_WIDTH / dimensions.x;
		if (autoScaleFactor > ((float) TARGET_IMAGE_HEIGHT / dimensions.z))
			autoScaleFactor = (float) TARGET_IMAGE_HEIGHT / dimensions.z;
	}
	
	/********************************************************************************
	 * Performs a certain animation of 3D model if applicable
	 * @param id	: the index of the animation in animation list
	 */
	public void doAnimation(int id) {
		if (animation != null) {
			if (id >= 0 && id < this.animations.size) {
				animation.animate(animations.get(id).id, -1, 1f, null, 0.2f);
			}
		}
	}
	
	/********************************************************************************
	 * Updates the animation of 3D model if applicable.
	 */
	public void updateAnimation() {
		if (animation != null) {
			animation.update(Gdx.graphics.getDeltaTime());
		}
	}
	
	/********************************************************************************
	 * Returns the offset position of the 3D model on screen.
	 * @return The offset position
	 */
	public Vector2 getPosition() {
		return offset;
	}
	
	/********************************************************************************
	 * Sets offset position on screen of the 3D model.
	 * @param x	: horizontal offset
	 * @param y	: vertical offset
	 */
	public void setPosition(float x, float y) {
		offset.set(x, y);
	}
	
	/********************************************************************************
	 * Returns the scale factor received from server
	 * @return	The scale factor value
	 */
	public float getScaleFactor() {
		return scaleFactor;
	}
	
	/********************************************************************************
	 * Sets the original scale factor. This value is received from server.
	 * @param s	: the scale factor
	 */
	public void setScaleFactor(float s) {
		scaleFactor = s;
	}
	
	/********************************************************************************
	 * Rotates the 3D model around the world z-axis.
	 * @param angle	: the rotation angle
	 */
	public void rotateVerticalAxis(float angle) {
		Matrix4 invMat = gestureRotationMatrix.cpy().inv();
		Vector3 oldZAxis = (new Vector3(0f, 1f, 0f)).mul(invMat);
		gestureRotationMatrix.rotate(oldZAxis, -angle);
	}
	
	/********************************************************************************
	 * Rotates the 3D model around the world x-axis
	 * @param angle	: the rotation angle
	 */
	public void rotateXAxis(float angle) {
		Matrix4 invMat = gestureRotationMatrix.cpy().inv();
		Vector3 oldXAxis = (new Vector3(1f, 0f, 0f)).mul(invMat);
		gestureRotationMatrix.rotate(oldXAxis, -angle);
	}

	/********************************************************************************
	 * Returns the current scale, modified by gesture input.
	 * @return the current scale
	 */
	public float getCurrentScale() {
		return currentScale;
	}
	
	/********************************************************************************
	 * Scales the 3D model by a factor s.
	 * @param s	: the scale factor
	 */
	public void scale(float s) {
		currentScale = s;
	}
	
	/********************************************************************************
	 * Applies the gesture into 3D model.
	 */
	private void updateGestureInto3DModel() {
		transform.mul(gestureRotationMatrix.cpy().scale(currentScale, currentScale, currentScale));
	}
	
	/********************************************************************************
	 * Updates all the transformation for the 3D model
	 */
	public void update() {
		
		if (entranceScale < 1)
			entranceScale += 1f / 12;
		
		originalMatrix = (new Matrix4()).idt()
				.scale(0.5f, 0.5f, 0.5f) // for consistence with iOS
				.scale(autoScaleFactor, autoScaleFactor, autoScaleFactor)
				.scale(scaleFactor, scaleFactor, scaleFactor)
				.scale(entranceScale, entranceScale, entranceScale);
//		originalMatrix = (new Matrix4()).idt().rotate(Vector3.Z, -90)
//				.scale(autoScaleFactor, autoScaleFactor, autoScaleFactor)
//				.scale(scaleFactor, scaleFactor, scaleFactor)
//				.scale(entranceScale, entranceScale, entranceScale);
		transform.idt().mul(originalMatrix);

		updateGestureInto3DModel();
		updateAnimation();
	}
	
	private float entranceScale = 0;
}
