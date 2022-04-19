package uk.ac.bris.cs.scotlandyard.model;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {
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
		@Nonnull @Override public GameSetup getSetup() { return setup; }

		/**
		 * @return all players in the game
		 */
		@Nonnull @Override
		public ImmutableSet<Piece> getPlayers() {
            Piece[] getPiece = new Piece[detectives.size() + 1];
            getPiece[0] = mrX.piece();

            for (int i = 0; i < detectives.size(); i++) {
                getPiece[i + 1] = detectives.get(i).piece();
            }
            ImmutableSet<Piece> set = null;
            if (detectives.size() == 1) {
                set = ImmutableSet.of(getPiece[0], getPiece[1]);
            }
            if (detectives.size() == 2) {
                set = ImmutableSet.of(getPiece[0], getPiece[1], getPiece[2]);
            }
            if (detectives.size() == 3) {
                set = ImmutableSet.of(getPiece[0], getPiece[1], getPiece[2], getPiece[3]);
            }
            if (detectives.size() == 4) {
                set = ImmutableSet.of(getPiece[0], getPiece[1], getPiece[2], getPiece[3], getPiece[4]);
            }
            if (detectives.size() == 5) {
                set = ImmutableSet.of(getPiece[0], getPiece[1], getPiece[2], getPiece[3], getPiece[4], getPiece[5]);
            }
            return set;
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
		public GameState advance(Move move) throws IllegalArgumentException{
			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);
			return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);
		}

		/**
		 * @param detective the detective
		 * @return the location of the given detective; empty if the detective is not part of the game
		 */
		@Nonnull @Override
		public Optional<Integer> getDetectiveLocation(Detective detective) {
			// For all detectives, if Detective#piece == detective,
			// then return the location in an Optional.of();
			// otherwise, return Optional.empty();
			String detectiveColour;
			detectiveColour = detective.webColour();
			Optional<Player> p = detectives.stream()
					.filter(d -> d.piece().webColour().equals(detectiveColour))
					.findFirst();
				if (p.isEmpty()) return Optional.empty();
				else return Optional.of(p.get().location());
		}

		/**
		 * @param piece the player piece
		 * @return the ticket board of the given player; empty if the player is not part of the game
		 */
		@Nonnull @Override
		public Optional<Board.TicketBoard> getPlayerTickets(Piece piece) {
			ImmutableMap<Ticket, Integer> xt;
			xt = this.mrX.tickets();
			if (piece == MrX.MRX) return Optional.of(xt)
					.map(tickets -> ticket -> xt.getOrDefault(ticket, 0));

			String detectiveColour;
			detectiveColour = piece.webColour();
			Optional<Player> p = detectives.stream()
					.filter(d -> d.piece().webColour().equals(detectiveColour))
					.findFirst();
			ImmutableMap<Ticket, Integer> dt;
			if (p.isEmpty()) return Optional.empty();
			else {
				dt = p.get().tickets();
				return Optional.of(dt)
						.map(tickets -> ticket -> dt.getOrDefault(ticket, 0));
			}
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
			//TODO: testWinningPlayerIsEmptyInitially
			return null;
		}


		private static Set<SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source){
			//  create an empty collection of some sort, say, HashSet, to store all the SingleMove we generate
            HashSet<SingleMove> singleMoveHashSet = null;

            for(int destination : setup.graph.adjacentNodes(source)) {
                //  find out if destination is occupied by a detective
                //  if the location is occupied, don't add to the collection of moves to return
				boolean state = true;
				for (int n = 0; n<detectives.size(); n++) {
					if (destination == detectives.get(n).location()) state = false;
				}
				if (state) {
					{
						for (Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of())) {
							//  find out if the player has the required tickets
							//  if it does, construct a SingleMove and add it the collection of moves to return
							if (player.has(t.requiredTicket())) {
								singleMoveHashSet.add(new SingleMove(player.piece(), source, t.requiredTicket(), destination));
							}
						}
						if (player.has(Ticket.SECRET)) {
							// consider the rules of secret moves here
							// add moves to the destination via a secret ticket if there are any left with the player
							singleMoveHashSet.add( new SingleMove(player.piece(), source, Ticket.SECRET, destination));
						}
					}
				}
			}
			return singleMoveHashSet;
		}

        private static Set<DoubleMove> makeDoubleMoves(GameSetup setup, int source){
            HashSet<DoubleMove> doubleMoveHashSet = null;
            return doubleMoveHashSet;
        }
		/**
		 * @return the current available moves of the game.
		 * This is mutually exclusive with {@link #getWinner()}
		 */
		@Nonnull @Override
		public ImmutableSet<Move> getAvailableMoves() {
			HashSet<Move> set = null;
			set.add((Move) makeSingleMoves(setup, detectives, mrX, mrX.location()));
			for (Player d : detectives) {
				set.add((Move) makeSingleMoves(setup, detectives, d, d.location() ));
			}
            return (ImmutableSet<Move>) Collections.unmodifiableSet(set);
        }
	}

	/**
	 * @return a new instance of GameState.
	 */
	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) throws NullPointerException, IllegalArgumentException {

		//check setup
		boolean state = false;
        if( state ) throw new IllegalArgumentException();
		//TODO: testEmptyGraphShouldThrow

		//check mrX
		if (mrX == null) throw new NullPointerException();

		//check detectives
		if (detectives == null) throw new NullPointerException();
		Player detectiveCheck;
		String[] detectiveColour = new String[6];

		int[] detectiveLocation = new int[6];
		for (int i = 0; i<detectives.size(); i++) {
			detectiveCheck = detectives.get(i);
			if (detectiveCheck.has(Ticket.SECRET)) throw new IllegalArgumentException();
			if (detectiveCheck.has(Ticket.DOUBLE)) throw new IllegalArgumentException();

			detectiveColour[i] = detectiveCheck.piece().webColour();
			for (int k = 0; k<i; k++) {
				if (detectiveColour[k].equals(detectiveColour[i])) throw new IllegalArgumentException();
			}
			detectiveLocation[i] = detectiveCheck.location();
			for (int k = 0; k<i; k++) {
				if (detectiveLocation[k] == detectiveLocation[i]) throw new IllegalArgumentException();
			}

		}

		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);

	}



}
