import 'package:meta/meta.dart';

/// Information about a [Teacher].
class Teacher {
  final String name;
  final String email;

  /// A [Teacher] stores their name, their email address (it is used to send
  /// notifications to about students who are not ready with their evaluations).
  const Teacher({
    @required this.name,
    @required this.email,
  }) :  assert (name != null),
        assert (email != null);
}