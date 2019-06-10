package org.example.tictactoe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

/**
 * 下棋的游戏界面的Activity
 */

public class GameActivity extends Activity {
    public static final String KEY_RESTORE = "key_restore";
    public static final String PREF_RESTORE = "pref_restore";
    private MediaPlayer mMediaPlayer;
    private Handler mHandler = new Handler();
    private GameFragment mGameFragment;
    private MyDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // 创建存储游戏存档的数据库
        dbHelper = new MyDatabaseHelper(this, "GameData.db", null, 1);
        dbHelper.getWritableDatabase();

        // 获取一个跟踪fragment_game的句柄
        mGameFragment = (GameFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_game);
        boolean restore = getIntent().getBooleanExtra(KEY_RESTORE, false);
        // 获取Intent传递的标志
        if (restore) {
            // 接收到标志后从SQLite中查找最新添加的记录
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.query("Archive",null,null,null,null,null,null);
            cursor.moveToLast();
            // 读出查询到的存档 存储到gameData中
            String gameData = cursor.getString(cursor.getColumnIndex("data"));
            // putState方法进行解码 还原棋盘状态
            mGameFragment.putState(gameData);
        }
        Log.d("UT3", "restore = " + restore);
    }

    public void restartGame() {
        mGameFragment.restartGame();
    }

    // 宣布游戏结果
    public void reportWinner(final Tile.Owner winner) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
        }
        switch (winner) {
            case X:
                builder.setMessage("You win !");
                break;
            case O:
                builder.setMessage("You lose !");
                break;
            case BOTH:
                builder.setMessage("Tie !");
                break;
        }
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok_label,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
        final Dialog dialog = builder.create();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMediaPlayer = MediaPlayer.create(GameActivity.this,
                        winner == Tile.Owner.X ? R.raw.oldedgar_winner
                                : winner == Tile.Owner.O ? R.raw.notr_loser
                                : R.raw.department64_draw
                );
                mMediaPlayer.start();
                dialog.show();
            }
        }, 500);

        // 重置棋盘
        mGameFragment.initGame();
    }

    // 开始思考时显示的状态框
    public void startThinking() {
        View thinkView = findViewById(R.id.thinking);
        thinkView.setVisibility(View.VISIBLE);
    }

    // 到达时间后的状态框
    public void stopThinking() {
        View thinkView = findViewById(R.id.thinking);
        thinkView.setVisibility(View.GONE);
    }

    // 进入游戏时启动该方法
    @Override
    protected void onResume() {
        super.onResume();
        // 将音频传入多媒体播放器并且循环播放
        mMediaPlayer = MediaPlayer.create(this, R.raw.frankum_loop001e);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(null);
        // 停止播放音乐 重置多媒体播放器并且释放资源防止程序崩溃
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        // 将当前棋盘的状态存档
        String gameData = mGameFragment.getState();
        // 将存档存储到SQLite中
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("data",gameData);
        db.insert("Archive", null, values);
        Log.d("UT3", "state = " + gameData);
    }
}
