package at.co.netconsulting.parkingticket.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import at.co.netconsulting.parkingticket.Parkingplace
import at.co.netconsulting.parkingticket.R
import at.co.netconsulting.parkingticket.general.StaticFields
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

private const val PARKING_MAP_TAG = "ParkingMap"
private const val PARKING_MAP_BASE_URL = "https://parkingticket.local/"
private const val PARKING_MAP_PAGE_URL = "${PARKING_MAP_BASE_URL}index.html"
private const val PARKING_MAP_HOST = "parkingticket.local"

private val parkingLayerUrls = mapOf(
    "KURZPARKZONEOGD.json" to "https://data.wien.gv.at/daten/geo?service=WFS&request=GetFeature&version=1.1.0&typeName=ogdwien:KURZPARKZONEOGD&srsName=EPSG:4326&outputFormat=json",
    "PARKENANRAINEROGD.json" to "https://data.wien.gv.at/daten/geo?service=WFS&request=GetFeature&version=1.1.0&srsName=EPSG:4326&outputFormat=json&typeName=ogdwien:PARKENANRAINEROGD",
    "GARAGENOGD.json" to "https://data.wien.gv.at/daten/geo?service=WFS&request=GetFeature&version=1.1.0&typeName=ogdwien:GARAGENOGD&srsName=EPSG:4326&outputFormat=json"
)

object ParkingplaceScreenSetup {
    @JvmStatic
    fun init(composeView: ComposeView, activity: Parkingplace) {
        val context = activity as Context

        val isCheckedParkingZones = loadBool(context, StaticFields.PARKING_ZONES_VIENNA)
        val isCheckedResident = loadBool(context, StaticFields.RESIDENT_PARKING_VIENNA)
        val isCheckedGarages = loadBool(context, StaticFields.GARAGES_VIENNA)

        composeView.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                ParkingplaceScreen(
                    initialParkingZones = isCheckedParkingZones,
                    initialResident = isCheckedResident,
                    initialGarages = isCheckedGarages,
                    geoJsonItems = context.resources.getStringArray(R.array.geojson).toList(),
                    onSavePreference = { key, value -> saveBool(context, key, value) },
                    onBackPressed = { activity.finish() }
                )
            }
        }
    }

    private fun loadBool(context: Context, key: String): Boolean {
        return context.getSharedPreferences(key, Context.MODE_PRIVATE).getBoolean(key, false)
    }

    private fun saveBool(context: Context, key: String, value: Boolean) {
        context.getSharedPreferences(key, Context.MODE_PRIVATE).edit().putBoolean(key, value).commit()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ParkingplaceScreen(
    initialParkingZones: Boolean,
    initialResident: Boolean,
    initialGarages: Boolean,
    geoJsonItems: List<String>,
    onSavePreference: (String, Boolean) -> Unit,
    onBackPressed: () -> Unit
) {
    var showDialog by remember { mutableStateOf(true) }
    var parkingZones by remember { mutableStateOf(initialParkingZones) }
    var resident by remember { mutableStateOf(initialResident) }
    var garages by remember { mutableStateOf(initialGarages) }
    var choiceParking by remember { mutableIntStateOf(-1) }
    var dialogDismissed by remember { mutableStateOf(false) }

    // Layer selection dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Select map layers") },
            text = {
                Column {
                    if (geoJsonItems.size >= 3) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Checkbox(
                                checked = parkingZones,
                                onCheckedChange = {
                                    parkingZones = it
                                    onSavePreference(StaticFields.PARKING_ZONES_VIENNA, it)
                                }
                            )
                            Text(geoJsonItems[0], modifier = Modifier.padding(start = 8.dp))
                        }
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Checkbox(
                                checked = resident,
                                onCheckedChange = {
                                    resident = it
                                    onSavePreference(StaticFields.RESIDENT_PARKING_VIENNA, it)
                                }
                            )
                            Text(geoJsonItems[1], modifier = Modifier.padding(start = 8.dp))
                        }
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Checkbox(
                                checked = garages,
                                onCheckedChange = {
                                    garages = it
                                    onSavePreference(StaticFields.GARAGES_VIENNA, it)
                                }
                            )
                            Text(geoJsonItems[2], modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // Calculate choice code (same logic as original)
                    val selected = mutableListOf<Int>()
                    if (parkingZones) selected.add(1)
                    if (resident) selected.add(2)
                    if (garages) selected.add(3)

                    choiceParking = when {
                        selected.isEmpty() -> -1
                        selected.size == 1 -> selected[0]
                        selected.size == 2 -> selected[0] * 10 + selected[1]
                        else -> 123
                    }
                    showDialog = false
                    dialogDismissed = true
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onBackPressed) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parking places") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (dialogDismissed) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                factory = { ctx ->
                    WebView(ctx).apply {
                        setBackgroundColor(android.graphics.Color.WHITE)
                        settings.javaScriptEnabled = true
                        settings.databaseEnabled = true
                        settings.domStorageEnabled = true
                        settings.javaScriptCanOpenWindowsAutomatically = true
                        settings.setGeolocationEnabled(true)
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                consoleMessage ?: return false
                                Log.d(
                                    PARKING_MAP_TAG,
                                    "${consoleMessage.messageLevel()}: ${consoleMessage.message()} (${consoleMessage.sourceId()}:${consoleMessage.lineNumber()})"
                                )
                                return true
                            }

                            override fun onGeolocationPermissionsShowPrompt(
                                origin: String?,
                                callback: GeolocationPermissions.Callback?
                            ) {
                                callback?.invoke(origin, true, false)
                            }
                        }

                        webViewClient = object : ParkingMapWebViewClient(ctx) {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                view?.post {
                                    view.evaluateJavascript(
                                        """
                                            (function() {
                                                if (typeof init !== "function") {
                                                    console.error("Parking map init function is missing");
                                                    return "missing-init";
                                                }
                                                init($choiceParking, 12);
                                                return "started";
                                            })();
                                        """.trimIndent()
                                    ) { result ->
                                        Log.d(PARKING_MAP_TAG, "Init result: $result")
                                    }
                                }
                            }
                        }

                        loadDataWithBaseURL(
                            PARKING_MAP_BASE_URL,
                            loadMapHtml(ctx),
                            "text/html",
                            "UTF-8",
                            PARKING_MAP_PAGE_URL
                        )
                    }
                }
            )
        }
    }
}

private fun loadMapHtml(context: Context): String {
    return context.assets.open("index.html").bufferedReader().use { it.readText() }
}

private open class ParkingMapWebViewClient(
    private val context: Context
) : WebViewClient() {

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url ?: return null
        if (url.host != PARKING_MAP_HOST) {
            return null
        }

        return when (url.path) {
            "/leaflet.css" -> assetResponse("leaflet.css", "text/css")
            "/jquery.min.js" -> assetResponse("jquery.min.js", "application/javascript")
            "/leaflet.js" -> assetResponse("leaflet.js", "application/javascript")
            "/auto.png" -> assetResponse("auto.png", "image/png", null)
            else -> {
                if (url.path?.startsWith("/layers/") == true) {
                    parkingLayerResponse(url.lastPathSegment.orEmpty())
                } else {
                    null
                }
            }
        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
            Log.e(PARKING_MAP_TAG, "WebView error ${error?.errorCode}: ${error?.description}")
        }
    }

    private fun assetResponse(
        assetName: String,
        mimeType: String,
        encoding: String? = "UTF-8"
    ): WebResourceResponse? {
        return try {
            response(mimeType, encoding, context.assets.open(assetName))
        } catch (exception: Exception) {
            Log.e(PARKING_MAP_TAG, "Unable to load map asset $assetName", exception)
            null
        }
    }

    private fun parkingLayerResponse(layerName: String): WebResourceResponse {
        val sourceUrl = parkingLayerUrls[layerName]
            ?: return textResponse(404, "Not Found", """{"type":"FeatureCollection","features":[]}""")

        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(sourceUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 45_000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
            }

            val statusCode = connection.responseCode
            val responseBytes = if (statusCode in 200..299) {
                connection.inputStream.readBytes()
            } else {
                connection.errorStream?.readBytes() ?: ByteArray(0)
            }

            if (statusCode in 200..299) {
                Log.d(PARKING_MAP_TAG, "Loaded $layerName (${responseBytes.size} bytes)")
                response("application/json", "UTF-8", ByteArrayInputStream(responseBytes))
            } else {
                Log.e(PARKING_MAP_TAG, "Vienna layer $layerName returned HTTP $statusCode")
                textResponse(statusCode, connection.responseMessage ?: "Upstream Error", responseBytes.decodeToString())
            }
        } catch (exception: Exception) {
            Log.e(PARKING_MAP_TAG, "Unable to load Vienna layer $layerName", exception)
            textResponse(502, "Bad Gateway", """{"type":"FeatureCollection","features":[]}""")
        } finally {
            connection?.disconnect()
        }
    }

    private fun textResponse(
        statusCode: Int,
        reasonPhrase: String,
        body: String
    ): WebResourceResponse {
        return response(
            "application/json",
            "UTF-8",
            statusCode,
            reasonPhrase,
            ByteArrayInputStream(body.toByteArray())
        )
    }

    private fun response(
        mimeType: String,
        encoding: String?,
        data: InputStream
    ): WebResourceResponse {
        return response(mimeType, encoding, 200, "OK", data)
    }

    private fun response(
        mimeType: String,
        encoding: String?,
        statusCode: Int,
        reasonPhrase: String,
        data: InputStream
    ): WebResourceResponse {
        return WebResourceResponse(
            mimeType,
            encoding,
            statusCode,
            reasonPhrase,
            mapOf("Access-Control-Allow-Origin" to "*"),
            data
        )
    }
}
