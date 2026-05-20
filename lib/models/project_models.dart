/// CitySmart - Mobile Systems Data Serialization Matrix
/// This file houses the type-safe, defensively structured model layer for the
/// CitySmart urban simulation mobile client. It enforces strict safety boundaries
/// during JSON deserialization to eliminate runtime stage crashes arising from 
/// dynamic API payloads or mismatched type formats.

import 'dart:convert';

/// 1. UnstructuredIngestRequest
/// Formulates the baseline system prompt pipeline request.
class UnstructuredIngestRequest {
  /// The raw user prompt detailing the proposed urban project.
  final String userPrompt;

  const UnstructuredIngestRequest({
    required this.userPrompt,
  });

  /// Factory constructor to defensively deserialize JSON map into [UnstructuredIngestRequest].
  factory UnstructuredIngestRequest.fromJson(Map<String, dynamic> json) {
    return UnstructuredIngestRequest(
      userPrompt: json['user_prompt'] as String? ?? '',
    );
  }

  /// Exports the [UnstructuredIngestRequest] instance into a backend-compliant snake_case map.
  Map<String, dynamic> toJson() {
    return {
      'user_prompt': userPrompt,
    };
  }

  @override
  String toString() => 'UnstructuredIngestRequest(userPrompt: $userPrompt)';
}

/// 2. ExtractedMetadata
/// Holds the core relational indexing identifiers returned by the ingestion engine.
class ExtractedMetadata {
  /// Unique identifier of the project (lowercase, hyphenated unique slug).
  final String projectId;

  /// Parsed clean urban project title.
  final String title;

  const ExtractedMetadata({
    required this.projectId,
    required this.title,
  });

  /// Factory constructor to defensively deserialize JSON map into [ExtractedMetadata].
  factory ExtractedMetadata.fromJson(Map<String, dynamic> json) {
    return ExtractedMetadata(
      projectId: json['project_id'] as String? ?? '',
      title: json['title'] as String? ?? '',
    );
  }

  /// Exports the [ExtractedMetadata] instance into a backend-compliant snake_case map.
  Map<String, dynamic> toJson() {
    return {
      'project_id': projectId,
      'title': title,
    };
  }

  @override
  String toString() => 'ExtractedMetadata(projectId: $projectId, title: $title)';
}

/// 3. SavedPayload
/// Houses the raw extracted primary content text.
class SavedPayload {
  /// The extracted baseline text of the urban proposal.
  final String baselineProposalText;

  const SavedPayload({
    required this.baselineProposalText,
  });

  /// Factory constructor to defensively deserialize JSON map into [SavedPayload].
  factory SavedPayload.fromJson(Map<String, dynamic> json) {
    return SavedPayload(
      baselineProposalText: json['baseline_proposal_text'] as String? ?? '',
    );
  }

  /// Exports the [SavedPayload] instance into a backend-compliant snake_case map.
  Map<String, dynamic> toJson() {
    return {
      'baseline_proposal_text': baselineProposalText,
    };
  }

  @override
  String toString() => 'SavedPayload(baselineProposalText: $baselineProposalText)';
}

/// 4. IngestResponse
/// Encapsulates the multi-layered gateway response.
class IngestResponse {
  /// Status of the ingestion request.
  final String status;

  /// Extracted metadata containing project identification parameters.
  final ExtractedMetadata extractedMetadata;

  /// The primary saved payload content.
  final SavedPayload savedPayload;

  const IngestResponse({
    required this.status,
    required this.extractedMetadata,
    required this.savedPayload,
  });

  /// Factory constructor to defensively deserialize JSON map into [IngestResponse].
  factory IngestResponse.fromJson(Map<String, dynamic> json) {
    return IngestResponse(
      status: json['status'] as String? ?? '',
      extractedMetadata: json['extracted_metadata'] is Map<String, dynamic>
          ? ExtractedMetadata.fromJson(json['extracted_metadata'] as Map<String, dynamic>)
          : const ExtractedMetadata(projectId: '', title: ''),
      savedPayload: json['saved_payload'] is Map<String, dynamic>
          ? SavedPayload.fromJson(json['saved_payload'] as Map<String, dynamic>)
          : const SavedPayload(baselineProposalText: ''),
    );
  }

  /// Exports the [IngestResponse] instance into a backend-compliant snake_case map.
  Map<String, dynamic> toJson() {
    return {
      'status': status,
      'extracted_metadata': extractedMetadata.toJson(),
      'saved_payload': savedPayload.toJson(),
    };
  }

  @override
  String toString() {
    return 'IngestResponse(status: $status, extractedMetadata: $extractedMetadata, savedPayload: $savedPayload)';
  }
}

/// 5. AmendRequest
/// Structural payload for our multi-version variant branch engine.
class AmendRequest {
  /// Unique slug identifier of the project.
  final String projectId;

  /// Version tag associated with the new proposal variant.
  final String versionTag;

  /// Amended proposal text to inject into the branch system.
  final String amendedProposalText;

  const AmendRequest({
    required this.projectId,
    required this.versionTag,
    required this.amendedProposalText,
  });

  /// Factory constructor to defensively deserialize JSON map into [AmendRequest].
  factory AmendRequest.fromJson(Map<String, dynamic> json) {
    return AmendRequest(
      projectId: json['project_id'] as String? ?? '',
      versionTag: json['version_tag'] as String? ?? '',
      amendedProposalText: json['amended_proposal_text'] as String? ?? '',
    );
  }

  /// Exports the [AmendRequest] instance into a backend-compliant snake_case map.
  Map<String, dynamic> toJson() {
    return {
      'project_id': projectId,
      'version_tag': versionTag,
      'amended_proposal_text': amendedProposalText,
    };
  }

  @override
  String toString() {
    return 'AmendRequest(projectId: $projectId, versionTag: $versionTag, amendedProposalText: $amendedProposalText)';
  }
}

/// 6. CitizenPersona
/// Holds the behavioral attributes of our simulated local actors in the town hall.
class CitizenPersona {
  /// Name of the simulated actor.
  final String name;

  /// Demographic category/role of the citizen persona.
  final String demographicRole;

  /// Numerical sentiment score representing response/attitude.
  final int sentimentScore;

  /// The deep, first-person contextual agent behavior profile instruction.
  final String systemInstruction;

  const CitizenPersona({
    required this.name,
    required this.demographicRole,
    required this.sentimentScore,
    required this.systemInstruction,
  });

  /// Factory constructor to defensively deserialize JSON map into [CitizenPersona].
  factory CitizenPersona.fromJson(Map<String, dynamic> json) {
    return CitizenPersona(
      name: json['name'] as String? ?? '',
      demographicRole: json['demographic_role'] as String? ?? '',
      sentimentScore: (json['sentiment_score'] as num?)?.toInt() ?? 0,
      systemInstruction: json['system_instruction'] as String? ?? '',
    );
  }

  /// Exports the [CitizenPersona] instance into a backend-compliant snake_case map.
  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'demographic_role': demographicRole,
      'sentiment_score': sentimentScore,
      'system_instruction': systemInstruction,
    };
  }

  @override
  String toString() {
    return 'CitizenPersona(name: $name, demographicRole: $demographicRole, sentimentScore: $sentimentScore)';
  }
}

/// 7. ChatMessage
/// Evaluates dynamic entries injected into our live town hall scroll view.
class ChatMessage {
  /// Name of the citizen or agent dispatching the message.
  final String senderName;

  /// Role of the sender (e.g. system, citizen, moderator).
  final String role;

  /// Actual text statement.
  final String messageText;

  /// Precise moment the statement was injected into the log.
  final DateTime timestamp;

  /// The computed emotional or alignment index of the text snippet.
  final int sentimentScore;

  const ChatMessage({
    required this.senderName,
    required this.role,
    required this.messageText,
    required this.timestamp,
    required this.sentimentScore,
  });

  /// Factory constructor to defensively deserialize JSON map into [ChatMessage].
  factory ChatMessage.fromJson(Map<String, dynamic> json) {
    return ChatMessage(
      senderName: json['sender_name'] as String? ?? '',
      role: json['role'] as String? ?? '',
      messageText: json['message_text'] as String? ?? '',
      timestamp: json['timestamp'] != null
          ? (DateTime.tryParse(json['timestamp'].toString()) ?? DateTime.now())
          : DateTime.now(),
      sentimentScore: (json['sentiment_score'] as num?)?.toInt() ?? 0,
    );
  }

  /// Exports the [ChatMessage] instance into a backend-compliant snake_case map.
  Map<String, dynamic> toJson() {
    return {
      'sender_name': senderName,
      'role': role,
      'message_text': messageText,
      'timestamp': timestamp.toIso8601String(),
      'sentiment_score': sentimentScore,
    };
  }

  @override
  String toString() {
    return 'ChatMessage(senderName: $senderName, role: $role, messageText: $messageText, timestamp: $timestamp, sentimentScore: $sentimentScore)';
  }
}

/// 8. SimulationStateResponse
/// The core multi-agent evaluation output from Gemini 1.5 Pro.
class SimulationStateResponse {
  /// Unique identifier of the urban simulation project.
  final String projectId;

  /// Collection of simulated citizen actors participating in the evaluation.
  final List<CitizenPersona> personas;

  /// Score evaluating how well the project integrates into target communities.
  final int socialFeasibilityScore;

  /// Score assessing economic sustainability, return and cost efficacy.
  final int economicViabilityScore;

  /// Score capturing political receptiveness and compliance framework alignment.
  final int politicalAcceptanceScore;

  /// Rich markdown summary report delivered by the arbitration agent.
  final String arbitratorVerdict;

  const SimulationStateResponse({
    required this.projectId,
    required this.personas,
    required this.socialFeasibilityScore,
    required this.economicViabilityScore,
    required this.politicalAcceptanceScore,
    required this.arbitratorVerdict,
  });

  /// Factory constructor to defensively deserialize JSON map into [SimulationStateResponse].
  factory SimulationStateResponse.fromJson(Map<String, dynamic> json) {
    return SimulationStateResponse(
      projectId: json['project_id'] as String? ?? '',
      personas: (json['personas'] as List?)
              ?.map((e) => CitizenPersona.fromJson(e as Map<String, dynamic>))
              .toList() ??
          [],
      socialFeasibilityScore: (json['social_feasibility_score'] as num?)?.toInt() ?? 0,
      economicViabilityScore: (json['economic_viability_score'] as num?)?.toInt() ?? 0,
      politicalAcceptanceScore: (json['political_acceptance_score'] as num?)?.toInt() ?? 0,
      arbitratorVerdict: json['arbitrator_verdict'] as String? ?? '',
    );
  }

  /// Exports the [SimulationStateResponse] instance into a backend-compliant snake_case map.
  Map<String, dynamic> toJson() {
    return {
      'project_id': projectId,
      'personas': personas.map((e) => e.toJson()).toList(),
      'social_feasibility_score': socialFeasibilityScore,
      'economic_viability_score': economicViabilityScore,
      'political_acceptance_score': politicalAcceptanceScore,
      'arbitrator_verdict': arbitratorVerdict,
    };
  }

  @override
  String toString() {
    return 'SimulationStateResponse(projectId: $projectId, personas: ${personas.length}, socialFeasibilityScore: $socialFeasibilityScore, economicViabilityScore: $economicViabilityScore, politicalAcceptanceScore: $politicalAcceptanceScore)';
  }
}
