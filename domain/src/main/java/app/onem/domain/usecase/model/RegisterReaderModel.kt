package app.onem.domain.usecase.model

data class RegisterReaderModel(
    val label: String,
    val registrationCode: String,
    val location: String,
    val merchantUsername: String
)
