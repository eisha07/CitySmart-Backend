/// CitySmart - Mobile Systems State Management Engine
/// This provider serves as the single source of truth for the CitySmart
/// Flutter client. It coordinates ingestion, branching amendments, and
/// orchestrates the multi-agent town hall simulation narrative feed.

import 'dart:async';
import 'package:flutter/foundation.dart';
import '../models/project_models.dart';
import '../services/api_service.dart';

/// State tracking enumeration representing the progress of the multi-agent debate.
enum SimulationState {
  /// Baseline state, awaiting initial project ingestion.
  idle,

  /// Querying backend state to fetch simulation response.
  loadingBackend,

  /// Simulating real-time rolling narrative dialogue feed from virtual actors.
  runningDebate,

  /// Simulation complete, arbitration synthesis and metrics are rendered.
  synthesisCompleted,
}

/// Master state management engine extending [ChangeNotifier] for reactive state updates.
class AppStateProvider extends ChangeNotifier {
  final ApiService _apiService;

  AppStateProvider({ApiService? apiService}) : _apiService = apiService ?? ApiService();

  // Internal reactive state properties
  String? _currentProjectId;
  String? _projectTitle;
  String? _currentBaselineText;
  SimulationStateResponse? _simulationData;
  final List<ChatMessage> _activeChatTimeline = [];
  SimulationState _currentSimulationState = SimulationState.idle;
  bool _isIngesting = false;
  bool _isAmending = false;
  String? _networkErrorMessage;

  // Explicit getters
  String? get currentProjectId => _currentProjectId;
  String? get projectTitle => _projectTitle;
  String? get currentBaselineText => _currentBaselineText;
  SimulationStateResponse? get simulationData => _simulationData;

  /// Returns a read-only list representing the current chat timeline.
  List<ChatMessage> get activeChatTimeline => List.unmodifiable(_activeChatTimeline);
  SimulationState get currentSimulationState => _currentSimulationState;
  bool get isIngesting => _isIngesting;
  bool get isAmending => _isAmending;
  String? get networkErrorMessage => _networkErrorMessage;

  /// Explicit mutations for resetting or modifying properties externally
  void clearError() {
    _networkErrorMessage = null;
    notifyListeners();
  }

  void resetState() {
    _currentProjectId = null;
    _projectTitle = null;
    _currentBaselineText = null;
    _simulationData = null;
    _activeChatTimeline.clear();
    _currentSimulationState = SimulationState.idle;
    _isIngesting = false;
    _isAmending = false;
    _networkErrorMessage = null;
    notifyListeners();
  }

  /// 1. triggerInitialIngestion
  /// Submits the initial user prompt to the backend, tracking loading
  /// and handling standard API exception formatting.
  Future<bool> triggerInitialIngestion(String rawPrompt) async {
    _isIngesting = true;
    _networkErrorMessage = null;
    notifyListeners();

    try {
      final IngestResponse response = await _apiService.ingestProject(rawPrompt);
      
      _currentProjectId = response.extractedMetadata.projectId;
      _projectTitle = response.extractedMetadata.title;
      _currentBaselineText = response.savedPayload.baselineProposalText;
      
      _isIngesting = false;
      notifyListeners();
      return true;
    } catch (e) {
      _networkErrorMessage = e.toString().replaceFirst('Exception: ', '');
      _isIngesting = false;
      notifyListeners();
      return false;
    }
  }

  /// 2. triggerVariantAmendment
  /// Sends down the amended proposal variant under the active project slug.
  Future<bool> triggerVariantAmendment(String amendmentText, String versionTag) async {
    if (_currentProjectId == null) {
      _networkErrorMessage = 'Operational Error: No active project context loaded.';
      notifyListeners();
      return false;
    }

    _isAmending = true;
    _networkErrorMessage = null;
    notifyListeners();

    try {
      final AmendRequest request = AmendRequest(
        projectId: _currentProjectId!,
        versionTag: versionTag,
        amendedProposalText: amendmentText,
      );

      final bool success = await _apiService.amendProject(request);
      
      if (success) {
        // Sync our local baseline text view with the validated amendment
        _currentBaselineText = amendmentText;
      }

      _isAmending = false;
      notifyListeners();
      return success;
    } catch (e) {
      _networkErrorMessage = e.toString().replaceFirst('Exception: ', '');
      _isAmending = false;
      notifyListeners();
      return false;
    }
  }

  /// 3. launchTownHallSimulation
  /// Orchestrates the asynchronous sequential delivery feed of debate logs.
  Future<void> launchTownHallSimulation() async {
    if (_currentProjectId == null) {
      _networkErrorMessage = 'Simulation Error: No active project context found to simulate.';
      notifyListeners();
      return;
    }

    // Step A: Shift to loading state and purge any previous chat logs
    _currentSimulationState = SimulationState.loadingBackend;
    _activeChatTimeline.clear();
    _networkErrorMessage = null;
    notifyListeners();

    try {
      // Step B: Query the API service for simulation output
      final SimulationStateResponse response = await _apiService.fetchSimulationState(_currentProjectId!);
      _simulationData = response;

      // Step C: Shift to running debate view
      _currentSimulationState = SimulationState.runningDebate;
      notifyListeners();

      // Step D & E: Sequential looping of citizen statements with pacing delays
      for (final CitizenPersona persona in response.personas) {
        // Construct a synthetic ChatMessage wrapping the persona context
        final ChatMessage syntheticMessage = ChatMessage(
          senderName: persona.name,
          role: persona.demographicRole,
          messageText: persona.systemInstruction, // Holds the narrative speech text
          timestamp: DateTime.now(),
          sentimentScore: persona.sentimentScore,
        );

        _activeChatTimeline.add(syntheticMessage);
        notifyListeners();

        // Pause thread for 1500ms to mimic realistic conversation pace
        await Future.delayed(const Duration(milliseconds: 1500));
      }

      // Step F: Complete simulation pipeline
      _currentSimulationState = SimulationState.synthesisCompleted;
      notifyListeners();
    } catch (e) {
      _networkErrorMessage = e.toString().replaceFirst('Exception: ', '');
      _currentSimulationState = SimulationState.idle;
      notifyListeners();
    }
  }
}
