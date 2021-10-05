package kevinhwang.gpmailcontactssync

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty

@Validated
@ConstructorBinding
@ConfigurationProperties("app.google-oauth2")
data class GoogleOauth2ClientConfigurationProperties(
  @NotEmpty var clientId: String,
  @NotEmpty var clientSecret: String,
  @NotEmpty var clientHost: String,
  @Min(0) var clientPort: Int,
  @NotEmpty var clientCallbackPath: String
)
