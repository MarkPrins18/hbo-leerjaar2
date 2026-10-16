package models.gemini

import kotlinx.serialization.Serializable

/** The values Gemini estimates for a car. `null` means Gemini has no result. */
@Serializable
data class CarCostEstimate(
    val roadTaxPerQuarter: Double?,
    val consumptionCombined: Double?
)

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig
)

@Serializable
data class GeminiGenerationConfig(
    val responseMimeType: String,
    val responseSchema: GeminiSchema
)

@Serializable
data class GeminiSchema(
    val type: String,
    val properties: Map<String, GeminiSchema>? = null,
    val required: List<String>? = null,
    val nullable: Boolean? = null,
    val description: String? = null
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate> = emptyList()
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart> = emptyList()
)

@Serializable
data class GeminiPart(
    val text: String? = null
)
