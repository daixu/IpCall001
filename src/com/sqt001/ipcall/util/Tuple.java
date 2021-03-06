package com.sqt001.ipcall.util;

import java.util.Arrays;

/**
 * An immutable tuple of values that can be accessed using {@link #get} method.
 *
 * @param <A> Type of the tuple element.
 */
public class Tuple<A> {

  /**
   * The elements of the tuple.
   */
  private final A[] elements;

  /**
   * Factory method to create a tuple.
   *
   * @param <A> Type of the tuple element.
   * @param elements The elements of the tuple
   * @return A new tuple that contains {@code elements}
   */
  public static <A> Tuple<A> of(A ... elements) {
    return new Tuple<A>(elements);
  }

  /**
   * Constructor.
   *
   * @param elements The elements of the tuple.
   */
  public Tuple(A ... elements) {
    this.elements = elements;
  }

  /**
   * Returns the {@code index}th element of the tuple.
   *
   * @param index The index of the element.
   * @return The {@code index}th element of the tuple.
   */
  public A get(int index) {
    return elements[index];
  }

  /**
   * Returns the number of elements in the tuple.
   *
   * @return the number of elements in the tuple.
   */
  public int size() {
    return elements.length;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || o.getClass() != this.getClass()) {
      return false;
    }

    Tuple<A> o2 = (Tuple<A>) o;
    return Arrays.equals(elements, o2.elements);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(elements);
  }

  @Override
  public String toString() {
    return Arrays.toString(elements);
  }
}