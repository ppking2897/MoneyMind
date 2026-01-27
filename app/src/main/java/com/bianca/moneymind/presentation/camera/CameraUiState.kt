package com.bianca.moneymind.presentation.camera

import android.graphics.Bitmap
import com.bianca.moneymind.domain.model.ReceiptParseResult

/**
 * Camera screen states
 */
sealed class CameraState {
    /** Showing camera preview, ready to capture */
    data object Preview : CameraState()

    /** Processing the captured image */
    data object Processing : CameraState()

    /** Showing scan results for confirmation */
    data class Result(
        val bitmap: Bitmap,
        val parseResult: ReceiptParseResult
    ) : CameraState()

    /** Error state */
    data class Error(val message: String) : CameraState()
}

/**
 * Saved transaction info for passing back to previous screen
 */
data class SavedTransactionInfo(
    val amount: Double,
    val description: String
)

/**
 * UI State for Camera Screen
 */
data class CameraUiState(
    val state: CameraState = CameraState.Preview,
    val hasPermission: Boolean = false,
    val savedTransaction: SavedTransactionInfo? = null
) {
    val transactionSaved: Boolean get() = savedTransaction != null
    val isPreview: Boolean get() = state is CameraState.Preview
    val isProcessing: Boolean get() = state is CameraState.Processing
    val isResult: Boolean get() = state is CameraState.Result
    val isError: Boolean get() = state is CameraState.Error

    val errorMessage: String?
        get() = (state as? CameraState.Error)?.message

    val scanResult: CameraState.Result?
        get() = state as? CameraState.Result
}
