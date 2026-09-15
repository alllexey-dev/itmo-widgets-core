package dev.alllexey.itmowidgets.core.utils

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import dev.alllexey.itmowidgets.core.model.UserData
import dev.alllexey.itmowidgets.core.model.social.RelationshipState
import dev.alllexey.itmowidgets.core.model.social.UserProfile

/** Both profile fields are required; Gson must not bypass Kotlin's non-null contract. */
class UserProfileTypeAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (type.rawType != UserProfile::class.java) return null
        val userAdapter = gson.getAdapter(UserData::class.java)
        val relationshipAdapter = gson.getAdapter(RelationshipState::class.java)
        val adapter = object : TypeAdapter<UserProfile>() {
            override fun write(out: JsonWriter, value: UserProfile) {
                out.beginObject()
                out.name("user")
                userAdapter.write(out, value.user)
                out.name("relationship")
                relationshipAdapter.write(out, value.relationship)
                out.endObject()
            }

            override fun read(reader: JsonReader): UserProfile {
                if (reader.peek() != JsonToken.BEGIN_OBJECT) throw JsonParseException("Profile must be an object")
                var user: UserData? = null
                var relationship: RelationshipState? = null
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "user" -> {
                            if (user != null) throw JsonParseException("Duplicate profile user")
                            user = userAdapter.read(reader) ?: throw JsonParseException("Missing profile user")
                        }
                        "relationship" -> {
                            if (relationship != null) throw JsonParseException("Duplicate relationship")
                            relationship = relationshipAdapter.read(reader)
                        }
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                return UserProfile(
                    user ?: throw JsonParseException("Missing profile user"),
                    relationship ?: throw JsonParseException("Missing relationship"),
                )
            }
        }
        // ApiResponse.data remains nullable for failures; a non-null profile cannot be incomplete.
        @Suppress("UNCHECKED_CAST")
        return adapter.nullSafe() as TypeAdapter<T>
    }
}
