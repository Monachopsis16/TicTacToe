package org.example.tictactoe;

import android.app.Fragment;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.HashSet;
import java.util.Set;

/**
 * 实现游戏细节的类
 */

public class GameFragment extends Fragment {
    // 所有使用到的数据结构

    // 存储小棋盘
    static private int mLargeIds[] = {R.id.large1, R.id.large2, R.id.large3,
            R.id.large4, R.id.large5, R.id.large6, R.id.large7, R.id.large8,
            R.id.large9,};
    // 存储每个棋盘的九宫格
    static private int mSmallIds[] = {R.id.small1, R.id.small2, R.id.small3,
            R.id.small4, R.id.small5, R.id.small6, R.id.small7, R.id.small8,
            R.id.small9,};
    // Handler对象能够将事件推迟一定时间完成
    private Handler mHandler = new Handler();
    // 整个棋盘
    private Tile mEntireBoard = new Tile(this);
    // 九个小棋盘
    private Tile mLargeTiles[] = new Tile[9];
    // 所有的格子
    private Tile mSmallTiles[][] = new Tile[9][9];
    // 先手玩家
    private Tile.Owner mPlayer = Tile.Owner.X;
    // 存储所有能够下棋的格子
    private Set<Tile> mAvailable = new HashSet<Tile>();
    private int mSoundX, mSoundO, mSoundMiss, mSoundRewind;
    private SoundPool mSoundPool;
    private float mVolume = 1f;
    // 存储上一次下棋所在的小格子决定了换边后下棋所在的的小棋盘
    private int mLastSmall;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设备配置发生变化时保留这个片段
        setRetainInstance(true);
        initGame();
        mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        mSoundX = mSoundPool.load(getActivity(), R.raw.sergenious_movex, 1);
        mSoundO = mSoundPool.load(getActivity(), R.raw.sergenious_moveo, 1);
        mSoundMiss = mSoundPool.load(getActivity(), R.raw.erkanozan_miss, 1);
        mSoundRewind = mSoundPool.load(getActivity(), R.raw.joanne_rewind, 1);
    }

    private void clearAvailable() {
        mAvailable.clear();
    }

    private void addAvailable(Tile tile) {
        tile.animate();
        mAvailable.add(tile);
    }

    public boolean isAvailable(Tile tile) {
        return mAvailable.contains(tile);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =
                inflater.inflate(R.layout.large_board, container, false);
        initViews(rootView);
        updateAllTiles();
        return rootView;
    }

    private void initViews(View rootView) {
        mEntireBoard.setView(rootView);
        for (int large = 0; large < 9; large++) {
            View outer = rootView.findViewById(mLargeIds[large]);
            mLargeTiles[large].setView(outer);

            for (int small = 0; small < 9; small++) {
                ImageButton inner = (ImageButton) outer.findViewById
                        (mSmallIds[small]);
                final int fLarge = large;
                final int fSmall = small;
                final Tile smallTile = mSmallTiles[large][small];
                smallTile.setView(inner);
                inner.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        smallTile.animate();
                        if (isAvailable(smallTile)) {
                            ((GameActivity) getActivity()).startThinking();
                            mSoundPool.play(mSoundX, mVolume, mVolume, 1, 0, 1f);
                            makeMove(fLarge, fSmall);
                            think();
                        } else {
                            mSoundPool.play(mSoundMiss, mVolume, mVolume, 1, 0, 1f);
                        }
                    }
                });
            }
        }
    }

    // AI的思考过程
    private void think() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null) return;
                if (mEntireBoard.getOwner() == Tile.Owner.NEITHER) {
                    int move[] = new int[2];
                    pickMove(move);
                    if (move[0] != -1 && move[1] != -1) {
                        switchTurns();
                        mSoundPool.play(mSoundO, mVolume, mVolume,
                                1, 0, 1f);
                        makeMove(move[0], move[1]);
                        switchTurns();
                    }
                }
                ((GameActivity) getActivity()).stopThinking();
            }
        }, 1000);
    }

    // 根据evaluate方法选择收益最大的走法
    private void pickMove(int move[]) {
        Tile.Owner opponent = mPlayer == Tile.Owner.X ? Tile.Owner.O : Tile
                .Owner.X;
        int bestLarge = -1;
        int bestSmall = -1;
        int bestValue = Integer.MAX_VALUE;
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                Tile smallTile = mSmallTiles[large][small];
                if (isAvailable(smallTile)) {
                    // 尝试落子并且得分
                    Tile newBoard = mEntireBoard.deepCopy();
                    newBoard.getSubTiles()[large].getSubTiles()[small]
                            .setOwner(opponent);
                    int value = newBoard.evaluate();
                    Log.d("UT3",
                            "Moving to " + large + ", " + small + " gives value " +
                                    "" + value
                    );
                    if (value < bestValue) {
                        bestLarge = large;
                        bestSmall = small;
                        bestValue = value;
                    }
                }
            }
        }
        move[0] = bestLarge;
        move[1] = bestSmall;
        Log.d("UT3", "Best move is " + bestLarge + ", " + bestSmall);
    }

    //换边 双方轮流下棋
    private void switchTurns() {
        mPlayer = mPlayer == Tile.Owner.X ? Tile.Owner.O : Tile
                .Owner.X;
    }

    // 模拟下棋到格子中的过程
    private void makeMove(int large, int small) {
        mLastSmall = small;
        Tile smallTile = mSmallTiles[large][small];
        Tile largeTile = mLargeTiles[large];
        smallTile.setOwner(mPlayer);
        setAvailableFromLastMove(small);
        Tile.Owner oldWinner = largeTile.getOwner();
        Tile.Owner winner = largeTile.findWinner();
        if (winner != oldWinner) {
            largeTile.animate();
            largeTile.setOwner(winner);
        }
        winner = mEntireBoard.findWinner();
        mEntireBoard.setOwner(winner);
        updateAllTiles();
        if (winner != Tile.Owner.NEITHER) {
            ((GameActivity) getActivity()).reportWinner(winner);
        }
    }

    // 重新开始一局游戏
    public void restartGame() {
        mSoundPool.play(mSoundRewind, mVolume, mVolume, 1, 0, 1f);
        initGame();
        initViews(getView());
        updateAllTiles();
    }

    // 初始化游戏
    public void initGame() {
        Log.d("UT3", "init game");
        mEntireBoard = new Tile(this);
        // 创建小棋盘和格子
        for (int large = 0; large < 9; large++) {
            mLargeTiles[large] = new Tile(this);
            for (int small = 0; small < 9; small++) {
                mSmallTiles[large][small] = new Tile(this);
            }
            mLargeTiles[large].setSubTiles(mSmallTiles[large]);
        }
        mEntireBoard.setSubTiles(mLargeTiles);

        // 设置先手玩家可下棋格子
        mLastSmall = -1;
        setAvailableFromLastMove(mLastSmall);
    }

    // 根据上一步棋子所在小棋盘中的位置找到目标棋盘中可下棋的格子
    private void setAvailableFromLastMove(int small) {
        clearAvailable();
        // 找到换边后的目标棋盘所有可下棋的格子
        if (small != -1) {
            for (int dest = 0; dest < 9; dest++) {
                Tile tile = mSmallTiles[small][dest];
                if (tile.getOwner() == Tile.Owner.NEITHER)
                    addAvailable(tile);
            }
        }
        // 如果目标小棋盘中没有格子可供下棋 则令所有未被占据的格子都可下棋
        if (mAvailable.isEmpty()) {
            setAllAvailable();
        }
    }

    // 令所有的小棋盘中未被占据的小格子均可落子
    private void setAllAvailable() {
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                Tile tile = mSmallTiles[large][small];
                if (tile.getOwner() == Tile.Owner.NEITHER)
                    addAvailable(tile);
            }
        }
    }

    // 根据状态更新所有小棋盘和小格子的显示样式
    private void updateAllTiles() {
        mEntireBoard.updateDrawableState();
        for (int large = 0; large < 9; large++) {
            mLargeTiles[large].updateDrawableState();
            for (int small = 0; small < 9; small++) {
                mSmallTiles[large][small].updateDrawableState();
            }
        }
    }

    /**
     * 继续游戏后恢复游戏状态依赖的功能是基于编码和解码的过程:
     * getState将当前的游戏状态编码成字符串
     * 先将上一次棋子的位置记录以确定当前落子的棋盘
     * 然后将每一个小棋盘的状态(被一方占据/平局/未被占据)
     * 最后将所有的格子的状态记录
     * putState将getState记录的状态的字符串根据逗号分割后逐个还原
     * 最终恢复游戏状态 实现解码的过程
     */

    /**
     * 将游戏状态进行编码
     */
    public String getState() {
        StringBuilder builder = new StringBuilder(); // 存储游戏状态的字符串 使用逗号进行分隔
        builder.append(mLastSmall);                  // 添加上一次下棋时棋子所在的格子的位置
        builder.append(',');
        // 存储每一个小棋盘和其九宫格的状态
        for (int large = 0; large < 9; large++) {
            builder.append(mLargeTiles[large].getOwner().name());
            builder.append(',');
            for (int small = 0; small < 9; small++) {
                builder.append(mSmallTiles[large][small].getOwner().name());
                builder.append(',');
            }
        }
        return builder.toString();
    }

    /**
     * 将字符串还原为游戏状态
     */
    public void putState(String gameData) {
        String[] fields = gameData.split(",");
        int index = 0;
        mLastSmall = Integer.parseInt(fields[index++]);
        // 恢复每一个小棋盘和其九宫格的状态
        for (int large = 0; large < 9; large++) {
            Tile.Owner owner = Tile.Owner.valueOf(fields[index++]);
            mLargeTiles[large].setOwner(owner);
            for (int small = 0; small < 9; small++) {
                owner = Tile.Owner.valueOf(fields[index++]);
                mSmallTiles[large][small].setOwner(owner);
            }
        }
        // 寻找目标棋盘中可下棋的位置
        setAvailableFromLastMove(mLastSmall);
        updateAllTiles();
    }
}

