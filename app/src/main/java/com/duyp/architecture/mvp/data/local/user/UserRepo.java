package com.duyp.architecture.mvp.data.local.user;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.duyp.androidutils.CustomSharedPreferences;
import com.duyp.androidutils.functions.PlainAction;
import com.duyp.androidutils.functions.PlainConsumer;
import com.duyp.architecture.mvp.app.AppDatabase;
import com.duyp.architecture.mvp.data.Constants;
import com.duyp.architecture.mvp.data.model.User;
import com.duyp.architecture.mvp.utils.DbTaskHelper;
import com.google.gson.Gson;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by duypham on 9/7/17.
 * User repository for storing and retrieving user data from database / shared preference
 */

public class UserRepo {

    @NonNull
    private final Gson mGson;

    @NonNull
    private final CustomSharedPreferences mSharedPreferences;

    @NonNull
    private final UserDao mUserDao;

    @Nullable
    private LiveData<User> mUserLiveData;

    @Inject
    public UserRepo(@NonNull AppDatabase appDatabase, @NonNull CustomSharedPreferences sharedPreferences, @NonNull Gson gson) {
        this.mSharedPreferences = sharedPreferences;
        this.mGson = gson;
        mUserDao = appDatabase.userDao();
    }

    public CustomSharedPreferences getSharedPreferences() {
        return  mSharedPreferences;
    }

    /**
     * Save user to shared preference
     * @param user {@link User} to be saved
     */
    public void setUser(@NonNull User user, @Nullable PlainConsumer<LiveData<User>> userConsumer) {
        // save to shared preferences
        String userJson = mGson.toJson(user);
        mSharedPreferences.setPreferences(Constants.PREF_USER, userJson);

        // save to database
        DbTaskHelper.doTaskOnBackground(() -> {
            mUserDao.addUser(user);
            // retrieve live data user from Room database
            User currentUser;
            if (mUserLiveData == null || !user.equals(mUserLiveData.getValue())) {
                mUserLiveData = mUserDao.getUser(user.getId());
            }
            if (userConsumer != null) {
                userConsumer.accept(mUserLiveData);
            }
        });
    }

    /**
     * Update user data by new user if are the same
     * @param newUser new User data
     * @return true if user updated
     */
    public boolean updateUserIfEquals(@NonNull User newUser, @Nullable PlainConsumer<LiveData<User>> consumer) {
        User user = getUser();
        if (user != null && user.getId().equals(newUser.getId())) {
            setUser(newUser, consumer);
            return true;
        }
        return false;
    }

    /**
     * Get user from shared preference
     * but we should read user data from realm database to have an observable user data
     * @return saved {@link User} from realm database, can be observable
     */
    @Nullable
    public LiveData<User> getUserLiveData() {
        if (mUserLiveData == null) {
            User savedUser = getSavedUser();
            if (savedUser != null) {
                mUserLiveData = mUserDao.getUser(savedUser.getId());
            }
        }
        return mUserLiveData;
    }

    @Nullable
    public User getUser() {
        LiveData<User> userLiveData = getUserLiveData();
        if (userLiveData != null) {
            return userLiveData.getValue();
        }
        return null;
    }

    User getSavedUser() {
        String userJson = mSharedPreferences.getPreferences(Constants.PREF_USER, "");
        if (!TextUtils.isEmpty(userJson)) {
            return fromJson(userJson);
        }
        return null;
    }

    /**
     * Save user api token to shared preferences
     * @param token user api token
     */
    public void setUserToken(String token) {
        mSharedPreferences.setPreferences(Constants.PREF_USER_TOKEN, token);
    }

    /**
     * @return saved user token
     */
    public String getUserToken() {
        return mSharedPreferences.getPreferences(Constants.PREF_USER_TOKEN, "");
    }

    /**
     * Clear user from database
     */
    public void clearUser() {
        mSharedPreferences.setPreferences(Constants.PREF_USER, "");
        mSharedPreferences.setPreferences(Constants.PREF_USER_TOKEN, "");
        DbTaskHelper.doTaskOnBackground(() -> mUserDao.deleteUser(getUser()), Throwable::printStackTrace);
    }

    /**
     * Clear all data related to user from shared preference
     */
    public void clearAll() {
        clearUser();
        DbTaskHelper.doTaskOnBackground(mUserDao::deleteAllUsers, Throwable::printStackTrace);
    }

    /**
     * create User object from json string
     * @param userJson given json
     * @return {@link User} object
     */
    public User fromJson(@NonNull String userJson) {
        return mGson.fromJson(userJson, User.class);
    }

    /**
     * Clone a user
     * @param user given user
     * @return the copy {@link User} of given user
     */
    public User cloneUser(@NonNull User user) {
        String userJson = mGson.toJson(user);
        return fromJson(userJson);
    }
}