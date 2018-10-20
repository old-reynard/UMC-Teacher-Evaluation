import 'package:flutter/material.dart';
import 'package:umc_teacher_eval/choose_teachers.dart';
//import 'package:firebase_database/firebase_database.dart';
import 'package:umc_teacher_eval/auth.dart';

void main() => runApp(new EvalApp());

class EvalApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'UMC Teacher Evaluation',
      theme: new ThemeData(
        fontFamily: 'Raleway',
        primarySwatch: Colors.blue,
      ),
      home: Root(auth: new Auth()),
    );
  }
}

class Root extends StatefulWidget {
  Root({Key key, this.auth}) : super(key: key);

  final BaseAuth auth;

  @override
  State<Root> createState() => new _RootState();
}

enum AuthStatus { notSignedIn, signedIn }

class _RootState extends State<Root> {
  AuthStatus status = AuthStatus.notSignedIn;
//  final storage = FirebaseDatabase.instance.reference().child('teachers');

  @override
  void initState() {
    super.initState();
    widget.auth.currentUser().then((userId) {
      setState(() {
        status = userId == null ? AuthStatus.notSignedIn : AuthStatus.signedIn;
      });
    });
  }

  void _changeStatus(AuthStatus newStatus) {
    setState(() {
      status = newStatus;
    });
  }

  @override
  Widget build(BuildContext context) {
    switch (status) {
      case AuthStatus.notSignedIn:
        return AuthPage(
          auth: widget.auth,
          onSignIn: () => _changeStatus(AuthStatus.signedIn),
        );
      default:
        return ChooseTeachersPage(
          auth: widget.auth,
          onSignOut: () => _changeStatus(AuthStatus.notSignedIn),
        );
    }
  }
}
