package dev.jason.gboardpatches.extension.settings;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * Track and thumb drawables that give the platform {@code Switch} the Material 3 look:
 * a 52x32dp track, a 16dp outlined-state thumb and a 24dp checked thumb with a check mark.
 */
final class GboardMaterialSwitchDrawables {
    private static final float TRACK_WIDTH_DP = 52f;
    private static final float TRACK_HEIGHT_DP = 32f;
    // Two thumb boxes fill the track width, so the platform Switch sizes itself to 52dp.
    private static final float THUMB_BOX_WIDTH_DP = 26f;
    private static final float THUMB_CENTER_SHIFT_DP = 3f;
    private static final float DISABLED_ALPHA = 0.38f;

    private GboardMaterialSwitchDrawables() {
    }

    static final class Colors {
        final int checkedTrack;
        final int checkedThumb;
        final int checkedIcon;
        final int uncheckedTrack;
        final int uncheckedOutline;
        final int uncheckedThumb;

        Colors(int checkedTrack, int checkedThumb, int checkedIcon, int uncheckedTrack,
                int uncheckedOutline, int uncheckedThumb) {
            this.checkedTrack = checkedTrack;
            this.checkedThumb = checkedThumb;
            this.checkedIcon = checkedIcon;
            this.uncheckedTrack = uncheckedTrack;
            this.uncheckedOutline = uncheckedOutline;
            this.uncheckedThumb = uncheckedThumb;
        }
    }

    static Drawable track(Colors colors, float density) {
        return new TrackDrawable(colors, density);
    }

    static Drawable thumb(Colors colors, float density) {
        return new ThumbDrawable(colors, density);
    }

    private abstract static class StateDrawable extends Drawable {
        final Colors colors;
        final float density;
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boolean checked;
        boolean enabled = true;
        boolean pressed;

        StateDrawable(Colors colors, float density) {
            this.colors = colors;
            this.density = density;
        }

        @Override
        public boolean isStateful() {
            return true;
        }

        @Override
        protected boolean onStateChange(int[] state) {
            boolean nextChecked = false;
            boolean nextEnabled = false;
            boolean nextPressed = false;
            for (int value : state) {
                if (value == android.R.attr.state_checked) {
                    nextChecked = true;
                } else if (value == android.R.attr.state_enabled) {
                    nextEnabled = true;
                } else if (value == android.R.attr.state_pressed) {
                    nextPressed = true;
                }
            }
            boolean changed = nextChecked != checked
                    || nextEnabled != enabled
                    || nextPressed != pressed;
            checked = nextChecked;
            enabled = nextEnabled;
            pressed = nextPressed;
            if (changed) {
                invalidateSelf();
            }
            return changed;
        }

        int withEnabledAlpha(int color) {
            if (enabled) {
                return color;
            }
            int alpha = Math.round(((color >>> 24) & 0xFF) * DISABLED_ALPHA);
            return (alpha << 24) | (color & 0x00FFFFFF);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    private static final class TrackDrawable extends StateDrawable {
        private final RectF rect = new RectF();

        TrackDrawable(Colors colors, float density) {
            super(colors, density);
        }

        @Override
        public int getIntrinsicWidth() {
            return Math.round(TRACK_WIDTH_DP * density);
        }

        @Override
        public int getIntrinsicHeight() {
            return Math.round(TRACK_HEIGHT_DP * density);
        }

        @Override
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            float outline = 2f * density;
            float radius = bounds.height() / 2f;
            rect.set(bounds);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(withEnabledAlpha(checked ? colors.checkedTrack : colors.uncheckedTrack));
            canvas.drawRoundRect(rect, radius, radius, paint);
            if (!checked) {
                rect.inset(outline / 2f, outline / 2f);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(outline);
                paint.setColor(withEnabledAlpha(colors.uncheckedOutline));
                canvas.drawRoundRect(rect, radius - outline / 2f, radius - outline / 2f, paint);
            }
        }
    }

    private static final class ThumbDrawable extends StateDrawable {
        private final Path checkPath = new Path();

        ThumbDrawable(Colors colors, float density) {
            super(colors, density);
        }

        @Override
        public int getIntrinsicWidth() {
            return Math.round(THUMB_BOX_WIDTH_DP * density);
        }

        @Override
        public int getIntrinsicHeight() {
            return Math.round(TRACK_HEIGHT_DP * density);
        }

        @Override
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            float shift = THUMB_CENTER_SHIFT_DP * density;
            float centerX = bounds.exactCenterX() + (checked ? -shift : shift);
            float centerY = bounds.exactCenterY();
            float radius = (pressed ? 14f : checked ? 12f : 8f) * density;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(withEnabledAlpha(checked ? colors.checkedThumb : colors.uncheckedThumb));
            canvas.drawCircle(centerX, centerY, radius, paint);
            if (!checked) {
                return;
            }
            float unit = density;
            checkPath.reset();
            checkPath.moveTo(centerX - 5f * unit, centerY + 0.2f * unit);
            checkPath.lineTo(centerX - 1.6f * unit, centerY + 3.6f * unit);
            checkPath.lineTo(centerX + 5.2f * unit, centerY - 3.4f * unit);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(2f * unit);
            paint.setColor(withEnabledAlpha(colors.checkedIcon));
            canvas.drawPath(checkPath, paint);
        }
    }
}
