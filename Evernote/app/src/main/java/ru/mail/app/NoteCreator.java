package ru.mail.app;

import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;

/**
 * Created by vanik on 16.04.14.
 */
public class NoteCreator {
    private final static String XML_DOCTYPE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private final static String ENML_DOCTYPE = "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">";


    public Note makeNote(NoteStore.Client noteStore, String noteTitle, String noteBody, Notebook parentNotebook) {

        String nBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        nBody += "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">";
        nBody += "<en-note>" + noteBody + "</en-note>";

        // Create note object
        Note ourNote = new Note();
        ourNote.setTitle(noteTitle);
        ourNote.setContent(nBody);

        // parentNotebook is optional; if omitted, default notebook is used
        if (parentNotebook != null && parentNotebook.isSetGuid()) {
            ourNote.setNotebookGuid(parentNotebook.getGuid());
        }

        // Attempt to create note in Evernote account
        Note note = null;
        try {
            note = noteStore.createNote("newNote", ourNote);
        } catch (EDAMUserException edue) {
            // Something was wrong with the note data
            // See EDAMErrorCode enumeration for error code explanation
            // http://dev.evernote.com/documentation/reference/Errors.html#Enum_EDAMErrorCode
            System.out.println("EDAMUserException: " + edue);
        } catch (EDAMNotFoundException ednfe) {
            // Parent Notebook GUID doesn't correspond to an actual notebook
            System.out.println("EDAMNotFoundException: Invalid parent notebook GUID");
        } catch (Exception e) {
            // Other unexpected exceptions
            e.printStackTrace();
        }

        // Return created note object
        return note;

    }
}
