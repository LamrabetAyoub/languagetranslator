package com.example.languagetranslator.DAO;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.languagetranslator.models.Note;

import java.util.List;

@Dao
public interface NoteDao {
    @Insert
    long insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    LiveData<List<Note>> getAllNotes();

    @Query("SELECT * FROM notes WHERE id = :noteId")
    Note getNoteById(int noteId);
}