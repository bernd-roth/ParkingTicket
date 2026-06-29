package at.co.netconsulting.parkingticket.ui

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import at.co.netconsulting.parkingticket.ParkedCarActivity
import at.co.netconsulting.parkingticket.R
import at.co.netconsulting.parkingticket.parking.ParkingPathCodec
import at.co.netconsulting.parkingticket.parking.ParkingPositionStore
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private const val CAR_MAP_BASE_URL = "https://parked-car.local/"
private const val CAR_MAP_PAGE_URL = "${CAR_MAP_BASE_URL}parked_car.html"
private const val CAR_MAP_HOST = "parked-car.local"

object ParkedCarScreenSetup {
    @JvmStatic
    fun init(composeView: ComposeView, activity: ParkedCarActivity) {
        composeView.setContent {
            MaterialTheme(colorScheme = androidx.compose.material3.darkColorScheme()) {
                ParkedCarScreen(
                    context = activity,
                    onBack = { activity.finish() },
                    onClear = { activity.clearParkedCar() }
                )
            }
        }
    }
}

private data class ParkingMapSnapshot(
    val carLatitude: Double,
    val carLongitude: Double,
    val savedAt: Long,
    val path: List<ParkingPathCodec.Point>
) {
    fun toJson(): String {
        val pathJson = path.joinToString(prefix = "[", postfix = "]") {
            "[${it.latitude},${it.longitude}]"
        }
        val current = path.lastOrNull()
        val currentJson = if (current == null) "null" else "[${current.latitude},${current.longitude}]"
        return """{"car":[$carLatitude,$carLongitude],"current":$currentJson,"path":$pathJson}"""
    }

    fun distanceToCar(): Int? {
        val current = path.lastOrNull() ?: return null
        val result = FloatArray(1)
        Location.distanceBetween(current.latitude, current.longitude, carLatitude, carLongitude, result)
        return result[0].roundToInt()
    }
}

private fun loadSnapshot(context: Context): ParkingMapSnapshot? {
    val car = ParkingPositionStore.getCar(context) ?: return null
    return ParkingMapSnapshot(car.latitude, car.longitude, car.savedAt, ParkingPositionStore.getPath(context))
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ParkedCarScreen(context: Context, onBack: () -> Unit, onClear: () -> Unit) {
    var snapshot by remember { mutableStateOf(loadSnapshot(context)) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2_000L)
            snapshot = loadSnapshot(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.parked_car_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = context.getString(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            snapshot?.let { current ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Column {
                        Text(
                            context.getString(
                                R.string.car_saved_at,
                                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
                                    .format(Date(current.savedAt))
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            current.distanceToCar()?.let { context.getString(R.string.distance_to_car, it) }
                                ?: context.getString(R.string.waiting_for_location),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = onClear) { Text(context.getString(R.string.clear_car_position)) }
                }
            }

            AndroidView(
                modifier = Modifier.fillMaxWidth().weight(1f),
                factory = { webContext ->
                    WebView(webContext).apply {
                        contentDescription = webContext.getString(R.string.parked_car_map)
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadsImagesAutomatically = true
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                                Log.e("ParkedCarMap", "${message.message()} (${message.lineNumber()})")
                                return true
                            }
                        }
                        webViewClient = CarMapWebViewClient(webContext) {
                            snapshot?.let { updateParkingMap(it) }
                        }
                        loadDataWithBaseURL(
                            CAR_MAP_BASE_URL,
                            webContext.assets.open("parked_car.html").bufferedReader().use { it.readText() },
                            "text/html",
                            "UTF-8",
                            CAR_MAP_PAGE_URL
                        )
                    }
                },
                update = { webView ->
                    snapshot?.let { webView.updateParkingMap(it) }
                },
                onRelease = { it.destroy() }
            )
        }
    }
}

private class CarMapWebViewClient(
    private val context: Context,
    private val onReady: WebView.() -> Unit
) : WebViewClient() {
    override fun onPageFinished(view: WebView, url: String?) {
        view.onReady()
    }

    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        val url = request?.url ?: return null
        if (url.host != CAR_MAP_HOST) return null
        val asset = when (url.path) {
            "/leaflet.css" -> "leaflet.css" to "text/css"
            "/leaflet.js" -> "leaflet.js" to "application/javascript"
            else -> return null
        }
        return WebResourceResponse(asset.second, "UTF-8", context.assets.open(asset.first))
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        Log.e("ParkedCarMap", "Failed ${request?.url}: ${error?.description}")
    }

    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        Log.e("ParkedCarMap", "HTTP ${errorResponse?.statusCode} for ${request?.url}")
    }
}

private fun WebView.updateParkingMap(snapshot: ParkingMapSnapshot) {
    evaluateJavascript(
        "if (typeof updateParkingMap === 'function') { updateParkingMap(${snapshot.toJson()}); }",
        null
    )
}
