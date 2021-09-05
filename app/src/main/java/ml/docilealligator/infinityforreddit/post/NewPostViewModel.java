package ml.docilealligator.infinityforreddit.post;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import java.util.List;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.readpost.ReadPost;
import retrofit2.Retrofit;

public class NewPostViewModel extends ViewModel {
    private PostPaging3Repository repository;
    private LiveData<PagingData<Post>> posts;
    private PostPaging3PagingSource paging3PagingSource;

    private MutableLiveData<SortType> sortTypeLiveData;
    private MutableLiveData<PostFilter> postFilterLiveData;
    private SortTypeAndPostFilterLiveData sortTypeAndPostFilterLiveData;

    public NewPostViewModel(Executor executor, Retrofit retrofit, String accessToken, String accountName,
                         SharedPreferences sharedPreferences, SharedPreferences cache, int postType,
                         SortType sortType, PostFilter postFilter, List<ReadPost> readPostList) {
        sortTypeLiveData = new MutableLiveData<>();
        sortTypeLiveData.postValue(sortType);
        postFilterLiveData = new MutableLiveData<>();
        postFilterLiveData.postValue(postFilter);

        sortTypeAndPostFilterLiveData = new SortTypeAndPostFilterLiveData(sortTypeLiveData, postFilterLiveData);

        repository = new PostPaging3Repository(executor, retrofit, accessToken, accountName,
                sharedPreferences, cache, postType, sortType, postFilter, readPostList);
        paging3PagingSource = repository.returnPagingSoruce();
        Pager<String, Post> pager = new Pager<>(new PagingConfig(25, 25, false), this::returnPagingSoruce);

        posts = Transformations.switchMap(sortTypeAndPostFilterLiveData, sortAndPostFilter -> {
            paging3PagingSource.changeSortTypeAndPostFilter(
                    sortTypeLiveData.getValue(), postFilterLiveData.getValue());
            return PagingLiveData.cachedIn(PagingLiveData.getLiveData(pager), ViewModelKt.getViewModelScope(this));
        });
    }

    public NewPostViewModel(Executor executor, Retrofit retrofit, String accessToken, String accountName,
                         SharedPreferences sharedPreferences, SharedPreferences cache, String subredditName,
                         int postType, SortType sortType, PostFilter postFilter,
                         List<ReadPost> readPostList) {
        sortTypeLiveData = new MutableLiveData<>();
        sortTypeLiveData.postValue(sortType);
        postFilterLiveData = new MutableLiveData<>();
        postFilterLiveData.postValue(postFilter);

        sortTypeAndPostFilterLiveData = new SortTypeAndPostFilterLiveData(sortTypeLiveData, postFilterLiveData);

        repository = new PostPaging3Repository(executor, retrofit, accessToken, accountName,
                sharedPreferences, cache, subredditName, postType, sortType, postFilter, readPostList);
        paging3PagingSource = repository.returnPagingSoruce();
        Pager<String, Post> pager = new Pager<>(new PagingConfig(25, 25, false), this::returnPagingSoruce);

        posts = Transformations.switchMap(sortTypeAndPostFilterLiveData, sortAndPostFilter -> {
            paging3PagingSource.changeSortTypeAndPostFilter(
                    sortTypeLiveData.getValue(), postFilterLiveData.getValue());
            return PagingLiveData.cachedIn(PagingLiveData.getLiveData(pager), ViewModelKt.getViewModelScope(this));
        });
    }

    public NewPostViewModel(Executor executor, Retrofit retrofit, String accessToken, String accountName,
                         SharedPreferences sharedPreferences, SharedPreferences cache, String username,
                         int postType, SortType sortType, PostFilter postFilter, String where,
                         List<ReadPost> readPostList) {
        sortTypeLiveData = new MutableLiveData<>();
        sortTypeLiveData.postValue(sortType);
        postFilterLiveData = new MutableLiveData<>();
        postFilterLiveData.postValue(postFilter);

        sortTypeAndPostFilterLiveData = new SortTypeAndPostFilterLiveData(sortTypeLiveData, postFilterLiveData);

        repository = new PostPaging3Repository(executor, retrofit, accessToken, accountName,
                sharedPreferences, cache, username, postType, sortType, postFilter, where, readPostList);
        paging3PagingSource = repository.returnPagingSoruce();
        Pager<String, Post> pager = new Pager<>(new PagingConfig(25, 25, false), this::returnPagingSoruce);

        posts = Transformations.switchMap(sortTypeAndPostFilterLiveData, sortAndPostFilter -> {
            paging3PagingSource.changeSortTypeAndPostFilter(
                    sortTypeLiveData.getValue(), postFilterLiveData.getValue());
            return PagingLiveData.cachedIn(PagingLiveData.getLiveData(pager), ViewModelKt.getViewModelScope(this));
        });
    }

    public NewPostViewModel(Executor executor, Retrofit retrofit, String accessToken, String accountName,
                         SharedPreferences sharedPreferences, SharedPreferences cache, String subredditName,
                         String query, String trendingSource, int postType, SortType sortType,
                         PostFilter postFilter, List<ReadPost> readPostList) {
        sortTypeLiveData = new MutableLiveData<>();
        sortTypeLiveData.postValue(sortType);
        postFilterLiveData = new MutableLiveData<>();
        postFilterLiveData.postValue(postFilter);

        sortTypeAndPostFilterLiveData = new SortTypeAndPostFilterLiveData(sortTypeLiveData, postFilterLiveData);

        repository = new PostPaging3Repository(executor, retrofit, accessToken, accountName,
                sharedPreferences, cache, subredditName, query, trendingSource, postType, sortType, postFilter,
                readPostList);
        paging3PagingSource = repository.returnPagingSoruce();
        Pager<String, Post> pager = new Pager<>(new PagingConfig(25, 25, false), this::returnPagingSoruce);

        posts = Transformations.switchMap(sortTypeAndPostFilterLiveData, sortAndPostFilter -> {
            paging3PagingSource.changeSortTypeAndPostFilter(
                    sortTypeLiveData.getValue(), postFilterLiveData.getValue());
            return PagingLiveData.cachedIn(PagingLiveData.getLiveData(pager), ViewModelKt.getViewModelScope(this));
        });
    }

    public LiveData<PagingData<Post>> getPosts() {
        return posts;
    }

    private PostPaging3PagingSource returnPagingSoruce() {
        return repository.returnPagingSoruce();
    }

    public void changeSortType(SortType sortType) {
        sortTypeLiveData.postValue(sortType);
    }

    public void changePostFilter(PostFilter postFilter) {
        postFilterLiveData.postValue(postFilter);
    }

    public void refresh() {

    }

    public void retryLoadingMore() {

    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private Executor executor;
        private Retrofit retrofit;
        private String accessToken;
        private String accountName;
        private SharedPreferences sharedPreferences;
        private SharedPreferences postFeedScrolledPositionSharedPreferences;
        private String name;
        private String query;
        private String trendingSource;
        private int postType;
        private SortType sortType;
        private PostFilter postFilter;
        private String userWhere;
        private List<ReadPost> readPostList;

        public Factory(Executor executor, Retrofit retrofit, String accessToken, String accountName,
                       SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                       int postType, SortType sortType, PostFilter postFilter, List<ReadPost> readPostList) {
            this.executor = executor;
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.sharedPreferences = sharedPreferences;
            this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
            this.postType = postType;
            this.sortType = sortType;
            this.postFilter = postFilter;
            this.readPostList = readPostList;
        }

        public Factory(Executor executor, Retrofit retrofit, String accessToken, String accountName,
                       SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                       String name, int postType, SortType sortType, PostFilter postFilter,
                       List<ReadPost> readPostList) {this.executor = executor;
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.sharedPreferences = sharedPreferences;
            this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
            this.name = name;
            this.postType = postType;
            this.sortType = sortType;
            this.postFilter = postFilter;
            this.readPostList = readPostList;
        }

        //User posts
        public Factory(Executor executor, Retrofit retrofit, String accessToken, String accountName,
                       SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                       String username, int postType, SortType sortType, PostFilter postFilter, String where,
                       List<ReadPost> readPostList) {
            this.executor = executor;
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.sharedPreferences = sharedPreferences;
            this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
            this.name = username;
            this.postType = postType;
            this.sortType = sortType;
            this.postFilter = postFilter;
            userWhere = where;
            this.readPostList = readPostList;
        }

        public Factory(Executor executor, Retrofit retrofit, String accessToken, String accountName,
                       SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                       String name, String query, String trendingSource, int postType, SortType sortType,
                       PostFilter postFilter, List<ReadPost> readPostList) {
            this.executor = executor;
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.sharedPreferences = sharedPreferences;
            this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
            this.name = name;
            this.query = query;
            this.trendingSource = trendingSource;
            this.postType = postType;
            this.sortType = sortType;
            this.postFilter = postFilter;
            this.readPostList = readPostList;
        }

        //Anonymous Front Page
        public Factory(Executor executor, Retrofit retrofit, SharedPreferences sharedPreferences,
                       String concatenatedSubredditNames, int postType, SortType sortType, PostFilter postFilter) {
            this.executor = executor;
            this.retrofit = retrofit;
            this.sharedPreferences = sharedPreferences;
            this.name = concatenatedSubredditNames;
            this.postType = postType;
            this.sortType = sortType;
            this.postFilter = postFilter;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (postType == PostDataSource.TYPE_FRONT_PAGE) {
                return (T) new NewPostViewModel(executor, retrofit, accessToken, accountName, sharedPreferences,
                        postFeedScrolledPositionSharedPreferences, postType, sortType, postFilter, readPostList);
            } else if (postType == PostDataSource.TYPE_SEARCH) {
                return (T) new NewPostViewModel(executor, retrofit, accessToken, accountName, sharedPreferences,
                        postFeedScrolledPositionSharedPreferences, name, query, trendingSource, postType, sortType,
                        postFilter, readPostList);
            } else if (postType == PostDataSource.TYPE_SUBREDDIT || postType == PostDataSource.TYPE_MULTI_REDDIT) {
                return (T) new NewPostViewModel(executor, retrofit, accessToken, accountName, sharedPreferences,
                        postFeedScrolledPositionSharedPreferences, name, postType, sortType,
                        postFilter, readPostList);
            } else if (postType == PostDataSource.TYPE_ANONYMOUS_FRONT_PAGE) {
                return (T) new NewPostViewModel(executor, retrofit, null, null, sharedPreferences,
                        null, name, postType, sortType,
                        postFilter, null);
            } else {
                return (T) new NewPostViewModel(executor, retrofit, accessToken, accountName, sharedPreferences,
                        postFeedScrolledPositionSharedPreferences, name, postType, sortType,
                        postFilter, userWhere, readPostList);
            }
        }
    }

    private static class SortTypeAndPostFilterLiveData extends MediatorLiveData<Pair<PostFilter, SortType>> {
        public SortTypeAndPostFilterLiveData(LiveData<SortType> sortTypeLiveData, LiveData<PostFilter> postFilterLiveData) {
            addSource(sortTypeLiveData, sortType -> setValue(Pair.create(postFilterLiveData.getValue(), sortType)));
            addSource(postFilterLiveData, postFilter -> setValue(Pair.create(postFilter, sortTypeLiveData.getValue())));
        }
    }
}