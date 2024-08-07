package eu.toldi.infinityforlemmy;

import static android.util.Log.e;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.evernote.android.state.StateSaver;
import com.livefront.bridge.Bridge;
import com.livefront.bridge.SavedStateHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Named;

import eu.toldi.infinityforlemmy.activities.LockScreenActivity;
import eu.toldi.infinityforlemmy.broadcastreceivers.NetworkWifiStatusReceiver;
import eu.toldi.infinityforlemmy.broadcastreceivers.WallpaperChangeReceiver;
import eu.toldi.infinityforlemmy.events.ChangeAppLockEvent;
import eu.toldi.infinityforlemmy.events.ChangeNetworkStatusEvent;
import eu.toldi.infinityforlemmy.events.ToggleSecureModeEvent;
import eu.toldi.infinityforlemmy.font.ContentFontFamily;
import eu.toldi.infinityforlemmy.font.FontFamily;
import eu.toldi.infinityforlemmy.font.TitleFontFamily;
import eu.toldi.infinityforlemmy.settings.SettheImqaFragment;
import eu.toldi.infinityforlemmy.utils.SharedPreferencesUtils;
import eu.toldi.infinityforlemmy.utils.Utils;
import io.imqa.core.IMQAOption;
import io.imqa.core.logs.IdentifierCollector;

import io.imqa.crash.IMQACrashAgent;
import io.imqa.crash.report.ErrorRank;
import okhttp3.Call;
import okhttp3.OkHttpClient;

public class Infinity extends Application implements LifecycleObserver {
    public Typeface typeface;
    public Typeface titleTypeface;
    public Typeface contentTypeface;
    private AppComponent mAppComponent;
    private NetworkWifiStatusReceiver mNetworkWifiStatusReceiver;
    private boolean appLock;
    private long appLockTimeout;
    private boolean canStartLockScreenActivity = false;
    private boolean isSecureMode;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;

    @Inject
    @Named("security")
    SharedPreferences mSecuritySharedPreferences;
    @Inject
    @Named("glide")
    OkHttpClient glideOkHttpClient;
    @Override
    public void onCreate() {
        super.onCreate();

        mAppComponent = DaggerAppComponent.factory()
                .create(this);

        mAppComponent.inject(this);
        String serverValue = mSharedPreferences.getString("edit_server", "");
        String keyValue = mSharedPreferences.getString("edit_project", "");
        String IDValue = mSharedPreferences.getString("edit_customID", "");
        String EmailValue = mSharedPreferences.getString("edit_customEmail", "");
        String NameValue = mSharedPreferences.getString("edit_customName", "");

        io.imqa.core.IMQAOption imqaOption = new io.imqa.core.IMQAOption();

        imqaOption.setBuildType(false);
        imqaOption.setUploadPeriod(true);
        imqaOption.setCrashDirectUploadFlag(true);
        imqaOption.setKeepFileAtUploadFail(false);

        imqaOption.setHttpTracing(true);
        imqaOption.setPrintLog(true);
        imqaOption.setBehaviorTracing(true);
        imqaOption.setEventTracing(true);
        imqaOption.setDumpInterval(30000);
        imqaOption.setFileInterval(5);
//
        imqaOption.setServerUrl(serverValue);
        imqaOption.setCrashServerUrl(serverValue);


        io.imqa.mpm.IMQAMpmAgent.getInstance()
                .setOption(imqaOption)
                .setContext(this, "")
                .setProjectKey(keyValue)
                .init();
        Log.d("SERVERRRRR", imqaOption.getServerUrl());


     //   IMQACrashAgent.sendCustomException(new Exception("d"));
        im

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                IdentifierCollector.setCustomUserId(IDValue);
                IdentifierCollector.setCustomUserName(NameValue);
                IdentifierCollector.setCustomUserMail(EmailValue);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        Log.d("IMQAAAA",imqaOption.getProjectKey());
        Log.d("IMQAAAA",imqaOption.getServerUrl());

        appLock = mSecuritySharedPreferences.getBoolean(SharedPreferencesUtils.APP_LOCK, false);
        appLockTimeout = Long.parseLong(mSecuritySharedPreferences.getString(SharedPreferencesUtils.APP_LOCK_TIMEOUT, "600000"));
        isSecureMode = mSecuritySharedPreferences.getBoolean(SharedPreferencesUtils.SECURE_MODE, false);

        try {
            if (mSharedPreferences.getString(SharedPreferencesUtils.FONT_FAMILY_KEY, FontFamily.Default.name()).equals(FontFamily.Custom.name())) {
                typeface = Typeface.createFromFile(getExternalFilesDir("fonts") + "/font_family.ttf");
            }
            if (mSharedPreferences.getString(SharedPreferencesUtils.TITLE_FONT_FAMILY_KEY, TitleFontFamily.Default.name()).equals(TitleFontFamily.Custom.name())) {
                titleTypeface = Typeface.createFromFile(getExternalFilesDir("fonts") + "/title_font_family.ttf");
            }
            if (mSharedPreferences.getString(SharedPreferencesUtils.CONTENT_FONT_FAMILY_KEY, ContentFontFamily.Default.name()).equals(ContentFontFamily.Custom.name())) {
                contentTypeface = Typeface.createFromFile(getExternalFilesDir("fonts") + "/content_font_family.ttf");
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.unable_to_load_font, Toast.LENGTH_SHORT).show();
        }

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                if (activity instanceof CustomFontReceiver) {
                    ((CustomFontReceiver) activity).setCustomFont(typeface, titleTypeface, contentTypeface);
                }
            }

            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
                if (isSecureMode) {
                    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                if (canStartLockScreenActivity && appLock
                        && System.currentTimeMillis() - mSecuritySharedPreferences.getLong(SharedPreferencesUtils.LAST_FOREGROUND_TIME, 0) >= appLockTimeout
                        && !(activity instanceof LockScreenActivity)) {
                    Intent intent = new Intent(activity, LockScreenActivity.class);
                    activity.startActivity(intent);
                }
                canStartLockScreenActivity = false;
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }
        });

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        Bridge.initialize(getApplicationContext(), new SavedStateHandler() {
            @Override
            public void saveInstanceState(@NonNull Object target, @NonNull Bundle state) {
                StateSaver.saveInstanceState(target, state);
            }

            @Override
            public void restoreInstanceState(@NonNull Object target, @Nullable Bundle state) {
                StateSaver.restoreInstanceState(target, state);
            }
        });

        EventBus.builder().addIndex(new EventBusIndex()).installDefaultEventBus();

        EventBus.getDefault().register(this);

        mNetworkWifiStatusReceiver =
                new NetworkWifiStatusReceiver(() -> EventBus.getDefault().post(new ChangeNetworkStatusEvent(Utils.getConnectedNetwork(getApplicationContext()))));
        registerReceiver(mNetworkWifiStatusReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        registerReceiver(new WallpaperChangeReceiver(mSharedPreferences), new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED));

        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory((Call.Factory) glideOkHttpClient);
        Glide.get(this).getRegistry().replace(GlideUrl.class, InputStream.class, factory);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void appInForeground() {
        canStartLockScreenActivity = true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void appInBackground() {
        if (appLock) {
            mSecuritySharedPreferences.edit().putLong(SharedPreferencesUtils.LAST_FOREGROUND_TIME, System.currentTimeMillis()).apply();
        }
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }

    @Subscribe
    public void onToggleSecureModeEvent(ToggleSecureModeEvent secureModeEvent) {
        isSecureMode = secureModeEvent.isSecureMode;
    }

    @Subscribe
    public void onChangeAppLockEvent(ChangeAppLockEvent changeAppLockEvent) {
        appLock = changeAppLockEvent.appLock;
        appLockTimeout = changeAppLockEvent.appLockTimeout;
    }
}
