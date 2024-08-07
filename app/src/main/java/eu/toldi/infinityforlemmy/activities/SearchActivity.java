package eu.toldi.infinityforlemmy.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.toldi.infinityforlemmy.Infinity;
import eu.toldi.infinityforlemmy.R;
import eu.toldi.infinityforlemmy.RedditDataRoomDatabase;
import eu.toldi.infinityforlemmy.adapters.SearchActivityRecyclerViewAdapter;
import eu.toldi.infinityforlemmy.adapters.SubredditAutocompleteRecyclerViewAdapter;
import eu.toldi.infinityforlemmy.apis.RedditAPI;
import eu.toldi.infinityforlemmy.customtheme.CustomThemeWrapper;
import eu.toldi.infinityforlemmy.customviews.slidr.Slidr;
import eu.toldi.infinityforlemmy.events.SwitchAccountEvent;
import eu.toldi.infinityforlemmy.recentsearchquery.RecentSearchQuery;
import eu.toldi.infinityforlemmy.recentsearchquery.RecentSearchQueryViewModel;
import eu.toldi.infinityforlemmy.subreddit.ParseSubredditData;
import eu.toldi.infinityforlemmy.subreddit.SubredditData;
import eu.toldi.infinityforlemmy.subreddit.SubredditWithSelection;
import eu.toldi.infinityforlemmy.subscribedsubreddit.SubscribedSubredditData;
import eu.toldi.infinityforlemmy.utils.APIUtils;
import eu.toldi.infinityforlemmy.utils.LemmyUtils;
import eu.toldi.infinityforlemmy.utils.SharedPreferencesUtils;
import eu.toldi.infinityforlemmy.utils.Utils;
import io.imqa.mpm.IMQAMpmAgent;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SearchActivity extends BaseActivity {

    public static final String EXTRA_QUERY = "EQ";
    public static final String EXTRA_SUBREDDIT_NAME = "ESN";
    public static final String EXTRA_SUBREDDIT_IS_USER = "ESIU";
    public static final String EXTRA_SEARCH_ONLY_SUBREDDITS = "ESOS";
    public static final String EXTRA_SEARCH_ONLY_USERS = "ESOU";
    public static final String EXTRA_RETURN_SUBREDDIT_NAME = "ERSN";
    public static final String EXTRA_RETURN_SUBREDDIT_ICON_URL = "ERSIU";
    public static final String RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES = "RESSN";
    public static final String RETURN_EXTRA_SELECTED_USERNAMES = "RESU";
    public static final String EXTRA_RETURN_USER_NAME = "ERUN";
    public static final String EXTRA_RETURN_USER_ICON_URL = "ERUIU";
    public static final String EXTRA_IS_MULTI_SELECTION = "EIMS";
    public static final int SUICIDE_PREVENTION_ACTIVITY_REQUEST_CODE = 101;
    public static final String EXTRA_COMMUNITY_FULL_NAME = "ECF";

    private static final String SUBREDDIT_NAME_STATE = "SNS";
    private static final String SUBREDDIT_IS_USER_STATE = "SIUS";

    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 0;
    private static final int SUBREDDIT_SEARCH_REQUEST_CODE = 1;
    private static final int USER_SEARCH_REQUEST_CODE = 2;
    private static final String COMMUNITY_QUALIFIED_NAME = "CQN";

    @BindView(R.id.coordinator_layout_search_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_search_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.search_edit_text_search_activity)
    EditText searchEditText;
    @BindView(R.id.clear_search_edit_view_search_activity)
    ImageView clearSearchTextImageView;
    @BindView(R.id.link_handler_image_view_search_activity)
    ImageView linkHandlerImageView;
    @BindView(R.id.delete_all_recent_searches_button_search_activity)
    MaterialButton deleteAllSearchesButton;
    @BindView(R.id.subreddit_name_relative_layout_search_activity)
    RelativeLayout subredditNameRelativeLayout;
    @BindView(R.id.search_in_text_view_search_activity)
    TextView searchInTextView;
    @BindView(R.id.subreddit_name_text_view_search_activity)
    TextView subredditNameTextView;
    @BindView(R.id.divider_search_activity)
    View divider;
    @BindView(R.id.recycler_view_search_activity)
    RecyclerView recyclerView;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    @Named("nsfw_and_spoiler")
    SharedPreferences mNsfwAndSpoilerSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor executor;
    private String mAccountName;
    private String mAccessToken;
    private String query;
    private String subredditName;

    private SubscribedSubredditData communityData;

    private String communityQualifiedName;
    private boolean subredditIsUser;
    private boolean searchOnlySubreddits;
    private boolean searchOnlyUsers;
    private SearchActivityRecyclerViewAdapter adapter;
    private SubredditAutocompleteRecyclerViewAdapter subredditAutocompleteRecyclerViewAdapter;
    private Call<String> subredditAutocompleteCall;
    RecentSearchQueryViewModel mRecentSearchQueryViewModel;


    static {
        System.loadLibrary("native-lib");
    }

    private native void triggerNativeError();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        triggerNativeError();

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        setSupportActionBar(toolbar);

        clearSearchTextImageView.setVisibility(View.GONE);
        deleteAllSearchesButton.setVisibility(View.GONE);

        searchOnlySubreddits = getIntent().getBooleanExtra(EXTRA_SEARCH_ONLY_SUBREDDITS, false);
        searchOnlyUsers = getIntent().getBooleanExtra(EXTRA_SEARCH_ONLY_USERS, false);

        if (searchOnlySubreddits) {
            searchEditText.setHint(getText(R.string.search_only_communities_hint));
        } else if (searchOnlyUsers) {
            searchEditText.setHint(getText(R.string.search_only_users_hint));
        }

        mAccountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, null);
        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        boolean nsfw = mNsfwAndSpoilerSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.NSFW_BASE, false);

        subredditAutocompleteRecyclerViewAdapter = new SubredditAutocompleteRecyclerViewAdapter(this,
                mCustomThemeWrapper, subredditData -> {
            if (searchOnlySubreddits) {
                Intent returnIntent = new Intent();
                if (getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECTION, false)) {
                    ArrayList<SubredditWithSelection> subredditNameList = new ArrayList<>();
                    subredditNameList.add(new SubredditWithSelection(subredditData.getName(), subredditData.getIconUrl(), LemmyUtils.actorID2FullName(subredditData.getActorId())));
                    returnIntent.putParcelableArrayListExtra(RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES, subredditNameList);
                } else {
                    returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_NAME, new SubscribedSubredditData(subredditData));
                    returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_ICON_URL, subredditData.getIconUrl());
                }
                setResult(Activity.RESULT_OK, returnIntent);
            } else {
                Intent intent = new Intent(SearchActivity.this, ViewSubredditDetailActivity.class);
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, subredditData.getName());
                startActivity(intent);
            }
            finish();
        });

        if (mAccessToken == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            searchEditText.setImeOptions(searchEditText.getImeOptions() | EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING);
        }

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().trim().isEmpty()) {
                    if (subredditAutocompleteCall != null) {
                        subredditAutocompleteCall.cancel();
                    }

                    subredditAutocompleteCall = mOauthRetrofit.create(RedditAPI.class).subredditAutocomplete(APIUtils.getOAuthHeader(mAccessToken),
                            s.toString(), nsfw);
                    subredditAutocompleteCall.enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful()) {
                                ParseSubredditData.parseSubredditListingData(response.body(), nsfw, new ParseSubredditData.ParseSubredditListingDataListener() {
                                    @Override
                                    public void onParseSubredditListingDataSuccess(ArrayList<SubredditData> subredditData, String after) {
                                        subredditAutocompleteRecyclerViewAdapter.setSubreddits(subredditData);
                                        recyclerView.setAdapter(subredditAutocompleteRecyclerViewAdapter);
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
                    clearSearchTextImageView.setVisibility(View.VISIBLE);
                } else {
                    clearSearchTextImageView.setVisibility(View.GONE);
                }
            }
        });

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if ((actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN )) {
                if (!searchEditText.getText().toString().isEmpty()) {
                    search(searchEditText.getText().toString());
                    return true;
                }
            }
            return false;
        });

        clearSearchTextImageView.setOnClickListener(view -> {
            searchEditText.getText().clear();
        });

        linkHandlerImageView.setOnClickListener(view -> {
            if (!searchEditText.getText().toString().equals("")) {
                Intent intent = new Intent(this, LinkResolverActivity.class);
                intent.setData(Uri.parse(searchEditText.getText().toString()));
                startActivity(intent);
                finish();
            }
        });

        deleteAllSearchesButton.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.confirm)
                    .setMessage(R.string.confirm_delete_all_recent_searches)
                    .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                        executor.execute(() -> mRedditDataRoomDatabase.recentSearchQueryDao().deleteAllRecentSearchQueries(mAccountName));
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        });

        if (savedInstanceState != null) {
            subredditName = savedInstanceState.getString(SUBREDDIT_NAME_STATE);
            subredditIsUser = savedInstanceState.getBoolean(SUBREDDIT_IS_USER_STATE);

            if (subredditName == null) {
                subredditNameTextView.setText(R.string.all_communities);
            } else {
                subredditNameTextView.setText(subredditName);
            }
        } else {
            query = getIntent().getStringExtra(EXTRA_QUERY);
        }
        bindView();

        if (searchOnlySubreddits || searchOnlyUsers) {
            subredditNameRelativeLayout.setVisibility(View.GONE);
        } else {
            subredditNameRelativeLayout.setOnClickListener(view -> {
                Intent intent = new Intent(this, SubredditSelectionActivity.class);
                intent.putExtra(SubredditSelectionActivity.EXTRA_EXTRA_CLEAR_SELECTION, true);
                startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
            });
        }

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_SUBREDDIT_NAME)) {
            subredditName = intent.getStringExtra(EXTRA_SUBREDDIT_NAME);
            communityQualifiedName = intent.getStringExtra(EXTRA_COMMUNITY_FULL_NAME);
            subredditNameTextView.setText(subredditName);
            subredditIsUser = intent.getBooleanExtra(EXTRA_SUBREDDIT_IS_USER, false);
        }
    }

    private void bindView() {
        if (mAccountName != null) {
            adapter = new SearchActivityRecyclerViewAdapter(this, mCustomThemeWrapper, new SearchActivityRecyclerViewAdapter.ItemOnClickListener() {
                @Override
                public void onClick(String query) {
                    search(query);
                }

                @Override
                public void onDelete(RecentSearchQuery recentSearchQuery) {
                    executor.execute(() -> mRedditDataRoomDatabase.recentSearchQueryDao().deleteRecentSearchQueries(recentSearchQuery));
                }
            });
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setNestedScrollingEnabled(false);
            recyclerView.setAdapter(adapter);

            if (mSharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_SEARCH_HISTORY, true)) {
                mRecentSearchQueryViewModel = new ViewModelProvider(this,
                        new RecentSearchQueryViewModel.Factory(mRedditDataRoomDatabase, mAccountName))
                        .get(RecentSearchQueryViewModel.class);

                mRecentSearchQueryViewModel.getAllRecentSearchQueries().observe(this, recentSearchQueries -> {
                    if (recentSearchQueries != null && !recentSearchQueries.isEmpty()) {
                        divider.setVisibility(View.VISIBLE);
                        deleteAllSearchesButton.setVisibility(View.VISIBLE);
                    } else {
                        divider.setVisibility(View.GONE);
                        deleteAllSearchesButton.setVisibility(View.GONE);
                    }
                    adapter.setRecentSearchQueries(recentSearchQueries);
                });
            }
        }
    }

    private void search(String query) {
        if (query.equalsIgnoreCase("suicide") && mSharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_SUICIDE_PREVENTION_ACTIVITY, true)) {
            Intent intent = new Intent(this, SuicidePreventionActivity.class);
            intent.putExtra(SuicidePreventionActivity.EXTRA_QUERY, query);
            startActivityForResult(intent, SUICIDE_PREVENTION_ACTIVITY_REQUEST_CODE);
        } else {
            openSearchResult(query);
        }
    }

    private void openSearchResult(String query) {
        if (searchOnlySubreddits) {
            Intent intent = new Intent(SearchActivity.this, SearchSubredditsResultActivity.class);
            intent.putExtra(SearchSubredditsResultActivity.EXTRA_QUERY, query);
            intent.putExtra(SearchSubredditsResultActivity.EXTRA_IS_MULTI_SELECTION, getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECTION, false));
            startActivityForResult(intent, SUBREDDIT_SEARCH_REQUEST_CODE);
        } else if (searchOnlyUsers) {
            Intent intent = new Intent(this, SearchUsersResultActivity.class);
            intent.putExtra(SearchUsersResultActivity.EXTRA_QUERY, query);
            intent.putExtra(SearchUsersResultActivity.EXTRA_IS_MULTI_SELECTION, getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECTION, false));
            startActivityForResult(intent, USER_SEARCH_REQUEST_CODE);
        } else {
            Intent intent = new Intent(SearchActivity.this, SearchResultActivity.class);
            intent.putExtra(SearchResultActivity.EXTRA_QUERY, query);
            if (subredditName != null) {
                if (subredditIsUser) {
                    intent.putExtra(SearchResultActivity.EXTRA_SUBREDDIT_NAME, "u_" + subredditName);
                } else {
                    intent.putExtra(SearchResultActivity.EXTRA_SUBREDDIT_NAME, subredditName);
                    intent.putExtra(SearchResultActivity.EXTRA_COMMUNITY_QUALIFIED_NAME, communityQualifiedName);
                }
            }
            startActivity(intent);
            finish();
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
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(appBarLayout, null, toolbar);
        int toolbarPrimaryTextAndIconColorColor = mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor();
        searchEditText.setTextColor(toolbarPrimaryTextAndIconColorColor);
        searchEditText.setHintTextColor(mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor());
        clearSearchTextImageView.setColorFilter(mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor(), android.graphics.PorterDuff.Mode.SRC_IN);
        linkHandlerImageView.setColorFilter(mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor(), android.graphics.PorterDuff.Mode.SRC_IN);
        int colorAccent = mCustomThemeWrapper.getColorAccent();
        searchInTextView.setTextColor(colorAccent);
        deleteAllSearchesButton.setIconTint(ColorStateList.valueOf(mCustomThemeWrapper.getPrimaryIconColor()));
        subredditNameTextView.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
        divider.setBackgroundColor(mCustomThemeWrapper.getDividerColor());
        if (typeface != null) {
            Utils.setFontToAllTextViews(coordinatorLayout, typeface);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        searchEditText.requestFocus();

        if (query != null) {
            searchEditText.setText(query);
            searchEditText.setSelection(query.length());
            query = null;
        }

        Utils.showKeyboard(this, new Handler(), searchEditText);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.hideKeyboard(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == SUBREDDIT_SELECTION_REQUEST_CODE) {
                communityData = data.getParcelableExtra(SubredditSelectionActivity.EXTRA_RETURN_COMMUNITY_DATA);

                subredditIsUser = false;

                if (communityData == null) {
                    subredditNameTextView.setText(R.string.all_communities);
                } else {
                    subredditName = communityData.getName();
                    communityQualifiedName = communityData.getQualified_name();
                    subredditNameTextView.setText(subredditName);
                }
            } else if (requestCode == SUBREDDIT_SEARCH_REQUEST_CODE) {
                Intent returnIntent = new Intent();
                if (getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECTION, false)) {
                    returnIntent.putStringArrayListExtra(RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES, data.getStringArrayListExtra(SearchSubredditsResultActivity.RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES));
                } else {
                    SubscribedSubredditData communityData = data.getParcelableExtra(SearchSubredditsResultActivity.EXTRA_RETURN_SUBREDDIT_NAME);
                    String iconUrl = data.getStringExtra(SearchSubredditsResultActivity.EXTRA_RETURN_SUBREDDIT_ICON_URL);
                    returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_NAME, communityData);
                    returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_ICON_URL, iconUrl);
                }
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } else if (requestCode == USER_SEARCH_REQUEST_CODE) {
                Intent returnIntent = new Intent();
                if (getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECTION, false)) {
                    returnIntent.putStringArrayListExtra(RETURN_EXTRA_SELECTED_USERNAMES, data.getStringArrayListExtra(SearchUsersResultActivity.RETURN_EXTRA_SELECTED_USERNAMES));
                } else {
                    String username = data.getStringExtra(SearchUsersResultActivity.EXTRA_RETURN_USER_NAME);
                    String iconUrl = data.getStringExtra(SearchUsersResultActivity.EXTRA_RETURN_USER_ICON_URL);
                    returnIntent.putExtra(EXTRA_RETURN_USER_NAME, username);
                    returnIntent.putExtra(EXTRA_RETURN_USER_ICON_URL, iconUrl);
                }
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } else if (requestCode == SUICIDE_PREVENTION_ACTIVITY_REQUEST_CODE) {
                openSearchResult(data.getStringExtra(SuicidePreventionActivity.EXTRA_RETURN_QUERY));
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SUBREDDIT_NAME_STATE, subredditName);
        outState.putString(COMMUNITY_QUALIFIED_NAME, communityQualifiedName);
        outState.putBoolean(SUBREDDIT_IS_USER_STATE, subredditIsUser);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }
}
