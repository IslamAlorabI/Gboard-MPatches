package dev.jason.gboardpatches.extension.settings;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionSupport;
import android.speech.RecognitionSupportCallback;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.provider.Settings;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.VideoView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import dev.jason.gboardpatches.extension.R;
import dev.jason.gboardpatches.extension.BuildConfig;
import dev.jason.gboardpatches.extension.backuprestore.GboardExportFileNames;
import dev.jason.gboardpatches.extension.settings.keyboardpreview.GboardKeyboardPreviewController;

public final class GboardPatchesSettingsActivity extends Activity
        implements GboardPatchesSettingsContract.Host {
    private static final String TAG = "GboardPatches";
    public static final String EXTRA_NAVIGATION_PATH =
            "dev.jason.gboardpatches.extension.extra.PATCHES_NAVIGATION_PATH";
    private static final String ENTER_PREF_HEADER_EXTRA = "ENTER_PREF_HEADER";
    private static final String GBOARD_SETTINGS_ACTIVITY_CLASS =
            "com.google.android.apps.inputmethod.latin.preference.SettingsActivity";
    private static final String SUPPORTED_DEVELOPER_OPTIONS_TARGET_VERSION =
            "18.0.3.954559732-release-arm64-v8a";
    private static final String GBOARD_PACKAGE_STABLE =
            "com.google.android.inputmethod.latin";
    private static final String GBOARD_PACKAGE_JASON_DEV =
            "com.google.android.inputmethod.latin.jason.dev";
    private static final String GBOARD_PACKAGE_REVERSED_DEV =
            "dev.jason.com.google.android.inputmethod.latin";
    private static final String LIVE_TRANSCRIBE_PACKAGE_NAME =
            "com.google.audio.hearing.visualization.accessibility.scribe";
    private static final String SPEECH_SERVICES_PACKAGE_NAME =
            "com.google.android.tts";
    private static final String SPEECH_SERVICES_RECOGNITION_SERVICE_CLASS_NAME =
            "com.google.android.apps.speech.tts.googletts.service."
                    + "GoogleTTSRecognitionService";
    private static final String SPEECH_SERVICES_PLAY_STORE_URL =
            "https://play.google.com/store/apps/details?id=com.google.android.tts";
    private static final String LIVE_TRANSCRIBE_PLAY_STORE_URL =
            "https://play.google.com/store/apps/details?id=com.google.audio.hearing.visualization.accessibility.scribe";
    private static final String PLAY_STORE_PACKAGE_NAME = "com.android.vending";
    private static final long OFFLINE_SPEECH_LANGUAGE_QUERY_TIMEOUT_MS = 10_000L;
    private static final long RESTART_ICON_ROTATION_DURATION_MS = 700L;
    private static final long RESTART_DELAY_MS = 500L;
    private static final long RESTART_CRASH_RECOVERY_RESTORE_DELAY_MS = 2000L;
    private static final int REQUEST_CREATE_TEXT_DOCUMENT = 0x4742;
    private static final int REQUEST_OPEN_TEXT_DOCUMENT = 0x4743;
    private static final int REQUEST_OPEN_DOCUMENT_TREE = 0x4746;
    private static final int REQUEST_RUNTIME_PERMISSION = 0x4747;
    private static final int REQUEST_CREATE_BINARY_DOCUMENT = 0x4748;
    private static final int REQUEST_OPEN_BINARY_DOCUMENT = 0x4749;
    private static final int TOOLBAR_HEIGHT_DP = 64;
    private static final int TILE_OUTER_RADIUS_DP = 24;
    private static final int TILE_INNER_RADIUS_DP = 4;
    private static final int TILE_GAP_DP = 2;
    private static final long TOOLBAR_TITLE_FADE_DURATION_MS = 150L;
    private static final int NO_SCROLL_POSITION_REQUESTED = -1;
    private static final String TOOLBAR_TITLE_PATCHES = "Patches";
    private static final String ABOUT_AUTHOR_URL = "https://github.com/jasonwu1994";
    private static final String ABOUT_PATCH_REPOSITORY_URL =
            "https://github.com/IslamAlorabI/Gboard-MPatches";
    private static final String ABOUT_REDESIGN_NAME = "Islam AlorabI";
    private static final String ABOUT_REDESIGN_URL = "https://github.com/IslamAlorabI";
    private static final String DOCUMENT_TYPE_FALLBACK = "text/plain";
    private static final String DOCUMENT_PICKER_FAILED = "Unable to open file picker.";
    private static final String DOCUMENT_WRITE_FAILED = "Failed to export file.";
    private static final String DOCUMENT_READ_FAILED = "Failed to import file.";
    private Palette palette;
    private LinearLayout toolbarView;
    private TextView toolbarTitleView;
    private LinearLayout toolbarActionsView;
    private View restartButton;
    private ObjectAnimator restartButtonAnimator;
    private Typeface headlineTypeface;
    private Typeface bodyTypeface;
    private Typeface mediumTypeface;
    private boolean toolbarTitleCollapsed;
    private boolean restartRecommended;
    private boolean currentScreenMentionsRestart;
    private View headerCardView;
    private TextView headerTitleView;
    private TextView headerSummaryView;
    private LinearLayout panelContainer;
    private ScrollView contentScrollView;
    private LinearLayout contentColumn;
    private LinearLayout screenActionContainer;
    private GboardKeyboardPreviewController keyboardPreviewController;
    private List<GboardPatchesSettingsContract.Feature> features;
    private final GboardPatchesSettingsOrchestrator<GboardPatchesSettingsContract.Feature,
            GboardPatchesSettingsContract.Intent>
            settingsOrchestrator =
            new GboardPatchesSettingsOrchestrator<GboardPatchesSettingsContract.Feature,
                    GboardPatchesSettingsContract.Intent>();
    private final GboardPatchesSettingsScrollState scrollState =
            new GboardPatchesSettingsScrollState();
    private boolean featuresInitialized;
    private Object backInvokedCallback;
    private final Handler screenRefreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable screenRefreshRunnable = this::refresh;
    private final Runnable deferredRenderRunnable = this::initializeFeaturesAndRenderSafely;
    private final ExecutorService screenBuildExecutor =
            Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, "GboardPatchesScreenBuild");
                thread.setDaemon(true);
                return thread;
            });
    private final ExecutorService backgroundStateExecutor =
            Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, "GboardPatchesBackgroundState");
                thread.setDaemon(true);
                return thread;
            });
    private final Object screenBuildLock = new Object();
    private int requestedScrollYOnNextScreenApply = 0;
    private boolean initialFeatureFromIntentHandled;
    private PendingTextDocumentWrite pendingTextDocumentWrite;
    private GboardPatchesSettingsContract.StringValueConsumer pendingTextDocumentReader;
    private PendingBinaryDocumentWrite pendingBinaryDocumentWrite;
    private GboardPatchesSettingsContract.BinaryDocumentConsumer pendingBinaryDocumentReader;
    private GboardPatchesSettingsContract.StringValueConsumer pendingDocumentTreeConsumer;
    private GboardPatchesSettingsContract.Feature lifecycleVisibleFeature;
    private boolean featureLifecycleEnabled;
    private GboardPatchesSettingsContract.BooleanValueConsumer pendingPermissionConsumer;
    private volatile GboardPatchesSettingsContract.OfflineSpeechLanguages offlineSpeechLanguages =
            GboardPatchesSettingsContract.OfflineSpeechLanguages.loading();
    private SpeechRecognizer offlineSpeechRecognizer;
    private int offlineSpeechLanguageQueryGeneration;
    private Runnable offlineSpeechLanguageQueryTimeoutRunnable;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(GboardSettingsLocaleManager.wrapContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(resolveActivityTheme());
        super.onCreate(savedInstanceState);
        try {
            scheduleRestartCrashRecoveryGuardRestore();
            palette = Palette.forConfiguration(this, getResources().getConfiguration());
            features = Collections.emptyList();
            featuresInitialized = false;
            configureWindow();
            View contentView = buildContentView();
            setContentView(contentView);
            installWindowInsetsHandling(contentView);
            registerBackCallback();
        } catch (Throwable throwable) {
            showFatalFallbackScreen("Failed to initialize patches settings activity", throwable);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        setRestartButtonPending(false);
        initialFeatureFromIntentHandled = false;
        scheduleDeferredRender();
    }

    @Override
    protected void onResume() {
        super.onResume();
        featureLifecycleEnabled = true;
        applyOrchestration(settingsOrchestrator.accept(
                GboardPatchesSettingsOrchestrator.Event.resume()));
    }

    @Override
    protected void onPause() {
        featureLifecycleEnabled = false;
        applyOrchestration(settingsOrchestrator.accept(
                GboardPatchesSettingsOrchestrator.Event.pause()));
        cancelDeferredRender();
        cancelOfflineSpeechLanguageQuery();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        featureLifecycleEnabled = false;
        applyOrchestration(settingsOrchestrator.accept(
                GboardPatchesSettingsOrchestrator.Event.pause()));
        setRestartButtonPending(false);
        cancelDeferredRender();
        cancelOfflineSpeechLanguageQuery();
        unregisterBackCallback();
        detachKeyboardPreviewSafely();
        screenBuildExecutor.shutdownNow();
        backgroundStateExecutor.shutdownNow();
        super.onDestroy();
    }

    @Override
    @SuppressLint("GestureBackNavigation")
    public void onBackPressed() {
        if (!navigateBack()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void submit(GboardPatchesSettingsContract.Intent intent) {
        applyOrchestration(settingsOrchestrator.accept(
                GboardPatchesSettingsOrchestrator.Event.featureIntent(intent)));
    }

    @Override
    public GboardPatchesSettingsContract.OfflineSpeechLanguages getOfflineSpeechLanguages() {
        return offlineSpeechLanguages;
    }

    @Override
    public void refresh() {
        initializeFeaturesAndRenderSafely();
    }

    public void onManagedDialogShown() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            runOnUiThread(this::onManagedDialogShown);
            return;
        }
        applyOrchestration(settingsOrchestrator.accept(
                GboardPatchesSettingsOrchestrator.Event.modalShown()));
    }

    public void onManagedDialogDismissed() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            runOnUiThread(this::onManagedDialogDismissed);
            return;
        }
        applyOrchestration(settingsOrchestrator.accept(
                GboardPatchesSettingsOrchestrator.Event.modalDismissed()));
    }

    @Override
    public void openFeature(GboardPatchesSettingsContract.Feature feature) {
        if (feature == null) {
            return;
        }
        GboardPatchesSettingsOrchestrator.State<GboardPatchesSettingsContract.Feature> state =
                settingsOrchestrator.snapshot();
        scrollState.enterFeature(state.getCurrent() == null, currentScrollY());
        requestScrollPositionOnNextScreenApply(0);
        applyOrchestration(settingsOrchestrator.accept(
                GboardPatchesSettingsOrchestrator.Event.open(feature)));
    }

    @Override
    public void openTargetSettingsHeader(int headerKeyResourceId) {
        if (headerKeyResourceId == 0) {
            return;
        }
        String currentPackage = getPackageName();
        if (!isSupportedTargetPackage(currentPackage)) {
            showUnsupportedDeveloperOptionsPackageDialog(currentPackage, headerKeyResourceId);
            return;
        }
        if (tryLaunchTargetSettingsHeader(currentPackage, headerKeyResourceId, false)) {
            return;
        }
        throw new ActivityNotFoundException(
                "No supported Gboard Developer options entry is available");
    }

    private boolean tryLaunchTargetSettingsHeader(String packageName, int headerKeyResourceId) {
        return tryLaunchTargetSettingsHeader(packageName, headerKeyResourceId, false);
    }

    private boolean tryLaunchTargetSettingsHeader(String packageName, int headerKeyResourceId,
            boolean allowUnknownVersion) {
        String targetVersionName = targetPackageVersionName(packageName);
        if (!allowUnknownVersion
                && !isSupportedDeveloperOptionsTargetVersion(targetVersionName)) {
            return false;
        }

        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, GBOARD_SETTINGS_ACTIVITY_CLASS));
        intent.putExtra(ENTER_PREF_HEADER_EXTRA, headerKeyResourceId);
        try {
            startActivity(intent);
            return true;
        } catch (ActivityNotFoundException | SecurityException exception) {
            return false;
        }
    }

    private void showUnsupportedDeveloperOptionsPackageDialog(String packageName,
            int headerKeyResourceId) {
        String safePackageName = packageName == null ? "" : packageName;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.gboard_patches_developer_options_package_warning_title)
                .setMessage(GboardSettingsText.format(
                        this,
                        R.string.gboard_patches_developer_options_package_warning_message,
                        safePackageName))
                .setNegativeButton(R.string.gboard_patches_dialog_close, null)
                .setPositiveButton(R.string.gboard_patches_developer_options_try_anyway,
                        (ignored, which) -> tryLaunchUnverifiedDeveloperOptions(
                                safePackageName,
                                headerKeyResourceId))
                .create();
        dialog.setOnDismissListener(ignored -> onManagedDialogDismissed());
        try {
            dialog.show();
            onManagedDialogShown();
            tintDialogButtons(dialog);
        } catch (Throwable throwable) {
            Log.w(TAG, "Unable to show unsupported Gboard package warning", throwable);
            showDeveloperOptionsLaunchFailure(safePackageName);
        }
    }

    private void tryLaunchUnverifiedDeveloperOptions(String packageName,
            int headerKeyResourceId) {
        try {
            if (!tryLaunchTargetSettingsHeader(packageName, headerKeyResourceId, true)) {
                showDeveloperOptionsLaunchFailure(packageName);
            }
        } catch (Throwable throwable) {
            Log.w(TAG, "Unable to open Developer options for " + packageName, throwable);
            showDeveloperOptionsLaunchFailure(packageName);
        }
    }

    private void showDeveloperOptionsLaunchFailure(String packageName) {
        Toast.makeText(
                this,
                GboardSettingsText.format(
                        this,
                        R.string.gboard_patches_developer_options_launch_failed,
                        packageName),
                Toast.LENGTH_LONG)
                .show();
    }

    private String targetPackageVersionName(String packageName) {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException | SecurityException exception) {
            return null;
        }
    }

    private List<String> targetSettingsPackages() {
        Set<String> packageNames = new LinkedHashSet<String>();
        String currentPackage = getPackageName();
        if (currentPackage != null && !currentPackage.isEmpty()) {
            packageNames.add(currentPackage);
        }
        packageNames.add(GBOARD_PACKAGE_STABLE);
        packageNames.add(GBOARD_PACKAGE_JASON_DEV);
        packageNames.add(GBOARD_PACKAGE_REVERSED_DEV);
        return new ArrayList<String>(packageNames);
    }

    static boolean isSupportedDeveloperOptionsTargetVersion(String versionName) {
        return SUPPORTED_DEVELOPER_OPTIONS_TARGET_VERSION.equals(versionName);
    }

    private static boolean isSupportedTargetPackage(String packageName) {
        return GBOARD_PACKAGE_STABLE.equals(packageName)
                || GBOARD_PACKAGE_JASON_DEV.equals(packageName)
                || GBOARD_PACKAGE_REVERSED_DEV.equals(packageName);
    }

    @Override
    public void showChoiceDialog(String title, String[] labels, String[] values,
            String currentValue, String customValue, Runnable customAction,
            GboardPatchesSettingsContract.StringValueConsumer valueConsumer) {
        int checkedIndex = resolveCheckedIndex(values, currentValue);
        final Runnable[] pendingSelectionAction = new Runnable[1];
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setSingleChoiceItems(labels, checkedIndex, (dialogInterface, which) -> {
                    if (which < 0 || which >= values.length) {
                        return;
                    }
                    String selectedValue = values[which];
                    pendingSelectionAction[0] = () -> runSafely(
                            "handle choice dialog selection",
                            () -> {
                                if (customValue != null
                                        && customAction != null
                                        && customValue.equals(selectedValue)) {
                                    customAction.run();
                                } else {
                                    valueConsumer.accept(selectedValue);
                                    markSettingsChanged();
                                    renderCurrentScreenSafely();
                                }
                            });
                    dialogInterface.dismiss();
                })
                .setNegativeButton(text(R.string.gboard_patches_dialog_cancel), null)
                .create();
        dialog.setOnDismissListener(ignored -> {
            Runnable action = pendingSelectionAction[0];
            pendingSelectionAction[0] = null;
            onManagedDialogDismissed();
            if (action == null) {
                return;
            }
            postToDecorView(action);
        });
        try {
            dialog.show();
            onManagedDialogShown();
            tintDialogButtons(dialog);
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to show choice dialog", throwable);
        }
    }

    @Override
    public void showMultiChoiceDialog(String title, String[] labels, String[] values,
            boolean[] initiallySelected,
            String positiveLabel,
            GboardPatchesSettingsContract.StringListConsumer valueConsumer) {
        if (labels == null || values == null || initiallySelected == null
                || labels.length != values.length
                || labels.length != initiallySelected.length
                || valueConsumer == null) {
            return;
        }
        boolean[] selected = initiallySelected.clone();
        final Runnable[] pendingAction = new Runnable[1];
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMultiChoiceItems(labels, selected,
                        (ignored, which, checked) -> {
                            if (which >= 0 && which < selected.length) {
                                selected[which] = checked;
                            }
                        })
                .setPositiveButton(positiveLabel, null)
                .setNegativeButton(text(R.string.gboard_patches_dialog_cancel), null);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(ignored -> {
            tintDialogButtons(dialog);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                List<String> chosen = new ArrayList<String>();
                for (int index = 0; index < values.length; index++) {
                    if (selected[index]) {
                        chosen.add(values[index]);
                    }
                }
                if (chosen.isEmpty()) {
                    return;
                }
                pendingAction[0] = () -> runSafely(
                        "handle multi-choice dialog selection",
                        () -> {
                            valueConsumer.accept(chosen);
                            markSettingsChanged();
                        });
                dialog.dismiss();
            });
        });
        dialog.setOnDismissListener(ignored -> {
            Runnable action = pendingAction[0];
            pendingAction[0] = null;
            onManagedDialogDismissed();
            if (action != null) {
                postToDecorView(action);
            }
        });
        try {
            dialog.show();
            onManagedDialogShown();
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to show multi-choice dialog", throwable);
        }
    }

    @Override
    public void showPositiveIntegerDialog(String title, String hint, int initialValue,
            GboardPatchesSettingsContract.PositiveIntegerConsumer consumer) {
        EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint(hint);
        input.setText(Integer.toString(initialValue));
        input.setSelectAllOnFocus(true);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(8), dp(24), 0);
        container.addView(input, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(container)
                .setPositiveButton(text(R.string.gboard_patches_dialog_save), null)
                .setNegativeButton(
                        text(R.string.gboard_patches_dialog_cancel),
                        null)
                .create();
        dialog.setOnShowListener(ignored -> {
            tintDialogButtons(dialog);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                int value = parsePositiveInteger(input.getText().toString());
                if (value <= 0) {
                    input.setError(text(R.string.gboard_patches_dialog_error_positive));
                    return;
                }
                try {
                    consumer.accept(value);
                    markSettingsChanged();
                    dialog.dismiss();
                    renderCurrentScreenSafely();
                } catch (IllegalArgumentException exception) {
                    input.setError(exception.getMessage());
                } catch (Throwable throwable) {
                    Log.w(TAG, "Failed to persist positive integer setting", throwable);
                    input.setError(text(R.string.gboard_patches_dialog_error_save_failed));
                }
            });
        });
        dialog.setOnDismissListener(ignored -> onManagedDialogDismissed());
        try {
            dialog.show();
            onManagedDialogShown();
            input.requestFocus();
            input.setSelection(input.getText().length());
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to show positive integer dialog", throwable);
        }
    }

    @Override
    public void showTextInputDialog(String title, String hint, String initialValue,
            GboardPatchesSettingsContract.TextValueConsumer consumer) {
        EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setHint(hint);
        input.setText(initialValue == null ? "" : initialValue);
        input.setSelectAllOnFocus(true);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(8), dp(24), 0);
        container.addView(input, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(container)
                .setPositiveButton(text(R.string.gboard_patches_dialog_save), null)
                .setNegativeButton(
                        text(R.string.gboard_patches_dialog_cancel),
                        null)
                .create();
        dialog.setOnShowListener(ignored -> {
            tintDialogButtons(dialog);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                try {
                    consumer.accept(input.getText() == null ? "" : input.getText().toString());
                    markSettingsChanged();
                    dialog.dismiss();
                    renderCurrentScreenSafely();
                } catch (IllegalArgumentException exception) {
                    input.setError(exception.getMessage());
                } catch (Throwable throwable) {
                    Log.w(TAG, "Failed to persist text setting", throwable);
                    input.setError(text(R.string.gboard_patches_dialog_error_save_failed));
                }
            });
        });
        dialog.setOnDismissListener(ignored -> onManagedDialogDismissed());
        try {
            dialog.show();
            onManagedDialogShown();
            input.requestFocus();
            input.setSelection(input.getText().length());
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to show text input dialog", throwable);
        }
    }

    @Override
    public void showPreviewDialog(GboardPatchesSettingsContract.PreviewSpec previewSpec) {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(8), dp(24), 0);
        container.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        String message = previewSpec.getMessage();
        if (message != null && !message.isEmpty()) {
            TextView messageView = new TextView(this);
            messageView.setText(message);
            messageView.setTextColor(palette.textSecondary);
            messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
            messageView.setLineSpacing(0f, 1.1f);
            container.addView(messageView);
        }

        List<GboardPatchesSettingsContract.PreviewMedia> previewMedia =
                previewSpec.getMediaItems();
        List<VideoView> previewVideoViews = new ArrayList<VideoView>();
        boolean sideBySide = previewSpec.getLayout()
                == GboardPatchesSettingsContract.PreviewLayout.SIDE_BY_SIDE;
        if (!sideBySide || previewMedia.size() <= 1) {
            boolean firstImage = true;
            for (GboardPatchesSettingsContract.PreviewMedia previewItem : previewMedia) {
                View imageCard = buildPreviewMediaCard(
                        previewSpec.getTitle(),
                        previewItem,
                        false,
                        previewVideoViews);
                LinearLayout.LayoutParams imageCardParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                imageCardParams.topMargin = firstImage ? dp(16) : dp(12);
                imageCard.setLayoutParams(imageCardParams);
                container.addView(imageCard);
                firstImage = false;
            }
        } else {
            for (int index = 0; index < previewMedia.size(); index += 2) {
                LinearLayout previewRow = new LinearLayout(this);
                previewRow.setOrientation(LinearLayout.HORIZONTAL);
                previewRow.setBaselineAligned(false);
                LinearLayout.LayoutParams previewRowParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                previewRowParams.topMargin = index == 0 ? dp(16) : dp(12);
                previewRow.setLayoutParams(previewRowParams);

                View leftCard = buildPreviewMediaCard(
                        previewSpec.getTitle(),
                        previewMedia.get(index),
                        true,
                        previewVideoViews);
                LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f);
                leftParams.rightMargin = dp(6);
                previewRow.addView(leftCard, leftParams);

                if (index + 1 < previewMedia.size()) {
                    View rightCard = buildPreviewMediaCard(
                            previewSpec.getTitle(),
                            previewMedia.get(index + 1),
                            true,
                            previewVideoViews);
                    LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            1f);
                    rightParams.leftMargin = dp(6);
                    previewRow.addView(rightCard, rightParams);
                } else {
                    View spacer = new View(this);
                    previewRow.addView(spacer, new LinearLayout.LayoutParams(
                            0,
                            0,
                            1f));
                }

                container.addView(previewRow);
            }
        }
        scrollView.addView(container);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(previewSpec.getTitle())
                .setView(scrollView)
                .setPositiveButton(text(R.string.gboard_patches_dialog_close), null)
                .create();
        dialog.setOnDismissListener(ignored -> {
            for (VideoView previewVideoView : previewVideoViews) {
                try {
                    previewVideoView.stopPlayback();
                } catch (Throwable throwable) {
                    Log.w(TAG, "Failed to stop preview video playback", throwable);
                }
            }
            onManagedDialogDismissed();
        });
        try {
            dialog.show();
            onManagedDialogShown();
            tintDialogButtons(dialog);
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to show preview dialog", throwable);
        }
    }

    @Override
    public void showManagedDialog(GboardPatchesSettingsContract.ManagedDialogAction action) {
        if (action == null) {
            return;
        }
        if (Looper.myLooper() != Looper.getMainLooper()) {
            runOnUiThread(() -> showManagedDialog(action));
            return;
        }
        Throwable throwable = GboardManagedDialogRunner.run(
                action,
                this::onManagedDialogShown,
                this::onManagedDialogDismissed);
        if (throwable != null) {
            Log.w(TAG, "Failed to show managed settings dialog", throwable);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void createTextDocument(String fileName, String mimeType, String text,
            Runnable completionAction) {
        pendingTextDocumentWrite = new PendingTextDocumentWrite(
                text != null ? text : "",
                completionAction);
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setType(normalizeMimeType(mimeType));
        if (fileName != null && !fileName.trim().isEmpty()) {
            intent.putExtra(Intent.EXTRA_TITLE,
                    GboardExportFileNames.timestamped(fileName.trim()));
        }
        try {
            startActivityForResult(intent, REQUEST_CREATE_TEXT_DOCUMENT);
        } catch (ActivityNotFoundException ignored) {
            pendingTextDocumentWrite = null;
            Toast.makeText(this, DOCUMENT_PICKER_FAILED, Toast.LENGTH_SHORT).show();
        } catch (Throwable throwable) {
            pendingTextDocumentWrite = null;
            Log.w(TAG, "Failed to launch document create picker", throwable);
            Toast.makeText(this, DOCUMENT_PICKER_FAILED, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void openTextDocument(String[] mimeTypes,
            GboardPatchesSettingsContract.StringValueConsumer valueConsumer) {
        if (valueConsumer == null) {
            return;
        }
        pendingTextDocumentReader = valueConsumer;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        String[] normalizedMimeTypes = normalizeMimeTypes(mimeTypes);
        intent.setType(normalizedMimeTypes.length == 1
                ? normalizedMimeTypes[0]
                : "*/*");
        if (normalizedMimeTypes.length > 1) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, normalizedMimeTypes);
        }
        try {
            startActivityForResult(intent, REQUEST_OPEN_TEXT_DOCUMENT);
        } catch (ActivityNotFoundException ignored) {
            pendingTextDocumentReader = null;
            Toast.makeText(this, DOCUMENT_PICKER_FAILED, Toast.LENGTH_SHORT).show();
        } catch (Throwable throwable) {
            pendingTextDocumentReader = null;
            Log.w(TAG, "Failed to launch document open picker", throwable);
            Toast.makeText(this, DOCUMENT_PICKER_FAILED, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void createBinaryDocument(String fileName, String mimeType, byte[] data,
            Runnable completionAction) {
        pendingBinaryDocumentWrite = new PendingBinaryDocumentWrite(
                data == null ? new byte[0] : data.clone(), completionAction);
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                .setType(normalizeMimeType(mimeType));
        if (fileName != null && !fileName.isBlank()) {
            intent.putExtra(Intent.EXTRA_TITLE,
                    GboardExportFileNames.timestamped(fileName.trim()));
        }
        try {
            startActivityForResult(intent, REQUEST_CREATE_BINARY_DOCUMENT);
        } catch (Throwable throwable) {
            pendingBinaryDocumentWrite = null;
            Log.w(TAG, "Failed to launch binary document create picker", throwable);
            Toast.makeText(this, DOCUMENT_PICKER_FAILED, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void openBinaryDocument(String[] mimeTypes,
            GboardPatchesSettingsContract.BinaryDocumentConsumer documentConsumer) {
        if (documentConsumer == null) {
            return;
        }
        pendingBinaryDocumentReader = documentConsumer;
        String[] normalizedMimeTypes = normalizeMimeTypes(mimeTypes);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                .setType(normalizedMimeTypes.length == 1 ? normalizedMimeTypes[0] : "*/*");
        if (normalizedMimeTypes.length > 1) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, normalizedMimeTypes);
        }
        try {
            startActivityForResult(intent, REQUEST_OPEN_BINARY_DOCUMENT);
        } catch (Throwable throwable) {
            pendingBinaryDocumentReader = null;
            Log.w(TAG, "Failed to launch binary document open picker", throwable);
            Toast.makeText(this, DOCUMENT_PICKER_FAILED, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void requestTargetRestart() {
        requestGboardRestart();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void openDocumentTree(String initialTreeUri,
            GboardPatchesSettingsContract.StringValueConsumer valueConsumer) {
        if (valueConsumer == null) {
            return;
        }
        pendingDocumentTreeConsumer = valueConsumer;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && initialTreeUri != null && initialTreeUri.startsWith("content://")) {
            try {
                intent.putExtra("android.provider.extra.INITIAL_URI", Uri.parse(initialTreeUri));
            } catch (Throwable ignored) {
                // The picker can still open without an initial location.
            }
        }
        try {
            startActivityForResult(intent, REQUEST_OPEN_DOCUMENT_TREE);
        } catch (ActivityNotFoundException ignored) {
            pendingDocumentTreeConsumer = null;
            Toast.makeText(this, DOCUMENT_PICKER_FAILED, Toast.LENGTH_SHORT).show();
        } catch (Throwable throwable) {
            pendingDocumentTreeConsumer = null;
            Log.w(TAG, "Failed to launch document tree picker", throwable);
            Toast.makeText(this, DOCUMENT_PICKER_FAILED, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void requestRuntimePermission(String permission,
            GboardPatchesSettingsContract.BooleanValueConsumer resultConsumer) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            runOnUiThread(() -> requestRuntimePermission(permission, resultConsumer));
            return;
        }
        if (permission == null || permission.isBlank() || resultConsumer == null) {
            return;
        }
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            resultConsumer.accept(true);
            return;
        }
        if (pendingPermissionConsumer != null) {
            resultConsumer.accept(false);
            return;
        }
        pendingPermissionConsumer = resultConsumer;
        try {
            requestPermissions(new String[]{permission}, REQUEST_RUNTIME_PERMISSION);
        } catch (Throwable throwable) {
            pendingPermissionConsumer = null;
            Log.w(TAG, "Failed to request runtime permission " + permission, throwable);
            resultConsumer.accept(false);
        }
    }

    @Override
    public void openAllFilesAccessSettings(String unavailableMessage) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            runOnUiThread(() -> openAllFilesAccessSettings(unavailableMessage));
            return;
        }
        Intent appIntent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                .setData(Uri.parse("package:" + getPackageName()));
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ignored) {
            try {
                startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
            } catch (Throwable throwable) {
                Log.w(TAG, "Failed to launch all files access settings", throwable);
                Toast.makeText(this, unavailableMessage == null ? "" : unavailableMessage,
                        Toast.LENGTH_LONG).show();
            }
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to launch app all files access settings", throwable);
            Toast.makeText(this, unavailableMessage == null ? "" : unavailableMessage,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void openBatteryOptimizationSettings(String unavailableMessage) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            runOnUiThread(() -> openBatteryOptimizationSettings(unavailableMessage));
            return;
        }
        Intent appIntent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                .setData(Uri.parse("package:" + getPackageName()));
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ignored) {
            openBatteryOptimizationSettingsFallback(unavailableMessage);
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to launch app battery optimization settings", throwable);
            openBatteryOptimizationSettingsFallback(unavailableMessage);
        }
    }

    private void openBatteryOptimizationSettingsFallback(String unavailableMessage) {
        try {
            startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to launch battery optimization settings", throwable);
            Toast.makeText(this, unavailableMessage == null ? "" : unavailableMessage,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void showMessage(String message) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            runOnUiThread(() -> showMessage(message));
            return;
        }
        Toast.makeText(this, message == null ? "" : message, Toast.LENGTH_LONG).show();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CREATE_TEXT_DOCUMENT) {
            handleCreateTextDocumentResult(resultCode, data);
            return;
        }
        if (requestCode == REQUEST_OPEN_TEXT_DOCUMENT) {
            handleOpenTextDocumentResult(resultCode, data);
            return;
        }
        if (requestCode == REQUEST_OPEN_DOCUMENT_TREE) {
            handleOpenDocumentTreeResult(resultCode, data);
            return;
        }
        if (requestCode == REQUEST_CREATE_BINARY_DOCUMENT) {
            handleCreateBinaryDocumentResult(resultCode, data);
            return;
        }
        if (requestCode == REQUEST_OPEN_BINARY_DOCUMENT) {
            handleOpenBinaryDocumentResult(resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
            int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_RUNTIME_PERMISSION) {
            return;
        }
        GboardPatchesSettingsContract.BooleanValueConsumer consumer = pendingPermissionConsumer;
        pendingPermissionConsumer = null;
        if (consumer != null) {
            consumer.accept(grantResults != null
                    && grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED);
        }
    }

    private View buildPreviewMediaCard(String dialogTitle,
            GboardPatchesSettingsContract.PreviewMedia previewMedia, boolean compact,
            List<VideoView> previewVideoViews) {
        LinearLayout imageCard = new LinearLayout(this);
        imageCard.setOrientation(LinearLayout.VERTICAL);
        imageCard.setGravity(Gravity.CENTER_HORIZONTAL);
        imageCard.setBackground(buildCardDrawable(
                palette.surfaceContainerHigh,
                Color.TRANSPARENT,
                dp(24)));
        imageCard.setPadding(
                compact ? dp(10) : dp(16),
                compact ? dp(12) : dp(16),
                compact ? dp(10) : dp(16),
                compact ? dp(12) : dp(16));

        View previewContent = null;
        if (previewMedia instanceof GboardPatchesSettingsContract.PreviewImage previewImage) {
            previewContent = buildPreviewImageView(dialogTitle, previewImage, compact);
        } else if (previewMedia instanceof GboardPatchesSettingsContract.PreviewVideo previewVideo) {
            previewContent = buildPreviewVideoView(
                    dialogTitle,
                    previewVideo,
                    compact,
                    previewVideoViews);
        }

        if (previewContent != null) {
            ViewGroup.LayoutParams previewLayoutParams = previewContent.getLayoutParams();
            if (previewLayoutParams != null) {
                imageCard.addView(previewContent, previewLayoutParams);
            } else {
                imageCard.addView(previewContent, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        } else {
            TextView errorView = new TextView(this);
            errorView.setText(text(R.string.gboard_patches_preview_load_failed));
            errorView.setTextColor(palette.textSecondary);
            errorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
            imageCard.addView(errorView);
        }

        String caption = previewMedia.getCaption();
        if (caption != null && !caption.isEmpty()) {
            TextView captionView = new TextView(this);
            captionView.setText(caption);
            captionView.setTextColor(palette.textPrimary);
            captionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, compact ? 12f : 14f);
            captionView.setTypeface(mediumTypeface());
            LinearLayout.LayoutParams captionParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            captionParams.topMargin = compact ? dp(8) : dp(12);
            captionView.setLayoutParams(captionParams);
            captionView.setGravity(Gravity.CENTER_HORIZONTAL);
            imageCard.addView(captionView);
        }

        return imageCard;
    }

    private View buildPreviewImageView(String dialogTitle,
            GboardPatchesSettingsContract.PreviewImage previewImage, boolean compact) {
        Bitmap bitmap = decodePreviewBitmap(previewImage.getAssetPath());
        if (bitmap == null) {
            return null;
        }
        ImageView imageView = new ImageView(this);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setMaxHeight(compact ? dp(220) : dp(420));
        imageView.setImageBitmap(bitmap);
        imageView.setContentDescription(dialogTitle);
        return imageView;
    }

    private View buildPreviewVideoView(String dialogTitle,
            GboardPatchesSettingsContract.PreviewVideo previewVideo, boolean compact,
            List<VideoView> previewVideoViews) {
        Uri videoUri = resolvePreviewVideoUri(previewVideo);
        if (videoUri == null) {
            return null;
        }
        FrameLayout videoContainer = new FrameLayout(this);
        videoContainer.setForegroundGravity(Gravity.CENTER);
        videoContainer.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                compact ? dp(220) : dp(420)));

        VideoView videoView = new VideoView(this);
        videoView.setVideoURI(videoUri);
        videoView.setContentDescription(dialogTitle);
        videoView.setOnPreparedListener(mediaPlayer ->
                preparePreviewVideo(mediaPlayer, videoView, videoContainer, compact));
        videoView.setOnErrorListener((mediaPlayer, what, extra) -> {
            Log.w(TAG, "Failed to prepare preview video asset: " + previewVideo.getAssetPath()
                    + ", what=" + what + ", extra=" + extra);
            videoContainer.post(() -> showPreviewVideoLoadError(videoContainer));
            return true;
        });
        videoContainer.addView(videoView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        previewVideoViews.add(videoView);
        return videoContainer;
    }

    private void preparePreviewVideo(MediaPlayer mediaPlayer, VideoView videoView,
            FrameLayout videoContainer, boolean compact) {
        try {
            mediaPlayer.setVolume(0f, 0f);
            mediaPlayer.setLooping(true);
            updatePreviewVideoContainerHeight(
                    videoContainer,
                    mediaPlayer.getVideoWidth(),
                    mediaPlayer.getVideoHeight(),
                    compact);
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to configure preview video playback", throwable);
        }
        try {
            videoView.start();
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to start preview video playback", throwable);
        }
    }

    private void updatePreviewVideoContainerHeight(FrameLayout videoContainer, int videoWidth,
            int videoHeight, boolean compact) {
        if (videoContainer == null || videoWidth <= 0 || videoHeight <= 0) {
            return;
        }
        int maxHeight = compact ? dp(220) : dp(420);
        int minHeight = compact ? dp(96) : dp(140);
        videoContainer.post(() -> {
            int containerWidth = videoContainer.getWidth();
            if (containerWidth <= 0) {
                return;
            }
            int targetHeight = Math.round(
                    (float) containerWidth * ((float) videoHeight / (float) videoWidth));
            targetHeight = Math.max(minHeight, Math.min(maxHeight, targetHeight));
            ViewGroup.LayoutParams layoutParams = videoContainer.getLayoutParams();
            if (layoutParams == null || layoutParams.height == targetHeight) {
                return;
            }
            layoutParams.height = targetHeight;
            videoContainer.setLayoutParams(layoutParams);
        });
    }

    private Bitmap decodePreviewBitmap(String assetPath) {
        try (InputStream inputStream = getAssets().open(assetPath)) {
            return BitmapFactory.decodeStream(inputStream);
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to load preview asset: " + assetPath, throwable);
            return null;
        }
    }

    private Uri resolvePreviewVideoUri(GboardPatchesSettingsContract.PreviewVideo previewVideo) {
        if (previewVideo == null) {
            return null;
        }
        return resolvePreviewVideoAssetUri(previewVideo.getAssetPath());
    }

    private Uri resolvePreviewVideoAssetUri(String assetPath) {
        if (assetPath == null || assetPath.isEmpty()) {
            return null;
        }
        File cacheFile = materializePreviewVideoAsset(assetPath);
        if (cacheFile == null) {
            return null;
        }
        return Uri.fromFile(cacheFile);
    }

    private File materializePreviewVideoAsset(String assetPath) {
        try (InputStream inputStream = getAssets().open(assetPath)) {
            File cacheDirectory = new File(getCacheDir(), "preview-videos");
            if (!cacheDirectory.exists() && !cacheDirectory.mkdirs()) {
                Log.w(TAG, "Failed to create preview video cache directory: "
                        + cacheDirectory.getAbsolutePath());
                return null;
            }
            File outputFile = new File(cacheDirectory, sanitizePreviewAssetFileName(assetPath));
            try (FileOutputStream outputStream = new FileOutputStream(outputFile, false)) {
                byte[] buffer = new byte[8192];
                while (true) {
                    int read = inputStream.read(buffer);
                    if (read < 0) {
                        break;
                    }
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
            }
            return outputFile;
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to materialize preview video asset: " + assetPath, throwable);
            return null;
        }
    }

    private static String sanitizePreviewAssetFileName(String assetPath) {
        String normalized = assetPath.replace('\\', '/');
        int lastSlash = normalized.lastIndexOf('/');
        String fileName = lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
        if (fileName.isEmpty()) {
            return "preview-video.mp4";
        }
        return fileName;
    }

    private void showPreviewVideoLoadError(FrameLayout videoContainer) {
        if (videoContainer == null) {
            return;
        }
        videoContainer.removeAllViews();

        TextView errorView = new TextView(this);
        errorView.setText(text(R.string.gboard_patches_preview_load_failed));
        errorView.setTextColor(palette.textSecondary);
        errorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        errorView.setGravity(Gravity.CENTER);

        videoContainer.addView(errorView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER));
    }

    private void handleCreateTextDocumentResult(int resultCode, Intent data) {
        PendingTextDocumentWrite pendingWrite = pendingTextDocumentWrite;
        pendingTextDocumentWrite = null;
        if (resultCode != RESULT_OK || data == null || data.getData() == null
                || pendingWrite == null) {
            return;
        }
        try {
            writeTextDocument(data.getData(), pendingWrite.text);
            if (pendingWrite.completionAction != null) {
                pendingWrite.completionAction.run();
            }
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to write selected document", throwable);
            Toast.makeText(this, DOCUMENT_WRITE_FAILED, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleOpenTextDocumentResult(int resultCode, Intent data) {
        GboardPatchesSettingsContract.StringValueConsumer reader = pendingTextDocumentReader;
        pendingTextDocumentReader = null;
        Uri uri = selectedDocumentUri(data);
        if (resultCode != RESULT_OK || uri == null || reader == null) {
            return;
        }
        try {
            reader.accept(readTextDocument(uri));
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to read selected document", throwable);
            Toast.makeText(this, DOCUMENT_READ_FAILED, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCreateBinaryDocumentResult(int resultCode, Intent data) {
        PendingBinaryDocumentWrite pendingWrite = pendingBinaryDocumentWrite;
        pendingBinaryDocumentWrite = null;
        if (resultCode != RESULT_OK || data == null || data.getData() == null
                || pendingWrite == null) {
            return;
        }
        try (OutputStream output = getContentResolver().openOutputStream(data.getData(), "wt")) {
            if (output == null) {
                throw new java.io.IOException("Content resolver returned null output stream");
            }
            output.write(pendingWrite.data);
            output.flush();
            if (pendingWrite.completionAction != null) {
                pendingWrite.completionAction.run();
            }
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to write selected binary document", throwable);
            Toast.makeText(this, DOCUMENT_WRITE_FAILED, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleOpenBinaryDocumentResult(int resultCode, Intent data) {
        GboardPatchesSettingsContract.BinaryDocumentConsumer reader =
                pendingBinaryDocumentReader;
        pendingBinaryDocumentReader = null;
        Uri uri = selectedDocumentUri(data);
        if (resultCode != RESULT_OK || uri == null || reader == null) {
            return;
        }
        try {
            reader.accept(new GboardPatchesSettingsContract.BinaryDocument(
                    queryDisplayName(uri), getContentResolver().getType(uri),
                    readBinaryDocument(uri)));
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to read selected binary document", throwable);
            Toast.makeText(this, DOCUMENT_READ_FAILED, Toast.LENGTH_SHORT).show();
        }
    }

    private static Uri selectedDocumentUri(Intent data) {
        if (data == null) {
            return null;
        }
        Uri direct = data.getData();
        if (direct != null) {
            return direct;
        }
        ClipData clipData = data.getClipData();
        if (clipData == null || clipData.getItemCount() == 0) {
            return null;
        }
        ClipData.Item first = clipData.getItemAt(0);
        return first == null ? null : first.getUri();
    }

    private String queryDisplayName(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri,
                new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    return cursor.getString(index);
                }
            }
        } catch (Throwable ignored) {
            // Fall back to the URI path below.
        }
        return uri.getLastPathSegment();
    }

    private byte[] readBinaryDocument(Uri uri) throws java.io.IOException {
        try (InputStream input = getContentResolver().openInputStream(uri)) {
            if (input == null) {
                throw new java.io.IOException("Content resolver returned null input stream");
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
    }

    private void handleOpenDocumentTreeResult(int resultCode, Intent data) {
        GboardPatchesSettingsContract.StringValueConsumer consumer = pendingDocumentTreeConsumer;
        pendingDocumentTreeConsumer = null;
        if (resultCode != RESULT_OK || data == null || data.getData() == null
                || consumer == null) {
            return;
        }
        Uri treeUri = data.getData();
        int grantFlags = data.getFlags()
                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if ((grantFlags & Intent.FLAG_GRANT_READ_URI_PERMISSION) == 0) {
            return;
        }
        try {
            getContentResolver().takePersistableUriPermission(treeUri, grantFlags);
            consumer.accept(treeUri.toString());
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to persist selected document tree", throwable);
            Toast.makeText(this, DOCUMENT_PICKER_FAILED, Toast.LENGTH_SHORT).show();
        }
    }

    private void writeTextDocument(Uri uri, String text) throws java.io.IOException {
        try (OutputStream outputStream = getContentResolver().openOutputStream(uri, "wt")) {
            if (outputStream == null) {
                throw new java.io.IOException("Content resolver returned null output stream.");
            }
            outputStream.write((text != null ? text : "").getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }
    }

    private String readTextDocument(Uri uri) throws java.io.IOException {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                throw new java.io.IOException("Content resolver returned null input stream.");
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) >= 0) {
                outputStream.write(buffer, 0, read);
            }
            return outputStream.toString(StandardCharsets.UTF_8.name());
        }
    }

    private static String normalizeMimeType(String mimeType) {
        return mimeType != null && !mimeType.trim().isEmpty()
                ? mimeType.trim()
                : DOCUMENT_TYPE_FALLBACK;
    }

    private static String[] normalizeMimeTypes(String[] mimeTypes) {
        if (mimeTypes == null || mimeTypes.length == 0) {
            return new String[] { DOCUMENT_TYPE_FALLBACK };
        }
        List<String> normalized = new ArrayList<String>();
        for (String mimeType : mimeTypes) {
            if (mimeType != null && !mimeType.trim().isEmpty()) {
                normalized.add(mimeType.trim());
            }
        }
        if (normalized.isEmpty()) {
            normalized.add(DOCUMENT_TYPE_FALLBACK);
        }
        return normalized.toArray(new String[0]);
    }

    private void configureWindow() {
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(palette.windowBackground);
            window.setNavigationBarColor(palette.windowBackground);
        }
    }

    private int resolveActivityTheme() {
        boolean nightMode = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        return nightMode
                ? android.R.style.Theme_DeviceDefault_NoActionBar
                : android.R.style.Theme_DeviceDefault_Light_NoActionBar;
    }

    private View buildContentView() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(palette.windowBackground);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(shell);

        shell.addView(buildToolbar());

        contentScrollView = new ScrollView(this);
        contentScrollView.setClipToPadding(false);
        contentScrollView.setFillViewport(true);
        contentScrollView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        contentScrollView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f));

        contentColumn = new LinearLayout(this);
        contentColumn.setOrientation(LinearLayout.VERTICAL);
        contentColumn.setPadding(dp(16), 0, dp(16), 0);
        contentColumn.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        headerCardView = buildHeaderCard();
        contentColumn.addView(headerCardView);
        contentColumn.addView(buildPanelCard());

        contentScrollView.addView(contentColumn);
        installCollapsingTitle();
        shell.addView(contentScrollView);

        screenActionContainer = new LinearLayout(this);
        screenActionContainer.setOrientation(LinearLayout.VERTICAL);
        screenActionContainer.setPadding(dp(16), dp(8), dp(16), dp(16));
        screenActionContainer.setVisibility(View.GONE);
        shell.addView(screenActionContainer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        keyboardPreviewController = new GboardKeyboardPreviewController(
                this,
                this::targetSettingsPackages,
                this::onManagedDialogShown,
                this::onManagedDialogDismissed,
                palette.accent,
                palette.windowBackground);
        keyboardPreviewController.attachTo(root);
        return root;
    }

    private View buildToolbar() {
        toolbarView = new LinearLayout(this);
        toolbarView.setOrientation(LinearLayout.HORIZONTAL);
        toolbarView.setGravity(Gravity.CENTER_VERTICAL);
        toolbarView.setBackgroundColor(palette.windowBackground);
        toolbarView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(TOOLBAR_HEIGHT_DP)));
        toolbarView.setPadding(dp(8), 0, dp(4), 0);

        View backButton = buildBackButton();

        toolbarTitleView = new TextView(this);
        toolbarTitleView.setTextColor(palette.textPrimary);
        toolbarTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f);
        toolbarTitleView.setTypeface(headlineTypeface());
        toolbarTitleView.setSingleLine(true);
        toolbarTitleView.setEllipsize(TextUtils.TruncateAt.END);
        toolbarTitleView.setPadding(dp(8), 0, dp(8), 0);
        toolbarTitleView.setAlpha(0f);
        toolbarTitleCollapsed = false;
        toolbarTitleView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f));

        toolbarView.addView(backButton);
        toolbarView.addView(toolbarTitleView);
        toolbarActionsView = new LinearLayout(this);
        toolbarActionsView.setOrientation(LinearLayout.HORIZONTAL);
        toolbarActionsView.setGravity(Gravity.CENTER_VERTICAL);
        toolbarView.addView(toolbarActionsView);
        toolbarView.addView(buildRestartButton());
        return toolbarView;
    }

    private View buildRestartButton() {
        String restartLabel = text(R.string.gboard_patches_restart_action);
        restartButton = new View(this) {
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Path path = new Path();

            {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeJoin(Paint.Join.ROUND);
                paint.setStrokeWidth(getResources().getDisplayMetrics().density * 1.8f);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                float density = getResources().getDisplayMetrics().density;
                float iconSize = density * 24f;
                float left = (getWidth() - iconSize) / 2f;
                float top = (getHeight() - iconSize) / 2f;
                float scale = iconSize / 24f;

                paint.setColor(palette.accent);
                path.reset();
                path.moveTo(left + (4.48f * scale), top + (14.5f * scale));
                path.cubicTo(
                        left + (5.6f * scale), top + (17.7f * scale),
                        left + (8.65f * scale), top + (20f * scale),
                        left + (12.25f * scale), top + (20f * scale));
                path.cubicTo(
                        left + (16.8f * scale), top + (20f * scale),
                        left + (20.5f * scale), top + (16.3f * scale),
                        left + (20.5f * scale), top + (11.75f * scale));
                path.cubicTo(
                        left + (20.5f * scale), top + (7.2f * scale),
                        left + (16.8f * scale), top + (3.5f * scale),
                        left + (12.25f * scale), top + (3.5f * scale));
                path.cubicTo(
                        left + (8.55f * scale), top + (3.5f * scale),
                        left + (5.55f * scale), top + (5.95f * scale),
                        left + (4.5f * scale), top + (9.5f * scale));
                canvas.drawPath(path, paint);

                path.reset();
                path.moveTo(left + (4.5f * scale), top + (4.5f * scale));
                path.lineTo(left + (4.5f * scale), top + (9.5f * scale));
                path.lineTo(left + (9.5f * scale), top + (9.5f * scale));
                canvas.drawPath(path, paint);
            }
        };
        restartButton.setContentDescription(restartLabel);
        restartButton.setLayoutParams(new LinearLayout.LayoutParams(dp(48), dp(48)));
        restartButton.setBackground(buildRippleDrawable(dp(24)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            restartButton.setTooltipText(restartLabel);
        } else {
            restartButton.setOnLongClickListener(view -> {
                showSafeToast(R.string.gboard_patches_restart_action);
                return true;
            });
        }
        restartButton.setOnClickListener(view -> confirmGboardRestart());
        return restartButton;
    }

    private void scheduleRestartCrashRecoveryGuardRestore() {
        try {
            Context applicationContext = getApplicationContext();
            Context restoreContext = applicationContext != null ? applicationContext : this;
            boolean scheduled = screenRefreshHandler.postDelayed(
                    () -> restoreRestartCrashRecoveryGuard(restoreContext),
                    RESTART_CRASH_RECOVERY_RESTORE_DELAY_MS);
            if (!scheduled) {
                Log.w(TAG, "Failed to schedule Gboard crash recovery cleanup restoration");
            }
        } catch (Throwable throwable) {
            try {
                Log.w(TAG, "Failed to defer Gboard crash recovery cleanup restoration", throwable);
            } catch (Throwable ignored) {
                // The restart guard must never escape into the host app.
            }
        }
    }

    private void restoreRestartCrashRecoveryGuard(Context context) {
        try {
            if (!GboardRestartCrashRecoveryGuard.restoreIfPending(context)) {
                Log.w(TAG, "Failed to restore Gboard crash recovery cleanup setting");
            }
        } catch (Throwable throwable) {
            try {
                Log.w(TAG, "Failed to run Gboard crash recovery cleanup restoration", throwable);
            } catch (Throwable ignored) {
                // The restart guard must never escape into the host app.
            }
        }
    }

    private void confirmGboardRestart() {
        showConfirmDialog(
                text(R.string.gboard_patches_restart_confirm_title),
                text(R.string.gboard_patches_restart_confirm_message),
                text(R.string.gboard_patches_restart_confirm_action),
                this::requestGboardRestart);
    }

    /** Called after a setting changes; screens that say a restart is needed show a banner. */
    private void markSettingsChanged() {
        if (currentScreenMentionsRestart) {
            restartRecommended = true;
        }
    }

    private boolean screenMentionsRestart(GboardPatchesSettingsContract.Screen screen) {
        List<String> texts = new ArrayList<String>();
        texts.add(screen.getHeaderSummary());
        for (GboardPatchesSettingsContract.StatusBlock block : screen.getStatusBlocks()) {
            texts.add(block.getSummary());
        }
        for (GboardPatchesSettingsContract.Section section : screen.getSections()) {
            texts.add(section.getDescription());
            for (GboardPatchesSettingsContract.Row row : section.getItems()) {
                texts.add(row.getSummary());
            }
        }
        String marker = text(R.string.gboard_patches_restart_action).toLowerCase(Locale.ROOT);
        for (String value : texts) {
            if (value == null) {
                continue;
            }
            String normalized = value.toLowerCase(Locale.ROOT);
            if (normalized.contains(marker) || normalized.contains("restart gboard")) {
                return true;
            }
        }
        return false;
    }

    private View createRestartBanner() {
        LinearLayout banner = new LinearLayout(this);
        banner.setOrientation(LinearLayout.HORIZONTAL);
        banner.setGravity(Gravity.CENTER_VERTICAL);
        banner.setBackground(buildCardDrawable(
                palette.secondaryContainer,
                Color.TRANSPARENT,
                dp(TILE_OUTER_RADIUS_DP)));
        banner.setPadding(dp(16), dp(8), dp(8), dp(8));
        banner.setMinimumHeight(dp(56));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(12);
        banner.setLayoutParams(params);

        TextView messageView = new TextView(this);
        messageView.setText(text(R.string.gboard_patches_restart_banner_message));
        messageView.setTextColor(palette.onSecondaryContainer);
        messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        messageView.setTypeface(bodyTypeface());
        banner.addView(messageView, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView restartView = new TextView(this);
        restartView.setText(text(R.string.gboard_patches_restart_confirm_action));
        restartView.setTextColor(palette.onSecondaryContainer);
        restartView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        restartView.setTypeface(mediumTypeface());
        restartView.setGravity(Gravity.CENTER);
        restartView.setMinHeight(dp(40));
        restartView.setMinimumHeight(dp(40));
        restartView.setPadding(dp(16), 0, dp(16), 0);
        restartView.setBackground(buildRippleDrawable(dp(20)));
        restartView.setOnClickListener(view -> requestGboardRestart());
        banner.addView(restartView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return banner;
    }

    private void requestGboardRestart() {
        try {
            GboardPatchesSettingsOrchestrator.State<GboardPatchesSettingsContract.Feature> state =
                    settingsOrchestrator.snapshot();
            ArrayList<String> navigationPath = GboardPatchesNavigationPath.capture(
                    state.getBackStack(), state.getCurrent());

            Context applicationContext = getApplicationContext();
            Context restartContext = applicationContext != null ? applicationContext : this;
            if (!GboardRestartCrashRecoveryGuard.prepare(restartContext)) {
                handleRestartFailure("Failed to prepare safe Gboard restart", null);
                return;
            }
            setRestartButtonPending(true);
            boolean scheduled = screenRefreshHandler.postDelayed(
                    () -> coldRestartGboard(restartContext, navigationPath),
                    RESTART_DELAY_MS);
            if (!scheduled) {
                handleRestartFailure("Failed to schedule Gboard restart", null);
            }
        } catch (Throwable throwable) {
            handleRestartFailure("Failed to request Gboard restart", throwable);
        }
    }

    private void coldRestartGboard(Context context, ArrayList<String> navigationPath) {
        try {
            ComponentName patchesComponent =
                    new ComponentName(context, GboardPatchesSettingsActivity.class);
            Intent restartIntent = Intent.makeRestartActivityTask(patchesComponent);
            restartIntent.putStringArrayListExtra(EXTRA_NAVIGATION_PATH, navigationPath);
            restartIntent.setPackage(context.getPackageName());
            context.startActivity(restartIntent);
            System.exit(0);
        } catch (Throwable throwable) {
            handleRestartFailure("Failed to cold restart Gboard", throwable);
        }
    }

    private void setRestartButtonPending(boolean pending) {
        try {
            if (!pending) {
                stopRestartButtonAnimation();
            }
            if (restartButton == null) {
                return;
            }
            restartButton.setEnabled(!pending);
            restartButton.setAlpha(1f);
            if (pending) {
                startRestartButtonAnimation();
            }
        } catch (Throwable throwable) {
            try {
                Log.w(TAG, "Failed to update Gboard restart action", throwable);
            } catch (Throwable ignored) {
                // A restart failure must never escape into the host app.
            }
        }
    }

    private void startRestartButtonAnimation() {
        stopRestartButtonAnimation();
        if (restartButton == null) {
            return;
        }
        restartButtonAnimator = ObjectAnimator.ofFloat(
                restartButton,
                View.ROTATION,
                0f,
                -360f);
        restartButtonAnimator.setDuration(RESTART_ICON_ROTATION_DURATION_MS);
        restartButtonAnimator.setInterpolator(new LinearInterpolator());
        restartButtonAnimator.setRepeatCount(ValueAnimator.INFINITE);
        restartButtonAnimator.start();
    }

    private void stopRestartButtonAnimation() {
        if (restartButtonAnimator != null) {
            restartButtonAnimator.cancel();
            restartButtonAnimator = null;
        }
        if (restartButton != null) {
            restartButton.setRotation(0f);
        }
    }

    private void handleRestartFailure(String message, Throwable throwable) {
        try {
            if (!GboardRestartCrashRecoveryGuard.restoreIfPending(this)) {
                Log.w(TAG, "Failed to roll back Gboard restart guard");
            }
        } catch (Throwable ignored) {
            // A restart failure must never escape into the host app.
        }
        try {
            Log.w(TAG, message, throwable);
        } catch (Throwable ignored) {
            // A restart failure must never escape into the host app.
        }
        setRestartButtonPending(false);
        try {
            showRestartFailure();
        } catch (Throwable notificationThrowable) {
            try {
                Log.w(TAG, "Failed to show Gboard restart error", notificationThrowable);
            } catch (Throwable ignored) {
                // A restart failure must never escape into the host app.
            }
        }
    }

    private void showRestartFailure() {
        Toast.makeText(
                this,
                text(R.string.gboard_patches_restart_failed),
                Toast.LENGTH_LONG)
                .show();
    }

    private View buildBackButton() {
        View backButton = new View(this) {
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Path path = new Path();

            {
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeJoin(Paint.Join.ROUND);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                float density = getResources().getDisplayMetrics().density;
                float iconSize = density * 24f;
                float left = (getWidth() - iconSize) / 2f;
                float top = (getHeight() - iconSize) / 2f;
                float scale = iconSize / 24f;

                paint.setStyle(Paint.Style.FILL);
                paint.setColor(palette.surfaceContainerHighest);
                canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, density * 20f, paint);

                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(density * 2f);
                paint.setColor(palette.textPrimary);
                path.reset();
                path.moveTo(left + (19f * scale), top + (12f * scale));
                path.lineTo(left + (5.5f * scale), top + (12f * scale));
                path.moveTo(left + (11.5f * scale), top + (6f * scale));
                path.lineTo(left + (5.5f * scale), top + (12f * scale));
                path.lineTo(left + (11.5f * scale), top + (18f * scale));
                canvas.drawPath(path, paint);
            }
        };
        backButton.setContentDescription(text(R.string.gboard_patches_navigate_up));
        backButton.setLayoutParams(new LinearLayout.LayoutParams(dp(48), dp(48)));
        backButton.setBackground(buildRippleDrawable(dp(24)));
        backButton.setOnClickListener(view -> goBackOrFinish());
        return backButton;
    }

    private View buildHeaderCard() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(8), dp(12), dp(8), dp(4));
        header.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        headerTitleView = new TextView(this);
        headerTitleView.setTextColor(palette.textPrimary);
        headerTitleView.setTypeface(headlineTypeface());
        headerTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36f);
        headerTitleView.setLineSpacing(0f, 1.1f);
        headerTitleView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        headerSummaryView = new TextView(this);
        headerSummaryView.setTextColor(palette.textSecondary);
        headerSummaryView.setTypeface(bodyTypeface());
        headerSummaryView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        headerSummaryView.setLineSpacing(0f, 1.15f);
        LinearLayout.LayoutParams summaryParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        summaryParams.topMargin = dp(8);
        headerSummaryView.setLayoutParams(summaryParams);

        header.addView(headerTitleView);
        header.addView(headerSummaryView);
        return header;
    }

    private void installCollapsingTitle() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || contentScrollView == null) {
            return;
        }
        contentScrollView.setOnScrollChangeListener(
                (view, scrollX, scrollY, oldScrollX, oldScrollY) ->
                        updateCollapsingTitle(scrollY));
    }

    private void updateCollapsingTitle(int scrollY) {
        if (toolbarTitleView == null || headerTitleView == null || headerCardView == null) {
            return;
        }
        boolean headerVisible = headerCardView.getVisibility() == View.VISIBLE;
        int collapseOffset = Math.max(dp(24), headerTitleView.getBottom() - dp(12));
        boolean collapsed = !headerVisible
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || scrollY >= collapseOffset;
        headerTitleView.setAlpha(headerVisible
                ? Math.max(0f, 1f - (scrollY / (float) collapseOffset))
                : 1f);
        if (collapsed == toolbarTitleCollapsed) {
            return;
        }
        toolbarTitleCollapsed = collapsed;
        toolbarTitleView.animate()
                .alpha(collapsed ? 1f : 0f)
                .setDuration(TOOLBAR_TITLE_FADE_DURATION_MS)
                .start();
    }

    private View buildPanelCard() {
        panelContainer = new LinearLayout(this);
        panelContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams panelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        panelParams.topMargin = dp(8);
        panelContainer.setLayoutParams(panelParams);
        return panelContainer;
    }

    private void goBackOrFinish() {
        if (navigateToRootIfNeeded()) {
            return;
        }
        finish();
    }

    @Override
    public void openExternalUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ignored) {
            // Ignore devices without a visible browser handler.
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to open external URL: " + url, throwable);
        }
    }

    @Override
    public void openSpeechRecognitionAndSynthesisStoreListing() {
        if (openGooglePlayListing(
                SPEECH_SERVICES_PACKAGE_NAME,
                SPEECH_SERVICES_PLAY_STORE_URL)) {
            return;
        }
        showSafeToast(
                R.string.gboard_patches_advanced_voice_speech_services_open_failed);
    }

    @Override
    public void openLiveTranscribeLanguageManager() {
        Intent launchIntent = null;
        try {
            launchIntent = getPackageManager().getLaunchIntentForPackage(
                    LIVE_TRANSCRIBE_PACKAGE_NAME);
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to resolve Live Transcribe launcher", throwable);
        }
        if (tryStartActivity(launchIntent)) {
            return;
        }
        if (openGooglePlayListing(
                LIVE_TRANSCRIBE_PACKAGE_NAME,
                LIVE_TRANSCRIBE_PLAY_STORE_URL)) {
            return;
        }
        showSafeToast(
                R.string.gboard_patches_advanced_voice_live_transcribe_open_failed);
    }

    private void showSafeToast(int resourceId) {
        try {
            String message = GboardSettingsText.get(this, resourceId);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } catch (Throwable throwable) {
            try {
                Log.w(TAG, "Failed to show navigation error message", throwable);
            } catch (Throwable ignored) {
                // A patch error must never escape into the host app.
            }
        }
    }

    private boolean openGooglePlayListing(String packageName, String webUrl) {
        Intent marketIntent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + packageName));
        marketIntent.setPackage(PLAY_STORE_PACKAGE_NAME);
        if (tryStartActivity(marketIntent)) {
            return true;
        }
        return tryStartActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)));
    }

    private boolean tryStartActivity(Intent intent) {
        if (intent == null) {
            return false;
        }
        try {
            startActivity(intent);
            return true;
        } catch (ActivityNotFoundException | SecurityException exception) {
            Log.w(TAG, "Activity navigation attempt failed: " + intent, exception);
            return false;
        } catch (Throwable throwable) {
            Log.w(TAG, "Unexpected activity navigation failure: " + intent, throwable);
            return false;
        }
    }

    private void refreshOfflineSpeechLanguagesForCurrentFeature() {
        GboardPatchesSettingsContract.Feature currentFeature =
                settingsOrchestrator.snapshot().getCurrent();
        if (currentFeature != null && currentFeature.requiresOfflineSpeechLanguages()) {
            queryOfflineSpeechLanguages();
            return;
        }
        offlineSpeechLanguageQueryGeneration++;
        cancelOfflineSpeechLanguageQueryTimeout();
        destroyOfflineSpeechRecognizer();
    }

    private void queryOfflineSpeechLanguages() {
        offlineSpeechLanguageQueryGeneration++;
        cancelOfflineSpeechLanguageQueryTimeout();
        destroyOfflineSpeechRecognizer();
        offlineSpeechLanguages = GboardPatchesSettingsContract.OfflineSpeechLanguages.loading();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            offlineSpeechLanguages =
                    GboardPatchesSettingsContract.OfflineSpeechLanguages.unsupported();
            renderCurrentScreenSafely();
            return;
        }
        ComponentName recognitionService = new ComponentName(
                SPEECH_SERVICES_PACKAGE_NAME,
                SPEECH_SERVICES_RECOGNITION_SERVICE_CLASS_NAME);
        if (!isRecognitionServiceAvailable(recognitionService)) {
            offlineSpeechLanguages =
                    GboardPatchesSettingsContract.OfflineSpeechLanguages.unavailable();
            renderCurrentScreenSafely();
            return;
        }

        final int queryGeneration = offlineSpeechLanguageQueryGeneration;
        try {
            offlineSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(
                    this,
                    recognitionService);
            Intent recognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognitionIntent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            recognitionIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
            offlineSpeechLanguageQueryTimeoutRunnable = () -> {
                if (queryGeneration != offlineSpeechLanguageQueryGeneration) {
                    return;
                }
                Log.w(TAG, "Speech Services recognition support query timed out");
                completeOfflineSpeechLanguageQuery(
                        queryGeneration,
                        GboardPatchesSettingsContract.OfflineSpeechLanguages.error());
            };
            screenRefreshHandler.postDelayed(
                    offlineSpeechLanguageQueryTimeoutRunnable,
                    OFFLINE_SPEECH_LANGUAGE_QUERY_TIMEOUT_MS);
            offlineSpeechRecognizer.checkRecognitionSupport(
                    recognitionIntent,
                    getMainExecutor(),
                    new RecognitionSupportCallback() {
                        @Override
                        public void onSupportResult(RecognitionSupport recognitionSupport) {
                            if (queryGeneration != offlineSpeechLanguageQueryGeneration) {
                                return;
                            }
                            List<String> installedLanguages = recognitionSupport == null
                                    ? Collections.emptyList()
                                    : recognitionSupport.getInstalledOnDeviceLanguages();
                            Log.i(TAG, "Speech Services installed offline languages from "
                                    + recognitionService.flattenToShortString()
                                    + ": " + installedLanguages);
                            completeOfflineSpeechLanguageQuery(
                                    queryGeneration,
                                    GboardPatchesSettingsContract.OfflineSpeechLanguages.available(
                                            installedLanguages));
                        }

                        @Override
                        public void onError(int error) {
                            if (queryGeneration != offlineSpeechLanguageQueryGeneration) {
                                return;
                            }
                            Log.w(TAG,
                                    "Speech Services recognition support callback reported error "
                                            + error
                                            + "; waiting for timeout or a later support result");
                        }
                    });
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to query installed on-device speech languages", throwable);
            completeOfflineSpeechLanguageQuery(
                    queryGeneration,
                    GboardPatchesSettingsContract.OfflineSpeechLanguages.error());
        }
    }

    @SuppressWarnings("deprecation")
    private boolean isRecognitionServiceAvailable(ComponentName recognitionService) {
        try {
            android.content.pm.ServiceInfo serviceInfo = getPackageManager().getServiceInfo(
                    recognitionService,
                    0);
            return serviceInfo.enabled
                    && serviceInfo.applicationInfo != null
                    && serviceInfo.applicationInfo.enabled;
        } catch (PackageManager.NameNotFoundException exception) {
            Log.w(TAG, "Speech Services recognition service is unavailable: "
                    + recognitionService.flattenToShortString());
            return false;
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to inspect Speech Services recognition service: "
                    + recognitionService.flattenToShortString(), throwable);
            return false;
        }
    }

    private void completeOfflineSpeechLanguageQuery(
            int queryGeneration,
            GboardPatchesSettingsContract.OfflineSpeechLanguages result) {
        if (queryGeneration != offlineSpeechLanguageQueryGeneration) {
            return;
        }
        offlineSpeechLanguageQueryGeneration++;
        cancelOfflineSpeechLanguageQueryTimeout();
        offlineSpeechLanguages = result;
        destroyOfflineSpeechRecognizer();
        renderCurrentScreenSafely();
    }

    private void cancelOfflineSpeechLanguageQuery() {
        offlineSpeechLanguageQueryGeneration++;
        cancelOfflineSpeechLanguageQueryTimeout();
        destroyOfflineSpeechRecognizer();
    }

    private void cancelOfflineSpeechLanguageQueryTimeout() {
        if (offlineSpeechLanguageQueryTimeoutRunnable == null) {
            return;
        }
        screenRefreshHandler.removeCallbacks(offlineSpeechLanguageQueryTimeoutRunnable);
        offlineSpeechLanguageQueryTimeoutRunnable = null;
    }

    private void destroyOfflineSpeechRecognizer() {
        if (offlineSpeechRecognizer == null) {
            return;
        }
        try {
            offlineSpeechRecognizer.destroy();
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to destroy on-device speech recognizer", throwable);
        } finally {
            offlineSpeechRecognizer = null;
        }
    }

    private boolean navigateToRootIfNeeded() {
        return !navigateBack();
    }

    private boolean navigateBack() {
        GboardPatchesSettingsOrchestrator.State<GboardPatchesSettingsContract.Feature> state =
                settingsOrchestrator.snapshot();
        if (state.getCurrent() != null) {
            boolean returningToRoot = state.getBackStack().isEmpty();
            requestScrollPositionOnNextScreenApply(
                    scrollState.leaveFeature(returningToRoot));
        }
        return applyOrchestration(settingsOrchestrator.accept(
                GboardPatchesSettingsOrchestrator.Event.back()));
    }

    private void registerBackCallback() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        try {
            Class<?> callbackClass = Class.forName("android.window.OnBackInvokedCallback");
            Class<?> dispatcherClass = Class.forName("android.window.OnBackInvokedDispatcher");
            Object callback = Proxy.newProxyInstance(
                    callbackClass.getClassLoader(),
                    new Class<?>[] { callbackClass },
                    (proxy, method, args) -> {
                        if (method.getDeclaringClass() == Object.class) {
                            if ("hashCode".equals(method.getName())) {
                                return Integer.valueOf(System.identityHashCode(proxy));
                            }
                            if ("equals".equals(method.getName())) {
                                Object other = args != null && args.length > 0 ? args[0] : null;
                                return Boolean.valueOf(proxy == other);
                            }
                            if ("toString".equals(method.getName())) {
                                return "GboardPatchesOnBackInvokedCallbackProxy";
                            }
                        }
                        if ("onBackInvoked".equals(method.getName())) {
                            if (!navigateToRootIfNeeded()) {
                                finish();
                            }
                        }
                        return null;
                    });
            Object dispatcher = Activity.class.getMethod("getOnBackInvokedDispatcher")
                    .invoke(this);
            if (dispatcher == null) {
                return;
            }
            int priorityDefault = dispatcherClass.getField("PRIORITY_DEFAULT").getInt(null);
            dispatcherClass.getMethod("registerOnBackInvokedCallback", int.class, callbackClass)
                    .invoke(dispatcher, priorityDefault, callback);
            backInvokedCallback = callback;
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to register platform back callback", throwable);
            backInvokedCallback = null;
        }
    }

    private void unregisterBackCallback() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || backInvokedCallback == null) {
            return;
        }
        try {
            Class<?> callbackClass = Class.forName("android.window.OnBackInvokedCallback");
            Class<?> dispatcherClass = Class.forName("android.window.OnBackInvokedDispatcher");
            Object dispatcher = Activity.class.getMethod("getOnBackInvokedDispatcher")
                    .invoke(this);
            if (dispatcher != null) {
                dispatcherClass.getMethod("unregisterOnBackInvokedCallback", callbackClass)
                        .invoke(dispatcher, backInvokedCallback);
            }
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to unregister platform back callback", throwable);
        }
        backInvokedCallback = null;
    }

    private void installWindowInsetsHandling(View root) {
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            try {
                updateToolbarInsets(resolveTopInset(insets));
                int bottomInset = resolveBottomInset(insets);
                if (contentScrollView != null) {
                    int bottomPadding = keyboardPreviewController == null
                            ? bottomInset
                            : keyboardPreviewController.contentBottomPadding(bottomInset);
                    contentScrollView.setPadding(0, 0, 0, bottomPadding);
                }
                if (screenActionContainer != null) {
                    screenActionContainer.setPadding(
                            dp(16), dp(8), dp(16), dp(16) + bottomInset);
                }
                if (keyboardPreviewController != null) {
                    keyboardPreviewController.updateSafeAreaInsets(resolveRightInset(insets),
                            bottomInset);
                }
            } catch (Throwable throwable) {
                try {
                    Log.w(TAG, "Failed to apply patches settings insets", throwable);
                } catch (Throwable ignored) {
                    // Insets are optional UI polish and must not affect the settings host.
                }
            }
            return insets;
        });
        root.requestApplyInsets();
    }

    private void scheduleDeferredRender() {
        if (settingsOrchestrator.snapshot().isFatal()) {
            return;
        }
        View anchor = panelContainer != null ? panelContainer : getWindow().getDecorView();
        if (anchor == null) {
            initializeFeaturesAndRenderSafely();
            return;
        }
        anchor.removeCallbacks(deferredRenderRunnable);
        anchor.post(deferredRenderRunnable);
    }

    private void cancelDeferredRender() {
        View anchor = panelContainer != null ? panelContainer : getWindow().getDecorView();
        if (anchor != null) {
            anchor.removeCallbacks(deferredRenderRunnable);
        }
    }

    private void postToDecorView(Runnable action) {
        if (action == null) {
            return;
        }
        View anchor = panelContainer != null ? panelContainer : getWindow().getDecorView();
        if (anchor == null) {
            action.run();
            return;
        }
        anchor.post(action);
    }

    private void initializeFeaturesAndRenderSafely() {
        applyOrchestration(settingsOrchestrator.accept(
                GboardPatchesSettingsOrchestrator.Event.requestRender()));
    }

    private boolean applyOrchestration(
            GboardPatchesSettingsOrchestrator.Transition<
                    GboardPatchesSettingsContract.Feature,
                    GboardPatchesSettingsContract.Intent> transition) {
        boolean exitRequested = false;
        for (GboardPatchesSettingsOrchestrator.Effect<
                GboardPatchesSettingsContract.Feature,
                GboardPatchesSettingsContract.Intent> effect : transition.getEffects()) {
            switch (effect.getKind()) {
                case REQUEST_RENDER:
                    enqueueScreenBuild(effect.getGeneration());
                    break;
                case REFRESH_DEPENDENCIES:
                    refreshOfflineSpeechLanguagesForCurrentFeature();
                    break;
                case SCROLL_TO_TOP:
                    if (requestedScrollYOnNextScreenApply == NO_SCROLL_POSITION_REQUESTED) {
                        requestScrollPositionOnNextScreenApply(0);
                    }
                    break;
                case SCHEDULE_REFRESH:
                    scheduleScreenRefresh(effect.getDelayMs());
                    break;
                case CANCEL_REFRESH:
                    cancelScheduledScreenRefresh();
                    break;
                case CANCEL_PENDING_RENDER:
                    cancelDeferredRender();
                    break;
                case EXECUTE_INTENT:
                    GboardPatchesSettingsContract.Intent intent = effect.getPayload();
                    if (intent != null) {
                        intent.apply(this);
                    }
                    break;
                case EXIT:
                    exitRequested = true;
                    break;
                default:
                    break;
            }
        }
        syncVisibleFeatureLifecycle();
        return exitRequested;
    }

    private void syncVisibleFeatureLifecycle() {
        GboardPatchesSettingsContract.Feature target = featureLifecycleEnabled
                ? settingsOrchestrator.snapshot().getCurrent()
                : null;
        if (target == lifecycleVisibleFeature) {
            return;
        }
        GboardPatchesSettingsContract.Feature previous = lifecycleVisibleFeature;
        lifecycleVisibleFeature = target;
        if (previous != null) {
            try {
                previous.onHidden(this);
            } catch (Throwable throwable) {
                Log.w(TAG, "Failed to hide settings feature", throwable);
            }
        }
        if (target != null) {
            try {
                target.onVisible(this);
            } catch (Throwable throwable) {
                Log.w(TAG, "Failed to show settings feature", throwable);
            }
        }
    }

    private void enqueueScreenBuild(int buildGeneration) {
        try {
            screenBuildExecutor.execute(() -> buildAndApplyCurrentScreen(buildGeneration));
        } catch (RejectedExecutionException ignored) {
            // Activity is already tearing down.
        }
    }

    private static boolean containsEffect(
            GboardPatchesSettingsOrchestrator.Transition<?, ?> transition,
            GboardPatchesSettingsOrchestrator.EffectKind kind) {
        for (GboardPatchesSettingsOrchestrator.Effect<?, ?> effect : transition.getEffects()) {
            if (effect.getKind() == kind) {
                return true;
            }
        }
        return false;
    }

    private void ensureFeaturesInitialized() {
        if (featuresInitialized) {
            return;
        }
        synchronized (screenBuildLock) {
            if (featuresInitialized) {
                return;
            }
            try {
                features = GboardPatchesSettingsFeatureRegistry.features(this);
            } catch (Throwable throwable) {
                throw new IllegalStateException(
                        "Failed to resolve patches settings features",
                        throwable);
            }
            featuresInitialized = true;
        }
    }

    private void buildAndApplyCurrentScreen(int buildGeneration) {
        if (settingsOrchestrator.snapshot().isFatal()) {
            return;
        }

        GboardPatchesSettingsContract.Screen screen;
        GboardPatchesSettingsContract.Feature featureSnapshot =
                settingsOrchestrator.snapshot().getCurrent();
        try {
            ensureFeaturesInitialized();
            if (openInitialFeatureFromIntentIfNeeded()) {
                return;
            }
            featureSnapshot = settingsOrchestrator.snapshot().getCurrent();
            screen = featureSnapshot == null
                    ? buildRootScreen()
                    : featureSnapshot.buildScreen(this);
        } catch (IllegalStateException exception) {
            runOnUiThread(() -> {
                if (!isLatestScreenBuild(buildGeneration)
                        || settingsOrchestrator.snapshot().isFatal()
                        || isFinishing()) {
                    return;
                }
                Throwable cause = exception.getCause() == null ? exception : exception.getCause();
                showFatalFallbackScreen(exception.getMessage(), cause);
            });
            return;
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to render settings screen", throwable);
            screen = buildFeatureErrorScreen(settingsOrchestrator.snapshot().getCurrent());
        }

        GboardPatchesSettingsContract.Feature appliedFeature = featureSnapshot;
        GboardPatchesSettingsContract.Screen finalScreen = screen;
        runOnUiThread(() -> {
            if (isFinishing()
                    || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                    && isDestroyed())) {
                return;
            }
            GboardPatchesSettingsOrchestrator.Transition<
                    GboardPatchesSettingsContract.Feature,
                    GboardPatchesSettingsContract.Intent> renderTransition =
                    settingsOrchestrator.accept(
                            GboardPatchesSettingsOrchestrator.Event.renderReady(
                                    buildGeneration,
                                    finalScreen.getRefreshIntervalMs()));
            if (!containsEffect(renderTransition,
                    GboardPatchesSettingsOrchestrator.EffectKind.APPLY_RENDER)
                    || settingsOrchestrator.snapshot().isFatal()) {
                return;
            }
            try {
                applyScreen(finalScreen);
                applyOrchestration(renderTransition);
            } catch (Throwable throwable) {
                Log.w(TAG, "Failed to apply settings screen", throwable);
                try {
                    GboardPatchesSettingsOrchestrator.Transition<
                            GboardPatchesSettingsContract.Feature,
                            GboardPatchesSettingsContract.Intent> recoveryTransition =
                            settingsOrchestrator.accept(
                                    GboardPatchesSettingsOrchestrator.Event.renderFailed(
                                            buildGeneration));
                    if (!containsEffect(recoveryTransition,
                            GboardPatchesSettingsOrchestrator.EffectKind.APPLY_RECOVERY)) {
                        return;
                    }
                    GboardPatchesSettingsContract.Screen recoveryScreen =
                            buildFeatureErrorScreen(appliedFeature);
                    GboardPatchesSettingsOrchestrator.Transition<
                            GboardPatchesSettingsContract.Feature,
                            GboardPatchesSettingsContract.Intent> recoveryReady =
                            settingsOrchestrator.accept(
                                    GboardPatchesSettingsOrchestrator.Event.recoveryReady(
                                            buildGeneration,
                                            recoveryScreen.getRefreshIntervalMs()));
                    if (!containsEffect(recoveryReady,
                            GboardPatchesSettingsOrchestrator.EffectKind.APPLY_RECOVERY)) {
                        return;
                    }
                    applyScreen(recoveryScreen);
                    applyOrchestration(recoveryReady);
                } catch (Throwable fallbackThrowable) {
                    Log.w(TAG, "Failed to apply feature error screen", fallbackThrowable);
                    try {
                        fallbackThrowable.addSuppressed(throwable);
                    } catch (Throwable ignored) {
                    }
                    showFatalFallbackScreen("Failed to apply settings screen", fallbackThrowable);
                }
            }
        });
    }

    private boolean isLatestScreenBuild(int buildGeneration) {
        return buildGeneration == settingsOrchestrator.snapshot().getRenderGeneration();
    }

    private void updateToolbarInsets(int topInset) {
        if (toolbarView == null) {
            return;
        }
        ViewGroup.LayoutParams layoutParams = toolbarView.getLayoutParams();
        int targetHeight = dp(TOOLBAR_HEIGHT_DP) + topInset;
        if (layoutParams.height != targetHeight) {
            layoutParams.height = targetHeight;
            toolbarView.setLayoutParams(layoutParams);
        }
        toolbarView.setPadding(dp(8), topInset, dp(4), 0);
    }

    private int resolveTopInset(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Insets systemBarsInsets = insets.getInsets(WindowInsets.Type.systemBars());
            return systemBarsInsets.top;
        }
        return insets.getSystemWindowInsetTop();
    }

    private int resolveBottomInset(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Insets systemBarsInsets = insets.getInsets(WindowInsets.Type.systemBars());
            return systemBarsInsets.bottom;
        }
        return insets.getSystemWindowInsetBottom();
    }

    private int resolveRightInset(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Insets systemBarsInsets = insets.getInsets(WindowInsets.Type.systemBars());
            return systemBarsInsets.right;
        }
        return insets.getSystemWindowInsetRight();
    }

    private void renderCurrentScreenSafely() {
        initializeFeaturesAndRenderSafely();
    }

    private void applyScreen(GboardPatchesSettingsContract.Screen screen) {
        String title = TextUtils.isEmpty(screen.getToolbarTitle())
                ? screen.getHeaderTitle()
                : screen.getToolbarTitle();
        toolbarTitleView.setText(title);
        applyToolbarActions(screen.getToolbarActions());
        boolean fullBleed = screen.isFullBleed();
        headerCardView.setVisibility(fullBleed ? View.GONE : View.VISIBLE);
        contentColumn.setPadding(
                fullBleed ? 0 : dp(16),
                0,
                fullBleed ? 0 : dp(16),
                0);
        ViewGroup.LayoutParams rawPanelParams = panelContainer.getLayoutParams();
        if (rawPanelParams instanceof LinearLayout.LayoutParams panelParams) {
            panelParams.topMargin = fullBleed ? 0 : dp(8);
            panelContainer.setLayoutParams(panelParams);
        }
        headerTitleView.setText(title);
        headerSummaryView.setText(screen.getHeaderSummary());
        headerSummaryView.setVisibility(
                TextUtils.isEmpty(screen.getHeaderSummary()) ? View.GONE : View.VISIBLE);
        applyPanelStyle(screen.getPanelStyle());
        panelContainer.removeAllViews();
        currentScreenMentionsRestart = screenMentionsRestart(screen);
        if (restartRecommended && !fullBleed) {
            panelContainer.addView(createRestartBanner());
        }
        for (GboardPatchesSettingsContract.StatusBlock statusBlock : screen.getStatusBlocks()) {
            panelContainer.addView(createStatusBlockView(statusBlock));
        }
        for (GboardPatchesSettingsContract.Section section : screen.getSections()) {
            panelContainer.addView(createSectionView(section));
        }
        screenActionContainer.removeAllViews();
        GboardPatchesSettingsContract.CommandRow primaryAction = screen.getPrimaryAction();
        if (primaryAction == null) {
            screenActionContainer.setVisibility(View.GONE);
        } else {
            screenActionContainer.addView(createPrimaryActionButton(primaryAction));
            screenActionContainer.setVisibility(View.VISIBLE);
        }
        int requestedScrollY = consumeRequestedScrollPositionOnNextScreenApply();
        if (requestedScrollY >= 0) {
            scrollContentToPositionAfterLayout(requestedScrollY);
        }
        updateCollapsingTitle(currentScrollY());
    }

    private void applyToolbarActions(
            List<GboardPatchesSettingsContract.ToolbarAction> actions) {
        if (toolbarActionsView == null) {
            return;
        }
        toolbarActionsView.removeAllViews();
        if (actions == null) {
            return;
        }
        for (GboardPatchesSettingsContract.ToolbarAction action : actions) {
            if (action != null) {
                toolbarActionsView.addView(buildToolbarActionButton(action));
            }
        }
    }

    private View buildToolbarActionButton(GboardPatchesSettingsContract.ToolbarAction action) {
        View button = new View(this) {
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeJoin(Paint.Join.ROUND);
                paint.setStrokeWidth(getResources().getDisplayMetrics().density * 1.8f);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                if (action.getIcon() != GboardPatchesSettingsContract.ToolbarIcon.DELETE) {
                    return;
                }
                float density = getResources().getDisplayMetrics().density;
                float left = (getWidth() - density * 24f) / 2f;
                float top = (getHeight() - density * 24f) / 2f;
                paint.setColor(palette.textSecondary);
                canvas.drawLine(left + 5f * density, top + 6f * density,
                        left + 19f * density, top + 6f * density, paint);
                canvas.drawLine(left + 9f * density, top + 3.5f * density,
                        left + 15f * density, top + 3.5f * density, paint);
                canvas.drawRoundRect(left + 7f * density, top + 8f * density,
                        left + 17f * density, top + 21f * density,
                        1.5f * density, 1.5f * density, paint);
                canvas.drawLine(left + 10f * density, top + 11f * density,
                        left + 10f * density, top + 18f * density, paint);
                canvas.drawLine(left + 14f * density, top + 11f * density,
                        left + 14f * density, top + 18f * density, paint);
            }
        };
        button.setContentDescription(action.getContentDescription());
        button.setLayoutParams(new LinearLayout.LayoutParams(dp(48), dp(48)));
        button.setBackground(buildRippleDrawable(dp(24)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            button.setTooltipText(action.getContentDescription());
        }
        button.setOnClickListener(view -> runSafely(
                "handle toolbar action", action.getAction()));
        return button;
    }

    private void applyPanelStyle(GboardPatchesSettingsContract.PanelStyle panelStyle) {
        if (panelContainer == null) {
            return;
        }
        // Sections draw their own grouped tiles, so both panel styles sit directly on the
        // window background, as in Gboard's own settings.
        panelContainer.setBackground(null);
        panelContainer.setPadding(0, 0, 0, 0);
    }

    private void openFeaturePath(GboardPatchesSettingsContract.Feature... featurePath) {
        if (featurePath == null || featurePath.length == 0) {
            return;
        }
        List<GboardPatchesSettingsContract.Feature> sanitizedPath =
                new ArrayList<GboardPatchesSettingsContract.Feature>();
        for (GboardPatchesSettingsContract.Feature feature : featurePath) {
            if (feature != null) {
                sanitizedPath.add(feature);
            }
        }
        if (sanitizedPath.isEmpty()) {
            return;
        }
        scrollState.resetForDirectPath(sanitizedPath.size() - 1);
        requestScrollPositionOnNextScreenApply(0);
        applyOrchestration(settingsOrchestrator.accept(
                GboardPatchesSettingsOrchestrator.Event.replacePath(sanitizedPath)));
    }

    private boolean openInitialFeatureFromIntentIfNeeded() {
        if (initialFeatureFromIntentHandled) {
            return false;
        }
        initialFeatureFromIntentHandled = true;
        Intent intent = getIntent();
        if (intent == null) {
            return false;
        }
        if (restoreNavigationPathFromIntent(intent)) {
            return true;
        }
        return false;
    }

    private boolean restoreNavigationPathFromIntent(Intent intent) {
        if (!intent.hasExtra(EXTRA_NAVIGATION_PATH)) {
            return false;
        }
        ArrayList<String> pathIds = intent.getStringArrayListExtra(EXTRA_NAVIGATION_PATH);
        intent.removeExtra(EXTRA_NAVIGATION_PATH);
        List<GboardPatchesSettingsContract.Feature> resolvedPath =
                GboardPatchesNavigationPath.resolve(this, features, pathIds);
        if (pathIds == null
                || pathIds.isEmpty()
                || resolvedPath.size() != pathIds.size()) {
            return false;
        }
        runOnUiThread(() -> {
            scrollState.resetForDirectPath(resolvedPath.size() - 1);
            requestScrollPositionOnNextScreenApply(0);
            applyOrchestration(settingsOrchestrator.accept(
                    GboardPatchesSettingsOrchestrator.Event.replacePath(resolvedPath)));
        });
        return true;
    }

    private int currentScrollY() {
        return contentScrollView == null ? 0 : contentScrollView.getScrollY();
    }

    private void requestScrollPositionOnNextScreenApply(int scrollY) {
        requestedScrollYOnNextScreenApply = Math.max(0, scrollY);
    }

    private int consumeRequestedScrollPositionOnNextScreenApply() {
        int requestedScrollY = requestedScrollYOnNextScreenApply;
        requestedScrollYOnNextScreenApply = NO_SCROLL_POSITION_REQUESTED;
        return requestedScrollY;
    }

    private void scrollContentToPositionAfterLayout(int scrollY) {
        if (contentScrollView == null) {
            return;
        }
        int targetScrollY = Math.max(0, scrollY);
        contentScrollView.scrollTo(0, targetScrollY);
        contentScrollView.post(() -> {
            if (contentScrollView == null) {
                return;
            }
            contentScrollView.scrollTo(0, targetScrollY);
        });
    }

    private void scheduleScreenRefresh(long refreshIntervalMs) {
        cancelScheduledScreenRefresh();
        if (refreshIntervalMs <= 0L) {
            return;
        }
        screenRefreshHandler.postDelayed(screenRefreshRunnable, refreshIntervalMs);
    }

    private void cancelScheduledScreenRefresh() {
        screenRefreshHandler.removeCallbacks(screenRefreshRunnable);
    }

    private void showFatalFallbackScreen(String reason, Throwable throwable) {
        settingsOrchestrator.accept(GboardPatchesSettingsOrchestrator.Event.fatal());
        cancelScheduledScreenRefresh();
        setRestartButtonPending(false);
        Log.e(TAG, reason, throwable);
        detachKeyboardPreviewSafely();
        if (palette == null) {
            palette = Palette.forConfiguration(this, getResources().getConfiguration());
        }
        toolbarView = null;
        toolbarTitleView = null;
        toolbarActionsView = null;
        restartButton = null;
        headerCardView = null;
        headerTitleView = null;
        headerSummaryView = null;
        panelContainer = null;
        contentScrollView = null;
        contentColumn = null;
        screenActionContainer = null;
        setContentView(buildFatalFallbackView());
    }

    private View buildFatalFallbackView() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(palette.windowBackground);
        int padding = dp(24);
        root.setPadding(padding, padding, padding, padding);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        TextView titleView = new TextView(this);
        titleView.setText(text(R.string.gboard_patches_fatal_fallback_title));
        titleView.setTextColor(palette.textPrimary);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f);
        titleView.setTypeface(headlineTypeface());
        titleView.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView summaryView = new TextView(this);
        summaryView.setText(text(R.string.gboard_patches_fatal_fallback_summary));
        summaryView.setTextColor(palette.textSecondary);
        summaryView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        summaryView.setGravity(Gravity.CENTER_HORIZONTAL);
        summaryView.setLineSpacing(0f, 1.15f);
        LinearLayout.LayoutParams summaryParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        summaryParams.topMargin = dp(12);

        root.addView(titleView);
        root.addView(summaryView, summaryParams);
        return root;
    }

    private void detachKeyboardPreviewSafely() {
        GboardKeyboardPreviewController controller = keyboardPreviewController;
        keyboardPreviewController = null;
        if (controller == null) return;
        try {
            controller.detach();
        } catch (Throwable throwable) {
            try {
                Log.w(TAG, "Failed to detach keyboard preview", throwable);
            } catch (Throwable ignored) {
                // Activity teardown must continue even if diagnostics fail.
            }
        }
    }

    private GboardPatchesSettingsContract.Screen buildRootScreen() {
        return createRootScreen(
                this,
                features,
                BuildConfig.PATCH_AUTHOR,
                BuildConfig.PATCH_VERSION,
                () -> openExternalUrl(ABOUT_AUTHOR_URL),
                () -> openExternalUrl(ABOUT_PATCH_REPOSITORY_URL),
                ABOUT_REDESIGN_NAME,
                () -> openExternalUrl(ABOUT_REDESIGN_URL));
    }

    static GboardPatchesSettingsContract.Screen createRootScreen(
            GboardPatchesSettingsContract.FeatureHost host,
            List<? extends GboardPatchesSettingsContract.Feature> rootFeatures,
            String aboutAuthor,
            String patchVersion,
            Runnable authorAction,
            Runnable patchRepositoryAction) {
        return createRootScreen(host, rootFeatures, aboutAuthor, patchVersion, authorAction,
                patchRepositoryAction, null, null);
    }

    static GboardPatchesSettingsContract.Screen createRootScreen(
            GboardPatchesSettingsContract.FeatureHost host,
            List<? extends GboardPatchesSettingsContract.Feature> rootFeatures,
            String aboutAuthor,
            String patchVersion,
            Runnable authorAction,
            Runnable patchRepositoryAction,
            String redesignAuthor,
            Runnable redesignAction) {
        List<GboardPatchesSettingsContract.Row> featureRows =
                new ArrayList<GboardPatchesSettingsContract.Row>();
        Context context = host == null ? null : host.getContext();
        for (GboardPatchesSettingsContract.Feature feature : rootFeatures) {
            featureRows.add(new GboardPatchesSettingsContract.NavigationRow(
                    feature.getEntryIcon(),
                    feature.getEntryTitle(),
                    feature.getEntrySummary(),
                    true,
                    () -> feature.openRootEntry(host)));
        }
        List<GboardPatchesSettingsContract.Row> preferenceRows =
                new ArrayList<GboardPatchesSettingsContract.Row>();
        preferenceRows.add(new GboardPatchesSettingsContract.SelectorRow(
                GboardPatchesSettingsContract.RowIcon.LANGUAGE,
                GboardSettingsText.get(context, R.string.gboard_patches_language_title),
                GboardSettingsText.get(context, R.string.gboard_patches_language_summary),
                currentLanguageLabel(context),
                true,
                () -> showLanguageDialog(host)));
        List<GboardPatchesSettingsContract.Row> aboutRows =
                new ArrayList<GboardPatchesSettingsContract.Row>();
        aboutRows.add(new GboardPatchesSettingsContract.CommandRow(
                GboardPatchesSettingsContract.RowIcon.PERSON,
                GboardSettingsText.get(context, R.string.gboard_patches_about_author_title),
                aboutAuthor,
                true,
                authorAction));
        if (!TextUtils.isEmpty(redesignAuthor)) {
            aboutRows.add(new GboardPatchesSettingsContract.CommandRow(
                    GboardPatchesSettingsContract.RowIcon.EDIT,
                    GboardSettingsText.get(context, R.string.gboard_patches_about_redesign_title),
                    redesignAuthor,
                    redesignAction != null,
                    redesignAction));
        }
        aboutRows.add(new GboardPatchesSettingsContract.CommandRow(
                GboardPatchesSettingsContract.RowIcon.INFO,
                GboardSettingsText.get(context,
                        R.string.gboard_patches_about_patch_version_title),
                patchVersion,
                true,
                patchRepositoryAction));
        return new GboardPatchesSettingsContract.Screen(
                GboardSettingsText.get(context, R.string.gboard_patches_activity_title),
                GboardSettingsText.get(context, R.string.gboard_patches_header_badge),
                GboardSettingsText.get(context, R.string.gboard_patches_header_title),
                GboardSettingsText.get(context, R.string.gboard_patches_header_summary),
                Collections.emptyList(),
                Arrays.asList(
                        new GboardPatchesSettingsContract.Section(
                                GboardSettingsText.get(context,
                                        R.string.gboard_patches_section_features),
                                featureRows),
                        new GboardPatchesSettingsContract.Section(
                                GboardSettingsText.get(context,
                                        R.string.gboard_patches_section_about),
                                aboutRows),
                        new GboardPatchesSettingsContract.Section(
                                GboardSettingsText.get(context,
                                        R.string.gboard_patches_section_preferences),
                                preferenceRows)));
    }

    private static void showLanguageDialog(GboardPatchesSettingsContract.FeatureHost host) {
        if (host == null) {
            return;
        }
        Context context = host.getContext();
        GboardPatchesSettingsContract.showChoiceDialog(host,
                GboardSettingsText.get(context, R.string.gboard_patches_language_title),
                new String[] {
                        GboardSettingsText.get(context,
                                R.string.gboard_patches_language_system_label),
                        GboardSettingsText.get(context,
                                R.string.gboard_patches_language_english_label),
                        GboardSettingsText.get(context,
                                R.string.gboard_patches_language_traditional_chinese_label)
                },
                new String[] {
                        GboardSettingsLocaleManager.LANGUAGE_SYSTEM,
                        GboardSettingsLocaleManager.LANGUAGE_ENGLISH,
                        GboardSettingsLocaleManager.LANGUAGE_TRADITIONAL_CHINESE
                },
                context == null
                        ? GboardSettingsLocaleManager.LANGUAGE_SYSTEM
                        : GboardSettingsLocaleManager.readLanguagePreference(
                                GboardPatchesSettings.preferences(context)),
                "",
                () -> {
                },
                value -> {
                    if (context == null) {
                        return;
                    }
                    GboardSettingsLocaleManager.writeLanguagePreference(context, value);
                    if (context instanceof Activity activity) {
                        activity.recreate();
                    } else {
                        GboardPatchesSettingsContract.refresh(host);
                    }
                });
    }

    private static String currentLanguageLabel(Context context) {
        SharedPreferences preferences = context == null
                ? null
                : GboardPatchesSettings.preferences(context);
        String preference = GboardSettingsLocaleManager.readLanguagePreference(preferences);
        if (GboardSettingsLocaleManager.LANGUAGE_TRADITIONAL_CHINESE.equals(preference)) {
            return GboardSettingsText.get(context,
                    R.string.gboard_patches_language_traditional_chinese_label);
        }
        if (GboardSettingsLocaleManager.LANGUAGE_ENGLISH.equals(preference)) {
            return GboardSettingsText.get(context,
                    R.string.gboard_patches_language_english_label);
        }
        String effectiveLanguage = GboardSettingsLocaleManager.resolveEffectiveLanguageTag(
                preference,
                Locale.getDefault());
        String effectiveLabel =
                GboardSettingsLocaleManager.LANGUAGE_TRADITIONAL_CHINESE.equals(effectiveLanguage)
                        ? GboardSettingsText.get(context,
                                R.string.gboard_patches_language_traditional_chinese_label)
                        : GboardSettingsText.get(context,
                                R.string.gboard_patches_language_english_label);
        return GboardSettingsText.format(context,
                R.string.gboard_patches_language_system_value,
                effectiveLabel);
    }

    private GboardPatchesSettingsContract.Screen buildFeatureErrorScreen(
            GboardPatchesSettingsContract.Feature feature) {
        String toolbarTitle = feature != null
                ? feature.getEntryTitle()
                : TOOLBAR_TITLE_PATCHES;
        List<GboardPatchesSettingsContract.StatusBlock> statusBlocks =
                new ArrayList<GboardPatchesSettingsContract.StatusBlock>();
        statusBlocks.add(new GboardPatchesSettingsContract.StatusBlock(
                text(R.string.gboard_patches_error_row_title),
                text(R.string.gboard_patches_error_row_summary),
                GboardPatchesSettingsContract.StatusTone.WARNING));
        return new GboardPatchesSettingsContract.Screen(
                toolbarTitle,
                text(R.string.gboard_patches_header_badge),
                text(R.string.gboard_patches_error_header_title),
                text(R.string.gboard_patches_error_header_summary),
                statusBlocks,
                Collections.emptyList());
    }

    private View createRowView(GboardPatchesSettingsContract.Row row) {
        if (row instanceof GboardPatchesSettingsContract.CustomViewRow customViewRow) {
            return customViewRow.getViewFactory().create(this);
        }
        if (row instanceof GboardPatchesSettingsContract.ToggleRow toggleRow) {
            return createToggleRow(toggleRow);
        }
        if (row instanceof GboardPatchesSettingsContract.NavigationRow navigationRow) {
            return createNavigationRow(navigationRow);
        }
        if (row instanceof GboardPatchesSettingsContract.SelectorRow selectorRow) {
            return createSelectorRow(selectorRow);
        }
        if (row instanceof GboardPatchesSettingsContract.DetailRow detailRow) {
            return createDetailRow(detailRow);
        }
        if (row instanceof GboardPatchesSettingsContract.DangerRow dangerRow) {
            return createDangerRow(dangerRow);
        }
        if (row instanceof GboardPatchesSettingsContract.CommandRow commandRow) {
            return createCommandRow(commandRow);
        }
        if (row instanceof GboardPatchesSettingsContract.SwitchRow switchRow) {
            return createToggleRow(new GboardPatchesSettingsContract.ToggleRow(
                    switchRow.getTitle(),
                    switchRow.getSummary(),
                    switchRow.isEnabled(),
                    switchRow.isChecked(),
                    switchRow.getToggleAction(),
                    switchRow.getPreviewSpec()));
        }
        if (row instanceof GboardPatchesSettingsContract.ActionRow actionRow) {
            if (actionRow.shouldShowChevron()) {
                return createNavigationRow(new GboardPatchesSettingsContract.NavigationRow(
                        actionRow.getTitle(),
                        actionRow.getSummary(),
                        null,
                        actionRow.isEnabled(),
                        actionRow.getAction(),
                        actionRow.getPreviewSpec()));
            }
            return createCommandRow(new GboardPatchesSettingsContract.CommandRow(
                    actionRow.getTitle(),
                    actionRow.getSummary(),
                    actionRow.isEnabled(),
                    actionRow.getAction(),
                    actionRow.getPreviewSpec()));
        }
        if (row instanceof GboardPatchesSettingsContract.InfoRow infoRow) {
            return createDetailRow(new GboardPatchesSettingsContract.DetailRow(
                    infoRow.getTitle(),
                    infoRow.getSummary(),
                    infoRow.isEnabled(),
                    isLikelyMonospace(infoRow.getSummary())));
        }
        throw new IllegalArgumentException("Unsupported row type: " + row.getClass().getName());
    }

    private View createToggleRow(GboardPatchesSettingsContract.ToggleRow rowModel) {
        LinearLayout row = buildBaseRow();
        addLeadingIcon(row, rowModel);
        LinearLayout textColumn = buildRowTextColumn(true);
        TextView titleView = buildRowTitle(rowModel.getTitle());
        TextView summaryView = buildRowSummary(rowModel.getSummary(), false);
        textColumn.addView(titleView);
        textColumn.addView(summaryView);
        LinearLayout toggleSupportLine = buildToggleSupportLine(rowModel);
        if (toggleSupportLine != null) {
            textColumn.addView(toggleSupportLine);
        }

        Switch switchView = new Switch(this);
        switchView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        switchView.setShowText(false);
        applyMaterialSwitchStyle(switchView);
        switchView.setChecked(rowModel.isChecked());
        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            markSettingsChanged();
            try {
                backgroundStateExecutor.execute(() -> {
                    try {
                        rowModel.getToggleAction().accept(isChecked);
                    } catch (Throwable throwable) {
                        Log.w(TAG, "Failed to toggle switch row", throwable);
                    }
                    postToDecorView(this::renderCurrentScreenSafely);
                });
            } catch (RejectedExecutionException ignored) {
                // Activity is already tearing down.
            }
        });

        row.addView(textColumn);
        row.addView(switchView);
        bindSwitchRowState(row, titleView, summaryView, toggleSupportLine, switchView,
                rowModel.isEnabled());
        row.setOnClickListener(view -> {
            if (row.isEnabled()) {
                runSafely("toggle switch row", switchView::toggle);
            }
        });
        return row;
    }

    private View createNavigationRow(GboardPatchesSettingsContract.NavigationRow rowModel) {
        LinearLayout row = buildBaseRow();
        addLeadingIcon(row, rowModel);
        boolean hasTrailingText = rowModel.getTrailingText() != null
                && !rowModel.getTrailingText().isEmpty();
        View previewButton = buildPreviewButton(rowModel);
        LinearLayout textColumn = buildRowTextColumn(hasTrailingText || previewButton != null);
        TextView titleView = buildRowTitle(rowModel.getTitle());
        TextView summaryView = buildRowSummary(rowModel.getSummary(), false);
        textColumn.addView(titleView);
        textColumn.addView(summaryView);
        row.addView(textColumn);
        if (hasTrailingText) {
            row.addView(buildTrailingValue(rowModel.getTrailingText()),
                    trailingLayoutParams(previewButton != null ? dp(12) : 0));
        }
        if (previewButton != null) {
            row.addView(previewButton, trailingLayoutParams(0));
        }
        bindActionRowState(row, titleView, summaryView, rowModel.isEnabled());
        row.setOnClickListener(view -> {
            if (row.isEnabled()) {
                runSafely("handle navigation row", rowModel.getAction());
            }
        });
        return row;
    }

    private View createSelectorRow(GboardPatchesSettingsContract.SelectorRow rowModel) {
        LinearLayout row = buildBaseRow();
        addLeadingIcon(row, rowModel);
        LinearLayout textColumn = buildRowTextColumn(false);
        TextView titleView = buildRowTitle(rowModel.getTitle());
        TextView summaryView = buildRowSummary(rowModel.getSummary(), false);
        textColumn.addView(titleView);
        textColumn.addView(summaryView);
        LinearLayout selectorSupportLine = buildSelectorSupportLine(rowModel);
        if (selectorSupportLine != null) {
            textColumn.addView(selectorSupportLine);
        }
        row.addView(textColumn);
        bindSelectorRowState(row, titleView, summaryView, selectorSupportLine,
                rowModel.isEnabled());
        row.setOnClickListener(view -> {
            if (row.isEnabled()) {
                runSafely("handle selector row", rowModel.getAction());
            }
        });
        return row;
    }

    private View createDetailRow(GboardPatchesSettingsContract.DetailRow rowModel) {
        LinearLayout row = buildDetailRowContainer();
        TextView titleView = buildRowTitle(rowModel.getTitle());
        TextView summaryView = buildRowSummary(rowModel.getSummary(), rowModel.isMonospace());
        row.addView(titleView);
        row.addView(summaryView);
        titleView.setTextColor(rowModel.isEnabled() ? palette.textPrimary : palette.textDisabled);
        summaryView.setTextColor(rowModel.isEnabled() ? palette.textSecondary : palette.textDisabled);
        return row;
    }

    private View createCommandRow(GboardPatchesSettingsContract.CommandRow rowModel) {
        LinearLayout row = buildBaseRow();
        addLeadingIcon(row, rowModel);
        View previewButton = buildPreviewButton(rowModel);
        LinearLayout textColumn = buildRowTextColumn(previewButton != null);
        TextView titleView = buildRowTitle(rowModel.getTitle());
        TextView summaryView = buildRowSummary(rowModel.getSummary(), false);
        textColumn.addView(titleView);
        textColumn.addView(summaryView);
        row.addView(textColumn);
        if (previewButton != null) {
            row.addView(previewButton, trailingLayoutParams(0));
        }
        bindActionRowState(row, titleView, summaryView, rowModel.isEnabled());
        if (rowModel instanceof GboardPatchesSettingsContract.DangerRow && rowModel.isEnabled()) {
            titleView.setTextColor(palette.error);
        }
        row.setOnClickListener(view -> {
            if (row.isEnabled()) {
                runSafely("handle command row", rowModel.getAction());
            }
        });
        return row;
    }

    private View createDangerRow(GboardPatchesSettingsContract.DangerRow rowModel) {
        View row = createCommandRow(rowModel);
        row.setOnClickListener(view -> {
            if (row.isEnabled()) {
                showConfirmDialog(
                        rowModel.getConfirmTitle(),
                        rowModel.getConfirmMessage(),
                        rowModel.getAction());
            }
        });
        return row;
    }

    private View createStatusBlockView(GboardPatchesSettingsContract.StatusBlock block) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(buildCardDrawable(
                toneContainerColor(block.getTone()),
                Color.TRANSPARENT,
                dp(TILE_OUTER_RADIUS_DP)));
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(12);
        card.setLayoutParams(params);

        int contentColor = toneContentColor(block.getTone());
        TextView titleView = new TextView(this);
        titleView.setText(block.getTitle());
        titleView.setTextColor(contentColor);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        titleView.setTypeface(mediumTypeface());
        titleView.setVisibility(TextUtils.isEmpty(block.getTitle()) ? View.GONE : View.VISIBLE);
        card.addView(titleView);

        TextView summaryView = buildRowSummary(block.getSummary(), false);
        summaryView.setTextColor(contentColor);
        card.addView(summaryView);
        return card;
    }

    private View createPrimaryActionButton(GboardPatchesSettingsContract.CommandRow action) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        if (!TextUtils.isEmpty(action.getSummary())) {
            TextView captionView = buildRowSummary(action.getSummary(), false);
            captionView.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams captionParams =
                    (LinearLayout.LayoutParams) captionView.getLayoutParams();
            captionParams.topMargin = 0;
            captionParams.bottomMargin = dp(12);
            container.addView(captionView, captionParams);
        }

        boolean enabled = action.isEnabled();
        TextView button = new TextView(this);
        button.setText(action.getTitle());
        button.setGravity(Gravity.CENTER);
        button.setSingleLine(true);
        button.setEllipsize(TextUtils.TruncateAt.END);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        button.setTypeface(mediumTypeface());
        button.setTextColor(enabled ? palette.onAccent : palette.textDisabled);
        button.setMinHeight(dp(56));
        button.setMinimumHeight(dp(56));
        button.setPadding(dp(24), dp(12), dp(24), dp(12));
        GradientDrawable fill = new GradientDrawable();
        fill.setColor(enabled ? palette.accent : blendAlpha(palette.textPrimary, 0.12f));
        fill.setCornerRadius(dp(28));
        button.setBackground(new RippleDrawable(
                ColorStateList.valueOf(blendAlpha(palette.onAccent, 0.16f)), fill, null));
        button.setEnabled(enabled);
        button.setOnClickListener(view -> {
            if (action instanceof GboardPatchesSettingsContract.DangerRow dangerAction) {
                showConfirmDialog(
                        dangerAction.getConfirmTitle(),
                        dangerAction.getConfirmMessage(),
                        dangerAction.getAction());
            } else {
                runSafely("handle primary action", action.getAction());
            }
        });
        container.addView(button, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return container;
    }

    private View createSectionView(GboardPatchesSettingsContract.Section section) {
        boolean fullBleed =
                section.getStyle() == GboardPatchesSettingsContract.SectionStyle.FULL_BLEED;
        boolean hasTitle = section.getTitle() != null && !section.getTitle().isEmpty();
        boolean hasDescription =
                section.getDescription() != null && !section.getDescription().isEmpty();
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        if (!fullBleed && !hasTitle && !hasDescription) {
            containerParams.topMargin = dp(16);
        }
        container.setLayoutParams(containerParams);
        if (hasTitle) {
            container.addView(buildSectionTitle(section.getTitle()));
        }
        if (hasDescription) {
            container.addView(buildSectionSummary(section.getDescription(), false));
        }
        List<GboardPatchesSettingsContract.Row> items = section.getItems();
        for (int index = 0; index < items.size(); index++) {
            View rowView = createRowView(items.get(index));
            if (!fullBleed) {
                applyTileBackground(rowView, index, items.size());
            }
            ViewGroup.LayoutParams rawParams = rowView.getLayoutParams();
            LinearLayout.LayoutParams params = rawParams instanceof LinearLayout.LayoutParams
                    ? (LinearLayout.LayoutParams) rawParams
                    : new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            if (index > 0) {
                params.topMargin = fullBleed ? dp(6) : dp(TILE_GAP_DP);
            }
            rowView.setLayoutParams(params);
            container.addView(rowView);
        }
        return container;
    }

    private void applyTileBackground(View rowView, int index, int count) {
        if (rowView.getBackground() != null) {
            // Feature-owned custom views keep their own surface.
            return;
        }
        float topRadius = dp(index == 0 ? TILE_OUTER_RADIUS_DP : TILE_INNER_RADIUS_DP);
        float bottomRadius = dp(index == count - 1
                ? TILE_OUTER_RADIUS_DP
                : TILE_INNER_RADIUS_DP);
        rowView.setBackground(buildTileDrawable(topRadius, bottomRadius));
    }

    private void addLeadingIcon(LinearLayout row, GboardPatchesSettingsContract.Row rowModel) {
        GboardPatchesSettingsContract.RowIcon icon = rowModel.getLeadingIcon();
        if (icon == null || icon == GboardPatchesSettingsContract.RowIcon.NONE) {
            return;
        }
        View iconView = new View(this) {
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Path path = new Path();

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                float iconSize = getResources().getDisplayMetrics().density * 24f;
                paint.setColor(palette.textSecondary);
                GboardMaterialIcons.draw(canvas, paint, path, icon,
                        (getWidth() - iconSize) / 2f, (getHeight() - iconSize) / 2f, iconSize);
            }
        };
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(24), dp(24));
        params.setMarginEnd(dp(20));
        iconView.setLayoutParams(params);
        iconView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        iconView.setAlpha(rowModel.isEnabled() ? 1f : 0.38f);
        row.addView(iconView, 0);
    }

    private void bindSwitchRowState(LinearLayout row, TextView titleView, TextView summaryView,
            LinearLayout supportLine, Switch switchView, boolean enabled) {
        row.setEnabled(enabled);
        row.setClickable(enabled);
        switchView.setEnabled(enabled);
        titleView.setTextColor(enabled ? palette.textPrimary : palette.textDisabled);
        summaryView.setTextColor(enabled ? palette.textSecondary : palette.textDisabled);
        if (supportLine != null) {
            for (int index = 0; index < supportLine.getChildCount(); index++) {
                View child = supportLine.getChildAt(index);
                child.setEnabled(enabled);
                child.setAlpha(enabled ? 1f : 0.38f);
            }
        }
    }

    private void applyMaterialSwitchStyle(Switch switchView) {
        GboardMaterialSwitchDrawables.Colors colors = new GboardMaterialSwitchDrawables.Colors(
                palette.accent,
                palette.onAccent,
                palette.onAccentContainer,
                palette.surfaceContainerHighest,
                palette.outline,
                palette.outline);
        float density = getResources().getDisplayMetrics().density;
        switchView.setTrackDrawable(GboardMaterialSwitchDrawables.track(colors, density));
        switchView.setThumbDrawable(GboardMaterialSwitchDrawables.thumb(colors, density));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            switchView.setThumbTintList(null);
            switchView.setTrackTintList(null);
        }
        switchView.setSwitchMinWidth(0);
        switchView.setThumbTextPadding(0);
        switchView.setBackground(null);
    }

    private void runSafely(String operationName, Runnable action) {
        try {
            action.run();
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to " + operationName, throwable);
            renderCurrentScreenSafely();
        }
    }

    private void bindActionRowState(LinearLayout row, TextView titleView, TextView summaryView,
            boolean enabled) {
        row.setEnabled(enabled);
        row.setClickable(enabled);
        titleView.setTextColor(enabled ? palette.textPrimary : palette.textDisabled);
        summaryView.setTextColor(enabled ? palette.textSecondary : palette.textDisabled);
    }

    private void bindSelectorRowState(LinearLayout row, TextView titleView, TextView summaryView,
            LinearLayout supportLine, boolean enabled) {
        bindActionRowState(row, titleView, summaryView, enabled);
        if (supportLine != null) {
            for (int index = 0; index < supportLine.getChildCount(); index++) {
                View child = supportLine.getChildAt(index);
                child.setEnabled(enabled);
                child.setAlpha(enabled ? 1f : 0.38f);
            }
        }
    }

    private LinearLayout buildBaseRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(56));
        row.setPadding(dp(16), dp(14), dp(16), dp(14));
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return row;
    }

    private LinearLayout buildDetailRowContainer() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(16), dp(14), dp(16), dp(14));
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return row;
    }

    private LinearLayout buildRowTextColumn(boolean trailingSpacing) {
        LinearLayout textColumn = new LinearLayout(this);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f);
        textParams.gravity = Gravity.CENTER_VERTICAL;
        textColumn.setLayoutParams(textParams);
        textColumn.setPadding(0, 0, trailingSpacing ? dp(16) : 0, 0);
        return textColumn;
    }

    private TextView buildRowTitle(CharSequence text) {
        TextView titleView = new TextView(this);
        titleView.setTextColor(palette.textPrimary);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        titleView.setTypeface(bodyTypeface());
        titleView.setLineSpacing(0f, 1.1f);
        titleView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        titleView.setText(text);
        return titleView;
    }

    private TextView buildRowSummary(String text, boolean monospace) {
        TextView summaryView = new TextView(this);
        summaryView.setTextColor(palette.textSecondary);
        summaryView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        summaryView.setTypeface(bodyTypeface());
        summaryView.setLineSpacing(0f, 1.1f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(2);
        summaryView.setLayoutParams(params);
        summaryView.setText(text);
        summaryView.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
        if (monospace || isLikelyMonospace(text)) {
            summaryView.setTypeface(Typeface.MONOSPACE);
            summaryView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            summaryView.setLineSpacing(0f, 1.0f);
        }
        return summaryView;
    }

    private TextView buildSectionTitle(String text) {
        TextView titleView = new TextView(this);
        titleView.setText(text);
        titleView.setTextColor(palette.accent);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        titleView.setTypeface(mediumTypeface());
        titleView.setPadding(dp(16), dp(20), dp(16), dp(8));
        return titleView;
    }

    private TextView buildSectionSummary(String text, boolean monospace) {
        TextView summaryView = new TextView(this);
        summaryView.setText(text);
        summaryView.setTextColor(palette.textSecondary);
        summaryView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        summaryView.setTypeface(bodyTypeface());
        summaryView.setLineSpacing(0f, 1.1f);
        summaryView.setPadding(dp(16), 0, dp(16), dp(12));
        summaryView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        summaryView.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
        if (monospace || isLikelyMonospace(text)) {
            summaryView.setTypeface(Typeface.MONOSPACE);
            summaryView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        }
        return summaryView;
    }

    private TextView buildTrailingValue(String text) {
        TextView valueView = new TextView(this);
        valueView.setText(text);
        valueView.setTextColor(palette.textSecondary);
        valueView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        valueView.setTypeface(bodyTypeface());
        valueView.setGravity(Gravity.CENTER_VERTICAL);
        return valueView;
    }

    private LinearLayout buildSelectorSupportLine(
            GboardPatchesSettingsContract.SelectorRow rowModel) {
        boolean hasCurrentValue =
                rowModel.getCurrentValue() != null && !rowModel.getCurrentValue().isEmpty();
        View previewButton = buildPreviewButton(rowModel);
        if (!hasCurrentValue && previewButton == null) {
            return null;
        }

        LinearLayout supportLine = new LinearLayout(this);
        supportLine.setOrientation(LinearLayout.HORIZONTAL);
        supportLine.setGravity(Gravity.CENTER_VERTICAL);
        supportLine.setBaselineAligned(false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = previewButton != null ? dp(8) : dp(2);
        supportLine.setLayoutParams(params);

        if (hasCurrentValue) {
            supportLine.addView(buildCurrentValueText(rowModel.getCurrentValue()),
                    supportChipLayoutParams(0));
        }
        if (previewButton != null) {
            supportLine.addView(previewButton,
                    supportChipLayoutParams(hasCurrentValue ? dp(12) : 0));
        }
        return supportLine;
    }

    private LinearLayout buildToggleSupportLine(
            GboardPatchesSettingsContract.ToggleRow rowModel) {
        View previewButton = buildPreviewButton(rowModel);
        if (previewButton == null) {
            return null;
        }

        LinearLayout supportLine = new LinearLayout(this);
        supportLine.setOrientation(LinearLayout.HORIZONTAL);
        supportLine.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(8);
        supportLine.setLayoutParams(params);
        supportLine.addView(previewButton);
        return supportLine;
    }

    private TextView buildCurrentValueText(String text) {
        TextView valueView = new TextView(this);
        valueView.setText(text);
        valueView.setTextColor(palette.accent);
        valueView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        valueView.setTypeface(mediumTypeface());
        valueView.setMaxLines(1);
        valueView.setEllipsize(TextUtils.TruncateAt.END);
        valueView.setMaxWidth(dp(220));
        valueView.setContentDescription(
                text(R.string.gboard_patches_current_value_label) + ": " + text);
        return valueView;
    }

    private View buildPreviewButton(GboardPatchesSettingsContract.Row rowModel) {
        GboardPatchesSettingsContract.PreviewSpec previewSpec = rowModel.getPreviewSpec();
        if (previewSpec == null) {
            return null;
        }
        TextView previewButton = new TextView(this);
        previewButton.setText(text(R.string.gboard_patches_preview_label));
        previewButton.setTextColor(palette.onSecondaryContainer);
        previewButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        previewButton.setTypeface(mediumTypeface());
        previewButton.setGravity(Gravity.CENTER_VERTICAL);
        previewButton.setSingleLine(true);
        previewButton.setMinHeight(dp(32));
        previewButton.setMinimumHeight(dp(32));
        previewButton.setPadding(dp(10), dp(4), dp(16), dp(4));
        Drawable playIcon = buildPlayIconDrawable(palette.onSecondaryContainer);
        playIcon.setBounds(0, 0, dp(18), dp(18));
        previewButton.setCompoundDrawablesRelative(playIcon, null, null, null);
        previewButton.setCompoundDrawablePadding(dp(6));
        previewButton.setBackground(buildPillDrawable(palette.secondaryContainer));
        previewButton.setOnClickListener(view ->
                runSafely("show row preview", () -> showPreviewDialog(previewSpec)));
        return previewButton;
    }

    private LinearLayout.LayoutParams trailingLayoutParams(int rightMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.rightMargin = rightMargin;
        return params;
    }

    private LinearLayout.LayoutParams supportChipLayoutParams(int leftMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = leftMargin;
        return params;
    }

    private boolean isLikelyMonospace(String summaryText) {
        return summaryText != null
                && summaryText.contains("\n")
                && (summaryText.contains("|--")
                || summaryText.contains("`--")
                || summaryText.contains("├─")
                || summaryText.contains("└─")
                || summaryText.contains("\n  |")
                || summaryText.startsWith("[PC]")
                || summaryText.startsWith("[This device]")
                || summaryText.startsWith("http://")
                || summaryText.startsWith("https://"));
    }

    private int toneContainerColor(GboardPatchesSettingsContract.StatusTone tone) {
        if (tone == GboardPatchesSettingsContract.StatusTone.WARNING) {
            return palette.warningContainer;
        }
        if (tone == GboardPatchesSettingsContract.StatusTone.INFO) {
            return palette.secondaryContainer;
        }
        return palette.surfaceContainerHigh;
    }

    private int toneContentColor(GboardPatchesSettingsContract.StatusTone tone) {
        if (tone == GboardPatchesSettingsContract.StatusTone.WARNING) {
            return palette.onWarningContainer;
        }
        if (tone == GboardPatchesSettingsContract.StatusTone.INFO) {
            return palette.onSecondaryContainer;
        }
        return palette.textPrimary;
    }

    private Drawable buildPillDrawable(int fillColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(dp(999));
        return new RippleDrawable(ColorStateList.valueOf(palette.pressedOverlay), drawable, null);
    }

    private Drawable buildPlayIconDrawable(int color) {
        Path playPath = new Path();
        playPath.moveTo(8.5f, 6f);
        playPath.lineTo(18.5f, 12f);
        playPath.lineTo(8.5f, 18f);
        playPath.close();
        ShapeDrawable drawable = new ShapeDrawable(new PathShape(playPath, 24f, 24f));
        Paint paint = drawable.getPaint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(2f);
        return drawable;
    }

    private void showConfirmDialog(String title, String message, String confirmLabel,
            Runnable confirmAction) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(confirmLabel,
                        (dialogInterface, which) ->
                        runSafely("confirm settings action", confirmAction))
                .setNegativeButton(
                        text(R.string.gboard_patches_dialog_cancel),
                        null)
                .create();
        dialog.setOnDismissListener(ignored -> onManagedDialogDismissed());
        try {
            dialog.show();
            onManagedDialogShown();
            tintDialogButtons(dialog);
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to show confirmation dialog", throwable);
        }
    }

    private void showConfirmDialog(String title, String message, Runnable confirmAction) {
        showConfirmDialog(title, message, text(R.string.gboard_patches_confirm_action),
                confirmAction);
    }

    private void tintDialogButtons(AlertDialog dialog) {
        TextView positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(palette.accent);
        }
        TextView negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (negativeButton != null) {
            negativeButton.setTextColor(palette.accent);
        }
        TextView neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        if (neutralButton != null) {
            neutralButton.setTextColor(palette.accent);
        }
    }

    private int parsePositiveInteger(String value) {
        if (value == null) {
            return -1;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : -1;
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private int resolveCheckedIndex(String[] values, String currentValue) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(currentValue)) {
                return i;
            }
        }
        return -1;
    }

    private Drawable buildCardDrawable(int fillColor, int strokeColor, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(radiusDp);
        if (Color.alpha(strokeColor) > 0) {
            drawable.setStroke(dp(1), strokeColor);
        }
        return drawable;
    }

    private Drawable buildTileDrawable(float topRadius, float bottomRadius) {
        float[] radii = new float[] {
                topRadius, topRadius, topRadius, topRadius,
                bottomRadius, bottomRadius, bottomRadius, bottomRadius
        };
        GradientDrawable content = new GradientDrawable();
        content.setColor(palette.surface);
        content.setCornerRadii(radii);
        GradientDrawable mask = new GradientDrawable();
        mask.setColor(Color.WHITE);
        mask.setCornerRadii(radii);
        return new RippleDrawable(ColorStateList.valueOf(palette.pressedOverlay), content, mask);
    }

    private Drawable buildRippleDrawable(int radiusDp) {
        GradientDrawable mask = new GradientDrawable();
        mask.setColor(Color.WHITE);
        mask.setCornerRadius(radiusDp);
        return new RippleDrawable(
                ColorStateList.valueOf(palette.pressedOverlay),
                null,
                mask);
    }

    private int blendAlpha(int color, float alphaFraction) {
        int alpha = Math.round(Color.alpha(color) * alphaFraction);
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private String text(int resId) {
        return GboardSettingsText.get(this, resId);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private Typeface headlineTypeface() {
        if (headlineTypeface == null) {
            headlineTypeface = themeTypeface(deviceDefaultHeadlineAppearance(), 400);
        }
        return headlineTypeface;
    }

    private Typeface bodyTypeface() {
        if (bodyTypeface == null) {
            bodyTypeface = themeTypeface(android.R.style.TextAppearance_DeviceDefault, 400);
        }
        return bodyTypeface;
    }

    private Typeface mediumTypeface() {
        if (mediumTypeface == null) {
            mediumTypeface = themeTypeface(android.R.style.TextAppearance_DeviceDefault, 500);
        }
        return mediumTypeface;
    }

    // Looked up by name: the headline appearance is newer than the compile SDK may be.
    @SuppressLint("DiscouragedApi")
    private int deviceDefaultHeadlineAppearance() {
        int headline = getResources().getIdentifier(
                "TextAppearance.DeviceDefault.Headline", "style", "android");
        return headline != 0 ? headline : android.R.style.TextAppearance_DeviceDefault_Large;
    }

    /** Uses Google Sans where the device ships it (Pixel), otherwise the default sans serif. */
    /**
     * Uses the font of the system's DeviceDefault text styles, so the page follows the device
     * font (Google Sans on Pixel) like Gboard's own settings.
     */
    private Typeface themeTypeface(int textAppearance, int weight) {
        Typeface base = null;
        try {
            TextView probe = new TextView(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                probe.setTextAppearance(textAppearance);
            } else {
                probe.setTextAppearance(this, textAppearance);
            }
            base = probe.getTypeface();
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to resolve the system font", throwable);
        }
        if (base == null) {
            base = Typeface.DEFAULT;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return Typeface.create(base, weight, false);
        }
        return weight >= 500 ? Typeface.create(base, Typeface.BOLD) : base;
    }

    private static final class PendingTextDocumentWrite {
        final String text;
        final Runnable completionAction;

        PendingTextDocumentWrite(String text, Runnable completionAction) {
            this.text = text;
            this.completionAction = completionAction;
        }
    }

    private static final class PendingBinaryDocumentWrite {
        final byte[] data;
        final Runnable completionAction;

        PendingBinaryDocumentWrite(byte[] data, Runnable completionAction) {
            this.data = data;
            this.completionAction = completionAction;
        }
    }

    /** Material 3 color roles, taken from the wallpaper (Material You) when available. */
    private static final class Palette {
        final int windowBackground;
        final int surface;
        final int surfaceContainerHigh;
        final int surfaceContainerHighest;
        final int textPrimary;
        final int textSecondary;
        final int textDisabled;
        final int outline;
        final int accent;
        final int onAccent;
        final int accentContainer;
        final int onAccentContainer;
        final int secondaryContainer;
        final int onSecondaryContainer;
        final int error;
        final int warningContainer;
        final int onWarningContainer;
        final int pressedOverlay;

        private Palette(Roles roles) {
            this.windowBackground = roles.surfaceContainer;
            this.surface = roles.tile;
            this.surfaceContainerHigh = roles.surfaceContainerHigh;
            this.surfaceContainerHighest = roles.surfaceContainerHighest;
            this.textPrimary = roles.onSurface;
            this.textSecondary = roles.onSurfaceVariant;
            this.textDisabled = withAlpha(roles.onSurface, 0.38f);
            this.outline = roles.outline;
            this.accent = roles.primary;
            this.onAccent = roles.onPrimary;
            this.accentContainer = roles.primaryContainer;
            this.onAccentContainer = roles.onPrimaryContainer;
            this.secondaryContainer = roles.secondaryContainer;
            this.onSecondaryContainer = roles.onSecondaryContainer;
            this.error = roles.error;
            this.warningContainer = roles.warningContainer;
            this.onWarningContainer = roles.onWarningContainer;
            this.pressedOverlay = withAlpha(roles.onSurface, 0.10f);
        }

        static Palette forConfiguration(Context context, Configuration configuration) {
            boolean nightMode = (configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK)
                    == Configuration.UI_MODE_NIGHT_YES;
            Roles roles = baselineRoles(nightMode);
            try {
                if (Build.VERSION.SDK_INT >= 34) {
                    applySystemRoles(context, roles, nightMode);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    applySystemTonalPalette(context, roles, nightMode);
                }
            } catch (Throwable throwable) {
                Log.w(TAG, "Dynamic colors unavailable; using baseline palette", throwable);
                roles = baselineRoles(nightMode);
            }
            return new Palette(roles);
        }

        /** Gboard blue (tonal spot) for devices without dynamic color. */
        private static Roles baselineRoles(boolean nightMode) {
            Roles roles = new Roles();
            if (nightMode) {
                roles.surfaceContainer = Color.parseColor("#FF1D2024");
                roles.tile = Color.parseColor("#FF37393E");
                roles.surfaceContainerHigh = Color.parseColor("#FF282A2F");
                roles.surfaceContainerHighest = Color.parseColor("#FF33353A");
                roles.onSurface = Color.parseColor("#FFE2E2E9");
                roles.onSurfaceVariant = Color.parseColor("#FFC4C6D0");
                roles.outline = Color.parseColor("#FF8E9099");
                roles.primary = Color.parseColor("#FFAAC7FF");
                roles.onPrimary = Color.parseColor("#FF0A305F");
                roles.primaryContainer = Color.parseColor("#FF284777");
                roles.onPrimaryContainer = Color.parseColor("#FFD6E3FF");
                roles.secondaryContainer = Color.parseColor("#FF3E4759");
                roles.onSecondaryContainer = Color.parseColor("#FFDAE2F9");
                roles.error = Color.parseColor("#FFFFB4AB");
                roles.warningContainer = Color.parseColor("#FF5C4200");
                roles.onWarningContainer = Color.parseColor("#FFFFDEA8");
            } else {
                roles.surfaceContainer = Color.parseColor("#FFEDEDF4");
                roles.tile = Color.parseColor("#FFF9F9FF");
                roles.surfaceContainerHigh = Color.parseColor("#FFE7E8EE");
                roles.surfaceContainerHighest = Color.parseColor("#FFE2E2E9");
                roles.onSurface = Color.parseColor("#FF191C20");
                roles.onSurfaceVariant = Color.parseColor("#FF44474E");
                roles.outline = Color.parseColor("#FF74777F");
                roles.primary = Color.parseColor("#FF415F91");
                roles.onPrimary = Color.parseColor("#FFFFFFFF");
                roles.primaryContainer = Color.parseColor("#FFD6E3FF");
                roles.onPrimaryContainer = Color.parseColor("#FF284777");
                roles.secondaryContainer = Color.parseColor("#FFDAE2F9");
                roles.onSecondaryContainer = Color.parseColor("#FF3E4759");
                roles.error = Color.parseColor("#FFBA1A1A");
                roles.warningContainer = Color.parseColor("#FFFFDEA8");
                roles.onWarningContainer = Color.parseColor("#FF5C4200");
            }
            return roles;
        }

        /** Android 14+ publishes the Material 3 color roles directly. */
        private static void applySystemRoles(Context context, Roles roles, boolean nightMode) {
            String suffix = nightMode ? "_dark" : "_light";
            roles.surfaceContainer = systemColor(context, "system_surface_container" + suffix);
            roles.tile = systemColor(context, "system_surface_bright" + suffix);
            roles.surfaceContainerHigh = systemColor(context, nightMode
                    ? "system_surface_container_high_dark"
                    : "system_surface_container_high_light");
            roles.surfaceContainerHighest =
                    systemColor(context, "system_surface_container_highest" + suffix);
            roles.onSurface = systemColor(context, "system_on_surface" + suffix);
            roles.onSurfaceVariant = systemColor(context, "system_on_surface_variant" + suffix);
            roles.outline = systemColor(context, "system_outline" + suffix);
            roles.primary = systemColor(context, "system_primary" + suffix);
            roles.onPrimary = systemColor(context, "system_on_primary" + suffix);
            roles.primaryContainer = systemColor(context, "system_primary_container" + suffix);
            roles.onPrimaryContainer =
                    systemColor(context, "system_on_primary_container" + suffix);
            roles.secondaryContainer =
                    systemColor(context, "system_secondary_container" + suffix);
            roles.onSecondaryContainer =
                    systemColor(context, "system_on_secondary_container" + suffix);
            roles.error = systemColor(context, "system_error" + suffix);
        }

        /** Android 12 and 13 only publish tonal palettes, so map roles to their tones. */
        private static void applySystemTonalPalette(Context context, Roles roles,
                boolean nightMode) {
            if (nightMode) {
                roles.surfaceContainer = systemColor(context, "system_neutral1_900");
                roles.tile = systemColor(context, "system_neutral1_800");
                roles.surfaceContainerHigh = systemColor(context, "system_neutral1_800");
                roles.surfaceContainerHighest = systemColor(context, "system_neutral1_700");
                roles.onSurface = systemColor(context, "system_neutral1_100");
                roles.onSurfaceVariant = systemColor(context, "system_neutral2_200");
                roles.outline = systemColor(context, "system_neutral2_400");
                roles.primary = systemColor(context, "system_accent1_200");
                roles.onPrimary = systemColor(context, "system_accent1_800");
                roles.primaryContainer = systemColor(context, "system_accent1_700");
                roles.onPrimaryContainer = systemColor(context, "system_accent1_100");
                roles.secondaryContainer = systemColor(context, "system_accent2_700");
                roles.onSecondaryContainer = systemColor(context, "system_accent2_100");
            } else {
                roles.surfaceContainer = systemColor(context, "system_neutral1_50");
                roles.tile = systemColor(context, "system_neutral1_10");
                roles.surfaceContainerHigh = systemColor(context, "system_neutral1_100");
                roles.surfaceContainerHighest = systemColor(context, "system_neutral1_100");
                roles.onSurface = systemColor(context, "system_neutral1_900");
                roles.onSurfaceVariant = systemColor(context, "system_neutral2_700");
                roles.outline = systemColor(context, "system_neutral2_500");
                roles.primary = systemColor(context, "system_accent1_600");
                roles.onPrimary = systemColor(context, "system_accent1_0");
                roles.primaryContainer = systemColor(context, "system_accent1_100");
                roles.onPrimaryContainer = systemColor(context, "system_accent1_900");
                roles.secondaryContainer = systemColor(context, "system_accent2_100");
                roles.onSecondaryContainer = systemColor(context, "system_accent2_900");
            }
        }

        // Looked up by name so the extension does not depend on the compile SDK level.
        @SuppressLint("DiscouragedApi")
        private static int systemColor(Context context, String name) {
            int resourceId = context.getResources().getIdentifier(name, "color", "android");
            if (resourceId == 0) {
                throw new IllegalStateException("Missing system color " + name);
            }
            return context.getColor(resourceId);
        }

        private static int withAlpha(int color, float alphaFraction) {
            int alpha = Math.round(Color.alpha(color) * alphaFraction);
            return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
        }

        private static final class Roles {
            int surfaceContainer;
            int tile;
            int surfaceContainerHigh;
            int surfaceContainerHighest;
            int onSurface;
            int onSurfaceVariant;
            int outline;
            int primary;
            int onPrimary;
            int primaryContainer;
            int onPrimaryContainer;
            int secondaryContainer;
            int onSecondaryContainer;
            int error;
            int warningContainer;
            int onWarningContainer;
        }
    }
}

