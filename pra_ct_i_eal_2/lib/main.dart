import 'package:flutter/material.dart';
import 'package:sqflite/sqflite.dart';
import 'package:path_provider/path_provider.dart';
import 'package:file_picker/file_picker.dart';
import 'package:intl/intl.dart';
import 'package:path/path.dart' as p; // Alias for path package
import 'dart:io';

// -------------------- Models -------------------- 
class Assignment {
  int? id;
  String title;
  String description;
  DateTime deadline;
  String courseCode;
  String? facultyFilePath; // Path to the assignment document uploaded by faculty
  String? studentSolutionPath; // Path to the solution uploaded by student
  String status; // "Pending", "Submitted", "Overdue"

  Assignment({
    this.id,
    required this.title,
    required this.description,
    required this.deadline,
    required this.courseCode,
    this.facultyFilePath,
    this.studentSolutionPath,
    this.status = 'Pending',
  });

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'title': title,
      'description': description,
      'deadline': deadline.toIso8601String(), // Store as ISO8601 string
      'courseCode': courseCode,
      'facultyFilePath': facultyFilePath,
      'studentSolutionPath': studentSolutionPath,
      'status': status,
    };
  }

  factory Assignment.fromMap(Map<String, dynamic> map) {
    return Assignment(
      id: map['id'] as int?,
      title: map['title'] as String,
      description: map['description'] as String,
      deadline: DateTime.parse(map['deadline'] as String), // Parse from ISO8601 string
      courseCode: map['courseCode'] as String,
      facultyFilePath: map['facultyFilePath'] as String?,
      studentSolutionPath: map['studentSolutionPath'] as String?,
      status: map['status'] as String,
    );
  }

  // Helper to update status based on deadline
  void updateOverdueStatus() {
    if (status == 'Pending' && DateTime.now().isAfter(deadline)) {
      status = 'Overdue';
    }
  }
}

// -------------------- Database Helper -------------------- 
class DatabaseHelper {
  static const _databaseName = "AssignmentDatabase.db";
  static const _databaseVersion = 1;

  static const String tableAssignments = 'assignments';
  static const String columnId = 'id';
  static const String columnTitle = 'title';
  static const String columnDescription = 'description';
  static const String columnDeadline = 'deadline';
  static const String columnCourseCode = 'courseCode';
  static const String columnFacultyFilePath = 'facultyFilePath';
  static const String columnStudentSolutionPath = 'studentSolutionPath';
  static const String columnStatus = 'status';

  DatabaseHelper._privateConstructor();
  static final DatabaseHelper instance = DatabaseHelper._privateConstructor();

  static Database? _database;
  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  Future<Database> _initDatabase() async {
    Directory documentsDirectory = await getApplicationDocumentsDirectory();
    String path = p.join(documentsDirectory.path, _databaseName);
    return await openDatabase(path,
        version: _databaseVersion, onCreate: _onCreate);
  }

  Future<void> _onCreate(Database db, int version) async {
    await db.execute('''
          CREATE TABLE $tableAssignments (
            $columnId INTEGER PRIMARY KEY AUTOINCREMENT,
            $columnTitle TEXT NOT NULL,
            $columnDescription TEXT NOT NULL,
            $columnDeadline TEXT NOT NULL,
            $columnCourseCode TEXT NOT NULL,
            $columnFacultyFilePath TEXT,
            $columnStudentSolutionPath TEXT,
            $columnStatus TEXT NOT NULL
          )
          ''');
  }

  Future<int> insertAssignment(Assignment assignment) async {
    Database db = await database;
    return await db.insert(tableAssignments, assignment.toMap());
  }

  Future<List<Assignment>> getAllAssignments() async {
    Database db = await database;
    final List<Map<String, dynamic>> maps = await db.query(tableAssignments, orderBy: '$columnDeadline ASC');
    return List.generate(maps.length, (i) {
      Assignment assignment = Assignment.fromMap(maps[i]);
      assignment.updateOverdueStatus(); // Check if overdue upon fetching
      return assignment;
    });
  }
  
  Future<Assignment?> getAssignmentById(int id) async {
    Database db = await database;
    List<Map<String, dynamic>> maps = await db.query(tableAssignments,
        where: '$columnId = ?', whereArgs: [id], limit: 1);
    if (maps.isNotEmpty) {
      Assignment assignment = Assignment.fromMap(maps.first);
      assignment.updateOverdueStatus();
      return assignment;
    }
    return null;
  }

  Future<int> updateAssignment(Assignment assignment) async {
    Database db = await database;
    return await db.update(tableAssignments, assignment.toMap(),
        where: '$columnId = ?', whereArgs: [assignment.id]);
  }

  Future<int> deleteAssignment(int id) async {
    Database db = await database;
    return await db.delete(tableAssignments, where: '$columnId = ?', whereArgs: [id]);
  }
}

// -------------------- Main Application -------------------- 
void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await DatabaseHelper.instance.database; // Initialize DB
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Assignment Submission App',
      theme: ThemeData(
        primarySwatch: Colors.indigo,
        colorScheme: ColorScheme.fromSwatch(
          primarySwatch: Colors.indigo,
          accentColor: Colors.pinkAccent,
          brightness: Brightness.light,
        ).copyWith(secondary: Colors.pinkAccent),
        useMaterial3: true,
        textTheme: const TextTheme(
          titleLarge: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: Colors.indigo),
          bodyMedium: TextStyle(fontSize: 16),
          labelLarge: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white),
        ),
        cardTheme: const CardThemeData( // Changed from CardTheme to CardThemeData and added const
          elevation: 4,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
          margin: EdgeInsets.symmetric(vertical: 8, horizontal: 10),
        ),
        elevatedButtonTheme: ElevatedButtonThemeData(
          style: ElevatedButton.styleFrom(
            backgroundColor: Colors.indigo,
            foregroundColor: Colors.white,
            padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 20),
            textStyle: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
          ),
        ),
        appBarTheme: const AppBarTheme( // Added const
          backgroundColor: Colors.indigo,
          foregroundColor: Colors.white,
          titleTextStyle: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.white),
        ),
        inputDecorationTheme: InputDecorationTheme(
          border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
          focusedBorder: OutlineInputBorder(
            borderSide: const BorderSide(color: Colors.indigo, width: 2.0),
            borderRadius: BorderRadius.circular(8),
          ),
          labelStyle: const TextStyle(color: Colors.indigo),
        ),
      ),
      home: const RoleSelectionScreen(),
    );
  }
}

// -------------------- Role Selection Screen -------------------- 
class RoleSelectionScreen extends StatelessWidget {
  const RoleSelectionScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Select Your Role'),
      ),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: <Widget>[
              ElevatedButton(
                child: const Text('I am a Student'),
                onPressed: () {
                  Navigator.of(context).pushReplacement(
                    MaterialPageRoute(builder: (_) => const StudentAssignmentListScreen()),
                  );
                },
              ),
              const SizedBox(height: 20),
              ElevatedButton(
                child: const Text('I am Faculty'),
                onPressed: () {
                  Navigator.of(context).pushReplacement(
                    MaterialPageRoute(builder: (_) => const FacultyCreateAssignmentScreen()),
                  );
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}

// -------------------- Student: Assignment List Screen -------------------- 
class StudentAssignmentListScreen extends StatefulWidget {
  const StudentAssignmentListScreen({super.key});

  @override
  State<StudentAssignmentListScreen> createState() => _StudentAssignmentListScreenState();
}

class _StudentAssignmentListScreenState extends State<StudentAssignmentListScreen> {
  late Future<List<Assignment>> _assignmentsFuture;

  @override
  void initState() {
    super.initState();
    _loadAssignments();
  }

  void _loadAssignments() {
    setState(() {
      _assignmentsFuture = DatabaseHelper.instance.getAllAssignments();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Your Assignments'),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            tooltip: 'Change Role',
            onPressed: () {
               Navigator.of(context).pushReplacement(
                  MaterialPageRoute(builder: (_) => const RoleSelectionScreen()),
                );
            },
          )
        ],
      ),
      body: FutureBuilder<List<Assignment>>(
        future: _assignmentsFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snapshot.hasError) {
            return Center(child: Text('Error: ${snapshot.error}'));
          }
          if (!snapshot.hasData || snapshot.data!.isEmpty) {
            return const Center(child: Text('No assignments found.'));
          }
          final assignments = snapshot.data!;
          return ListView.builder(
            padding: const EdgeInsets.all(8.0),
            itemCount: assignments.length,
            itemBuilder: (context, index) {
              return AssignmentCard(assignment: assignments[index], onNavigate: () => _navigateToDetail(context, assignments[index]));
            },
          );
        },
      ),
    );
  }
  void _navigateToDetail(BuildContext context, Assignment assignment) async {
    final result = await Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => AssignmentDetailScreen(assignment: assignment),
      ),
    );
    if (result == true) { // If an update happened on detail screen
      _loadAssignments(); // Reload the list
    }
  }
}


// -------------------- Faculty: Create Assignment Screen -------------------- 
class FacultyCreateAssignmentScreen extends StatefulWidget {
  const FacultyCreateAssignmentScreen({super.key});

  @override
  State<FacultyCreateAssignmentScreen> createState() => _FacultyCreateAssignmentScreenState();
}

class _FacultyCreateAssignmentScreenState extends State<FacultyCreateAssignmentScreen> {
  final _formKey = GlobalKey<FormState>();
  final _titleController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _courseCodeController = TextEditingController();
  DateTime? _selectedDeadline;
  String? _pickedFilePath;

  Future<void> _pickDeadline(BuildContext context) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: _selectedDeadline ?? DateTime.now(),
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 365)),
    );
    if (picked != null && picked != _selectedDeadline) {
      // Check if context is still valid before using it across async gap
      if (!mounted) return;
      final TimeOfDay? time = await showTimePicker(
          context: context, 
          initialTime: TimeOfDay.fromDateTime(_selectedDeadline ?? DateTime.now()),
      );
      if (time != null) {
        setState(() {
          _selectedDeadline = DateTime(picked.year, picked.month, picked.day, time.hour, time.minute);
        });
      }
    }
  }

  Future<void> _pickFile() async {
    FilePickerResult? result = await FilePicker.platform.pickFiles(
      type: FileType.custom,
      allowedExtensions: ['pdf', 'doc', 'docx', 'jpg', 'png', 'txt'],
    );
    if (result != null) {
      setState(() {
        _pickedFilePath = result.files.single.path;
      });
    } else {
      // User canceled the picker
    }
  }

  Future<void> _createAssignment(BuildContext context) async {
    if (_formKey.currentState!.validate()) {
      if (_selectedDeadline == null) {
        // Check if context is still valid
        if (!mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Please select a deadline.')),
        );
        return;
      }

      final newAssignment = Assignment(
        title: _titleController.text,
        description: _descriptionController.text,
        deadline: _selectedDeadline!,
        courseCode: _courseCodeController.text,
        facultyFilePath: _pickedFilePath,
        status: 'Pending',
      );

      await DatabaseHelper.instance.insertAssignment(newAssignment);
      // Check if context is still valid
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Assignment created successfully!')),
      );
      _formKey.currentState!.reset();
      _titleController.clear();
      _descriptionController.clear();
      _courseCodeController.clear();
      setState(() {
        _selectedDeadline = null;
        _pickedFilePath = null;
      });
       // Optionally navigate away or refresh a list if displaying created assignments here
    }
  }
  
  @override
  void dispose() {
    _titleController.dispose();
    _descriptionController.dispose();
    _courseCodeController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Create New Assignment'),
         actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            tooltip: 'Change Role',
            onPressed: () {
               Navigator.of(context).pushReplacement(
                  MaterialPageRoute(builder: (_) => const RoleSelectionScreen()),
                );
            },
          )
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Form(
          key: _formKey,
          child: ListView(
            children: <Widget>[
              TextFormField(
                controller: _titleController,
                decoration: const InputDecoration(labelText: 'Title'),
                validator: (value) => value == null || value.isEmpty ? 'Please enter a title' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _descriptionController,
                decoration: const InputDecoration(labelText: 'Description'),
                maxLines: 3,
                validator: (value) => value == null || value.isEmpty ? 'Please enter a description' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _courseCodeController,
                decoration: const InputDecoration(labelText: 'Course Code'),
                validator: (value) => value == null || value.isEmpty ? 'Please enter a course code' : null,
              ),
              const SizedBox(height: 20),
              Row(
                children: <Widget>[
                  Expanded(
                    child: Text(
                      _selectedDeadline == null
                          ? 'No deadline selected'
                          : 'Deadline: ${DateFormat.yMMMd().add_jm().format(_selectedDeadline!)}',
                    ),
                  ),
                  TextButton(
                    onPressed: () => _pickDeadline(context), // Pass context
                    child: const Text('SELECT DEADLINE'),
                  ),
                ],
              ),
              const SizedBox(height: 12),
               Row(
                children: <Widget>[
                  Expanded(
                    child: Text(
                      _pickedFilePath == null
                          ? 'No assignment file selected'
                          : 'File: ${p.basename(_pickedFilePath!)}',
                       overflow: TextOverflow.ellipsis,
                    ),
                  ),
                  TextButton(
                    onPressed: _pickFile,
                    child: const Text('UPLOAD FILE'),
                  ),
                ],
              ),
              const SizedBox(height: 24),
              ElevatedButton(
                onPressed: () => _createAssignment(context), // Pass context
                child: const Text('CREATE ASSIGNMENT'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

// -------------------- Assignment Detail/Submission Screen (Student) -------------------- 
class AssignmentDetailScreen extends StatefulWidget {
  final Assignment assignment;

  const AssignmentDetailScreen({super.key, required this.assignment});

  @override
  State<AssignmentDetailScreen> createState() => _AssignmentDetailScreenState();
}

class _AssignmentDetailScreenState extends State<AssignmentDetailScreen> {
  String? _studentSubmissionPath;
  late Assignment _currentAssignment;

  @override
  void initState() {
    super.initState();
    _currentAssignment = widget.assignment;
    _studentSubmissionPath = _currentAssignment.studentSolutionPath;
  }

  Future<void> _pickSolutionFile() async {
    FilePickerResult? result = await FilePicker.platform.pickFiles(
       type: FileType.custom,
      allowedExtensions: ['pdf', 'doc', 'docx', 'jpg', 'png', 'txt'],
    );
    if (result != null) {
      setState(() {
        _studentSubmissionPath = result.files.single.path;
      });
    } 
  }

  Future<void> _submitSolution(BuildContext context) async {
    if (_studentSubmissionPath == null) {
      // Check if context is still valid
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please select a solution file to submit.')),
      );
      return;
    }
    
    _currentAssignment.studentSolutionPath = _studentSubmissionPath;
    _currentAssignment.status = 'Submitted';

    await DatabaseHelper.instance.updateAssignment(_currentAssignment);
    // Check if context is still valid
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Assignment submitted successfully!')),
    );
    setState(() {}); // Rebuild to reflect status change
    // Check if context is still valid
    if (!mounted) return;
    Navigator.of(context).pop(true); // Pop and indicate an update happened
  }

  @override
  Widget build(BuildContext context) {
    // Re-check status in case it became overdue while viewing
    if (_currentAssignment.status == 'Pending' && DateTime.now().isAfter(_currentAssignment.deadline)) {
        _currentAssignment.status = 'Overdue';
    }

    bool canSubmit = _currentAssignment.status == 'Pending' || _currentAssignment.status == 'Overdue';
    if (_currentAssignment.status == 'Overdue' && _currentAssignment.studentSolutionPath != null) {
        // If overdue but already submitted (e.g. status was manually changed after submission), treat as submitted for UI
        canSubmit = false;
    }

    return Scaffold(
      appBar: AppBar(
        title: Text(_currentAssignment.title),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: ListView(
          children: <Widget>[
            Text('Course Code: ${_currentAssignment.courseCode}', style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 8),
            Text('Description:', style: Theme.of(context).textTheme.titleMedium),
            Text(_currentAssignment.description, style: Theme.of(context).textTheme.bodyLarge),
            const SizedBox(height: 16),
            Text(
              'Deadline: ${DateFormat.yMMMd().add_jm().format(_currentAssignment.deadline)}',
              style: TextStyle(
                fontWeight: FontWeight.bold,
                color: _currentAssignment.status == 'Overdue' && _currentAssignment.studentSolutionPath == null 
                       ? Colors.red 
                       : Theme.of(context).textTheme.bodyLarge?.color,
              ),
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                Text('Status: ', style: Theme.of(context).textTheme.titleMedium),
                AssignmentStatusChip(status: _currentAssignment.status), 
              ],
            ),
            const SizedBox(height: 16),
            if (_currentAssignment.facultyFilePath != null)
              Card(
                child: ListTile(
                  leading: const Icon(Icons.attach_file, color: Colors.indigo),
                  title: Text('Assignment File: ${p.basename(_currentAssignment.facultyFilePath!)}'),
                  subtitle: Text(_currentAssignment.facultyFilePath!),
                  // TODO: Implement file opening/downloading logic if needed
                  onTap: () { 
                     ScaffoldMessenger.of(context).showSnackBar(
                       SnackBar(content: Text('Viewing/Downloading faculty file: ${p.basename(_currentAssignment.facultyFilePath!)}\n(Feature not fully implemented)')),
                    );
                  },
                ),
              ),
            const SizedBox(height: 20),
            if (canSubmit)
              Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Text('Submit Your Solution:', style: Theme.of(context).textTheme.titleMedium),
                  const SizedBox(height: 8),
                  Row(
                    children: <Widget>[
                      Expanded(
                        child: Text(
                          _studentSubmissionPath == null
                              ? 'No solution file selected'
                              : 'Selected: ${p.basename(_studentSubmissionPath!)}',
                           overflow: TextOverflow.ellipsis,
                        ),
                      ),
                      TextButton(
                        onPressed: _pickSolutionFile,
                        child: const Text('PICK SOLUTION FILE'),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: _studentSubmissionPath != null ? () => _submitSolution(context) : null, // Pass context
                    child: const Text('SUBMIT ASSIGNMENT'),
                  ),
                ],
              ),
            if (_currentAssignment.status == 'Submitted' && _currentAssignment.studentSolutionPath != null)
              Card(
                color: Colors.green[50],
                child: ListTile(
                   leading: const Icon(Icons.check_circle, color: Colors.green),
                   title: Text('You have submitted: ${p.basename(_currentAssignment.studentSolutionPath!)}'),
                   subtitle: Text(_currentAssignment.studentSolutionPath!),
                   // TODO: Allow re-submission or viewing submitted file details
                )
              ),
          ],
        ),
      ),
    );
  }
}

// -------------------- Widgets -------------------- 
class AssignmentCard extends StatelessWidget {
  final Assignment assignment;
  final void Function(BuildContext context, Assignment assignment) onNavigate; // Adjusted to pass context and assignment

  const AssignmentCard({super.key, required this.assignment, required this.onNavigate});

  @override
  Widget build(BuildContext context) {
    // Ensure status is updated based on current time before building card
    if (assignment.status == 'Pending' && DateTime.now().isAfter(assignment.deadline)) {
        assignment.status = 'Overdue';
    }

    return Card(
      margin: const EdgeInsets.symmetric(vertical: 6.0, horizontal: 2.0),
      child: ListTile(
        contentPadding: const EdgeInsets.all(12.0),
        title: Text(assignment.title, style: Theme.of(context).textTheme.titleLarge?.copyWith(color: Theme.of(context).primaryColorDark)),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            const SizedBox(height: 4),
            Text('Course: ${assignment.courseCode}'),
            const SizedBox(height: 4),
            Text(
              'Deadline: ${DateFormat.yMMMd().add_jm().format(assignment.deadline)}',
              style: TextStyle(
                fontWeight: FontWeight.w600,
                color: (assignment.status == 'Overdue' && assignment.studentSolutionPath == null) ? Colors.redAccent : Colors.black87,
              ),
            ),
            const SizedBox(height: 6),
            Row(
              children: [
                 const Text('Status: ', style: TextStyle(fontWeight: FontWeight.w500)), // Added const
                 AssignmentStatusChip(status: assignment.status), 
              ],
            )
          ],
        ),
        trailing: const Icon(Icons.arrow_forward_ios, size: 16),
        onTap: () => onNavigate(context, assignment), // Pass context and assignment
      ),
    );
  }
}

class AssignmentStatusChip extends StatelessWidget {
  final String status;
  const AssignmentStatusChip({super.key, required this.status});

  @override
  Widget build(BuildContext context) {
    Color chipColor = Colors.grey;
    IconData chipIcon = Icons.hourglass_empty;
    String chipLabel = status;

    switch (status) {
      case 'Pending':
        chipColor = Colors.orangeAccent;
        chipIcon = Icons.hourglass_top_rounded;
        break;
      case 'Submitted':
        chipColor = Colors.green;
        chipIcon = Icons.check_circle_outline_rounded;
        break;
      case 'Overdue':
        chipColor = Colors.redAccent;
        chipIcon = Icons.error_outline_rounded;
        break;
    }

    return Chip(
      avatar: Icon(chipIcon, color: Colors.white, size: 18),
      label: Text(chipLabel, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
      backgroundColor: chipColor,
      padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 2.0),
    );
  }
}
