package com.bianca.moneymind.data.remote.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Service for OCR using ML Kit
 * Uses Chinese text recognizer for better Traditional Chinese support
 */
@Singleton
class MlKitOcrService @Inject constructor() {

    private val recognizer: TextRecognizer by lazy {
        TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    }

    /**
     * Recognize text from a bitmap image
     *
     * @param bitmap The image to process
     * @return OcrResult containing recognized text
     */
    suspend fun recognizeText(bitmap: Bitmap): OcrResult {
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        return suspendCancellableCoroutine { continuation ->
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val textBlocks = visionText.textBlocks.map { block ->
                        TextBlockResult(
                            text = block.text,
                            lines = block.lines.map { it.text },
                            boundingBox = block.boundingBox?.let { rect ->
                                BoundingBox(
                                    left = rect.left,
                                    top = rect.top,
                                    right = rect.right,
                                    bottom = rect.bottom
                                )
                            }
                        )
                    }

                    val result = OcrResult(
                        fullText = visionText.text,
                        textBlocks = textBlocks,
                        confidence = calculateConfidence(textBlocks)
                    )

                    continuation.resume(result)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }

            continuation.invokeOnCancellation {
                // Cleanup if needed
            }
        }
    }

    /**
     * Calculate overall confidence based on text blocks
     * More text blocks generally means better recognition
     */
    private fun calculateConfidence(textBlocks: List<TextBlockResult>): Float {
        if (textBlocks.isEmpty()) return 0f

        // Simple heuristic: more lines = potentially better receipt scan
        val totalLines = textBlocks.sumOf { it.lines.size }
        return when {
            totalLines >= 5 -> 0.9f
            totalLines >= 3 -> 0.7f
            totalLines >= 1 -> 0.5f
            else -> 0.3f
        }
    }

    /**
     * Close the recognizer when no longer needed
     */
    fun close() {
        recognizer.close()
    }
}
