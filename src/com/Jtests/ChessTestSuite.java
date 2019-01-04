package com.Jtests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({TestPieces.class,
                     TestBoard.class,
                     TestStaleMate.class,
                     TestPlayer.class,
                     TestCheckmate.class,
                     //TestMiniMax.class (not usable at the moment),
                     //TestAlphaBeta.class(works, takes too long),
                     TestCastling.class,
                     TestPawnStructure.class,
                     TestFENParser.class,
                     //TestEngine.class (not usable at the moment),
                     TestKingSafety.class,
                     TestRookStructure.class})
public class ChessTestSuite {
}
