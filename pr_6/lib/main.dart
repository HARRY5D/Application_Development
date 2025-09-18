import 'package:flutter/material.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Temperature Converter',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'Temperature Converter'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  final List<String> _units = ['Celsius', 'Fahrenheit', 'Kelvin'];
  String? _sourceUnit;
  String? _targetUnit;
  String _inputValue = '';
  String _convertedValue = '';
  final TextEditingController _inputController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _sourceUnit = _units[0]; // Default to Celsius
    _targetUnit = _units[1]; // Default to Fahrenheit
    _inputController.addListener(_convertTemperature);
  }

  @override
  void dispose() {
    _inputController.dispose();
    super.dispose();
  }

  void _convertTemperature() {
    setState(() {
      _inputValue = _inputController.text;
      if (_inputValue.isEmpty) {
        _convertedValue = '';
        return;
      }

      double? inputTemp = double.tryParse(_inputValue);
      if (inputTemp == null || _sourceUnit == null || _targetUnit == null) {
        _convertedValue = 'Invalid input';
        return;
      }

      if (_sourceUnit == _targetUnit) {
        _convertedValue = inputTemp.toStringAsFixed(2);
        return;
      }

      double result;

      // Convert source to Celsius first
      double tempInCelsius;
      if (_sourceUnit == 'Fahrenheit') {
        tempInCelsius = (inputTemp - 32) * 5 / 9;
      } else if (_sourceUnit == 'Kelvin') {
        tempInCelsius = inputTemp - 273.15;
      } else { // Celsius
        tempInCelsius = inputTemp;
      }

      // Convert from Celsius to target unit
      if (_targetUnit == 'Fahrenheit') {
        result = (tempInCelsius * 9 / 5) + 32;
      } else if (_targetUnit == 'Kelvin') {
        result = tempInCelsius + 273.15;
      } else { // Celsius
        result = tempInCelsius;
      }
      _convertedValue = result.toStringAsFixed(2);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: <Widget>[
            TextField(
              controller: _inputController,
              keyboardType: const TextInputType.numberWithOptions(decimal: true),
              decoration: const InputDecoration(
                labelText: 'Enter Temperature',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 20),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: <Widget>[
                Expanded(
                  child: DropdownButtonFormField<String>(
                    decoration: const InputDecoration(labelText: 'From'),
                    value: _sourceUnit,
                    items: _units.map((String unit) {
                      return DropdownMenuItem<String>(
                        value: unit,
                        child: Text(unit),
                      );
                    }).toList(),
                    onChanged: (String? newValue) {
                      setState(() {
                        _sourceUnit = newValue;
                        _convertTemperature();
                      });
                    },
                  ),
                ),
                const SizedBox(width: 20),
                Expanded(
                  child: DropdownButtonFormField<String>(
                    decoration: const InputDecoration(labelText: 'To'),
                    value: _targetUnit,
                    items: _units.map((String unit) {
                      return DropdownMenuItem<String>(
                        value: unit,
                        child: Text(unit),
                      );
                    }).toList(),
                    onChanged: (String? newValue) {
                      setState(() {
                        _targetUnit = newValue;
                        _convertTemperature();
                      });
                    },
                  ),
                ),
              ],
            ),
            const SizedBox(height: 30),
            if (_convertedValue.isNotEmpty)
              Text(
                'Result: $_convertedValue ${_targetUnit ?? ''}',
                style: Theme.of(context).textTheme.headlineMedium,
                textAlign: TextAlign.center,
              ),
          ],
        ),
      ),
    );
  }
}
