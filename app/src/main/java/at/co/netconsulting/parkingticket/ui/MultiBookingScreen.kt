package at.co.netconsulting.parkingticket.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import at.co.netconsulting.parkingticket.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ActiveParkingBooking(
    val alarmRequestCode: Int,
    val licensePlate: String,
    val city: String,
    val nextTicketTimeMillis: Long,
    val remainingTickets: Int
)

data class ParkingBookingRequest(
    val licensePlate: String,
    val startHour: Int,
    val startMinute: Int,
    val stopHour: Int,
    val stopMinute: Int,
    val intervalMinutes: Int,
    val city: String,
    val duration: String,
    val stopEnabled: Boolean
)

private data class ParkingBookingDraft(
    val id: Long,
    val licensePlate: String,
    val startHour: Int,
    val startMinute: Int,
    val stopHour: Int,
    val stopMinute: Int,
    val intervalText: String,
    val city: String,
    val duration: String,
    val stopEnabled: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    cities: List<String>,
    defaultLicensePlate: String,
    state: MainScreenState,
    onStartAlarms: (List<ParkingBookingRequest>) -> Unit,
    onStopBooking: (ActiveParkingBooking) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToParkingplaces: () -> Unit,
    onSaveCarPosition: () -> Unit,
    onNavigateToParkedCar: () -> Unit,
    getDurationsForCity: (String) -> List<String>
) {
    val now = Calendar.getInstance()
    val initialCity = cities.firstOrNull() ?: ""
    val initialDuration = getDurationsForCity(initialCity).firstOrNull() ?: ""
    val bookings = remember {
        mutableStateListOf(
            ParkingBookingDraft(
                1L, defaultLicensePlate,
                now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE),
                now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE),
                "1", initialCity, initialDuration, false
            )
        )
    }
    var nextBookingId by remember { mutableLongStateOf(2L) }
    var bookingsToConfirm by remember {
        mutableStateOf<List<ParkingBookingRequest>>(emptyList())
    }
    var bookingsToStop by remember {
        mutableStateOf<List<ActiveParkingBooking>>(emptyList())
    }
    var validationMessage by remember { mutableStateOf<String?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    val nextTicketInfo by state.nextParkingTicketText
    val carPositionSaved by state.carPositionSaved
    val activeBookings by state.activeBookings

    Scaffold(
        topBar = {
            MultiBookingTopBar(
                menuExpanded = menuExpanded,
                carPositionSaved = carPositionSaved,
                onMenuChanged = { menuExpanded = it },
                onSettings = onNavigateToSettings,
                onParkingplaces = onNavigateToParkingplaces,
                onParkedCar = onNavigateToParkedCar
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
            Text("Configure every license plate separately. Add as many entries as needed.")
            bookings.forEachIndexed { index, draft ->
                key(draft.id) {
                    BookingEditor(
                        index + 1, draft, bookings.size > 1, cities,
                        getDurationsForCity,
                        { changed ->
                            val i = bookings.indexOfFirst { it.id == changed.id }
                            if (i >= 0) bookings[i] = changed
                            validationMessage = null
                        },
                        {
                            bookings.removeAll { it.id == draft.id }
                            validationMessage = null
                        }
                    )
                }
            }
            OutlinedButton(
                onClick = {
                    val current = Calendar.getInstance()
                    val city = cities.firstOrNull() ?: ""
                    bookings.add(
                        ParkingBookingDraft(
                            nextBookingId++, defaultLicensePlate,
                            current.get(Calendar.HOUR_OF_DAY), current.get(Calendar.MINUTE),
                            current.get(Calendar.HOUR_OF_DAY), current.get(Calendar.MINUTE),
                            "1", city,
                            getDurationsForCity(city).firstOrNull() ?: "", false
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add another license plate")
            }
            validationMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        stringResource(R.string.parked_car_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(onClick = onSaveCarPosition, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.save_car_position))
                        }
                        OutlinedButton(
                            onClick = onNavigateToParkedCar,
                            enabled = carPositionSaved,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.show_route_to_car))
                        }
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    nextTicketInfo.ifEmpty {
                        "Your next booked\nparking ticket\nwill be shown here"
                    },
                    modifier = Modifier.padding(16.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        val invalidPlate = bookings.indexOfFirst { it.licensePlate.isBlank() }
                        val invalidInterval = bookings.indexOfFirst {
                            it.intervalText.toIntOrNull() !in 0..1440
                        }
                        val invalidConfig = bookings.indexOfFirst {
                            it.city.isBlank() || it.duration.toIntOrNull() == null
                        }
                        validationMessage = when {
                            invalidPlate >= 0 ->
                                "License plate ${invalidPlate + 1} must not be empty."
                            invalidInterval >= 0 ->
                                "Interval for plate ${invalidInterval + 1} must be 0 to 1440."
                            invalidConfig >= 0 ->
                                "City and duration for plate ${invalidConfig + 1} are required."
                            else -> null
                        }
                        if (validationMessage == null) {
                            bookingsToConfirm = bookings.map { it.toRequest() }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Review & start")
                }
                OutlinedButton(
                    onClick = {
                        if (activeBookings.isEmpty()) {
                            validationMessage = "There are no active bookings to stop."
                        } else {
                            validationMessage = null
                            bookingsToStop = activeBookings
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Stop")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (bookingsToConfirm.isNotEmpty()) {
        BookingConfirmationDialog(
            bookings = bookingsToConfirm,
            onDismiss = { bookingsToConfirm = emptyList() },
            onConfirm = {
                val confirmed = bookingsToConfirm
                bookingsToConfirm = emptyList()
                onStartAlarms(confirmed)
            }
        )
    }

    if (bookingsToStop.isNotEmpty()) {
        StopBookingSelectionDialog(
            bookings = bookingsToStop,
            onDismiss = { bookingsToStop = emptyList() },
            onStop = { booking ->
                bookingsToStop = emptyList()
                onStopBooking(booking)
            }
        )
    }
}

private fun ParkingBookingDraft.toRequest() = ParkingBookingRequest(
    licensePlate.trim(), startHour, startMinute, stopHour, stopMinute,
    intervalText.toInt(), city, duration, stopEnabled && city == "Wien"
)

@Composable
private fun StopBookingSelectionDialog(
    bookings: List<ActiveParkingBooking>,
    onDismiss: () -> Unit,
    onStop: (ActiveParkingBooking) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Stop parking booking") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Choose the booking that should be stopped.")
                bookings.forEachIndexed { index, booking ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "${index + 1}. ${booking.licensePlate}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(booking.city)
                            Text("Next ticket ${booking.nextTicketTimeMillis.formatDateTime()}")
                            Text("Remaining tickets ${booking.remainingTickets}")
                            Button(
                                onClick = { onStop(booking) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Stop this booking")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Back") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MultiBookingTopBar(
    menuExpanded: Boolean,
    carPositionSaved: Boolean,
    onMenuChanged: (Boolean) -> Unit,
    onSettings: () -> Unit,
    onParkingplaces: () -> Unit,
    onParkedCar: () -> Unit
) {
    TopAppBar(
        title = { Text("Parking Ticket") },
        actions = {
            IconButton(onClick = { onMenuChanged(true) }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { onMenuChanged(false) }
            ) {
                DropdownMenuItem(
                    text = { Text("Settings") },
                    onClick = { onMenuChanged(false); onSettings() }
                )
                DropdownMenuItem(
                    text = { Text("Parking places") },
                    onClick = { onMenuChanged(false); onParkingplaces() }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.parked_car_title)) },
                    enabled = carPositionSaved,
                    onClick = { onMenuChanged(false); onParkedCar() }
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingEditor(
    number: Int,
    draft: ParkingBookingDraft,
    canRemove: Boolean,
    cities: List<String>,
    getDurationsForCity: (String) -> List<String>,
    onDraftChanged: (ParkingBookingDraft) -> Unit,
    onRemove: () -> Unit
) {
    val durations = getDurationsForCity(draft.city)
    val stopAllowed = draft.city == "Wien"
    val startState = rememberTimePickerState(
        draft.startHour, draft.startMinute, true
    )
    val stopState = rememberTimePickerState(
        draft.stopHour, draft.stopMinute, true
    )
    LaunchedEffect(startState.hour, startState.minute) {
        if (startState.hour != draft.startHour ||
            startState.minute != draft.startMinute
        ) {
            onDraftChanged(
                draft.copy(
                    startHour = startState.hour,
                    startMinute = startState.minute
                )
            )
        }
    }
    LaunchedEffect(stopState.hour, stopState.minute) {
        if (stopState.hour != draft.stopHour ||
            stopState.minute != draft.stopMinute
        ) {
            onDraftChanged(
                draft.copy(
                    stopHour = stopState.hour,
                    stopMinute = stopState.minute
                )
            )
        }
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "License plate $number",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (canRemove) {
                    TextButton(onClick = onRemove) { Text("Remove") }
                }
            }
            OutlinedTextField(
                value = draft.licensePlate,
                onValueChange = {
                    onDraftChanged(draft.copy(licensePlate = it))
                },
                label = { Text("License plate") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text("Start time", style = MaterialTheme.typography.labelLarge)
            TimeInput(state = startState)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Stop time",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = draft.stopEnabled && stopAllowed,
                    onCheckedChange = {
                        onDraftChanged(draft.copy(stopEnabled = it))
                    },
                    enabled = stopAllowed
                )
            }
            if (draft.stopEnabled && stopAllowed) {
                TimeInput(state = stopState)
            } else if (!stopAllowed) {
                Text(
                    "Stop time is available for Wien only",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            CityDropdown(
                label = "City",
                items = cities,
                selectedItem = draft.city,
                onItemSelected = { city ->
                    onDraftChanged(
                        draft.copy(
                            city = city,
                            duration = getDurationsForCity(city).firstOrNull() ?: "",
                            stopEnabled = draft.stopEnabled && city == "Wien"
                        )
                    )
                },
                crestResolver = ::getCrestForCity
            )
            CityDropdown(
                label = "Duration",
                items = durations,
                selectedItem = draft.duration,
                onItemSelected = {
                    onDraftChanged(draft.copy(duration = it))
                }
            )
            OutlinedTextField(
                value = draft.intervalText,
                onValueChange = { value ->
                    if (value.isEmpty() || value.all(Char::isDigit)) {
                        onDraftChanged(draft.copy(intervalText = value))
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
}

@Composable
private fun BookingConfirmationDialog(
    bookings: List<ParkingBookingRequest>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Review ${bookings.size} parking ticket" +
                    if (bookings.size == 1) "" else "s"
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                bookings.forEachIndexed { index, booking ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "${index + 1}. ${booking.licensePlate}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text("${booking.city} · ${booking.duration} min")
                            Text(
                                "Starts ${booking.startHour.twoDigits()}:" +
                                    booking.startMinute.twoDigits()
                            )
                            if (booking.stopEnabled) {
                                Text(
                                    "Stops ${booking.stopHour.twoDigits()}:" +
                                        booking.stopMinute.twoDigits()
                                )
                            }
                            Text("Interval ${booking.intervalMinutes} min")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Confirm & start") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Back") }
        }
    )
}

private fun Int.twoDigits(): String = toString().padStart(2, '0')

private fun Long.formatDateTime(): String =
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(this))
