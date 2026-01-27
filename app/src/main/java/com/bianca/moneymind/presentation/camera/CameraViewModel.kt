package com.bianca.moneymind.presentation.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bianca.moneymind.domain.model.InputType
import com.bianca.moneymind.domain.model.Transaction
import com.bianca.moneymind.domain.model.TransactionType
import com.bianca.moneymind.domain.repository.TransactionRepository
import com.bianca.moneymind.domain.usecase.ocr.ParseReceiptUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val parseReceiptUseCase: ParseReceiptUseCase,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun onPermissionGranted() {
        _uiState.update { it.copy(hasPermission = true) }
    }

    fun onPermissionDenied() {
        _uiState.update {
            it.copy(
                hasPermission = false,
                state = CameraState.Error("需要相機權限才能掃描收據")
            )
        }
    }

    /**
     * Process captured image
     */
    fun onImageCaptured(bitmap: Bitmap) {
        _uiState.update { it.copy(state = CameraState.Processing) }

        viewModelScope.launch {
            // Convert Bitmap to ByteArray for use case (domain layer uses ByteArray)
            val imageData = ByteArrayOutputStream().use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                stream.toByteArray()
            }

            parseReceiptUseCase(imageData)
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            state = CameraState.Result(
                                bitmap = bitmap,
                                parseResult = result
                            )
                        )
                    }
                }
                .onError { exception ->
                    _uiState.update {
                        it.copy(state = CameraState.Error(exception.message ?: "未知錯誤"))
                    }
                }
        }
    }

    /**
     * Confirm and save the scanned transaction
     */
    fun onConfirmTransaction(
        amount: Double,
        categoryId: String,
        description: String,
        date: LocalDate
    ) {
        viewModelScope.launch {
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = amount,
                description = description,
                categoryId = categoryId,
                date = date,
                createdAt = Instant.now(),
                inputType = InputType.OCR,
                receiptImagePath = null, // We don't save the image
                merchantName = _uiState.value.scanResult?.parseResult?.receipt?.merchantName,
                note = null,
                rawInput = null
            )

            transactionRepository.add(transaction)

            // Notify that transaction was saved with info
            _uiState.update {
                it.copy(
                    savedTransaction = SavedTransactionInfo(
                        amount = amount,
                        description = description
                    )
                )
            }
        }
    }

    /**
     * Reset transaction saved flag after navigation
     */
    fun onTransactionSavedHandled() {
        _uiState.update { it.copy(savedTransaction = null, state = CameraState.Preview) }
    }

    /**
     * Retry scanning
     */
    fun onRetry() {
        _uiState.update { it.copy(state = CameraState.Preview) }
    }

    /**
     * Cancel and go back
     */
    fun onCancel() {
        _uiState.update { it.copy(state = CameraState.Preview) }
    }
}
