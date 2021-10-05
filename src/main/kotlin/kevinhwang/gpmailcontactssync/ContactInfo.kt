package kevinhwang.gpmailcontactssync

import com.google.api.services.people.v1.model.Person

data class ContactInfo(val name: String, val source: Person)
