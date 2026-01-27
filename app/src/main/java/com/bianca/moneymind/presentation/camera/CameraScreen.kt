package com.bianca.moneymind.presentation.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bianca.moneymind.domain.model.ReceiptParseResult
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Result key for passing saved transaction info back to previous screen
 */
object CameraScreenResult {
    const val KEY_TRANSACTION_SAVED = "transaction_saved"
    const val KEY_AMOUNT = "saved_amount"
    const val KEY_DESCRIPTION = "saved_description"
}

@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    onTransactionSaved: (amount: Double, description: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Image capture instance
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onPermissionGranted()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    // Check permission on launch
    LaunchedEffect(Unit) {
        val permission = Manifest.permission.CAMERA
        when {
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.onPermissionGranted()
            }
            else -> {
                permissionLauncher.launch(permission)
            }
        }
    }

    // Navigate when transaction is saved
    LaunchedEffect(uiState.savedTransaction) {
        uiState.savedTransaction?.let { saved ->
            viewModel.onTransactionSavedHandled()
            onTransactionSaved(saved.amount, saved.description)
        }
    }

    CameraContent(
        uiState = uiState,
        imageCapture = imageCapture,
        onNavigateBack = onNavigateBack,
        onCapture = {
            scope.launch {
                val bitmap = imageCapture.takePicture(
                    ContextCompat.getMainExecutor(context)
                )
                bitmap?.let { viewModel.onImageCaptured(it) }
            }
        },
        onConfirm = { amount, categoryId, description, date ->
            viewModel.onConfirmTransaction(amount, categoryId, description, date)
            // Navigation will happen via LaunchedEffect when transactionSaved becomes true
        },
        onRetry = viewModel::onRetry,
        onCancel = {
            viewModel.onCancel()
            onNavigateBack()
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CameraContent(
    uiState: CameraUiState,
    imageCapture: ImageCapture,
    onNavigateBack: () -> Unit,
    onCapture: () -> Unit,
    onConfirm: (Double, String, String, LocalDate) -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("掃描收據", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState.state) {
                is CameraState.Preview -> {
                    if (uiState.hasPermission) {
                        CameraPreviewContent(
                            imageCapture = imageCapture,
                            onCapture = onCapture
                        )
                    } else {
                        PermissionDeniedContent(onCancel = onCancel)
                    }
                }

                is CameraState.Processing -> {
                    ProcessingContent()
                }

                is CameraState.Result -> {
                    ResultContent(
                        bitmap = state.bitmap,
                        parseResult = state.parseResult,
                        onConfirm = onConfirm,
                        onRetry = onRetry,
                        onCancel = onCancel
                    )
                }

                is CameraState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = onRetry,
                        onCancel = onCancel
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraPreviewContent(
    imageCapture: ImageCapture,
    onCapture: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        CameraPreview(
            imageCapture = imageCapture,
            modifier = Modifier.fillMaxSize()
        )

        // Receipt frame overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .border(
                        width = 2.dp,
                        color = Color.White.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        }

        // Hint text
        Text(
            text = "將收據對準框內",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Capture button
        IconButton(
            onClick = onCapture,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .size(72.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = "拍照",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
private fun ProcessingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("正在辨識收據...")
        }
    }
}

@Composable
private fun ResultContent(
    bitmap: Bitmap,
    parseResult: ReceiptParseResult,
    onConfirm: (Double, String, String, LocalDate) -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    val receipt = parseResult.receipt

    // Editable fields
    var amount by remember {
        mutableStateOf(receipt.totalAmount?.toString() ?: "")
    }
    var description by remember {
        mutableStateOf(receipt.merchantName ?: receipt.items.firstOrNull()?.name ?: "")
    }
    var selectedDate by remember {
        mutableStateOf(receipt.date ?: LocalDate.now())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Preview image (small)
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "收據照片",
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confidence indicator
        val confidence = receipt.confidence
        val confidenceColor = when {
            confidence >= 0.8f -> MaterialTheme.colorScheme.primary
            confidence >= 0.5f -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.error
        }
        Text(
            text = "辨識信心度: ${(confidence * 100).toInt()}%",
            color = confidenceColor,
            style = MaterialTheme.typography.labelMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Amount field
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("金額") },
            prefix = { Text("$") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Description field
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("描述") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Date display
        OutlinedTextField(
            value = selectedDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")),
            onValueChange = { },
            label = { Text("日期") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            singleLine = true
        )

        // Items list (if any)
        if (receipt.items.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "辨識到的品項",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    receipt.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.name} x${item.quantity}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = item.totalPrice?.let { "$$it" } ?: "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("重拍")
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("取消")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val amountValue = amount.toDoubleOrNull()
                if (amountValue != null && amountValue > 0) {
                    onConfirm(
                        amountValue,
                        parseResult.suggestedCategoryId ?: "expense_other",
                        description,
                        selectedDate
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = amount.toDoubleOrNull()?.let { it > 0 } == true
        ) {
            Text("確認記錄")
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancel) {
                Text("取消")
            }
            Button(onClick = onRetry) {
                Text("重試")
            }
        }
    }
}

@Composable
private fun PermissionDeniedContent(onCancel: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "需要相機權限才能掃描收據",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "請在設定中開啟相機權限",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onCancel) {
            Text("返回")
        }
    }
}
