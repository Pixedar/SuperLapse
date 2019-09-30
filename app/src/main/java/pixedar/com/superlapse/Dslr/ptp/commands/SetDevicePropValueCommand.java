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
import pixedar.com.superlapse.Dslr.ptp.PtpConstants.Datatype;
import pixedar.com.superlapse.Dslr.ptp.PtpConstants.Operation;
import pixedar.com.superlapse.Dslr.ptp.PtpConstants.Response;
import pixedar.com.superlapse.Dslr.ptp.PtpConstants.Type;

public class SetDevicePropValueCommand extends Command {

    private final int property;
    private final int value;
    private final int datatype;

    public SetDevicePropValueCommand(PtpCamera camera, int property, int value, int datatype) {
        super(camera);
        this.property = property;
        this.value = value;
        this.datatype = datatype;
        hasDataToSend = true;
    }

    @Override
    public void exec(IO io) {
        io.handleCommand(this);
        if (responseCode == Response.DeviceBusy) {
            camera.onDeviceBusy(this, true);
            return;
        } else if (responseCode == Response.Ok) {
            camera.onPropertyChanged(property, value);
        }
    }

    @Override
    public void encodeCommand(ByteBuffer b) {
        encodeCommand(b, Operation.SetDevicePropValue, property);
    }

    @Override
    public void encodeData(ByteBuffer b) {
            //header
            b.putInt(12 + PtpConstants.getDatatypeSize(datatype));
            b.putShort((short) Type.Data);
            b.putShort((short) Operation.SetDevicePropValue);
            b.putInt(camera.currentTransactionId());

            // specific block
            if (datatype == Datatype.int8 || datatype == Datatype.uint8) {
                b.put((byte) value);
            } else if (datatype == Datatype.int16 || datatype == Datatype.uint16) {
                b.putShort((short) value);
            } else if (datatype == Datatype.int32 || datatype == Datatype.uint32) {
                b.putInt(value);
            } else {
                throw new UnsupportedOperationException();
            }

         /*   log.log("start");
            for(byte c:b.array()){
                log.log(String.format("%02X ",c));
            }
            log.log("end");*/
        }
}
