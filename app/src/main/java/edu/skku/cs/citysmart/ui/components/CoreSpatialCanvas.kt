package edu.skku.cs.citysmart.ui.components

import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.gson.JsonElement
import edu.skku.cs.citysmart.domain.PersonaMetadata
import edu.skku.cs.citysmart.domain.SpatialTelemetryCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UPDATED: Core Spatial Canvas
 * - Features a robust Geocoding Engine that prioritizes live telemetry data.
 * - Falls back to prompt-based location detection if data is pending.
 * - Renders one exact proposal pin and clusters activated agents.
 */
@Composable
fun CoreSpatialCanvas(
    telemetry: SpatialTelemetryCollection,
    agents: List<PersonaMetadata> = emptyList(),
    userPrompt: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val isInspectionMode = LocalInspectionMode.current
    var searchQuery by remember { mutableStateOf("") }
    
    // Track if we have successfully snapped to the REAL data location
    var hasAutoCentered by remember { mutableStateOf(false) }

    // Helper to pull coordinates out safely
    fun getFirstCoord(coordinates: JsonElement): LatLng? {
        return try {
            val arr = coordinates.asJsonArray
            if (arr.size() == 0) return null
            if (arr[0].isJsonPrimitive) LatLng(arr[1].asDouble, arr[0].asDouble)
            else {
                val firstPoint = arr[0].asJsonArray
                LatLng(firstPoint[1].asDouble, firstPoint[0].asDouble)
            }
        } catch (e: Exception) { null }
    }

    // DYNAMIC POSITIONING: Calculate a fallback center based on the data
    val dynamicInitialCenter = remember(telemetry) {
        val firstFeatureWithCoords = telemetry.features.firstOrNull {
            getFirstCoord(it.geometry.coordinates) != null
        }
        firstFeatureWithCoords?.let { getFirstCoord(it.geometry.coordinates) } ?: LatLng(
            33.6844,
            73.0479
        ) // Smart default to Islamabad if empty
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(dynamicInitialCenter, 15.0f)
    }

    // SPATIAL PARSER: Filter assets purely based on data layout properties
    val mapState = remember(telemetry) {
        val allSites = telemetry.features.filter {
            val p = it.properties.agentProfile.lowercase()
            !p.contains("commuter") && !p.contains("driver") && !p.contains("agent") &&
            !p.contains("shopkeeper") && !p.contains("resident") && !p.contains("friction")
        }

        val primaryProposal = allSites.find {
            val p = it.properties.agentProfile.lowercase()
            p.contains("infrastructure") || p.contains("node") || p.contains("terminal")
        } ?: allSites.firstOrNull()

        val agentics = telemetry.features.filter { it != primaryProposal }
        val frictions = telemetry.features.filter {
            it.properties.frictionIntensity > 1.3f || it.properties.agentProfile.lowercase().contains("friction")
        }

        Triple(primaryProposal, agentics, frictions)
    }

    // ROBUST CENTERING LOGIC
    LaunchedEffect(telemetry.features.size, userPrompt) {
        val primaryPin = mapState.first?.let { getFirstCoord(it.geometry.coordinates) }

        if (primaryPin != null && !hasAutoCentered) {
            // SCENARIO 1: Live telemetry data has arrived.
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(primaryPin, 16.5f), 1500)
            hasAutoCentered = true
        } else if (!hasAutoCentered && userPrompt.isNotBlank()) {
            // SCENARIO 2: Data hasn't arrived yet, try geocoding the prompt.
            coroutineScope.launch {
                try {
                    val geocoder = Geocoder(context)
                    val searchString = if (userPrompt.contains(" in ", ignoreCase = true)) {
                        userPrompt.substringAfterLast(" in ", userPrompt)
                    } else {
                        userPrompt.split(" ").take(8).joinToString(" ")
                    }
                    val addresses = withContext(Dispatchers.IO) {
                        geocoder.getFromLocationName(searchString, 1)
                    }
                    if (!addresses.isNullOrEmpty()) {
                        val target = LatLng(addresses[0].latitude, addresses[0].longitude)
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(target, 16.0f), 2000)
                        hasAutoCentered = true
                    }
                } catch (e: Exception) { }
            }
        }
    }

    fun resolveAgentDisplayInfo(feature: edu.skku.cs.citysmart.domain.GeoJsonFeature): Pair<String, String> {
        val props = feature.properties
        val detailedPersona = agents.find {
            it.name.equals(props.agentName, ignoreCase = true) ||
                    it.name.equals(props.agentId, ignoreCase = true)
        }
        val displayName = detailedPersona?.name ?: props.agentName ?: props.agentId.ifBlank { "Simulation Agent" }
        val displaySnippet = buildString {
            val demographics = detailedPersona?.demographics ?: props.demographics
            if (!demographics.isNullOrBlank()) append("$demographics | ")
            val bio = detailedPersona?.shortDescription?.split(".")?.firstOrNull() ?:
            props.agentRole ?: props.agentProfile.replace("_", " ").uppercase()
            append(bio)
        }
        return displayName to displaySnippet
    }

    val performSearch = {
        if (searchQuery.isNotBlank()) {
            focusManager.clearFocus()
            coroutineScope.launch {
                try {
                    val geocoder = Geocoder(context)
                    val addresses = withContext(Dispatchers.IO) { geocoder.getFromLocationName(searchQuery, 1) }
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(address.latitude, address.longitude), 17f), 1000)
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Search Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isInspectionMode) {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212)), contentAlignment = Alignment.Center) {
                Text("MAP ENGINE ACTIVE", color = Color.Gray)
            }
        } else {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isTrafficEnabled = true),
                uiSettings = MapUiSettings(zoomControlsEnabled = false, compassEnabled = true)
            ) {
                val (primarySite, agentics, frictions) = mapState

                // 1. RENDER PRIMARY PROPOSAL PIN
                primarySite?.let { feature ->
                    val pinLocation = getFirstCoord(feature.geometry.coordinates)
                    if (pinLocation != null) {
                        Marker(
                            state = MarkerState(position = pinLocation),
                            title = "PROPOSAL: ${feature.properties.agentName?.uppercase() ?: "INTERVENTION NODE"}",
                            snippet = "Exact site identified from simulation.",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                            zIndex = 100f
                        )
                    }
                }

                // 2. RENDER ACTIVATED AGENTS
                agentics.forEach { feature ->
                    val profile = feature.properties.agentProfile.lowercase()
                    val (agentName, agentSnippet) = resolveAgentDisplayInfo(feature)

                    val pinHue = when {
                        profile.contains("commuter") -> BitmapDescriptorFactory.HUE_MAGENTA
                        profile.contains("driver") -> BitmapDescriptorFactory.HUE_CYAN
                        profile.contains("shopkeeper") -> BitmapDescriptorFactory.HUE_ORANGE
                        else -> BitmapDescriptorFactory.HUE_YELLOW
                    }

                    getFirstCoord(feature.geometry.coordinates)?.let { pinLocation ->
                        Marker(
                            state = MarkerState(position = pinLocation),
                            title = agentName,
                            snippet = agentSnippet,
                            icon = BitmapDescriptorFactory.defaultMarker(pinHue),
                            zIndex = 15f
                        )
                    }
                }

                // 3. RENDER FRICTION ZONES
                frictions.forEach { feature ->
                    getFirstCoord(feature.geometry.coordinates)?.let { pinLocation ->
                        Circle(
                            center = pinLocation,
                            radius = (40f * feature.properties.frictionIntensity).toDouble(),
                            fillColor = Color.Red.copy(alpha = 0.15f),
                            strokeColor = Color.Red.copy(alpha = 0.4f),
                            strokeWidth = 2f
                        )
                    }
                }
            }
        }

        // --- SEARCH OVERLAY ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(16.dp).background(Color(0xCC0A0A0A), RoundedCornerShape(12.dp)),
            placeholder = { Text("Search location...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { performSearch() }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF00E676))
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { performSearch() }),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E676), unfocusedBorderColor = Color.Transparent, focusedTextColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // --- DYNAMIC LEGEND ---
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp).background(Color(0xCC000000), RoundedCornerShape(8.dp)).padding(12.dp)
        ) {
            Text("TELEMETRY STATUS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LegendItem(Color.Red, "EXACT PROPOSAL LOCATION")
            LegendItem(Color(0xFFFF00FF), "ACTIVATED AGENTS")
            LegendItem(Color(0xFF00FFFF), "TRANSIT ASSETS")
            LegendItem(Color.Red.copy(alpha = 0.4f), "CONFLICT ZONES")
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label.uppercase(), color = Color.LightGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
    }
}
