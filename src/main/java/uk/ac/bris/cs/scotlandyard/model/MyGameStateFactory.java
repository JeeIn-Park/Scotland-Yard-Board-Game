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
			this.winner = checkWinner();
			if(setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");
		}

		private ImmutableSet<Piece> checkWinner() {

			if (remaining.contains(mrX.piece())) {
				///if mrx cannot move
				if (getAvailableMoves().isEmpty()) {
					remaining = null;
					return ImmutableSet.copyOf(getDetectivePieceArrayList());
				}
				//if mrx filled the log and it becomes mrx's turn again
				if (log.size() == setup.moves.size()){
					remaining = null;
					return ImmutableSet.of(mrX.piece());
				}
			}

			boolean state = true;
			for (Player p : detectives) {
				// if detective catch mrx
				if (p.location() == mrX.location()){
					remaining = null;
					return ImmutableSet.copyOf(getDetectivePieceArrayList());
				}
				// if there is at least one detective has tickets, state becomes false and skip to find winner
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


		@Nonnull @Override public GameSetup getSetup() { return setup; }


		public List<Piece> getDetectivePieceArrayList() {
			List<Piece> getPiece = new ArrayList<>();

			for (Player d : detectives) {
				getPiece.add(d.piece());
			}

			return getPiece;
		}


		@Nonnull @Override
		public ImmutableSet<Piece> getPlayers() {

			Piece[] getPiece = new Piece[detectives.size() + 1];
			getPiece[0] = mrX.piece();

			for (int i = 0; i < detectives.size(); i++) {
				getPiece[i + 1] = detectives.get(i).piece();
			}
			return ImmutableSet.copyOf(getPiece);
        }


		@Nonnull @Override
		public GameState advance(Move move) throws IllegalArgumentException{
			this.moves = getAvailableMoves();
			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);

			Player newMrx = this.mrX;
			List<Player> newDetectives = new LinkedList<>(detectives);
			List<LogEntry> advanceLog = new ArrayList<>(log);

			//when mrx moves
			if(move.commencedBy() == mrX.piece()){

				//remaining
				List<Piece> r = getDetectivePieceArrayList();
				for (Player p : detectives) {
					if (p.tickets().equals(ImmutableMap.of( TAXI, 0, BUS, 0, UNDERGROUND, 0, Ticket.DOUBLE, 0, Ticket.SECRET, 0))){
						r.remove(p.piece());
					}
				}
				remaining = ImmutableSet.copyOf(r);

				//tickets, log
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
						if (setup.moves.get(advanceLog.size()))
							advanceLog.add(LogEntry.reveal(move.ticket1, move.destination1));
						else advanceLog.add(LogEntry.hidden(move.ticket1));
						if (setup.moves.get(advanceLog.size()))
							advanceLog.add(LogEntry.reveal(move.ticket2, move.destination2));
						else advanceLog.add(LogEntry.hidden(move.ticket2));

						mrxTickets.put(move.ticket1, mrxTickets.get(move.ticket1) -1);
						mrxTickets.put(move.ticket2, mrxTickets.get(move.ticket2) -1);
						mrxTickets.put(Ticket.DOUBLE, mrxTickets.get(Ticket.DOUBLE) -1);

						return new Player(mrX.piece(), ImmutableMap.copyOf(mrxTickets), move.destination2);
					}
				});
			}


			//when detective moves
			if(move.commencedBy() != mrX.piece()) {

				//remaining
				List<Piece> remainingL = new ArrayList<>(remaining);
				remainingL.remove(move.commencedBy());
				if (remainingL.isEmpty()) {
					remaining = ImmutableSet.of(MrX.MRX);
				} else remaining = ImmutableSet.copyOf(remainingL);

				//ticket
				Map<Ticket, Integer> addMrxTickets = new HashMap<>();
				addMrxTickets.putAll(mrX.tickets());
				addMrxTickets.put(((SingleMove) move).ticket, addMrxTickets.get(((SingleMove) move).ticket) + 1);
				newMrx = new Player(mrX.piece(), ImmutableMap.copyOf(addMrxTickets), mrX.location());

				Map<Ticket, Integer> detTickets = new HashMap<>();
				for (Player p : detectives) {
					if (p.piece() == move.commencedBy()) {
						Player player;
						player = p;
						newDetectives.remove(p);
						detTickets.putAll(player.tickets());
						detTickets.put(((SingleMove) move).ticket, detTickets.get(((SingleMove) move).ticket) - 1);
					}
				}

				Player newDet = new Player(move.commencedBy(), ImmutableMap.copyOf(detTickets), ((SingleMove) move).destination);
				newDetectives.add(newDet);
			}
				return new MyGameState(setup, remaining, ImmutableList.copyOf(advanceLog), newMrx, newDetectives);
		}


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


		@Nonnull @Override
		public Optional<Board.TicketBoard> getPlayerTickets(Piece piece) {
			ImmutableMap<Ticket, Integer> xt;
			xt = this.mrX.tickets();
			if (piece == MrX.MRX) return Optional.of(xt)
					.map(tickets -> ticket -> xt.getOrDefault(ticket, 0));

			Optional<Player> p = detectives.stream()
					.filter(d -> d.piece().webColour().equals(piece.webColour()))
					.findFirst();
			ImmutableMap<Ticket, Integer> dt;
			if (p.isEmpty()) return Optional.empty();
			else { dt = p.get().tickets(); return Optional.of(dt)
						.map(tickets -> ticket -> dt.getOrDefault(ticket, 0));}
		}


		@Nonnull @Override
		public ImmutableList<LogEntry> getMrXTravelLog() {return log;}


		@Nonnull @Override
		public ImmutableSet<Piece> getWinner() { 
			return winner;
		}


		private static Set<SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source){
            HashSet<SingleMove> singleMoveS = new HashSet<>();
            for(int destination : setup.graph.adjacentNodes(source)) {
                //  if destination is occupied by a detective, don't add to the collection of moves to return
				boolean state = true;
				for (Player detective : detectives) {
					if (destination == detective.location()) { state = false; break; }
				}

				if (state) {
						for (Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of())) {
							//  if the player has the required tickets, construct a SingleMove and add it the collection of moves to return
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


        private static Set<DoubleMove> makeDoubleMoves(
				GameSetup setup, List<Player> detectives, Player mrX, int source, ImmutableList<LogEntry> log){
			HashSet<DoubleMove> doubleMoveS = new HashSet<>();
			//check whether mrx has double ticket
			if (mrX.has(Ticket.DOUBLE) && setup.moves.size() - log.size() > 1){

				//get required information of first move from makeSingleMove
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
						//  if destination is occupied by a detective, don't add to the collection of moves to return
					}

					if (state) {
						for (Transport t : setup.graph.edgeValueOrDefault(destination1, destination2, ImmutableSet.of())) {
							//  if the player has the required tickets, construct a DoubleMove and add it the collection of moves to return
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


		@Nonnull @Override
		public ImmutableSet<Move> getAvailableMoves() {
			if (remaining == null) { return ImmutableSet.of(); }

			//mrx
			if (remaining.contains(mrX.piece())) {
				Set<SingleMove> mrxSingleS = new HashSet<>(makeSingleMoves(setup, detectives, mrX, mrX.location()));
				Set<DoubleMove> mrxDoubleS = new HashSet<>(makeDoubleMoves(setup, detectives, mrX, mrX.location(), this.log));
				Move[] mrxMove = new Move[mrxSingleS.size() + mrxDoubleS.size()];


				// single move
				Iterator<SingleMove> mrxSingleSE = mrxSingleS.iterator();
				for (int i = 0; i < mrxSingleS.size(); i++) {
					mrxMove[i] = mrxSingleSE.next();
				}

				// double move
				Iterator<DoubleMove> mrxDoubleSE = mrxDoubleS.iterator();
				for (int i = 0; i < mrxDoubleS.size(); i++) {
					mrxMove[mrxSingleS.size() + i] = mrxDoubleSE.next();
				}

				return ImmutableSet.copyOf(mrxMove);
			}

			//detective
			else {
				List<Set<SingleMove>> detSingleSL = new ArrayList<>();

				// get moves from remaining detective players
				for (Player d : detectives) {
					if (remaining.contains(d.piece()))
						detSingleSL.add(makeSingleMoves(setup, detectives, d, d.location()));
				}

				List<SingleMove> detSingleL = new ArrayList<>();
				for (Set<SingleMove> detSingleS : detSingleSL) {
					detSingleL.addAll(detSingleS);
				}

				return ImmutableSet.copyOf(detSingleL);
			}
		}
	}


	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) throws NullPointerException, IllegalArgumentException {

		//check graph
        if( setup.graph.nodes().isEmpty() ) throw new IllegalArgumentException();


		//check mrX
		if (mrX == null) throw new NullPointerException();


		//check detectives
		if (detectives == null) throw new NullPointerException();
		List<Piece> detectiveColour = new ArrayList<>();
		List<Integer> detectiveLocation = new ArrayList<>();

		for (Player d : detectives) {

			if (d.has(Ticket.SECRET) || d.has(Ticket.DOUBLE)) throw new IllegalArgumentException();

			if (detectiveColour.contains(d.piece())) throw new IllegalArgumentException();
			else detectiveColour.add(d.piece());

			if (detectiveLocation.contains(d.location())) throw new IllegalArgumentException();
			else detectiveLocation.add(d.location());

		}

		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);
	}

}
