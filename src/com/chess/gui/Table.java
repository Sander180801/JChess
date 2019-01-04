package com.chess.gui;

import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.MoveTransition;
import com.chess.engine.player.Player;
import com.chess.engine.player.ai.StandardBoardEvaluator;
import com.chess.engine.player.ai.StockAlphaBeta;
import com.chess.fen.FenUtilities;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static javax.swing.JFrame.setDefaultLookAndFeelDecorated;
import static javax.swing.SwingUtilities.*;

public class Table extends Observable {


    public boolean AIisDone;
    private final JFrame gameFrame;
    private final GameHistoryPanel gameHistoryPanel;
    private final TakenPiecesPanel takenPiecesPanel;
    private final BoardPanel boardPanel;
    private final DebugPanel debugPanel;
    private final MoveLog moveLog;
    private final GameSetup gameSetup;

    private Board chessBoard;

    private Tile sourceTile;
    private Tile destinationTile;
    private Piece humanMovedPiece;
    private BoardDirection boardDirection;

    private boolean highlightLegalMoves;

    private final static Dimension OUTER_FRAME_DIMENSION = new Dimension(600, 600);
    private static final Dimension BOARD_PANEL_DIMENSION = new Dimension(400, 350);
    private final static Dimension TILE_PANEL_DIMENSION = new Dimension(10, 10);
    public String promotionPiece;

    private Color lightTileColor = Color.decode("#4bd6e5");
    private Color darkTileColor = Color.decode("#1e3884");

    private static final Table INSTANCE = new Table();
    private Move computerMove;
    private AIThinkTank thinkTank;

    private Table() {
        this.gameFrame = new JFrame("JChess");
        this.gameFrame.setLayout(new BorderLayout());
        final JMenuBar tableMenuBar = createTableMenuBar();
        this.gameFrame.setJMenuBar(tableMenuBar);
        this.gameFrame.setSize(OUTER_FRAME_DIMENSION);
        this.chessBoard = Board.createStandardBoard();
        this.gameHistoryPanel = new GameHistoryPanel();
        this.takenPiecesPanel = new TakenPiecesPanel();
        this.boardPanel = new BoardPanel();
        this.debugPanel = new DebugPanel();
        this.moveLog = new MoveLog();
        setDefaultLookAndFeelDecorated(true);
        this.addObserver(new TableGameAIWatcher());
        this.gameSetup = new GameSetup(this.gameFrame, true);
        this.boardDirection = BoardDirection.NORMAL;
        this.highlightLegalMoves = true;
        this.AIisDone = false;
        this.promotionPiece = "Queen";
        this.gameFrame.add(this.takenPiecesPanel, BorderLayout.WEST);
        this.gameFrame.add(this.boardPanel, BorderLayout.CENTER);
        this.gameFrame.add(this.gameHistoryPanel, BorderLayout.EAST);
        this.gameFrame.add(debugPanel, BorderLayout.SOUTH);
        this.gameFrame.setVisible(true);
        center(this.gameFrame);
    }

    public static Table get() {
        return INSTANCE;
    }

    public void show() {
        Table.get().getMoveLog().clear();
        Table.get().getGameHistoryPanel().redo(chessBoard, Table.get().getMoveLog());
        Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
        Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
        Table.get().getDebugPanel().redo();
    }

    private DebugPanel getDebugPanel() {
        return this.debugPanel;
    }

    private GameSetup getGameSetup() {
        return this.gameSetup;
    }

    private JFrame getGameFrame() {
        return this.gameFrame;
    }

    private JMenuBar createTableMenuBar() {
        final JMenuBar tableMenuBar = new JMenuBar();
        tableMenuBar.add(createFileMenu());
        tableMenuBar.add(createPreferenceMenu());
        tableMenuBar.add(createOptionsMenu());
        return tableMenuBar;
    }

    private static void center(final JFrame frame) {
        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        final int w = frame.getSize().width;
        final int h = frame.getSize().height;
        final int x = (dim.width - w) / 2;
        final int y = (dim.height - h) / 2;
        frame.setLocation(x, y);
    }

    private JMenu createFileMenu() {
        final JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        final JMenuItem openFEN = new JMenuItem("Load FEN File", KeyEvent.VK_F);
        openFEN.addActionListener(e -> {
            String fenString = JOptionPane.showInputDialog("Input FEN");
            undoAllMoves();
            chessBoard = FenUtilities.createGameFromFEN(fenString);
            Table.get().getBoardPanel().drawBoard(chessBoard);
        });
        fileMenu.add(openFEN);

        final JMenuItem parseFEN = new JMenuItem("Parse FEN File", KeyEvent.VK_P);
        parseFEN.addActionListener(e -> {
            String FENString = FenUtilities.createFENFromGame(chessBoard);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection stringSelection = new StringSelection(FENString);
            clipboard.setContents(stringSelection, null);
            JOptionPane.showMessageDialog(gameFrame, "The FEN for this game is: " + "\n" + FENString + "\n" + "This text has been added to your clipboard. Paste and save this somewhere to save your game");
        });
        fileMenu.add(parseFEN);

        final JMenuItem exitMenuItem = new JMenuItem("Exit", KeyEvent.VK_E);
        exitMenuItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitMenuItem);

        return fileMenu;
    }

    private JMenu createPreferenceMenu() {

        final JMenu preferencesMenu = new JMenu("Preferences");
        preferencesMenu.setMnemonic(KeyEvent.VK_P);
        final JMenu colorChooserSubMenu = new JMenu("Choose Colors");
        colorChooserSubMenu.setMnemonic(KeyEvent.VK_S);

        final JMenuItem chooseDarkMenuItem = new JMenuItem("Choose Dark Tile Color",KeyEvent.VK_D);
        colorChooserSubMenu.add(chooseDarkMenuItem);

        final JMenuItem chooseLightMenuItem = new JMenuItem("Choose Light Tile Color",KeyEvent.VK_L);
        colorChooserSubMenu.add(chooseLightMenuItem);

        final JMenuItem chooseBorderColorMenuItem = new JMenuItem("Choose Border Color",KeyEvent.VK_B);
        colorChooserSubMenu.add(chooseBorderColorMenuItem);

        preferencesMenu.add(colorChooserSubMenu);
        chooseDarkMenuItem.addActionListener(e -> {
            final Color colorChoice = JColorChooser.showDialog(Table.get().getGameFrame(), "Choose Dark Tile Color",
                    Table.get().getGameFrame().getBackground());
            if (colorChoice != null) {
                Table.get().getBoardPanel().setTileDarkColor(chessBoard, colorChoice);
            }
        });

        chooseLightMenuItem.addActionListener(e -> {
            final Color colorChoice = JColorChooser.showDialog(Table.get().getGameFrame(), "Choose Light Tile Color",
                    Table.get().getGameFrame().getBackground());
            if (colorChoice != null) {
                Table.get().getBoardPanel().setTileLightColor(chessBoard, colorChoice);
            }
        });

        chooseBorderColorMenuItem.addActionListener(e -> {

            final Color colorChoice = JColorChooser.showDialog(Table.get().getGameFrame(), "Choose Border Color",
                    Table.get().getGameFrame().getBackground());
            if (colorChoice != null) {
                Table.get().getBoardPanel().setBackground(colorChoice);
            }

        });
        final JMenuItem flipBoardMenuItem = new JMenuItem("Flip Board",KeyEvent.VK_F);
        flipBoardMenuItem.addActionListener(e -> {
            boardDirection = boardDirection.opposite();
            boardPanel.drawBoard(chessBoard);
        });
        preferencesMenu.add(flipBoardMenuItem);

        preferencesMenu.addSeparator();

        final JCheckBoxMenuItem legalMoveHighlighterCheckbox = new JCheckBoxMenuItem("Highlight legal moves", true);

        legalMoveHighlighterCheckbox.addActionListener(e -> highlightLegalMoves = legalMoveHighlighterCheckbox.isSelected());
        preferencesMenu.add(legalMoveHighlighterCheckbox);

        preferencesMenu.addSeparator();

        final JMenuItem PromotionItem = new JMenuItem("Choose Promotion Piece",KeyEvent.VK_P);
        PromotionItem.addActionListener(e -> {
            Object[] possibleValues = {"Queen", "Knight", "Rook", "Bishop"};
            Object selectedValue = JOptionPane.showInputDialog(null,
                    "Choose a piece to promote your pawn to", "Input",
                    JOptionPane.INFORMATION_MESSAGE, null,
                    possibleValues, possibleValues[0]);
            if (selectedValue != null) {
                promotionPiece = selectedValue.toString();
            } else {
                promotionPiece = "Queen";
            }

        });

        preferencesMenu.add(PromotionItem);

        return preferencesMenu;
    }

    private JMenu createOptionsMenu() {

        final JMenu optionsMenu = new JMenu("Options");
        optionsMenu.setMnemonic(KeyEvent.VK_O);

        final JMenuItem resetMenuItem = new JMenuItem("New Game", KeyEvent.VK_N);
        resetMenuItem.addActionListener(e -> undoAllMoves());
        optionsMenu.add(resetMenuItem);

        final JMenuItem evaluateBoardMenuItem = new JMenuItem("Evaluate Board", KeyEvent.VK_E);
        evaluateBoardMenuItem.addActionListener(e -> System.out.println(StandardBoardEvaluator.get().evaluate(chessBoard, gameSetup.getSearchDepth())));
        optionsMenu.add(evaluateBoardMenuItem);

        /*final JMenuItem escapeAnalysis = new JMenuItem("Escape Analysis Score", KeyEvent.VK_S);
        escapeAnalysis.addActionListener(e -> {
            final Move lastMove = moveLog.getMoves().get(moveLog.size() - 1);
            if (lastMove != null) {
                System.out.println(MoveUtils.exchangeScore(lastMove));
            }

        });
        optionsMenu.add(escapeAnalysis);*/

        final JMenuItem legalMovesMenuItem = new JMenuItem("Current State", KeyEvent.VK_C);
        legalMovesMenuItem.addActionListener(e -> {
            System.out.println(chessBoard.getWhitePieces());
            System.out.println(chessBoard.getBlackPieces());
            System.out.println(playerInfo(chessBoard.currentPlayer()));
            System.out.println(playerInfo(chessBoard.currentPlayer().getOpponent()));
        });
        optionsMenu.add(legalMovesMenuItem);

        final JMenuItem undoMoveMenuItem = new JMenuItem("Undo last move", KeyEvent.VK_M);
        undoMoveMenuItem.addActionListener(e -> {
            if (Table.get().getMoveLog().size() > 0) {
                undoLastMove();
            }
        });
        optionsMenu.add(undoMoveMenuItem);

        final JMenuItem setupGameMenuItem = new JMenuItem("Setup Game",KeyEvent.VK_G);
        setupGameMenuItem.addActionListener(e -> {
            Table.get().getGameSetup().promptUser();
            Table.get().setupUpdate(Table.get().getGameSetup());

        });

        optionsMenu.add(setupGameMenuItem);

        final JMenuItem printBoardMenuItem = new JMenuItem("Print Board", KeyEvent.VK_B);
        printBoardMenuItem.addActionListener(e -> System.out.print(Table.get().chessBoard.toString()));
        optionsMenu.add(printBoardMenuItem);

        return optionsMenu;
    }

    private static String playerInfo(final Player player) {
        return ("Player is: " + player.getAlliance() + "\nlegal moves (" + player.getLegalMoves().size() + ") = " + player.getLegalMoves() + "\ninCheck = " +
                player.isInCheck() + "\nisInCheckMate = " + player.isInCheckMate() +
                "\nisCastled = " + player.isCastled()) + "\n";
    }

    private void undoAllMoves() {
        for (int i = Table.get().getMoveLog().size() - 1; i >= 0; i--) {
            final Move lastMove = Table.get().getMoveLog().removeMove(Table.get().getMoveLog().size() - 1);
            this.chessBoard = this.chessBoard.currentPlayer().unMakeMove(lastMove).getToBoard();
        }
        this.computerMove = null;
        Table.get().getMoveLog().clear();
        Table.get().getGameHistoryPanel().redo(chessBoard, Table.get().getMoveLog());
        Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
        Table.get().getBoardPanel().drawBoard(Board.createStandardBoard());
        Table.get().getDebugPanel().redo();
        if (thinkTank != null) {
            thinkTank.cancel(true);
        }
    }

    private void undoLastMove() {
        final Move lastMove = Table.get().getMoveLog().removeMove(Table.get().getMoveLog().size() - 1);
        this.chessBoard = this.chessBoard.currentPlayer().unMakeMove(lastMove).getToBoard();
        this.computerMove = null;
        Table.get().getMoveLog().removeMove(lastMove);
        Table.get().getGameHistoryPanel().redo(chessBoard, Table.get().getMoveLog());
        Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
        Table.get().getBoardPanel().drawBoard(chessBoard);
        Table.get().getDebugPanel().redo();
    }


    private void setupUpdate(final GameSetup gameSetup) {

        setChanged();
        notifyObservers(gameSetup);

    }

    public class TableGameAIWatcher implements Observer {

        @Override
        public void update(final Observable o, final Object arg) {

            if (Table.get().getGameSetup().isAIPlayer(Table.get().getGameBoard().currentPlayer()) &&
                    !Table.get().getGameBoard().currentPlayer().isInCheckMate() &&
                    !Table.get().getGameBoard().currentPlayer().isInStaleMate()) {
                //create an AI thread
                //execute ai work
                System.out.println(Table.get().getGameBoard().currentPlayer() + " is set to AI, thinking....");
                thinkTank = new AIThinkTank();
                thinkTank.execute();
            }

            if (Table.get().getGameBoard().currentPlayer().isInCheckMate()) {
                JOptionPane.showMessageDialog(Table.get().getBoardPanel(),
                        "Game Over: Player " + Table.get().getGameBoard().currentPlayer() + " is in checkmate!", "Game Over",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            if (Table.get().getGameBoard().currentPlayer().isInStaleMate()) {
                JOptionPane.showMessageDialog(Table.get().getBoardPanel(),
                        "Game Over: Player " + Table.get().getGameBoard().currentPlayer() + " is in stalemate!", "Game Over",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        }
    }

    public class AIThinkTank extends SwingWorker<Move, String> {

        AIThinkTank() {

        }

        @Override
        protected Move doInBackground() {

            AIisDone = isDone();
            int numPieces = Table.get().getGameBoard().getWhitePieces().size() +
                    Table.get().getGameBoard().getBlackPieces().size();
            //dit zorgt ervoor dat bij weinig stukken op het bord het ab algoritme dieper graaft
            int bonusDepth = Math.min(4, 1 + Math.round((float) 32 / numPieces));

            final StockAlphaBeta strategy =
                    new StockAlphaBeta(Table.get().getGameSetup().getSearchDepth() + bonusDepth);
            strategy.addObserver(Table.get().getDebugPanel());


            return strategy.execute(
                    Table.get().getGameBoard());
        }


        @Override
        public void done() {

            try {
                final Move bestMove = get();

                Table.get().updateComputerMove(bestMove);
                Table.get().updateGameBoard(Table.get().getGameBoard().currentPlayer().makeMove(bestMove).getToBoard());
                Table.get().getMoveLog().addMoves(bestMove);
                Table.get().getGameHistoryPanel().redo(Table.get().getGameBoard(), Table.get().getMoveLog());
                Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
                Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
                Table.get().getDebugPanel().redo();
                Table.get().moveMadeUpdate(PlayerType.COMPUTER);
                AIisDone = isDone();
            } catch (final Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void updateGameBoard(final Board board) {
        this.chessBoard = board;
    }

    private void updateComputerMove(final Move move) {
        this.computerMove = move;
    }


    private MoveLog getMoveLog() {
        return this.moveLog;
    }

    private GameHistoryPanel getGameHistoryPanel() {
        return this.gameHistoryPanel;
    }

    private Board getGameBoard() {
        return this.chessBoard;
    }

    private TakenPiecesPanel getTakenPiecesPanel() {
        return this.takenPiecesPanel;
    }

    private BoardPanel getBoardPanel() {
        return this.boardPanel;
    }

    private void moveMadeUpdate(final PlayerType playerType) {
        setChanged();
        notifyObservers(playerType);

    }

    public enum BoardDirection {

        NORMAL {
            @Override
            List<TilePanel> traverse(List<TilePanel> boardTiles) {
                return boardTiles;
            }

            @Override
            BoardDirection opposite() {
                return FLIPPED;
            }
        },
        FLIPPED {
            @Override
            List<TilePanel> traverse(List<TilePanel> boardTiles) {
                Collections.reverse(boardTiles);
                return boardTiles;
            }

            @Override
            BoardDirection opposite() {
                return NORMAL;
            }
        };

        abstract List<TilePanel> traverse(final List<TilePanel> boardTiles);

        abstract BoardDirection opposite();
    }

    private class BoardPanel extends JPanel {
        final List<TilePanel> boardTiles;

        BoardPanel() {
            super(new GridLayout(8, 8));
            this.boardTiles = new ArrayList<>();
            for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
                final TilePanel tilePanel = new TilePanel(this, i);
                this.boardTiles.add(tilePanel);
                add(tilePanel);
            }
            setPreferredSize(BOARD_PANEL_DIMENSION);
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setBackground(Color.decode("#00165b"));
            validate();
        }

        void drawBoard(final Board board) {
            removeAll();
            for (final TilePanel tilePanel : boardDirection.traverse(boardTiles)) {
                tilePanel.drawTile(board);
                add(tilePanel);
            }
            validate();
            repaint();
        }

        void setTileDarkColor(final Board board,
                              final Color darkColor) {
            for (final TilePanel boardTile : boardTiles) {
                boardTile.setDarkTileColor(darkColor);
            }
            drawBoard(board);
        }

        void setTileLightColor(final Board board,
                               final Color lightColor) {
            for (final TilePanel boardTile : boardTiles) {
                boardTile.setLightTileColor(lightColor);
            }
            drawBoard(board);
        }
    }

    static class MoveLog {

        private final List<Move> moves;

        MoveLog() {
            this.moves = new ArrayList<>();
        }

        List<Move> getMoves() {
            return this.moves;
        }

        void addMoves(final Move move) {
            this.moves.add(move);
        }

        int size() {
            return this.moves.size();
        }

        void clear() {
            this.moves.clear();
        }

        Move removeMove(int index) {
            return this.moves.remove(index);
        }

        boolean removeMove(final Move move) {
            return this.moves.remove(move);
        }
    }

    public enum PlayerType {
        HUMAN,
        COMPUTER
    }

    private class TilePanel extends JPanel {

        private final int tileID;

        TilePanel(final BoardPanel boardPanel,
                  final int tileID) {
            super(new GridBagLayout());
            this.tileID = tileID;
            setPreferredSize(TILE_PANEL_DIMENSION);
            assignTileColor();
            highlightTileBorder(chessBoard);
            assignTilePieceIcon(chessBoard);

            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(final MouseEvent e) {

                    if (isRightMouseButton(e)) {
                        sourceTile = null;
                        destinationTile = null;
                        humanMovedPiece = null;
                    } else if (isLeftMouseButton(e)) {
                        if (sourceTile == null) {
                            sourceTile = chessBoard.getTile(tileID);
                            humanMovedPiece = sourceTile.getPiece();
                            if (humanMovedPiece == null) {
                                sourceTile = null;
                            }
                        } else {
                            destinationTile = chessBoard.getTile(tileID);
                            final Move move = Move.MoveFactory.createMove(chessBoard, sourceTile.getTileCoordinate(), destinationTile.getTileCoordinate());
                            final MoveTransition transition = chessBoard.currentPlayer().makeMove(move);
                            if (transition.getMoveStatus().isDone()) {
                                chessBoard = transition.getToBoard();
                                moveLog.addMoves(move);
                            }
                            sourceTile = null;
                            destinationTile = null;
                            humanMovedPiece = null;
                        }

                        invokeLater(() -> {
                            gameHistoryPanel.redo(chessBoard, moveLog);
                            takenPiecesPanel.redo(moveLog);
                            boardPanel.drawBoard(chessBoard);
                            if (gameSetup.isAIPlayer(chessBoard.currentPlayer())) {
                                Table.get().moveMadeUpdate(PlayerType.HUMAN);
                            }
                            boardPanel.drawBoard(chessBoard);
                            debugPanel.redo();
                        });

                    }

                }
                //deze functies zijn ongebruikt omdat ik geen gebruik ga maken van drag-en-dropverplaatsing bij de stukken in de GUI
                @Override
                public void mousePressed(final MouseEvent e) {

                }

                @Override
                public void mouseReleased(final MouseEvent e) {

                }

                @Override
                public void mouseEntered(final MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
            });


            validate();
        }

        void drawTile(final Board board) {
            assignTileColor();
            assignTilePieceIcon(board);
            highlightAIMove();
            highLightLegals(board);
            highlightTileBorder(chessBoard);
            validate();
            repaint();
        }

        void setLightTileColor(final Color color) {
            lightTileColor = color;
        }

        void setDarkTileColor(final Color color) {
            darkTileColor = color;
        }


        private void highlightTileBorder(final Board board) {
            if (humanMovedPiece != null &&
                    humanMovedPiece.getPieceAlliance() == board.currentPlayer().getAlliance() &&
                    humanMovedPiece.getPiecePosition() == this.tileID) {
                setBorder(BorderFactory.createLineBorder(Color.ORANGE, 3));
            } else {
                setBorder(null);
            }
        }

        private void highlightAIMove() {
            if (computerMove != null) {
                if (this.tileID == computerMove.getCurrentCoordinate()) {
                    setBackground(Color.pink);
                } else if (this.tileID == computerMove.getDestinationCoordinate()) {
                    setBackground(Color.red);
                }
            }
        }

        private void assignTilePieceIcon(final Board board) {
            this.removeAll();
            if (board.getTile(this.tileID).isTileOccupied()) {

                try {

                    //white bishop: WB.gif
                    String defaultPieceImagePath = "art/pieces/";
                    final BufferedImage image = ImageIO.read(new File(defaultPieceImagePath + board.getTile(this.tileID).getPiece().getPieceAlliance().toString().substring(0, 1) +
                            board.getTile(this.tileID).getPiece().toString() + ".gif"));
                    add(new JLabel(new ImageIcon(image)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void highLightLegals(final Board board) {
            if (highlightLegalMoves) {
                for (final Move move : pieceLegalMoves(board)) {
                    if (move.getDestinationCoordinate() == this.tileID) {
                        try {
                            add(new JLabel(new ImageIcon(ImageIO.read(new File("art/green_dot.png")))));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        private Collection<Move> pieceLegalMoves(final Board board) {
            if (humanMovedPiece != null && humanMovedPiece.getPieceAlliance() == board.currentPlayer().getAlliance()) {
                Collection<Move> castleMoves = null;
                if (humanMovedPiece.getPieceType().isKing()) {
                    if (humanMovedPiece.getPieceAlliance().isBlack()) {
                        castleMoves = board.blackPlayer().calculateKingCastles(board.blackPlayer().getLegalMoves(), board.whitePlayer().getLegalMoves());
                    }
                    if (humanMovedPiece.getPieceAlliance().isWhite()) {
                        castleMoves = board.whitePlayer().calculateKingCastles(board.whitePlayer().getLegalMoves(), board.blackPlayer().getLegalMoves());
                    }
                }
                if (castleMoves == null) {
                    castleMoves = Collections.emptyList();
                }
                Collection<Move> legalMoves = humanMovedPiece.calculateLegalMoves(board);
                legalMoves = Lists.newArrayList(Iterables.concat(legalMoves, castleMoves));
                return legalMoves;
            }
            return Collections.emptyList();
        }


        private void assignTileColor() {
            if (BoardUtils.INSTANCE.EIGHTH_RANK.get(this.tileID) ||
                    BoardUtils.INSTANCE.SIXTH_RANK.get(this.tileID) ||
                    BoardUtils.INSTANCE.FOURTH_RANK.get(this.tileID) ||
                    BoardUtils.INSTANCE.SECOND_RANK.get(this.tileID)) {
                setBackground(this.tileID % 2 == 0 ? lightTileColor : darkTileColor);
            } else if (BoardUtils.INSTANCE.SEVENTH_RANK.get(this.tileID) ||
                    BoardUtils.INSTANCE.FIFTH_RANK.get(this.tileID) ||
                    BoardUtils.INSTANCE.THIRD_RANK.get(this.tileID) ||
                    BoardUtils.INSTANCE.FIRST_RANK.get(this.tileID)) {
                setBackground(this.tileID % 2 != 0 ? lightTileColor : darkTileColor);
            }

        }


    }

}
