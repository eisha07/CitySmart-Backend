/// CitySmart - Mobile Systems Town Hall Simulation View
/// This file implements Page 2: The Interactive 10-Persona Town Hall Grid
/// & Dialogue Matrix. It includes horizontal avatar metrics, KakaoTalk-themed
/// message feeds, and automated scrolling control scripts.

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/app_state_provider.dart';
import '../models/project_models.dart';
import '../widgets/summary_dialog.dart';

/// Page 2: Town Hall Simulation view showing real-time debate dialogues.
class TownHallSimulationView extends StatefulWidget {
  const TownHallSimulationView({super.key});

  @override
  State<TownHallSimulationView> createState() => _TownHallSimulationViewState();
}

class _TownHallSimulationViewState extends State<TownHallSimulationView> {
  late final ScrollController _scrollController;
  int _lastTimelineLength = 0;
  bool _dialogShown = false;

  @override
  void initState() {
    super.initState();
    _scrollController = ScrollController();
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  /// Automatically scrolls the KakaoTalk chat log to the bottom.
  void _scrollToBottom() {
    if (_scrollController.hasClients) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final appState = Provider.of<AppStateProvider>(context);

    // Watch for simulation completed state to launch the glassmorphic modal popup
    if (appState.currentSimulationState == SimulationState.synthesisCompleted && !_dialogShown) {
      _dialogShown = true;
      WidgetsBinding.instance.addPostFrameCallback((_) {
        showDialog(
          context: context,
          barrierColor: Colors.black.withOpacity(0.55),
          builder: (context) => const SummaryDialog(),
        );
      });
    } else if (appState.currentSimulationState != SimulationState.synthesisCompleted) {
      _dialogShown = false;
    }

    // Watch for chat log additions to animate scrolling
    final int currentLength = appState.activeChatTimeline.length;
    if (currentLength > _lastTimelineLength) {
      _lastTimelineLength = currentLength;
      _scrollToBottom();
    }

    return Scaffold(
      backgroundColor: const Color(0xFF1E1E24),
      body: SafeArea(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Header Matrix Info Bar
            _buildHeader(appState),

            // Main display depending on current simulation states
            Expanded(
              child: _buildSimulationBody(appState),
            ),
          ],
        ),
      ),
    );
  }

  /// Builds a dark-mode styled header showing active status summaries.
  Widget _buildHeader(AppStateProvider appState) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 14.0),
      decoration: BoxDecoration(
        color: const Color(0xFF141416),
        border: Border.all(color: Colors.white.withOpacity(0.04), width: 1),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'TOWN HALL DEBATE MATRIX',
                  style: TextStyle(
                    color: Color(0xFF00F2FE),
                    fontWeight: FontWeight.bold,
                    fontSize: 10.0,
                    letterSpacing: 1.2,
                  ),
                ),
                const SizedBox(height: 4.0),
                Text(
                  appState.projectTitle ?? 'No Active Context',
                  style: const TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                    fontSize: 15.0,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ),
          ),
          if (appState.currentSimulationState == SimulationState.runningDebate) ...[
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10.0, vertical: 4.0),
              decoration: BoxDecoration(
                color: const Color(0xFF00F2FE).withOpacity(0.1),
                borderRadius: BorderRadius.circular(12.0),
                border: Border.all(color: const Color(0xFF00F2FE).withOpacity(0.3)),
              ),
              child: const Row(
                children: [
                  SizedBox(
                    width: 6.0,
                    height: 6.0,
                    child: CircularProgressIndicator(
                      strokeWidth: 1.5,
                      valueColor: AlwaysStoppedAnimation<Color>(Color(0xFF00F2FE)),
                    ),
                  ),
                  SizedBox(width: 6.0),
                  Text(
                    'LIVE FEED',
                    style: TextStyle(
                      color: Color(0xFF00F2FE),
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

  /// Builds the primary viewport according to simulation status.
  Widget _buildSimulationBody(AppStateProvider appState) {
    if (appState.currentProjectId == null) {
      return const _AlertFallbackView(
        icon: Icons.lock_rounded,
        title: 'Project Context Locked',
        subtitle: 'Please ingest an urban proposal document on Page 1 first to initialize the state matrix.',
      );
    }

    if (appState.currentSimulationState == SimulationState.idle && appState.simulationData == null) {
      return _buildLaunchPortal(appState);
    }

    if (appState.currentSimulationState == SimulationState.loadingBackend) {
      return const Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            CircularProgressIndicator(
              valueColor: AlwaysStoppedAnimation<Color>(Color(0xFF00F2FE)),
              strokeWidth: 3,
            ),
            SizedBox(height: 24.0),
            Text(
              'Orchestrating town hall agents...',
              style: TextStyle(color: Colors.white, fontSize: 16.0, fontWeight: FontWeight.bold),
            ),
            SizedBox(height: 6.0),
            Text(
              'Simulating socio-economic debate metrics via Gemini',
              style: TextStyle(color: Color(0xFF6C6C7D), fontSize: 13.0),
            ),
          ],
        ),
      );
    }

    // Otherwise render: 1. Sentiment Profile list, 2. KakaoTalk Dialogue feeds
    final List<CitizenPersona> personas = appState.simulationData?.personas ?? [];
    
    return Column(
      children: [
        // 1. Upper Avatar Sentiment Row (Fixed Height: 110px)
        Container(
          height: 110.0,
          color: const Color(0xFF1E1E24),
          child: ListView.builder(
            scrollDirection: Axis.horizontal,
            physics: const BouncingScrollPhysics(),
            padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 12.0),
            itemCount: personas.length,
            itemBuilder: (context, index) {
              final persona = personas[index];
              final String initials = persona.name.isNotEmpty 
                  ? (persona.name.split(' ').map((s) => s.isNotEmpty ? s[0] : '').take(2).join().toUpperCase())
                  : 'CP';
              final bool isPositive = persona.sentimentScore > 50;

              return Padding(
                padding: const EdgeInsets.symmetric(horizontal: 10.0),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Stack(
                      children: [
                        // Main round circular avatar
                        Container(
                          width: 52.0,
                          height: 52.0,
                          decoration: BoxDecoration(
                            shape: BoxShape.shapeCircle,
                            gradient: const LinearGradient(
                              colors: [Color(0xFF3A3D40), Color(0xFF181B1D)],
                              begin: Alignment.topLeft,
                              end: Alignment.bottomRight,
                            ),
                            border: Border.all(
                              color: Colors.white.withOpacity(0.12),
                              width: 1.5,
                            ),
                          ),
                          child: Center(
                            child: Text(
                              initials,
                              style: const TextStyle(
                                color: Colors.white,
                                fontWeight: FontWeight.bold,
                                fontSize: 14.0,
                              ),
                            ),
                          ),
                        ),
                        // Overlapping floating badge on the top right
                        Positioned(
                          top: 0,
                          right: 0,
                          child: Container(
                            padding: const EdgeInsets.all(3.0),
                            decoration: BoxDecoration(
                              color: isPositive ? const Color(0xFF2EC4B6) : const Color(0xFFFF5D73),
                              shape: BoxShape.shapeCircle,
                              border: Border.all(color: const Color(0xFF1E1E24), width: 1.5),
                            ),
                            child: Icon(
                              isPositive ? Icons.thumb_up_rounded : Icons.thumb_down_rounded,
                              size: 10.0,
                              color: Colors.white,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 6.0),
                    // Small persona name below
                    SizedBox(
                      width: 60.0,
                      child: Text(
                        persona.name,
                        style: const TextStyle(
                          color: Color(0xFFC7C7CD),
                          fontSize: 10.0,
                        ),
                        textAlign: TextAlign.center,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                  ],
                ),
              );
            },
          ),
        ),

        // 2. Lower KakaoTalk Dialogue Field (Expanded)
        Expanded(
          child: Container(
            color: const Color(0xFFBACEE0), // Solid classic KakaoTalk Blue
            child: ListView.builder(
              controller: _scrollController,
              physics: const BouncingScrollPhysics(),
              padding: const EdgeInsets.fromLTRB(16.0, 16.0, 16.0, 100.0), // Hover safety offset
              itemCount: appState.activeChatTimeline.length,
              itemBuilder: (context, index) {
                final message = appState.activeChatTimeline[index];
                final String firstInitial = message.senderName.isNotEmpty ? message.senderName[0].toUpperCase() : 'C';

                return Padding(
                  padding: const EdgeInsets.only(bottom: 20.0),
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // KakaoTalk Circular Profile Avatar Placeholder
                      Container(
                        width: 40.0,
                        height: 40.0,
                        decoration: const BoxDecoration(
                          color: Color(0xFFFFFFFF),
                          shape: BoxShape.shapeCircle,
                        ),
                        child: Center(
                          child: Text(
                            firstInitial,
                            style: const TextStyle(
                              color: Color(0xFF8E8E93),
                              fontWeight: FontWeight.bold,
                              fontSize: 15.0,
                            ),
                          ),
                        ),
                      ),
                      const SizedBox(width: 10.0),

                      // Speech Bubble Block
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            // Header: Name [Demographic Role]
                            Text(
                              '${message.senderName} (${message.role})',
                              style: const TextStyle(
                                color: Color(0xFF4A4A4A),
                                fontSize: 12.0,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            const SizedBox(height: 4.0),

                            // KakaoTalk Signature Golden Bubble Box
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 14.0, vertical: 12.0),
                              decoration: const BoxDecoration(
                                color: Color(0xFFFEE500),
                                borderRadius: BorderRadius.only(
                                  topRight: Radius.circular(14.0),
                                  bottomLeft: Radius.circular(14.0),
                                  bottomRight: Radius.circular(14.0),
                                ),
                              ),
                              child: Text(
                                message.messageText,
                                style: const TextStyle(
                                  color: Color(0xFF191919),
                                  fontSize: 15.0,
                                  height: 1.4,
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                );
              },
            ),
          ),
        ),
      ],
    );
  }

  /// Builds a premium glowing dashboard portal to launch the simulation.
  Widget _buildLaunchPortal(AppStateProvider appState) {
    return Center(
      child: SingleChildScrollView(
        padding: const EdgeInsets.all(32.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(28.0),
              decoration: BoxDecoration(
                color: const Color(0xFF00F2FE).withOpacity(0.04),
                shape: BoxShape.circle,
                border: Border.all(color: const Color(0xFF00F2FE).withOpacity(0.15), width: 2),
              ),
              child: const Icon(Icons.people_alt_rounded, size: 72.0, color: Color(0xFF00F2FE)),
            ),
            const SizedBox(height: 32.0),
            const Text(
              'TOWN HALL DEBATE CORE',
              style: TextStyle(
                color: Colors.white,
                fontWeight: FontWeight.bold,
                fontSize: 20.0,
                letterSpacing: 1.5,
              ),
            ),
            const SizedBox(height: 12.0),
            const Text(
              'Initiate a 10-persona multi-agent evaluation grid. Watch virtual agents debate zoning modifications, traffic pathways, and infrastructure plans in real-time.',
              style: TextStyle(color: Color(0xFF8E8E93), fontSize: 13.0, height: 1.5),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 36.0),

            // Premium Neon Launch CTA Button
            InkWell(
              onTap: () => appState.launchTownHallSimulation(),
              borderRadius: BorderRadius.circular(30.0),
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 36.0, vertical: 16.0),
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    colors: [Color(0xFF00F2FE), Color(0xFF4FACFE)],
                  ),
                  borderRadius: BorderRadius.circular(30.0),
                  boxShadow: [
                    BoxShadow(
                      color: const Color(0xFF00F2FE).withOpacity(0.35),
                      blurRadius: 16.0,
                      offset: const Offset(0, 4.0),
                    ),
                  ],
                ),
                child: const Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(Icons.play_arrow_rounded, color: Color(0xFF141416), size: 20.0),
                    SizedBox(width: 8.0),
                    Text(
                      'START TOWN HALL DEBATE',
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
          ],
        ),
      ),
    );
  }
}

// Helper alert stub view
class _AlertFallbackView extends StatelessWidget {
  final IconData icon;
  final String title;
  final String subtitle;

  const _AlertFallbackView({
    required this.icon,
    required this.title,
    required this.subtitle,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon, size: 56.0, color: const Color(0xFF6C6C7D)),
            const SizedBox(height: 18.0),
            Text(
              title,
              style: const TextStyle(
                color: Colors.white,
                fontWeight: FontWeight.bold,
                fontSize: 18.0,
              ),
            ),
            const SizedBox(height: 8.0),
            Text(
              subtitle,
              style: const TextStyle(color: Color(0xFF6C6C7D), fontSize: 13.0, height: 1.4),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}
