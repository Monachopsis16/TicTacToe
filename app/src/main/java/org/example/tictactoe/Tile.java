package org.example.tictactoe;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageButton;

/**
 * Tile 类表示任何层次的棋盘格子:
 * 它可以是可包含X或O的最小格子、包含9个格子的小棋盘或包含9个小棋盘的大棋盘。
 */
public class Tile {
    // 定义Tile类的四种状态:被X占据, 被O占据, 未被占据, 打成平局(一块小棋盘格子下满 但没有一方获胜)
    public enum Owner {
        X, O, NEITHER, BOTH
    }

    // 定义棋盘的格子四种等级
    private static final int LEVEL_X = 0;
    private static final int LEVEL_O = 1;
    private static final int LEVEL_BLANK = 2;
    private static final int LEVEL_AVAILABLE = 3;
    private static final int LEVEL_TIE = 3;

    private final GameFragment mGame;
    private Owner mOwner = Owner.NEITHER;
    private View mView;
    private Tile mSubTiles[];

    public Tile(GameFragment game) {
        this.mGame = game;
    }

    public Tile deepCopy() {
        Tile tile = new Tile(mGame);
        tile.setOwner(getOwner());
        if (getSubTiles() != null) {
            Tile newTiles[] = new Tile[9];
            Tile oldTiles[] = getSubTiles();
            for (int child = 0; child < 9; child++) {
                newTiles[child] = oldTiles[child].deepCopy();
            }
            tile.setSubTiles(newTiles);
        }
        return tile;
    }

    public View getView() {
        return mView;
    }

    public void setView(View view) {
        this.mView = view;
    }

    public Owner getOwner() {
        return mOwner;
    }

    public void setOwner(Owner owner) {
        this.mOwner = owner;
    }

    public Tile[] getSubTiles() {
        return mSubTiles;
    }

    public void setSubTiles(Tile[] subTiles) {
        this.mSubTiles = subTiles;
    }

    public void updateDrawableState() {
        if (mView == null) return;
        int level = getLevel();
        if (mView.getBackground() != null) {
            mView.getBackground().setLevel(level);
        }
        if (mView instanceof ImageButton) {
            Drawable drawable = ((ImageButton) mView).getDrawable();
            drawable.setLevel(level);
        }
    }

    private int getLevel() {
        int level = LEVEL_BLANK;
        switch (mOwner) {
            case X:
                level = LEVEL_X;
                break;
            case O:
                level = LEVEL_O;
                break;
            case BOTH:
                level = LEVEL_TIE;
                break;
            case NEITHER:
                level = mGame.isAvailable(this) ? LEVEL_AVAILABLE : LEVEL_BLANK;
                break;
        }
        return level;
    }

    private void countCaptures(int totalX[], int totalO[]) {
        int capturedX, capturedO;
        // 检查每列
        for (int row = 0; row < 3; row++) {
            capturedX = capturedO = 0;
            for (int col = 0; col < 3; col++) {
                Owner owner = mSubTiles[3 * row + col].getOwner();
                if (owner == Owner.X || owner == Owner.BOTH) capturedX++;
                if (owner == Owner.O || owner == Owner.BOTH) capturedO++;
            }
            totalX[capturedX]++;
            totalO[capturedO]++;
        }

        // 检查每行
        for (int col = 0; col < 3; col++) {
            capturedX = capturedO = 0;
            for (int row = 0; row < 3; row++) {
                Owner owner = mSubTiles[3 * row + col].getOwner();
                if (owner == Owner.X || owner == Owner.BOTH) capturedX++;
                if (owner == Owner.O || owner == Owner.BOTH) capturedO++;
            }
            totalX[capturedX]++;
            totalO[capturedO]++;
        }

        // 检查对角线
        capturedX = capturedO = 0;
        for (int diag = 0; diag < 3; diag++) {
            Owner owner = mSubTiles[3 * diag + diag].getOwner();
            if (owner == Owner.X || owner == Owner.BOTH) capturedX++;
            if (owner == Owner.O || owner == Owner.BOTH) capturedO++;
        }
        totalX[capturedX]++;
        totalO[capturedO]++;
        capturedX = capturedO = 0;
        for (int diag = 0; diag < 3; diag++) {
            Owner owner = mSubTiles[3 * diag + (2 - diag)].getOwner();
            if (owner == Owner.X || owner == Owner.BOTH) capturedX++;
            if (owner == Owner.O || owner == Owner.BOTH) capturedO++;
        }
        totalX[capturedX]++;
        totalO[capturedO]++;
    }

    public Owner findWinner() {
        // 若已经有占据者 直接返回
        if (getOwner() != Owner.NEITHER)
            return getOwner();

        int totalX[] = new int[4];
        int totalO[] = new int[4];
        countCaptures(totalX, totalO);
        if (totalX[3] > 0) return Owner.X;
        if (totalO[3] > 0) return Owner.O;

        // 检查是否为平局
        int total = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Owner owner = mSubTiles[3 * row + col].getOwner();
                if (owner != Owner.NEITHER) total++;
            }
            if (total == 9) return Owner.BOTH;
        }

        // 双方均未占据
        return Owner.NEITHER;
    }

    //评估函数 将下棋的情况进行量化
    public int evaluate() {
        switch (getOwner()) {
            case X:
                return 100;
            case O:
                return -100;
            case NEITHER:
                int total = 0;
                if (getSubTiles() != null) {
                    for (int tile = 0; tile < 9; tile++) {
                        total += getSubTiles()[tile].evaluate();
                    }
                    int totalX[] = new int[4];
                    int totalO[] = new int[4];
                    countCaptures(totalX, totalO);
                    total = total * 100 + totalX[1] * 2 + totalX[2] * 16 +
                            totalX[3] * 128 - totalO[1] * 2 - totalO[2] * 16 - totalO[3] * 128;
                }
                return total;
        }
        return 0;
    }

    public void animate() {
        Animator anim = AnimatorInflater.loadAnimator(mGame.getActivity(),
                R.animator.tictactoe);
        if (getView() != null) {
            anim.setTarget(getView());
            anim.start();
        }
    }
}
