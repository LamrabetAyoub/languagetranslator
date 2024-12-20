package com.example.languagetranslator;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.languagetranslator.DAO.NoteDao;
import com.example.languagetranslator.database.NoteDatabase;
import com.example.languagetranslator.models.Note;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteViewModel extends AndroidViewModel {
    private NoteDao noteDao;
    private LiveData<List<Note>> allNotes;
    private ExecutorService executorService;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        NoteDatabase database = NoteDatabase.getInstance(application);
        noteDao = database.noteDao();
        allNotes = noteDao.getAllNotes();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Note note) {
        executorService.execute(() -> noteDao.insert(note));
    }

    public void update(Note note) {
        executorService.execute(() -> noteDao.update(note));
    }

    public void delete(Note note) {
        executorService.execute(() -> noteDao.delete(note));
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public Note getNoteById(int id) {
        try {
            return executorService.submit(() -> noteDao.getNoteById(id)).get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}