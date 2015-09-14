/**
 * Copyright 2012 Facebook
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This class has been referred from Facebook's Hackbook example.
 */

package com.gkxim.android.thanhniennews.social;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.gkxim.android.utils.GKIMLog;

/**
 * Skeleton base class for RequestListeners, providing default error handling.
 * Applications should handle these error conditions.
 */
@SuppressWarnings("deprecation")
public abstract class FBBaseRequestListener implements RequestListener {
	private static final String TAG = FBBaseRequestListener.class.getSimpleName();
    @Override
    public void onFacebookError(FacebookError e, final Object state) {
        GKIMLog.lf(null, 5, TAG + "=>Facebook: " + e.getMessage());
    }

    @Override
    public void onFileNotFoundException(FileNotFoundException e, final Object state) {
    	GKIMLog.lf(null, 5, TAG + "=>Facebook: " + e.getMessage());
    }

    @Override
    public void onIOException(IOException e, final Object state) {
    	GKIMLog.lf(null, 5, TAG + "=>Facebook: " + e.getMessage());
    }

    @Override
    public void onMalformedURLException(MalformedURLException e, final Object state) {
    	GKIMLog.lf(null, 5, TAG + "=>Facebook: " + e.getMessage());
    }

}