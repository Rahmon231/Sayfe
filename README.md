# Sayfe
<p align="center">  
The mobile application is a personal safety tool that allows users to quickly send a message containing their current location to designated emergency contacts in the case of an emergency. The app is triggered by tapping the volume up button twice and can be triggered in the background.
When activated, the app sends an in-app notification to the user's emergency contacts, as well as an SMS message containing the user's current location. The user can add emergency contacts to the app and customize the message that is sent in an emergency. The app uses the phone's GPS functionality to determine the user's location, ensuring that the emergency contacts receive accurate and up-to-date information.
With its easy-to-use design and quick access trigger, this app provides a simple and effective way for people to stay safe and let their loved ones know their location in case of an emergency.
</p>

## Download
 Still in development ⚠ ..
 
 ## Tech stack 
 
 - [Kotlin](https://kotlinlang.org/) based, [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) + [Flow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/) for asynchronous.
- Jetpack
  - Lifecycle: Observe Android lifecycles and handle UI states upon the lifecycle changes.
  - ViewModel: Manages UI-related data holder and lifecycle aware. Allows data to survive configuration changes such as screen rotations.
  - ViewBinding: View binding is a feature that allows you to more easily write code that interacts with views.
  
  - Architecture
  - MVVM (Model - View - ViewModel) with Clean Architecture
  - Repository Pattern
- [Retrofit2 & OkHttp3](https://github.com/square/retrofit): Construct the REST APIs and paging network data.
- [Moshi](https://github.com/square/moshi/): A modern JSON library for Kotlin and Java.
- [ksp](https://github.com/google/ksp): Kotlin Symbol Processing API.
- [Material-Components](https://github.com/material-components/material-components-android): Material design components for building ripple animation, and CardView.

