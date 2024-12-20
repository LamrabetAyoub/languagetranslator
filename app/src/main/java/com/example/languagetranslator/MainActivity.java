package com.example.languagetranslator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TranslationApp";

    private TextInputEditText inputText;
    private AutoCompleteTextView sourceLanguageSpinner, targetLanguageSpinner;
    private MaterialButton translateButton;
    private ImageButton swapLanguagesButton, microphoneButton, cameraButton;
    private TextView resultTextView;
    private ProgressBar progressBar;

    private OkHttpClient client = new OkHttpClient();
    private TextRecognizer textRecognizer;

    // Permission Launchers
    private final ActivityResultLauncher<String> speechPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startSpeechRecognition();
                } else {
                    Toast.makeText(this, "Speech recognition permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    // Speech Recognition Launcher
    private final ActivityResultLauncher<Intent> speechRecognitionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> speechResults = result.getData()
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    if (speechResults != null && !speechResults.isEmpty()) {
                        inputText.setText(speechResults.get(0));
                    }
                }
            });

    // Camera Launcher
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    if (photo != null) {
                        recognizeTextFromImage(photo);
                    }
                }
            });

    // Language map remains the same as in previous implementation
    private static final Map<String, String> LANGUAGES = new HashMap<String, String>() {{
        put("en", "English");
        put("fr", "French");
        put("es", "Spanish");
        put("de", "German");
        put("ar", "Arabic");
        put("zh", "Chinese");
        put("ru", "Russian");
        put("ja", "Japanese");
        put("it", "Italian");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        initializeComponents();

        // Set up text recognizer
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        // Set up button listeners
        setupButtonListeners();
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("NOTE_CONTENT")) {
            String noteContent = extras.getString("NOTE_CONTENT");
            inputText.setText(noteContent);
        }

// Add Bottom Navigation setup
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_translate);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_notes) {
                startActivity(new Intent(this, NotesActivity.class));
                return true;
            } else if (id == R.id.nav_translate) {
                return true;
            }
            return false;
        });
    }

    private void initializeComponents() {
        inputText = findViewById(R.id.inputText);
        sourceLanguageSpinner = findViewById(R.id.sourceLanguageSpinner);
        targetLanguageSpinner = findViewById(R.id.targetLanguageSpinner);
        translateButton = findViewById(R.id.translateButton);
        swapLanguagesButton = findViewById(R.id.swapLanguagesButton);
        resultTextView = findViewById(R.id.resultTextView);
        progressBar = findViewById(R.id.progressBar);
        microphoneButton = findViewById(R.id.microphoneButton);
        cameraButton = findViewById(R.id.cameraButton);

        // Populate language spinners
        setupLanguageSpinners();
    }

    private void setupLanguageSpinners() {
        String[] languageNames = LANGUAGES.values().toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                languageNames
        );

        sourceLanguageSpinner.setAdapter(adapter);
        targetLanguageSpinner.setAdapter(adapter);

        // Set default selections
        sourceLanguageSpinner.setText(LANGUAGES.get("en"), false);
        targetLanguageSpinner.setText(LANGUAGES.get("fr"), false);
    }

    private void setupButtonListeners() {
        // Microphone button
        microphoneButton.setOnClickListener(v -> checkSpeechPermission());

        // Camera button
        cameraButton.setOnClickListener(v -> checkCameraPermission());

        // Translate button
        translateButton.setOnClickListener(v -> performTranslation());

        // Swap languages button
        swapLanguagesButton.setOnClickListener(v -> swapLanguages());
    }

    private void checkSpeechPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            speechPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        } else {
            startSpeechRecognition();
        }
    }

    private void startSpeechRecognition() {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to translate");

            speechRecognitionLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Speech recognition error", e);
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        try {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(cameraIntent);
        } catch (Exception e) {
            Log.e(TAG, "Camera error", e);
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void recognizeTextFromImage(Bitmap bitmap) {
        try {
            InputImage image = InputImage.fromBitmap(bitmap, 0);

            progressBar.setVisibility(View.VISIBLE);
            resultTextView.setText("Recognizing text...");

            textRecognizer.process(image)
                    .addOnSuccessListener(this::handleTextRecognitionSuccess)
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Text recognition failed", e);
                        progressBar.setVisibility(View.GONE);
                        resultTextView.setText("Text recognition failed");
                        Toast.makeText(this, "Could not recognize text", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Text recognition error", e);
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleTextRecognitionSuccess(Text result) {
        progressBar.setVisibility(View.GONE);

        if (result.getTextBlocks().isEmpty()) {
            resultTextView.setText("No text found");
            return;
        }

        // Combine all text blocks
        StringBuilder recognizedText = new StringBuilder();
        for (Text.TextBlock block : result.getTextBlocks()) {
            recognizedText.append(block.getText()).append(" ");
        }

        inputText.setText(recognizedText.toString().trim());
    }

    // Existing methods like performTranslation(), swapLanguages() remain the same
    private void swapLanguages() {
        String sourceLanguage = sourceLanguageSpinner.getText().toString();
        String targetLanguage = targetLanguageSpinner.getText().toString();

        sourceLanguageSpinner.setText(targetLanguage, false);
        targetLanguageSpinner.setText(sourceLanguage, false);
    }
    private String getLanguageCode(String languageName) {
        for (Map.Entry<String, String> entry : LANGUAGES.entrySet()) {
            if (entry.getValue().equals(languageName)) {
                return entry.getKey();
            }
        }
        return "en"; // Default to English
    }


    private void performTranslation() {
        String textToTranslate = inputText.getText().toString().trim();
        String sourceLang = getLanguageCode(sourceLanguageSpinner.getText().toString());
        String targetLang = getLanguageCode(targetLanguageSpinner.getText().toString());

        if (textToTranslate.isEmpty()) {
            Toast.makeText(this, "Please enter text to translate", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button and show progress
        translateButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        resultTextView.setText("Translating...");

        // Encode text
        String encodedText;
        try {
            encodedText= URLEncoder.encode(textToTranslate, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            translateButton.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            resultTextView.setText("Error encoding text");
            Toast.makeText(this, "Error encoding text", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call Translation API
        String url = "https://api.mymemory.translated.net/get?q=" + encodedText +
                "&langpair=" + sourceLang + "|" + targetLang;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    translateButton.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    resultTextView.setText("Translation failed: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    translateButton.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                });

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        resultTextView.setText("Failed to fetch translation");
                        Toast.makeText(MainActivity.this, "Translation service error", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                String responseBody = response.body().string();
                try {
                    JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                    String translatedText = jsonResponse
                            .getAsJsonObject("responseData")
                            .get("translatedText")
                            .getAsString();

                    runOnUiThread(() -> resultTextView.setText(translatedText));
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        resultTextView.setText("Error parsing translation");
                        Toast.makeText(MainActivity.this, "Parsing error", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}