package com.imminentmeals.dean;

import javax.annotation.Nonnull;

/**
 * Contract for the {@link State} creator.
 */
public interface StateCreator {

  /**
   * Creates an instance of the given {@link State}.
   *
   * @param stateClass Type of state to create
   * @param <T> Generic of something that is a {@link State}
   * @param <R> Generic of something that is a {@link T}
   * @return an instance of {@link R} which is a state
   */
  @Nonnull <T extends State, R extends T> R createState(@Nonnull Class<T> stateClass);
}
