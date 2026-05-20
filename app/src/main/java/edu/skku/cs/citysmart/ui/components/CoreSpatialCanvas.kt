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
import androidx.compose.material.icons.filled.Close
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
import edu.skku.cs.citysmart.ui.theme.CitySmartTheme
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
                telemetry.features.forEach { feature ->
                    val coordsData = feature.geometry.coordinates
                    
                    val pathColor = when (feature.properties.agentProfile) {
                        "proposed_infrastructure" -> Color.White
                        "female_commuter" -> Color(0xFFFF00FF)
                        "qingqi_driver" -> Color(0xFF00FFFF)
                        else -> if (feature.properties.frictionIntensity > 1.3f) Color.Red else Color(0xFF00FF00)
                    }

                    if (feature.geometry.type == "LineString") {
                        val points = coordsData.map { LatLng(it[1], it[0]) }
                        if (points.isNotEmpty()) {
                            Polyline(
                                points = points,
                                color = pathColor,
                                width = if (feature.properties.agentProfile == "proposed_infrastructure") 25f else 18f,
                                geodesic = true,
                                zIndex = if (feature.properties.agentProfile == "proposed_infrastructure") 1f else 0f
                            )
                        }
                    }

                    if (feature.geometry.type == "Point" && coordsData.isNotEmpty()) {
                        val point = LatLng(coordsData[0][1], coordsData[0][0])
                        Marker(
                            state = MarkerState(position = point),
                            title = feature.properties.agentProfile,
                            icon = BitmapDescriptorFactory.defaultMarker(
                                when (feature.properties.agentProfile) {
                                    "female_commuter" -> BitmapDescriptorFactory.HUE_MAGENTA
                                    "qingqi_driver" -> BitmapDescriptorFactory.HUE_CYAN
                                    else -> BitmapDescriptorFactory.HUE_RED
                                }
                            )
                        )
                    }

                    if (feature.properties.frictionIntensity > 1.0f) {
                        coordsData.forEach {
                            Circle(
                                center = LatLng(it[1], it[0]),
                                radius = (40f * feature.properties.frictionIntensity).toDouble(),
                                fillColor = pathColor.copy(alpha = 0.3f),
                                strokeColor = pathColor,
                                strokeWidth = 2f
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

        // --- LEGEND OVERLAY ---
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp).background(Color(0xCC000000), RoundedCornerShape(8.dp)).padding(12.dp)
        ) {
            Text("TELEMETRY KEY", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LegendItem(Color.White, "PROPOSED STRUCTURE / BLUEPRINT")
            LegendItem(Color.Red, "HIGH FRICTION / BOTTLENECK")
            LegendItem(Color(0xFFFF00FF), "VULNERABLE COMMUTER")
            LegendItem(Color(0xFF00FFFF), "TRANSIT OPERATOR")
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
