package com.example.languagetranslator.adapters;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.languagetranslator.R;
import com.example.languagetranslator.models.Note;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<Note> notes = new ArrayList<>();
    private OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
        void onEditClick(Note note);
        void onDeleteClick(Note note);
        void onTranslateClick(Note note);
    }

    public void setOnNoteClickListener(OnNoteClickListener listener) {
        this.listener = listener;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note currentNote = notes.get(position);
        holder.titleTextView.setText(currentNote.getTitle());
        holder.contentTextView.setText(currentNote.getContent());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        holder.timestampTextView.setText(sdf.format(currentNote.getTimestamp()));
        holder.languageTextView.setText(currentNote.getLanguage());
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView contentTextView;
        TextView timestampTextView;
        TextView languageTextView;
        ImageButton editButton;
        ImageButton deleteButton;
        ImageButton translateButton;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.note_title);
            contentTextView = itemView.findViewById(R.id.note_content);
            timestampTextView = itemView.findViewById(R.id.note_timestamp);
            languageTextView = itemView.findViewById(R.id.note_language);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
            translateButton = itemView.findViewById(R.id.translate_button);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onNoteClick(notes.get(position));
                }
            });

            editButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(notes.get(position));
                }
            });

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(notes.get(position));
                }
            });

            translateButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onTranslateClick(notes.get(position));
                }
            });
        }
    }
}