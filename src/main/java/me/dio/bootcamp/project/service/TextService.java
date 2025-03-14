package me.dio.bootcamp.project.service;

import me.dio.bootcamp.project.entity.Text;
import me.dio.bootcamp.project.repository.TextRepository;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Service
public class TextService {

    private final TextRepository textRepository;

    public TextService(TextRepository textRepository) {
        this.textRepository = textRepository;
    }

    public Text saveText(String originalText, int lines) {
        String summarizedText = summarizeText(originalText, lines);

        Text text = new Text();
        text.setText(originalText);
        text.setTextReduced(summarizedText);

        return textRepository.save(text);
    }
    public List<Text> findByContent(String text) {
        return textRepository.findByTextContainingIgnoreCase(text);
    }

    public List<Text> findAll() {
        return textRepository.findAll();
    }

    public Optional<Text> findById(Long id) {
        return textRepository.findById(id);
    }

    public Text updateText(Long id, String newText, int lines) {
        Optional<Text> optionalText = textRepository.findById(id);

        if (optionalText.isPresent()) {
            Text text = optionalText.get();
            String summarizedText = summarizeText(newText, lines);

            text.setText(newText);
            text.setTextReduced(summarizedText);

            return textRepository.save(text);
        } else {
            throw new RuntimeException("Texto não encontrado com o ID: " + id);
        }
    }

    public void deleteText(Long id) {
        if (textRepository.existsById(id)) {
            textRepository.deleteById(id);
        } else {
            throw new RuntimeException("Texto não encontrado com o ID: " + id);
        }
    }

    public boolean existsByText(String text) {
        return !textRepository.findByTextContainingIgnoreCase(text).isEmpty();
    }


    private String summarizeText(String text, int lines) {
        try (InputStream modelIn = getClass().getResourceAsStream("/models/pt-sent.bin")) {
            if (modelIn == null) {
                throw new IOException("Modelo de detecção de sentenças não encontrado!");
            }

            SentenceModel model = new SentenceModel(modelIn);
            SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);

            String[] sentences = sentenceDetector.sentDetect(text);

            int maxSentences = Math.min(sentences.length, lines);
            StringBuilder summary = new StringBuilder();

            for (int i = 0; i < maxSentences; i++) {
                summary.append(sentences[i]).append(" ");
            }

            return summary.toString().trim();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar modelo OpenNLP", e);
        }
    }

}
