package kevinhwang.gpmailcontactssync.tribe.model.person

import org.springframework.validation.annotation.Validated
import java.time.Instant
import javax.validation.constraints.NotEmpty

@Validated
data class Person(
  val person_id: Int,
  val genres: List<Genre>
) {
  @Validated
  data class Genre(
    @NotEmpty val name: String,
    @NotEmpty val description: String,
    @NotEmpty val sections: List<Section>
  ) {
    @Validated
    data class Section(
      @NotEmpty val name: String,
      @NotEmpty val description: String,
      val attribute_types: List<AttributeType>
    ) {
      @Validated
      data class AttributeType(
        @NotEmpty val name: String,
        @NotEmpty val description: String,
        @NotEmpty val datatype: String,
        val is_editable: Boolean,
        val singleton: Boolean,
        val listable: Boolean,
        val attributes: List<Attribute>
      ) {
        @Validated
        data class Attribute(
          val id: Int,
          val start_date: Instant,
          val end_date: Instant?,
          val is_derived: Boolean,
          val value: Any?
        )
      }
    }
  }

  fun getAttributeOrNull(genre: String, section: String, attribute: String): List<Genre.Section.AttributeType.Attribute>? =
    genres.singleOrNull { it.name == genre }
      ?.sections
      ?.singleOrNull { it.name == section }
      ?.attribute_types
      ?.singleOrNull { it.name == attribute }
      ?.attributes
}
