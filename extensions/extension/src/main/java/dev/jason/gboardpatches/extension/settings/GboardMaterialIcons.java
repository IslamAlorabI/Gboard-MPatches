package dev.jason.gboardpatches.extension.settings;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/**
 * Draws settings row icons on a 24-unit grid in the outlined Material Symbols style.
 * Extension resources are not merged into the host APK, so icons are drawn in code.
 */
final class GboardMaterialIcons {
    private static final float STROKE_WIDTH = 1.9f;

    private GboardMaterialIcons() {
    }

    static void draw(Canvas canvas, Paint paint, Path path,
            GboardPatchesSettingsContract.RowIcon icon, float left, float top, float size) {
        if (icon == null || icon == GboardPatchesSettingsContract.RowIcon.NONE) {
            return;
        }
        Grid grid = new Grid(canvas, paint, path, left, top, size / 24f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(STROKE_WIDTH * grid.scale);
        switch (icon) {
            case AI_VOICE:
                grid.begin(12f, 3f);
                grid.quadTo(13f, 11f, 21f, 12f);
                grid.quadTo(13f, 13f, 12f, 21f);
                grid.quadTo(11f, 13f, 3f, 12f);
                grid.quadTo(11f, 11f, 12f, 3f);
                grid.close();
                break;
            case KEYBOARD:
                grid.roundRect(3f, 6f, 21f, 18f, 2f);
                for (float x = 7f; x <= 17f; x += 3.33f) {
                    grid.dot(x, 9.5f);
                    grid.dot(x, 12.5f);
                }
                grid.line(8.5f, 15.5f, 15.5f, 15.5f);
                break;
            case KEYS:
                grid.roundRect(4f, 4f, 10.5f, 10.5f, 1.5f);
                grid.roundRect(13.5f, 4f, 20f, 10.5f, 1.5f);
                grid.roundRect(4f, 13.5f, 10.5f, 20f, 1.5f);
                grid.roundRect(13.5f, 13.5f, 20f, 20f, 1.5f);
                break;
            case CLIPBOARD:
                grid.roundRect(5f, 5f, 19f, 21f, 2f);
                grid.roundRect(9f, 3f, 15f, 7f, 1.5f);
                grid.line(8.5f, 12f, 15.5f, 12f);
                grid.line(8.5f, 16f, 13f, 16f);
                break;
            case FILE_TRANSFER:
                grid.line(9f, 19f, 9f, 5f);
                grid.polyline(5f, 9f, 9f, 5f, 13f, 9f);
                grid.line(15f, 5f, 15f, 19f);
                grid.polyline(11f, 15f, 15f, 19f, 19f, 15f);
                break;
            case SETTINGS_STYLE:
                grid.roundRect(3f, 4f, 21f, 20f, 2f);
                grid.line(3f, 9f, 21f, 9f);
                grid.line(10f, 9f, 10f, 20f);
                break;
            case BACKUP:
                grid.arc(4f, 4f, 20f, 20f, 210f, 300f);
                grid.polyline(5.07f, 4f, 5.07f, 8f, 9f, 8f);
                grid.polyline(12f, 8f, 12f, 12f, 15f, 14f);
                break;
            case DEVELOPER:
                grid.polyline(8f, 7f, 3f, 12f, 8f, 17f);
                grid.polyline(16f, 7f, 21f, 12f, 16f, 17f);
                grid.line(14f, 5f, 10f, 19f);
                break;
            case PERSON:
                grid.circle(12f, 8f, 4f);
                grid.begin(4f, 20f);
                grid.quadTo(4f, 14f, 12f, 14f);
                grid.quadTo(20f, 14f, 20f, 20f);
                grid.stroke();
                break;
            case INFO:
                grid.circle(12f, 12f, 9f);
                grid.line(12f, 11f, 12f, 16.5f);
                grid.dot(12f, 7.8f);
                break;
            case EDIT:
                grid.begin(4f, 20f);
                grid.lineTo(4f, 16.5f);
                grid.lineTo(15.5f, 5f);
                grid.quadTo(16.5f, 4f, 17.5f, 5f);
                grid.lineTo(19f, 6.5f);
                grid.quadTo(20f, 7.5f, 19f, 8.5f);
                grid.lineTo(7.5f, 20f);
                grid.close();
                break;
            case LANGUAGE:
                grid.circle(12f, 12f, 9f);
                grid.oval(8f, 3f, 16f, 21f);
                grid.line(3f, 12f, 21f, 12f);
                break;
            default:
                break;
        }
    }

    private static final class Grid {
        private final Canvas canvas;
        private final Paint paint;
        private final Path path;
        private final float left;
        private final float top;
        private final float scale;
        private final RectF rect = new RectF();

        Grid(Canvas canvas, Paint paint, Path path, float left, float top, float scale) {
            this.canvas = canvas;
            this.paint = paint;
            this.path = path;
            this.left = left;
            this.top = top;
            this.scale = scale;
        }

        private float x(float value) {
            return left + value * scale;
        }

        private float y(float value) {
            return top + value * scale;
        }

        void line(float x1, float y1, float x2, float y2) {
            canvas.drawLine(x(x1), y(y1), x(x2), y(y2), paint);
        }

        void polyline(float x1, float y1, float x2, float y2, float x3, float y3) {
            begin(x1, y1);
            lineTo(x2, y2);
            lineTo(x3, y3);
            stroke();
        }

        void dot(float cx, float cy) {
            canvas.drawPoint(x(cx), y(cy), paint);
        }

        void circle(float cx, float cy, float radius) {
            canvas.drawCircle(x(cx), y(cy), radius * scale, paint);
        }

        void oval(float l, float t, float r, float b) {
            rect.set(x(l), y(t), x(r), y(b));
            canvas.drawOval(rect, paint);
        }

        void roundRect(float l, float t, float r, float b, float radius) {
            rect.set(x(l), y(t), x(r), y(b));
            canvas.drawRoundRect(rect, radius * scale, radius * scale, paint);
        }

        void arc(float l, float t, float r, float b, float startAngle, float sweepAngle) {
            rect.set(x(l), y(t), x(r), y(b));
            canvas.drawArc(rect, startAngle, sweepAngle, false, paint);
        }

        void begin(float startX, float startY) {
            path.reset();
            path.moveTo(x(startX), y(startY));
        }

        void lineTo(float toX, float toY) {
            path.lineTo(x(toX), y(toY));
        }

        void quadTo(float controlX, float controlY, float toX, float toY) {
            path.quadTo(x(controlX), y(controlY), x(toX), y(toY));
        }

        void close() {
            path.close();
            stroke();
        }

        void stroke() {
            canvas.drawPath(path, paint);
        }
    }
}
