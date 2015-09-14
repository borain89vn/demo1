package com.knx.framework.arcontents;

import java.util.ArrayList;

import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.GestureDetector.GestureAdapter;
import com.badlogic.gdx.math.Vector2;
import com.knx.framework.arcontents.old.Static3DModel;
import com.knx.framework.arcontents.overlay.Overlay3DModel;

public class GestureListener extends GestureAdapter {
	
	private float initialZoomDistance;
	private float previousZoomScale;
	
	private static final float ROTATE_MULTIPLIER = 0.5f;
	private static final float ROTATE_THRESHOLD = .5f;
	
	private static final float SCALE_THRESHOLD = 0.01f;
	private static final float SCALE_UP_LIMIT = 5.0f;
	private static final float SCALE_DOWN_LIMIT = 0.2f;
	
	private ArrayList<Static3DModel> selfRotatedInstances;
	private ArrayList<Overlay3DModel> overlayInstances;
	private Static3DModel activeInstance;
	
	private ArrayList<Vector2> touchDownPointers = new ArrayList<Vector2>();
	
	/********************************************************************************
	 * Constructor method.
	 */
	public GestureListener() {
		super();
	}
	
	/********************************************************************************
	 * Sets self-rotated instances affected by gesture input. Registering self-rotated
	 * instances unregisters overlay instances.
	 * @param arr	: the array of model instances
	 */
	public void registerSelfRotatedInstances(ArrayList<Static3DModel> arr) {
		selfRotatedInstances = arr;
		if (overlayInstances != null) {
			overlayInstances.clear();
			overlayInstances = null;
		}
	}
	
	/********************************************************************************
	 * Sets overlay instances affected by gesture input. Registering overlay instances
	 * unregisters self-rotated instances.
	 * @param arr	: the array of model instances
	 */
	public void registerOverlayInstances(ArrayList<Overlay3DModel> arr) {
		overlayInstances = arr;
		if (selfRotatedInstances != null) {
			selfRotatedInstances.clear();
			selfRotatedInstances = null;
		}
	}
	
	/********************************************************************************
	 * Override method. This is called when the screen is touched.
	 * @param x			: x-coordinates
	 * @param y			: y-coordinates
	 * @param pointer	: the pointer index of touches the screen
	 * @param button	:
	 */
	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		
		if (selfRotatedInstances == null && overlayInstances == null)
			return false;
		
		activeInstance = null;
		if (selfRotatedInstances != null && selfRotatedInstances.size() > 0) {
			
			if (pointer == 0) {
				touchDownPointers.clear();
			}
			
			touchDownPointers.add(new Vector2(x, y));
			
			switch (touchDownPointers.size()) {
				case 1:
					for (Static3DModel instance : selfRotatedInstances) {
						if (instance.isTouched(touchDownPointers.get(0).x, Gdx.graphics.getHeight() - touchDownPointers.get(0).y)) {
							activeInstance = instance;
						}
					}
					break;
				case 2:
					for (Static3DModel instance : selfRotatedInstances) {
						if ((instance.isTouched(touchDownPointers.get(0).x, Gdx.graphics.getHeight() - touchDownPointers.get(0).y))
								&& (instance.isTouched(touchDownPointers.get(1).x, Gdx.graphics.getHeight() - touchDownPointers.get(1).y))) {
							activeInstance = instance;
						}
					}
					break;
//				default:
//					activeInstance = null;
			}
		}
		
		if (overlayInstances != null && overlayInstances.size() > 0) {
			if (pointer == 0) {
				touchDownPointers.clear();
			}
			
			touchDownPointers.add(new Vector2(x, y));
		}
		
		initialZoomDistance = 0;
		previousZoomScale = 1;
		
		return false;
	}
	
	/********************************************************************************
	 * Override method. This is called when tapping on screen.
	 * @param x			: x-coordinates
	 * @param y			: y-coordinates
	 * @param count		: number of taps
	 * @param button	: 
	 */
	@Override
	public boolean tap(float x, float y, int count, int button) {
		
		if (selfRotatedInstances == null && overlayInstances == null)
			return false;
		
		if (selfRotatedInstances != null && activeInstance == null)
			return false;
		
		if (activeInstance != null)
			activeInstance.toggleSeflRotation();
		
		return false;
	}

	/********************************************************************************
	 * Override method. This is called when dragging on screen.
	 * @param x			: x-coordinates
	 * @param y			: y-coordinates
	 * @param deltaX	: the difference in x-coordinates
	 * @param deltaY	: the difference in y-coordinates
	 */
	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		
		if (selfRotatedInstances == null && overlayInstances == null)
			return false;
		
		if (selfRotatedInstances != null && activeInstance == null)
			return false;
		
		boolean horizontally = Math.abs(deltaX) > Math.abs(deltaY);
		float rotateDeg = horizontally ? -deltaX : -deltaY;
		rotateDeg *= ROTATE_MULTIPLIER;

		if (Math.abs(rotateDeg) > ROTATE_THRESHOLD) {
			if (horizontally) {
				if (selfRotatedInstances != null) {
//					activeInstance.rotateZAxis(rotateDeg);
					activeInstance.rotateYAxis(rotateDeg);
				}
				if (overlayInstances != null)	
					for (Overlay3DModel instance : overlayInstances)
						instance.rotateVerticalAxis(rotateDeg);
			} else {
				if (selfRotatedInstances != null) {
					activeInstance.rotateXAxis(rotateDeg);
				}
				if (overlayInstances != null)
					for (Overlay3DModel instance : overlayInstances)
						instance.rotateXAxis(rotateDeg);
			}
		}
		return false;
	}
	
	/********************************************************************************
	 * Override method. This is called when dragging stops.
	 * @param x			: x-coordinates
	 * @param y			: y-coordinates
	 * @param pointer	: the pointer index of touches the screen
	 * @param button	: 
	 */
	@Override
	public boolean panStop(float x, float y, int pointer, int button) {
		
		if (selfRotatedInstances == null && overlayInstances == null)
			return false;
		
		if (selfRotatedInstances != null && activeInstance == null)
			return false;
		
		return false;
	}

	/********************************************************************************
	 * Override method. This is called when zoom occurs.
	 * @param initialDistance	: original distance 
	 * @param distance			: current distance
	 */
	@Override
	public boolean zoom(float initialDistance, float distance) {
		
		Log.i("ARiseGestureListener", "Distance: " + initialDistance + " - " + distance);
		
		if (selfRotatedInstances == null && overlayInstances == null)
			return false;
		
		if (selfRotatedInstances != null && activeInstance == null)
			return false;
		
		float scale = distance / initialDistance;
		
		if (initialDistance == initialZoomDistance) {
			scale /= previousZoomScale;
		} else {
			initialZoomDistance = initialDistance;
		}
		
		previousZoomScale = distance / initialDistance;

		if (Math.abs(scale - 1.0f) > SCALE_THRESHOLD) {
			if (activeInstance != null) {
				float resultScale = activeInstance.getCurrentScale() * scale;
				if (resultScale <= SCALE_UP_LIMIT && resultScale >= SCALE_DOWN_LIMIT) { // only perform scale in allowed limits
					activeInstance.scale(resultScale);
				}
			}
			
			if (overlayInstances != null) {
				for (Overlay3DModel instance : overlayInstances) {
					float resultScale = instance.getCurrentScale() * scale;
					if (resultScale <= SCALE_UP_LIMIT && resultScale >= SCALE_DOWN_LIMIT) // only perform scale in allowed limits
						instance.scale(resultScale);
				}
			}
		}
		
		return false;
	}
	
	/********************************************************************************
	 * Override method. This is called when long pressing occurs.
	 * @param x			: x-coordinates
	 * @param y			: y-coordinates
	 */
	@Override
	public boolean longPress(float x, float y) {
		
		if (selfRotatedInstances == null && overlayInstances == null)
			return false;
		
		if (selfRotatedInstances != null && activeInstance == null)
			return false;
		
		if (activeInstance != null)
			activeInstance.reset();
		
		for (Overlay3DModel instance : overlayInstances) {
			instance.reset();
		}
		
		return false;
	}
}