package at.co.netconsulting.parkingticket.ui

import android.annotation.SuppressLint
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
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
import android.content.Context
import at.co.netconsulting.parkingticket.general.StaticFields

object ParkingplaceScreenSetup {
    @JvmStatic
    fun init(composeView: ComposeView, activity: Parkingplace) {
        val context = activity as Context

        val isCheckedParkingZones = loadBool(context, StaticFields.PARKING_ZONES_VIENNA)
        val isCheckedResident = loadBool(context, StaticFields.RESIDENT_PARKING_VIENNA)
        val isCheckedGarages = loadBool(context, StaticFields.GARAGES_VIENNA)

        val gpsEnabled = activity.isGPSEnabled

        composeView.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                ParkingplaceScreen(
                    gpsEnabled = gpsEnabled,
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
    gpsEnabled: Boolean,
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

    if (!gpsEnabled) {
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
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Text("Please turn on location services so that your current position can be shown.")
            }
        }
        return
    }

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
                        settings.javaScriptEnabled = true
                        settings.databaseEnabled = true
                        settings.domStorageEnabled = true
                        settings.javaScriptCanOpenWindowsAutomatically = true
                        settings.setGeolocationEnabled(true)

                        webChromeClient = object : WebChromeClient() {
                            override fun onGeolocationPermissionsShowPrompt(
                                origin: String?,
                                callback: GeolocationPermissions.Callback?
                            ) {
                                callback?.invoke(origin, true, false)
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                view?.loadUrl("javascript:init($choiceParking, 12)")
                            }
                        }

                        loadUrl("file:///android_asset/index.html")
                    }
                }
            )
        }
    }
}
