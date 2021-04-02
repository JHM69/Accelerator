package org.jhm69.battle_of_quiz.repository;


import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;

import org.jhm69.battle_of_quiz.dao.DaoAccess;
import org.jhm69.battle_of_quiz.db.QuizResultDatabase;
import org.jhm69.battle_of_quiz.models.Users;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.jhm69.battle_of_quiz.ui.activities.MainActivity.userId;

public class UserRepository {
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final DaoAccess resultDao;
    QuizResultDatabase database;

    public UserRepository(Application application) {
        database = QuizResultDatabase.getInstance(application);
        resultDao = database.daoAccess();
    }

    public void insertUser(final Users user) {
        new UserRepository.InsertUserAsyncTask(resultDao).execute(user);
    }

    public Users getUser() {
        return resultDao.getTask(FirebaseAuth.getInstance().getUid());
    }

    public void updateUser(Users user) {
        resultDao.update(user);
    }


    public void updateUserImage(String image) {
        new UserRepository.UpdateImageAsyncTask(resultDao).execute(image);
    }

    public void updateXp(int x) {
        new UserRepository.UpdateXPAsyncTask(resultDao).execute(x);
    }

    public void updateScore(int x) {
        new UserRepository.UpdateScoreAsyncTask(resultDao).execute(x);
    }

    public void updateWin(int x) {
        new UserRepository.UpdateWinAsyncTask(resultDao).execute(x);
    }

    public void updateLose(int x) {
        new UserRepository.UpdateLoseAsyncTask(resultDao).execute(x);
    }

    public void updateDraw(int x) {
        new UserRepository.UpdateDrawAsyncTask(resultDao).execute(x);
    }


    public void deleteDb() {
        database.clearAllTables();
    }

    public LiveData<Users> getLiveUser() {
        return resultDao.getLiveTask(FirebaseAuth.getInstance().getUid());
    }

    public Users getStaticUser() {
        return resultDao.getStaticUser(FirebaseAuth.getInstance().getUid());
    }

    private static class UpdateScoreAsyncTask extends AsyncTask<Integer, Void, Void> {
        private final DaoAccess resultDao;

        private UpdateScoreAsyncTask(DaoAccess resultDao) {
            this.resultDao = resultDao;
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            resultDao.updateScore(integers[0], userId);
            return null;
        }
    }

    private static class InsertUserAsyncTask extends AsyncTask<Users, Void, Void> {
        private final DaoAccess resultDao;

        private InsertUserAsyncTask(DaoAccess resultDao) {
            this.resultDao = resultDao;
        }

        @Override
        protected Void doInBackground(Users... results) {
            resultDao.insert(results[0]);
            return null;
        }
    }


    private static class UpdateLoseAsyncTask extends AsyncTask<Integer, Void, Void> {
        private final DaoAccess resultDao;

        private UpdateLoseAsyncTask(DaoAccess resultDao) {
            this.resultDao = resultDao;
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            resultDao.updateLose(integers[0], userId);
            return null;
        }
    }


    private static class UpdateDrawAsyncTask extends AsyncTask<Integer, Void, Void> {
        private final DaoAccess resultDao;

        private UpdateDrawAsyncTask(DaoAccess resultDao) {
            this.resultDao = resultDao;
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            resultDao.updateDraw(integers[0], userId);
            return null;
        }
    }


    private static class UpdateWinAsyncTask extends AsyncTask<Integer, Void, Void> {
        private final DaoAccess resultDao;

        private UpdateWinAsyncTask(DaoAccess resultDao) {
            this.resultDao = resultDao;
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            resultDao.updateWin(integers[0], userId);
            return null;
        }
    }


    private static class UpdateXPAsyncTask extends AsyncTask<Integer, Void, Void> {
        private final DaoAccess resultDao;

        private UpdateXPAsyncTask(DaoAccess resultDao) {
            this.resultDao = resultDao;
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            resultDao.updateXp(integers[0], userId);
            return null;
        }
    }

    private static class UpdateImageAsyncTask extends AsyncTask<String, Void, Void> {
        private final DaoAccess resultDao;

        private UpdateImageAsyncTask(DaoAccess resultDao) {
            this.resultDao = resultDao;
        }

        @Override
        protected Void doInBackground(String... integers) {
            resultDao.updateUserImage(integers[0], userId);
            return null;
        }
    }


}
