/// CitySmart - Mobile Systems Primary Interface Matrix
/// This file implements the master frame, layout stack, and custom stylized
/// floating navigation menu for the multi-agent simulation application.
/// It integrates smooth cross-fade views and visual styling contracts.

import 'package:flutter/material.dart';

/// The master application frame containing the custom floating menu and view stack.
class RootNavigationScreen extends StatefulWidget {
  const RootNavigationScreen({super.key});

  @override
  State<RootNavigationScreen> createState() => _RootNavigationScreenState();
}

class _RootNavigationScreenState extends State<RootNavigationScreen> {
  int _currentIndex = 0;

  /// Ordered list of layout sub-views within our primary application framework.
  late final List<Widget> _subViews;

  @override
  void initState() {
    super.initState();
    _subViews = [
      const PromptIngestionView(),
      const TownHallSimulationView(),
      const SubmittedProposalView(),
      const VersionBranchingView(),
      const SimulationMetricsView(),
    ];
  }

  /// Sets the selected layout view index with a state rebuild.
  void _onTabSelected(int index) {
    if (_currentIndex == index) return;
    setState(() {
      _currentIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0D0D0F),
      body: SafeArea(
        top: false, // Ensure full bleed representation
        child: Stack(
          children: [
            // 1. Primary view switcher container
            Positioned.fill(
              child: AnimatedSwitcher(
                duration: const Duration(milliseconds: 350),
                switchInCurve: Curves.easeInOut,
                switchOutCurve: Curves.easeInOut,
                transitionBuilder: (Widget child, Animation<double> animation) {
                  return FadeTransition(
                    opacity: animation,
                    child: child,
                  );
                },
                child: SizedBox(
                  key: ValueKey<int>(_currentIndex),
                  child: _subViews[_currentIndex],
                ),
              ),
            ),

            // 2. Stylized floating navigation menu widget
            Positioned(
              bottom: 24.0,
              left: 20.0,
              right: 20.0,
              child: Container(
                padding: const EdgeInsets.symmetric(vertical: 14.0, horizontal: 12.0),
                decoration: BoxDecoration(
                  color: const Color(0xFF141416),
                  borderRadius: BorderRadius.circular(28.0),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.45),
                      blurRadius: 18.0,
                      offset: const Offset(0, 6.0),
                      spreadRadius: 2.0,
                    ),
                  ],
                  border: Border.all(
                    color: Colors.white.withOpacity(0.06),
                    width: 1.0,
                  ),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceAround,
                  children: [
                    _buildNavItem(
                      index: 0,
                      icon: Icons.add_comment_rounded,
                      tooltip: 'Ingestion',
                    ),
                    _buildNavItem(
                      index: 1,
                      icon: Icons.forum_rounded,
                      tooltip: 'Town Hall',
                    ),
                    _buildNavItem(
                      index: 2,
                      icon: Icons.assignment_rounded,
                      tooltip: 'Proposal',
                    ),
                    _buildNavItem(
                      index: 3,
                      icon: Icons.alt_route_rounded,
                      tooltip: 'Branching',
                    ),
                    _buildNavItem(
                      index: 4,
                      icon: Icons.analytics_rounded,
                      tooltip: 'Metrics',
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

  /// Builds a single animated navigation icon.
  Widget _buildNavItem({
    required int index,
    required IconData icon,
    required String tooltip,
  }) {
    final bool isSelected = _currentIndex == index;
    final Color iconColor = isSelected ? const Color(0xFF00F2FE) : const Color(0xFF6C6C7D);

    Widget iconWidget = IconButton(
      onPressed: () => _onTabSelected(index),
      icon: Icon(icon, color: iconColor, size: 26.0),
      tooltip: tooltip,
      splashColor: const Color(0xFF00F2FE).withOpacity(0.15),
      highlightColor: Colors.transparent,
    );

    // Apply scale animation scale factor to selected tab
    if (isSelected) {
      iconWidget = Transform.scale(
        scale: 1.15,
        child: iconWidget,
      );
    }

    return AnimatedContainer(
      duration: const Duration(milliseconds: 250),
      curve: Curves.easeOut,
      child: iconWidget,
    );
  }
}

// ============================================================================
// STYLIZED PREMIUM PLACEHOLDER VIEWS
// ============================================================================

/// Beautiful gradient container style for all sub-views.
class _BaseGradientView extends StatelessWidget {
  final Widget child;

  const _BaseGradientView({required this.child});

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [
            Color(0xFF10111A), // Sleek midnight blue/indigo
            Color(0xFF08080A), // Extremely deep obsidian black
          ],
        ),
      ),
      child: child,
    );
  }
}

/// 1. PromptIngestionView
class PromptIngestionView extends StatelessWidget {
  const PromptIngestionView({super.key});

  @override
  Widget build(BuildContext context) {
    return _BaseGradientView(
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: const Color(0xFF00F2FE).withOpacity(0.08),
                shape: BoxShape.circle,
                border: Border.all(color: const Color(0xFF00F2FE).withOpacity(0.2), width: 1.5),
              ),
              child: const Icon(Icons.add_comment_rounded, size: 64, color: Color(0xFF00F2FE)),
            ),
            const SizedBox(height: 24),
            const Text(
              'PROMPT INGESTION',
              style: TextStyle(
                color: Colors.white,
                fontSize: 22,
                fontWeight: FontWeight.bold,
                letterSpacing: 2,
              ),
            ),
            const SizedBox(height: 8),
            const Text(
              'Formulate baseline system prompt pipeline requests',
              style: TextStyle(color: Color(0xFF6C6C7D), fontSize: 13),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}

/// 2. TownHallSimulationView
class TownHallSimulationView extends StatelessWidget {
  const TownHallSimulationView({super.key});

  @override
  Widget build(BuildContext context) {
    return _BaseGradientView(
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: const Color(0xFF9D4EDD).withOpacity(0.08),
                shape: BoxShape.circle,
                border: Border.all(color: const Color(0xFF9D4EDD).withOpacity(0.2), width: 1.5),
              ),
              child: const Icon(Icons.forum_rounded, size: 64, color: Color(0xFF9D4EDD)),
            ),
            const SizedBox(height: 24),
            const Text(
              'TOWN HALL DEBATE',
              style: TextStyle(
                color: Colors.white,
                fontSize: 22,
                fontWeight: FontWeight.bold,
                letterSpacing: 2,
              ),
            ),
            const SizedBox(height: 8),
            const Text(
              'Simulate live conversational multi-agent feedback streams',
              style: TextStyle(color: Color(0xFF6C6C7D), fontSize: 13),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}

/// 3. SubmittedProposalView
class SubmittedProposalView extends StatelessWidget {
  const SubmittedProposalView({super.key});

  @override
  Widget build(BuildContext context) {
    return _BaseGradientView(
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: const Color(0xFF2EC4B6).withOpacity(0.08),
                shape: BoxShape.circle,
                border: Border.all(color: const Color(0xFF2EC4B6).withOpacity(0.2), width: 1.5),
              ),
              child: const Icon(Icons.assignment_rounded, size: 64, color: Color(0xFF2EC4B6)),
            ),
            const SizedBox(height: 24),
            const Text(
              'PROPOSAL OVERVIEW',
              style: TextStyle(
                color: Colors.white,
                fontSize: 22,
                fontWeight: FontWeight.bold,
                letterSpacing: 2,
              ),
            ),
            const SizedBox(height: 8),
            const Text(
              'Review current baseline proposals and system outputs',
              style: TextStyle(color: Color(0xFF6C6C7D), fontSize: 13),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}

/// 4. VersionBranchingView
class VersionBranchingView extends StatelessWidget {
  const VersionBranchingView({super.key});

  @override
  Widget build(BuildContext context) {
    return _BaseGradientView(
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: const Color(0xFFFF9F1C).withOpacity(0.08),
                shape: BoxShape.circle,
                border: Border.all(color: const Color(0xFFFF9F1C).withOpacity(0.2), width: 1.5),
              ),
              child: const Icon(Icons.alt_route_rounded, size: 64, color: Color(0xFFFF9F1C)),
            ),
            const SizedBox(height: 24),
            const Text(
              'VERSION BRANCHING',
              style: TextStyle(
                color: Colors.white,
                fontSize: 22,
                fontWeight: FontWeight.bold,
                letterSpacing: 2,
              ),
            ),
            const SizedBox(height: 8),
            const Text(
              'Modify variant proposals and branch simulation states',
              style: TextStyle(color: Color(0xFF6C6C7D), fontSize: 13),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}

/// 5. SimulationMetricsView
class SimulationMetricsView extends StatelessWidget {
  const SimulationMetricsView({super.key});

  @override
  Widget build(BuildContext context) {
    return _BaseGradientView(
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: const Color(0xFFFF5D73).withOpacity(0.08),
                shape: BoxShape.circle,
                border: Border.all(color: const Color(0xFFFF5D73).withOpacity(0.2), width: 1.5),
              ),
              child: const Icon(Icons.analytics_rounded, size: 64, color: Color(0xFFFF5D73)),
            ),
            const SizedBox(height: 24),
            const Text(
              'SIMULATION ANALYTICS',
              style: TextStyle(
                color: Colors.white,
                fontSize: 22,
                fontWeight: FontWeight.bold,
                letterSpacing: 2,
              ),
            ),
            const SizedBox(height: 8),
            const Text(
              'Evaluate feasibility scores and legal arbitrator verdicts',
              style: TextStyle(color: Color(0xFF6C6C7D), fontSize: 13),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}
