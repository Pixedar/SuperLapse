package pixedar.com.superlapse.Dslr.ptp;

import android.content.Context;
import android.content.Intent;

public interface PtpService {

    void setCameraListener(Camera.CamerStateListener listener);

    void initialize(Context context, Intent intent,boolean resume);

    void shutdown();

    void lazyShutdown();

    public static class Singleton {
        private static PtpService singleton;

        public static PtpService getInstance(Context context) {
            if (singleton == null) {
                singleton = new PtpUsbService(context);
            }
            return singleton;
        }

        public static void setInstance(PtpService service) {
            singleton = service;
        }
    }
}
