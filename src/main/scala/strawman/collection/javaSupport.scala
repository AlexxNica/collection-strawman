package strawman.collection

import strawman.collection.immutable.List

import scala.{Array, Char, Int, Long, Double, Boolean, AnyVal, specialized}
import scala.Predef.String
import scala.annotation.unchecked.uncheckedVariance
import scala.reflect.ClassTag

import java.util.PrimitiveIterator

import strawman.collection.mutable.{ArrayBuffer, Buildable, StringBuilder}

class StringOps(val s: String)
  extends AnyVal with IterableOps[Char]
    with SeqMonoTransforms[Char, String]
    with IterablePolyTransforms[Char, List]
    with Buildable[Char, String]
    with ArrayLike[Char] {

  protected def coll = new StringView(s)
  def iterator() = coll.iterator()

  protected def fromIterableWithSameElemType(coll: Iterable[Char]): String = {
    val sb = new StringBuilder
    for (ch <- coll) sb += ch
    sb.result
  }

  def fromIterable[B](coll: Iterable[B]): List[B] = List.fromIterable(coll)

  protected[this] def newBuilder = new StringBuilder

  def length = s.length
  def apply(i: Int) = s.charAt(i)

  override def knownSize = s.length

  override def className = "String"

  /** Overloaded version of `map` that gives back a string, where the inherited
    *  version gives back a sequence.
    */
  def map(f: Char => Char): String = {
    val sb = new StringBuilder
    for (ch <- s) sb += f(ch)
    sb.result
  }

  /** Overloaded version of `flatMap` that gives back a string, where the inherited
    *  version gives back a sequence.
    */
  def flatMap(f: Char => String): String = {
    val sb = new StringBuilder
    for (ch <- s) sb ++= f(ch)
    sb.result
  }

  /** Overloaded version of `++` that gives back a string, where the inherited
    *  version gives back a sequence.
    */
  def ++(xs: IterableOnce[Char]): String = {
    val sb = new StringBuilder() ++= s
    for (ch <- xs.iterator()) sb += ch
    sb.result
  }

  /** Another overloaded version of `++`. */
  def ++(xs: String): String = s + xs
}

case class StringView(s: String) extends IndexedView[Char] {
  def length = s.length
  def apply(n: Int) = s.charAt(n)
  override def className = "StringView"
  def elementClassTag: ClassTag[Char] = ClassTag.Char
}


class ArrayOps[A](val xs: Array[A])
  extends AnyVal with IterableOps[A]
    with SeqMonoTransforms[A, Array[A]]
    with Buildable[A, Array[A]]
    with ArrayLike[A] {

  protected def coll = new ArrayView(xs)
  def iterator() = coll.iterator()

  def length = xs.length
  def apply(i: Int) = xs.apply(i)

  override def view = new ArrayView(xs)

  def elemTag: ClassTag[A] = ClassTag(xs.getClass.getComponentType)

  protected def fromIterableWithSameElemType(coll: Iterable[A]): Array[A] = coll.toArray[A](elemTag)

  def fromIterable[B: ClassTag](coll: Iterable[B]): Array[B] = coll.toArray[B]

  protected[this] def newBuilder = ArrayBuffer[A]()(elemTag).mapResult(_.toArray(elemTag))

  override def knownSize = xs.length

  override def className = "Array"

  def map[B: ClassTag](f: A => B): Array[B] = fromIterable(View.Map(coll, f))
  def flatMap[B: ClassTag](f: A => IterableOnce[B]): Array[B] = fromIterable(View.FlatMap(coll, f))
  def ++[B >: A : ClassTag](xs: IterableOnce[B]): Array[B] = fromIterable(View.Concat(coll, xs))
  def zip[B: ClassTag](xs: IterableOnce[B]): Array[(A, B)] = fromIterable(View.Zip(coll, xs))
}

class ArrayView[@specialized(Int, Long, Double) A](val elems: Array[A], val start: Int, val end: Int) extends IndexedView[A] { self =>
  def this(elems: Array[A]) = this(elems, 0, elems.length)

  def length = end - start

  def apply(n: Int) = elems(start + n)

  override def className = "ArrayView"

  def elementClassTag: ClassTag[A] = ClassTag(elems.getClass.getComponentType)

  override def specializedIterator(implicit spec: Specialized[A@uncheckedVariance]): spec.Iterator = {
    elementClassTag.runtimeClass match {
      case java.lang.Integer.TYPE =>
        val intElems = elems.asInstanceOf[Array[Int]]
        def it: PrimitiveIterator.OfInt = new PrimitiveIterator.OfInt {
          private var current = 0
          def hasNext = current < self.length
          def nextInt(): Int = {
            val r = intElems(start + current)
            current += 1
            r
          }
        }
        spec.asInstanceOf[Specialized[_]] match {
          case Specialized.SpecializedInt =>
            it
          case Specialized.SpecializedLong =>
            new PrimitiveIterator.OfLong {
              override def nextLong(): Long = it.nextInt()
              override def hasNext: Boolean = it.hasNext
            }
          case Specialized.SpecializedDouble =>
            new PrimitiveIterator.OfDouble {
              override def nextDouble(): Double = it.nextInt()
              override def hasNext: Boolean = it.hasNext
            }
          case _ =>
            super.specializedIterator(spec)
        }
      //TODO case java.lang.Integer.LONG =>
      //TODO case java.lang.Integer.DOUBLE =>
      case _ => super.specializedIterator(spec)
    }
  }.asInstanceOf[spec.Iterator]
}
