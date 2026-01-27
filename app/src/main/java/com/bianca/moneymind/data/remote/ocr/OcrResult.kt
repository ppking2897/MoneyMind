package com.bianca.moneymind.data.remote.ocr

/**
 * Result of OCR text recognition
 */
data class OcrResult(
    val fullText: String,
    val textBlocks: List<TextBlockResult>,
    val confidence: Float
)

/**
 * A block of text from OCR
 */
data class TextBlockResult(
    val text: String,
    val lines: List<String>,
    val boundingBox: BoundingBox?
)

/**
 * Bounding box for text location
 */
data class BoundingBox(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)
