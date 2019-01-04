package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.Player;

import java.util.Collection;

public final class StandardBoardEvaluator implements BoardEvaluator {
    private static final int CHECK_BONUS = 50;
    private static final int CHECK_MATE_BONUS = 10000;
    private static final int DEPTH_BONUS = 80;
    private static final int CASTLE_BONUS = 60;
    private static final StandardBoardEvaluator INSTANCE = new StandardBoardEvaluator();

    private StandardBoardEvaluator() {
    }

    public static StandardBoardEvaluator get() {
        return INSTANCE;
    }

    @Override
    public int evaluate(final Board board, final int depth) {
        return scorePlayer(board, board.whitePlayer(), depth) -
                scorePlayer(board, board.blackPlayer(), depth);
    }

    private int scorePlayer(final Board board,
                            final Player player,
                            final int depth) {

        return pieceValue(player) + mobility(player) + check(player) +
                checkmate(player, depth) + castled(player) + centerOccupationBonus(player) + centerAttackBonus(player) + pawnStructure(player)
                + rookStructure(board, player);
    }

    private static int centerOccupationBonus(final Player player) {
        int occupationBonus = 0;
        if ((player.getActivePieces().size() + player.getOpponent().getActivePieces().size()) >= 10) {
            for (Piece piece : player.getActivePieces()) {
                if (piece.getPiecePosition() == BoardUtils.INSTANCE.getCoordinateAtPosition("d5") ||
                        piece.getPiecePosition() == BoardUtils.INSTANCE.getCoordinateAtPosition("e5") ||
                        piece.getPiecePosition() == BoardUtils.INSTANCE.getCoordinateAtPosition("d4") ||
                        piece.getPiecePosition() == BoardUtils.INSTANCE.getCoordinateAtPosition("e4")) {
                    occupationBonus += 4;
                }

            }
        }
        return occupationBonus;
    }


    private static int centerAttackBonus(final Player player) {

        int attackBonus = 0;

        if (player.getActivePieces().size() + player.getOpponent().getActivePieces().size() >= 10) {

            Collection<Move> attackMovesOnD5 = Player.calculateAttacksOnTile(BoardUtils.INSTANCE.getCoordinateAtPosition("d5"), player.getLegalMoves());
            Collection<Move> attackMovesOnE5 = Player.calculateAttacksOnTile(BoardUtils.INSTANCE.getCoordinateAtPosition("e5"), player.getLegalMoves());
            Collection<Move> attackMovesOnD4 = Player.calculateAttacksOnTile(BoardUtils.INSTANCE.getCoordinateAtPosition("d4"), player.getLegalMoves());
            Collection<Move> attackMovesOnE4 = Player.calculateAttacksOnTile(BoardUtils.INSTANCE.getCoordinateAtPosition("e4"), player.getLegalMoves());
            if(player.getAlliance().isWhite()){
                attackBonus += (attackMovesOnD5.size() + attackMovesOnE5.size()*2 + attackMovesOnD4.size() + attackMovesOnE4.size()*2);
            }
            else if (player.getAlliance().isBlack()){
                attackBonus += (attackMovesOnD5.size()*2 + attackMovesOnE5.size() + attackMovesOnD4.size()*2 + attackMovesOnE4.size());
            }
        }

        if (attackBonus <= 0) {
            return 0;
        } else {
            return attackBonus;
        }
    }


    private static int castled(Player player) {
        if (player.getActivePieces().size() + player.getOpponent().getActivePieces().size() >= 10) {
            return player.isCastled() ? CASTLE_BONUS : 0;
        }
        return 0;
    }

    private static int checkmate(Player player, int depth) {

        return player.getOpponent().isInCheckMate() ? CHECK_MATE_BONUS * (depthBonus(depth) + Math.min(120, player.getActivePieces().size() * 10 + player.getOpponent().getActivePieces().size() * 10)) : 0;
    }

    private static int depthBonus(int depth) {
        return depth == 0 ? 1 : DEPTH_BONUS * depth;
    }

    private static int check(Player player) {
        return player.getOpponent().isInCheck() ? CHECK_BONUS : 0;
    }

    private static int mobility(Player player) {
        return player.getLegalMoves().size();
    }

    private static int pawnStructure(final Player player) {
        return PawnStructureAnalyzer.get().pawnStructureScore(player);
    }

    private static int rookStructure(final Board board, final Player player) {
        return RookStructureAnalyzer.get().rookStructureScore(board, player);
    }

    private static int pieceValue(final Player player) {
        int pieceValueScore = 0;
        for (final Piece piece : player.getActivePieces()) {
            pieceValueScore += piece.getPieceValue();
        }
        return pieceValueScore;
    }
}
