package strawman.collection.mutable

import strawman.collection.{EndoIterable, IterableFactory, IterableLike, Iterator}

import scala.{Boolean, Option, Unit}
import scala.Predef.???

/** Mutable set backed by a hash trie */
final class HashSet[A]
  extends Set[A]
    with IterableLike[A, HashSet]
    with Buildable[A, HashSet[A]]
    with Builder[A, HashSet[A]] {

  def +=(elem: A): this.type = ???
  def -=(elem: A): this.type = ???
  def contains(elem: A): Boolean = ???
  def get(elem: A): Option[A] = ???

  def fromIterable[B](it: EndoIterable[B]): HashSet[B] = HashSet.fromIterable(it)
  protected[this] def fromIterableWithSameElemType(coll: EndoIterable[A]): HashSet[A] = fromIterable(coll)

  def newBuilder: Builder[A, HashSet[A]] = new HashSet[A]

  def result: HashSet[A] = this

  def iterator(): Iterator[A] = ???

  def clear(): Unit = ???

  def & (that: strawman.collection.EndoSet[A]): HashSet[A] = ???

  def ++ (that: strawman.collection.EndoSet[A]): HashSet[A] = ???

}

object HashSet extends IterableFactory[HashSet] {

  def fromIterable[B](it: EndoIterable[B]): HashSet[B] = {
    val result = new HashSet[B]
    for (elem <- it) {
      result += elem
    }
    result
  }

}
