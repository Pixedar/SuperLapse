package pixedar.com.superlapse.Camera2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.DngCreator;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import pixedar.com.superlapse.Camera2.Utility.RefCountedAutoCloseable;
import pixedar.com.superlapse.TimelapseSettingsController;

import static android.graphics.Bitmap.createScaledBitmap;

public  class ImageSaver implements Runnable {

    private static final String TAG = "Camera2Controller";
    /**
     * The image to save.
     */
    private final Image mImage;
    /**
     * The file we save the image into.
     */
    private final File mFile;

    /**
     * The CaptureResult for this image capture.
     */
    private final CaptureResult mCaptureResult;

    /**
     * The CameraCharacteristics for this camera device.
     */
    private final CameraCharacteristics mCharacteristics;

    /**
     * The Context to use when updating MediaStore with the saved images.
     */
    private final Context mContext;

    /**
     * A reference counted wrapper for the ImageReader that owns the given image.
     */
    private final RefCountedAutoCloseable<ImageReader> mReader;
    public static int counter = 0;
    private String date;

    public int getCounter(){
        return counter;
    }
    private ImageSaver(Image image, File file, CaptureResult result,
                       CameraCharacteristics characteristics, Context context,
                       RefCountedAutoCloseable<ImageReader> reader,String date) {
        mImage = image;
        mFile = file;
        mCaptureResult = result;
        mCharacteristics = characteristics;
        mContext = context;
        mReader = reader;
        this.date = date;
    }
    private void save(final byte[] data) throws IOException{
        FileOutputStream outStream;

            if (TimelapseSettingsController.settings.saveOnSD) {
                DocumentFile file = TimelapseSettingsController.pickedDir.createFile("//MIME type", Integer.toString(counter) + ".jpg");
                outStream = (FileOutputStream) mContext.getContentResolver().openOutputStream(file.getUri());
            } else {
                File sd = new File(Environment.getExternalStorageDirectory() + "/TimelapseData/", date);
                if (!sd.isDirectory()) {
                    sd.mkdirs();
                }
                outStream = new FileOutputStream(sd + "/" + Integer.toString(counter) + ".jpg");
            }

            if (TimelapseSettingsController.settings.compression== 0 && (TimelapseSettingsController.settings.resoluionMenu[2] || TimelapseSettingsController.settings.resoluionMenu[3])) {
                outStream.write(data);
            } else {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                Bitmap photo = bitmap;
         /*       if (TimelapseSettingsController.settings.resoluionMenu[1]) {
                    photo = createScaledBitmap(bitmap, 1920, 1080, true);*/
             //   } else if (TimelapseSettingsController.settings.resoluionMenu[0]) {
                if (TimelapseSettingsController.settings.resoluionMenu[0]){
                    photo = createScaledBitmap(bitmap, 1920, 180, true);
                }

                photo.compress(Bitmap.CompressFormat.JPEG, TimelapseSettingsController.settings.compression, bytes);

                if (outStream != null) {
                    outStream.write(bytes.toByteArray());
                    outStream.close();
                }
            }
            counter++;

    }
    @Override
    public void run() {
        boolean success = false;
        int format = mImage.getFormat();
        switch (format) {
            case ImageFormat.JPEG: {
                ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                try {
                    save(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("DEBUG", e.getMessage());
                }finally {
                    mImage.close();
                }

              /*  FileOutputStream output = null;
                try {
                    output = new FileOutputStream(mFile);
                    output.write(bytes);
                    success = true;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mImage.close();
                    closeOutput(output);
                }*/
                break;
            }
            case ImageFormat.RAW_SENSOR: {
                DngCreator dngCreator = new DngCreator(mCharacteristics, mCaptureResult);
                FileOutputStream output = null;
                try {
                    output = new FileOutputStream(mFile);
                    dngCreator.writeImage(output, mImage);
                    success = true;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mImage.close();
                    closeOutput(output);
                }
                break;
            }
            default: {
                Log.e(TAG, "Cannot save image, unexpected image format:" + format);
                break;
            }
        }

        // Decrement reference count to allow ImageReader to be closed to free up resources.
        mReader.close();

        // If saving the file succeeded, update MediaStore.
        if (success) {
            MediaScannerConnection.scanFile(mContext, new String[]{mFile.getPath()},
                    /*mimeTypes*/null, new MediaScannerConnection.MediaScannerConnectionClient() {
                        @Override
                        public void onMediaScannerConnected() {
                            // Do nothing
                        }

                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i(TAG, "Scanned " + path + ":");
                            Log.i(TAG, "-> uri=" + uri);
                        }
                    });
        }
    }

    private static void closeOutput(OutputStream outputStream) {
        if (null != outputStream) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public static class ImageSaverBuilder {
        private Image mImage;
        private File mFile;
        private CaptureResult mCaptureResult;
        private CameraCharacteristics mCharacteristics;
        private Context mContext;
        private RefCountedAutoCloseable<ImageReader> mReader;

        /**
         * Construct a new ImageSaverBuilder using the given {@link Context}.
         *
         * @param context a {@link Context} to for accessing the
         *                {@link android.provider.MediaStore}.
         */
        String date;
        public ImageSaverBuilder(final Context context, String date) {
            mContext = context;
            this.date = date;
        }

        public synchronized ImageSaverBuilder setRefCountedReader(
                RefCountedAutoCloseable<ImageReader> reader) {
            if (reader == null) throw new NullPointerException();

            mReader = reader;
            return this;
        }

        public synchronized ImageSaverBuilder setImage(final Image image) {
            if (image == null) throw new NullPointerException();
            mImage = image;
            return this;
        }

        public synchronized ImageSaverBuilder setFile(final File file) {
            if (file == null) throw new NullPointerException();
            mFile = file;
            return this;
        }

        public synchronized ImageSaverBuilder setResult(final CaptureResult result) {
            if (result == null) throw new NullPointerException();
            mCaptureResult = result;
            return this;
        }

        public synchronized ImageSaverBuilder setCharacteristics(
                final CameraCharacteristics characteristics) {
            if (characteristics == null) throw new NullPointerException();
            mCharacteristics = characteristics;
            return this;
        }

        public synchronized ImageSaver buildIfComplete() {
            if (!isComplete()) {
                return null;
            }
            return new ImageSaver(mImage, mFile, mCaptureResult, mCharacteristics, mContext,
                    mReader,date);
        }

        public synchronized String getSaveLocation() {
            return (mFile == null) ? "Unknown" : mFile.toString();
        }

        private boolean isComplete() {
            return mImage != null && mFile != null && mCaptureResult != null
                    && mCharacteristics != null;
        }
    }
}