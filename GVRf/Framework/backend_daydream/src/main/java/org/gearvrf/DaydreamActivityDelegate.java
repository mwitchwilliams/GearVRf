/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf;

import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.view.KeyEvent;

import org.gearvrf.utility.Log;
import org.gearvrf.utility.VrAppSettings;

/**
 * {@inheritDoc}
 */
final class DaydreamActivityDelegate implements GVRActivity.GVRActivityDelegate {
    private GVRActivity mActivity;

    @Override
    public void onCreate(GVRActivity activity) {
        mActivity = activity;
    }

    @Override
    public IActivityNative getActivityNative() {
        return null;
    }

    @Override
    public GVRViewManager makeViewManager() {
        return new DaydreamViewManager(mActivity, mActivity.getScript());
    }

    @Override
    public GVRViewManager makeMonoscopicViewManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GVRCameraRig makeCameraRig(GVRContext context) {
        return new GVRCameraRig(context);
    }

    @Override
    public GVRConfigurationManager makeConfigurationManager(GVRActivity activity) {
        return new GVRConfigurationManager(activity) {
            @Override
            public boolean isHmtConnected() {
                return false;
            }
        };
    }

    @Override
    public void onInitAppSettings(VrAppSettings appSettings) {
        // This is the only place where the setDockListenerRequired flag can be set before
        // the check in GVRActivity.
        mActivity.getConfigurationManager().setDockListenerRequired(false);
    }

    @Override
    public void parseXmlSettings(AssetManager assetManager, String dataFilename) {
        new DaydreamXMLParser(assetManager, dataFilename, mActivity.getAppSettings());
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    }

    @Override
    public void setScript(GVRScript gvrScript, String dataFileName) {
    }

    @Override
    public void setViewManager(GVRViewManager viewManager) {
    }

    @Override
    public VrAppSettings makeVrAppSettings() {
        final VrAppSettings settings = new VrAppSettings();
        final VrAppSettings.EyeBufferParams params = settings.getEyeBufferParams();
        params.setResolutionHeight(VrAppSettings.DEFAULT_FBO_RESOLUTION);
        params.setResolutionWidth(VrAppSettings.DEFAULT_FBO_RESOLUTION);
        return settings;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return false;
    }
}