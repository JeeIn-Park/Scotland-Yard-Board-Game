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

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;



public final class MyGameStateFactory implements Factory<GameState> {
	private final class MyGameState implements GameState {
		private final GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private final ImmutableList<LogEntry> log;
		private final Player mrX;
		private final List<Player> detectives;
		private ImmutableSet<Move> moves;
		private final ImmutableSet<Piece> winner;

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
			this.winner = checkWinner();
			if(setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");
		}


		/**
		 * Computes whether game is over or not; If game is over, set remaining to null
		 *
		 * @return the winner of this game; empty if the game is not over
		 */
		private ImmutableSet<Piece> checkWinner() {

			if (remaining.contains(mrX.piece())) {
				// MrX is cornered by detectives
				if (getAvailableMoves().isEmpty()) {
					remaining = null;
					return ImmutableSet.copyOf(getDetectivePieceArrayList());
				}
				// MrX filled the log and subsequently all detectives failed to catch him
				if (log.size() == setup.moves.size()){
					remaining = null;
					return ImmutableSet.of(mrX.piece());
				}
			}

			boolean state = true;
			for (Player p : detectives) {
				// detective catches mrx
				if (p.location() == mrX.location()){
					remaining = null;
					return ImmutableSet.copyOf(getDetectivePieceArrayList());
				}
				// all detectives used given ticket, so they cannot move anymore
				if (! p.tickets().equals(ImmutableMap.of(
						TAXI, 0,
						BUS, 0,
						UNDERGROUND, 0,
						Ticket.DOUBLE, 0,
						Ticket.SECRET, 0))){
					state = false;
				}
			}
			if(state) {
				remaining = null;
				return ImmutableSet.of(mrX.piece());
			}

			return ImmutableSet.of();
		}



		/**
		 * @return the current game setup
		 */
		@Nonnull @Override public GameSetup getSetup() { return setup; }



		/**
		 * Get a list of detectives as a list of {@link Player}s and computes piece of them
		 *
		 * @return the detectives as a list of {@link Piece}s
		 */
		public List<Piece> getDetectivePieceArrayList() {
			List<Piece> getPiece = new ArrayList<>();

			for (Player d : detectives) {
				getPiece.add(d.piece());
			}

			return getPiece;
		}



		/**
		 * @return all players in the game as a set of {@link Piece}s
		 */
		@Nonnull @Override
		public ImmutableSet<Piece> getPlayers() {

			Piece[] getPiece = new Piece[detectives.size() + 1];
			getPiece[0] = mrX.piece();

			for (int i = 0; i < detectives.size(); i++) {
				getPiece[i + 1] = detectives.get(i).piece();
			}
			return ImmutableSet.copyOf(getPiece);
        }



		/**
		 * Computes the next game state given a move from {@link #getAvailableMoves()} has been
		 * chosen and supplied as the parameter
		 *
		 * @param move the move to make
		 * @return the game state of which the given move has been made
		 * @throws IllegalArgumentException if the move was not a move from {@link #getAvailableMoves()}
		 */
		@Nonnull @Override
		public GameState advance(Move move) throws IllegalArgumentException{
			this.moves = getAvailableMoves();
			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);

			Player newMrx = this.mrX;
			List<Player> newDetectives = new LinkedList<>(detectives);
			List<LogEntry> advanceLog = new ArrayList<>(log);

			// when mrx moves
			if(move.commencedBy() == mrX.piece()){

				// set remaining with detectives who have tickets
				List<Piece> r = getDetectivePieceArrayList();
				for (Player p : detectives) {
					if (p.tickets().equals(ImmutableMap.of( TAXI, 0, BUS, 0, UNDERGROUND, 0, Ticket.DOUBLE, 0, Ticket.SECRET, 0))){
						r.remove(p.piece());
					}
				}
				remaining = ImmutableSet.copyOf(r);

				// add log according to given move
				// use tickets and decrement used ticket count
				Map<Ticket, Integer> mrxTickets = new HashMap<>(mrX.tickets());
				newMrx = move.accept(new Visitor<>() {
					@Override
					public Player visit(SingleMove move) {
						if (setup.moves.get(advanceLog.size()))
							advanceLog.add(LogEntry.reveal(move.ticket, move.destination));
						else advanceLog.add(LogEntry.hidden(move.ticket));

						mrxTickets.put(move.ticket, mrxTickets.get(move.ticket) -1);
						return new Player(mrX.piece(), ImmutableMap.copyOf(mrxTickets), move.destination);
					}

					@Override
					public Player visit(DoubleMove move) {
						// first move in double move
						if (setup.moves.get(advanceLog.size()))
							advanceLog.add(LogEntry.reveal(move.ticket1, move.destination1));
						else advanceLog.add(LogEntry.hidden(move.ticket1));
						// second move in double move
						if (setup.moves.get(advanceLog.size()))
							advanceLog.add(LogEntry.reveal(move.ticket2, move.destination2));
						else advanceLog.add(LogEntry.hidden(move.ticket2));

						// decrement used first move, double move and double tickets
						mrxTickets.put(move.ticket1, mrxTickets.get(move.ticket1) -1);
						mrxTickets.put(move.ticket2, mrxTickets.get(move.ticket2) -1);
						mrxTickets.put(Ticket.DOUBLE, mrxTickets.get(Ticket.DOUBLE) -1);

						return new Player(mrX.piece(), ImmutableMap.copyOf(mrxTickets), move.destination2);
					}
				});
			}


			// when detective moves
			if(move.commencedBy() != mrX.piece()) {

				// set remaining with detectives who haven't moved current round
				// if there is no remaining detectives for current round, set remaining with MrX
				List<Piece> remainingL = new ArrayList<>(remaining);
				remainingL.remove(move.commencedBy());
				if (remainingL.isEmpty()) {
					remaining = ImmutableSet.of(MrX.MRX);
				} else remaining = ImmutableSet.copyOf(remainingL);


				// use ticket
				// give used ticket to MrX
				Map<Ticket, Integer> addMrxTickets = new HashMap<>(mrX.tickets());
				addMrxTickets.put(((SingleMove) move).ticket, addMrxTickets.get(((SingleMove) move).ticket) + 1);
				newMrx = new Player(mrX.piece(), ImmutableMap.copyOf(addMrxTickets), mrX.location());

				// decrement used ticket count
				Map<Ticket, Integer> detTickets = new HashMap<>();
				for (Player p : detectives) {
					if (p.piece() == move.commencedBy()) {
						detTickets.putAll(p.tickets());
						newDetectives.remove(p);
						detTickets.put(((SingleMove) move).ticket, detTickets.get(((SingleMove) move).ticket) - 1);
					}
				}

				Player newDet = new Player(move.commencedBy(), ImmutableMap.copyOf(detTickets), ((SingleMove) move).destination);
				newDetectives.add(newDet);
			}
				return new MyGameState(setup, remaining, ImmutableList.copyOf(advanceLog), newMrx, newDetectives);
		}



		/**
		 * @param detective the detective
		 * @return the location of the given detective; empty if the detective is not part of the game
		 */
		@Nonnull @Override
		public Optional<Integer> getDetectiveLocation(Detective detective) {
			// if Detective#piece == detective for all detectives, return the location in an Optional.of();
			// otherwise, return Optional.empty();
			Optional<Player> p = detectives.stream()
					.filter(d -> d.piece().webColour().equals(detective.webColour()))
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

			// Tickets of MrX
			if (piece == MrX.MRX) return Optional.of(mrX.tickets())
					.map(tickets -> ticket -> mrX.tickets().getOrDefault(ticket, 0));

			// Tickets of detective
			Optional<Player> p = detectives.stream()
					.filter(d -> d.piece().webColour().equals(piece.webColour()))
					.findFirst();
			if (p.isEmpty()) return Optional.empty();
			else { return Optional.of(p.get().tickets())
						.map(tickets -> ticket -> p.get().tickets().getOrDefault(ticket, 0));}
		}



		/**
		 * @return MrX's travel log as a list of {@link LogEntry}s
		 */
		@Nonnull @Override
		public ImmutableList<LogEntry> getMrXTravelLog() {return log;}



		/**
		 * @return the winner of this game; empty if the game has no winners yet
		 * This is mutually exclusive with {@link #getAvailableMoves()}
		 */
		@Nonnull @Override
		public ImmutableSet<Piece> getWinner() {return winner;}



		/**
		 * Computes the available single move for player(MrX or detectives)
		 *
		 * @param setup the game setup
		 * @param detectives detective players
		 * @param player a game player
		 * @param source player's current location
		 * @return all available single moves of player as a set of {@link SingleMove}s
		 */
		private static Set<SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source){
            HashSet<SingleMove> singleMoveS = new HashSet<>();
            for(int destination : setup.graph.adjacentNodes(source)) {
                // if destination is occupied by a detective, don't add to the collection of moves to return
				boolean state = true;
				for (Player detective : detectives) {
					if (destination == detective.location()) { state = false; break; }
				}

				if (state) {
						for (Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of())) {
							// if the player has the required tickets, construct a SingleMove and add it the collection of moves to return
							if (player.has(t.requiredTicket()))
								singleMoveS.add(new SingleMove(player.piece(), source, t.requiredTicket(), destination));}

						if (player.has(Ticket.SECRET)) {
							// add moves to the destination via a secret ticket if there are any left with the player
							singleMoveS.add(new SingleMove(player.piece(), source, Ticket.SECRET, destination));
						}
				}
			}
			return singleMoveS;
		}



		/**
		 * Computes the available single move for MrX player
		 *
		 * @param setup the game setup
		 * @param detectives detective players
		 * @param mrX MrX player
		 * @param source player's current location
		 * @param log  MrX's travel log
		 * @return all available double moves of MrX as a set of {@link DoubleMove}s
		 */
        private static Set<DoubleMove> makeDoubleMoves(
				GameSetup setup, List<Player> detectives, Player mrX, int source, ImmutableList<LogEntry> log){
			HashSet<DoubleMove> doubleMoveS = new HashSet<>();
			// check whether mrx has double ticket
			if (mrX.has(Ticket.DOUBLE) && setup.moves.size() - log.size() > 1){

				// get information of first move from makeSingleMove
			Set<SingleMove> firstMoves = makeSingleMoves(setup, detectives, mrX, source);
			Iterator<SingleMove> firstMoveE = firstMoves.iterator();
			for (int i = 0; i<firstMoves.size(); i++) {
				SingleMove firstMove = firstMoveE.next();
				Ticket ticket1 = firstMove.ticket;
				Ticket ticket2;
				int destination1 = firstMove.destination;

				for (int destination2 : setup.graph.adjacentNodes(destination1)) {
					boolean state = true;
					for (Player detective : detectives) {
						if (destination2 == detective.location()) { state = false; break; }
						// if second destination is occupied by a detective, don't add to the collection of moves to return
					}

					if (state) {
						for (Transport t : setup.graph.edgeValueOrDefault(destination1, destination2, ImmutableSet.of())) {
							// if the player has the required tickets, construct a DoubleMove and add it the collection of moves to return
							ticket2 = t.requiredTicket();
							if (mrX.has(ticket2)) {
								if (ticket1 != ticket2)
									doubleMoveS.add(new DoubleMove(mrX.piece(), source, ticket1, destination1, ticket2, destination2));
								else if ( mrX.hasAtLeast(ticket1, 2))
									doubleMoveS.add(new DoubleMove(mrX.piece(), source, ticket1, destination1, ticket1, destination2));

								if (mrX.has(Ticket.SECRET)) {
									// add moves to the destination via a secret ticket if there are any left with the player
									doubleMoveS.add(new DoubleMove(mrX.piece(), source, Ticket.SECRET, destination1, ticket2, destination2));
									doubleMoveS.add(new DoubleMove(mrX.piece(), source, ticket1, destination1, Ticket.SECRET, destination2));

									if (mrX.hasAtLeast(Ticket.SECRET, 2))
										doubleMoveS.add(new DoubleMove(mrX.piece(), source, Ticket.SECRET, destination1, Ticket.SECRET, destination2));}}}}}}}

            return doubleMoveS;
        }

		/**
		 * @return the current available moves of the game; empty if the game is over
		 * This is mutually exclusive with {@link #getWinner()}
		 */
		@Nonnull @Override
		public ImmutableSet<Move> getAvailableMoves() {
			// when game is over, the remaining is set to null and return empty set
			if (remaining == null) { return ImmutableSet.of(); }

			// get available move for MrX
			if (remaining.contains(mrX.piece())) {
				Set<Move> mrxMove = new HashSet<>();
				mrxMove.addAll(makeSingleMoves(setup, detectives, mrX, mrX.location()));
				mrxMove.addAll(makeDoubleMoves(setup, detectives, mrX, mrX.location(), this.log));
				return ImmutableSet.copyOf(mrxMove);
			}

			// get available move for detective
			else {
				List<SingleMove> detMove = new ArrayList<>();

				// get moves only from remaining detective players
				for (Player d : detectives) {
					if (remaining.contains(d.piece()))
						detMove.addAll(makeSingleMoves(setup, detectives, d, d.location()));
				}

				return ImmutableSet.copyOf(detMove);
			}
		}
	}



	/**
	 * call {@link MyGameState} constructor if passed arguments are valid
	 *
	 * @param setup the game setup
	 * @param mrX MrX player
	 * @param detectives detective players
	 * @return GameState from calling {@link MyGameState} constructor
	 * @throws NullPointerException if MrX or detectives are null
	 * @throws IllegalArgumentException if passed arguments are not valid
	 */
	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) throws NullPointerException, IllegalArgumentException {

		// check graph
        if( setup.graph.nodes().isEmpty() ) throw new IllegalArgumentException();


		// check mrX
		if (mrX == null) throw new NullPointerException();


		// check detectives
		if (detectives == null) throw new NullPointerException();
		List<Piece> detectiveColour = new ArrayList<>();
		List<Integer> detectiveLocation = new ArrayList<>();

		for (Player d : detectives) {
			// detectives cannot hold SECRET or DOUBLE ticket which are for MrX
			if (d.has(Ticket.SECRET) || d.has(Ticket.DOUBLE)) throw new IllegalArgumentException();

			// each detective need to have unique piece
			if (detectiveColour.contains(d.piece())) throw new IllegalArgumentException();
			else detectiveColour.add(d.piece());

			// detectives cannot locate on same station
			if (detectiveLocation.contains(d.location())) throw new IllegalArgumentException();
			else detectiveLocation.add(d.location());

		}

		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);
	}

}
