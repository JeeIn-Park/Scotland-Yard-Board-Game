package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.*;
import javax.annotation.Nonnull;


public final class MyModelFactory implements Factory<Model> {
	private final class MyModel implements Model{
		private Board board;
		private List<Observer> observers;

		private MyModel(){
			this.observers = new ArrayList<>();
		}



		@Nonnull @Override
		public Board getCurrentBoard() {
			return this.board;}



		@Override
		public void registerObserver(Observer observer) throws NullPointerException, IllegalArgumentException {
			if(observer == null) {throw new NullPointerException();}
			if(observers.contains(observer)) throw new IllegalArgumentException();
			observers.add(observer);}



		@Override
		public void unregisterObserver(Observer observer) throws NullPointerException, IllegalArgumentException{
			if(observer == null) {throw new NullPointerException();}
			if(! observers.contains(observer)) {throw new IllegalArgumentException();}
			observers.remove(observer);}



		@Nonnull @Override
		public ImmutableSet<Observer> getObservers() {
			return ImmutableSet.copyOf(observers);}

		/**
		 * @param move delegates the move to the underlying
		 * {@link uk.ac.bris.cs.scotlandyard.model.Board.GameState}
		 */
		@Override
		public void chooseMove(@Nonnull Move move) {

		}
	}

	@Nonnull @Override public Model build(GameSetup setup,
	                                      Player mrX,
	                                      ImmutableList<Player> detectives) {
		return new MyModel();}

}
