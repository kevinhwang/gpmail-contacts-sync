package kevinhwang.gpmailcontactssync

import org.springframework.core.ParameterizedTypeReference

fun <T> Sequence<T>.takeWhileInclusive(pred: (T) -> Boolean): Sequence<T> {
  var shouldContinue = true
  return takeWhile {
    val result = shouldContinue
    shouldContinue = pred(it)
    result
  }
}

inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}
