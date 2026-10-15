package net.syrupstudios.edger.config.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import net.minecraft.resources.ResourceLocation

object ResourceLocationSerializer : JsonSerializer<ResourceLocation>() {
    override fun serialize(value: ResourceLocation, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.toString())
    }
}

object ResourceLocationDeserializer : JsonDeserializer<ResourceLocation>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): ResourceLocation = ResourceLocation(p.valueAsString)
}

@JsonSerialize(using = ResourceLocationSerializer::class)
@JsonDeserialize(using = ResourceLocationDeserializer::class)
abstract class ResourceLocationMixin
