package at.co.netconsulting.parkingticket.ui

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import at.co.netconsulting.parkingticket.MainActivity
import at.co.netconsulting.parkingticket.R
import java.util.Calendar
import kotlin.math.roundToInt

class MainScreenState {
    var nextParkingTicketText = mutableStateOf("")

    fun updateNextParkingTicket(text: String) {
        nextParkingTicketText.value = text
    }
}

object MainScreenSetup {
    @JvmStatic
    fun init(composeView: ComposeView, activity: MainActivity, state: MainScreenState) {
        val context = composeView.context
        val cities = context.resources.getStringArray(R.array.city).toList()

        composeView.setContent {
            MaterialTheme(
                colorScheme = darkColorScheme()
            ) {
                MainScreen(
                    cities = cities,
                    state = state,
                    onStartAlarm = { startH, startM, stopH, stopM, interval, city, duration, stopEnabled ->
                        activity.startAlarmFromCompose(
                            startH, startM, stopH, stopM, interval, city, duration, stopEnabled
                        )
                    },
                    onStopAlarm = { city ->
                        activity.stopAlarmFromCompose(city)
                    },
                    onNavigateToSettings = { activity.navigateToSettings() },
                    onNavigateToParkingplaces = { activity.navigateToParkingplaces() },
                    getDurationsForCity = { city -> getDurationsForCity(context, city) }
                )
            }
        }
    }

    private fun getDurationsForCity(context: Context, city: String): List<String> {
        val arrayId = when (city) {
            "Baden Z1 (Blaue Zone)" -> R.array.baden_z1_minutes
            "Baden Z2 (Grüne Zone)" -> R.array.baden_z2_minutes
            "Bruck an der Leitha" -> R.array.bruck_an_der_leitha_minutes
            "Eisenstadt Zone A" -> R.array.eisenstadt_zona_a_minutes
            "Eisenstadt Zone B" -> R.array.eisenstadt_zona_b_minutes
            "Eisenstadt Zone C" -> R.array.eisenstadt_zona_c_minutes
            "Gleisdorf" -> R.array.gleisdorf
            "Gmunden" -> R.array.gmunden
            "Graz Zone 1" -> R.array.graz_z1
            "Graz Zone 2" -> R.array.graz_z2
            "Graz Zone 3" -> R.array.graz_z3
            "Graz Zone 5" -> R.array.graz_z5
            "Graz Zone 15" -> R.array.graz_z15
            "Hall in Tirol" -> R.array.hall_in_tirol
            "IIG Parkplatz Sillside" -> R.array.iig_parkplatz_sillside
            "Innsbruck Zone 1" -> R.array.innsbruck_zone_1
            "Innsbruck Zone 2" -> R.array.innsbruck_zone_2
            "Innsbruck Zone 3" -> R.array.innsbruck_zone_3
            "Innsbruck Zone 4" -> R.array.innsbruck_zone_4
            "Innsbruck Zone 5" -> R.array.innsbruck_zone_5
            "Klagenfurt Zone 1" -> R.array.klagenfurt_zone_1
            "Klagenfurt Zone 2" -> R.array.klagenfurt_zone_2
            "Klosterneuburg Zone 1" -> R.array.klosterneuburg_zone_1
            "Klosterneuburg Zone 2" -> R.array.klosterneuburg_zone_2
            "Korneuburg" -> R.array.korneuburg
            "Krems Zone 1 (Blaue Zone)" -> R.array.krems_zone_1
            "Krems Zone 2" -> R.array.krems_zone_2
            "Linz Zone 1" -> R.array.linz_zone_1
            "Linz Zone 2" -> R.array.linz_zone_2
            "Linz Zone 3" -> R.array.linz_zone_3
            "Mödling" -> R.array.moedling
            "Neusiedl am See Zone 1" -> R.array.neusiedl_am_see_zone_1
            "Neusiedl am See Zone 2" -> R.array.neusiedl_am_see_zone_2
            "Oberwart" -> R.array.oberwart
            "Parktiger Flughafen Wien" -> R.array.parktiger_flughafen_wien
            "Parktiger P + R Aspern" -> R.array.parktiger_p_r_aspern
            "Parktiger P + R Heiligenstadt" -> R.array.parktiger_p_r_heiligenstadt
            "Perchtoldsdorf Zone 1" -> R.array.perchtoldsdorf_zone_1
            "Perchtoldsdorf Zone 2" -> R.array.perchtoldsdorf_zone_2
            "Poertschach" -> R.array.poertschach
            "Ried Zone 1" -> R.array.klagenfurt_zone_1
            "Salzburg Zone 1" -> R.array.salzburg_zone_1
            "Schwechat Zone 1" -> R.array.schwechat_zone_1
            "Schärding Zone 1" -> R.array.schaerding_zone_1
            "Spittal" -> R.array.spittal
            "Sankt Pölten Zone 1" -> R.array.sankt_poelten_zone_1
            "St. Pölten Zone 2" -> R.array.sankt_poelten_zone_2
            "Steyr Zone 1" -> R.array.steyr_zone_1
            "Steyr Zone 2" -> R.array.steyr_zone_2
            "Steyr Zone 3" -> R.array.steyr_zone_3
            "Steyr Zone 4" -> R.array.steyr_zone_4
            "Steyr Zone 5" -> R.array.steyr_zone_5
            "Stockerau" -> R.array.stockerau
            "Tulln" -> R.array.tulln
            "Velden Zone 1" -> R.array.velden_zone_1
            "Velden Zone 2" -> R.array.velden_zone_2
            "Villach" -> R.array.villach
            "Weiz Zone A" -> R.array.weiz_zone_a
            "Weiz Zone B" -> R.array.weiz_zone_b
            "Wels" -> R.array.wels
            "Wien" -> R.array.wien
            "Wiener Neustadt Zone 1" -> R.array.wiener_neustadt_zone_1
            "Wiener Neustadt Zone 2" -> R.array.wiener_neustadt_zone_2
            "Wipark" -> R.array.wipark
            "Zell am See" -> R.array.zell_am_see
            else -> return emptyList()
        }
        return context.resources.getStringArray(arrayId).toList()
    }
}

fun getCrestForCity(city: String): Int {
    val baseName = when {
        city.startsWith("Baden") -> "crest_baden"
        city.startsWith("Bruck") -> "crest_bruck_an_der_leitha"
        city.startsWith("Eisenstadt") -> "crest_eisenstadt"
        city == "Gleisdorf" -> "crest_gleisdorf"
        city == "Gmunden" -> "crest_gmunden"
        city.startsWith("Graz") -> "crest_graz"
        city.startsWith("Hall") -> "crest_hall_in_tirol"
        city.startsWith("IIG") -> "crest_innsbruck"
        city.startsWith("Innsbruck") -> "crest_innsbruck"
        city.startsWith("Klagenfurt") -> "crest_klagenfurt"
        city.startsWith("Klosterneuburg") -> "crest_klosterneuburg"
        city == "Korneuburg" -> "crest_korneuburg"
        city.startsWith("Krems") -> "crest_krems"
        city.startsWith("Linz") -> "crest_linz"
        city.startsWith("Mödling") || city.startsWith("Moedling") -> "crest_moedling"
        city.startsWith("Neusiedl") -> "crest_neusiedl_am_see"
        city == "Oberwart" -> "crest_oberwart"
        city.startsWith("Parktiger") -> "crest_wien"
        city.startsWith("Perchtoldsdorf") -> "crest_perchtoldsdorf"
        city.startsWith("Poertschach") || city.startsWith("Pörtschach") -> "crest_poertschach"
        city.startsWith("Ried") -> "crest_ried"
        city.startsWith("Salzburg") -> "crest_salzburg"
        city.startsWith("Schwechat") -> "crest_schwechat"
        city.startsWith("Schärding") || city.startsWith("Schaerding") -> "crest_schaerding"
        city == "Spittal" -> "crest_spittal"
        city.startsWith("Sankt Pölten") || city.startsWith("St. Pölten") -> "crest_st_poelten"
        city.startsWith("Steyr") -> "crest_steyr"
        city == "Stockerau" -> "crest_stockerau"
        city == "Tulln" -> "crest_tulln"
        city.startsWith("Velden") -> "crest_velden"
        city == "Villach" -> "crest_villach"
        city.startsWith("Weiz") -> "crest_weiz"
        city == "Wels" -> "crest_wels"
        city == "Wien" -> "crest_wien"
        city.startsWith("Wiener Neustadt") -> "crest_wiener_neustadt"
        city == "Wipark" -> "crest_wien"
        city.startsWith("Zell") -> "crest_zell_am_see"
        else -> return 0
    }
    return try {
        R.drawable::class.java.getField(baseName).getInt(null)
    } catch (e: Exception) {
        0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    cities: List<String>,
    state: MainScreenState,
    onStartAlarm: (Int, Int, Int, Int, Int, String, String, Boolean) -> Unit,
    onStopAlarm: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToParkingplaces: () -> Unit,
    getDurationsForCity: (String) -> List<String>
) {
    val now = Calendar.getInstance()
    var selectedCity by remember { mutableStateOf(cities.firstOrNull() ?: "") }
    var durations by remember { mutableStateOf(getDurationsForCity(selectedCity)) }
    var selectedDuration by remember { mutableStateOf(durations.firstOrNull() ?: "") }
    var stopTimerEnabled by remember { mutableStateOf(false) }
    var intervalText by remember { mutableStateOf("1") }
    val interval = intervalText.toIntOrNull()?.coerceIn(0, 1440) ?: 0
    val stopTimerAllowed = selectedCity == "Wien"

    val startTimeState = rememberTimePickerState(
        initialHour = now.get(Calendar.HOUR_OF_DAY),
        initialMinute = now.get(Calendar.MINUTE),
        is24Hour = true
    )
    val stopTimeState = rememberTimePickerState(
        initialHour = now.get(Calendar.HOUR_OF_DAY),
        initialMinute = now.get(Calendar.MINUTE),
        is24Hour = true
    )

    var menuExpanded by remember { mutableStateOf(false) }
    val nextTicketInfo by state.nextParkingTicketText

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parking Ticket") },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = {
                                menuExpanded = false
                                onNavigateToSettings()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Parking places") },
                            onClick = {
                                menuExpanded = false
                                onNavigateToParkingplaces()
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Start Time
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Start Time",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TimeInput(state = startTimeState)
                }
            }

            // Stop Time
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Stop Time",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = stopTimerEnabled,
                            onCheckedChange = { stopTimerEnabled = it },
                            enabled = stopTimerAllowed
                        )
                    }
                    if (stopTimerEnabled && stopTimerAllowed) {
                        Spacer(modifier = Modifier.height(12.dp))
                        TimeInput(state = stopTimeState)
                    }
                    if (!stopTimerAllowed) {
                        Text(
                            "Available for Wien only",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // City & Duration
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Configuration",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // City dropdown
                    CityDropdown(
                        label = "City",
                        items = cities,
                        selectedItem = selectedCity,
                        onItemSelected = { city ->
                            selectedCity = city
                            durations = getDurationsForCity(city)
                            selectedDuration = durations.firstOrNull() ?: ""
                        },
                        crestResolver = ::getCrestForCity
                    )

                    // Duration dropdown
                    CityDropdown(
                        label = "Duration",
                        items = durations,
                        selectedItem = selectedDuration,
                        onItemSelected = { selectedDuration = it }
                    )

                    // Interval input
                    OutlinedTextField(
                        value = intervalText,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                intervalText = newValue
                            }
                        },
                        label = { Text("Interval (min)") },
                        supportingText = { Text("0 - 1440") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        nextTicketInfo.ifEmpty { "Your next booked\nparking ticket\nwill be shown here" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        onStartAlarm(
                            startTimeState.hour,
                            startTimeState.minute,
                            stopTimeState.hour,
                            stopTimeState.minute,
                            interval,
                            selectedCity,
                            selectedDuration,
                            stopTimerEnabled && stopTimerAllowed
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Start")
                }
                OutlinedButton(
                    onClick = { onStopAlarm(selectedCity) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Stop")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityDropdown(
    label: String,
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    crestResolver: ((String) -> Int)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedItem,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = if (crestResolver != null) {
                {
                    val resId = crestResolver(selectedItem)
                    if (resId != 0) {
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else null,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    leadingIcon = if (crestResolver != null) {
                        {
                            val resId = crestResolver(item)
                            if (resId != 0) {
                                Image(
                                    painter = painterResource(id = resId),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    } else null,
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
