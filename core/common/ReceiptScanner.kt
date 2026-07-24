package com.naveenapps.expensemanager.core.common

import android.content.Context
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

@ExperimentalGetImage
class ReceiptAnalyzer(private val onAmountFound: (Double) -> Unit) : ImageAnalysis.Analyzer {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    // Scan the lines of text on the paper receipt
                    for (block in visionText.textBlocks) {
                        val text = block.text
                        // Regex matches currency layouts like KES 500, KSH 1200, or 2500.00
                        if (text.contains("KES", ignoreCase = true) || text.contains("KSH", ignoreCase = true)) {
                            val numbers = text.replace(Regex("[^0-9.]"), "")
                            val parsedAmount = numbers.toDoubleOrNull()
                            if (parsedAmount != null) {
                                onAmountFound(parsedAmount)
                                break
                            }
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
