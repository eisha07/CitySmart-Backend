/// CitySmart - Mobile Systems Prompt Ingestion View
/// This file implements Page 1: The Prompt Ingestion View. It captures
/// unstructured urban proposal documents, coordinates backend ingestion,
/// and handles state boundaries dynamically via AppStateProvider.

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:google_fonts/google_fonts.dart';
import '../providers/app_state_provider.dart';

/// Page 1: Prompt Ingestion View mimicking modern generative AI canvas environments.
class PromptIngestionView extends StatefulWidget {
  const PromptIngestionView({super.key});

  @override
  State<PromptIngestionView> createState() => _PromptIngestionViewState();
}

class _PromptIngestionViewState extends State<PromptIngestionView> {
  late final TextEditingController _promptController;

  @override
  void initState() {
    super.initState();
    _promptController = TextEditingController();
  }

  @override
  void dispose() {
    _promptController.dispose();
    super.dispose();
  }

  /// Initiates the ingestion sequence through the AppStateProvider pipeline.
  Future<void> _handleIngestion(AppStateProvider appState) async {
    final String prompt = _promptController.text.trim();
    if (prompt.isEmpty) return;

    // Focus scope dismissal to hide keyboards gracefully
    FocusScope.of(context).unfocus();

    final bool success = await appState.triggerInitialIngestion(prompt);
    
    if (success) {
      _promptController.clear();
      // Optionally notify user or prompt switching via custom scaffold feedback
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            backgroundColor: const Color(0xFF00F2FE),
            content: Text(
              'Ingestion Successful: ${appState.projectTitle}',
              style: const TextStyle(color: Color(0xFF141416), fontWeight: FontWeight.bold),
            ),
            duration: const Duration(seconds: 3),
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final appState = Provider.of<AppStateProvider>(context);

    return Scaffold(
      backgroundColor: const Color(0xFF1A1A1E),
      body: SafeArea(
        child: LayoutBuilder(
          builder: (context, constraints) {
            return SingleChildScrollView(
              physics: const BouncingScrollPhysics(),
              child: ConstrainedBox(
                constraints: BoxConstraints(
                  minHeight: constraints.maxHeight,
                ),
                child: IntrinsicHeight(
                  child: Column(
                    children: [
                      // 1. Upper Workspace Area (Scrollable & Expanded)
                      Expanded(
                        child: Padding(
                          padding: const EdgeInsets.symmetric(horizontal: 24.0, vertical: 32.0),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              const SizedBox(height: 48.0),
                              // Elegant Premium Typographic Header
                              Text(
                                'Where should CitySmart\nintervene today?',
                                style: GoogleFonts.plusJakartaSans(
                                  fontSize: 26.0,
                                  fontWeight: FontWeight.bold,
                                  color: Colors.white,
                                  height: 1.3,
                                ),
                              ),
                              const SizedBox(height: 16.0),
                              // Muted Subtitle
                              const Text(
                                'Paste a raw, unformatted urban proposal document or local city planner notes to begin parsing the multi-agent town hall matrix.',
                                style: TextStyle(
                                  fontSize: 14.0,
                                  color: Color(0xFF8E8E93),
                                  height: 1.5,
                                ),
                              ),
                              const SizedBox(height: 24.0),

                              // Premium Custom Network Error Alert Box
                              if (appState.networkErrorMessage != null) ...[
                                Container(
                                  padding: const EdgeInsets.all(16.0),
                                  decoration: BoxDecoration(
                                    color: const Color(0xFFFF5D73).withOpacity(0.08),
                                    borderRadius: BorderRadius.circular(16.0),
                                    border: Border.all(
                                      color: const Color(0xFFFF5D73).withOpacity(0.25),
                                      width: 1.0,
                                    ),
                                  ),
                                  child: Row(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      const Icon(
                                        Icons.error_outline_rounded,
                                        color: Color(0xFFFF5D73),
                                        size: 20.0,
                                      ),
                                      const SizedBox(width: 12.0),
                                      Expanded(
                                        child: Column(
                                          crossAxisAlignment: CrossAxisAlignment.start,
                                          children: [
                                            const Text(
                                              'Ingestion Failure',
                                              style: TextStyle(
                                                color: Color(0xFFFF5D73),
                                                fontWeight: FontWeight.bold,
                                                fontSize: 14.0,
                                              ),
                                            ),
                                            const SizedBox(height: 4.0),
                                            Text(
                                              appState.networkErrorMessage!,
                                              style: const TextStyle(
                                                color: Color(0xFFFF8E9E),
                                                fontSize: 13.0,
                                                height: 1.4,
                                              ),
                                            ),
                                          ],
                                        ),
                                      ),
                                      IconButton(
                                        onPressed: () => appState.clearError(),
                                        icon: const Icon(
                                          Icons.close_rounded,
                                          color: Color(0xFFFF8E9E),
                                          size: 18.0,
                                        ),
                                        padding: EdgeInsets.zero,
                                        constraints: const BoxConstraints(),
                                      ),
                                    ],
                                  ),
                                ),
                              ],

                              // Dynamic Active State Display Box
                              if (appState.currentProjectId != null) ...[
                                const SizedBox(height: 20.0),
                                Container(
                                  width: double.infinity,
                                  padding: const EdgeInsets.symmetric(horizontal: 18.0, vertical: 16.0),
                                  decoration: BoxDecoration(
                                    color: const Color(0xFF00F2FE).withOpacity(0.05),
                                    borderRadius: BorderRadius.circular(16.0),
                                    border: Border.all(
                                      color: const Color(0xFF00F2FE).withOpacity(0.2),
                                      width: 1.0,
                                    ),
                                  ),
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      const Row(
                                        children: [
                                          Icon(Icons.check_circle_outline, color: Color(0xFF00F2FE), size: 18.0),
                                          SizedBox(width: 8.0),
                                          Text(
                                            'ACTIVE SIMULATION MODEL CONTEXT',
                                            style: TextStyle(
                                              color: Color(0xFF00F2FE),
                                              fontWeight: FontWeight.bold,
                                              fontSize: 11.0,
                                              letterSpacing: 1.2,
                                            ),
                                          ),
                                        ],
                                      ),
                                      const SizedBox(height: 8.0),
                                      Text(
                                        appState.projectTitle ?? '',
                                        style: const TextStyle(
                                          color: Colors.white,
                                          fontWeight: FontWeight.bold,
                                          fontSize: 16.0,
                                        ),
                                      ),
                                      const SizedBox(height: 4.0),
                                      Text(
                                        'ID: ${appState.currentProjectId}',
                                        style: const TextStyle(
                                          color: Color(0xFF6C6C7D),
                                          fontSize: 12.0,
                                          fontFamily: 'monospace',
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                              ],
                            ],
                          ),
                        ),
                      ),

                      // 2. Bottom Interaction Bar
                      Padding(
                        padding: const EdgeInsets.fromLTRB(16.0, 8.0, 16.0, 100.0), // Padding adjusted to hover safely above float nav
                        child: Container(
                          padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
                          decoration: BoxDecoration(
                            color: const Color(0xFF26262B),
                            borderRadius: BorderRadius.circular(24.0),
                            border: Border.all(
                              color: Colors.white.withOpacity(0.06),
                              width: 1.0,
                            ),
                            boxShadow: [
                              BoxShadow(
                                color: Colors.black.withOpacity(0.25),
                                blurRadius: 12.0,
                                offset: const Offset(0, 4.0),
                              ),
                            ],
                          ),
                          child: Row(
                            crossAxisAlignment: CrossAxisAlignment.center,
                            children: [
                              Expanded(
                                child: TextField(
                                  controller: _promptController,
                                  maxLines: null,
                                  keyboardType: TextInputType.multiline,
                                  style: const TextStyle(color: Colors.white, fontSize: 15.0),
                                  decoration: const InputDecoration(
                                    border: InputBorder.none,
                                    hintText: 'Propose a new infrastructure scheme, zoning amendment, or traffic layout baseline...',
                                    hintStyle: TextStyle(
                                      color: Color(0xFF6C6C7D),
                                      fontSize: 14.0,
                                    ),
                                    contentPadding: EdgeInsets.symmetric(vertical: 8.0),
                                  ),
                                ),
                              ),
                              const SizedBox(width: 8.0),

                              // Trailing Execution Button (Spinner or Send Icon)
                              AnimatedSwitcher(
                                duration: const Duration(milliseconds: 200),
                                child: appState.isIngesting
                                    ? const SizedBox(
                                        width: 36.0,
                                        height: 36.0,
                                        child: Padding(
                                          padding: EdgeInsets.all(8.0),
                                          child: CircularProgressIndicator(
                                            valueColor: AlwaysStoppedAnimation<Color>(Color(0xFF00F2FE)),
                                            strokeWidth: 2.0,
                                          ),
                                        ),
                                      )
                                    : Container(
                                        decoration: const BoxDecoration(
                                          color: Color(0xFF00F2FE),
                                          shape: BoxShape.circle,
                                        ),
                                        child: IconButton(
                                          onPressed: () => _handleIngestion(appState),
                                          icon: const Icon(
                                            Icons.send_rounded,
                                            color: Color(0xFF141416),
                                            size: 18.0,
                                          ),
                                          padding: EdgeInsets.zero,
                                          constraints: const BoxConstraints(
                                            minWidth: 36.0,
                                            minHeight: 36.0,
                                          ),
                                        ),
                                      ),
                              ),
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            );
          },
        ),
      ),
    );
  }
}
