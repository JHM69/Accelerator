package org.jhm69.battle_of_quiz.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import org.jhm69.battle_of_quiz.messege.model.Chat;
import org.jhm69.battle_of_quiz.messege.model.Chatlist;
import org.jhm69.battle_of_quiz.models.Post;
import org.jhm69.battle_of_quiz.models.Users;
import org.jhm69.battle_of_quiz.ui.activities.quiz.BattleModel;
import org.jhm69.battle_of_quiz.ui.activities.quiz.Result;

import java.util.List;

@Dao
public interface DaoAccess {

    //--------------for user handing
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Users member);

    @Query("SELECT * FROM Users WHERE id =:id")
    Users getTask(String id);

    @Query("SELECT * FROM Users WHERE id =:uid")
    LiveData<Users> getLiveTask(String uid);

    @Query("SELECT * FROM Users WHERE id =:uid")
    Users getStaticUser(String uid);

    @Update
    void update(Users users);

    @Query("UPDATE Users SET score=score+:score WHERE id = :id")
    void updateScore(int score, String id);

    @Query("UPDATE Users SET win=win+:score WHERE id = :id")
    void updateWin(int score, String id);

    @Query("UPDATE Users SET lose=lose+:score WHERE id = :id")
    void updateLose(int score, String id);

    @Query("UPDATE Users SET draw=draw+:score WHERE id = :id")
    void updateDraw(int score, String id);

    @Query("UPDATE Users SET image=:df WHERE id = :id")
    void updateUserImage(String df, String id);

    @Query("UPDATE Users SET reward=reward+:score WHERE id = :id")
    void updateXp(int score, String id);

    @Query("UPDATE Users SET reward=:score WHERE id = :id")
    void setReward(int score, String id);



    //-----------------------------------


    @Query("UPDATE Users SET score=:score WHERE id = :id")
    void setScore(int score, String id);

    @Query("UPDATE Users SET win=:score WHERE id = :id")
    void setWin(int score, String id);

    @Query("UPDATE Users SET lose=:score WHERE id = :id")
    void setLose(int score, String id);

    @Query("UPDATE Users SET draw=:score WHERE id = :id")
    void setDraw(int score, String id);

    @Query("UPDATE Users SET reward=:score WHERE id = :id")
    void setXp(int score, String id);

    //------------Offline quize result handling
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertResult(BattleModel battleModel);

    @Query("SELECT * FROM BattleModel WHERE battleId =:taskId")
    LiveData<BattleModel> getResult(String taskId);


    @Query("SELECT * FROM BattleModel WHERE battleId =:taskId")
    BattleModel getBattleOffline(String taskId);


    @Query("SELECT * FROM BattleModel ORDER BY timestamp")
    LiveData<List<BattleModel>> getAllResult();

    @Query("DELETE FROM BattleModel WHERE battleId = :result")
    void deleteResult(String result);


    //------------Offline quize result handling


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBattleResult(Result result);

    @Query("SELECT * FROM Result WHERE battleId =:taskId")
    LiveData<Result> getResultOfBattle(String taskId);

    @Query("SELECT * FROM Result ORDER BY timestamp")
    LiveData<List<Result>> getAllResultOfBattle();

    @Query("SELECT * FROM Result WHERE battleId =:id")
    Result resultExists(String id);

    @Delete
    void deleteResultOfBattle(Result result);

    @Update
    void updateResultOfBattle(Result result);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPost(Post post);

    @Query("SELECT * FROM Post WHERE postId =:taskId")
    LiveData<Post> getPost(String taskId);

    @Query("SELECT * FROM Post ORDER BY timestamp")
    LiveData<List<Post>> getAllSavedPost();

    @Delete
    void deletePost(Post result);

    @Query("SELECT * FROM Result WHERE `action` =:x ORDER BY timestamp")
    LiveData<List<Result>> getAllInvites(int x);
    @Query("SELECT * FROM Result WHERE `battleId` =:battleIdNew")
    Result getNewResult(String battleIdNew);

    @Query("SELECT * FROM Chatlist ORDER BY lastTimestamp DESC")
    LiveData<List<Chatlist>> getAllUser();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(Chatlist chatlist);


    @Query("DELETE FROM Chatlist WHERE id = :string")
    int deleteChat(String string);

    @Query("SELECT * FROM Chat WHERE sender = :u OR receiver = :u ORDER BY timestamp ASC")
    LiveData<List<Chat>> getAllChat(String u);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insertMessage(Chat chat);

    @Query("DELETE FROM Users WHERE id = :id")
    void deleteUser(String id);
}
