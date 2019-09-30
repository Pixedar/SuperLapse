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

import android.util.Log;

import pixedar.com.superlapse.Dslr.AppConfig;
import pixedar.com.superlapse.Dslr.ptp.PtpCamera;
import pixedar.com.superlapse.Dslr.ptp.PtpCamera.IO;
import pixedar.com.superlapse.Dslr.ptp.PtpConstants;
import pixedar.com.superlapse.Dslr.ptp.PtpConstants.Response;
import pixedar.com.superlapse.Dslr.ptp.model.ObjectInfo;

import java.nio.ByteBuffer;

public class GetObjectInfoCommand extends Command {

    private final String TAG = GetObjectInfoCommand.class.getSimpleName();

    private final int outObjectHandle;
    private ObjectInfo inObjectInfo;

    public GetObjectInfoCommand(PtpCamera camera, int objectHandle) {
        super(camera);
        this.outObjectHandle = objectHandle;
    }

    public ObjectInfo getObjectInfo() {
        return inObjectInfo;
    }

    @Override
    public void exec(IO io) {
        io.handleCommand(this);
        if (responseCode == Response.DeviceBusy) {
            camera.onDeviceBusy(this, true);
        }
        if (inObjectInfo != null) {
            if (AppConfig.LOG) {
                Log.i(TAG, inObjectInfo.toString());
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
        inObjectInfo = null;
    }

    @Override
    public void encodeCommand(ByteBuffer b) {
        encodeCommand(b, PtpConstants.Operation.GetObjectInfo, outObjectHandle);
    }

    @Override
    protected void decodeData(ByteBuffer b, int length) {
        inObjectInfo = new ObjectInfo(b, length);
    }
}