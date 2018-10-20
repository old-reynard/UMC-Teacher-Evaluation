//Copyright 2018 Kit Gerasimov
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

import 'package:flutter/painting.dart';
import 'package:flutter/material.dart';

/// Static class that stores data constants for the app
class TeacherEvalContract {
  /// title and instruction for Choose Your Teachers screen
  static const String chooseTeacherHeadline             = 'Teacher Evaluation';
  static const String chooseTeacherInstruction =
      'Choose all teachers who worked with you this term and tap the Forward arrow';

  /// instruction before questions start
  static const String instructionsOnMarks = '''Please take some time to give us some feedback. Read each question and rate it from 1 to 5. \n\nIf you have any comments to make, please write them on the last page, after all the questions. \n\nThank you!''';

  static const String noteOnMarks = '\nThe number rating stands for the following: \n';

  /// explanations for marks before questions start
  static const List<String> marksExplanations = <String>[
    'rarely',
    'once in a while',
    'sometimes',
    'most of the time',
    'almost always'
  ];

  static const List<String> facMarksExplanations = <String>[
    'strongly disagree',
    'disagree',
    'neutral',
    'agree',
    'strongly agree',
  ];

  static String getMarkExplanations(List<String> list) {
    String explanations = '';
    for (int i = 0; i < list.length; i++) {
      explanations += '${i + 1}: ${list[i]}\n';
    }
    return explanations;
  }


  /// labels for different groups of questions
  static const String organisationQuestionsLabel        = 'Lessons';
  static const String presentationQuestionsLabel        = 'Presentation';
  static const String classroomManagementQuestionsLabel = 'In Classroom';
  static const String facilitiesQuestionsLabel          = 'Facilities';

  static const String facilitiesInstruction = '''
    Please take some time to give us some feedback about our facilities and administration. 
    Read each question and rate it by choosing which you feel best describes each question.
    ''';

  static const String freeFeedbackInstruction =
  '''Here at UMC, we pride ourselves on the quality education and experience that we provide to our students.\n
If there is anything you are unsatisfied with, please let us know!  \n
We are always working hard to ensure we are providing you with the best learning experience.\n''';

  static const String freeFeedbackRemark = 'Your feedback is important to us!';
  static const String freeFeedbackRemark2 =
      'It\'s OK to write comments in your own language';

  /// questions to be asked from students
  static const List<String> organisationQuestions = [
    'Teacher is well prepared and organized (photocopy or handouts and equipment ready)',
    'Teacher’s lessons and activities are clear and well organized to help my learning',
    'Exercises, tests and activities are suitable for my level',
    'Teacher gives each student equal opportunity to speak up and be active in class',
    'Teacher is creative in developing activities and lessons (revised if boring etc.)',
    'Each class has an instructional component (e.g. where the teacher teaches something)',
  ];

  static const List<String> presentationQuestions = [
    'Teacher sets a positive & active classroom atmosphere (walks around, stands up, etc)',
    'Teacher’s explanation is clear (makes use of the board to explain or uses examples)',
    'Teacher uses different teaching methods (whiteboard, group activity, peer to peer)',
    'Teacher keeps me engaged',
    'Teacher gives good feedback & corrects mistakes to help me learn',
  ];

  static const List<String> classroomManagementQuestions = [
    'Teacher starts class on time, manages class activities, knows when to end lesson',
    'Teacher maintains a professional image throughout the class',
    'Teacher deals with behavior issues (enforce red card and 15mins late policy)',
    'Teacher is supportive and patient',
    'This teacher encourages students to be interactive in class',
  ];

  static const List<String> facilitiesQuestions = [
    'The school is always clean',
    'The resources provided to students work and are helpful (e.g. Computers, Wi-Fi)',
    'I am happy with the size of my classes',
    'The administrative staff is friendly and helpful',
    'I am interested in joining the school activities'
  ];

  static const String commentsLabel = 'Comments';

  static final int numberOfTeachersQuestions = organisationQuestions.length +
      presentationQuestions.length +
      classroomManagementQuestions.length;

  static final int numberOfQuestions = numberOfTeachersQuestions +
      facilitiesQuestions.length;
}

/// static class that stores values for general decorations of the app
class TeacherEvalValues {
  /// colors of the app
  static final Color background = Colors.grey[50];
  static final Color elementColor = Colors.blue;
  static final Color accent = Colors.deepOrangeAccent;

  /// [TextStyle] used throughout the app for common text
  static final TextStyle bodies =
      new TextStyle(fontSize: 20.0, color: Colors.black);

  /// [TextStyle] used throughout the app for headlines
  static final TextStyle headlines = new TextStyle(
    fontSize: 24.0, color: Colors.black
  );

  /// [TextStyle] used throughout the app for smaller remarks
  static final TextStyle details = new TextStyle(
    fontSize: 16.0, color: Colors.grey[600],
  );
}
