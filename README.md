# "WorkManager" a serverless android application using Kotlin and Firestore

WorkManager is a serverless android application where data is saved in Firestore. We will be creating a cloud function to send the push notification. You can use this application as a reference for implementing Firestore.

## Prerequisite
To continue, you need to have a  basic idea on Kotlin and Firestore.

Here is a video on Kotlin


[![IMAGE ALT TEXT HERE](https://i1.ytimg.com/vi/ZIHnQQsfvD4/0.jpg)](https://www.youtube.com/watch?v=ZIHnQQsfvD4&t=20s)


[Click Here to know more about Firestore](https://firebase.google.com/docs/firestore/quickstart")

## Getting Started
In Firebase there are mainly two main database concepts. One is [Real-time database](https://firebase.google.com/docs/database/")  and the other one is [Firestore](https://firebase.google.com/docs/firestore/").Firestore is in beta mode, which means code can change. In this application, We are concentrating on Firestore.
[Click Here to know more about the differences between Real-time database and Firestore](https://firebase.google.com/docs/database/rtdb-vs-firestore")


### Project creation in firebase
**Sptep 1.** Create a project in Firebase [Link](https://firebase.google.com/docs/firestore/quickstart")

**Sptep 2.** Provide  details like Package name,SHA1 etc to configure

**Sptep 2.** On completing step2,a file named `google-services.json`.Download this file and add to the app folder. With this process, you are done with configuration.

### Push Notification in serverless application

Since we don't have a server to handle push notification, We are going to develop a cloud function and host them in Googe cloud functions. On hosting you can view them on your firebase console.
To start with, you need to install Nodejs on your local system. With help of nodejs framework and firebase tool, you need to create a cloud function. On completing this process you can upload it on to firebase cloud function. I have kept everything simple in here. The logic is quite simple, We have a collection(For the time being let us assume collection to be a table) named Notification. Whenever there is an entry in notification collection, we need to trigger our cloud function. This cloud function will send the push notification.

**Step 1.** Install node.js in your local system [ Click Here to download Nodejs](https://nodejs.org/en/") 

**Step 2.** Make a folder for cloud function and open command prompt here

**Step 3.** Use this command to install firebase tools `npm install -g firebase-tools`

**Step 4.** Now login to your account using `firebase login`

**Step 5.** Now configure your application using `firebase init`, on completing you can find an index.js file

**Step 6.** add necessary code in the index.js file and upload the function using `firebase deploy` 

I have added the cloud function code in firebasefunction folder please have a look.

[In a similar fashion, you can host your admin website in Firebase Hosting section(Iwill be updating same)]

### Note:
`google-services.json` added in this project will connect your app to my firebase account.You need to replace it with yours .

As i said above,Firestore is in beta so there are possibilities of change. I will be updating them.

Methods associated with push notification is only tested in Windows system.


## ScreenShot

Not yet complete....

[Demo Download](https://github.com/appitiza/workmanager/tree/master/app/release/app-release.apk)

## License


Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
