package com.chess.engine.player;

import com.chess.engine.board.Move;

public enum MoveUtils {
    INSTANCE;

    public static int exchangeScore(final Move move) {
        if (move == Move.MoveFactory.getNullMove()) {
            return 1;
        }
        return move.isAttack() ?
                5 * exchangeScore(move.getBoard().getTransitionMove()) :
                exchangeScore(move.getBoard().getTransitionMove());

    }
}
