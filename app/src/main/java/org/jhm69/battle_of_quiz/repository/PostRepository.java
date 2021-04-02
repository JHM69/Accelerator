package org.jhm69.battle_of_quiz.repository;import android.app.Application;import android.os.AsyncTask;import androidx.lifecycle.LiveData;import org.jhm69.battle_of_quiz.dao.DaoAccess;import org.jhm69.battle_of_quiz.db.QuizResultDatabase;import org.jhm69.battle_of_quiz.models.Post;import java.util.List;public class PostRepository {    private final DaoAccess resultDao;    private final LiveData<List<Post>> allPosts;    public PostRepository(Application application) {        QuizResultDatabase database = QuizResultDatabase.getInstance(application);        resultDao = database.daoAccess();        allPosts = resultDao.getAllSavedPost();    }    public void insert(Post result) {        new PostRepository.InsertPostAsyncTask(resultDao).execute(result);    }    public void delete(Post result) {        new PostRepository.DeletePostAsyncTask(resultDao).execute(result);    }    public LiveData<List<Post>> getAllPosts() {        return allPosts;    }    private static class InsertPostAsyncTask extends AsyncTask<Post, Void, Void> {        private final DaoAccess resultDao;        private InsertPostAsyncTask(DaoAccess resultDao) {            this.resultDao = resultDao;        }        @Override        protected Void doInBackground(Post... results) {            resultDao.insertPost(results[0]);            return null;        }    }    private static class DeletePostAsyncTask extends AsyncTask<Post, Void, Void> {        private final DaoAccess resultDao;        private DeletePostAsyncTask(DaoAccess resultDao) {            this.resultDao = resultDao;        }        @Override        protected Void doInBackground(Post... results) {            resultDao.deletePost(results[0]);            return null;        }    }}