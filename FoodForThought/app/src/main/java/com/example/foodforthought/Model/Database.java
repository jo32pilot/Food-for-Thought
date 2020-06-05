/**
 * The Database class is used as an abstraction from firestore's methods. This should make
 * maintaining our code much easier as well as making it more readable.
 *
 * @author John Li
 */
package com.example.foodforthought.Model;

import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

/**
 * The DataBase class is not as powerful or as versatile as what firestore has to offer.
 * However, this class should make it easier to interact with the database.
 *
 * Some features of firestore that you may find useful that aren't implemented in this class
 * might include:
 * - Batch writes: Create a transaction to perform multiple writes as one request.
 * - Queried document reads: Query system to get documents based on user specified criteria.
 */
public class Database {
    FirebaseFirestore db;

    /**
     * Initializes object with database instance.
     */
    public Database(){
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Helper method to modularize getting document references.
     * @param collection Collection in the database to get reference from.
     * @param key Document whose reference will be returned.
     * @return the reference to the specified document.
     */
    private DocumentReference getDocRef(String collection, String key){
        return db.collection(collection).document(key);
    }

    /**
     * Writes the passed in data to the database. Use this if you don't want to
     * use the write methods which have default listeners provided.
     * @param collection Collection where the document will be written.
     * @param key Document name for the data passed in.
     * @param doc The data to be written.
     */
    public void write(String collection, String key, Map<String, Object> doc){
        getDocRef(collection, key).set(doc);
    }

    /**
     * Writes the passed in data to the database.
     * @param collection Collection where the document will be written.
     * @param key Document name for the data passed in.
     * @param doc The data to be written.
     * @param onSuccess Listener callback to be called on write success.
     * @param onFailure Listener callback to be called on write failure.
     */
    public void write(String collection,
                          String key,
                          Map<String, Object> doc,
                          OnSuccessListener<Void> onSuccess,
                          OnFailureListener onFailure){

        getDocRef(collection, key)
                .set(doc)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Writes the passed in data to the database. Provides a default failure listener
     * that writes the passed in failure message to the passed in fragment.
     * @param collection Collection where the document will be written.
     * @param key Document name for the data passed in.
     * @param doc The data to be written.
     * @param frag The fragment where the failure message will be written.
     * @param failureMessage The message to be written to the passed in fragment on failure.
     * @param onSuccess Listener callback to be called on write success.
     */
    public void write(String collection,
                          String key,
                          Map<String, Object> doc,
                          Fragment frag,
                          String failureMessage,
                          OnSuccessListener<Void> onSuccess){

        write(collection, key, doc, onSuccess, new DefaultFailListener(failureMessage, frag));
    }

    /**
     * Writes the passed in data to the database. Provides a default success and failure listeners
     * that writes the passed in success or failure message to the passed in fragment.
     * @param collection Collection where the document will be written.
     * @param key Document name for the data passed in.
     * @param doc The data to be written.
     * @param frag The fragment where the failure message will be written.
     * @param successMessage The message to be written to the passed in frament on failure.
     * @param failureMessage The message to be written to the passed in fragment on failure.
     */
    public void write(String collection,
                          String key,
                          Map<String, Object> doc,
                          Fragment frag,
                          String successMessage,
                          String failureMessage){

        write(collection, key, doc, frag, failureMessage,
                new DefaultSuccessListener<Void>(successMessage, frag));
    }

    /**
     * Updates a specified document's fields.
     * @param collection Collection whose document will be updated.
     * @param key Name of document whose data will be updated.
     * @param mapping Mapping of fields and their updated values.
     * @param onSuccess Listener callback to be called on write success.
     * @param onFailure Listener callback to be called on write failure.
     */
    public void update(String collection,
                       String key,
                       Map<String, Object> mapping,
                       OnSuccessListener<Void> onSuccess,
                       OnFailureListener onFailure){
        getDocRef(collection, key)
                .update(mapping)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Updates a specified document's fields. Provides a default failure listener
     * that writes the passed in failure message to the passed in fragment.
     * @param collection Collection whose document will be updated.
     * @param key Name of document whose data will be updated.
     * @param mapping Mapping of fields and their updated values.
     * @param frag The fragment where the failure message will be written.
     * @param failureMessage The message to be written to the passed in fragment on failure.
     * @param onSuccess Listener callback to be called on write success.
     */
    public void update(String collection,
                      String key,
                      Map<String, Object> mapping,
                      Fragment frag,
                      String failureMessage,
                      OnSuccessListener<Void> onSuccess){

        update(collection, key, mapping, onSuccess, new DefaultFailListener(failureMessage, frag));

    }

    /**
     * Updates a specified document's fields. Provides a default success and failure listeners
     * that writes the passed in success or failure message to the passed in fragment.
     * @param collection Collection whose document will be updated.
     * @param key Name of document whose data will be updated.
     * @param mapping Mapping of fields and their updated values.
     * @param frag The fragment where the failure message will be written.
     * @param successMessage The message to be written to the passed in fragment on success.
     * @param failureMessage The message to be written to the passed in fragment on failure.
     */
    public void update(String collection,
                       String key,
                       Map<String, Object> mapping,
                       Fragment frag,
                       String successMessage,
                       String failureMessage){

        update(collection, key, mapping, frag, failureMessage,
                new DefaultSuccessListener<Void>(successMessage, frag));
    }

    /**
     * Deletes a specified document from the database.
     * @param collection Collection whose document will be deleted.
     * @param key Name of document to delete.
     * @param onSuccess Listener callback to call on deletion success.
     * @param onFailure Listener callback to call on deletion failure.
     */
    public void delete(String collection,
                       String key,
                       OnSuccessListener<Void> onSuccess,
                       OnFailureListener onFailure){

        getDocRef(collection, key)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Deletes a specified document from the database. Provides a default success and failure
     * listener that writes the passed in success or failure message to the passed in fragment.
     * @param collection Collection whose document will be deleted.
     * @param key Name of document to delete.
     * @param frag The fragment where the failure message will be written.
     * @param failureMessage The message to be written to the passed in fragment on failure.
     * @param onSuccess Listener callback to be called on deletion success.
     */
    public void delete(String collection,
                       String key,
                       Fragment frag,
                       String failureMessage,
                       OnSuccessListener<Void> onSuccess){

        delete(collection, key, onSuccess, new DefaultFailListener(failureMessage, frag));

    }

    /**
     * Deletes a specified document from the database. Provides a default success and failure
     * listeners that writes the passed in success or failure message to the passed in fragment.
     * @param collection Collection whose document will be deleted.
     * @param key Name of document to delete.
     * @param frag The fragment where the failure message will be written.
     * @param failureMessage The message to be written to the passed in fragment on failure.
     * @param successMessage The message to be written to the passed in fragment on success.
     */
    public void delete(String collection,
                       String key,
                       Fragment frag,
                       String successMessage,
                       String failureMessage){

        delete(collection, key, frag, failureMessage,
                new DefaultSuccessListener<Void>(successMessage, frag));
    }

    /**
     * Reads document from database.
     * @param collection Collection of document to read.
     * @param key Name of document to read.
     * @param onComplete Listener callback to call on read completion.
     */
    public void getDocument(String collection,
                            String key,
                            OnCompleteListener<DocumentSnapshot> onComplete) {
        getDocRef(collection, key).get().addOnCompleteListener(onComplete);
    }

    /**
     * Reads all documents from database.
     * @param collection Collection whose documents will be read.
     * @param onComplete Listener callback to call on read completion.
     */
    public void getDocuments(String collection, OnCompleteListener onComplete) {
        db.collection(collection).get().addOnCompleteListener(onComplete);
    }

    /**
     * Returns reference to database instance.
     * @return the database instance of this object.
     */
    public FirebaseFirestore getDB(){
        return db;
    }
}