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

import pixedar.com.superlapse.Dslr.ptp.PtpCamera;
import pixedar.com.superlapse.Dslr.ptp.PtpCamera.IO;
import pixedar.com.superlapse.Dslr.ptp.PtpConstants;
import pixedar.com.superlapse.Dslr.ptp.PtpConstants.Operation;
import pixedar.com.superlapse.Dslr.ptp.PtpConstants.Response;
import pixedar.com.superlapse.Dslr.ptp.model.DeviceInfo;

import java.nio.ByteBuffer;

public class GetDeviceInfoCommand extends Command {

    private DeviceInfo info;

    public GetDeviceInfoCommand(PtpCamera camera) {
        super(camera);
    }

    @Override
    public void exec(IO io) {
        io.handleCommand(this);
        if (responseCode != Response.Ok) {
            camera.onPtpError(String.format("Couldn't read device information, error code \"%s\"",
                    PtpConstants.responseToString(responseCode)));
        } else if (info == null) {
            camera.onPtpError("Couldn't retrieve device information");
        }
    }

    @Override
    public void reset() {
        super.reset();
        info = null;
    }

    @Override
    public void encodeCommand(ByteBuffer b) {
        encodeCommand(b, Operation.GetDeviceInfo);
    }

    @Override
    protected void decodeData(ByteBuffer b, int length) {
        info = new DeviceInfo(b, length);
        camera.setDeviceInfo(info);
    }
}