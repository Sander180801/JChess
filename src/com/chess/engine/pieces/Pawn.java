package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.gui.Table;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.chess.engine.board.Move.*;

public class Pawn extends Piece {

    private static final int[] CANDIDATE_MOVE_COORDINATE = {7, 8, 9, 16};

    private static boolean AIisDone = Table.get().AIisDone;

    public Pawn(final Alliance pieceAlliance, final int piecePosition) {
        super(PieceType.PAWN, piecePosition, pieceAlliance, true);
    }

    public Pawn(final Alliance pieceAlliance, final int piecePosition, final boolean isFirstMove) {
        super(PieceType.PAWN, piecePosition, pieceAlliance, isFirstMove);
    }

    @Override
    public Collection<Move> calculateLegalMoves(Board board) {

        final List<Move> legalMoves = new ArrayList<>();


        for (final int currentCandidateOffset : CANDIDATE_MOVE_COORDINATE) {

            final int candidateDestinationCoordinate = this.piecePosition + (this.pieceAlliance.getDirection() * currentCandidateOffset);

            if (!BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
                continue;
            }

            if (currentCandidateOffset == 8 && !board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                if (this.pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)) {
                    if (!AIisDone) {
                        String promotionPiece = Table.get().promotionPiece;
                        switch (promotionPiece) {
                            case "Queen":
                                legalMoves.add(new PawnPromotion(
                                        new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedQueen(this.pieceAlliance, candidateDestinationCoordinate)));
                            case "Rook":
                                legalMoves.add(new PawnPromotion(
                                        new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedRook(this.pieceAlliance, candidateDestinationCoordinate)));
                            case "Bishop":
                                legalMoves.add(new PawnPromotion(
                                        new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedBishop(this.pieceAlliance, candidateDestinationCoordinate)));
                            case "Knight":
                                legalMoves.add(new PawnPromotion(
                                        new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedKnight(this.pieceAlliance, candidateDestinationCoordinate)));
                        }
                    } else {
                        legalMoves.add(new PawnPromotion(
                                new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedQueen(this.pieceAlliance, candidateDestinationCoordinate)));
                        legalMoves.add(new PawnPromotion(
                                new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedRook(this.pieceAlliance, candidateDestinationCoordinate)));
                        legalMoves.add(new PawnPromotion(
                                new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedBishop(this.pieceAlliance, candidateDestinationCoordinate)));
                        legalMoves.add(new PawnPromotion(
                                new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedKnight(this.pieceAlliance, candidateDestinationCoordinate)));
                    }
                } else {
                    legalMoves.add(new PawnMove(board, this, candidateDestinationCoordinate));
                }

            } else if (currentCandidateOffset == 16 && this.isFirstMove() && (
                    (BoardUtils.INSTANCE.SECOND_RANK.get(this.piecePosition) && this.getPieceAlliance().isBlack()) || (BoardUtils.INSTANCE.SEVENTH_RANK.get(this.piecePosition) && this.getPieceAlliance().isWhite()))) {

                final int behindCandidateDestinationCoordinate = this.piecePosition + (this.pieceAlliance.getDirection() * 8);
                if (!board.getTile(behindCandidateDestinationCoordinate).isTileOccupied() &&
                        !board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                    legalMoves.add(new PawnJump(board, this, candidateDestinationCoordinate));
                }
            } else if (currentCandidateOffset == 7 &&
                    !((BoardUtils.INSTANCE.EIGHTH_COLUMN.get(this.piecePosition) && this.pieceAlliance.isWhite()) ||
                            (BoardUtils.INSTANCE.FIRST_COLUMN.get(this.piecePosition) && this.pieceAlliance.isBlack()))) {

                if (board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                    final Piece pieceOnCandidate = board.getTile(candidateDestinationCoordinate).getPiece();
                    if (this.pieceAlliance != pieceOnCandidate.getPieceAlliance()) {
                        if (this.pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)) {
                            if (!AIisDone) {
                                /*de computer denkt niet, dus er is geen computer aan de beurt.
                                  Omdat de computer zelf moet kunnen kiezen naar welk stuk hij promoveert, voegen we bij de computer vier legale zetten toe.
                                  Human players promoveren naar het stuk wat ze bij de settings gekozen hebben*/
                                String promotionPiece = Table.get().promotionPiece;
                                switch (promotionPiece) {
                                    case "Queen":
                                        legalMoves.add(new PawnPromotion(
                                                new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedQueen(this.pieceAlliance, candidateDestinationCoordinate)));
                                    case "Rook":
                                        legalMoves.add(new PawnPromotion(
                                                new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedRook(this.pieceAlliance, candidateDestinationCoordinate)));
                                    case "Bishop":
                                        legalMoves.add(new PawnPromotion(
                                                new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedBishop(this.pieceAlliance, candidateDestinationCoordinate)));
                                    case "Knight":
                                        legalMoves.add(new PawnPromotion(
                                                new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedKnight(this.pieceAlliance, candidateDestinationCoordinate)));
                                }
                            } else {
                                legalMoves.add(new PawnPromotion(
                                        new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedQueen(this.pieceAlliance, candidateDestinationCoordinate)));
                                legalMoves.add(new PawnPromotion(
                                        new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedRook(this.pieceAlliance, candidateDestinationCoordinate)));
                                legalMoves.add(new PawnPromotion(
                                        new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedBishop(this.pieceAlliance, candidateDestinationCoordinate)));
                                legalMoves.add(new PawnPromotion(
                                        new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedKnight(this.pieceAlliance, candidateDestinationCoordinate)));
                            }
                        } else {
                            legalMoves.add(
                                    new PawnAttackMove(board, this, candidateDestinationCoordinate, pieceOnCandidate));
                        }

                    }
                } else if (board.getEnPassantPawn() != null) {
                    if (board.getEnPassantPawn().getPiecePosition() == (this.piecePosition + (this.pieceAlliance.getOppositeDirection()))) {
                        final Piece piece = board.getEnPassantPawn();
                        if (this.pieceAlliance != piece.getPieceAlliance()) {
                            legalMoves.add(new PawnEnPassantAttackMove(board, this, candidateDestinationCoordinate, piece));
                        }
                    }

                }

            } else if (currentCandidateOffset == 9 &&
                    !((BoardUtils.INSTANCE.FIRST_COLUMN.get(this.piecePosition) && this.pieceAlliance.isWhite()) ||
                            (BoardUtils.INSTANCE.EIGHTH_COLUMN.get(this.piecePosition) && this.pieceAlliance.isBlack()))) {
                if (board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                    final Piece pieceOnCandidate = board.getTile(candidateDestinationCoordinate).getPiece();
                    if (this.pieceAlliance != pieceOnCandidate.getPieceAlliance()) {
                        if (this.pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)) {
                            if (!AIisDone) {
                                String promotionPiece = Table.get().promotionPiece;
                                switch (promotionPiece) {
                                    case "Queen":
                                        legalMoves.add(new PawnPromotion(
                                                new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedQueen(this.pieceAlliance, candidateDestinationCoordinate)));
                                    case "Rook":
                                        legalMoves.add(new PawnPromotion(
                                                new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedRook(this.pieceAlliance, candidateDestinationCoordinate)));
                                    case "Bishop":
                                        legalMoves.add(new PawnPromotion(
                                                new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedBishop(this.pieceAlliance, candidateDestinationCoordinate)));
                                    case "Knight":
                                        legalMoves.add(new PawnPromotion(
                                                new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedKnight(this.pieceAlliance, candidateDestinationCoordinate)));
                                }
                            } else {
                                legalMoves.add(new PawnPromotion(
                                        new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedQueen(this.pieceAlliance, candidateDestinationCoordinate)));
                                legalMoves.add(new PawnPromotion(
                                        new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedRook(this.pieceAlliance, candidateDestinationCoordinate)));
                                legalMoves.add(new PawnPromotion(
                                        new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedBishop(this.pieceAlliance, candidateDestinationCoordinate)));
                                legalMoves.add(new PawnPromotion(
                                        new PawnMove(board, this, candidateDestinationCoordinate), PieceUtils.INSTANCE.getMovedKnight(this.pieceAlliance, candidateDestinationCoordinate)));
                            }


                        } else {
                            legalMoves.add(
                                    new PawnAttackMove(board, this, candidateDestinationCoordinate,
                                            board.getPiece(candidateDestinationCoordinate)));
                        }
                    }
                } else if (board.getEnPassantPawn() != null) {
                    if (board.getEnPassantPawn().getPiecePosition() == (this.piecePosition - (this.pieceAlliance.getOppositeDirection()))) {
                        final Piece piece = board.getEnPassantPawn();
                        if (this.pieceAlliance != piece.getPieceAlliance()) {
                            legalMoves.add(new PawnEnPassantAttackMove(board, this, candidateDestinationCoordinate, piece));
                        }
                    }

                }


            }
        }

        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public int locationBonus() {
        return this.pieceAlliance.pawnBonus(this.piecePosition);
    }

    @Override
    public Pawn movePiece(Move move) {
        return new Pawn(move.getMovedPiece().getPieceAlliance(), move.getDestinationCoordinate());
    }

    @Override
    public String toString() {
        return PieceType.PAWN.toString();
    }


}
