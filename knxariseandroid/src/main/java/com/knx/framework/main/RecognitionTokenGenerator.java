package com.knx.framework.main;

import java.util.concurrent.atomic.AtomicLong;

public class RecognitionTokenGenerator {
	
	private static final String BASE_TOKEN = "android_" + android.os.Build.SERIAL + "_";
	
	private static RecognitionTokenGenerator singleton = null;
	
	private long startTime;
	private AtomicLong counter;
	
	public static void startNewSession() {
		singleton = new RecognitionTokenGenerator(System.currentTimeMillis());
	}
	
	public static RecognitionTokenGenerator getCurrentSession() {
		if (singleton == null)
			startNewSession();
		return singleton;
	}
	
	public static void endCurrentSession() {
		singleton = null;
	}
	
	private RecognitionTokenGenerator(long startTime) {
		this.startTime = startTime;
		this.counter = new AtomicLong(0);
	}
	
	public synchronized String increaseAndGet() {
		String token = BASE_TOKEN + startTime + "_" + counter.incrementAndGet();
		return token;
	}
}
