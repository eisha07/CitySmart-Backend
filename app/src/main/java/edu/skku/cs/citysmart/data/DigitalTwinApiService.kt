package edu.skku.cs.citysmart.data

import edu.skku.cs.citysmart.domain.UrbanSimulationState
import edu.skku.cs.citysmart.domain.SpatialTelemetryCollection
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DigitalTwinApiService {

    /**
     * Step 12: Fetch live spatial trajectories and friction factor geo-coordinates
     * Endpoint: GET /api/v1/simulation/{projectId}/telemetry
     */
    @GET("api/v1/simulation/{projectId}/telemetry")
    suspend fun getSpatialTelemetry(
        @Path("projectId") projectId: String
    ): SpatialTelemetryCollection

    /**
     * Steps 13, 14, & 15: Fetch the overarching Master Simulation State
     * This brings down the live debate ticks, blueprint revisions, scores, and arbitrator verdict terminal string.
     * Endpoint: GET /api/v1/simulation/{projectId}/state
     */
    @GET("api/v1/simulation/{projectId}/state")
    suspend fun getMasterSimulationState(
        @Path("projectId") projectId: String
    ): UrbanSimulationState
}