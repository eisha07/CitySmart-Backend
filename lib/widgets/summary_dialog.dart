/// CitySmart - Mobile Systems Summary Dialog Widget
/// This file implements the glassmorphic modal popup overlay.
/// It renders the final multi-agent debate synthesis from our arbitrator
/// utilizing dynamic Markdown rendering overlays and heavy backdrop filtering blurs.

import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:flutter_markdown/flutter_markdown.dart';
import '../providers/app_state_provider.dart';

/// Glassmorphic summary report dialog displaying final arbitration verdicts.
class SummaryDialog extends StatelessWidget {
  const SummaryDialog({super.key});

  @override
  Widget build(BuildContext context) {
    final appState = Provider.of<AppStateProvider>(context);
    final String verdict = appState.simulationData?.arbitratorVerdict ?? '';

    return Dialog(
      backgroundColor: Colors.transparent,
      insetPadding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 40.0),
      child: BackdropFilter(
        // High-fidelity vertical and horizontal image filter blur index
        filter: ImageFilter.blur(sigmaX: 12.0, sigmaY: 12.0),
        child: Container(
          padding: const EdgeInsets.all(24.0),
          decoration: BoxDecoration(
            // Semi-transparent obsidian card container tint
            color: Colors.black.withOpacity(0.65),
            borderRadius: BorderRadius.circular(28.0),
            // Clean glass perimeter border highlights
            border: Border.all(
              color: Colors.white.withOpacity(0.15),
              width: 1.0,
            ),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.5),
                blurRadius: 24.0,
                offset: const Offset(0, 10.0),
              ),
            ],
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // 1. Header Section
              Row(
                children: [
                  const Icon(
                    Icons.auto_awesome_rounded, // AI Sparkles Indicator
                    color: Color(0xFF00F2FE),
                    size: 24.0,
                  ),
                  const SizedBox(width: 12.0),
                  Expanded(
                    child: Text(
                      'Urban Arbitrator Synthesis Report',
                      style: TextStyle(
                        color: const Color(0xFF00F2FE),
                        fontWeight: FontWeight.bold,
                        fontSize: 20.0,
                        shadows: [
                          Shadow(
                            color: const Color(0xFF00F2FE).withOpacity(0.35),
                            blurRadius: 12.0,
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16.0),
              Container(
                height: 1.0,
                color: Colors.white.withOpacity(0.08),
              ),
              const SizedBox(height: 18.0),

              // 2. Report Content Window (Expanded Height Bounds)
              Flexible(
                child: ConstrainedBox(
                  constraints: const BoxConstraints(maxHeight: 420.0),
                  child: SingleChildScrollView(
                    physics: const BouncingScrollPhysics(),
                    child: MarkdownBody(
                      data: verdict.isNotEmpty 
                          ? verdict 
                          : '## No Verdict Generated\n\nThe simulation arbitrator has not returned a rich synthesis verdict.',
                      styleSheet: MarkdownStyleSheet(
                        p: TextStyle(
                          color: Colors.white.withOpacity(0.9),
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
                          color: Colors.white.withOpacity(0.7),
                          fontSize: 13.5,
                          fontStyle: FontStyle.italic,
                        ),
                        strong: const TextStyle(
                          color: Color(0xFF00F2FE),
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 24.0),

              // 3. Dismiss Blueprint Button
              Row(
                mainAxisAlignment: MainAxisAlignment.end,
                children: [
                  TextButton(
                    onPressed: () => Navigator.of(context).pop(),
                    style: TextButton.styleFrom(
                      padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 12.0),
                      backgroundColor: Colors.white.withOpacity(0.05),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(16.0),
                        side: BorderSide(
                          color: Colors.white.withOpacity(0.08),
                          width: 1.0,
                        ),
                      ),
                    ),
                    child: const Text(
                      'Dismiss Report',
                      style: TextStyle(
                        color: Color(0xFF00F2FE),
                        fontWeight: FontWeight.bold,
                        fontSize: 13.0,
                        letterSpacing: 0.5,
                      ),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
