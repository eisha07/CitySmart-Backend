/// CitySmart - Mobile Systems Submitted Proposal View
/// This file implements Page 3: The Submitted Proposal Clipboard Sheet view.
/// It renders a skeuomorphic wood-and-steel clipboard hosting active proposals,
/// alongside an inline diff engine demonstrating policy modifications.

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/app_state_provider.dart';

/// Page 3: Submitted Proposal Clipboard View displaying active proposal texts.
class SubmittedProposalView extends StatefulWidget {
  const SubmittedProposalView({super.key});

  @override
  State<SubmittedProposalView> createState() => _SubmittedProposalViewState();
}

class _SubmittedProposalViewState extends State<SubmittedProposalView> {
  String? _originalText;
  String? _lastProjectId;

  /// Parses the baseline and amended texts to build custom formatted TextSpans.
  List<TextSpan> _buildDynamicProposalSpans(String currentText) {
    final String original = _originalText ?? currentText;

    // Standard styling variables
    const TextStyle bodyStyle = TextStyle(
      color: Color(0xFF2C3E50),
      fontSize: 15.0,
      height: 1.6,
      fontFamily: 'serif',
    );

    // If no amendments exist, return the standard text flow
    if (original == currentText) {
      return [
        TextSpan(text: currentText, style: bodyStyle),
      ];
    }

    // Otherwise, generate a marked-up tactile diff document representation
    return [
      const TextSpan(
        text: "ORIGINAL POLICY RESOLUTION (WITHDRAWN):\n",
        style: TextStyle(
          color: Color(0xFFC0392B),
          fontWeight: FontWeight.bold,
          fontSize: 12.0,
          letterSpacing: 1.0,
          fontFamily: 'monospace',
        ),
      ),
      TextSpan(
        text: '$original\n\n',
        style: TextStyle(
          color: Colors.red.shade800,
          decoration: TextDecoration.lineThrough,
          decorationColor: Colors.red.shade900,
          decorationThickness: 2.5,
          fontSize: 14.5,
          height: 1.5,
          fontFamily: 'serif',
        ),
      ),
      const TextSpan(
        text: "AMENDED POLICY RESOLUTION (ADOPTED):\n",
        style: TextStyle(
          color: Color(0xFF27AE60),
          fontWeight: FontWeight.bold,
          fontSize: 12.0,
          letterSpacing: 1.0,
          fontFamily: 'monospace',
        ),
      ),
      TextSpan(
        text: '[ $currentText ]',
        style: const TextStyle(
          color: Color(0xFF1E824C),
          fontStyle: FontStyle.italic,
          fontWeight: FontWeight.bold,
          fontSize: 15.0,
          height: 1.5,
          fontFamily: 'serif',
        ),
      ),
    ];
  }

  @override
  Widget build(BuildContext context) {
    final appState = Provider.of<AppStateProvider>(context);
    final String currentText = appState.currentBaselineText ?? '';

    // Automatically sync and store original baseline text per project context
    if (appState.currentProjectId != _lastProjectId) {
      _lastProjectId = appState.currentProjectId;
      _originalText = appState.currentBaselineText;
    }

    return Scaffold(
      backgroundColor: const Color(0xFF2D1510),
      body: SafeArea(
        child: Column(
          children: [
            // Clipboard Top Action Header
            _buildActionHeader(appState),

            Expanded(
              child: _buildClipboardBody(currentText, appState),
            ),
          ],
        ),
      ),
    );
  }

  /// Builds a clean top action bar with state summaries.
  Widget _buildActionHeader(AppStateProvider appState) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 14.0),
      decoration: const BoxDecoration(
        color: Color(0xFF1F0F0B),
        border: Border(bottom: BorderSide(color: Colors.white10, width: 1.0)),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          const Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'TACTILE LEDGER VIEW',
                style: TextStyle(
                  color: Color(0xFFFF9F1C),
                  fontWeight: FontWeight.bold,
                  fontSize: 10.0,
                  letterSpacing: 1.2,
                ),
              ),
              SizedBox(height: 4.0),
              Text(
                'Proposal Paper Clipboard',
                style: TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                  fontSize: 15.0,
                ),
              ),
            ],
          ),
          if (_originalText != null && _originalText != appState.currentBaselineText) ...[
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10.0, vertical: 4.0),
              decoration: BoxDecoration(
                color: const Color(0xFF27AE60).withOpacity(0.12),
                borderRadius: BorderRadius.circular(12.0),
                border: Border.all(color: const Color(0xFF27AE60).withOpacity(0.3)),
              ),
              child: const Row(
                children: [
                  Icon(Icons.edit_note_rounded, color: Color(0xFF2EC4B6), size: 14.0),
                  SizedBox(width: 4.0),
                  Text(
                    'AMENDED',
                    style: TextStyle(
                      color: Color(0xFF2EC4B6),
                      fontSize: 9.0,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ],
      ),
    );
  }

  /// Builds the high-fidelity skeuomorphic wooden clipboard stack.
  Widget _buildClipboardBody(String currentText, AppStateProvider appState) {
    if (appState.currentProjectId == null) {
      return const Center(
        child: Padding(
          padding: EdgeInsets.all(32.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(Icons.lock_rounded, size: 56.0, color: Color(0xFF8D6E63)),
              const SizedBox(height: 18.0),
              Text(
                'Tactile Ledger Locked',
                style: TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                  fontSize: 18.0,
                ),
              ),
              const SizedBox(height: 8.0),
              Text(
                'Please ingest an urban proposal document on Page 1 to load the clipboard ledger view.',
                style: TextStyle(color: Color(0xFF8D6E63), fontSize: 13.0, height: 1.4),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      );
    }

    return Container(
      width: double.infinity,
      height: double.infinity,
      padding: const EdgeInsets.fromLTRB(18.0, 18.0, 18.0, 100.0), // Hover safety gap
      decoration: const BoxDecoration(
        // Layer 1: Clipboard Wooden Base (Gradients and wood colors)
        gradient: RadialGradient(
          center: Alignment.center,
          radius: 1.3,
          colors: [
            Color(0xFF5D4037), // Organic medium wood brown
            Color(0xFF2D1510), // Extremely deep backing shadow wood
          ],
        ),
      ),
      child: Stack(
        clipBehavior: Clip.none,
        alignment: Alignment.topCenter,
        children: [
          // Layer 3: The Document Paper Core
          Positioned.fill(
            top: 45.0, // Top margin offset to seat below mechanical clip clamp
            child: Container(
              padding: const EdgeInsets.all(24.0),
              decoration: BoxDecoration(
                color: const Color(0xFFF9F9F6), // Legal Off-White Engineering Sheet
                borderRadius: BorderRadius.circular(4.0),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.3),
                    blurRadius: 12.0,
                    offset: const Offset(0, 6.0),
                  ),
                  BoxShadow(
                    color: Colors.black.withOpacity(0.15),
                    blurRadius: 4.0,
                    offset: const Offset(0, 2.0),
                  ),
                ],
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  // Paper Title Header Area
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Expanded(
                        child: Text(
                          (appState.projectTitle ?? 'URBAN SCHEME').toUpperCase(),
                          style: const TextStyle(
                            fontFamily: 'monospace',
                            color: Color(0xFF7F8C8D),
                            fontWeight: FontWeight.bold,
                            fontSize: 13.0,
                            letterSpacing: 1.5,
                          ),
                        ),
                      ),
                      Text(
                        'REF: ${appState.currentProjectId?.substring(0, 8).toUpperCase() ?? ''}',
                        style: const TextStyle(
                          fontFamily: 'monospace',
                          color: Color(0xFFBDC3C7),
                          fontSize: 11.0,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12.0),
                  Container(
                    height: 1.5,
                    color: const Color(0xFFBDC3C7).withOpacity(0.4),
                  ),
                  const SizedBox(height: 16.0),

                  // Scrollable Paper Text Body
                  Expanded(
                    child: SingleChildScrollView(
                      physics: const BouncingScrollPhysics(),
                      child: RichText(
                        text: TextSpan(
                          children: _buildDynamicProposalSpans(currentText),
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),

          // Layer 2: Top Mechanical Binder Clip
          Positioned(
            top: 20.0,
            child: Container(
              width: 140.0,
              height: 35.0,
              decoration: BoxDecoration(
                // Industrial Brushed Steel Gradient
                gradient: const LinearGradient(
                  colors: [
                    Color(0xFFECEFF1), // Highlights
                    Color(0xFFB0BEC5), // Metal base
                    Color(0xFF78909C), // Shadows
                  ],
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                ),
                borderRadius: BorderRadius.circular(6.0),
                border: Border.all(
                  color: Colors.black.withOpacity(0.15),
                  width: 1.0,
                ),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.45),
                    blurRadius: 8.0,
                    offset: const Offset(0, 4.0),
                  ),
                ],
              ),
              child: Center(
                child: Container(
                  width: 32.0,
                  height: 12.0,
                  decoration: BoxDecoration(
                    color: const Color(0xFF37474F), // Clip structural bolt
                    borderRadius: BorderRadius.circular(4.0),
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
