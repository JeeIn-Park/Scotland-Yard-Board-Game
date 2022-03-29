package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import javax.annotation.Nonnull;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
		// TODO
		try {
			throw new RuntimeException("e");
		} catch (Exception e) {
			System.out.println("EXception haha");
		}

		return null;
	}

}
