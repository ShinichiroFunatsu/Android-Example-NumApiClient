package app.sfunatsu.numberapiclient.model

data class NumTrivia(
    val text: String,
    val number: Long,
    val found: Boolean,
    val type: String
)