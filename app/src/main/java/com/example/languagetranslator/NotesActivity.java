package com.example.languagetranslator;



import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.languagetranslator.adapters.NoteAdapter;
import com.example.languagetranslator.models.Note;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NotesActivity extends AppCompatActivity {
    private NoteViewModel noteViewModel;
    private NoteAdapter noteAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_notes);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_translate) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_notes) {
                return true;
            }
            return false;
        });

        // RecyclerView Setup
        recyclerView = findViewById(R.id.notes_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteAdapter = new NoteAdapter();
        recyclerView.setAdapter(noteAdapter);

        // ViewModel Setup
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(this, notes -> {
            noteAdapter.setNotes(notes);

            // Optional: Show/hide empty view
            findViewById(R.id.empty_view).setVisibility(
                    notes.isEmpty() ? View.VISIBLE : View.GONE
            );
        });

        // Note Click Listeners
        noteAdapter.setOnNoteClickListener(new NoteAdapter.OnNoteClickListener() {
            @Override
            public void onNoteClick(Note note) {
                Intent intent = new Intent(NotesActivity.this, AddEditNoteActivity.class);
                intent.putExtra("NOTE_ID", note.getId());
                startActivity(intent);
            }

            @Override
            public void onEditClick(Note note) {
                Intent intent = new Intent(NotesActivity.this, AddEditNoteActivity.class);
                intent.putExtra("NOTE_ID", note.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Note note) {
                noteViewModel.delete(note);
                Toast.makeText(NotesActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTranslateClick(Note note) {
                // Navigate to translation with note content
                Intent intent = new Intent(NotesActivity.this, MainActivity.class);
                intent.putExtra("NOTE_CONTENT", note.getContent());
                startActivity(intent);
            }
        });

        // Add Note FAB
        FloatingActionButton fabAddNote = findViewById(R.id.fab_add_note);
        fabAddNote.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditNoteActivity.class);
            startActivity(intent);
        });
    }
}