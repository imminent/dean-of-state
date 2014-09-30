package com.imminentmeals.dean;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;

/**
 * The state of the app.
 */
public abstract class State extends EndlessObserver<Event> {

  private static final Map<Class<? extends State>, Constructor<State>> _constructors =
      new HashMap<>();
  private final Preconditions _preconditions;
  private final Observable<Event> _eventChannel;
  private Subscription _subscription;
  private List<State> _superStates;

  protected State(@Nonnull Observable<Event> eventChannel) {
    _preconditions = new Preconditions();
    _eventChannel = eventChannel;
  }

  protected State(@Nonnull State state) {
    _preconditions = new Preconditions();
    _eventChannel = state._eventChannel;
  }

  /**
   * Entry method for the {@link State}. This will be triggered on each external transition to the
   * State,
   * and for each of the State's super States as well. Do not call to super.<br/>
   * If class hierarchy is A -> B-> C, then entry order is A, B, C.
   */
  protected abstract void didEnter(@Nonnull Machine machine);

  /**
   * Exit method for the {@link State}. This will be triggered on each external transition to the
   * State,
   * and for each of the State's super States as well. Do not call to super.<br/>
   * If class hierarchy is A -> B-> C, then exit order is C, B, A.
   */
  protected abstract void didExit(@Nonnull Machine machine);

  /**
   * Specifies which {@link Event}s the {@link State} observes.
   *
   * @param event A given event
   * @return {@code true} indicates the given event is observed by this state
   */
  protected boolean observes(@Nonnull Event event) {
    return false;
  }

  /**
   * Specifies when a transition associated with the given {@link State} is not blocked from
   * occurring.
   *
   * @return {@code true} indicates that the transition can occur
   */
  protected boolean isNotGuarded(@SuppressWarnings("UnusedParameters") @Nonnull State state) {
    return true;
  }


  final void create() {
    _subscription = _eventChannel.filter(filter()).subscribe(this);
  }

  final void entry(@Nonnull Machine machine) {
    // Calls super states entry methods before calling it on the substate
    for (State superState : superStates()) {
      _preconditions.assertEntryCalledOnlyOnce(machine.isVerifyingEvents(), superState.getClass());
      superState.didEnter(machine);
    }
    _preconditions.assertEntryCalledOnlyOnce(machine.isVerifyingEvents(), this.getClass());
    didEnter(machine);
  }

  final void exit(@Nonnull Machine machine) {
    // Calls substate exit methods before calling it on the super states
    _preconditions.assertExitCalledOnlyOnce(machine.isVerifyingEvents(), this.getClass());
    _subscription.unsubscribe();
    didExit(machine);
    for (State superState : superStates()) {
      _preconditions.assertExitCalledOnlyOnce(machine.isVerifyingEvents(), superState.getClass());
      superState._subscription.unsubscribe();
      superState.didExit(machine);
    }
  }

  @Nonnull final Func1<Event, Boolean> filter() {
    return new Func1<Event, Boolean>() {
      @Override public Boolean call(Event event) {
        // Event will be observed if the active states observe it, and it hasn't been observed already
        final boolean willBeObserved = !event.wasObserved() && observes(event);
        if (willBeObserved) {
          event.willBeObserve();
        }
        return willBeObserved;
      }
    };
  }

  @Nonnull private List<? extends State> superStates() {
    if (_superStates != null) {
      return _superStates;
    }
    _superStates = new LinkedList<>();
    for (Class<?> superClass = this.getClass().getSuperclass();
        !superClass.isAssignableFrom(State.class); superClass = superClass.getSuperclass()) {
      try {
        @SuppressWarnings("unchecked")
        final Constructor<State> constructor = getConstructor((Class<State>) superClass);
        final State superState = constructor.newInstance(this);
        superState.create();
        _superStates.add(0, superState);
      } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
          IllegalAccessException error) {
        throw new AssertionError("Error getting super state: ", error);
      }
    }
    return _superStates;
  }

  @Nonnull private Constructor<State> getConstructor(Class<State> stateClass)
      throws NoSuchMethodException {
    if (_constructors.containsKey(stateClass)) {
      return _constructors.get(stateClass);
    }
    //noinspection unchecked
    final Constructor<State> constructor = stateClass.getConstructor(stateClass);
    _constructors.put(stateClass, constructor);
    return constructor;
  }

  private static class Preconditions {

    private final Set<Class<?>> _entryCalls;
    private final Set<Class<?>> _exitCalls;

    Preconditions() {
      _entryCalls = new HashSet<>();
      _exitCalls = new HashSet<>();
    }

    void assertEntryCalledOnlyOnce(boolean isVerifyingEvents, Class<?> stateClass) {
      if (isVerifyingEvents && _entryCalls.contains(stateClass)) {
        throw new AssertionError("Expected entry to be called once per super state");
      } else if (isVerifyingEvents) {
        _entryCalls.add(stateClass);
      }
    }

    void assertExitCalledOnlyOnce(boolean isVerifyingEvents, Class<?> stateClass) {
      if (isVerifyingEvents && _exitCalls.contains(stateClass)) {
        throw new AssertionError("Expected exit to be called once per super state");
      } else if (isVerifyingEvents) {
        _exitCalls.add(stateClass);
      }
    }
  }
}
