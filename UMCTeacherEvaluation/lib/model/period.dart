class Period {

  final String order;
  final String teacher;

  const Period({this.order, this.teacher}) : assert(order != null);

  @override
  String toString() {
    var printable = teacher == null ? 'someone' : teacher;
    return 'Period $order taught by $printable';
  }
}