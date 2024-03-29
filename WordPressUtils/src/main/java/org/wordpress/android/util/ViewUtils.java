package org.wordpress.android.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.core.view.ViewCompat;

import java.util.concurrent.atomic.AtomicInteger;

public class ViewUtils {
    /**
     * Generate a value suitable for use in {@link View#setId(int)}.
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public static int generateViewId() {
        return View.generateViewId();
    }

    private static final AtomicInteger NEXT_GENERATED_ID = new AtomicInteger(1);

    /**
     * Copied from {@link View#generateViewId()}
     * Generate a value suitable for use in {@link View#setId(int)}.
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    private static int copiedGenerateViewId() {
        for (;;) {
            final int result = NEXT_GENERATED_ID.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) {
                newValue = 1; // Roll over to 1, not 0.
            }
            if (NEXT_GENERATED_ID.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    public static void setButtonBackgroundColor(Context context, View button, @StyleRes int styleId,
                                                @AttrRes int colorAttribute) {
        TypedArray a = context.obtainStyledAttributes(styleId, new int[]{colorAttribute});
        ColorStateList color = a.getColorStateList(0);
        a.recycle();
        ViewCompat.setBackgroundTintList(button, color);
    }

    /**
     * adds an inset circular shadow outline the passed view - note that
     * the view should have its elevation set prior to calling this
     */
    public static void addCircularShadowOutline(@NonNull View view) {
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
    }
}
