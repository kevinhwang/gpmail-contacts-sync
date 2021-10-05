package kevinhwang.gpmailcontactssync.tribe.model.list

import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotEmpty

@Validated
data class ListedPerson(
  val id: Int,
  val attributes: List<Attribute>
) {
  @Validated
  data class Attribute(
    @NotEmpty val name: String,

    val values: List<Any>
  )

  fun getAttributeValuesOrNull(@NotEmpty name: String): List<Any>? =
    attributes
      .singleOrNull { name == it.name }
      ?.values

  fun getAttributeValues(@NotEmpty name: String): List<Any> = checkNotNull(getAttributeValuesOrNull(name))
}
