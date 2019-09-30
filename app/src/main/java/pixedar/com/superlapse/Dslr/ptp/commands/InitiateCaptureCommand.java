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

import java.nio.ByteBuffer;

import pixedar.com.superlapse.Dslr.ptp.PtpCamera;
import pixedar.com.superlapse.Dslr.ptp.PtpCamera.IO;
import pixedar.com.superlapse.Dslr.ptp.PtpConstants;
import pixedar.com.superlapse.Dslr.ptp.PtpConstants.Response;

public class InitiateCaptureCommand extends Command {
    boolean type;
    public InitiateCaptureCommand(PtpCamera camera ,boolean type) {
        super(camera);
        this.type = type;
    }

    @Override
    public void exec(IO io) {
        io.handleCommand(this);
        if (responseCode == Response.DeviceBusy) {
            camera.onDeviceBusy(this, true); // TODO when nikon live view is enabled this stalls
            return;
        }
    }

    @Override
    public void encodeCommand(ByteBuffer b) {

       encodeCommand(b, PtpConstants.Operation.InitiateCapture, 0, 0);


     //   encodeCommand(b, Operation.InitiateCapture, 0, 0);
    }
}
