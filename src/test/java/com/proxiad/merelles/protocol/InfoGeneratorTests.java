package com.proxiad.merelles.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.codingame.game.Player;
import com.proxiad.merelles.game.Board;
import com.proxiad.merelles.game.Location;
import com.proxiad.merelles.game.NoPossibleMovesException;
import com.proxiad.merelles.game.PlayerColor;
import com.proxiad.merelles.game.PlayerData;

public class InfoGeneratorTests {

	static class ControlledColorPlayer extends Player {
		private PlayerData data;

		ControlledColorPlayer(PlayerColor color) {
			data = new PlayerData(null, color, 9);
			
			PlayerColor oppositeColor = color == PlayerColor.BLACK ? PlayerColor.WHITE : PlayerColor.BLACK;
			PlayerData opponent = new PlayerData(null, oppositeColor, 9);
			data.setOpponent(opponent);
		}

		@Override
		public PlayerData getData() {
			return data;
		}
	}
	
	private Board board;
	private Player whitePlayer;
	private InfoGenerator generator;
	
	@Before
	public void setUp() throws Exception {
		board = new Board();
		whitePlayer = new ControlledColorPlayer(PlayerColor.WHITE);
		generator = new InfoGenerator();
	}

	@Test
	public void testEmptyBoardInfo() throws NoPossibleMovesException {
		List<String> blackInfos = generator.gameInfoForPlayer(board, whitePlayer, 200).collect(Collectors.toList());

		// Info line,
		// nb pieces = 0 (empty board)
		// (0 lines for pieces)
		// nb moves = 24 (24 free slots on the board)
		// one line per slot
		assertEquals(3 + 24, blackInfos.size());

		assertEquals("WHITE 200 0 0 9 9", blackInfos.get(0));
		
		// 0 pieces on the board
		assertEquals("0", blackInfos.get(1));

		assertEquals("24", blackInfos.get(2));
		
		for (int direction = 0; direction < 8; ++direction) {
			for (int radius = 0; radius < 3; ++radius) {				
				assertTrue(blackInfos.stream().skip(3).anyMatch(moveTester(direction, radius)));
			}
		}
	}

	protected Predicate<String> moveTester(int direction, int radius) {
		String coordinates = String.format(" %d %d 0 0", direction, radius);
		return input -> input.endsWith(coordinates);
	}
	
	@Test
	public void testFirstWhiteInfo() throws NoPossibleMovesException {
		// black player plays
		board.putPiece(new Location(2, 1), PlayerColor.BLACK);
		whitePlayer.getData().getOpponent().updateCountsAfterPut();
		
		// what does white player receive?
		List<String> whiteInfos = generator.gameInfoForPlayer(board, whitePlayer, 108).collect(Collectors.toList());

		// Info line,
		// nb pieces = 1
		// x BLACK 2 1 = the first black piece
		// nb moves = 23 (23 free slots on the board)
		// one line per slot
		assertEquals(4 + 23, whiteInfos.size());

		assertEquals("WHITE 108 0 1 9 8", whiteInfos.get(0));
		
		// 1 piece on the board
		assertEquals("1", whiteInfos.get(1));
		assertTrue(whiteInfos.get(2).endsWith(" 1 2 1"));
		
		assertEquals("23", whiteInfos.get(3));
		for (int direction = 0; direction < 8; ++direction) {
			for (int radius = 0; radius < 3; ++radius) {
				if (direction != 2 || radius != 1) {
					assertTrue(whiteInfos.stream().skip(4).anyMatch(moveTester(direction, radius)));
				}
			}
		}
	}
	
	@Test (expected = NoPossibleMovesException.class)
	public void testStuckThrowsException() throws NoPossibleMovesException {
		board.putPiece(new Location(2, 0), PlayerColor.WHITE);
		board.putPiece(new Location(2, 1), PlayerColor.WHITE);
		board.putPiece(new Location(2, 2), PlayerColor.WHITE);
		board.putPiece(new Location(3, 1), PlayerColor.WHITE);

		board.putPiece(new Location(1, 0), PlayerColor.BLACK);
		board.putPiece(new Location(1, 1), PlayerColor.BLACK);
		board.putPiece(new Location(1, 2), PlayerColor.BLACK);

		board.putPiece(new Location(3, 0), PlayerColor.BLACK);
		board.putPiece(new Location(4, 1), PlayerColor.BLACK);
		board.putPiece(new Location(3, 2), PlayerColor.BLACK);

		// simulates end of placement phase
		for (int i = 0; i < 9; ++i) {
			whitePlayer.getData().updateCountsAfterPut();
		}
		
		generator.gameInfoForPlayer(board, whitePlayer, 200).collect(Collectors.toList());
	}
}
