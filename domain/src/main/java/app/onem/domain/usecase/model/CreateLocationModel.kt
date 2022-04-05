package app.onem.domain.usecase.model

data class CreateLocationModel(
    val displayName: String,
    val address: Map<String, String>,
    val merchantUsername: String
)
