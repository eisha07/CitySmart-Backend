/// CitySmart - Mobile Systems Initialization Entrypoint
/// This file bootstraps the Flutter application. It hooks up native widgets bindings,
/// configures a global dark visual theme framework, and initializes MultiProvider
/// state layers.

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/app_state_provider.dart';
import 'views/root_navigation_screen.dart';

void main() {
  // Ensure native bindings are fully initialized before running execution loops
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const CitySmartApp());
}

/// The root application widget wrapping providers and MaterialApp themes.
class CitySmartApp extends StatelessWidget {
  const CitySmartApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider<AppStateProvider>(
          create: (_) => AppStateProvider(),
        ),
      ],
      child: MaterialApp(
        title: 'CitySmart Multi-Agent Simulation',
        debugShowCheckedModeBanner: false,
        // Enforce modern, customized high-contrast dark visual profile
        theme: ThemeData(
          brightness: Brightness.dark,
          primaryColor: const Color(0xFF00F2FE),
          scaffoldBackgroundColor: const Color(0xFF0D0D0F),
          colorScheme: const ColorScheme.dark(
            primary: Color(0xFF00F2FE),
            secondary: Color(0xFFFF9F1C),
            background: Color(0xFF0D0D0F),
            surface: Color(0xFF141416),
            error: Color(0xFFFF5D73),
          ),
          textTheme: const TextTheme(
            bodyLarge: TextStyle(color: Colors.white, fontSize: 15.0),
            bodyMedium: TextStyle(color: Color(0xFFC7C7CD), fontSize: 14.0),
          ),
          dividerTheme: const DividerThemeData(
            color: Colors.white10,
            thickness: 1.0,
          ),
        ),
        home: const RootNavigationScreen(),
      ),
    );
  }
}
