const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendNotification = functions.firestore
    .document('notification/{userId}')
    .onCreate((snap, context) => {
        const newValue = snap.data();
        const to = newValue.to;
        // console.log(to)
        var db = admin.firestore();

        if (to === 'all') {
            //send push to topic 'notification'
            // The topic name can be optionally prefixed with "/topics/".
            var topic_name = 'notification';

            // See documentation on defining a message payload.

            const text = newValue.message;
            const payload_all = {
                notification: {
                    title: newValue.title,
                    body: text ?
                        text.length <= 100 ? text : text.substring(0, 97) + "..." : ""
                },
                topic: topic_name
            };

            // Send a message to devices subscribed to the provided topic.
            return admin.messaging().send(payload_all);
        } else {
            var dbRef = db.collection('users').doc(to);
            var query = dbRef.get()
                .then(doc => {
                    if (doc.exists) {
                        console.log('Token=>', doc.data().token);

                        const text = newValue.message;
                        const payload_user = {
                            notification: {
                                title: newValue.title,
                                body: text ?
                                    text.length <= 100 ? text : text.substring(0, 97) + "..." : ""
                            }
                        };
                        return admin.messaging().sendToDevice(doc.data().token, payload_user);

                    } else {
                        throw new Error("Profile doesn't exist")
                    }
                })
                .catch(err => {
                    console.log('Error getting documents', err);
                    return "Error getting documents"
                });
        }
        return snap.data;

    });