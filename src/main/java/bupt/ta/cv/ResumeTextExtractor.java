package bupt.ta.cv;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Extracts plain text from uploaded CV files for LLM processing.
 * Supports .txt, .pdf, .docx. Legacy .doc is not supported here.
 */
public final class ResumeTextExtractor {

    private static final int MAX_CHARS = 12_000;

    private ResumeTextExtractor() {
    }

    public static String extract(Path absoluteFile, String extensionLowercase) throws IOException {
        if (absoluteFile == null || !Files.isRegularFile(absoluteFile)) {
            return "";
        }
        String ext = extensionLowercase != null ? extensionLowercase.toLowerCase() : "";
        String raw;
        switch (ext) {
            case ".txt":
                raw = Files.readString(absoluteFile, StandardCharsets.UTF_8);
                break;
            case ".pdf":
                try (PDDocument doc = PDDocument.load(absoluteFile.toFile())) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    raw = stripper.getText(doc);
                }
                break;
            case ".docx":
                try (FileInputStream fis = new FileInputStream(absoluteFile.toFile());
                     XWPFDocument doc = new XWPFDocument(fis)) {
                    StringBuilder sb = new StringBuilder();
                    for (XWPFParagraph p : doc.getParagraphs()) {
                        sb.append(p.getText()).append('\n');
                    }
                    raw = sb.toString();
                }
                break;
            default:
                return "";
        }
        return limit(raw);
    }

    private static String limit(String s) {
        if (s == null) {
            return "";
        }
        String t = s.replace('\0', ' ').trim();
        if (t.length() <= MAX_CHARS) {
            return t;
        }
        return t.substring(0, MAX_CHARS);
    }
}
