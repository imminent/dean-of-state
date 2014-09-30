package com.imminentmeals.dean;

import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.annotation.Nonnull;
import javax.inject.Singleton;
import rx.subjects.Subject;

/**
 * Transitions the app between states.
 */
@Singleton
public class Machine {

  private final Subject<Event, Event> _eventChannel;
  private final Set<State> _states;
  private final StateCreator _stateCreator;
  private boolean _shouldVerifyEvents;

  @Inject Machine(@Nonnull StateCreator stateCreator, @Nonnull Subject<Event, Event> eventChannel,
      @Nonnull InitialState initialState) {
    _stateCreator = stateCreator;
    _states = new HashSet<>();
    _eventChannel = eventChannel;
    initialState.didEnter(this);
    _shouldVerifyEvents = false;
  }

  /**
   * Creates a readable String of the given set of active states.
   *
   * @param states The given states
   * @return Readable String of the given states
   */
  public static String toString(@Nonnull Set<State> states) {
    //CHECKSTYLE:OFF
    final StringBuilder builder = new StringBuilder(states.size() * 12 + 4);
    //CHECKSTYLE:ON
    builder.append('{');
    for (State state : states) {
      builder.append(state).append(", ");
    }
    final int length = builder.length();
    if (length > 1) {
      builder.replace(length - 1, length + 1, "");
    }
    builder.append(" }");
    return builder.toString();
  }

  /**
   * Transitions the state machine from one state to a new state.
   *
   * @param fromState The state the machine is leaving
   * @param toState The state the machine is entering
   */
  public void externalTransition(@Nonnull State fromState,
      @Nonnull Class<? extends State> toState) {
    final State newState = getState(toState);
    if (fromState.isNotGuarded(newState)) {
      fromState.exit(this);
      _states.remove(fromState);
      _states.add(newState);
      newState.entry(this);
    }
  }

  /**
   * Transitions the state machine to the same start again (a self-transition).
   *
   * @param state The state the machine is re-entering
   */
  public void selfTransition(@Nonnull State state) {
    externalTransition(state, state.getClass());
  }

  /**
   * Delivers events to observers.
   *
   * @param event The event to deliver
   */
  public void post(@Nonnull Event event) {
    if (_shouldVerifyEvents) {
      event.assertValidity(_states);
    }
    event.machine(this);
    _eventChannel.onNext(event);
  }

  /**
   * Determines if the given {@link State} is currently active in the state machine.
   *
   * @param state The given state to check
   * @return {@code true} indicates that the given state is active
   */
  public boolean isActive(@Nonnull Class<? extends State> state) {
    for (State activeState : _states) {
      if (state.isInstance(activeState)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Enables {@link Event} verifications.
   *
   * @see Event#assertValidity(Set)
   */
  public void shouldVerifyEvents() {
    _shouldVerifyEvents = true;
  }

  /**
   * Disables {@link Event} verifications.
   *
   * @see Event#assertValidity(Set)
   */
  public void noEventVerification() {
    _shouldVerifyEvents = false;
  }

  protected void initialTransition(@Nonnull Class<? extends State> toState) {
    final State newState = getState(toState);
    _states.add(newState);
    newState.entry(this);
  }

  @Nonnull protected <T extends State> T getState(@Nonnull Class<T> state) {
    final T newState = _stateCreator.createState(state);
    newState.create();
    return newState;
  }

  boolean isVerifyingEvents() {
    return _shouldVerifyEvents;
  }
}
