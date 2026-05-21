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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import edu.skku.cs.citysmart.domain.GeoJsonFeature
import edu.skku.cs.citysmart.domain.GeometryData
import edu.skku.cs.citysmart.domain.SpatialTelemetryCollection
import edu.skku.cs.citysmart.domain.TelemetryProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CoreSpatialCanvas(
    telemetry: SpatialTelemetryCollection,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val isInspectionMode = LocalInspectionMode.current
    var searchQuery by remember { mutableStateOf("") }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(33.6844, 73.0479), 14.5f)
    }

    // --- AUTO-CENTER ON NEW TELEMETRY ---
    LaunchedEffect(telemetry) {
        if (telemetry.features.isNotEmpty()) {
            val firstFeature = telemetry.features.first()
            val coords = firstFeature.geometry.coordinates.firstOrNull()
            if (coords != null && coords.size >= 2) {
                val newLocation = LatLng(coords[1], coords[0])
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(newLocation, 15f),
                    1500
                )
            }
        }
    }

    val performSearch = {
        if (searchQuery.isNotBlank()) {
            focusManager.clearFocus()
            coroutineScope.launch {
                try {
                    val geocoder = Geocoder(context)
                    val addresses = withContext(Dispatchers.IO) {
                        @Suppress("DEPRECATION")
                        geocoder.getFromLocationName(searchQuery, 1)
                    }
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val targetLatLng = LatLng(address.latitude, address.longitude)
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(targetLatLng, 16f),
                            1000
                        )
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Search Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // --- DATA TRACKING FOR DYNAMIC LEGEND ---
    val activeProfiles = remember(telemetry) {
        val set = mutableSetOf<String>()
        telemetry.features.forEach { 
            if (it.properties.agentProfile == "proposed_infrastructure") {
                set.add("PROPOSED")
            } else if (it.properties.frictionIntensity > 1.3f) {
                set.add("FRICTION")
            } else {
                set.add(it.properties.agentProfile)
            }
        }
        set
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
                properties = MapProperties(
                    isTrafficEnabled = true,
                    mapStyleOptions = null 
                ),
                uiSettings = MapUiSettings(zoomControlsEnabled = false, compassEnabled = true)
            ) {
                telemetry.features.forEach { feature ->
                    val coordsData = feature.geometry.coordinates
                    val profile = feature.properties.agentProfile
                    
                    val color = when (profile) {
                        "proposed_infrastructure" -> Color.White
                        "female_commuter" -> Color(0xFFFF00FF)
                        "qingqi_driver" -> Color(0xFF00FFFF)
                        else -> if (feature.properties.frictionIntensity > 1.3f) Color.Red else Color(0xFF00FF00)
                    }

                    if (profile == "proposed_infrastructure" && feature.geometry.type == "LineString") {
                        val points = coordsData.map { LatLng(it[1], it[0]) }
                        Polyline(
                            points = points,
                            color = Color.White,
                            width = 12f,
                            pattern = listOf(com.google.android.gms.maps.model.Dash(20f), com.google.android.gms.maps.model.Gap(10f)),
                            zIndex = 10f
                        )
                        Polygon(
                            points = points,
                            fillColor = Color.White.copy(alpha = 0.15f),
                            strokeWidth = 0f
                        )
                    } 
                    else {
                        val pinLocation = if (feature.geometry.type == "Point" && coordsData.isNotEmpty()) {
                            LatLng(coordsData[0][1], coordsData[0][0])
                        } else if (feature.geometry.type == "LineString" && coordsData.isNotEmpty()) {
                            val mid = coordsData.size / 2
                            LatLng(coordsData[mid][1], coordsData[mid][0])
                        } else null

                        if (pinLocation != null) {
                            Circle(
                                center = pinLocation,
                                radius = (50f * feature.properties.frictionIntensity).toDouble(),
                                fillColor = color.copy(alpha = 0.25f),
                                strokeColor = color.copy(alpha = 0.6f),
                                strokeWidth = 3f
                            )

                            // UPDATED: Show Agent Name in Title on pin click
                            Marker(
                                state = MarkerState(position = pinLocation),
                                title = feature.properties.agentName ?: feature.properties.agentId,
                                snippet = "Profile: $profile",
                                icon = BitmapDescriptorFactory.defaultMarker(
                                    when (profile) {
                                        "female_commuter" -> BitmapDescriptorFactory.HUE_MAGENTA
                                        "qingqi_driver" -> BitmapDescriptorFactory.HUE_CYAN
                                        else -> if (feature.properties.frictionIntensity > 1.3f) BitmapDescriptorFactory.HUE_RED else BitmapDescriptorFactory.HUE_GREEN
                                    }
                                )
                            )
                        }
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

        // --- DYNAMIC LEGEND OVERLAY ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .background(Color(0xCC000000), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text("ACTIVE TELEMETRY KEY", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (activeProfiles.contains("PROPOSED")) {
                LegendItem(Color.White, "PROPOSED INFRASTRUCTURE")
            }
            if (activeProfiles.contains("FRICTION")) {
                LegendItem(Color.Red, "CRITICAL FRICTION ZONE")
            }
            if (activeProfiles.contains("female_commuter")) {
                LegendItem(Color(0xFFFF00FF), "VULNERABLE COMMUTER")
            }
            if (activeProfiles.contains("qingqi_driver")) {
                LegendItem(Color(0xFF00FFFF), "TRANSIT OPERATOR")
            }
            if (activeProfiles.isEmpty()) {
                Text("SCANNING FOR AGENTS...", color = Color.DarkGray, fontSize = 8.sp)
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = Color.LightGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
    }
}
