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
import pixedar.com.superlapse.Dslr.ptp.PtpCamera.IO;
import pixedar.com.superlapse.Dslr.ptp.PtpConstants;
import pixedar.com.superlapse.Dslr.ptp.PtpConstants.Operation;
import pixedar.com.superlapse.Dslr.ptp.PtpConstants.Response;

import java.nio.ByteBuffer;

import pixedar.com.superlapse.Dslr.ptp.PtpCamera;

public class CloseSessionCommand extends Command {

    private final String TAG = CloseSessionCommand.class.getSimpleName();

    public CloseSessionCommand(PtpCamera camera) {
        super(camera);
    }

    @Override
    public void exec(IO io) {
        io.handleCommand(this);
        // Can this even happen?
        if (responseCode == Response.DeviceBusy) {
            camera.onDeviceBusy(this, true);
            return;
        }
        // close even when error happened
        camera.onSessionClosed();
        if (responseCode != Response.Ok) {
            // TODO error report
            if (AppConfig.LOG) {
                Log.w(TAG,
                        String.format("Error response when closing session, response %s",
                                PtpConstants.responseToString(responseCode)));
            }
        }
    }

    @Override
    public void encodeCommand(ByteBuffer b) {
        encodeCommand(b, Operation.CloseSession);
    }
}
