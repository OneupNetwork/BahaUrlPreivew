package com.baha.url.preview

import android.net.Uri
import kotlinx.coroutines.*
import java.util.*

class BahaUrlPreview(val url: String, var callback: IUrlPreviewCallback?) {
    val scope = CoroutineScope(Job() + Dispatchers.Main)
    private val imageExtensionArray = arrayOf(".gif", ".png", ".jpg", ".jpeg", ".bmp", ".webp")

    fun fetchUrlPreview(timeOut: Int = 30000) {
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            callback?.onFailed(throwable)
        }
        scope.launch(exceptionHandler) {
            fetch(timeOut)
        }
    }

    private suspend fun fetch(timeOut: Int = 30000) {
        lateinit var urlInfoItem: UrlInfoItem
        if (checkIsImageUrl()) {
            urlInfoItem = UrlInfoItem(url = url, image = url)
        } else {
            val document = getDocument(url, timeOut)
            urlInfoItem = parseHtml(document)
            urlInfoItem.url = url
        }
        callback?.onComplete(urlInfoItem)
    }

    private fun checkIsImageUrl(): Boolean {
        val uri = Uri.parse(url)
        var isImage = false
        for (imageExtension in imageExtensionArray) {
            if (uri.path != null && uri.path!!.toLowerCase(Locale.getDefault()).endsWith(imageExtension)) {
                isImage = true
                break
            }
        }
        return isImage
    }

    fun cleanUp() {
        scope.cancel()
        callback = null
    }
}