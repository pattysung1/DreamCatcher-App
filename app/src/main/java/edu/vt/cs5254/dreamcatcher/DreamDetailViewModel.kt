package edu.vt.cs5254.dreamcatcher

import androidx.lifecycle.ViewModel
import android.text.format.DateFormat
import java.util.Date

class DreamDetailViewModel : ViewModel() {

    var dream: Dream
    lateinit var lastUpdateDateTime : String

    init {
        dream = Dream(title = "My First Dream")
        dream.entries += listOf(
            DreamEntry(
                kind = DreamEntryKind.REFLECTION,
                text = "Reflection One",
                dreamId = dream.id
            ),
            DreamEntry(
                kind = DreamEntryKind.REFLECTION,
                text = "Reflection Two",
                dreamId = dream.id
            ),
            DreamEntry(
                kind = DreamEntryKind.DEFERRED,
                dreamId = dream.id
            )
        )

        lastUpdateDateTime = DateFormat.format("'Last updated' yyyy-MM-dd 'at' hh:mm:ss A", Date()).toString()
    }
}