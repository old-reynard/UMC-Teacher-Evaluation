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
import 'dart:async';
import 'package:umc_teacher_eval/model/period.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:umc_teacher_eval/data/TeacherEvalContract.dart';
import 'package:umc_teacher_eval/notification.dart';
import 'package:umc_teacher_eval/auth.dart';
import 'package:firebase_auth/firebase_auth.dart';

final String fieldKey = 'field';
final String nameKey = 'name';
final fireStore = Firestore.instance.collection('teachers');
final fireAuth = FirebaseAuth.instance;

/// This page starts the app after signing in and is meant to choose teachers
/// who worked with the user in the particular term
class ChooseTeachersPage extends StatefulWidget {
  ChooseTeachersPage({Key key, this.auth, this.onSignOut}) : super(key: key);

  final BaseAuth auth;
  final VoidCallback onSignOut;

  @override
  _ChooseTeachersPageState createState() => new _ChooseTeachersPageState();
}

class _ChooseTeachersPageState extends State<ChooseTeachersPage> {
  Map<int, Period> periodMap = Map();

  /// Global key, used to create snack messages with errors
  final key = new GlobalKey<ScaffoldState>();

  @override
  void initState() {
    super.initState();
    _initMap();
  }

  @override
  Widget build(BuildContext context) {
    return new Scaffold(
      key: key,
      appBar: _appBar(TeacherEvalContract.chooseTeacherHeadline),
      drawer: _drawer(),
      bottomNavigationBar: _bottomBar(),
      floatingActionButtonLocation: FloatingActionButtonLocation.endDocked,
      floatingActionButton: FloatingActionButton(
        heroTag: 'increment',
        child: Icon(Icons.add),
        onPressed: _incrementPeriods,
        backgroundColor: TeacherEvalValues.accent,
      ),
      body: _listItems(),
    );
  }

  /* UI */
  _dismissible(int i) {
    Period period = Period(order: '$i');
    return Dismissible(
      key: ObjectKey(period),
      child: ListTile(
        title: _dropdown(i),
      ),
      onDismissed: (direction) {
        _deletePeriod(period);
      },
      background: Container(color: Colors.orangeAccent),
    );
  }

  _listItems() {
    int len = periodMap.length + 3;
    return ListView.builder(
        itemCount: len,
        itemBuilder: (context, index) {
          if (index == 0) {
            return Padding(
              padding: EdgeInsets.all(16.0),
              child: Text(
                TeacherEvalContract.chooseTeacherInstruction,
                style: TeacherEvalValues.details,
                textAlign: TextAlign.center,
              ),
            );
          } else if (index == len - 2) {
            return Divider(color: TeacherEvalValues.elementColor, height: 16.0,);
          } else if (index == len - 1) {
            return Text(
              'Swipe right or left to delete\n',
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 12.0),
            );
          }
          return _dismissible(index - 1);
        });
  }

  Widget _appBar(String text) {
    return AppBar(
      title: Text(
        text,
        style: TeacherEvalValues.headlines,
        maxLines: 2,
      ),
//      leading: IconButton(
//        icon: Icon(
//          Icons.exit_to_app,
//          color: TeacherEvalValues.elementColor,
//        ),
//        onPressed: () => _logoutDialog(),
//      ),
      centerTitle: true,
      elevation: 0.0,
      backgroundColor: TeacherEvalValues.background,
      actions: <Widget>[
        IconButton(
          icon: const Icon(Icons.arrow_forward),
          color: TeacherEvalValues.elementColor,
          tooltip: 'Ready',
          onPressed: () {
            _handleForward();
          },
        )
      ],
    );
  }

  Widget _bottomBar() {
    const edgeInsets = EdgeInsets.symmetric(
      horizontal: 16.0,
    );

    return BottomAppBar(
      shape: CircularNotchedRectangle(),
      elevation: 8.0,
      color: TeacherEvalValues.background,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.start,
        children: <Widget>[
          Padding(
            padding: edgeInsets,
            child: IconButton(
                icon: Icon(Icons.menu),
                onPressed: () {
                  key.currentState.openDrawer();
                }),
          ),
          Padding(
              padding: edgeInsets,
              child:
                  IconButton(icon: Icon(Icons.autorenew), onPressed: _initMap)),
        ],
      ),
    );
  }

  Widget _drawer() {
    final user = fireAuth.currentUser();
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
                    return Text(snapshot.data.email);
                  } else {
                    return Text('Loading...');
                  }
                }),
          ),
          ListTile(
            onTap: () {
              _initMap();
              Navigator.pop(context);
            },
            title: Text('Reset all periods'),
            trailing: Icon(Icons.autorenew),
          ),
          ListTile(
            onTap: _logoutDialog,
            title: Text('Log out'),
            trailing: Icon(Icons.exit_to_app),
          ),
        ],
      ),
    );
  }

  Widget _dropdown(int position) {
    var period = periodMap[position];

    return StreamBuilder<QuerySnapshot>(
      stream: Firestore.instance.collection('teachers').snapshots(),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return Text("Loading...");
        }
        if (snapshot != null && snapshot.data.documents != null) {
          return Container(
            margin: EdgeInsets.only(top: 16.0),
            padding: EdgeInsets.symmetric(horizontal: 8.0),
            decoration: BoxDecoration(
                color: Colors.grey[50],
                border: Border.all(
                  color: Colors.grey[400],
                  width: 1.0,
                )),
            child: Theme(
              data: Theme.of(context).copyWith(
                canvasColor: Colors.grey[50],
              ),
              child: DropdownButtonHideUnderline(
                child: ButtonTheme(
                  alignedDropdown: true,
                  child: DropdownButton<String>(
                    style: Theme.of(context).textTheme.subhead,
                    hint: Text(
                        'Your teacher in period ${int.parse(period.order) + 1}'),
                    value: period.teacher == null ? null : period.teacher,
                    items: snapshot.data.documents.map((DocumentSnapshot doc) {
                      var name = doc[nameKey];

                      return DropdownMenuItem<String>(
                          child: Text(name), value: name);
                    }).toList(),
                    onChanged: (String value) {
                      _savePeriod(position, value);
                    },
                  ),
                ),
              ),
            ),
          );
        }
      },
    );
  }

  /* BACKEND */
  void _logout() async {
    try {
      await widget.auth.signOut();
      widget.onSignOut();
    } catch (e) {
      print(e);
    }
  }

  Future<Null> _logoutDialog() async {
    return showDialog<Null>(
      context: context,
      barrierDismissible: false, // user must tap button!
      builder: (BuildContext context) {
        return new AlertDialog(
          title: new Text(
            'Logout?',
            style: TextStyle(color: TeacherEvalValues.elementColor),
          ),
          content: new SingleChildScrollView(
            child: new ListBody(children: [
              Text('Do you want to log out and enter as a different user?'),
            ]),
          ),
          actions: <Widget>[
            new FlatButton(
              child: new Text('No, stay!'),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
            new FlatButton(
                child: new Text('Yes, log out!'),
                onPressed: () {
                  _logout();
                  Navigator.of(context).pop();
                }),
          ],
        );
      },
    );
  }

  void _deletePeriod(Period period) {
    var key = int.parse(period.order);

    if (periodMap.containsKey(key)) {
      periodMap.remove(key);
    }
    _reorder();
    print('after deleting >>> ' + periodMap.toString());
  }

  void _savePeriod(int position, String name) {
    setState(() {
      periodMap[position] = Period(order: '$position', teacher: name);
    });
    print('after saving >>> ' + periodMap.toString());
  }

  void _incrementPeriods() {
    int len = periodMap.length;
    if (len < 10) {
      setState(() {
        periodMap[len] = Period(order: '${_biggest() + 1}');
      });
    }
    print('after adding >>> ' + periodMap.toString());
  }

  void _initMap() {
    setState(() {
      periodMap.clear();
      for (int i = 0; i < 3; i++) {
        periodMap[i] = Period(order: '$i');
      }
    });
  }

  int _biggest() {
    int max = 0;
    for (var index in periodMap.keys) {
      var order = int.parse(periodMap[index].order);
      if (order > max) {
        max = order;
      }
    }
    return max;
  }

  void _reorder() {
    var values = periodMap.values.toList();
    Map<int, Period> temp = Map();
    for (int i = 0; i < periodMap.length; i++) temp[i] = values[i];
    periodMap = temp;
  }

  int _validate() {
    for (var key in periodMap.keys) {
      var period = periodMap[key];
      if (period.teacher == null) return int.parse(period.order);
    }
    return -1;
  }

  void _showVerificationSnack(String message) {
    key.currentState.showSnackBar(
      new SnackBar(
        backgroundColor: TeacherEvalValues.elementColor,
        content: Text(
          message,
          textAlign: TextAlign.center,
        ),
      ),
    );
  }

  void _handleForward() {
    int code = _validate();
    if (code != -1) {
      String message =
          'Choose teachers for all periods\n OR\n delete them from the list\n\n' +
              'Don\'t forget period ${code + 1}!';
      _showVerificationSnack(message);
    } else {
      var list = periodMap.values.toList();
      Navigator.of(context).push(
        MaterialPageRoute<Null>(
          builder: (BuildContext context) => new NotificationPage(
                teachers: list,
                auth: widget.auth,
              ),
        ),
      );
    }
  } 
}
