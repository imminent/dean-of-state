package com.imminentmeals.dean;

import rx.Subscriber;

/**
 * An {@linkplain Subscriber observer} for a channel that goes on endlessly.
 */
public abstract class EndlessObserver<T> extends Subscriber<T> {

  @Override public void onCompleted() {
  }
}
