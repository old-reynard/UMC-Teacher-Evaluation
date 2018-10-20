# UMC Teacher Evaluation

[Upper Madison College](http://umcollege.ca) in Toronto uses their students feedback to review and improve the teachers' performance.

This repository contains two apps that automate this process.

UMC Teacher Evaluation is the students' application that allows them to register and evaluate their teachers and the school's facilities or leave some feedback without any restrictions. The app is written in the Dart programming language and thus aims to work on both Android and iOS operating systems. It also uses combination Flutter + Firebase which solves the problem of data storage in an affordable manner.

![UMC Teacher Eval screenshot 1](https://i.imgur.com/mHQQLgg.jpg)
![UMC Teacher Eval screenshot 2](https://i.imgur.com/4aihkil.jpg)

The Android part of the app is published [here](https://play.google.com/store/apps/details?id=com.umc.umcteachereval).

UMC Admin is the second app intended for internal use only. It is written in Java for native Android and it is where all the data processing happens. The app knows how to sort data from Firebase servers and organise it in a processable form (that is, XLS tables; the app uses the JXL library for building the Excel report files). This is done in order to re-create the work flow traditional for the college and automate it.


![UMC teacher evaluation report](https://i.imgur.com/nMKvRIP.png)

