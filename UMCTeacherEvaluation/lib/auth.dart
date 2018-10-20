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
import 'package:firebase_auth/firebase_auth.dart';
import 'package:google_sign_in/google_sign_in.dart';
import 'package:umc_teacher_eval/data/TeacherEvalContract.dart';


/// The app's authorisation page that support email and Google account auth
class AuthPage extends StatefulWidget {
  AuthPage({Key key, this.auth, this.onSignIn}) : super(key: key);

  /// Custom auth that is based on Firebase auth feature
  final BaseAuth auth;
  final VoidCallback onSignIn;

  @override
  _AuthPageState createState() => new _AuthPageState();
}

class _AuthPageState extends State<AuthPage> {
  /// user's sign-in data
  String _email;
  String _password;

  /// Hint used to help debug the sign-in process
  String _authHint = '';

  /// indicates if the user is logging in or signing up
  FormType _form = FormType.login;
  final TextStyle textStyle = TeacherEvalValues.bodies;
  static final formKey = new GlobalKey<FormState>();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: _getGeneralAppBar('Welcome to UMC!'),
      backgroundColor: TeacherEvalValues.background,
      body: SingleChildScrollView(
        child: Center(
          child: Form(
            key: formKey,
            child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
              children: _textWidgets() + _userWidgets() + _submitWidgets() + _googleWidgets() + [hintText()],
            ),
          ),
        ),
      ),
    );
  }


  /// checks if the forms contain valid user data;
  /// returns true, if they do
  bool _validateAndSave() {
    final form = formKey.currentState;
    if (form.validate()) {
      form.save();
      return true;
    }
    return false;
  }


  /// checks if the forms contain valid data and tries to sign the user in or up
  void _validateAndSubmit() async {
    if (_validateAndSave()) {
      try {
        String userId = _form == FormType.login
            ? await widget.auth.signIn(_email, _password)
            : await widget.auth.createUser(_email, _password);

        setState(() {
          _authHint = 'Signed In\n\nUser id: $userId';
        });
        widget.onSignIn();
      } catch (e) {
        setState(() {
          _authHint = 'Sign In Error\n\n${e.toString()}';
        });
        print(e);
      }
    }
  }

  void _gSubmit() async {
    try {
      FirebaseUser user = await widget.auth.gSignIn();
      setState(() {
        _authHint = 'Signed In\n\nUser id: ${user.displayName}';
      });
      widget.onSignIn();
    } catch (e) {
      setState(() {
        _authHint = 'Sign In Error\n\n${e.toString()}';
      });
      print(e);
    }
  }


  /// creates input widgets for the screen
  List<Widget> _userWidgets() {
    String email = 'Email';
    String password = 'Password';
    var padding = EdgeInsets.all(16.0);

    Widget emailField = padded(
        child: TextFormField(
          key: Key(email),
          decoration: new InputDecoration(
            labelText: email,
            contentPadding: padding
          ),
          autocorrect: false,
          validator: (val) => val.isEmpty ? 'Email can\'t be empty' : null,
          onSaved: (val) => _email = val.trim(),
        ));
    Widget passField = padded(
        child: TextFormField(
          key: Key(password),
          decoration: new InputDecoration(
            labelText: password,
            contentPadding: padding
          ),
          obscureText: true,
          autocorrect: false,
          validator: (val) => val.isEmpty ? 'Password can\'t be empty' : null,
          onSaved: (val) => _password = val,
        ));
    return [emailField, passField];
  }

  /// creates text widgets for the screen
  List<Widget> _textWidgets() {
    return <Widget>[
      Text(
        'Please introduce yourself!\n',
        style: textStyle,
        textAlign: TextAlign.center,
      ),
      Text(
        'In order to know who you are, we will need your email\n',
        style: textStyle,
        textAlign: TextAlign.center,
      ),
      Text(
        'We promise to keep it a secret!\n',
        style: TeacherEvalValues.details,
        textAlign: TextAlign.center,
      ),
    ];
  }

  /// creates submit widgets for the screen
  List<Widget> _submitWidgets() {
    String login = 'Login';
    String needAccount = 'need_account';
    switch (_form) {
      case FormType.login:
        return <Widget>[
          PrimaryButton(
            key: Key(login),
            text: login,
            height: 44.0,
            onPressed: _validateAndSubmit,
          ),
          FlatButton(
            key: Key(needAccount),
            child: Text('Need an account? Register!'),
            onPressed: _moveToRegister,
          ),
        ];
      case FormType.register:
        return <Widget>[
          PrimaryButton(
            key: Key('register'),
            text: 'Create an account',
            height: 44.0,
            onPressed: _validateAndSubmit,
          ),
          FlatButton(
            key: Key('need-login'),
            child: Text('Have an account? Log in!'),
            onPressed: _moveToLogin,
          ),
        ];
    }
    return null;
  }

  /// creates google sign-in widgets for the screen
  List<Widget> _googleWidgets() {
    return <Widget>[
      FlatButton(
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text('Or login with a Google account!'),
            Icon(Icons.account_circle)
          ],
        ),
        onPressed: () { _gSubmit(); },
      )
    ];
  }


  /// updates the screen from login to register state
  void _moveToRegister() {
    formKey.currentState.reset();
    setState(() {
      _form = FormType.register;
      _authHint = '';
    });
  }

  /// updates the screen from register to login state
  void _moveToLogin() {
    formKey.currentState.reset();
    setState(() {
      _form = FormType.login;
      _authHint = '';
    });
  }


  /// creates the AppBar for the screen
  Widget _getGeneralAppBar(String title) {
    return AppBar(
      title: Text(
        title,
        style: TeacherEvalValues.headlines,
        maxLines: 2,
      ),
      centerTitle: true,
      elevation: 0.0,
      backgroundColor: TeacherEvalValues.background,
    );
  }

  /// implements [_authHint] Widget for sign-in debugging
  Widget hintText() {
    return new Container(
      //height: 80.0,
        padding: const EdgeInsets.all(32.0),
        child: new Text(
            _authHint,
            key: new Key('hint'),
            style: new TextStyle(fontSize: 12.0, color: Colors.grey),
            textAlign: TextAlign.center)
    );
  }

  /// Padding utility
  Widget padded({Widget child}) {
    return new Padding(
      padding: EdgeInsets.symmetric(vertical: 8.0),
      child: child,
    );
  }
}


/// Button used for sign-in / register features
class PrimaryButton extends StatelessWidget {
  PrimaryButton({this.key, this.text, this.height, this.onPressed})
      : super(key: key);
  final Key key;
  final String text;
  final double height;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    return new ConstrainedBox(
      constraints: BoxConstraints.expand(height: height),
      child: new RaisedButton(
          child: new Text(text,
              style: new TextStyle(color: Colors.white, fontSize: 20.0)),
          shape: new RoundedRectangleBorder(
              borderRadius: BorderRadius.all(Radius.circular(height / 2))),
          color: Colors.blue,
          textColor: Colors.black87,
          onPressed: onPressed),
    );
  }
}


/// constants that keep track of whether the user is logged in or not
enum FormType { login, register }


abstract class BaseAuth {

  Future<String> currentUser();
  Future<String> signIn(String email, String password);
  Future<String> createUser(String email, String password);
  Future<void> signOut();
  Future<FirebaseUser> gSignIn();
}

/// Custom auth that is based on Firebase auth feature
class Auth implements BaseAuth {
  final FirebaseAuth _fireBaseAuth = FirebaseAuth.instance;
  final GoogleSignIn _googleSignIn = GoogleSignIn();

  Future<String> signIn(String email, String password) async {
    FirebaseUser user = await _fireBaseAuth.signInWithEmailAndPassword(email: email, password: password);
    return user.uid;
  }

  Future<String> createUser(String email, String password) async {
    FirebaseUser user = await _fireBaseAuth.createUserWithEmailAndPassword(email: email, password: password);
    return user.uid;
  }

  Future<String> currentUser() async {
    FirebaseUser user = await _fireBaseAuth.currentUser();
    return user != null ? user.uid : null;
  }

  Future<void> signOut() async {
    await _googleSignIn.signOut();
    return _fireBaseAuth.signOut();
  }

  Future<FirebaseUser> gSignIn() async {
    GoogleSignInAccount googleUser = await _googleSignIn.signIn();
    GoogleSignInAuthentication googleAuth = await googleUser.authentication;
    FirebaseUser user = await _fireBaseAuth.signInWithGoogle(
        idToken: googleAuth.idToken, accessToken: googleAuth.accessToken
    );
    print("signed in " + user.displayName);
    return user;
  }
}
