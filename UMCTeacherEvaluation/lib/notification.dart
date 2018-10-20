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
import 'package:flutter/painting.dart';
import 'dart:io';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:umc_teacher_eval/give_mark.dart';
import 'package:umc_teacher_eval/model/period.dart';
import 'package:umc_teacher_eval/auth.dart';
import 'package:umc_teacher_eval/data/TeacherEvalContract.dart';

/// This page will serve as tutorial point for the user and explain what needs
/// to be done
class NotificationPage extends StatelessWidget {
  final List<Period> teachers;
  final BaseAuth auth;

  NotificationPage({Key key, this.teachers, this.auth}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        elevation: 0.0,
        backgroundColor: TeacherEvalValues.background,
        title: Text(
          TeacherEvalContract.chooseTeacherHeadline,
          style: TeacherEvalValues.headlines,
        ),
      ),
      body: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: <Widget>[
            Text(
              TeacherEvalContract.instructionsOnMarks,
              style: TeacherEvalValues.bodies,
              textAlign: TextAlign.center,
            ),
            Text(
              TeacherEvalContract.noteOnMarks,
              style: TeacherEvalValues.bodies,
              textAlign: TextAlign.center,
            ),
            Text(
              TeacherEvalContract.getMarkExplanations(
                  TeacherEvalContract.marksExplanations),
              style: TeacherEvalValues.bodies,
              textAlign: TextAlign.start,
            ),
            RawMaterialButton(
              child: Text(
                'Ok!',
                style: TextStyle(fontSize: 20.0, color: Colors.white),
              ),
              highlightColor: Colors.blue[300],
              fillColor: TeacherEvalValues.elementColor,
              onPressed: () {
                Navigator.of(context).push(
                  MaterialPageRoute<Null>(
                    builder: (BuildContext context) => new GiveMarkPage(
                          teachers: teachers,
                        ),
                  ),
                );
              },
            )
          ],
        ),
      ),
    );
  }
}

/// This page is used for collecting students' feedback and is displayed after
/// all the questions
class FeedbackPage extends StatelessWidget {
  final BaseAuth auth;
  final List<Period> teachers;
  final controller = TextEditingController();

  FeedbackPage({Key key, this.auth, this.teachers}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        centerTitle: true,
        elevation: 0.0,
        backgroundColor: TeacherEvalValues.background,
        title: Text(
          TeacherEvalContract.commentsLabel,
          style: TeacherEvalValues.headlines,
        ),
      ),
      body: SingleChildScrollView(
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              Text(
                TeacherEvalContract.freeFeedbackRemark,
                style: TeacherEvalValues.bodies,
                textAlign: TextAlign.center,
              ),
              Text(
                TeacherEvalContract.freeFeedbackInstruction,
                style: TeacherEvalValues.bodies,
                textAlign: TextAlign.center,
              ),
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: TextField(
                  controller: controller,
                  onSubmitted: (content) {
                    if (content != null && content.isNotEmpty) {
                      FirebaseAuth.instance.currentUser().then((user) {
                        Firestore.instance.collection('feedback').add({
                          'sender': user.email,
                          'text': content,
                          'description': _getSenderDescription(),
                          'timestamp': DateTime.now().millisecondsSinceEpoch,
                        });
                      });
                    }

                    Navigator.of(context).push(MaterialPageRoute(
                        builder: (BuildContext context) => new GoodbyePage()));
                  },
                  maxLines: 9,
                  decoration: InputDecoration(
                      labelText: 'Your comment',
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(0.0),
                      ),
                      hintText: TeacherEvalContract.freeFeedbackRemark2,
                      hintStyle: TextStyle(fontSize: 14.0)),
                  keyboardType: TextInputType.text,
                ),
              ),
              RawMaterialButton(
                  child: Text(
                    'Ok!',
                    style: TextStyle(fontSize: 20.0, color: Colors.white),
                  ),
                  highlightColor: Colors.blue[300],
                  fillColor: TeacherEvalValues.elementColor,
                  onPressed: () {
                    String feedback = controller.text;

                    if (feedback != null && feedback.isNotEmpty) {
                      FirebaseAuth.instance.currentUser().then((user) {
                        Firestore.instance.collection('feedback').add({
                          'sender': user.email,
                          'text': feedback,
                          'description': _getSenderDescription(),
                          'timestamp': DateTime.now().millisecondsSinceEpoch,
                        });
                      });
                    }

                    Navigator.of(context).push(MaterialPageRoute(
                        builder: (BuildContext context) => new GoodbyePage()));
                  }),
            ],
          ),
        ),
      ),
    );
  }

  String _getSenderDescription() {
    String description = '';
    for (Period p in teachers) {
      description += p.teacher + ' in period ' + '${int.parse(p.order) + 1}, ';
    }
    return description;
  }
}

class GoodbyePage extends StatelessWidget {
  GoodbyePage({Key key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        elevation: 0.0,
        centerTitle: true,
        backgroundColor: TeacherEvalValues.background,
        title: Text(
          "That's it!",
          style: TeacherEvalValues.headlines,
        ),
      ),
      body: SingleChildScrollView(
        child: Center(
          child: Column(
            children: <Widget>[
              Text(
                '\n\nThanks for your time!\n\n',
                style: TeacherEvalValues.headlines,
              ),
              RawMaterialButton(
                child: Text(
                  'Bye!',
                  style: TextStyle(fontSize: 20.0, color: Colors.white),
                ),
                highlightColor: Colors.blue[300],
                fillColor: TeacherEvalValues.elementColor,
                //todo logout?
                onPressed: () => exit(0),
              )
            ],
          ),
        ),
      ),
    );
  }
}
