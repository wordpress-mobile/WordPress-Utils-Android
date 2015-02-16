package org.wordpress.android.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MediaUtils {
    public class RequestCode {
        public static final int ACTIVITY_REQUEST_CODE_PICTURE_LIBRARY = 1000;
        public static final int ACTIVITY_REQUEST_CODE_TAKE_PHOTO      = 1100;
        public static final int ACTIVITY_REQUEST_CODE_VIDEO_LIBRARY   = 1200;
        public static final int ACTIVITY_REQUEST_CODE_TAKE_VIDEO      = 1300;
    }

    public static final String[] RECOGNIZED_IMAGE_EXTENSIONS = {
        ".png", ".jpg", ".jpeg", ".gif"
    };
    public static final String[] RECOGNIZED_VIDEO_EXTENSIONS = {
        ".ogv", ".mp4", ".m4v", ".mov", ".wmv", ".avi", ".mpg", ".3gp", ".3g2"
    };
    public static final String[] RECOGNIZED_DOCUMENT_EXTENSIONS = {
        ".doc", ".docx", ".odt", ".pdf"
    };
    public static final String[] RECOGNIZED_PRESENTATION_EXTENSIONS = {
        ".ppt", ".pptx", ".pps", ".ppsx", ".key"
    };
    public static final String[] RECOGNIZED_SPREADSHEET_EXTENSIONS = {
        ".xls", ".xlsx"
    };

    private static final long FADE_TIME_MS = 250;

    /**
     * Generic method for determining if a given URL has a recognizable extension
     *
     * @param url
     *  the URL of the file
     * @param recognizedExtensions
     *  an array of recognized extensions to compare
     * @return
     *  true if the given URL has a recognized extension, otherwise false
     */
    public static boolean isRecognized(String url, String[] recognizedExtensions) {
        if (url == null) {
            return false;
        }

        for (String extension : recognizedExtensions) {
            if (url.endsWith(extension)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines if the file referred to by url has a recognized image extension.
     *
     * @param url
     *  the URL of the image file
     * @return
     *  true if the URL is recognized as a image file, otherwise false
     */
    public static boolean isValidImage(String url) {
        return isRecognized(url, RECOGNIZED_IMAGE_EXTENSIONS);
    }

    /**
     * Determines if the file referred to by url has a recognized video extension.
     *
     * @param url
     *  the URL of the video file
     * @return
     *  true if the URL is recognized as a video file, otherwise false
     */
    public static boolean isVideo(String url) {
        return isRecognized(url, RECOGNIZED_VIDEO_EXTENSIONS);
    }

    /**
     * Determines if the file referred to by url has a recognized document extension.
     *
     * @param url
     *  the URL of the document file
     * @return
     *  true if the URL is recognized as a document file, otherwise false
     */
    private static boolean isDocument(String url) {
        return isRecognized(url, RECOGNIZED_DOCUMENT_EXTENSIONS);
    }

    /**
     * Determines if the file referred to by url has a recognized presentation extension.
     *
     * @param url
     *  the URL of the presentation file
     * @return
     *  true if the URL is recognized as a presentation file, otherwise false
     */
    private static boolean isPresentation(String url) {
        return isRecognized(url, RECOGNIZED_PRESENTATION_EXTENSIONS);
    }

    /**
     * Determines if the file referred to by url has a recognized spreadsheet extension.
     *
     * @param url
     *  the URL of the spreadsheet file
     * @return
     *  true if the URL is recognized as a spreadsheet file, otherwise false
     */
    private static boolean isSpreadsheet(String url) {
        return isRecognized(url, RECOGNIZED_SPREADSHEET_EXTENSIONS);
    }

    /**
     * Determines the MIME type of data in a given stream.
     *
     * @param stream
     *  the input data to be interpreted
     * @return
     *  the MIME type if one can be determined
     */
    public static String getMimeTypeOfInputStream(InputStream stream) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(stream, null, options);

        return options.outMimeType;
    }

    /**
     * Appends an appropriate extension to to a given file based on its MIME type.
     *
     * @param mediaFile
     *  the file used to generate the file name
     * @param mimeType
     *  the MIME type of the file
     * @return
     *  the file name, including an appropriate extension for the given MIME type
     */
    public static String getMediaFileName(File mediaFile, String mimeType) {
        String originalFileName = mediaFile.getName().toLowerCase();
        String extension = MimeTypeMap.getFileExtensionFromUrl(originalFileName);

        // File name already has the extension in it
        if (!TextUtils.isEmpty(extension)) {
            return originalFileName;
        }

        if (!TextUtils.isEmpty(mimeType)) { //try to get the extension from mimeType
            String fileExtension = getExtensionForMimeType(mimeType);
            if (!TextUtils.isEmpty(fileExtension)) {
                originalFileName += "." + fileExtension;
            }
        } else {
            //No mimetype and no extension!!
            AppLog.w(AppLog.T.API, "No mimetype and no extension for " + mediaFile.getPath());
        }

        return originalFileName;
    }

    /**
     * Determines a file extension for a given MIME type by querying the {@link android.webkit.MimeTypeMap}.
     *
     * @param mimeType
     *  the MIME type to get an extension for
     * @return
     *  an extension for the given MIME type; never null
     */
    public static String getExtensionForMimeType(String mimeType) {
        if (TextUtils.isEmpty(mimeType)) {
            return "";
        }

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String fileExtensionFromMimeType = mimeTypeMap.getExtensionFromMimeType(mimeType);
        if (TextUtils.isEmpty(fileExtensionFromMimeType)) {
            // We're still without an extension - split the mime type and retrieve it
            String[] split = mimeType.split("/");
            fileExtensionFromMimeType = split.length > 1 ? split[1] : split[0];
        }

        return fileExtensionFromMimeType.toLowerCase();
    }

    /**
     * Determines the MIME type of an existing media file.
     *
     * @param mediaFile
     *  the input file
     * @return
     *  the MIME type, if one can be determined; never null
     */
    public static String getMediaFileMimeType(File mediaFile) {
        String originalFileName = mediaFile.getName().toLowerCase();
        String mimeType = UrlUtils.getUrlMimeType(originalFileName);

        if (TextUtils.isEmpty(mimeType)) {
            try {
                String filePathForGuessingMime;
                if (mediaFile.getPath().contains("://")) {
                    filePathForGuessingMime = Uri.encode(mediaFile.getPath(), ":/");
                } else {
                    filePathForGuessingMime = "file://"+ Uri.encode(mediaFile.getPath(), "/");
                }
                URL urlForGuessingMime = new URL(filePathForGuessingMime);
                URLConnection uc = urlForGuessingMime.openConnection();
                String guessedContentType = uc.getContentType(); //internally calls guessContentTypeFromName(url.getFile()); and guessContentTypeFromStream(is);
                // check if returned "content/unknown"
                if (!TextUtils.isEmpty(guessedContentType) && !guessedContentType.equals("content/unknown")) {
                    mimeType = guessedContentType;
                }
            } catch (MalformedURLException e) {
                AppLog.e(AppLog.T.API, "MalformedURLException while trying to guess the content type for the file here " + mediaFile.getPath() + " with URLConnection", e);
            }
            catch (IOException e) {
                AppLog.e(AppLog.T.API, "Error while trying to guess the content type for the file here " + mediaFile.getPath() +" with URLConnection", e);
            }
        }

        // No mimeType yet? Try to decode the image and get the mimeType from there
        if (TextUtils.isEmpty(mimeType)) {
            try {
                DataInputStream inputStream = new DataInputStream(new FileInputStream(mediaFile));
                String mimeTypeFromStream = getMimeTypeOfInputStream(inputStream);
                if (!TextUtils.isEmpty(mimeTypeFromStream)) {
                    mimeType = mimeTypeFromStream;
                }
                inputStream.close();
            } catch (FileNotFoundException e) {
                AppLog.e(AppLog.T.API, "FileNotFoundException while trying to guess the content type for the file " + mediaFile.getPath(), e);
            } catch (IOException e) {
                AppLog.e(AppLog.T.API, "IOException while trying to guess the content type for the file " + mediaFile.getPath(), e);
            }
        }

        if (TextUtils.isEmpty(mimeType)) {
            mimeType = "";
        } else {
            if (mimeType.equalsIgnoreCase("video/mp4v-es")) { //Fixes #533. See: http://tools.ietf.org/html/rfc3016
                mimeType = "video/mp4";
            }
        }

        return mimeType;
    }

    /**
     * Determines which drawable resource to use as a placeholder image for a given file URL.
     *
     * @param url
     *  the URL of the file
     * @return
     *  the resource ID of the drawable resource to use if the file is identified, otherwise -1
     */
    public static int getPlaceholder(String url) {
        if (isValidImage(url))
            return R.drawable.media_image_placeholder;
        else if(isDocument(url))
            return R.drawable.media_document;
        else if(isPresentation(url))
            return R.drawable.media_powerpoint;
        else if(isSpreadsheet(url))
            return R.drawable.media_spreadsheet;
        else if(isVideo(url))
            return R.drawable.media_movieclip;

        return -1;
    }

    /**
     * Retrieves specified columns from {@link android.provider.MediaStore.Images.Thumbnails}.
     *
     * @param contentResolver
     *  required for query
     * @param columns
     *  column names to retrieve
     * @return
     *  resultant rows with requested columns
     */
    public static Cursor getMediaStoreThumbnails(ContentResolver contentResolver, String[] columns) {
        Uri thumbnailUri = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
        return MediaStore.Images.Thumbnails.query(contentResolver, thumbnailUri, columns);
    }

    /**
     * Retrieves specified columns from {@link android.provider.MediaStore.Video}.
     *
     * @param contentResolver
     *  required for query
     * @param columns
     *  column names to retrieve
     * @return
     *  resultant rows with requested columns
     */
    public static Cursor getDeviceMediaStoreVideos(ContentResolver contentResolver, String[] columns) {
        Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        return MediaStore.Video.query(contentResolver, videoUri, columns);
    }

    /**
     * Shows a cancellable dialog that reminds the user to check their SD card.
     *
     * @param activity
     *  required to show the dialog
     */
    private static void showSDCardRequiredDialog(Activity activity) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(activity.getResources().getText(R.string.sd_card_required_title));
        dialogBuilder.setMessage(activity.getResources().getText(R.string.sd_card_required_message));
        dialogBuilder.setPositiveButton(activity.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        dialogBuilder.setCancelable(true);
        dialogBuilder.create().show();
    }

    /**
     * Fades a {@link android.graphics.Bitmap} image into an {@link android.widget.ImageView} with
     * a {@link org.wordpress.android.util.MediaUtils#FADE_TIME_MS} duration.
     *
     * Same as calling {@link org.wordpress.android.util.MediaUtils#fadeInImage(android.widget.ImageView, android.graphics.Bitmap, long)}
     * with duration {@link org.wordpress.android.util.MediaUtils#FADE_TIME_MS}.
     *
     * @param imageView
     *  the {@link android.view.View} to display the image
     * @param image
     *  the image to display
     */
    public static void fadeInImage(ImageView imageView, Bitmap image) {
        fadeInImage(imageView, image, FADE_TIME_MS);
    }

    /**
     * Fades a {@link android.graphics.Bitmap} image into an {@link android.widget.ImageView} with
     * a {@link org.wordpress.android.util.MediaUtils#FADE_TIME_MS} duration.
     *
     * @param imageView
     *  the {@link android.view.View} to display the image
     * @param image
     *  the image to display
     */
    public static void fadeInImage(ImageView imageView, Bitmap image, long duration) {
        if (imageView != null) {
            imageView.setImageBitmap(image);
            Animation alpha = new AlphaAnimation(0.25f, 1.0f);
            alpha.setDuration(duration);
            imageView.startAnimation(alpha);
            // Use the implementation below if you can figure out how to make it work on all devices
            // My Galaxy S3 (4.1.2) would not animate
//            imageView.setImageBitmap(image);
//            ObjectAnimator.ofFloat(imageView, View.ALPHA, 0.25f, 1.0f).setDuration(duration).start();
        }
    }

    public static class BackgroundFetchThumbnail extends AsyncTask<Uri, String, Bitmap> {
        public static final int TYPE_IMAGE = 0;
        public static final int TYPE_VIDEO = 1;

        private WeakReference<ImageView> mReference;
        private ImageLoader.ImageCache mCache;
        private int mType;
        private int mWidth;
        private int mHeight;
        private int mRotation;

        public BackgroundFetchThumbnail(ImageView resultStore, ImageLoader.ImageCache cache, int type, int width, int height, int rotation) {
            mReference = new WeakReference<ImageView>(resultStore);
            mCache = cache;
            mType = type;
            mWidth = width;
            mHeight = height;
            mRotation = rotation;
        }

        @Override
        protected Bitmap doInBackground(Uri... params) {
            String uri = params[0].toString();
            Bitmap bitmap = null;

            if (mType == TYPE_IMAGE) {
                Bitmap imageBitmap = BitmapFactory.decodeFile(uri);
                bitmap = ThumbnailUtils.extractThumbnail(imageBitmap, mWidth, mHeight);

                Matrix rotation = new Matrix();
                rotation.setRotate(mRotation, bitmap.getWidth() / 2.0f, bitmap.getHeight() / 2.0f);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotation, false);
            } else if (mType == TYPE_VIDEO) {
                // MICRO_KIND = 96 x 96
                // MINI_KIND = 512 x 384
                bitmap = ThumbnailUtils.createVideoThumbnail(uri, MediaStore.Video.Thumbnails.MINI_KIND);
            }

            if (mCache != null && bitmap != null) {
                mCache.putBitmap(uri, bitmap);
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            ImageView imageView = mReference.get();

            if (imageView != null) {
                if (imageView.getTag() == this) {
                    imageView.setTag(null);
                    fadeInImage(imageView, result);
                }
            }
        }
    }
}
