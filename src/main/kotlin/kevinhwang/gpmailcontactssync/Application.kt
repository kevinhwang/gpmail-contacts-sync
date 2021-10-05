package kevinhwang.gpmailcontactssync

import com.google.api.services.people.v1.PeopleService
import com.google.api.services.people.v1.model.Person
import com.google.api.services.people.v1.model.UpdateContactPhotoRequest
import kevinhwang.gpmailcontactssync.tribe.TribeClient
import mu.KotlinLogging
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.core.io.ByteArrayResource
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.*

fun main(args: Array<String>) {
  runApplication<Application>(*args)
}

private val LOGGER = KotlinLogging.logger {}

@SpringBootApplication
@ConfigurationPropertiesScan
class Application(
  private val googlePeopleService: Lazy<PeopleService>,
  private val webClientBuilder: WebClient.Builder,
  private val optionalTribeClient: Optional<TribeClient>
) : ApplicationRunner {

  private fun listContacts(pageToken: String? = null) = googlePeopleService.value
    .people()
    .connections()
    .list("people/me")
    .setPageToken(pageToken)
    .setSources(listOf("READ_SOURCE_TYPE_PROFILE", "READ_SOURCE_TYPE_CONTACT"))
    .setPersonFields("names,emailAddresses,photos")
    .execute()

  private fun getPhotoFromProfile(person: Person): Pair<String, Mono<ByteArray>>? =
    person.photos.firstOrNull {
      it.metadata?.source?.type == "PROFILE" && (it.metadata?.primary ?: false) && !(it.default ?: false)
    }
      ?.url
      ?.replace("""=s\d+$""".toRegex(), "=s2048")
      ?.let {
        it to webClientBuilder
          .clone()
          .baseUrl(it)
          .build()
          .get()
          .retrieve()
          .bodyToMono(ByteArrayResource::class.java)
          .mapNotNull { it.byteArray }
      }

  private fun getPhotoFromTribe(gpmail: String): Mono<Pair<String, Mono<ByteArray>>> =
    optionalTribeClient.get().let { tribe ->
      tribe
        .listPeople(mapOf("gpmail" to gpmail), setOf("gpmail"))
        .single()
        .flatMap { tribe.getPerson(it.id) }
        .mapNotNull {
          it
            .getAttributeOrNull("user-info", "personal", "avatar")
            ?.singleOrNull()
            ?.id
        }
        .map { tribe.getAsset(it!!) }
    }

  override fun run(args: ApplicationArguments?) {
    LOGGER.info { "Listing contacts..." }

    val contacts = generateSequence(this::listContacts) {
      listContacts(it.nextPageToken)
    }
      .takeWhileInclusive { it.nextPageToken != null }
      .flatMap { it.connections }
      .toSet()

    LOGGER.info { "Listed ${contacts.size} contacts" }

    val contactsMissingExplicitPhotos = contacts.filter {
      it.photos.none {
        it.metadata?.source?.type == "CONTACT" && (it.metadata?.primary ?: false) && !(it.default ?: false)
      }
    }.map {
      ContactInfo(
        name = it.names.first {
          it.metadata.primary ?: false
        }.displayName,
        source = it
      )
    }.toSet()

    LOGGER.info { "Found ${contactsMissingExplicitPhotos.size} contacts missing explicit photos" }

    contactsMissingExplicitPhotos.forEach { (name, source) ->
      val resourceName = source.resourceName

      val photoFromProfile = getPhotoFromProfile(source)

      val (photoUrl, photoBytes) = if (photoFromProfile != null) {
        LOGGER.info { "Found photo=${photoFromProfile.first} from directory for contact=${name}" }
        photoFromProfile
      } else {
        LOGGER.info { "No photos found in directory for contact=${name}" }

        if (optionalTribeClient.isPresent) {
          source
            .emailAddresses
            ?.firstOrNull {
              it.metadata?.source?.type == "DOMAIN_PROFILE" && it.value?.endsWith("@gpmail.org") ?: false
            }
            ?.value
            ?.let {
              LOGGER.info { "Looking up photo from gpmail=${it} in Tribe" }

              getPhotoFromTribe(it)
                .block()
            }
            ?.also { (url, _) -> LOGGER.info { "Found photo=${url} from Tribe for contact=${name}" } }
            ?: let {
              LOGGER.warn { "No photo for contact=${name} found in Tribe" }

              return@forEach
            }
        } else {
          return@forEach
        }
      }

      print("Set photo=${photoUrl} for contact=${name}? (Y/n): ")
      val answer = readLine()

      if (answer?.lowercase() != "y") {
        LOGGER.info { "Skipping contact=${name}" }
        return@forEach
      }

      LOGGER.info { "Setting photo=${photoUrl} for person=${resourceName}" }


      val photoB64 = Base64.getEncoder().encodeToString(photoBytes.block())

      val request = UpdateContactPhotoRequest().setPhotoBytes(photoB64)

      googlePeopleService.value.people().updateContactPhoto(resourceName, request).execute()

      LOGGER.info { "Set photo for person=${resourceName}" }
    }
  }
}
