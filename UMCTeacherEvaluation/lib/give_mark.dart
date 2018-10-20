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


import 'package:flutter/material.dart';
import 'package:umc_teacher_eval/notification.dart';
import 'dart:async';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:umc_teacher_eval/model/mark.dart';
import 'model/period.dart';
import 'package:umc_teacher_eval/data/TeacherEvalContract.dart';

String teacherKey = 'teacher';
String periodKey = 'period';
String marksKey = 'marks';
String valueKey = 'value';

/// This is the main page of the app where the user give performance evaluation
/// to teachers.
class GiveMarkPage extends StatefulWidget {
  GiveMarkPage({Key key, this.teachers}) : super(key: key);

  final List<Period> teachers;

  @override
  State createState() => new _GiveMarkPageState();
}

class _GiveMarkPageState extends State<GiveMarkPage> {
  final int numberOfMarks = 5;

  /// Counter for the list of questions
  int order = 0;

  /// A map for marks and fills it with null values
  Map<int, dynamic> result = Map();

  /// A list for facility marks (only one receiver, so no map needed)
  List<Mark> facilityMarks = <Mark>[];

  /// Global key, used to create snack messages with errors
  final key = new GlobalKey<ScaffoldState>();


  ///creates a map and a list for marks and fills them with null values
  @override
  void initState() {

    int numberOfFacQuestions = TeacherEvalContract.numberOfQuestions -
        TeacherEvalContract.numberOfTeachersQuestions;

    for (int j = 0; j < numberOfFacQuestions; j++) {
      facilityMarks.add(null);
    }
    _initMap();

    super.initState();
  }


  @override
  Widget build(BuildContext context) {
    return new Scaffold(
      appBar: _appBar(_prepareLabel(_getListOrder())),
      drawer: _drawer(),
      bottomNavigationBar: _bottomBar(),
      key: key,
      body: SingleChildScrollView(
        child: Center(
          child: Column(
            children: _createGiveMarkLayout(order),
          ),
        ),
      ),
    );
  }


  /// creates a round button with a mark;
  /// expects a teacher (to later store their mark to db), a boolean state
  /// (to decide what color to use) and a mark value (to draw it as a child;
  /// used late in [createMarksForOne]
  Widget _createMarkButton(int markValue, bool active, int currentKey) {
    return new InkWell(
      onTap: () {
        if (order < TeacherEvalContract.numberOfTeachersQuestions) {
          setState(() {
            result[currentKey][marksKey][order] =
            new Mark(param: order, teacher: result[currentKey][teacherKey], value: markValue);
          });
        } else {
          setState(() {
            facilityMarks[order - TeacherEvalContract.numberOfTeachersQuestions] =
            new Mark(param: order, teacher: "Facility", value: markValue);
          });
        }
      },
      child: new Container(
        width: 50.0,
        height: 50.0,
        decoration: new BoxDecoration(
          border: !active ? Border.all(color: Colors.blue, width: 2.0) : null,
          borderRadius: new BorderRadius.circular(30.0),
          color: _getFillingColor(active),
        ),
        child: Center(
          child: Text(
            '$markValue',
            style: TextStyle(
              fontFamily: 'RobotoMono',
              color: _getFillingColor(!active),
              fontSize: 20.0,
              fontWeight: FontWeight.bold,
            ),
          ),
        ),
      ),
    );
  }


  /// creates a row of buttons with marks for one teacher;
  /// used later in [createGiveMarkLayout]
  Widget _createMarksForOne(int key) {
    List<Widget> buttons = <Widget>[];
    for (int i = 1; i <= numberOfMarks; i++) {
      /* decides if the button is clicked or not */
      bool active;
      if (key != null) {
        /* if current teacher has no mark, draw an inactive button */
        if (result[key][marksKey][order] == null) {
          active = false;
        } else {
          /* else a mark for the question is extracted and compared against
        possible mark values */
          var value = result[key][marksKey][order].value;
          if (value != null) {
            active = value != null && value == i;
          }
        }
      } else {
        int count = order - TeacherEvalContract.numberOfTeachersQuestions;
        if (facilityMarks[count] == null) {
          active = false;
        } else {
          var value = facilityMarks[count].value;
          active = value != null && value == i;
        }
      }

      buttons.add(Padding(
        padding: EdgeInsets.all(4.0),
        child: _createMarkButton(i, active, key),
      ));
    }
    /* returns a row of buttons */
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: buttons,
    );
  }


  /// creates the whole layout for the entire Give Mark page;
  /// expects a number that shows the order of the question in the global list
  List<Widget> _createGiveMarkLayout(int order) {
    /* counts the questions in a separate Text view */
    String counter = '${order + 1} / ${TeacherEvalContract.numberOfQuestions}';

    List<Widget> layout = <Widget>[];

    /* extract the list of all questions */
    var questions = _prepareQuestions();
    layout.add(Padding(
      padding: EdgeInsets.all(8.0),

      /* counter for questions */
      child: Text(
        counter,
        textAlign: TextAlign.center,
        style: TeacherEvalValues.details,
      ),
    ));
    layout.add(
      Padding(
        padding: EdgeInsets.all(8.0),

        /* current question */
        child: Text(
          questions[order],
          textAlign: TextAlign.center,
          style: TeacherEvalValues.bodies,
        ),
      ),
    );

    if (order < TeacherEvalContract.numberOfTeachersQuestions) {
      /* add all previously selected teachers to the screen */
      for (var t in result.keys) {
        layout.add(Padding(
          padding: EdgeInsets.all(8.0),
          child: Text(
            result[t][teacherKey],
            textAlign: TextAlign.start,
            style: TeacherEvalValues.bodies,
          ),
        ));
        layout.add(_createMarksForOne(t));
      }
    /* facility marks case */
    } else if (order >= TeacherEvalContract.numberOfTeachersQuestions &&
        order < TeacherEvalContract.numberOfQuestions) {
      layout.add(_createMarksForOne(null));
    }
    return layout;
  }


  /// returns color depending on the binary choice of state
  Color _getFillingColor(bool active) {
    return active ? Colors.blue : Colors.white;
  }


  /// checks if the use has given all necessary marks for current question;
  /// sends a snack message if not;
  /// called every time the user taps the forward arrow
  bool _verifyMarks() {
    if (order < TeacherEvalContract.numberOfTeachersQuestions) {
      for (var t in result.keys) {
        var teacher = result[t];
        if (teacher[marksKey][order] == null) {
          String snack = 'Don\'t forget to give a mark to ${teacher[teacherKey]}!';
          _showVerificationSnack(snack);
          return false;
        }
      }
      return true;
    } else {
      int count = order - TeacherEvalContract.numberOfTeachersQuestions;
      if (facilityMarks[count] == null) {
        String snack = 'Don\'t leave it empty!';
        _showVerificationSnack(snack);
        return false;
      } else {
        return true;
      }
    }
  }


  /// called when the user tries to advance to the next question
  void _handleForwardButton() {
    bool verified = _verifyMarks();
    if (verified) {
      if (order < TeacherEvalContract.numberOfQuestions - 1) {
        setState(() {
          order++;
        });
      }
    }
    print(result);
    print(facilityMarks);
  }


  /// called when the user tries to go back to the previous question
  void _handleBackButton() {
    if (order > 0) {
      setState(() {
        order--;
      });
    }
  }


  /// returns the comprehensive list of all questions
  List<String> _prepareQuestions() {
    List<String> questions = <String>[];
    questions.addAll(TeacherEvalContract.organisationQuestions);
    questions.addAll(TeacherEvalContract.presentationQuestions);
    questions.addAll(TeacherEvalContract.classroomManagementQuestions);
    questions.addAll(TeacherEvalContract.facilitiesQuestions);
    return questions;
  }


  /// determines what category of questions is being displayed right now, based
  /// on the [order]
  int _getListOrder() {
    int lengthOfOne = TeacherEvalContract.organisationQuestions.length;
    int lengthOfTwo =
        lengthOfOne + TeacherEvalContract.presentationQuestions.length;
    int lengthOfThree =
        lengthOfTwo + TeacherEvalContract.classroomManagementQuestions.length;

    if (order >= 0 && order < lengthOfOne)
      return 0;
    else if (order >= lengthOfOne && order < lengthOfTwo)
      return 1;
    else if (order >= lengthOfTwo && order < lengthOfThree)
      return 2;
    else
      return 3;
  }


  /// returns the label to be used in AppBar depending on what [getListOrder]
  /// returns
  String _prepareLabel(int list) {
    switch (list) {
      case 0:
        return TeacherEvalContract.organisationQuestionsLabel;
      case 1:
        return TeacherEvalContract.presentationQuestionsLabel;
      case 2:
        return TeacherEvalContract.classroomManagementQuestionsLabel;
      default:
        return TeacherEvalContract.facilitiesQuestionsLabel;
    }
  }


  /// opens new screen when the user has answered all the questions and taps
  /// the OK button
  void _handleOkButton() {
    if (_verifyMarks()) {
      _proceedDialog();
    }
  }

  /// shows a snack message that lets the user know if they are doing everything
  /// correctly;
  /// used for verification in [verifyMarks]
  void _showVerificationSnack(String message) {
    key.currentState.showSnackBar(
      new SnackBar(
        backgroundColor: TeacherEvalValues.elementColor,
        content: Text(message, textAlign: TextAlign.center,),
      ),
    );
  }


  /// creates the AppBar for this screen
  Widget _appBar(String text) {

    return AppBar(
      title: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          Text(
            text,
            style: TeacherEvalValues.headlines,
            maxLines: 2,
            softWrap: true,
          )
        ],
      ),
      centerTitle: true,
      elevation: 0.0,
      backgroundColor: TeacherEvalValues.background,
      actions: <Widget>[IconButton(
              color: TeacherEvalValues.background,
              tooltip: 'Previous choice',
              icon: const Icon(Icons.arrow_forward),
              onPressed: () => print('')),
      ],
    );
  }


  /// creates the bottom bar for the app;
  /// the bottom bar is used for reference information about possible marks
  Widget _bottomBar() {

    var arrowButton = IconButton(
        color: TeacherEvalValues.elementColor,
        icon: const Icon(Icons.arrow_forward),
        tooltip: 'Next choice',
        onPressed: _handleForwardButton);

    var okButton = IconButton(
        color: TeacherEvalValues.elementColor,
        icon: const Icon(Icons.check),
        tooltip: 'Ready',
        onPressed: _handleOkButton);

    var backArrow = Opacity(
      opacity: order == 0 ? 0.0 : 1.0,
      child: IconButton(
          color: TeacherEvalValues.elementColor,
          tooltip: 'Previous choice',
          icon: const Icon(Icons.arrow_back),
          onPressed: _handleBackButton),
    );

    return BottomAppBar(
      elevation: 16.0,
      color: TeacherEvalValues.background,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16.0),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.center,
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: <Widget>[
              backArrow,
              Expanded(
                child: RawMaterialButton(
                  onPressed: _explainMarks,
                  child: Text('\tWhat do these numbers mean?',
                    textAlign: TextAlign.center,
                  ),
                ),
              ),
              order == TeacherEvalContract.numberOfQuestions - 1
            ? okButton
            : arrowButton,

            ],
          ),
      ),
    );
  }


  /// creates and shows an alert dialog with reference information about marks
  Future<Null> _explainMarks() async {
    List<Widget> content;

    if (order < TeacherEvalContract.numberOfTeachersQuestions) {
      content = <Widget>[
        new Text('Please give each teacher a mark for their work.\n'),
        new Text('Here is what the marks mean:\n'),
        new Text(
          TeacherEvalContract.getMarkExplanations(TeacherEvalContract.marksExplanations),
        )
      ];
    } else {
      content = <Widget>[
        new Text('Please take some time to give us some feedback about our facilities and administration\n'),
        new Text('Here is what the marks mean:\n'),
        new Text(
          TeacherEvalContract.getMarkExplanations(TeacherEvalContract.facMarksExplanations),
        )
      ];
    }

    return showDialog<Null>(
      context: context,
      barrierDismissible: false, // user must tap button!
      builder: (BuildContext context) {
        return new AlertDialog(
          title: new Text('What do these numbers mean?',
            style: TextStyle(color: TeacherEvalValues.elementColor),
          ),
          content: new SingleChildScrollView(
            child: new ListBody(
              children: content
            ),
          ),
          actions: <Widget>[
            new FlatButton(
              child: new Text('Ok!'),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }

  /// creates a map with marks for all the teachers selected in the previous
  /// screen
  void _initMap() {
    for (int i = 0; i < widget.teachers.length; i++) {
      result[i] = {
        teacherKey: widget.teachers[i].teacher,
        periodKey: widget.teachers[i].order,
        marksKey: List<Mark>(TeacherEvalContract.numberOfTeachersQuestions)
      };
    }
  }


  /// navigates to Feedback page
  void _navigateToFeedback() {
    Navigator.of(context).push(MaterialPageRoute<Null>(
        builder: (BuildContext context) =>
        new FeedbackPage(teachers: widget.teachers,)));
  }


  /// saves marks to Firestore database before leaving the screen
  void _saveMarks(String sender) {
    var db = Firestore.instance.collection(marksKey);
    var timestamp = DateTime.now().millisecondsSinceEpoch;
    for (int i = 0; i < result.length; i++) {
      var entry = result[i];
      var marks = entry[marksKey];
      var teacher = entry[teacherKey];
      var period = int.parse(entry[periodKey]);
      for (Mark mark in marks) {
        db.add({
          teacherKey  : teacher,
          periodKey   : period,
          'sender'    : sender,
          'question'  : mark.param,
          'timestamp' : timestamp,
          valueKey    : mark.value
        });
      }
    }
    
    var facDb = Firestore.instance.collection('facilities');
    for (Mark facMark in facilityMarks) {
      facDb.add({
        'sender'      : sender,
        'timestamp'   : timestamp,
        'question'    : facMark.param - TeacherEvalContract.numberOfTeachersQuestions,
        valueKey      : facMark.value
      });
    }
  }
  

  /// creates the drawer for this screen
  Widget _drawer() {
    final user = FirebaseAuth.instance.currentUser();
    return Drawer(

      child: ListView(
        children: <Widget>[
          UserAccountsDrawerHeader(
            decoration: BoxDecoration(
              image: DecorationImage(
                  image: AssetImage('assets/maple.jpg'), fit: BoxFit.fill),
            ),
            currentAccountPicture: FutureBuilder(
                future: user,
                builder: (BuildContext context,
                    AsyncSnapshot<FirebaseUser> snapshot) {
                  if (snapshot.connectionState == ConnectionState.done) {
                    if (snapshot.data.photoUrl != null) {
                      final url = snapshot.data.photoUrl;
                      return GestureDetector(
                        child: CircleAvatar(
                          backgroundImage: NetworkImage(url),
                        ),
                      );
                    } else
                      return Container();
                  } else {
                    return Container();
                  }
                }),
            accountName: FutureBuilder(
                future: user,
                builder: (BuildContext context,
                    AsyncSnapshot<FirebaseUser> snapshot) {
                  if (snapshot.connectionState == ConnectionState.done) {
                    if (snapshot.data.displayName != null) {
                      return Text(
                        snapshot.data.displayName,
                        style: TextStyle(
                            fontSize: 14.0, fontWeight: FontWeight.bold),
                      );
                    } else {
                      return Container();
                    }
                  } else {
                    return Container();
                  }
                }),
            accountEmail: FutureBuilder(
                future: user,
                builder: (BuildContext context,
                    AsyncSnapshot<FirebaseUser> snapshot) {
                  if (snapshot.connectionState == ConnectionState.done) {
//                    sender = snapshot.data.email;
                    return Text(snapshot.data.email);
                  } else {
                    return Text('Loading...');
                  }
                }),
          ),
          ListTile(
            onTap: () {
              _initMap();
              setState(() {
                order = 0;
              });
//              Navigator.pop(context);
            },
            title: Text('Start again'),
            trailing: Icon(Icons.autorenew),
          ),
          ListTile(
//            onTap: _logoutDialog,
            title: Text('Log out'),
            trailing: Icon(Icons.exit_to_app),
          ),
        ],
      ),
    );
  }


  /// creates a dialog that asks the user if they want to leave the screen
  /// upon clicking 'yes', saves all the marks to database and navigates to
  /// Feedback page
  Future<Null> _proceedDialog() async {
    return showDialog<Null>(
      context: context,
      barrierDismissible: false, // user must tap button!
      builder: (BuildContext context) {
        return new AlertDialog(
          title: new Text(
            'Are you ready?',
            style: TextStyle(color: TeacherEvalValues.elementColor),
          ),
          content: new SingleChildScrollView(
            child: new ListBody(children: [
              Text('Have you finished giving marks or do you need more time?'),
            ]),
          ),
          actions: <Widget>[
            new FlatButton(
              child: new Text('No, I need more time!'),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
            new FlatButton(
                child: new Text('Yes, let\'s go!'),
                onPressed: () {
                  FirebaseAuth.instance.currentUser().then((user) {
                    String sender = user.email;
                    _saveMarks(sender);
                    _navigateToFeedback();
                  });
                }),
          ],
        );
      },
    );
  }

}


