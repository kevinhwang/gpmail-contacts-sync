package kevinhwang.gpmailcontactssync

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.MemoryDataStoreFactory
import com.google.api.services.people.v1.PeopleService
import com.google.api.services.people.v1.PeopleServiceScopes
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component
import java.util.function.Supplier


@Component
class GooglePeopleServiceConfiguration {
  @Bean
  fun googleApiClientHttpTransport(): HttpTransport = GoogleNetHttpTransport.newTrustedTransport()

  @Bean
  fun googleApiClientJsonFactory(): JsonFactory = GsonFactory.getDefaultInstance()

  @Bean
  fun googleClientSecrets(
    jsonFactory: JsonFactory,
    resourceLoader: ResourceLoader,
    googleOauth2ClientConfigurationProperties: GoogleOauth2ClientConfigurationProperties
  ): GoogleClientSecrets =
    GoogleClientSecrets.load(
      jsonFactory,
      resourceLoader.getResource("classpath:google-oauth2-client-secrets-template.json").inputStream.reader()
    )
      .apply {
        web.apply {
          clientId = googleOauth2ClientConfigurationProperties.clientId
          clientSecret = googleOauth2ClientConfigurationProperties.clientSecret
        }
      }

  @Bean
  fun googleAuthCodeFlow(
    httpTransport: HttpTransport,
    jsonFactory: JsonFactory,
    googleClientSecrets: GoogleClientSecrets,
  ): AuthorizationCodeFlow = GoogleAuthorizationCodeFlow.Builder(
    httpTransport,
    jsonFactory,
    googleClientSecrets,
    setOf(PeopleServiceScopes.CONTACTS)
  )
    .setDataStoreFactory(MemoryDataStoreFactory.getDefaultInstance())
    .setAccessType("offline")
    .build()

  @Bean
  fun localServerReceiver(googleOauth2ClientConfigurationProperties: GoogleOauth2ClientConfigurationProperties): VerificationCodeReceiver =
    LocalServerReceiver.Builder()
      .setHost(googleOauth2ClientConfigurationProperties.clientHost)
      .setPort(googleOauth2ClientConfigurationProperties.clientPort)
      .setCallbackPath(googleOauth2ClientConfigurationProperties.clientCallbackPath)
      .build()

  @Bean
  fun googleAuthCodeApp(
    flow: AuthorizationCodeFlow,
    receiver: VerificationCodeReceiver
  ): AuthorizationCodeInstalledApp =
    AuthorizationCodeInstalledApp(flow, receiver)

  @Bean
  fun googlePeopleServiceSupplier(
    httpTransport: HttpTransport,
    jsonFactory: JsonFactory,
    authorizationCodeInstalledApp: AuthorizationCodeInstalledApp
  ): Lazy<PeopleService> = lazy {
    PeopleService.Builder(
      httpTransport,
      jsonFactory,
      authorizationCodeInstalledApp.authorize("user")
    ).build()
  }
}
