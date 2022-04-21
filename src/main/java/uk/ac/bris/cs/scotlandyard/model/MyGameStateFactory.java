package uk.ac.bris.cs.scotlandyard.model;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.checkerframework.checker.units.qual.A;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

import java.util.*;
import java.util.stream.Stream;
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
			this.moves = getAvailableMoves();
			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);

			Piece nowMove = move.commencedBy();
			Player newMrx = this.mrX;
			List<Player> newDetectives = new LinkedList<>(detectives);
			int destinationOfMove = move.accept(new Visitor<>() {
				@Override
				public Integer visit(SingleMove move) {
					return move.destination;}
				@Override
				public Integer visit(DoubleMove move) {
					return move.destination2;}
			});
			List<LogEntry> advanceLog = new ArrayList<>(log);

			//when mrx moves
			if(nowMove == mrX.piece()){

				Piece[] getPiece = new Piece[detectives.size()];
				for (int i = 0; i < detectives.size(); i++) {
					getPiece[i] = detectives.get(i).piece();
				}
				remaining = ImmutableSet.copyOf(getPiece);
				Map<Ticket, Integer> mrxTickets = new HashMap<>();
				mrxTickets.putAll(mrX.tickets());
				if (move.getClass() == SingleMove.class){
					mrxTickets.put(((SingleMove) move).ticket, (int)mrxTickets.get(((SingleMove) move).ticket) -1);
				}
				if (move.getClass() == DoubleMove.class){
					mrxTickets.put(((DoubleMove) move).ticket1, (int)mrxTickets.get(((DoubleMove) move).ticket1) -1);
					mrxTickets.put(((DoubleMove) move).ticket2, (int)mrxTickets.get(((DoubleMove) move).ticket2) -1);
				}
				newMrx = new Player(mrX.piece(), ImmutableMap.copyOf(mrxTickets), destinationOfMove);


				for (Integer r : ScotlandYard.REVEAL_MOVES){
					if (move.getClass() == SingleMove.class){
						if (r == advanceLog.size()+1){advanceLog.add(LogEntry.hidden(((SingleMove) move).ticket)); break;}
						else {advanceLog.add(LogEntry.reveal(((SingleMove) move).ticket, destinationOfMove)); break;}
					}
					else if (move.getClass() == DoubleMove.class) {
						if (r == advanceLog.size()+1){
							advanceLog.add(LogEntry.hidden(((DoubleMove) move).ticket1));
							advanceLog.add(LogEntry.reveal(((DoubleMove) move).ticket2, destinationOfMove)); break;}
						else if (r == advanceLog.size()+2){
							advanceLog.add(LogEntry.reveal(((DoubleMove) move).ticket1, ((DoubleMove) move).destination1));
							advanceLog.add(LogEntry.hidden(((DoubleMove) move).ticket2));break;}
						else {advanceLog.add(LogEntry.reveal(((DoubleMove) move).ticket1, ((DoubleMove) move).destination1));
							advanceLog.add(LogEntry.reveal(((DoubleMove) move).ticket2, destinationOfMove));break;}
					}
				}
			}

			//when detective moves
			if(nowMove != mrX.piece()) {

				List<Piece> remainingL = new ArrayList<>(remaining);
				remainingL.remove(nowMove);
				int remainingSize = remainingL.size();
				Piece[] remainingA = remainingL.toArray(new Piece[remainingSize]);
				if (remainingSize == 0) {
					remaining = ImmutableSet.of(MrX.MRX);
				} else remaining = ImmutableSet.copyOf(remainingA);

				Map<Ticket, Integer> addMrxTickets = new HashMap<>();
				addMrxTickets.putAll(mrX.tickets());
				addMrxTickets.put(((SingleMove) move).ticket, (int) addMrxTickets.get(((SingleMove) move).ticket) + 1);
				newMrx = new Player(mrX.piece(), ImmutableMap.copyOf(addMrxTickets), mrX.location());


				Player player = null;
				for (Player p : newDetectives) {
					if (p.piece() == nowMove) {
						player = p;
						newDetectives.remove(p);
					}
				}

				Map<Ticket, Integer> detTickets = new HashMap<>();
				detTickets.putAll(player.tickets());
				detTickets.put(((SingleMove) move).ticket, (int) detTickets.get(((SingleMove) move).ticket) - 1);

				Player newDet = new Player(player.piece(), ImmutableMap.copyOf(detTickets), destinationOfMove);
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
			else {dt = p.get().tickets();
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
						}}}}
			return singleMoveHashSet;
		}

        private static Set<DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player mrX, int source){
            HashSet<DoubleMove> doubleMoveHashSet = new HashSet<>();
			Set<SingleMove> firstMoves = makeSingleMoves(setup, detectives, mrX, source);
			Iterator<SingleMove> firstMoveE = firstMoves.iterator();
			if (mrX.has(Ticket.DOUBLE) && setup.moves.size()>1){
				//TODO: change to log size
			for (int i = 0; i<firstMoves.size(); i++) {
				SingleMove firstMove = firstMoveE.next();
				Ticket ticket1 = firstMove.ticket;
				Ticket ticket2;
				int destination1 = firstMove.destination;

				//  find out if destination is occupied by a detective
				//  if the location is occupied, don't add to the collection of moves to return
				for (int destination2 : setup.graph.adjacentNodes(destination1)) {
					boolean state = true;
					for (int n = 0; n < detectives.size(); n++) {
						if (destination2 == detectives.get(n).location()) state = false;
					}
					if (state) {
						for (Transport t : setup.graph.edgeValueOrDefault(destination1, destination2, ImmutableSet.of())) {
							//  find out if the player has the required tickets
							//  if it does, construct a SingleMove and add it the collection of moves to return
							ticket2 = t.requiredTicket();
							if (mrX.has(ticket2)) {
								if (ticket1 != ticket2) {
									doubleMoveHashSet.add(new DoubleMove(mrX.piece(), source, ticket1, destination1, ticket2, destination2));
								}
								if (ticket1 == ticket2 & mrX.hasAtLeast(ticket1, 2)) {
									doubleMoveHashSet.add(new DoubleMove(mrX.piece(), source, ticket1, destination1, ticket1, destination2));
								}
								if (mrX.has(Ticket.SECRET)) {
									// consider the rules of secret moves here
									// add moves to the destination via a secret ticket if there are any left with the player
									doubleMoveHashSet.add(new DoubleMove(mrX.piece(), source, Ticket.SECRET, destination1, ticket2, destination2));
									doubleMoveHashSet.add(new DoubleMove(mrX.piece(), source, ticket1, destination1, Ticket.SECRET, destination2));

									if (mrX.hasAtLeast(Ticket.SECRET, 2)) {
										doubleMoveHashSet.add(new DoubleMove(mrX.piece(), source, Ticket.SECRET, destination1, Ticket.SECRET, destination2));

									}}}}}}}}

            return doubleMoveHashSet;
        }

		/**
		 * @return the current available moves of the game.
		 * This is mutually exclusive with {@link #getWinner()}
		 */
		@Nonnull @Override
		public ImmutableSet<Move> getAvailableMoves() {
			//mrx
			Set<SingleMove> mrxSingleS = new HashSet<>();
			mrxSingleS = makeSingleMoves(setup, detectives, mrX, mrX.location());
			Set<DoubleMove> mrxDoubleS = new HashSet<>();
			mrxDoubleS = makeDoubleMoves(setup, detectives, mrX, mrX.location());

			Move[] mrxMove = new Move[mrxSingleS.size() + mrxDoubleS.size()];

			Iterator<SingleMove> mrxSingleSE = mrxSingleS.iterator();
			for (int i = 0; i<mrxSingleS.size(); i++){
				mrxMove[i] = mrxSingleSE.next();
			}
			Iterator<DoubleMove> mrxDoubleSE = mrxDoubleS.iterator();
			for (int i = 0; i<mrxDoubleS.size(); i++){
				mrxMove[mrxSingleS.size()+i] = mrxDoubleSE.next();
			}

			//detective
			List<Set<SingleMove>> detSingleSL = new ArrayList<>();
			List<SingleMove> detSingleL = new ArrayList<>();
			for (Player d : detectives) {
				if(remaining.contains(d.piece())) {
					detSingleSL.add(makeSingleMoves(setup, detectives, d, d.location()));
				}
			}
			for ( Set<SingleMove> detSingleS : detSingleSL){
				detSingleL.addAll(detSingleS);
			}
			int detMoveSize = detSingleL.size();
			Move[] detMove = detSingleL.toArray(new Move[detMoveSize]);

			if (remaining.contains(mrX.piece())) {return ImmutableSet.copyOf(mrxMove);}
				else return ImmutableSet.copyOf(detMove);

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
