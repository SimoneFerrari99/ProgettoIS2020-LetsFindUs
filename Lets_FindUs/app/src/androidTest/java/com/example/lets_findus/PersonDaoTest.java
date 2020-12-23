package com.example.lets_findus;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.lets_findus.utilities.AppDatabase;
import com.example.lets_findus.utilities.Person;
import com.example.lets_findus.utilities.PersonDao;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.Executors;

@RunWith(AndroidJUnit4.class)
public class PersonDaoTest {
    private PersonDao pd;
    private AppDatabase db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.databaseBuilder(context, AppDatabase.class, "meeting_db").build();
        pd = db.personDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void storePerson() throws Exception {
        Person p = new Person("", "Gazz", Person.Sex.MALE, 1999);
        pd.insert(p).get();
    }


    @Test
    public void storeAndUpdatePerson() throws Exception {
        Person p = new Person("", "Gazz", Person.Sex.MALE, 1999);
        ListenableFuture<Long> inserted = pd.insert(p);
        Futures.addCallback(inserted, new FutureCallback<Long>() {
            @Override
            public void onSuccess(@NullableDecl Long result) {
                ListenableFuture<Person> lastIns = pd.getLastPersonInserted();
                Futures.addCallback(lastIns, new FutureCallback<Person>() {
                    @Override
                    public void onSuccess(@NullableDecl Person result) {
                        result.nickname = "Pluto";
                        pd.update(result);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.d("storeAndUpdatePerson", "Select failed");
                    }
                }, Executors.newSingleThreadExecutor());
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("storeAndUpdatePerson", "Insert failed");
            }
        }, Executors.newSingleThreadExecutor());
    }

    @Test
    public void loadPersonFromId() throws Exception{
        ListenableFuture<Person> getPerson = pd.getPersonById(1);
        Futures.addCallback(getPerson, new FutureCallback<Person>() {
            @Override
            public void onSuccess(@NullableDecl Person result) {
                assert result != null;
                Log.d("TestSuccess", "Nickname: "+result.nickname);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("TestFailure", "Select failure");
            }

        }, Executors.newSingleThreadExecutor());
    }

    @Test
    public void deletePerson() throws Exception{
        ListenableFuture<Person> getPerson = pd.getPersonById(1);
        Futures.addCallback(getPerson, new FutureCallback<Person>() {
            @Override
            public void onSuccess(@NullableDecl Person result) {
                pd.deleteAll(result);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("TestFailure", "Select failure");
            }

        }, Executors.newSingleThreadExecutor());
        pd.deletePersonById(3);
    }

    @Test
    public void selectLastPerson() throws Exception{
        Person p = new Person("", "Ciccio", Person.Sex.OTHER, 1999);
        ListenableFuture<Long> ins = pd.insert(p);
        Futures.addCallback(ins, new FutureCallback<Long>() {
            @Override
            public void onSuccess(@NullableDecl Long result) {
                ListenableFuture<Person> lastIns = pd.getLastPersonInserted();
                Futures.addCallback(lastIns, new FutureCallback<Person>() {
                    @Override
                    public void onSuccess(@NullableDecl Person result) {
                        Log.d("TestSuccess", "Nickname: " + result.nickname);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.d("TestFailure", "Select failure");
                    }
                }, Executors.newSingleThreadExecutor());
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("TestFailure", "Insert failure");
            }
        }, Executors.newSingleThreadExecutor());
    }
}
