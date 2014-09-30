package com.imminentmeals.dean;

import javax.annotation.Nonnull;
import rx.Observable;

/**
 * Establishes the initial state of the state machine.
 */
public class InitialState extends State {

  protected InitialState(@Nonnull Observable<Event> eventChannel) {
    super(eventChannel);
  }

  /**
   * Defines the list of states the state machine starts in.
   *
   * @return The list of initial states
   */
  protected Class[] initialStates() {
    return new Class[0];
  }

  @SuppressWarnings("unchecked" /* Class -> Class<? extends State>*/)
  @Override protected final void didEnter(@Nonnull Machine machine) {
    for (Class initialState : initialStates()) {
      machine.initialTransition(initialState);
    }
  }

  @Override protected final void didExit(@Nonnull Machine machine) {
  }

  @Override public final void onError(Throwable e) {
  }

  @Override public final void onNext(Event event) {
  }

  @Override protected final boolean observes(@Nonnull Event event) {
    return super.observes(event);
  }

  @Override protected final boolean isNotGuarded(@Nonnull State state) {
    return super.isNotGuarded(state);
  }

  @Override public final void onCompleted() {
  }
}
