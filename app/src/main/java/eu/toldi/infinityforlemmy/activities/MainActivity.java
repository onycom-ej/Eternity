package eu.toldi.infinityforlemmy.activities;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.toldi.infinityforlemmy.ActivityToolbarInterface;
import eu.toldi.infinityforlemmy.FetchSubscribedThing;
import eu.toldi.infinityforlemmy.Infinity;
import eu.toldi.infinityforlemmy.MarkPostAsReadInterface;
import eu.toldi.infinityforlemmy.PullNotificationWorker;
import eu.toldi.infinityforlemmy.R;
import eu.toldi.infinityforlemmy.RecyclerViewContentScrollingInterface;
import eu.toldi.infinityforlemmy.RedditDataRoomDatabase;
import eu.toldi.infinityforlemmy.RetrofitHolder;
import eu.toldi.infinityforlemmy.SortType;
import eu.toldi.infinityforlemmy.SortTypeSelectionCallback;
import eu.toldi.infinityforlemmy.account.AccountViewModel;
import eu.toldi.infinityforlemmy.adapters.SubredditAutocompleteRecyclerViewAdapter;
import eu.toldi.infinityforlemmy.adapters.navigationdrawer.NavigationDrawerRecyclerViewMergedAdapter;
import eu.toldi.infinityforlemmy.apis.RedditAPI;
import eu.toldi.infinityforlemmy.asynctasks.InsertSubscribedThings;
import eu.toldi.infinityforlemmy.asynctasks.SwitchAccount;
import eu.toldi.infinityforlemmy.asynctasks.SwitchToAnonymousMode;
import eu.toldi.infinityforlemmy.bottomsheetfragments.FABMoreOptionsBottomSheetFragment;
import eu.toldi.infinityforlemmy.bottomsheetfragments.PostLayoutBottomSheetFragment;
import eu.toldi.infinityforlemmy.bottomsheetfragments.PostTypeBottomSheetFragment;
import eu.toldi.infinityforlemmy.bottomsheetfragments.RandomBottomSheetFragment;
import eu.toldi.infinityforlemmy.bottomsheetfragments.SortTimeBottomSheetFragment;
import eu.toldi.infinityforlemmy.bottomsheetfragments.SortTypeBottomSheetFragment;
import eu.toldi.infinityforlemmy.customtheme.CustomThemeWrapper;
import eu.toldi.infinityforlemmy.customviews.LinearLayoutManagerBugFixed;
import eu.toldi.infinityforlemmy.customviews.NavigationWrapper;
import eu.toldi.infinityforlemmy.events.ChangeDisableSwipingBetweenTabsEvent;
import eu.toldi.infinityforlemmy.events.ChangeHideFabInPostFeedEvent;
import eu.toldi.infinityforlemmy.events.ChangeHideKarmaEvent;
import eu.toldi.infinityforlemmy.events.ChangeInboxCountEvent;
import eu.toldi.infinityforlemmy.events.ChangeLockBottomAppBarEvent;
import eu.toldi.infinityforlemmy.events.ChangeNSFWEvent;
import eu.toldi.infinityforlemmy.events.ChangeRequireAuthToAccountSectionEvent;
import eu.toldi.infinityforlemmy.events.ChangeShowAvatarOnTheRightInTheNavigationDrawerEvent;
import eu.toldi.infinityforlemmy.events.ChangeUseCircularFabEvent;
import eu.toldi.infinityforlemmy.events.RecreateActivityEvent;
import eu.toldi.infinityforlemmy.events.SwitchAccountEvent;
import eu.toldi.infinityforlemmy.fragments.PostFragment;
import eu.toldi.infinityforlemmy.message.ReadMessage;
import eu.toldi.infinityforlemmy.multireddit.MultiReddit;
import eu.toldi.infinityforlemmy.multireddit.MultiRedditViewModel;
import eu.toldi.infinityforlemmy.post.MarkPostAsRead;
import eu.toldi.infinityforlemmy.post.Post;
import eu.toldi.infinityforlemmy.post.PostPagingSource;
import eu.toldi.infinityforlemmy.readpost.InsertReadPost;
import eu.toldi.infinityforlemmy.site.FetchSiteInfo;
import eu.toldi.infinityforlemmy.site.SiteInfo;
import eu.toldi.infinityforlemmy.subreddit.ParseSubredditData;
import eu.toldi.infinityforlemmy.subreddit.SubredditData;
import eu.toldi.infinityforlemmy.subscribedsubreddit.SubscribedSubredditData;
import eu.toldi.infinityforlemmy.subscribedsubreddit.SubscribedSubredditViewModel;
import eu.toldi.infinityforlemmy.subscribeduser.SubscribedUserData;
import eu.toldi.infinityforlemmy.user.FetchUserData;
import eu.toldi.infinityforlemmy.user.MyUserInfo;
import eu.toldi.infinityforlemmy.user.UserData;
import eu.toldi.infinityforlemmy.utils.APIUtils;
import eu.toldi.infinityforlemmy.utils.CustomThemeSharedPreferencesUtils;
import eu.toldi.infinityforlemmy.utils.LemmyUtils;
import eu.toldi.infinityforlemmy.utils.SharedPreferencesUtils;
import eu.toldi.infinityforlemmy.utils.Utils;
import io.imqa.core.logs.IdentifierCollector;
import io.imqa.mpm.IMQAMpmAgent;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends BaseActivity implements SortTypeSelectionCallback,
        PostTypeBottomSheetFragment.PostTypeSelectionCallback, PostLayoutBottomSheetFragment.PostLayoutSelectionCallback,
        ActivityToolbarInterface, FABMoreOptionsBottomSheetFragment.FABOptionSelectionCallback,
        RandomBottomSheetFragment.RandomOptionSelectionCallback, MarkPostAsReadInterface, RecyclerViewContentScrollingInterface {





    static final String EXTRA_MESSSAGE_FULLNAME = "ENF";
    static final String EXTRA_NEW_ACCOUNT_NAME = "ENAN";

    private static final String FETCH_USER_INFO_STATE = "FUIS";
    private static final String FETCH_SUBSCRIPTIONS_STATE = "FSS";
    private static final String DRAWER_ON_ACCOUNT_SWITCH_STATE = "DOASS";
    private static final String MESSAGE_FULLNAME_STATE = "MFS";
    private static final String NEW_ACCOUNT_NAME_STATE = "NANS";
    private static final String INBOX_COUNT_STATE = "ICS";

    private static final int LOGIN_ACTIVITY_REQUEST_CODE = 0;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.navigation_view_main_activity)
    NavigationView navigationView;
    @BindView(R.id.coordinator_layout_main_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_main_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.view_pager_main_activity)
    ViewPager2 viewPager2;
    @BindView(R.id.collapsing_toolbar_layout_main_activity)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar)
    MaterialToolbar toolbar;
    @BindView(R.id.nav_drawer_recycler_view_main_activity)
    RecyclerView navDrawerRecyclerView;
    @BindView(R.id.tab_layout_main_activity)
    TabLayout tabLayout;
    MultiRedditViewModel multiRedditViewModel;
    SubscribedSubredditViewModel subscribedSubredditViewModel;
    AccountViewModel accountViewModel;
    @Inject
    @Named("no_oauth")
    RetrofitHolder mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("sort_type")
    SharedPreferences mSortTypeSharedPreferences;
    @Inject
    @Named("post_layout")
    SharedPreferences mPostLayoutSharedPreferences;
    @Inject
    @Named("main_activity_tabs")
    SharedPreferences mMainActivityTabsSharedPreferences;
    @Inject
    @Named("nsfw_and_spoiler")
    SharedPreferences mNsfwAndSpoilerSharedPreferences;
    @Inject
    @Named("bottom_app_bar")
    SharedPreferences mBottomAppBarSharedPreference;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    @Named("navigation_drawer")
    SharedPreferences mNavigationDrawerSharedPreferences;
    @Inject
    @Named("security")
    SharedPreferences mSecuritySharedPreferences;
    @Inject
    @Named("internal")
    SharedPreferences mInternalSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;

    @Inject
    MarkPostAsRead markPostAsRead;

    private FragmentManager fragmentManager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private NavigationDrawerRecyclerViewMergedAdapter adapter;
    private NavigationWrapper navigationWrapper;
    private Call<String> subredditAutocompleteCall;
    private String mAccessToken;
    private String mAccountName;

    private String mAccountQualifiedName;
    private boolean mFetchUserInfoSuccess = false;
    private boolean mFetchSubscriptionsSuccess = false;
    private boolean mDrawerOnAccountSwitch = false;
    private int mMessageFullname;
    private String mNewAccountName;
    private boolean hideFab;

    private boolean useCircularFab;
    private boolean showBottomAppBar;
    private int mBackButtonAction;
    private boolean mLockBottomAppBar;
    private boolean mDisableSwipingBetweenTabs;
    private boolean mShowFavoriteMultiReddits;
    private boolean mShowMultiReddits;
    private boolean mShowFavoriteSubscribedSubreddits;
    private boolean mShowSubscribedSubreddits;
    private int fabOption;
    private int inboxCount;

    private boolean mBearerTokenUsed = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //.installSplashScreen(this);

        ((Infinity) getApplication()).getAppComponent().inject(this);

        setTheme(R.style.AppTheme_NoActionBarWithTransparentStatusBar);

        setHasDrawerLayout();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);



//        try {
//            // JNI 함수 호출
//            triggerNativeError();
//        } catch (Exception e) {
//            // JNI 호출 중 발생한 예외 처리
//            Log.d("MainActvity", "JNI ERROR " + e.getMessage());
//            //textView.setText("JNI 에러 발생: " + e.getMessage());
//            e.printStackTrace();
//        }

        ButterKnife.bind(this);

        hideFab = mSharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_FAB_IN_POST_FEED, false);
        useCircularFab = mSharedPreferences.getBoolean(SharedPreferencesUtils.USE_CIRCULAR_FAB, false);
        showBottomAppBar = mSharedPreferences.getBoolean(SharedPreferencesUtils.BOTTOM_APP_BAR_KEY, false);

        navigationWrapper = new NavigationWrapper(findViewById(R.id.bottom_app_bar_bottom_app_bar), findViewById(R.id.linear_layout_bottom_app_bar),
                findViewById(R.id.option_1_bottom_app_bar), findViewById(R.id.option_2_bottom_app_bar),
                findViewById(R.id.option_3_bottom_app_bar), findViewById(R.id.option_4_bottom_app_bar),
                findViewById(R.id.fab_main_activity),
                findViewById(R.id.navigation_rail), showBottomAppBar);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(appBarLayout);
            }

            if (isImmersiveInterface()) {
                drawer.setStatusBarBackgroundColor(Color.TRANSPARENT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    drawer.setFitsSystemWindows(false);
                    window.setDecorFitsSystemWindows(false);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(toolbar);

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    if (navigationWrapper.navigationRailView == null) {
                        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) navigationWrapper.floatingActionButton.getLayoutParams();
                        params.bottomMargin += navBarHeight;
                        navigationWrapper.floatingActionButton.setLayoutParams(params);
                    }
                    if (navigationWrapper.bottomAppBar != null) {
                        navigationWrapper.linearLayoutBottomAppBar.setPadding(navigationWrapper.linearLayoutBottomAppBar.getPaddingLeft(),
                                navigationWrapper.linearLayoutBottomAppBar.getPaddingTop(), navigationWrapper.linearLayoutBottomAppBar.getPaddingRight(), navBarHeight);
                    }
                    navDrawerRecyclerView.setPadding(0, 0, 0, navBarHeight);
                }
            } else {
                drawer.setStatusBarBackgroundColor(mCustomThemeWrapper.getColorPrimaryDark());
            }
        }

        setSupportActionBar(toolbar);
        setToolbarGoToTop(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor());
        drawer.addDrawerListener(toggle);
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                if (adapter != null) {
                    adapter.closeAccountSectionWithoutChangeIconResource(true);
                }
            }
        });
        toggle.syncState();

        mViewPager2 = viewPager2;

        mBackButtonAction = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.MAIN_PAGE_BACK_BUTTON_ACTION, "0"));
        mLockBottomAppBar = mSharedPreferences.getBoolean(SharedPreferencesUtils.LOCK_BOTTOM_APP_BAR, false);
        mDisableSwipingBetweenTabs = mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_SWIPING_BETWEEN_TABS, false);

        fragmentManager = getSupportFragmentManager();

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        mRetrofit.setAccessToken(mAccessToken);
        mBearerTokenUsed = mCurrentAccountSharedPreferences.getBoolean(SharedPreferencesUtils.BEARER_TOKEN_AUTH, true);

        mAccountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, null);
        mAccountQualifiedName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_QUALIFIED_NAME, null);
        String instance = (mAccessToken == null) ? mSharedPreferences.getString(SharedPreferencesUtils.ANONYMOUS_ACCOUNT_INSTANCE, APIUtils.API_BASE_URI) : mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_INSTANCE, null);
        if (instance != null) {
            mRetrofit.setBaseURL(instance);
        }

        if (savedInstanceState != null) {
            mFetchUserInfoSuccess = savedInstanceState.getBoolean(FETCH_USER_INFO_STATE);
            mFetchSubscriptionsSuccess = savedInstanceState.getBoolean(FETCH_SUBSCRIPTIONS_STATE);
            mDrawerOnAccountSwitch = savedInstanceState.getBoolean(DRAWER_ON_ACCOUNT_SWITCH_STATE);
            mMessageFullname = savedInstanceState.getInt(MESSAGE_FULLNAME_STATE);
            mNewAccountName = savedInstanceState.getString(NEW_ACCOUNT_NAME_STATE);
            inboxCount = savedInstanceState.getInt(INBOX_COUNT_STATE);
        } else {
            mMessageFullname = getIntent().getIntExtra(EXTRA_MESSSAGE_FULLNAME, 0);
            mNewAccountName = getIntent().getStringExtra(EXTRA_NEW_ACCOUNT_NAME);
        }


        initializeNotificationAndBindView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAccessToken == null) {
            String instancePreference = mSharedPreferences.getString(SharedPreferencesUtils.ANONYMOUS_ACCOUNT_INSTANCE, APIUtils.API_BASE_URI);
            if (!instancePreference.startsWith(mRetrofit.getBaseURL())) {
                mRetrofit.setBaseURL(instancePreference);
                this.recreate();
            }
        } else {
            checkUserToken();
        }
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        int backgroundColor = mCustomThemeWrapper.getBackgroundColor();
        drawer.setBackgroundColor(backgroundColor);
        navigationWrapper.applyCustomTheme(mCustomThemeWrapper.getBottomAppBarIconColor(), mCustomThemeWrapper.getBottomAppBarBackgroundColor());
        navigationView.setBackgroundColor(backgroundColor);
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(appBarLayout, collapsingToolbarLayout, toolbar);
        applyTabLayoutTheme(tabLayout);
        applyFABTheme(navigationWrapper.floatingActionButton, useCircularFab);
    }

    private void initializeNotificationAndBindView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityResultLauncher<String> requestNotificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> mInternalSharedPreferences.edit().putBoolean(SharedPreferencesUtils.HAS_REQUESTED_NOTIFICATION_PERMISSION, true).apply());

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                if (!mInternalSharedPreferences.getBoolean(SharedPreferencesUtils.HAS_REQUESTED_NOTIFICATION_PERMISSION, false)) {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
            }
        }

        boolean enableNotification = mSharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_NOTIFICATION_KEY, true);
        long notificationInterval = Long.parseLong(mSharedPreferences.getString(SharedPreferencesUtils.NOTIFICATION_INTERVAL_KEY, "1"));
        TimeUnit timeUnit = (notificationInterval == 15 || notificationInterval == 30) ? TimeUnit.MINUTES : TimeUnit.HOURS;

        WorkManager workManager = WorkManager.getInstance(this);

        if (mNewAccountName != null) {
            if (mAccountQualifiedName == null || !mAccountName.equals(mNewAccountName)) {
                SwitchAccount.switchAccount(mRedditDataRoomDatabase, mRetrofit, mCurrentAccountSharedPreferences,
                        mExecutor, new Handler(), mNewAccountName, newAccount -> {
                            EventBus.getDefault().post(new SwitchAccountEvent(getClass().getName()));
                            Toast.makeText(this, R.string.account_switched, Toast.LENGTH_SHORT).show();

                            mNewAccountName = null;
                            if (newAccount != null) {
                                mAccessToken = newAccount.getAccessToken();
                                mAccountName = newAccount.getDisplay_name();
                                mAccountQualifiedName = newAccount.getAccountName();
                            }

                            setNotification(workManager, notificationInterval, timeUnit, enableNotification);

                            bindView();
                        });
            } else {
                setNotification(workManager, notificationInterval, timeUnit, enableNotification);

                bindView();
            }
        } else {
            setNotification(workManager, notificationInterval, timeUnit, enableNotification);

            bindView();
        }
    }

    private void setNotification(WorkManager workManager, long notificationInterval, TimeUnit timeUnit,
                                 boolean enableNotification) {
        if (enableNotification) {
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            PeriodicWorkRequest pullNotificationRequest =
                    new PeriodicWorkRequest.Builder(PullNotificationWorker.class,
                            notificationInterval, timeUnit)
                            .setConstraints(constraints)
                            .build();

            workManager.enqueueUniquePeriodicWork(PullNotificationWorker.UNIQUE_WORKER_NAME,
                    ExistingPeriodicWorkPolicy.KEEP, pullNotificationRequest);
        } else {
            workManager.cancelUniqueWork(PullNotificationWorker.UNIQUE_WORKER_NAME);
        }
    }

    private void bottomAppBarOptionAction(int option) {
        switch (option) {
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SUBSCRIPTIONS: {
                Intent intent = new Intent(this, SubscribedThingListingActivity.class);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_MULTIREDDITS: {
                Intent intent = new Intent(this, SubscribedThingListingActivity.class);
                intent.putExtra(SubscribedThingListingActivity.EXTRA_SHOW_MULTIREDDITS, true);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_INBOX: {
                Intent intent = new Intent(this, InboxActivity.class);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_PROFILE: {
                Intent intent = new Intent(this, ViewUserDetailActivity.class);
                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, mAccountName);
                intent.putExtra(ViewUserDetailActivity.EXTRA_QUALIFIED_USER_NAME_KEY, mAccountQualifiedName);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_REFRESH: {
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.refresh();
                }
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_CHANGE_SORT_TYPE: {
                changeSortType();
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_CHANGE_POST_LAYOUT: {
                PostLayoutBottomSheetFragment postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
                postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SEARCH: {
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_GO_TO_SUBREDDIT:
                goToSubreddit();
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_GO_TO_USER:
                goToUser();
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_RANDOM:
                randomThing();
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_HIDE_READ_POSTS:
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.hideReadPosts();
                }
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_FILTER_POSTS:
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.filterPosts();
                }
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_UPVOTED: {
                Intent intent = new Intent(this, AccountPostsActivity.class);
                intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_UPVOTED);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_DOWNVOTED: {
                Intent intent = new Intent(this, AccountPostsActivity.class);
                intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_DOWNVOTED);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_HIDDEN: {
                Intent intent = new Intent(this, AccountPostsActivity.class);
                intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_HIDDEN);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SAVED: {
                Intent intent = new Intent(MainActivity.this, AccountSavedThingActivity.class);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_GILDED: {
                Intent intent = new Intent(this, AccountPostsActivity.class);
                intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_GILDED);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_GO_TO_TOP: {
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.goBackToTop();
                }
                break;
            }
            default:
                PostTypeBottomSheetFragment postTypeBottomSheetFragment = new PostTypeBottomSheetFragment();
                postTypeBottomSheetFragment.show(getSupportFragmentManager(), postTypeBottomSheetFragment.getTag());
                break;

        }
    }

    private int getBottomAppBarOptionDrawableResource(int option) {
        switch (option) {
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SUBSCRIPTIONS:
                return R.drawable.ic_subscritptions_bottom_app_bar_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_MULTIREDDITS:
                return R.drawable.ic_multi_reddit_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_INBOX:
                return R.drawable.ic_inbox_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SUBMIT_POSTS:
                return R.drawable.ic_add_day_night_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_REFRESH:
                return R.drawable.ic_refresh_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_CHANGE_SORT_TYPE:
                return R.drawable.ic_sort_toolbar_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_CHANGE_POST_LAYOUT:
                return R.drawable.ic_post_layout_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SEARCH:
                return R.drawable.ic_search_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_GO_TO_SUBREDDIT:
                return R.drawable.ic_subreddit_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_GO_TO_USER:
                return R.drawable.ic_user_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_RANDOM:
                return R.drawable.ic_random_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_HIDE_READ_POSTS:
                return R.drawable.ic_hide_read_posts_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_FILTER_POSTS:
                return R.drawable.ic_filter_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_UPVOTED:
                return R.drawable.ic_arrow_upward_black_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_DOWNVOTED:
                return R.drawable.ic_arrow_downward_black_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_HIDDEN:
                return R.drawable.ic_outline_lock_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SAVED:
                return R.drawable.ic_outline_bookmarks_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_GILDED:
                return R.drawable.ic_star_border_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_GO_TO_TOP:
                return R.drawable.ic_keyboard_double_arrow_up_24;
            default:
                return R.drawable.ic_account_circle_24dp;
        }
    }

    private void bindView() {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        if (showBottomAppBar) {
            int optionCount = mBottomAppBarSharedPreference.getInt((mAccessToken == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_COUNT, 4);
            int option1 = mBottomAppBarSharedPreference.getInt((mAccessToken == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_1, SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SUBSCRIPTIONS);
            int option2 = mBottomAppBarSharedPreference.getInt((mAccessToken == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_2, SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_MULTIREDDITS);

            if (optionCount == 2) {
                navigationWrapper.bindOptionDrawableResource(getBottomAppBarOptionDrawableResource(option1), getBottomAppBarOptionDrawableResource(option2));

                if (navigationWrapper.navigationRailView == null) {
                    navigationWrapper.option2BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option1);
                    });

                    navigationWrapper.option4BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option2);
                    });
                } else {
                    navigationWrapper.navigationRailView.setOnItemSelectedListener(item -> {
                        int itemId = item.getItemId();
                        if (itemId == R.id.navigation_rail_option_1) {
                            bottomAppBarOptionAction(option1);
                            return true;
                        } else if (itemId == R.id.navigation_rail_option_2) {
                            bottomAppBarOptionAction(option2);
                            return true;
                        }
                        return false;
                    });
                }
            } else {
                int option3 = mBottomAppBarSharedPreference.getInt((mAccessToken == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_3, mAccessToken == null ? SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_REFRESH : SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_INBOX);
                int option4 = mBottomAppBarSharedPreference.getInt((mAccessToken == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_4, mAccessToken == null ? SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_CHANGE_SORT_TYPE : SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_PROFILE);

                navigationWrapper.bindOptionDrawableResource(getBottomAppBarOptionDrawableResource(option1),
                        getBottomAppBarOptionDrawableResource(option2), getBottomAppBarOptionDrawableResource(option3),
                        getBottomAppBarOptionDrawableResource(option4));

                if (navigationWrapper.navigationRailView == null) {
                    navigationWrapper.option1BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option1);
                    });

                    navigationWrapper.option2BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option2);
                    });

                    navigationWrapper.option3BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option3);
                    });

                    navigationWrapper.option4BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option4);
                    });
                } else {
                    navigationWrapper.navigationRailView.setOnItemSelectedListener(item -> {
                        int itemId = item.getItemId();
                        if (itemId == R.id.navigation_rail_option_1) {
                            bottomAppBarOptionAction(option1);
                            return true;
                        } else if (itemId == R.id.navigation_rail_option_2) {
                            bottomAppBarOptionAction(option2);
                            return true;
                        } else if (itemId == R.id.navigation_rail_option_3) {
                            bottomAppBarOptionAction(option3);
                            return true;
                        } else if (itemId == R.id.navigation_rail_option_4) {
                            bottomAppBarOptionAction(option4);
                            return true;
                        }
                        return false;
                    });
                }
            }
        } else {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) navigationWrapper.floatingActionButton.getLayoutParams();
            lp.setAnchorId(View.NO_ID);
            lp.gravity = Gravity.END | Gravity.BOTTOM;
            navigationWrapper.floatingActionButton.setLayoutParams(lp);
        }

        fabOption = mBottomAppBarSharedPreference.getInt((mAccessToken == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB,
                SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_SUBMIT_POSTS);
        switch (fabOption) {
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_REFRESH:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_refresh_24dp);
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_CHANGE_SORT_TYPE:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_sort_toolbar_24dp);
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_CHANGE_POST_LAYOUT:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_post_layout_24dp);
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_SEARCH:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_search_24dp);
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_GO_TO_SUBREDDIT:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_subreddit_24dp);
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_GO_TO_USER:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_user_24dp);
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_RANDOM:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_random_24dp);
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_HIDE_READ_POSTS:
                if (mAccessToken == null) {
                    navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_filter_24dp);
                    fabOption = SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_FILTER_POSTS;
                } else {
                    navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_hide_read_posts_24dp);
                }
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_FILTER_POSTS:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_filter_24dp);
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_GO_TO_TOP:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_keyboard_double_arrow_up_24);
                break;
            default:
                if (mAccessToken == null) {
                    navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_filter_24dp);
                    fabOption = SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_FILTER_POSTS;
                } else {
                    navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_add_day_night_24dp);
                }
                break;
        }
        navigationWrapper.floatingActionButton.setOnClickListener(view -> {
            switch (fabOption) {
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_REFRESH: {
                    if (sectionsPagerAdapter != null) {
                        sectionsPagerAdapter.refresh();
                    }
                    break;
                }
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_CHANGE_SORT_TYPE: {
                    changeSortType();
                    break;
                }
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_CHANGE_POST_LAYOUT: {
                    PostLayoutBottomSheetFragment postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
                    postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
                    break;
                }
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_SEARCH: {
                    Intent intent = new Intent(this, SearchActivity.class);
                    startActivity(intent);
                    break;
                }
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_GO_TO_SUBREDDIT:
                    goToSubreddit();
                    break;
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_GO_TO_USER:
                    goToUser();
                    break;
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_RANDOM:
                    randomThing();
                    break;
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_HIDE_READ_POSTS:
                    if (sectionsPagerAdapter != null) {
                        sectionsPagerAdapter.hideReadPosts();
                    }
                    break;
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_FILTER_POSTS:
                    if (sectionsPagerAdapter != null) {
                        sectionsPagerAdapter.filterPosts();
                    }
                    break;
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_GO_TO_TOP:
                    if (sectionsPagerAdapter != null) {
                        sectionsPagerAdapter.goBackToTop();
                    }
                    break;
                default:
                    PostTypeBottomSheetFragment postTypeBottomSheetFragment = new PostTypeBottomSheetFragment();
                    postTypeBottomSheetFragment.show(getSupportFragmentManager(), postTypeBottomSheetFragment.getTag());
                    break;
            }
        });
        navigationWrapper.floatingActionButton.setOnLongClickListener(view -> {
            FABMoreOptionsBottomSheetFragment fabMoreOptionsBottomSheetFragment= new FABMoreOptionsBottomSheetFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean(FABMoreOptionsBottomSheetFragment.EXTRA_ANONYMOUS_MODE, mAccessToken == null);
            fabMoreOptionsBottomSheetFragment.setArguments(bundle);
            fabMoreOptionsBottomSheetFragment.show(getSupportFragmentManager(), fabMoreOptionsBottomSheetFragment.getTag());
            return true;
        });
        navigationWrapper.floatingActionButton.setVisibility(hideFab ? View.GONE : View.VISIBLE);

        adapter = new NavigationDrawerRecyclerViewMergedAdapter(this, mSharedPreferences,
                mNsfwAndSpoilerSharedPreferences, mNavigationDrawerSharedPreferences, mSecuritySharedPreferences,
                mCustomThemeWrapper, mAccountName, mAccountQualifiedName, new NavigationDrawerRecyclerViewMergedAdapter.ItemClickListener() {
            @Override
            public void onMenuClick(int stringId) {
                Intent intent = null;
                if (stringId == R.string.profile) {
                    intent = new Intent(MainActivity.this, ViewUserDetailActivity.class);
                    intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, mAccountName);
                    intent.putExtra(ViewUserDetailActivity.EXTRA_QUALIFIED_USER_NAME_KEY, mAccountQualifiedName);
                } else if (stringId == R.string.subscriptions) {
                    intent = new Intent(MainActivity.this, SubscribedThingListingActivity.class);
                } else if (stringId == R.string.multi_reddit) {
                            intent = new Intent(MainActivity.this, SubscribedThingListingActivity.class);
                            intent.putExtra(SubscribedThingListingActivity.EXTRA_SHOW_MULTIREDDITS, true);
                        } else if (stringId == R.string.trending) {
                            intent = new Intent(MainActivity.this, TrendingActivity.class);
                        } else if (stringId == R.string.upvoted) {
                            intent = new Intent(MainActivity.this, AccountPostsActivity.class);
                            intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_UPVOTED);
                        } else if (stringId == R.string.downvoted) {
                            intent = new Intent(MainActivity.this, AccountPostsActivity.class);
                            intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_DOWNVOTED);
                        } else if (stringId == R.string.hidden) {
                            intent = new Intent(MainActivity.this, AccountPostsActivity.class);
                            intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_HIDDEN);
                        } else if (stringId == R.string.account_saved_thing_activity_label) {
                            intent = new Intent(MainActivity.this, AccountSavedThingActivity.class);
                        } else if (stringId == R.string.gilded) {
                            intent = new Intent(MainActivity.this, AccountPostsActivity.class);
                            intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_GILDED);
                        } else if (stringId == R.string.light_theme) {
                            mSharedPreferences.edit().putString(SharedPreferencesUtils.THEME_KEY, "0").apply();
                            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                            mCustomThemeWrapper.setThemeType(CustomThemeSharedPreferencesUtils.LIGHT);
                        } else if (stringId == R.string.dark_theme) {
                            mSharedPreferences.edit().putString(SharedPreferencesUtils.THEME_KEY, "1").apply();
                            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                            if (mSharedPreferences.getBoolean(SharedPreferencesUtils.AMOLED_DARK_KEY, false)) {
                                mCustomThemeWrapper.setThemeType(CustomThemeSharedPreferencesUtils.AMOLED);
                            } else {
                                mCustomThemeWrapper.setThemeType(CustomThemeSharedPreferencesUtils.DARK);
                            }
                        } else if (stringId == R.string.enable_nsfw) {
                            if (sectionsPagerAdapter != null) {
                                mNsfwAndSpoilerSharedPreferences.edit().putBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.NSFW_BASE, true).apply();
                                sectionsPagerAdapter.changeNSFW(true);
                            }
                        } else if (stringId == R.string.disable_nsfw) {
                            if (sectionsPagerAdapter != null) {
                                mNsfwAndSpoilerSharedPreferences.edit().putBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.NSFW_BASE, false).apply();
                                sectionsPagerAdapter.changeNSFW(false);
                            }
                        } else if (stringId == R.string.settings) {
                            intent = new Intent(MainActivity.this, SettingsActivity.class);
                        } else if (stringId == R.string.add_account) {
                            Intent addAccountIntent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivityForResult(addAccountIntent, LOGIN_ACTIVITY_REQUEST_CODE);
                        } else if (stringId == R.string.anonymous_account) {
                            SwitchToAnonymousMode.switchToAnonymousMode(mRedditDataRoomDatabase, mCurrentAccountSharedPreferences,
                                    mExecutor, new Handler(), false, () -> {
                                        Intent anonymousIntent = new Intent(MainActivity.this, MainActivity.class);
                                        startActivity(anonymousIntent);
                                        finish();
                                    });
                        } else if (stringId == R.string.log_out) {
                            SwitchToAnonymousMode.switchToAnonymousMode(mRedditDataRoomDatabase, mCurrentAccountSharedPreferences,
                                    mExecutor, new Handler(), true,
                                    () -> {
                                        Intent logOutIntent = new Intent(MainActivity.this, MainActivity.class);
                                        startActivity(logOutIntent);
                                        finish();
                                    });
                        } else if (stringId == R.string.anonymous_account_instance) {
                            changeAnonymousAccountInstance();
                } else if (stringId == R.string.blocks) {
                    intent = new Intent(MainActivity.this, BlockedThingListingActivity.class);
                } else if (stringId == R.string.instance_info) {
                    intent = new Intent(MainActivity.this, InstanceInfoActivity.class);
                }
                        if (intent != null) {
                            startActivity(intent);
                        }
                        drawer.closeDrawers();
                    }

            @Override
            public void onSubscribedSubredditClick(String subredditName, String communityQualifiedName) {
                Intent intent = new Intent(MainActivity.this, ViewSubredditDetailActivity.class);
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, subredditName);
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_COMMUNITY_FULL_NAME_KEY, communityQualifiedName);
                startActivity(intent);
            }

                    @Override
                    public void onAccountClick(String accountName) {
                        SwitchAccount.switchAccount(mRedditDataRoomDatabase,mRetrofit, mCurrentAccountSharedPreferences,
                                mExecutor, new Handler(), accountName, newAccount -> {
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    }
                });
        adapter.setInboxCount(inboxCount);
        navDrawerRecyclerView.setLayoutManager(new LinearLayoutManagerBugFixed(this));
        navDrawerRecyclerView.setAdapter(adapter.getConcatAdapter());

        int tabCount = mMainActivityTabsSharedPreferences.getInt((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_COUNT, 3);
        mShowFavoriteMultiReddits = mMainActivityTabsSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_FAVORITE_MULTIREDDITS, false);
        mShowMultiReddits = mMainActivityTabsSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_MULTIREDDITS, false);
        mShowFavoriteSubscribedSubreddits = mMainActivityTabsSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_FAVORITE_SUBSCRIBED_SUBREDDITS, false);
        mShowSubscribedSubreddits = mMainActivityTabsSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_SUBSCRIBED_SUBREDDITS, false);
        sectionsPagerAdapter = new SectionsPagerAdapter(this, tabCount, mShowFavoriteMultiReddits,
                mShowMultiReddits, mShowFavoriteSubscribedSubreddits, mShowSubscribedSubreddits);
        viewPager2.setAdapter(sectionsPagerAdapter);
        viewPager2.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);
        viewPager2.setUserInputEnabled(!mDisableSwipingBetweenTabs);
        if (mMainActivityTabsSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_TAB_NAMES, true)) {
            if (mShowFavoriteMultiReddits || mShowMultiReddits || mShowFavoriteSubscribedSubreddits || mShowSubscribedSubreddits) {
                tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            } else {
                tabLayout.setTabMode(TabLayout.MODE_FIXED);
            }
            new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
                switch (position) {
                    case 0:
                        Utils.setTitleWithCustomFontToTab(typeface, tab, mMainActivityTabsSharedPreferences.getString((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_TITLE, getString(R.string.subscribed_feed)));
                        break;
                    case 1:
                        Utils.setTitleWithCustomFontToTab(typeface, tab, mMainActivityTabsSharedPreferences.getString((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_TITLE, getString(R.string.local)));
                        break;
                    case 2:
                        Utils.setTitleWithCustomFontToTab(typeface, tab, mMainActivityTabsSharedPreferences.getString((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_TITLE, getString(R.string.all)));
                        break;
                }
                if (position >= tabCount && (mShowFavoriteMultiReddits || mShowMultiReddits ||
                        mShowFavoriteSubscribedSubreddits || mShowSubscribedSubreddits)
                        && sectionsPagerAdapter != null) {
                    if (position - tabCount < sectionsPagerAdapter.favoriteMultiReddits.size()) {
                        Utils.setTitleWithCustomFontToTab(typeface, tab, sectionsPagerAdapter.favoriteMultiReddits.get(position - tabCount).getDisplayName());
                    } else if (position - tabCount - sectionsPagerAdapter.favoriteMultiReddits.size() < sectionsPagerAdapter.multiReddits.size()) {
                        Utils.setTitleWithCustomFontToTab(typeface, tab, sectionsPagerAdapter.multiReddits.get(position - tabCount
                                - sectionsPagerAdapter.favoriteMultiReddits.size()).getDisplayName());
                    } else if (position - tabCount - sectionsPagerAdapter.favoriteMultiReddits.size()
                            - sectionsPagerAdapter.multiReddits.size() < sectionsPagerAdapter.favoriteSubscribedSubreddits.size()) {
                        Utils.setTitleWithCustomFontToTab(typeface, tab, sectionsPagerAdapter.favoriteSubscribedSubreddits.get(position - tabCount
                                - sectionsPagerAdapter.favoriteMultiReddits.size()
                                - sectionsPagerAdapter.multiReddits.size()).getName());
                    } else if (position - tabCount - sectionsPagerAdapter.favoriteMultiReddits.size()
                            - sectionsPagerAdapter.multiReddits.size()
                            - sectionsPagerAdapter.favoriteSubscribedSubreddits.size() < sectionsPagerAdapter.subscribedSubreddits.size()) {
                        Utils.setTitleWithCustomFontToTab(typeface, tab, sectionsPagerAdapter.subscribedSubreddits.get(position - tabCount
                                - sectionsPagerAdapter.favoriteMultiReddits.size()
                                - sectionsPagerAdapter.multiReddits.size()
                                - sectionsPagerAdapter.favoriteSubscribedSubreddits.size()).getName());
                    }
                }
            }).attach();
        } else {
            tabLayout.setVisibility(View.GONE);
        }

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (showBottomAppBar) {
                    navigationWrapper.showNavigation();
                }
                if (!hideFab) {
                    navigationWrapper.showFab();
                }
                sectionsPagerAdapter.displaySortTypeInToolbar();
            }
        });

        fixViewPager2Sensitivity(viewPager2);

        loadSubscriptions();

        multiRedditViewModel = new ViewModelProvider(this, new MultiRedditViewModel.Factory(getApplication(),
                mRedditDataRoomDatabase, mAccountName == null ? "-" : mAccountName))
                .get(MultiRedditViewModel.class);

        multiRedditViewModel.getAllFavoriteMultiReddits().observe(this, multiReddits -> {
            if (mShowFavoriteMultiReddits && sectionsPagerAdapter != null) {
                sectionsPagerAdapter.setFavoriteMultiReddits(multiReddits);
            }
        });

        multiRedditViewModel.getAllMultiReddits().observe(this, multiReddits -> {
            if (mShowMultiReddits && sectionsPagerAdapter != null) {
                sectionsPagerAdapter.setMultiReddits(multiReddits);
            }
        });

        subscribedSubredditViewModel = new ViewModelProvider(this,
                new SubscribedSubredditViewModel.Factory(getApplication(), mRedditDataRoomDatabase, mAccountQualifiedName == null ? "-" : mAccountQualifiedName))
                .get(SubscribedSubredditViewModel.class);
        subscribedSubredditViewModel.getAllSubscribedSubreddits().observe(this,
                subscribedSubredditData -> {
                    adapter.setSubscribedSubreddits(subscribedSubredditData);
                    if (mShowSubscribedSubreddits && sectionsPagerAdapter != null) {
                        sectionsPagerAdapter.setSubscribedSubreddits(subscribedSubredditData);
                    }
                });
        subscribedSubredditViewModel.getAllFavoriteSubscribedSubreddits().observe(this, subscribedSubredditData -> {
            adapter.setFavoriteSubscribedSubreddits(subscribedSubredditData);
            if (mShowFavoriteSubscribedSubreddits && sectionsPagerAdapter != null) {
                sectionsPagerAdapter.setFavoriteSubscribedSubreddits(subscribedSubredditData);
            }
        });

        accountViewModel = new ViewModelProvider(this,
                new AccountViewModel.Factory(mRedditDataRoomDatabase)).get(AccountViewModel.class);
        accountViewModel.getAccountsExceptCurrentAccountLiveData().observe(this, adapter::changeAccountsDataset);
        accountViewModel.getCurrentAccountLiveData().observe(this, account -> {
            if (account != null) {
                adapter.updateAccountInfo(account.getProfileImageUrl(), account.getBannerImageUrl());
            }
        });

        loadUserData();

        if (mAccessToken != null) {
            if (mMessageFullname != 0) {
                ReadMessage.readMessage(mOauthRetrofit, mAccessToken, mMessageFullname, new ReadMessage.ReadMessageListener() {
                    @Override
                    public void readSuccess() {
                        mMessageFullname = 0;
                    }

                    @Override
                    public void readFailed() {

                    }
                });
            }
        }
    }

    private void loadSubscriptions() {
        if (mAccessToken != null && !mFetchSubscriptionsSuccess) {
            FetchSubscribedThing.fetchSubscribedThing(mRetrofit.getRetrofit(), mAccessToken, mAccountQualifiedName, null,
                    new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(),
                    new FetchSubscribedThing.FetchSubscribedThingListener() {
                        @Override
                        public void onFetchSubscribedThingSuccess(ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                                                  ArrayList<SubscribedUserData> subscribedUserData,
                                                                  ArrayList<SubredditData> subredditData) {
                            InsertSubscribedThings.insertSubscribedThings(
                                    mExecutor,
                                    new Handler(),
                                    mRedditDataRoomDatabase,
                                    mAccountQualifiedName,
                                    subscribedSubredditData,
                                    subscribedUserData,
                                    subredditData,
                                    () -> mFetchSubscriptionsSuccess = true);
                        }

                        @Override
                        public void onFetchSubscribedThingFail() {
                            mFetchSubscriptionsSuccess = false;
                        }
                    });
        }
    }

    private void checkUserToken() {
        if (mBearerTokenUsed) {
            FetchUserData.validateAuthToken(mRetrofit.getRetrofit(), new FetchUserData.ValidateAuthTokenListener() {
                @Override
                public void onValidateAuthTokenSuccess() {
                }

                @Override
                public void onValidateAuthTokenFailed() {
                    // Alert user that the token is invalid and they need to re-login
                    new MaterialAlertDialogBuilder(MainActivity.this, R.style.MaterialAlertDialogTheme)
                            .setTitle(R.string.token_expired)
                            .setMessage(R.string.token_expired_message)
                            .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                // Username without instance
                                String username = mAccountName.substring(0, mAccountQualifiedName.indexOf("@"));
                                intent.putExtra(LoginActivity.EXTRA_INPUT_USERNAME, username);
                                intent.putExtra(LoginActivity.EXTRA_INPUT_INSTANCE, mRetrofit.getBaseURL());
                                startActivity(intent);
                            })
                            .setCancelable(false)
                            .show();
                }
            });
        }
    }

    private void loadUserData() {
        if (!mFetchUserInfoSuccess) {
            FetchUserData.fetchUnreadCount(mRetrofit.getRetrofit(), mAccessToken, new FetchUserData.FetchUserUnreadCountListener() {
                @Override
                public void onFetchUserUnreadCountSuccess(int inboxCount) {
                    MainActivity.this.inboxCount = inboxCount;
                    if (adapter != null) {
                        adapter.setInboxCount(inboxCount);
                    }
                }

                @Override
                public void onFetchUserUnreadCountFailed() {
                }
            });
            FetchUserData.fetchUserData(mRedditDataRoomDatabase, mRetrofit.getRetrofit(), mAccessToken,
                    mAccountName, new FetchUserData.FetchUserDataListener() {
                        @Override
                        public void onFetchUserDataSuccess(UserData userData, int inboxCount) {
                            mAccountName = userData.getName();
                            mFetchUserInfoSuccess = true;
                        }

                        @Override
                        public void onFetchUserDataFailed() {
                            mFetchUserInfoSuccess = false;
                        }
                    });
            if (mAccessToken != null) {
                FetchSiteInfo.fetchSiteInfo(mRetrofit.getRetrofit(), mAccessToken, new FetchSiteInfo.FetchSiteInfoListener() {
                    @Override
                    public void onFetchSiteInfoSuccess(SiteInfo siteInfo, MyUserInfo myUserInfo) {
                        String[] version = siteInfo.getVersion().split("\\.");
                        if (version.length > 0) {
                            Log.d("MainActvity", "Lemmy Version: " + version[0] + "." + version[1]);
                            int majorVersion = Integer.parseInt(version[0]);
                            int minorVersion = Integer.parseInt(version[1]);
                            if (majorVersion > 0 || (majorVersion == 0 && minorVersion >= 19)) {
                                mRetrofit.setAccessToken(mAccessToken);
                                mCurrentAccountSharedPreferences.edit().putBoolean(SharedPreferencesUtils.BEARER_TOKEN_AUTH, true).apply();
                                checkUserToken();
                            } else {
                                mRetrofit.setAccessToken(null);
                                mCurrentAccountSharedPreferences.edit().putBoolean(SharedPreferencesUtils.BEARER_TOKEN_AUTH, false).apply();
                            }
                        }
                    }

                    @Override
                    public void onFetchSiteInfoFailed(boolean parseFailed) {

                    }
                });
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    private void changeSortType() {
        int currentPostType = sectionsPagerAdapter.getCurrentPostType();
        PostFragment postFragment = sectionsPagerAdapter.getCurrentFragment();
        if (postFragment != null) {
            SortTypeBottomSheetFragment sortTypeBottomSheetFragment = SortTypeBottomSheetFragment.getNewInstance(currentPostType, postFragment.getSortType());
            sortTypeBottomSheetFragment.show(getSupportFragmentManager(), sortTypeBottomSheetFragment.getTag());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_search_main_activity) {
           // Intent intent = new Intent(this, SearchActivity.class);
            Intent intent = new Intent(this, WebViewActivity.class);
            Uri uri = Uri.parse("https://imqa.io");
            intent.setData(uri);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_sort_main_activity) {
            Intent intent = new Intent(this, WebViewActivity.class);
            String WcrashValue = mSharedPreferences.getString("edit_wcrash", "");
            Uri uri =Uri.parse(WcrashValue);
            intent.setData(uri);
            startActivity(intent);
            //changeSortType();
            return true;
        } else if (itemId == R.id.action_crash) {
            int a = 1/0;
        } else if (itemId == R.id.action_anr) {
            long endTime = System.currentTimeMillis() + 10000; // 10초 동안 실행
            while (System.currentTimeMillis() < endTime) {
                // 무거운 작업을 수행하여 CPU 사용량을 높임
                double value = Math.random();
                for (int i = 0; i < 1000000; i++) {
                    value = Math.sin(value) + Math.cos(value);
                }
            }
        } else if (itemId == R.id.action_anr2) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        // 무한 루프를 실행하여 메인 스레드를 차단
                    }
                }
            });


        return true;

    }else if (itemId == R.id.action_refresh_main_activity)

    {
        sectionsPagerAdapter.refresh();
        mFetchUserInfoSuccess = false;
        loadUserData();
        return true;
    } else if (itemId == R.id.action_change_post_layout_main_activity) {
            PostLayoutBottomSheetFragment postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
            postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isOpen()) {
            drawer.close();
        } else {
            if (mBackButtonAction == SharedPreferencesUtils.MAIN_PAGE_BACK_BUTTON_ACTION_CONFIRM_EXIT) {
                new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.exit_app)
                        .setPositiveButton(R.string.yes, (dialogInterface, i)
                                -> finish())
                        .setNegativeButton(R.string.no, null)
                        .show();
            } else if (mBackButtonAction == SharedPreferencesUtils.MAIN_PAGE_BACK_BUTTON_ACTION_OPEN_NAVIGATION_DRAWER) {
                drawer.open();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (sectionsPagerAdapter != null) {
            return sectionsPagerAdapter.handleKeyDown(keyCode) || super.onKeyDown(keyCode, event);
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FETCH_USER_INFO_STATE, mFetchUserInfoSuccess);
        outState.putBoolean(FETCH_SUBSCRIPTIONS_STATE, mFetchSubscriptionsSuccess);
        outState.putBoolean(DRAWER_ON_ACCOUNT_SWITCH_STATE, mDrawerOnAccountSwitch);
        outState.putInt(MESSAGE_FULLNAME_STATE, mMessageFullname);
        outState.putString(NEW_ACCOUNT_NAME_STATE, mNewAccountName);
        outState.putInt(INBOX_COUNT_STATE, inboxCount);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void sortTypeSelected(SortType sortType) {
        sectionsPagerAdapter.changeSortType(sortType);
    }

    @Override
    public void sortTypeSelected(String sortType) {
        SortTimeBottomSheetFragment sortTimeBottomSheetFragment = new SortTimeBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SortTimeBottomSheetFragment.EXTRA_SORT_TYPE, sortType);
        sortTimeBottomSheetFragment.setArguments(bundle);
        sortTimeBottomSheetFragment.show(getSupportFragmentManager(), sortTimeBottomSheetFragment.getTag());
    }

    @Override
    public void postTypeSelected(int postType) {
        Intent intent;
        switch (postType) {
            case PostTypeBottomSheetFragment.TYPE_TEXT:
                intent = new Intent(MainActivity.this, PostTextActivity.class);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_LINK:
                intent = new Intent(MainActivity.this, PostLinkActivity.class);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_IMAGE:
                intent = new Intent(MainActivity.this, PostImageActivity.class);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_VIDEO:
                intent = new Intent(MainActivity.this, PostVideoActivity.class);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_GALLERY:
                intent = new Intent(MainActivity.this, PostGalleryActivity.class);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_POLL:
                intent = new Intent(MainActivity.this, PostPollActivity.class);
                startActivity(intent);
        }
    }

    @Override
    public void postLayoutSelected(int postLayout) {
        sectionsPagerAdapter.changePostLayout(postLayout);
    }

    @Override
    public void contentScrollUp() {
        if (showBottomAppBar && !mLockBottomAppBar) {
            navigationWrapper.showNavigation();
        }
        if (!(showBottomAppBar && mLockBottomAppBar) && !hideFab) {
            navigationWrapper.showFab();
        }
    }

    @Override
    public void contentScrollDown() {
        if (!(showBottomAppBar && mLockBottomAppBar) && !hideFab) {
            navigationWrapper.hideFab();
        }
        if (showBottomAppBar && !mLockBottomAppBar) {
            navigationWrapper.hideNavigation();
        }
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        if (!getClass().getName().equals(event.excludeActivityClassName)) {
            finish();
        }
    }

    @Subscribe
    public void onChangeNSFWEvent(ChangeNSFWEvent changeNSFWEvent) {
        sectionsPagerAdapter.changeNSFW(changeNSFWEvent.nsfw);
        if (adapter != null) {
            adapter.setNSFWEnabled(changeNSFWEvent.nsfw);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecreateActivityEvent(RecreateActivityEvent recreateActivityEvent) {
        ActivityCompat.recreate(this);
    }

    @Subscribe
    public void onChangeLockBottomAppBar(ChangeLockBottomAppBarEvent changeLockBottomAppBarEvent) {
        mLockBottomAppBar = changeLockBottomAppBarEvent.lockBottomAppBar;
    }

    @Subscribe
    public void onChangeDisableSwipingBetweenTabsEvent(ChangeDisableSwipingBetweenTabsEvent changeDisableSwipingBetweenTabsEvent) {
        mDisableSwipingBetweenTabs = changeDisableSwipingBetweenTabsEvent.disableSwipingBetweenTabs;
        viewPager2.setUserInputEnabled(!mDisableSwipingBetweenTabs);
    }

    @Subscribe
    public void onChangeRequireAuthToAccountSectionEvent(ChangeRequireAuthToAccountSectionEvent changeRequireAuthToAccountSectionEvent) {
        if (adapter != null) {
            adapter.setRequireAuthToAccountSection(changeRequireAuthToAccountSectionEvent.requireAuthToAccountSection);
        }
    }

    @Subscribe
    public void onChangeShowAvatarOnTheRightInTheNavigationDrawerEvent(ChangeShowAvatarOnTheRightInTheNavigationDrawerEvent event) {
        if (adapter != null) {
            adapter.setShowAvatarOnTheRightInTheNavigationDrawer(event.showAvatarOnTheRightInTheNavigationDrawer);
            int previousPosition = -1;
            if (navDrawerRecyclerView.getLayoutManager() != null) {
                previousPosition = ((LinearLayoutManagerBugFixed) navDrawerRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            }

            RecyclerView.LayoutManager layoutManager = navDrawerRecyclerView.getLayoutManager();
            navDrawerRecyclerView.setAdapter(null);
            navDrawerRecyclerView.setLayoutManager(null);
            navDrawerRecyclerView.setAdapter(adapter.getConcatAdapter());
            navDrawerRecyclerView.setLayoutManager(layoutManager);

            if (previousPosition > 0) {
                navDrawerRecyclerView.scrollToPosition(previousPosition);
            }
        }
    }

    @Subscribe
    public void onChangeInboxCountEvent(ChangeInboxCountEvent event) {
        if (adapter != null) {
            adapter.setInboxCount(event.inboxCount);
        }
    }

    @Subscribe
    public void onChangeHideKarmaEvent(ChangeHideKarmaEvent event) {
        if (adapter != null) {
            adapter.setHideKarma(event.hideKarma);
        }
    }

    @Subscribe
    public void onChangeHideFabInPostFeed(ChangeHideFabInPostFeedEvent event) {
        hideFab = event.hideFabInPostFeed;
        navigationWrapper.floatingActionButton.setVisibility(hideFab ? View.GONE : View.VISIBLE);
    }

    @Subscribe
    public void onChangeUseCircularFab(ChangeUseCircularFabEvent event) {
        useCircularFab = event.isUseCircularFab();
        applyFABTheme(navigationWrapper.floatingActionButton, useCircularFab);
    }

    @Override
    public void onLongPress() {
        if (sectionsPagerAdapter != null) {
            sectionsPagerAdapter.goBackToTop();
        }
    }

    @Override
    public void displaySortType() {
        if (sectionsPagerAdapter != null) {
            sectionsPagerAdapter.displaySortTypeInToolbar();
        }
    }

    @Override
    public void fabOptionSelected(int option) {
        switch (option) {
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_SUBMIT_POST:
                PostTypeBottomSheetFragment postTypeBottomSheetFragment = new PostTypeBottomSheetFragment();
                postTypeBottomSheetFragment.show(getSupportFragmentManager(), postTypeBottomSheetFragment.getTag());
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_REFRESH:
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.refresh();
                }
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_CHANGE_SORT_TYPE:
                changeSortType();
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_CHANGE_POST_LAYOUT:
                PostLayoutBottomSheetFragment postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
                postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_SEARCH:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_GO_TO_SUBREDDIT: {
                goToSubreddit();
                break;
            }
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_GO_TO_USER: {
                goToUser();
                break;
            }
            case FABMoreOptionsBottomSheetFragment.FAB_HIDE_READ_POSTS: {
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.hideReadPosts();
                }
                break;
            }
            case FABMoreOptionsBottomSheetFragment.FAB_FILTER_POSTS: {
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.filterPosts();
                }
                break;
            }
            case FABMoreOptionsBottomSheetFragment.FAB_GO_TO_TOP: {
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.goBackToTop();
                }
                break;
            }
        }
    }

    private void changeAnonymousAccountInstance() {
        View rootView = getLayoutInflater().inflate(R.layout.dialog_go_to_thing_edit_text, coordinatorLayout, false);
        TextInputEditText thingEditText = rootView.findViewById(R.id.text_input_edit_text_go_to_thing_edit_text);

        thingEditText.requestFocus();
        thingEditText.setText(mSharedPreferences.getString(SharedPreferencesUtils.ANONYMOUS_ACCOUNT_INSTANCE, APIUtils.API_BASE_URI));
        Utils.showKeyboard(this, new Handler(), thingEditText);
        thingEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                Utils.hideKeyboard(this);
                String url = thingEditText.getText().toString();
                if (url.isEmpty()) {
                    thingEditText.setError("Instance URL cannot be empty");
                    return false;
                }
                if (!url.startsWith("http://") || !url.startsWith("https://")) {
                    url = "https://" + url;
                }
                try {
                    URL urlObj = new URL(url);
                    url = urlObj.getProtocol() + "://" + urlObj.getHost() + "/";
                    mSharedPreferences.edit().putString(SharedPreferencesUtils.ANONYMOUS_ACCOUNT_INSTANCE, url).apply();
                    mRetrofit.setBaseURL(url);
                    sectionsPagerAdapter.getCurrentFragment().refresh();
                } catch (MalformedURLException e) {
                    thingEditText.setError("Invalid URL");
                    return false;
                }
                return true;
            }
            return false;
        });

        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(R.string.anonymous_account_instance)
                .setView(rootView)
                .setPositiveButton(R.string.ok, (dialogInterface, i)
                        -> {
                    Utils.hideKeyboard(this);
                    String url = thingEditText.getText().toString();
                    if (url.isEmpty()) {
                        thingEditText.setError("Instance URL cannot be empty");
                        return;
                    }
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "https://" + url;
                    }
                    try {
                        URL urlObj = new URL(url);
                        url = urlObj.getProtocol() + "://" + urlObj.getHost() + "/";
                        mSharedPreferences.edit().putString(SharedPreferencesUtils.ANONYMOUS_ACCOUNT_INSTANCE, url).apply();
                        mRetrofit.setBaseURL(url);
                        sectionsPagerAdapter.getCurrentFragment().refresh();
                    } catch (MalformedURLException e) {
                        thingEditText.setError("Invalid URL");
                    }
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    Utils.hideKeyboard(this);
                })
                .setOnDismissListener(dialogInterface -> {
                    Utils.hideKeyboard(this);
                })
                .show();
    }

    private void goToSubreddit() {
        View rootView = getLayoutInflater().inflate(R.layout.dialog_go_to_thing_edit_text, coordinatorLayout, false);
        TextInputEditText thingEditText = rootView.findViewById(R.id.text_input_edit_text_go_to_thing_edit_text);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view_go_to_thing_edit_text);
        SubredditAutocompleteRecyclerViewAdapter adapter = new SubredditAutocompleteRecyclerViewAdapter(
                this, mCustomThemeWrapper, subredditData -> {
            Utils.hideKeyboard(this);
            Intent intent = new Intent(MainActivity.this, ViewSubredditDetailActivity.class);
            intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, subredditData.getName());
            intent.putExtra(ViewSubredditDetailActivity.EXTRA_COMMUNITY_FULL_NAME_KEY, LemmyUtils.actorID2FullName(subredditData.getActorId()));
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        thingEditText.requestFocus();
        Utils.showKeyboard(this, new Handler(), thingEditText);
        thingEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                Utils.hideKeyboard(this);
                Intent subredditIntent = new Intent(this, ViewSubredditDetailActivity.class);
                String communityName = thingEditText.getText().toString();
                if (communityName.startsWith("!")) {
                    communityName = communityName.substring(1);
                }
                subredditIntent.putExtra(ViewSubredditDetailActivity.EXTRA_COMMUNITY_FULL_NAME_KEY, communityName);

                startActivity(subredditIntent);
                return true;
            }
            return false;
        });

        boolean nsfw = mNsfwAndSpoilerSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.NSFW_BASE, false);
        thingEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (subredditAutocompleteCall != null) {
                    subredditAutocompleteCall.cancel();
                }
                subredditAutocompleteCall = mOauthRetrofit.create(RedditAPI.class).subredditAutocomplete(APIUtils.getOAuthHeader(mAccessToken),
                        editable.toString(), nsfw);
                subredditAutocompleteCall.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            ParseSubredditData.parseSubredditListingData(response.body(), nsfw, new ParseSubredditData.ParseSubredditListingDataListener() {
                                @Override
                                public void onParseSubredditListingDataSuccess(ArrayList<SubredditData> subredditData, String after) {
                                    adapter.setSubreddits(subredditData);
                                }

                                @Override
                                public void onParseSubredditListingDataFail() {

                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

                    }
                });
            }
        });
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(R.string.go_to_community)
                .setView(rootView)
                .setPositiveButton(R.string.ok, (dialogInterface, i)
                        -> {
                    Utils.hideKeyboard(this);
                    Intent subredditIntent = new Intent(this, ViewSubredditDetailActivity.class);
                    String communityName = thingEditText.getText().toString();
                    if (communityName.startsWith("!")) {
                        communityName = communityName.substring(1);
                    }
                    subredditIntent.putExtra(ViewSubredditDetailActivity.EXTRA_COMMUNITY_FULL_NAME_KEY, communityName);
                    startActivity(subredditIntent);
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    Utils.hideKeyboard(this);
                })
                .setOnDismissListener(dialogInterface -> {
                    Utils.hideKeyboard(this);
                })
                .show();
    }

    private void goToUser() {
        View rootView = getLayoutInflater().inflate(R.layout.dialog_go_to_thing_edit_text, coordinatorLayout, false);
        TextInputEditText thingEditText = rootView.findViewById(R.id.text_input_edit_text_go_to_thing_edit_text);
        thingEditText.requestFocus();
        Utils.showKeyboard(this, new Handler(), thingEditText);
        thingEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                Utils.hideKeyboard(this);
                Intent userIntent = new Intent(this, ViewUserDetailActivity.class);
                String qualifiedName = thingEditText.getText().toString();
                if (qualifiedName.startsWith("@")) {
                    qualifiedName = qualifiedName.substring(1);
                }
                userIntent.putExtra(ViewUserDetailActivity.EXTRA_QUALIFIED_USER_NAME_KEY, qualifiedName);
                startActivity(userIntent);
                return true;
            }
            return false;
        });
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(R.string.go_to_user)
                .setView(rootView)
                .setPositiveButton(R.string.ok, (dialogInterface, i)
                        -> {
                    Utils.hideKeyboard(this);
                    Intent userIntent = new Intent(this, ViewUserDetailActivity.class);
                    String qualifiedName = thingEditText.getText().toString();
                    if (qualifiedName.startsWith("@")) {
                        qualifiedName = qualifiedName.substring(1);
                    }
                    userIntent.putExtra(ViewUserDetailActivity.EXTRA_QUALIFIED_USER_NAME_KEY, "kiiim");
                    startActivity(userIntent);
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    Utils.hideKeyboard(this);
                })
                .setOnDismissListener(dialogInterface -> {
                    Utils.hideKeyboard(this);
                })
                .show();
    }

    private void randomThing() {
        RandomBottomSheetFragment randomBottomSheetFragment = new RandomBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(RandomBottomSheetFragment.EXTRA_IS_NSFW, !mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, false) && mNsfwAndSpoilerSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.NSFW_BASE, false));
        randomBottomSheetFragment.setArguments(bundle);
        randomBottomSheetFragment.show(getSupportFragmentManager(), randomBottomSheetFragment.getTag());
    }

    @Override
    public void randomOptionSelected(int option) {
        Intent intent = new Intent(this, FetchRandomSubredditOrPostActivity.class);
        intent.putExtra(FetchRandomSubredditOrPostActivity.EXTRA_RANDOM_OPTION, option);
        startActivity(intent);
    }

    @Override
    public void markPostAsRead(Post post) {
        markPostAsRead.markPostAsRead(post.getId(), mAccessToken, new MarkPostAsRead.MarkPostAsReadListener() {
            @Override
            public void onMarkPostAsReadSuccess() {
                InsertReadPost.insertReadPost(mRedditDataRoomDatabase, mExecutor, mAccountQualifiedName, post.getId());
            }

            @Override
            public void onMarkPostAsReadFailed() {
                Toast.makeText(MainActivity.this, R.string.mark_post_as_read_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void doNotShowRedditAPIInfoAgain() {
        mInternalSharedPreferences.edit().putBoolean(SharedPreferencesUtils.DO_NOT_SHOW_REDDIT_API_INFO_V2_AGAIN, true).apply();
    }

    private class SectionsPagerAdapter extends FragmentStateAdapter {
        int tabCount;
        boolean showFavoriteMultiReddits;
        boolean showMultiReddits;
        boolean showFavoriteSubscribedSubreddits;
        boolean showSubscribedSubreddits;
        List<MultiReddit> favoriteMultiReddits;
        List<MultiReddit> multiReddits;
        List<SubscribedSubredditData> favoriteSubscribedSubreddits;
        List<SubscribedSubredditData> subscribedSubreddits;

        SectionsPagerAdapter(FragmentActivity fa, int tabCount, boolean showFavoriteMultiReddits,
                             boolean showMultiReddits, boolean showFavoriteSubscribedSubreddits,
                             boolean showSubscribedSubreddits) {
            super(fa);
            this.tabCount = tabCount;
            favoriteMultiReddits = new ArrayList<>();
            multiReddits = new ArrayList<>();
            favoriteSubscribedSubreddits = new ArrayList<>();
            subscribedSubreddits = new ArrayList<>();
            this.showFavoriteMultiReddits = showFavoriteMultiReddits;
            this.showMultiReddits = showMultiReddits;
            this.showFavoriteSubscribedSubreddits = showFavoriteSubscribedSubreddits;
            this.showSubscribedSubreddits = showSubscribedSubreddits;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                int postType = mMainActivityTabsSharedPreferences.getInt((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_HOME);
                String name = mMainActivityTabsSharedPreferences.getString((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_NAME, "");
                return generatePostFragment(postType, name);
            } else {
                if (showFavoriteMultiReddits) {
                    if (position >= tabCount && position - tabCount < favoriteMultiReddits.size()) {
                        int postType = SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT;
                        String name = favoriteMultiReddits.get(position - tabCount).getPath();
                        return generatePostFragment(postType, name);
                    }
                }

                if (showMultiReddits) {
                    if (position >= tabCount && position - tabCount - favoriteMultiReddits.size() < multiReddits.size()) {
                        int postType = SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT;
                        String name = multiReddits.get(position - tabCount - favoriteMultiReddits.size()).getPath();
                        return generatePostFragment(postType, name);
                    }
                }

                if (showFavoriteSubscribedSubreddits) {
                    if (position >= tabCount && position - tabCount - favoriteMultiReddits.size()
                            - multiReddits.size() < favoriteSubscribedSubreddits.size()) {
                        int postType = SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT;
                        String name = favoriteSubscribedSubreddits.get(position - tabCount
                                - favoriteMultiReddits.size()
                                - multiReddits.size()).getName();
                        return generatePostFragment(postType, name);
                    }
                }
                if (showSubscribedSubreddits) {
                    if (position >= tabCount && position - tabCount - favoriteMultiReddits.size()
                            - multiReddits.size() - favoriteSubscribedSubreddits.size() < subscribedSubreddits.size()) {
                        int postType = SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT;
                        String name = subscribedSubreddits.get(position - tabCount - favoriteMultiReddits.size()
                                - multiReddits.size() - favoriteSubscribedSubreddits.size()).getName();
                        return generatePostFragment(postType, name);
                    }
                }

                int postType;
                String name;
                if (position == 1) {
                     postType = mMainActivityTabsSharedPreferences.getInt((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_POPULAR);
                     name = mMainActivityTabsSharedPreferences.getString((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_NAME, "");
                } else {
                    postType = mMainActivityTabsSharedPreferences.getInt((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_ALL);
                    name = mMainActivityTabsSharedPreferences.getString((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME, "");
                }
                return generatePostFragment(postType, name);
            }
        }

        public void setFavoriteMultiReddits(List<MultiReddit> favoriteMultiReddits) {
            this.favoriteMultiReddits = favoriteMultiReddits;
            notifyDataSetChanged();
        }

        public void setMultiReddits(List<MultiReddit> multiReddits) {
            this.multiReddits = multiReddits;
            notifyDataSetChanged();
        }

        public void setFavoriteSubscribedSubreddits(List<SubscribedSubredditData> favoriteSubscribedSubreddits) {
            this.favoriteSubscribedSubreddits = favoriteSubscribedSubreddits;
            notifyDataSetChanged();
        }

        public void setSubscribedSubreddits(List<SubscribedSubredditData> subscribedSubreddits) {
            this.subscribedSubreddits = subscribedSubreddits;
            notifyDataSetChanged();
        }

        private Fragment generatePostFragment(int postType, String name) {
            if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_HOME) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, mAccessToken == null ? PostPagingSource.TYPE_ANONYMOUS_FRONT_PAGE : PostPagingSource.TYPE_FRONT_PAGE);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                fragment.setArguments(bundle);
                return fragment;
            } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_ALL) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostPagingSource.TYPE_FRONT_PAGE);
                bundle.putString(PostFragment.EXTRA_NAME, "all");
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                fragment.setArguments(bundle);
                return fragment;
            } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostPagingSource.TYPE_SUBREDDIT);
                bundle.putString(PostFragment.EXTRA_NAME, name);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                fragment.setArguments(bundle);
                return fragment;
            } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putString(PostFragment.EXTRA_NAME, name);
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, mAccessToken == null ? PostPagingSource.TYPE_ANONYMOUS_MULTIREDDIT : PostPagingSource.TYPE_MULTI_REDDIT);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                fragment.setArguments(bundle);
                return fragment;
            } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_USER) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostPagingSource.TYPE_USER);
                bundle.putString(PostFragment.EXTRA_USER_NAME, name);
                bundle.putString(PostFragment.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_SUBMITTED);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                fragment.setArguments(bundle);
                return fragment;
            } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_UPVOTED
                    || postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_DOWNVOTED
                    || postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_HIDDEN
                    || postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SAVED
                    || postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_GILDED) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostPagingSource.TYPE_USER);
                bundle.putString(PostFragment.EXTRA_USER_NAME, mAccountName);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                bundle.putBoolean(PostFragment.EXTRA_DISABLE_READ_POSTS, true);

                if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_UPVOTED) {
                    bundle.putString(PostFragment.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_UPVOTED);
                } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_DOWNVOTED) {
                    bundle.putString(PostFragment.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_DOWNVOTED);
                } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_HIDDEN) {
                    bundle.putString(PostFragment.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_HIDDEN);
                } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SAVED) {
                    bundle.putString(PostFragment.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_SAVED);
                } else {
                    bundle.putString(PostFragment.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_GILDED);
                }

                fragment.setArguments(bundle);
                return fragment;
            } else {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostPagingSource.TYPE_FRONT_PAGE);
                bundle.putString(PostFragment.EXTRA_NAME, "local");
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                fragment.setArguments(bundle);
                return fragment;
            }
        }

        @Override
        public int getItemCount() {
            return tabCount + favoriteMultiReddits.size() + multiReddits.size() +
                    favoriteSubscribedSubreddits.size() + subscribedSubreddits.size();
        }

        @Nullable
        private PostFragment getCurrentFragment() {
            if (viewPager2 == null || fragmentManager == null) {
                return null;
            }
            Fragment fragment = fragmentManager.findFragmentByTag("f" + viewPager2.getCurrentItem());
            if (fragment instanceof PostFragment) {
                return (PostFragment) fragment;
            }
            return null;
        }

        boolean handleKeyDown(int keyCode) {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                return currentFragment.handleKeyDown(keyCode);
            }
            return false;
        }

        int getCurrentPostType() {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                return currentFragment.getPostType();
            }
            return PostPagingSource.TYPE_SUBREDDIT;
        }

        void changeSortType(SortType sortType) {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                currentFragment.changeSortType(sortType);
            }
            displaySortTypeInToolbar();
        }

        public void refresh() {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                currentFragment.refresh();
            }
        }

        void changeNSFW(boolean nsfw) {
            for (int i = 0; i < getItemCount(); i++) {
                Fragment fragment = fragmentManager.findFragmentByTag("f" + i);
                if (fragment instanceof PostFragment) {
                    ((PostFragment) fragment).changeNSFW(nsfw);
                }
            }
        }

        void changePostLayout(int postLayout) {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                currentFragment.changePostLayout(postLayout);
            }
        }

        void goBackToTop() {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                currentFragment.goBackToTop();
            }
        }

        void displaySortTypeInToolbar() {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                SortType sortType = currentFragment.getSortType();
                Utils.displaySortTypeInToolbar(sortType, toolbar);
            }
        }

        void hideReadPosts() {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                currentFragment.hideReadPosts();
            }
        }

        void filterPosts() {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                currentFragment.filterPosts();
            }
        }
    }
}