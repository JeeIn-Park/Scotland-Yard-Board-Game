package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;

import java.util.*;
import javax.annotation.Nonnull;


public final class MyModelFactory implements Factory<Model> {
	private final class MyModel implements Model{
		private GameState game;
		private List<Observer> observers;

		private MyModel(
				final GameSetup setup,
				final Player mrX,
				final List<Player> detectives){

			this.observers = new ArrayList<>();
			this.game = new MyGameStateFactory().build(setup, mrX, ImmutableList.copyOf(detectives));

		}


		/**
		 * @return the current game board
		 */
		@Nonnull @Override
		public Board getCurrentBoard() {
			return this.game;}



		/**
		 * Registers an observer to the model. It is an error to register the same observer more than
		 * once.
		 *
		 * @param observer the observer to register
		 */
		@Override
		public void registerObserver(Observer observer) throws NullPointerException, IllegalArgumentException {
			if(observer == null) {throw new NullPointerException();}
			if(observers.contains(observer)) throw new IllegalArgumentException();
			observers.add(observer);}



		/**
		 * Unregisters an observer to the model. It is an error to unregister an observer not
		 * previously registered with {@link #registerObserver(Observer)}.
		 *
		 * @param observer the observer to register
		 */
		@Override
		public void unregisterObserver(Observer observer) throws NullPointerException, IllegalArgumentException{
			if(observer == null) {throw new NullPointerException();}
			if(! observers.contains(observer)) {throw new IllegalArgumentException();}
			observers.remove(observer);}



		/**
		 * @return all currently registered observers of the model
		 */
		@Nonnull @Override
		public ImmutableSet<Observer> getObservers() {
			return ImmutableSet.copyOf(observers);}



		/**
		 * @param move delegates the move to the underlying
		 * {@link uk.ac.bris.cs.scotlandyard.model.Board.GameState}
		 */
		@Override public void chooseMove(@Nonnull Move move){
			game = game.advance(move);
			Observer.Event e = Observer.Event.GAME_OVER;
			if (game.getWinner().isEmpty()) { e = Observer.Event.MOVE_MADE;}

			for (Observer o : observers){
				o.onModelChanged(getCurrentBoard(), e);
			}
		}

	}

	/**
	 * call {@link MyModel} constructor
	 *
	 * @param setup the game setup
	 * @param mrX MrX player
	 * @param detectives detective players
	 * @return Model from calling {@link MyModel} constructor
	 */
	@Nonnull @Override public Model build(GameSetup setup,
	                                      Player mrX,
	                                      ImmutableList<Player> detectives) {
		return new MyModel(setup, mrX, detectives);}

}
