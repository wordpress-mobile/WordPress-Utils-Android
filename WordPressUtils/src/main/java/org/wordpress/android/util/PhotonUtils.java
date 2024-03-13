package org.wordpress.android.util;

import android.text.TextUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * routines related to the Photon API
 * http://developer.wordpress.com/docs/photon/
 */
public class PhotonUtils {
    private PhotonUtils() {
        throw new AssertionError();
    }

    /*
     * returns true if the passed url is an obvious "mshots" url
     */
    public static boolean isMshotsUrl(final String imageUrl) {
        return (imageUrl != null && imageUrl.contains("/mshots/"));
    }

    /*
     * returns a photon url for the passed image with the resize query set to the passed
     * dimensions - note that the passed quality parameter will only affect JPEGs
     */
    public enum Quality {
        HIGH,
        MEDIUM,
        LOW
    }

    public static final String ATOMIC_MEDIA_PROXY_URL_PREFIX = "https://public-api.wordpress.com/wpcom/v2/sites/";
    public static final String ATOMIC_MEDIA_PROXY_URL_SUFFIX = "/atomic-auth-proxy/file";

    public static String getPhotonImageUrl(String imageUrl, int width, int height) {
        return getPhotonImageUrl(imageUrl, width, height, Quality.MEDIUM);
    }

    public static String getPhotonImageUrl(String imageUrl, int width, int height, boolean isPrivateAtomicSite) {
        return getPhotonImageUrl(imageUrl, width, height, Quality.MEDIUM, isPrivateAtomicSite);
    }

    public static String getPhotonImageUrl(String imageUrl, int width, int height, Quality quality) {
        return getPhotonImageUrl(imageUrl, width, height, quality, false);
    }

    public static String getPhotonImageUrl(String imageUrl, int width, int height, Quality quality,
                                           boolean isPrivateAtomicSite) {
        if (TextUtils.isEmpty(imageUrl)) {
            return "";
        }

        // make sure it's valid
        int schemePos = imageUrl.indexOf("://");
        if (schemePos == -1) {
            return imageUrl;
        }

        // we have encountered some image urls that incorrectly have a # fragment part, which
        // must be removed before removing the query string
        int fragmentPos = imageUrl.indexOf("#");
        if (fragmentPos > 0) {
            imageUrl = imageUrl.substring(0, fragmentPos);
        }

        String urlCopy = imageUrl;

        // remove existing query string since it may contain params that conflict with the passed ones
        imageUrl = UrlUtils.removeQuery(imageUrl);

        // if this is an "mshots" url, skip photon and return it with a query that sets the width/height
        if (isMshotsUrl(imageUrl)) {
            return imageUrl + "?w=" + width + "&h=" + height;
        }

        // strip=info removes Exif, IPTC and comment data from the output image.
        String query = "?strip=info";

        switch (quality) {
            case HIGH:
                query += "&quality=100";
                break;
            case LOW:
                query += "&quality=35";
                break;
            default: // medium
                query += "&quality=65";
                break;
        }

        // if both width & height are passed use the "resize" param, use only "w" or "h" if just
        // one of them is set
        if (width > 0 && height > 0) {
            query += "&resize=" + width + "," + height;
        } else if (width > 0) {
            query += "&w=" + width;
        } else if (height > 0) {
            query += "&h=" + height;
        }

        if (isPrivateAtomicSite) {
            try {
                URL url = new URL(imageUrl);
                String slug = url.getHost();
                String path = url.getPath();
                return ATOMIC_MEDIA_PROXY_URL_PREFIX + slug + ATOMIC_MEDIA_PROXY_URL_SUFFIX
                       + "?path=" + path + "&" + query;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "";
            }
        }

        // return passed url+query if it's already a photon url
        if (imageUrl.contains(".wp.com")) {
            if (imageUrl.contains("i0.wp.com") || imageUrl.contains("i1.wp.com") || imageUrl.contains("i2.wp.com")) {
                boolean useSsl = urlCopy.indexOf("?") > 0 && urlCopy.contains("ssl=1");

                if (useSsl) {
                    query += "&ssl=1";
                }

                return imageUrl + query;
            }
        }

        // use wordpress.com as the host if image is on wordpress.com since it supports the same
        // query params and, more importantly, can handle images in private blogs
        if (imageUrl.contains("wordpress.com")) {
            return imageUrl + query;
        }

        // must use ssl=1 parameter for https image urls
        boolean useSSl = UrlUtils.isHttps(imageUrl);
        if (useSSl) {
            query += "&ssl=1";
        }

        int beginIndex = schemePos + 3;
        if (beginIndex < 0 || beginIndex > imageUrl.length()) {
            // Fallback to original URL if the beginIndex is invalid to avoid `StringIndexOutOfBoundsException`
            // Ref: https://github.com/wordpress-mobile/WordPress-Android/issues/18626
            return imageUrl;
        }
        return "https://i0.wp.com/" + imageUrl.substring(beginIndex) + query;
    }
}
