package com.example.languagetranslator;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.languagetranslator.models.Note;
import com.example.languagetranslator.adapters.NoteAdapter;
import com.google.android.material.button.MaterialButton;

public class AddEditNoteActivity extends AppCompatActivity {
    private EditText titleEditText, contentEditText;
    private AutoCompleteTextView languageSpinner;
    private NoteViewModel noteViewModel;
    private int noteId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_note);

        // Initialize Views
        titleEditText = findViewById(R.id.note_title_input);
        contentEditText = findViewById(R.id.note_content_input);
        languageSpinner = findViewById(R.id.note_language_spinner);
        MaterialButton saveButton = findViewById(R.id.save_note_button);

        // Language Spinner Setup
        String[] languages = {"English", "French", "Spanish", "German", "Arabic", "Chinese", "Russian", "Japanese", "Italian"};
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                languages
        );
        languageSpinner.setAdapter(languageAdapter);

        // ViewModel
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        // Check if editing existing note
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("NOTE_ID")) {
            noteId = extras.getInt("NOTE_ID");
            Note existingNote = noteViewModel.getNoteById(noteId);

            titleEditText.setText(existingNote.getTitle());
            contentEditText.setText(existingNote.getContent());
            languageSpinner.setText(existingNote.getLanguage(), false);
        }

        // Save Button
        saveButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString().trim();
            String content = contentEditText.getText().toString().trim();
            String language = languageSpinner.getText().toString();

            if (title.isEmpty() || content.isEmpty() || language.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Note note = new Note(title, content, language);
            if (noteId != -1) {
                note.setId(noteId);
                noteViewModel.update(note);
            } else {
                noteViewModel.insert(note);
            }

            finish();
        });
    }
}