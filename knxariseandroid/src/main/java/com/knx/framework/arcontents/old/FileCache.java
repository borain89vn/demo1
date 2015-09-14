package com.knx.framework.arcontents.old;

import java.io.File;

import android.content.Context;

import com.knx.framework.main.Shared;

public class FileCache {

	private File cacheDir;

	public FileCache(Context context) {
		// Find the dir to save cached images

//		File assetDir = context.getDir("assets", Context.MODE_PRIVATE);
//		cacheDir = new File(assetDir.getAbsolutePath() + "/arise_poster");
		
		cacheDir = new File(Shared.getPosterDir(context));
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
	}

	public File getFile(String url) {

		String filename = (new File(url)).getName();
		File f = new File(cacheDir, filename);
		return f;
	}

	public void clear() {
		File[] files = cacheDir.listFiles();
		if (files == null)
			return;
		for (File f : files)
			f.delete();
	}

}