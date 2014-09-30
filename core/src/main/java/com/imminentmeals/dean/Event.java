package com.imminentmeals.dean;

import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Something that triggers behavior in the state machine.
 */
public class Event {

  private Machine _machine;
  private boolean _wasObserved = false;

  /**
   * Retrieves the reference to the state machine.
   *
   * @return The state machine
   */
  @Nonnull public Machine machine() {
    return _machine;
  }

  /**
   * Assertions about the validity of the {@link Event} being triggered given the current state of
   * the state machine.
   *
   * @param activeStates The active states in the state machine
   */
  protected void assertValidity(@Nonnull Set<State> activeStates) {
  }

  /* package */void machine(@Nonnull Machine machine) {
    _machine = machine;
  }

  /* package */void willBeObserve() {
    _wasObserved = true;
  }

  /**
   * Indicates when the {@link Event} has already been seen.
   *
   * @return {@code true} indicates that the event was already observed
   */
  /* package */boolean wasObserved() {
    return _wasObserved;
  }
}
