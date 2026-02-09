package at.co.netconsulting.parkingticket.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import at.co.netconsulting.parkingticket.SettingsActivity
import at.co.netconsulting.parkingticket.general.StaticFields

class SettingsScreenState(context: Context) {
    private val ctx = context

    var telephoneNumber = mutableStateOf(loadString(StaticFields.TELEPHONE_NUMBER, StaticFields.DEFAULT_TELEPHONE_NUMBER))
    var licensePlate = mutableStateOf(loadString(StaticFields.LICENSE_PLATE, StaticFields.DEFAULT_NUMBER_PLATE))
    var waitMinutes = mutableStateOf(loadInt(StaticFields.WAIT_MINUTES, 0).toString())
    var alternateBooking = mutableStateOf(loadString(StaticFields.ALTERNATE_BOOKING, StaticFields.NO_ALTERNATE_BOOKING))
    var showAlertDialog = mutableStateOf(loadString(StaticFields.ALERT_DIALOG, StaticFields.DIALOG_YES))
    var voiceMessageExpired = mutableStateOf(loadBoolean(StaticFields.VOICE_MESSAGE_PARKING_TICKET_EXPIRED, false))

    private fun loadString(key: String, default: String): String {
        val sh = ctx.getSharedPreferences(key, Context.MODE_PRIVATE)
        return sh.getString(key, default) ?: default
    }

    private fun loadInt(key: String, default: Int): Int {
        val sh = ctx.getSharedPreferences(key, Context.MODE_PRIVATE)
        return sh.getInt(key, default)
    }

    private fun loadBoolean(key: String, default: Boolean): Boolean {
        val sh = ctx.getSharedPreferences(key, Context.MODE_PRIVATE)
        return sh.getBoolean(key, default)
    }

    fun saveAll() {
        saveString(StaticFields.TELEPHONE_NUMBER, telephoneNumber.value)
        saveString(StaticFields.LICENSE_PLATE, licensePlate.value)
        val minutes = waitMinutes.value.toIntOrNull() ?: 0
        saveInt(StaticFields.WAIT_MINUTES, minutes)
    }

    fun saveAlternateBooking(value: String) {
        alternateBooking.value = value
        saveString(StaticFields.ALTERNATE_BOOKING, value)
    }

    fun saveShowAlertDialog(value: String) {
        showAlertDialog.value = value
        saveString(StaticFields.ALERT_DIALOG, value)
    }

    fun saveVoiceMessageExpired(value: Boolean) {
        voiceMessageExpired.value = value
        saveBoolean(StaticFields.VOICE_MESSAGE_PARKING_TICKET_EXPIRED, value)
    }

    private fun saveString(key: String, value: String) {
        ctx.getSharedPreferences(key, Context.MODE_PRIVATE).edit().putString(key, value).commit()
    }

    private fun saveInt(key: String, value: Int) {
        ctx.getSharedPreferences(key, Context.MODE_PRIVATE).edit().putInt(key, value).commit()
    }

    private fun saveBoolean(key: String, value: Boolean) {
        ctx.getSharedPreferences(key, Context.MODE_PRIVATE).edit().putBoolean(key, value).commit()
    }
}

object SettingsScreenSetup {
    @JvmStatic
    fun init(composeView: ComposeView, activity: SettingsActivity) {
        val state = SettingsScreenState(activity)

        composeView.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                SettingsScreen(
                    state = state,
                    onBackPressed = { activity.finish() },
                    onNavigateToMain = {
                        activity.startActivity(
                            android.content.Intent(activity, at.co.netconsulting.parkingticket.MainActivity::class.java)
                        )
                    },
                    onNavigateToParkingplaces = {
                        activity.startActivity(
                            android.content.Intent(activity, at.co.netconsulting.parkingticket.Parkingplace::class.java)
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsScreenState,
    onBackPressed: () -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToParkingplaces: () -> Unit
) {
    var telephoneNumber by state.telephoneNumber
    var licensePlate by state.licensePlate
    var waitMinutes by state.waitMinutes
    val alternateBooking by state.alternateBooking
    val showAlertDialog by state.showAlertDialog
    var voiceMessageExpired by state.voiceMessageExpired

    var telephoneError by remember { mutableStateOf<String?>(null) }
    var licensePlateError by remember { mutableStateOf<String?>(null) }
    var waitMinutesError by remember { mutableStateOf<String?>(null) }
    var showSavedSnackbar by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showSavedSnackbar) {
        if (showSavedSnackbar) {
            snackbarHostState.showSnackbar("Settings saved")
            showSavedSnackbar = false
        }
    }

    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Main") },
                            onClick = { menuExpanded = false; onNavigateToMain() }
                        )
                        DropdownMenuItem(
                            text = { Text("Parking places") },
                            onClick = { menuExpanded = false; onNavigateToParkingplaces() }
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Phone & License plate
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Account", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = telephoneNumber,
                        onValueChange = {
                            telephoneNumber = it
                            telephoneError = null
                        },
                        label = { Text("Telephone number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = telephoneError != null,
                        supportingText = telephoneError?.let { { Text(it) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = licensePlate,
                        onValueChange = {
                            licensePlate = it
                            licensePlateError = null
                        },
                        label = { Text("License plate") },
                        isError = licensePlateError != null,
                        supportingText = licensePlateError?.let { { Text(it) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Voice message settings
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Voice messages", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = waitMinutes,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                waitMinutes = newValue
                                val num = newValue.toIntOrNull()
                                waitMinutesError = if (num != null && (num < 0 || num > 5)) {
                                    "Please set between 0 and 5"
                                } else null
                            }
                        },
                        label = { Text("Wait minutes for reminder") },
                        supportingText = {
                            Text(waitMinutesError ?: "0 = no voice message, 5 = max wait")
                        },
                        isError = waitMinutesError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Voice message if parking ticket expired",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = voiceMessageExpired,
                            onCheckedChange = { state.saveVoiceMessageExpired(it) }
                        )
                    }
                }
            }

            // Alternate booking
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Alternate booking", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))

                    Column(Modifier.selectableGroup()) {
                        RadioOption(
                            text = "15/30",
                            selected = alternateBooking == StaticFields.FIFTEEN_THIRTY,
                            onClick = { state.saveAlternateBooking(StaticFields.FIFTEEN_THIRTY) }
                        )
                        RadioOption(
                            text = "30/15",
                            selected = alternateBooking == StaticFields.THIRTY_FIFTEEN,
                            onClick = { state.saveAlternateBooking(StaticFields.THIRTY_FIFTEEN) }
                        )
                        RadioOption(
                            text = "No alternate booking",
                            selected = alternateBooking == StaticFields.NO_ALTERNATE_BOOKING,
                            onClick = { state.saveAlternateBooking(StaticFields.NO_ALTERNATE_BOOKING) }
                        )
                    }
                }
            }

            // Show bookings overview
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Show bookings overview", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))

                    Column(Modifier.selectableGroup()) {
                        RadioOption(
                            text = "Yes",
                            selected = showAlertDialog == StaticFields.DIALOG_YES,
                            onClick = { state.saveShowAlertDialog(StaticFields.DIALOG_YES) }
                        )
                        RadioOption(
                            text = "No",
                            selected = showAlertDialog == StaticFields.DIALOG_NO,
                            onClick = { state.saveShowAlertDialog(StaticFields.DIALOG_NO) }
                        )
                    }
                }
            }

            // Save button
            Button(
                onClick = {
                    // Validate
                    val phoneEmpty = telephoneNumber.trim().isEmpty()
                    val plateEmpty = licensePlate.trim().isEmpty()

                    if (phoneEmpty) telephoneError = "Telephone number must not be empty"
                    if (plateEmpty) licensePlateError = "License plate must not be empty"

                    if (!phoneEmpty && !plateEmpty && waitMinutesError == null) {
                        state.saveAll()
                        showSavedSnackbar = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RadioOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
