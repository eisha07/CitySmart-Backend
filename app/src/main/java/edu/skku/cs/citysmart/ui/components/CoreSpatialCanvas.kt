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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
 * UPDATED: Core Map Engine
 * - Now renders only ONE exact proposal pin derived from the prompt.
 * - Clusters all "Activated Agents" around that intervention zone.
 */
@Composable
fun CoreSpatialCanvas(
    telemetry: SpatialTelemetryCollection,
    agents: List<PersonaMetadata> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val isInspectionMode = LocalInspectionMode.current
    var searchQuery by remember { mutableStateOf("") }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(33.70, 73.05), 14.5f)
    }

    // Resolver for Agent Details
    fun resolveAgentDisplayInfo(feature: edu.skku.cs.citysmart.domain.GeoJsonFeature): Pair<String, String> {
        val props = feature.properties
        val detailedPersona = agents.find {
            it.name.equals(props.agentName, ignoreCase = true) ||
                    it.name.equals(props.agentId, ignoreCase = true) ||
                    props.agentProfile.lowercase().contains(it.name.lowercase())
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

    // Grouping Telemetry: Identify the ONE primary site and the Agentics
    val mapState = remember(telemetry) {
        val allSites = telemetry.features.filter {
            val p = it.properties.agentProfile.lowercase()
            !p.contains("commuter") && !p.contains("driver") && !p.contains("agent") &&
            !p.contains("shopkeeper") && !p.contains("resident") && !p.contains("friction")
        }

        // Find the ONE primary location (e.g., infrastructure node or first item)
        val primaryProposal = allSites.find {
            val p = it.properties.agentProfile.lowercase()
            p.contains("infrastructure") || p.contains("node") || p.contains("terminal")
        } ?: allSites.firstOrNull()

        val agentics = telemetry.features.filter {
            val p = it.properties.agentProfile.lowercase()
            p.contains("commuter") || p.contains("driver") || p.contains("agent") ||
            p.contains("shopkeeper") || p.contains("resident")
        }

        val frictions = telemetry.features.filter {
            it.properties.frictionIntensity > 1.3f || it.properties.agentProfile.lowercase().contains("friction")
        }

        Triple(primaryProposal, agentics, frictions)
    }

    // Center camera on the Primary Proposal Pin
    LaunchedEffect(mapState.first) {
        mapState.first?.let { feature ->
            getFirstCoord(feature.geometry.coordinates)?.let { newLocation ->
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(newLocation, 16.5f), 1500)
            }
        }
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

                // 1. RENDER ONLY THE ONE EXACT PROPOSAL PIN
                primarySite?.let { feature ->
                    val pinLocation = getFirstCoord(feature.geometry.coordinates)
                    if (pinLocation != null) {
                        Marker(
                            state = MarkerState(position = pinLocation),
                            title = "PROPOSAL: ${feature.properties.agentName?.uppercase() ?: "INTERVENTION NODE"}",
                            snippet = "The exact location identified from your prompt.",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED), // Red for the exact target
                            zIndex = 100f
                        )
                    }
                }

                // 2. RENDER THE ACTIVATED AGENTICS IN THE AREA
                agentics.forEach { feature ->
                    val profile = feature.properties.agentProfile.lowercase()
                    val (agentName, agentSnippet) = resolveAgentDisplayInfo(feature)

                    val pinHue = when {
                        profile.contains("commuter") -> BitmapDescriptorFactory.HUE_MAGENTA
                        profile.contains("driver") -> BitmapDescriptorFactory.HUE_CYAN
                        profile.contains("shopkeeper") -> BitmapDescriptorFactory.HUE_ORANGE
                        profile.contains("resident") -> BitmapDescriptorFactory.HUE_VIOLET
                        else -> BitmapDescriptorFactory.HUE_YELLOW
                    }

                    if (feature.geometry.type == "LineString") {
                        val points = try {
                            feature.geometry.coordinates.asJsonArray.map {
                                val p = it.asJsonArray
                                LatLng(p[1].asDouble, p[0].asDouble)
                            }
                        } catch (e: Exception) { emptyList() }

                        if (points.isNotEmpty()) {
                            Polyline(
                                points = points, 
                                color = if (profile.contains("commuter")) Color(0xFFFF00FF) else Color(0xFF00FFFF), 
                                width = 6f, 
                                zIndex = 5f
                            )
                            Marker(
                                state = MarkerState(position = points.first()),
                                title = agentName,
                                snippet = agentSnippet,
                                icon = BitmapDescriptorFactory.defaultMarker(pinHue)
                            )
                        }
                    } else {
                        val pinLocation = getFirstCoord(feature.geometry.coordinates)
                        if (pinLocation != null) {
                            Marker(
                                state = MarkerState(position = pinLocation),
                                title = agentName,
                                snippet = agentSnippet,
                                icon = BitmapDescriptorFactory.defaultMarker(pinHue),
                                zIndex = 15f
                            )
                        }
                    }
                }

                // 3. Render Friction Zones as subtle indicators
                frictions.forEach { feature ->
                    val pinLocation = getFirstCoord(feature.geometry.coordinates)
                    if (pinLocation != null) {
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

        // --- DYNAMIC TELEMETRY KEY (LEGEND) ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .background(Color(0xCC000000), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text("TELEMETRY STATUS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LegendItem(Color.Red, "EXACT PROPOSAL LOCATION")
            LegendItem(Color(0xFFFF00FF), "ACTIVATED AGENTICS")
            LegendItem(Color(0xFF00FFFF), "TRANSIT AGENTICS")
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
