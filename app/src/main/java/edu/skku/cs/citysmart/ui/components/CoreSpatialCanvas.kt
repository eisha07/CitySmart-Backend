package edu.skku.cs.citysmart.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import edu.skku.cs.citysmart.domain.GeoJsonFeature
import edu.skku.cs.citysmart.domain.GeometryData
import edu.skku.cs.citysmart.domain.SpatialTelemetryCollection
import edu.skku.cs.citysmart.domain.TelemetryProperties
import edu.skku.cs.citysmart.ui.theme.CitySmartTheme

@Composable
fun CoreSpatialCanvas(
    telemetry: SpatialTelemetryCollection,
    modifier: Modifier = Modifier
) {
    val isInspectionMode = LocalInspectionMode.current
    val defaultLocation = remember(telemetry) {
        val firstFeature = telemetry.features.firstOrNull()
        val coords = firstFeature?.geometry?.coordinates?.firstOrNull()
        if (coords != null && coords.size >= 2) {
            LatLng(coords[1], coords[0])
        } else {
            LatLng(33.6844, 73.0479)
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 14.5f)
    }

    val mapProperties = remember {
        MapProperties(
            mapType = MapType.NORMAL,
            isBuildingEnabled = true,
            isTrafficEnabled = false
        )
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = true
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .background(Color(0xFF0A0A0A), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "LIVE GEOGRAPHIC TELEMETRY & POLICY FRICTION MAP",
            color = Color.Gray,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isInspectionMode) {
                // Placeholder for Preview mode
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "MAP ENGINE ACTIVE\n(Live tiles only on Device)",
                        color = Color.DarkGray,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    // Simple visualization of telemetry for the preview
                    Canvas(modifier = Modifier.size(100.dp)) {
                        drawCircle(color = Color(0xFFFF3D00).copy(alpha = 0.3f), radius = 40f)
                        drawLine(color = Color(0xFF00E5FF), start = Offset(0f, 50f), end = Offset(100f, 50f), strokeWidth = 5f)
                    }
                }
            } else {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = uiSettings
                ) {
                    telemetry.features.forEach { feature ->
                        val coordinates = feature.geometry.coordinates.map { LatLng(it[1], it[0]) }
                        
                        val pathColor = when (feature.properties.agentProfile) {
                            "female_commuter" -> Color(0xFFE91E63) 
                            "qingqi_driver" -> Color(0xFF00E5FF)   
                            else -> Color.White
                        }

                        if (coordinates.isNotEmpty() && feature.geometry.type == "LineString") {
                            Polyline(
                                points = coordinates,
                                color = pathColor,
                                width = 12f,
                                geodesic = true
                            )
                        }

                        if (feature.properties.frictionIntensity > 1.0f) {
                            coordinates.forEach { point ->
                                Circle(
                                    center = point,
                                    radius = (40f * feature.properties.frictionIntensity).toDouble(),
                                    fillColor = Color(0xFFFF3D00).copy(alpha = 0.35f),
                                    strokeColor = Color(0xFFFF3D00),
                                    strokeWidth = 3f
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CoreSpatialCanvasPreview() {
    val sampleTelemetry = SpatialTelemetryCollection(
        features = listOf(
            GeoJsonFeature(
                geometry = GeometryData(
                    type = "LineString",
                    coordinates = listOf(
                        listOf(73.0479, 33.6844),
                        listOf(73.0579, 33.6944)
                    )
                ),
                properties = TelemetryProperties(
                    agentProfile = "female_commuter",
                    frictionIntensity = 1.5f
                )
            ),
            GeoJsonFeature(
                geometry = GeometryData(
                    type = "LineString",
                    coordinates = listOf(
                        listOf(73.0379, 33.6744),
                        listOf(73.0479, 33.6844)
                    )
                ),
                properties = TelemetryProperties(
                    agentProfile = "qingqi_driver",
                    frictionIntensity = 0.5f
                )
            )
        )
    )
    CitySmartTheme {
        CoreSpatialCanvas(telemetry = sampleTelemetry)
    }
}
