package kevinhwang.gpmailcontactssync

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty

@Validated
@ConstructorBinding
@ConfigurationProperties("app.tribe")
@ConditionalOnProperty("app.tribe.api-key")
data class TribeConfigurationProperties(
  @NotEmpty var host: String,
  @NotEmpty var apiKey: String
)
