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
			ImmutableSet<Piece> result = ImmutableSet.copyOf(getPiece);
			return result;
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
			return null;
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
            HashSet<SingleMove> singleMoveHashSet = new HashSet<>();

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

        private static Set<DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player mrX, int source){
            HashSet<DoubleMove> doubleMoveHashSet = new HashSet<>();
			Set<SingleMove> firstMoves = makeSingleMoves(setup, detectives, mrX, source);
			Iterator<SingleMove> firstMoveE = firstMoves.iterator();
			for (int i = 0; i<firstMoves.size(); i++){
				SingleMove firstMove = firstMoveE.next();
				Ticket ticket1 = firstMove.ticket;
				Ticket ticket2;
				int destination1 = firstMove.destination;

				//  find out if destination is occupied by a detective
				//  if the location is occupied, don't add to the collection of moves to return
				for(int destination2 : setup.graph.adjacentNodes(destination1)){
					boolean state = true;
					for (int n = 0; n<detectives.size(); n++) {
						if (destination2 == detectives.get(n).location()) state = false;

					}
					if (state) {
							for (Transport t : setup.graph.edgeValueOrDefault(destination1, destination2, ImmutableSet.of())) {
								//  find out if the player has the required tickets
								//  if it does, construct a SingleMove and add it the collection of moves to return
								if (mrX.has(t.requiredTicket())) {
									doubleMoveHashSet.add(new DoubleMove(mrX.piece(), source, ticket1, destination1, t.requiredTicket(), destination2));
									ticket2 = t.requiredTicket();
									if (mrX.has(Ticket.SECRET)) {
										// consider the rules of secret moves here
										// add moves to the destination via a secret ticket if there are any left with the player
										doubleMoveHashSet.add(new DoubleMove(mrX.piece(), source, Ticket.SECRET, destination1, ticket2, destination2));
										doubleMoveHashSet.add(new DoubleMove(mrX.piece(), source, ticket1, destination1, Ticket.SECRET, destination2));

										if(mrX.hasAtLeast(Ticket.SECRET, 2)) {
											doubleMoveHashSet.add(new DoubleMove(mrX.piece(), source, Ticket.SECRET, destination1, Ticket.SECRET, destination2));
										}
									}
								}
							}
					}
				}


			}

            return doubleMoveHashSet;
        }
		/**
		 * @return the current available moves of the game.
		 * This is mutually exclusive with {@link #getWinner()}
		 */
		@Nonnull @Override
		public ImmutableSet<Move> getAvailableMoves() {

			//mrx
			//TODO: double move
			Set<SingleMove> mrxSingle = new HashSet<>();
			mrxSingle = makeSingleMoves(setup, detectives, mrX, mrX.location());
			Set<DoubleMove> mrxDouble = new HashSet<>();
			mrxDouble = makeDoubleMoves(setup, detectives, mrX, mrX.location());

			Move[] mrxMove = new Move[mrxSingle.size() + mrxDouble.size()];

			Iterator<SingleMove> mrxSingleE = mrxSingle.iterator();
			for (int i = 0; i<mrxSingle.size(); i++){
				mrxMove[i] = mrxSingleE.next();
			}
			Iterator<DoubleMove> mrxDoubleE = mrxDouble.iterator();
			for (int i = 0; i<mrxDouble.size(); i++){
				mrxMove[mrxSingle.size()+i] = mrxDoubleE.next();
			}


			//detective
			Set<Set<SingleMove>> detSingle2 = new HashSet<>();
			int detectiveMoveSize = 0;
			for (Player d : detectives) {
				detSingle2.add(makeSingleMoves(setup, detectives, d, d.location() ));
				detectiveMoveSize += makeSingleMoves(setup, detectives, d, d.location()).size();
			}
			Iterator<Set<SingleMove>> detSingleE2 = detSingle2.iterator();
			Set<SingleMove>[] detMove2 = new Set[detSingle2.size()];
			for (int i = 0; i<detSingle2.size(); i++){
				detMove2[i] = detSingleE2.next();
			}

			Move[] detMove = new Move[detectiveMoveSize];

			Set<SingleMove> detSingle = new HashSet<>();
			int detMoveStorage = 0;
			int for_detMoveStorage = 0;
			for (int i = 0; i<detMove2.length; i++){
				detSingle = detMove2[i];
				detMoveStorage += for_detMoveStorage;
				for (int k = detMoveStorage; k<detMove2[i].size() + detMoveStorage; k++){
					Iterator<SingleMove> detSingleE = detSingle.iterator();
					detMove[k] = detSingleE.next();
					for_detMoveStorage ++;
				}
			}

			//TODO: why doesn't contain detectives' move
			return ImmutableSet.copyOf(mrxMove);

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
        if( setup.graph.nodes().isEmpty() ) throw new IllegalArgumentException();

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
