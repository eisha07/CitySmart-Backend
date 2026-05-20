/// CitySmart - Mobile Systems Network Communication Layer
/// This service provides standalone networking routines wrapping Dart's native
/// [http] library. It strictly verifies server status codes and converts FastAPI
/// pipeline errors into robust, human-readable Dart exceptions.

import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/project_models.dart';

/// Standalone networking service for the CitySmart mobile gateway.
class ApiService {
  /// The loopback interface address for Android Virtual Devices (AVD).
  static const String baseUrl = 'http://10.0.2.2:8000';

  final http.Client _client;

  /// Constructor permitting injection of custom http client configurations (useful for testing).
  ApiService({http.Client? client}) : _client = client ?? http.Client();

  /// Strictly audits the [http.Response] meta-headers.
  /// If the request fails (outside 200-201 boundary), it parses the FastAPI
  /// error body to extract detail keys or falls back to a code-specific exception.
  void _auditResponse(http.Response response) {
    final int code = response.statusCode;
    if (code == 200 || code == 201) {
      return;
    }

    String errorMessage = 'Request failed with status code: $code';

    try {
      if (response.body.isNotEmpty) {
        final decoded = json.decode(response.body);
        if (decoded is Map<String, dynamic> && decoded.containsKey('detail')) {
          final detail = decoded['detail'];
          if (detail is String) {
            errorMessage = detail;
          } else if (detail is List) {
            // Join list items (common in FastAPI dynamic model validation schemas)
            errorMessage = detail.map((e) {
              if (e is Map && e.containsKey('msg')) {
                return e['msg'].toString();
              }
              return e.toString();
            }).join(', ');
          } else {
            errorMessage = detail.toString();
          }
        }
      }
    } catch (_) {
      // Non-JSON or unparseable response, retain fallback message
    }

    throw Exception(errorMessage);
  }

  /// 1. ingestProject
  /// Executes a POST transaction to `$baseUrl/api/v1/projects/ingest`
  /// with [prompt] nested inside an [UnstructuredIngestRequest] payload wrapper.
  Future<IngestResponse> ingestProject(String prompt) async {
    final Uri url = Uri.parse('$baseUrl/api/v1/projects/ingest');
    final Map<String, String> headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    };

    final String requestBody = json.encode(
      UnstructuredIngestRequest(userPrompt: prompt).toJson(),
    );

    final http.Response response = await _client.post(
      url,
      headers: headers,
      body: requestBody,
    );

    _auditResponse(response);

    final Map<String, dynamic> decodedJson = json.decode(response.body) as Map<String, dynamic>;
    return IngestResponse.fromJson(decodedJson);
  }

  /// 2. amendProject
  /// Executes a POST transaction to `$baseUrl/api/v1/projects/amend`
  /// passing down the variant branching configuration parameters.
  /// Returns [true] if the HTTP status returns in the success boundary (200/201).
  Future<bool> amendProject(AmendRequest request) async {
    final Uri url = Uri.parse('$baseUrl/api/v1/projects/amend');
    final Map<String, String> headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    };

    final String requestBody = json.encode(request.toJson());

    final http.Response response = await _client.post(
      url,
      headers: headers,
      body: requestBody,
    );

    _auditResponse(response);

    return response.statusCode == 200 || response.statusCode == 201;
  }

  /// 3. fetchSimulationState
  /// Executes an explicit GET network call to `$baseUrl/api/v1/simulation/$projectId/state`.
  Future<SimulationStateResponse> fetchSimulationState(String projectId) async {
    final Uri url = Uri.parse('$baseUrl/api/v1/simulation/$projectId/state');
    final Map<String, String> headers = {
      'Accept': 'application/json',
    };

    final http.Response response = await _client.get(
      url,
      headers: headers,
    );

    _auditResponse(response);

    final Map<String, dynamic> decodedJson = json.decode(response.body) as Map<String, dynamic>;
    return SimulationStateResponse.fromJson(decodedJson);
  }
}
