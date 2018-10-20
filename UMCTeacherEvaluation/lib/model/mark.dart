import 'package:meta/meta.dart';
import 'package:umc_teacher_eval/data/TeacherEvalContract.dart';

/// Information about a [Mark].
class Mark {
  final int param;
  final int value;
  final String teacher;
  final int teacherId;

  /// A [Mark] is given for some question (described as param) and has its own
  /// value.
  Mark({
    @required this.param,
    this.value,
    @required this.teacher,
    this.teacherId,
  })  : assert(param != null),
        assert(param < TeacherEvalContract.numberOfQuestions),
        assert(param >= 0),
        assert(teacher != null);

  @override
  String toString() {
    return '$value for question $param';
  }


}
