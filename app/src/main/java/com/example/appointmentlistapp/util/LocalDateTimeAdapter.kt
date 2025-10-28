package com.example.appointmentlistapp.data.remote.adapters // Canvia pel teu subpaquet

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class LocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {

    // Defineix el format que coincideix amb la sortida del teu servidor C#
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun read(reader: JsonReader): LocalDateTime? {
        val dateString = reader.nextString() // Gson llegeix la cadena JSON
        return try {
            LocalDateTime.parse(dateString, formatter)
        } catch (e: DateTimeParseException) {
            e.printStackTrace()
            null
        }
    }

    override fun write(writer: JsonWriter, value: LocalDateTime?) {
        if (value == null) {
            writer.nullValue()
        } else {
            // Escriu LocalDateTime com una cadena ISO 8601 per a l'API
            writer.value(value.format(formatter))
        }
    }
}