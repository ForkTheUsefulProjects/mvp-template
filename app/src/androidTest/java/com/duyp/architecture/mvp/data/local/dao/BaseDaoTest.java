package com.duyp.architecture.mvp.data.local.dao;

import android.support.annotation.NonNull;
import android.util.Log;

import com.duyp.architecture.mvp.BaseTest;
import com.duyp.architecture.mvp.dagger.TestAppComponent;
import com.duyp.architecture.mvp.data.local.RealmDatabase;
import com.duyp.architecture.mvp.data.model.Issue;
import com.duyp.architecture.mvp.data.model.Repository;
import com.duyp.architecture.mvp.data.model.User;
import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;

import java.util.Date;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by duypham on 9/21/17.
 *
 */
public abstract class BaseDaoTest extends BaseTest{

    public static final String TAG = "test";

    @Inject
    RealmDatabase realmDatabase;

    @Inject
    Gson gson;

    @Inject
    Realm realm;

    @Before
    public void setUp() throws Exception {
        Log.d(TAG, "setUp: ");
        inject(getTestApplication().getAppComponent());
    }

    @After
    public void tearDown() throws Exception {
        Log.d(TAG, "tearDown: ");
        realmDatabase.clearAll();
        getTestApplication().clearAppComponent();
    }

    public abstract void inject(TestAppComponent appComponent);

    User sampleUser(Long id) {
        return sampleUser(id, "duyp");
    }

    User sampleUser(Long id, String login) {
        User user = new User();
        user.setLogin(login);
        user.setBio("This is test user");
        user.setId(id);
        return user;
    }

    Issue sampleIssue(Long id) {
        return sampleIssue(id, 123L);
    }

    Issue sampleIssue(Long id, Long repoId) {
        Issue issue = new Issue();
        issue.setId(id);
        issue.setRepoId(repoId);
        issue.setTitle("This is title of issue " + id);
        issue.setBody("This is body of issue " + id);
        issue.setCreatedAt(new Date());
        issue.setUser(sampleUser(43L));
        issue.setLabels(new RealmList<>());
        return issue;
    }

    Repository sampleRepository(Long id, @NonNull User owner) {
        Repository repository = new Repository();
        repository.setId(id);
        repository.setOwner(owner);
        repository.setName("repo" + id);
        repository.setFullName(owner.getLogin() + "/" + repository.getName());
        return repository;
    }
}
