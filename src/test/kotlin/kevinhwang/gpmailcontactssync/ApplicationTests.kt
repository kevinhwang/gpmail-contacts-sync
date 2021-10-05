package kevinhwang.gpmailcontactssync

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
class ApplicationTests {

  @MockBean
  lateinit var application: Application

  @Test
  fun contextLoads() {
  }

}
