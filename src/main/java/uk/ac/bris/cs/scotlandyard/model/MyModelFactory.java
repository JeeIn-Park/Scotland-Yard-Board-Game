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


		@Nonnull @Override
		public Board getCurrentBoard() {
			return this.game;}


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


		@Override public void chooseMove(@Nonnull Move move){
			game = game.advance(move);
			Observer.Event e = Observer.Event.GAME_OVER;
			if (game.getWinner().isEmpty()) { e = Observer.Event.MOVE_MADE;}

			for (Observer o : observers){
				o.onModelChanged(getCurrentBoard(), e);
			}
		}

	}

	@Nonnull @Override public Model build(GameSetup setup,
	                                      Player mrX,
	                                      ImmutableList<Player> detectives) {
		return new MyModel(setup, mrX, detectives);}

}
