/// CitySmart - Mobile Systems Version Branching View
/// This file implements Page 4: The Version Branching View. It facilitates
/// variant policy amendments and handles transaction states safely with overlays.

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/app_state_provider.dart';

/// Page 4: Version Branching View allowing users to propose amendments.
class VersionBranchingView extends StatefulWidget {
  const VersionBranchingView({super.key});

  @override
  State<VersionBranchingView> createState() => _VersionBranchingViewState();
}

class _VersionBranchingViewState extends State<VersionBranchingView> {
  late final TextEditingController _amendmentController;
  late final TextEditingController _versionTagController;

  @override
  void initState() {
    super.initState();
    _amendmentController = TextEditingController();
    _versionTagController = TextEditingController();
  }

  @override
  void dispose() {
    _amendmentController.dispose();
    _versionTagController.dispose();
    super.dispose();
  }

  /// Triggers the asynchronous variant amendment process.
  Future<void> _handleAmendment(AppStateProvider appState) async {
    final String amendmentText = _amendmentController.text.trim();
    final String versionTag = _versionTagController.text.trim();

    if (amendmentText.isEmpty || versionTag.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          backgroundColor: Color(0xFFFF5D73),
          content: Text(
            'Form Error: Both amendment text and version tag are required.',
            style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
          ),
        ),
      );
      return;
    }

    FocusScope.of(context).unfocus();

    final bool success = await appState.triggerVariantAmendment(amendmentText, versionTag);

    if (success && mounted) {
      _amendmentController.clear();
      _versionTagController.clear();
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          backgroundColor: const Color(0xFF2EC4B6),
          content: Text(
            'Variant Amendment Registered: $versionTag Successfully Synced.',
            style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
          ),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final appState = Provider.of<AppStateProvider>(context);

    return Scaffold(
      backgroundColor: const Color(0xFF141416),
      body: SafeArea(
        child: Stack(
          children: [
            // 1. Primary Form Workspace (Hardened LayoutBuilder and SingleChildScrollView)
            LayoutBuilder(
              builder: (context, constraints) {
                return SingleChildScrollView(
                  physics: const BouncingScrollPhysics(),
                  child: ConstrainedBox(
                    constraints: BoxConstraints(
                      minHeight: constraints.maxHeight,
                    ),
                    child: IntrinsicHeight(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          _buildHeader(appState),
                          Expanded(
                            child: _buildFormBody(appState),
                          ),
                        ],
                      ),
                    ),
                  ),
                );
              },
            ),

            // 2. Transacting/Loading Modal Overlay Screen Blocker
            if (appState.isAmending) ...[
              Positioned.fill(
                child: Container(
                  color: Colors.black.withOpacity(0.75),
                  child: Center(
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 32.0, vertical: 24.0),
                      decoration: BoxDecoration(
                        color: const Color(0xFF1E1E24),
                        borderRadius: BorderRadius.circular(20.0),
                        border: Border.all(color: Colors.white.withOpacity(0.08), width: 1),
                      ),
                      child: const Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          CircularProgressIndicator(
                            valueColor: AlwaysStoppedAnimation<Color>(Color(0xFFFF9F1C)),
                            strokeWidth: 3,
                          ),
                          SizedBox(height: 20.0),
                          Text(
                            'Branching simulation state...',
                            style: TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.bold,
                              fontSize: 15.0,
                            ),
                          ),
                          SizedBox(height: 4.0),
                          Text(
                            'Registering variant proposal to the ledger',
                            style: TextStyle(
                              color: Color(0xFF6C6C7D),
                              fontSize: 12.0,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  /// Builds the top summary title bar.
  Widget _buildHeader(AppStateProvider appState) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 14.0),
      decoration: const BoxDecoration(
        color: Color(0xFF0D0D0F),
        border: Border(bottom: BorderSide(color: Colors.white10, width: 1.0)),
      ),
      child: const Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'BRANCH ENGINE PANEL',
            style: TextStyle(
              color: Color(0xFFFF9F1C),
              fontWeight: FontWeight.bold,
              fontSize: 10.0,
              letterSpacing: 1.2,
            ),
          ),
          SizedBox(height: 4.0),
          Text(
            'Zoning & Policy Amendment',
            style: TextStyle(
              color: Colors.white,
              fontWeight: FontWeight.bold,
              fontSize: 15.0,
            ),
          ),
        ],
      ),
    );
  }

  /// Builds the form container holding text fields and button controls.
  Widget _buildFormBody(AppStateProvider appState) {
    if (appState.currentProjectId == null) {
      return const Expanded(
        child: Center(
          child: Padding(
            padding: EdgeInsets.all(32.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.lock_rounded, size: 56.0, color: Color(0xFF6C6C7D)),
                const SizedBox(height: 18.0),
                Text(
                  'Branching Engine Locked',
                  style: TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                    fontSize: 18.0,
                  ),
                ),
                const SizedBox(height: 8.0),
                Text(
                  'Please ingest an urban proposal document on Page 1 first to active branching engines.',
                  style: TextStyle(color: Color(0xFF6C6C7D), fontSize: 13.0, height: 1.4),
                  textAlign: TextAlign.center,
                ),
              ],
            ),
          ),
        ),
      );
    }

    return Padding(
      padding: const EdgeInsets.fromLTRB(24.0, 24.0, 24.0, 100.0), // Hover safety padding
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // Project Metadata Context Card
          Container(
            padding: const EdgeInsets.all(16.0),
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.03),
              borderRadius: BorderRadius.circular(16.0),
              border: Border.all(color: Colors.white.withOpacity(0.06), width: 1.0),
            ),
            child: Row(
              children: [
                const Icon(Icons.info_outline_rounded, color: Color(0xFFFF9F1C), size: 20.0),
                const SizedBox(width: 12.0),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'ACTIVE REVISION CONTEXT',
                        style: TextStyle(
                          color: Color(0xFFFF9F1C),
                          fontWeight: FontWeight.bold,
                          fontSize: 10.0,
                          letterSpacing: 1.0,
                        ),
                      ),
                      const SizedBox(height: 4.0),
                      Text(
                        appState.projectTitle ?? '',
                        style: const TextStyle(
                          color: Colors.white,
                          fontWeight: FontWeight.bold,
                          fontSize: 14.0,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 24.0),

          // 1. Version Tag Identifier Input
          const Text(
            'VERSION IDENTIFIER TAG',
            style: TextStyle(
              color: Color(0xFF6C6C7D),
              fontWeight: FontWeight.bold,
              fontSize: 11.0,
              letterSpacing: 1.0,
            ),
          ),
          const SizedBox(height: 8.0),
          Container(
            decoration: BoxDecoration(
              color: const Color(0xFF1E1E24),
              borderRadius: BorderRadius.circular(14.0),
              border: Border.all(color: Colors.white.withOpacity(0.08), width: 1.0),
            ),
            child: TextField(
              controller: _versionTagController,
              style: const TextStyle(color: Colors.white, fontSize: 14.0),
              decoration: const InputDecoration(
                hintText: 'e.g. v2.1-zoning-amendment',
                hintStyle: TextStyle(color: Color(0xFF6C6C7D), fontSize: 13.0),
                contentPadding: EdgeInsets.symmetric(horizontal: 16.0, vertical: 14.0),
                border: InputBorder.none,
              ),
            ),
          ),
          const SizedBox(height: 24.0),

          // 2. Large Paragraph Amendment Text Input (maxLines: 8)
          const Text(
            'AMENDED POLICY / ZONING PROPOSAL STATEMENT',
            style: TextStyle(
              color: Color(0xFF6C6C7D),
              fontWeight: FontWeight.bold,
              fontSize: 11.0,
              letterSpacing: 1.0,
            ),
          ),
          const SizedBox(height: 8.0),
          Container(
            decoration: BoxDecoration(
              color: const Color(0xFF1E1E24),
              borderRadius: BorderRadius.circular(16.0),
              border: Border.all(color: Colors.white.withOpacity(0.08), width: 1.0),
            ),
            child: TextField(
              controller: _amendmentController,
              maxLines: 8,
              style: const TextStyle(color: Colors.white, fontSize: 14.0),
              decoration: const InputDecoration(
                hintText: 'Enter the updated proposal text including pathways, housing modifications, greenways, or cost reduction schemes...',
                hintStyle: TextStyle(color: Color(0xFF6C6C7D), fontSize: 13.0),
                contentPadding: EdgeInsets.all(16.0),
                border: InputBorder.none,
              ),
            ),
          ),
          const Spacer(),
          const SizedBox(height: 24.0),

          // 3. Full-Width Execution Action Button
          InkWell(
            onTap: () => _handleAmendment(appState),
            borderRadius: BorderRadius.circular(30.0),
            child: Container(
              padding: const EdgeInsets.symmetric(vertical: 16.0),
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  colors: [Color(0xFFFF9F1C), Color(0xFFFF6B6B)],
                ),
                borderRadius: BorderRadius.circular(30.0),
                boxShadow: [
                  BoxShadow(
                    color: const Color(0xFFFF9F1C).withOpacity(0.25),
                    blurRadius: 16.0,
                    offset: const Offset(0, 4.0),
                  ),
                ],
              ),
              child: const Center(
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(Icons.alt_route_rounded, color: Color(0xFF141416), size: 20.0),
                    SizedBox(width: 8.0),
                    Text(
                      'COMMIT REVISION BRANCH',
                      style: TextStyle(
                        color: Color(0xFF141416),
                        fontWeight: FontWeight.bold,
                        fontSize: 14.0,
                        letterSpacing: 1.0,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
