package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

import java.util.*;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.xml.stream.Location;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {
	/**
	 * @return a new instance of GameState.
	 */
	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives)

			throws NullPointerException, IllegalArgumentException {

		if (mrX == null) throw (new NullPointerException());
			else if (detectives == null) throw (new NullPointerException());
		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);
	}

	private final class MyGameState implements GameState {
		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;

		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives){

			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
			if(setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");
		}

		/**
		 * @return the current game setup
		 */
		@Nonnull @Override public GameSetup getSetup() {return setup;}

		/**
		 * @return all players in the game
		 */
		@Nonnull @Override
		public ImmutableSet<Piece> getPlayers() {
			return null;
		}

		/**
		 * Computes the next game state given a move from {@link #getAvailableMoves()} has been
		 * chosen and supplied as the parameter
		 *
		 * @param move the move to make
		 * @return the game state of which the given move has been made
		 * @throws IllegalArgumentException if the move was not a move from
		 *                                  {@link #getAvailableMoves()}
		 */
		@Nonnull @Override
		public GameState advance(Move move) {
			return null;
		}

		/**
		 * @param detective the detective
		 * @return the location of the given detective; empty if the detective is not part of the game
		 */
		@Nonnull @Override
		public Optional<Integer> getDetectiveLocation(Detective detective) {
			// For all detectives, if Detective#piece == detective, then return the location in an Optional.of();
			// otherwise, return Optional.empty();
			if ( detective.webColour().equals("#f00")) {
				Optional<Player> p = detectives.stream()
						.filter(d -> d.piece().equals(Detective.RED))
						.findFirst();
				return Optional.of(p.get().location());
			}
			else if ( detective.webColour().equals("#0f0")) {
				Optional<Player> p = detectives.stream()
						.filter(d -> d.piece().equals(Detective.GREEN))
						.findFirst();
				return Optional.of(p.get().location());
			}
			else if ( detective.webColour().equals("#00f")) {
				Optional<Player> p = detectives.stream()
						.filter(d -> d.piece().equals(Detective.BLUE))
						.findFirst();
				return Optional.of(p.get().location());
			}
			else if ( detective.webColour().equals("#fff")) {
				Optional<Player> p = detectives.stream()
						.filter(d -> d.piece().equals(Detective.WHITE))
						.findFirst();
				return Optional.of(p.get().location());
			}
			else if ( detective.webColour().equals("#ff0")) {
				Optional<Player> p = detectives.stream()
						.filter(d -> d.piece().equals(Detective.YELLOW))
						.findFirst();
				return Optional.of(p.get().location());
			}
				else return Optional.empty();
		}

		/**
		 * @param piece the player piece
		 * @return the ticket board of the given player; empty if the player is not part of the game
		 */
		@Nonnull @Override
		public Optional<Board.TicketBoard> getPlayerTickets(Piece piece) {
			return Optional.empty();
		}

		/**
		 * @return MrX's travel log as a list of {@link LogEntry}s.
		 */
		@Nonnull @Override
		public ImmutableList<LogEntry> getMrXTravelLog() {return log;}

		/**
		 * @return the winner of this game; empty if the game has no winners yet
		 * This is mutually exclusive with {@link #getAvailableMoves()}
		 */
		@Nonnull @Override
		public ImmutableSet<Piece> getWinner() {
			return null;
		}

		/**
		 * @return the current available moves of the game.
		 * This is mutually exclusive with {@link #getWinner()}
		 */
		@Nonnull @Override
		public ImmutableSet<Move> getAvailableMoves() {
			return null;
		}
	}




}
