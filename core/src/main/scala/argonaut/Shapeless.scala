package argonaut

import shapeless.{ Strict, Witness }
import argonaut.derive._
import argonaut.util.LowPriority

trait SingletonInstances {

  implicit def singletonTypeEncodeJson[S, W]
   (implicit
     w: Witness.Aux[S],
     widen: Widen.Aux[S, W],
     underlying: EncodeJson[W]
   ): EncodeJson[S] =
    underlying.contramap[S](widen.to)

  implicit def singletonTypeDecodeJson[S, W]
   (implicit
     w: Witness.Aux[S],
     widen: Widen.Aux[S, W],
     underlying: DecodeJson[W]
   ): DecodeJson[S] =
    DecodeJson { c =>
      underlying.decode(c).flatMap { w0 =>
        widen.from(w0) match {
          case Some(s) => DecodeResult.ok(s)
          case None => DecodeResult.fail(s"Expected ${w.value}, got $w0", c.history)
        }
      }
    }

}

trait DefaultProductCodec {
  implicit def defaultJsonProductCodecFor[T]: JsonProductCodecFor[T] =
    new JsonProductCodecFor[T] {
      def codec = JsonProductCodec.obj
    }
}

trait DefaultSumCodec {
  implicit def defaultJsonSumCodecFor[T]: JsonSumCodecFor[T] =
    new JsonSumCodecFor[T] {
      def codec = JsonSumCodec.obj
    }
}

trait DerivedInstances extends DefaultProductCodec with DefaultSumCodec {

  implicit def mkEncodeJson[T]
   (implicit
     priority: Strict[LowPriority[EncodeJson[T], MkEncodeJson[T]]]
   ): EncodeJson[T] =
    priority
      .value
      .value
      .encodeJson

  implicit def mkDecodeJson[T]
   (implicit
     priority: Strict[LowPriority[DecodeJson[T], MkDecodeJson[T]]]
   ): DecodeJson[T] =
    priority
      .value
      .value
      .decodeJson

}

trait CachedDerivedInstances extends DefaultProductCodec with DefaultSumCodec {

  implicit def mkEncodeJson[T]
   (implicit
     priority: Strict.Global[LowPriority[EncodeJson[T], MkEncodeJson[T]]]
   ): EncodeJson[T] =
    priority
      .value
      .value
      .encodeJson

  implicit def mkDecodeJson[T]
   (implicit
     priority: Strict.Global[LowPriority[DecodeJson[T], MkDecodeJson[T]]]
   ): DecodeJson[T] =
    priority
      .value
      .value
      .decodeJson

}

object Shapeless
  extends SingletonInstances
  with DerivedInstances {

  object Cached
    extends SingletonInstances
    with CachedDerivedInstances

}
