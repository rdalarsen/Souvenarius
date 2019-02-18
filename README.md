## Souvenarius

In this the 6th and final project in the Udacity Android Developer Nanodegree, students are expected to demonstrate proficiency in the previously taught Android concepts by incorporating them in an app that is entirely their own creation.

This is Souvenarius, a small app that lets the user save a virtual souvenir, a photo of a place or object, along with time, place and a story; it is the culmination of a month worth of work, and I'm proud of myself for my accomplishment!

#### Highlights

- Leverages cloud services: Firebase Auth, Firebase Storage, Firebase Realtime Database
- Improves the user experience by automatically fetching user's current location (if permitted to do so)
- Featurs an AppWidget displaying the user's most recent Souvenir, along with functionality allowing for easy access to adding a Souvenir to the collection
- Offline viewing of souvenirs with Room database used as cache
- Multi-user support
- Complete CRUD support for managing Souvenirs
- Useful and visually appealing Material Design UI patterns such as auto-hiding Toolbar and a Floating Action Button

#### Technical features

In addition to the formal requirements set forth by Udacity, I also looked for possibilities to incorporate a few industry-relevant techincal features that provide good value to the development effort. To this end, I also took it upon me to learn and implement the following technical features.

- DataBinding, the easier and more flexible way of binding data to views.
- Dagger2, the dependency injection pattern that eases development and provides increased separation of concerns
- Automated testing (instrumented and non-instrumented) of critical app components via a combination of Espresso, JUnit, and Mockito