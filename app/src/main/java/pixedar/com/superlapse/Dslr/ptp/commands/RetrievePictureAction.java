/**
 * Copyright 2013 Nils Assbeck, Guersel Ayaz and Michael Zoech
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
package pixedar.com.superlapse.Dslr.ptp.commands;

import android.graphics.Bitmap;
import android.util.Log;

import pixedar.com.superlapse.Dslr.ptp.PtpAction;
import pixedar.com.superlapse.Dslr.ptp.PtpCamera;
import pixedar.com.superlapse.Dslr.ptp.PtpCamera.IO;
import pixedar.com.superlapse.Dslr.ptp.PtpConstants;
import pixedar.com.superlapse.Dslr.ptp.PtpConstants.Response;
import pixedar.com.superlapse.Dslr.ptp.model.ObjectInfo;

public class RetrievePictureAction implements PtpAction {

    private final PtpCamera camera;
    private final int objectHandle;
    private final int sampleSize;

    public RetrievePictureAction(PtpCamera camera, int objectHandle, int sampleSize) {
        this.camera = camera;
        this.objectHandle = objectHandle;
        this.sampleSize = sampleSize;
    }

    @Override
    public void exec(IO io) {
        Log.d("DEBUG","1D");
        GetObjectInfoCommand getInfo = new GetObjectInfoCommand(camera, objectHandle);
        io.handleCommand(getInfo);

        if (getInfo.getResponseCode() != Response.Ok) {
            Log.d("DEBUG","2D");
            return;
        }

        ObjectInfo objectInfo = getInfo.getObjectInfo();
        if (objectInfo == null) {
            Log.d("DEBUG","3D");
            return;
        }

        Bitmap thumbnail = null;
        if (objectInfo.thumbFormat == PtpConstants.ObjectFormat.JFIF
                || objectInfo.thumbFormat == PtpConstants.ObjectFormat.EXIF_JPEG) {
            GetThumb getThumb = new GetThumb(camera, objectHandle);
            io.handleCommand(getThumb);
            if (getThumb.getResponseCode() == Response.Ok) {
                thumbnail = getThumb.getBitmap();
                Log.d("DEBUG","1C");
            }
        }else {
            Log.d("DEBUG","2C");
        }

        GetObjectCommand getObject = new GetObjectCommand(camera, objectHandle, sampleSize);
        io.handleCommand(getObject);

        if (getObject.getResponseCode() != Response.Ok) {
            Log.d("DEBUG","3C");
            return;
        }

        if (getObject.getBitmap() == null) {
            Log.d("DEBUG","4C");
            if (getObject.isOutOfMemoryError()) {
                Log.d("DEBUG","5C");
                camera.onPictureReceived(objectHandle, getInfo.getObjectInfo().filename, thumbnail, null);
            }
            return;
        }

        if (thumbnail == null) {
            // TODO resize real picture?
            Log.d("DEBUG","6C");
        }

        camera.onPictureReceived(objectHandle, getInfo.getObjectInfo().filename, thumbnail, getObject.getBitmap());
    }

    @Override
    public void reset() {
    }
}
