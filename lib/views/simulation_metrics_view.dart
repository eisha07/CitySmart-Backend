/// CitySmart - Mobile Systems Simulation Metrics View
/// This file implements Page 5: The Simulation Metrics View. It maps
/// custom visual parameter gauges, color scale allocations, and full-width
/// scrollable markdown analytical summaries on a unified dashboard.

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:flutter_markdown/flutter_markdown.dart';
import '../providers/app_state_provider.dart';

/// Page 5: Simulation Metrics dashboard displaying data metrics.
class SimulationMetricsView extends StatelessWidget {
  const SimulationMetricsView({super.key});

  @override
  Widget build(BuildContext context) {
    final appState = Provider.of<AppStateProvider>(context);
    final simulation = appState.simulationData;

    return Scaffold(
      backgroundColor: const Color(0xFF0D0D0F),
      body: SafeArea(
        child: Column(
          children: [
            _buildHeader(appState),
            Expanded(
              child: _buildDashboardBody(appState, simulation),
            ),
          ],
        ),
      ),
    );
  }

  /// Builds the top title context banner.
  Widget _buildHeader(AppStateProvider appState) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 14.0),
      decoration: const BoxDecoration(
        color: Color(0xFF141416),
        border: Border(bottom: BorderSide(color: Colors.white10, width: 1.0)),
      ),
      child: const Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'ANALYTICS ENGINE CORE',
            style: TextStyle(
              color: Color(0xFFFF5D73),
              fontWeight: FontWeight.bold,
              fontSize: 10.0,
              letterSpacing: 1.2,
            ),
          ),
          SizedBox(height: 4.0),
          Text(
            'Urban Feasibility Dashboard',
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

  /// Builds the main dashboard body or locked states.
  Widget _buildDashboardBody(AppStateProvider appState, dynamic simulation) {
    if (appState.currentProjectId == null || simulation == null) {
      return const Center(
        child: Padding(
          padding: EdgeInsets.all(32.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(Icons.analytics_rounded, size: 56.0, color: Color(0xFF6C6C7D)),
              const SizedBox(height: 18.0),
              Text(
                'Metrics Dashboard Locked',
                style: TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                  fontSize: 18.0,
                ),
              ),
              const SizedBox(height: 8.0),
              Text(
                'Please launch and complete a Town Hall simulation debate on Page 2 first to generate metrics analytics.',
                style: TextStyle(color: Color(0xFF6C6C7D), fontSize: 13.0, height: 1.4),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      );
    }

    final int social = simulation.socialFeasibilityScore;
    final int economic = simulation.economicViabilityScore;
    final int political = simulation.politicalAcceptanceScore;
    final String verdict = simulation.arbitratorVerdict;

    return SingleChildScrollView(
      physics: const BouncingScrollPhysics(),
      padding: const EdgeInsets.fromLTRB(16.0, 20.0, 16.0, 100.0), // Hover safety padding
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // Upper Boundary: Metrics Gauges Row
          Row(
            children: [
              MetricGauge(
                title: 'Social',
                score: social,
              ),
              MetricGauge(
                title: 'Economic',
                score: economic,
              ),
              MetricGauge(
                title: 'Political',
                score: political,
              ),
            ],
          ),
          const SizedBox(height: 24.0),

          // Lower Section: Wide Padded Markdown Card
          Container(
            padding: const EdgeInsets.all(20.0),
            decoration: BoxDecoration(
              color: const Color(0xFF141416),
              borderRadius: BorderRadius.circular(20.0),
              border: Border.all(
                color: Colors.white.withOpacity(0.06),
                width: 1.0,
              ),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const Row(
                  children: [
                    Icon(
                      Icons.auto_awesome_rounded,
                      color: Color(0xFF00F2FE),
                      size: 20.0,
                    ),
                    SizedBox(width: 8.0),
                    Text(
                      'ARBITRATOR POLICY VERDICT',
                      style: TextStyle(
                        color: Color(0xFF00F2FE),
                        fontWeight: FontWeight.bold,
                        fontSize: 12.0,
                        letterSpacing: 1.0,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16.0),
                MarkdownBody(
                  data: verdict.isNotEmpty ? verdict : '*No policy summaries generated.*',
                  styleSheet: MarkdownStyleSheet(
                    p: TextStyle(
                      color: Colors.white.withOpacity(0.85),
                      fontSize: 14.0,
                      height: 1.6,
                    ),
                    h1: const TextStyle(
                      color: Color(0xFF00F2FE),
                      fontSize: 18.0,
                      fontWeight: FontWeight.bold,
                      height: 1.5,
                    ),
                    h2: const TextStyle(
                      color: Color(0xFF00F2FE),
                      fontSize: 16.0,
                      fontWeight: FontWeight.bold,
                      height: 1.5,
                    ),
                    h3: const TextStyle(
                      color: Colors.white,
                      fontSize: 15.0,
                      fontWeight: FontWeight.bold,
                      height: 1.5,
                    ),
                    listBullet: const TextStyle(
                      color: Color(0xFF00F2FE),
                      fontSize: 14.0,
                    ),
                    blockquoteDecoration: BoxDecoration(
                      color: Colors.white.withOpacity(0.04),
                      borderRadius: BorderRadius.circular(8.0),
                      border: const Border(
                        left: BorderSide(
                          color: Color(0xFF00F2FE),
                          width: 3.0,
                        ),
                      ),
                    ),
                    blockquote: TextStyle(
                      color: Colors.white.withOpacity(0.65),
                      fontSize: 13.5,
                      fontStyle: FontStyle.italic,
                    ),
                    strong: const TextStyle(
                      color: Color(0xFF00F2FE),
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

/// Custom parameter gauge displaying progress percentage bar with color grading.
class MetricGauge extends StatelessWidget {
  final String title;
  final int score;

  const MetricGauge({
    super.key,
    required this.title,
    required this.score,
  });

  /// Explicit color grading evaluation mapping algorithm
  Color _getEvaluatedColor(int value) {
    if (value < 50) return const Color(0xFFEF4444); // Crimson warning
    if (value <= 70) return const Color(0xFFF59E0B); // Amber warning
    return const Color(0xFF10B981); // Emerald green
  }

  @override
  Widget build(BuildContext context) {
    final Color trackColor = _getEvaluatedColor(score);

    return Expanded(
      child: Container(
        margin: const EdgeInsets.symmetric(horizontal: 5.0),
        padding: const EdgeInsets.symmetric(horizontal: 10.0, vertical: 16.0),
        decoration: BoxDecoration(
          color: const Color(0xFF141416),
          borderRadius: BorderRadius.circular(18.0),
          border: Border.all(
            color: Colors.white.withOpacity(0.05),
            width: 1.0,
          ),
        ),
        child: Column(
          children: [
            Text(
              title.toUpperCase(),
              style: const TextStyle(
                color: Color(0xFF6C6C7D),
                fontSize: 10.0,
                fontWeight: FontWeight.bold,
                letterSpacing: 1.2,
              ),
              textAlign: TextAlign.center,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
            const SizedBox(height: 12.0),
            Text(
              '$score%',
              style: TextStyle(
                color: trackColor,
                fontSize: 26.0,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16.0),

            // Underlying Custom Progress Tracking Bar (fractional flex representation)
            Container(
              height: 5.0,
              width: double.infinity,
              decoration: BoxDecoration(
                color: Colors.white.withOpacity(0.06),
                borderRadius: BorderRadius.circular(3.0),
              ),
              child: Row(
                children: [
                  Expanded(
                    flex: score.clamp(0, 100),
                    child: Container(
                      decoration: BoxDecoration(
                        color: trackColor,
                        borderRadius: BorderRadius.circular(3.0),
                        boxShadow: [
                          BoxShadow(
                            color: trackColor.withOpacity(0.35),
                            blurRadius: 8.0,
                          ),
                        ],
                      ),
                    ),
                  ),
                  Expanded(
                    flex: (100 - score).clamp(0, 100),
                    child: const SizedBox.shrink(),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
