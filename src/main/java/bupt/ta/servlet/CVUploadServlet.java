package bupt.ta.servlet;

import bupt.ta.cv.ResumeTextExtractor;
import bupt.ta.llm.DeepSeekClient;
import bupt.ta.llm.LlmProfileExtractionService;
import bupt.ta.model.TAProfile;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@MultipartConfig(maxFileSize = 5242880, maxRequestSize = 5242880) // 5MB
public class CVUploadServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(CVUploadServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = (String) req.getSession().getAttribute("userId");
        Part filePart = req.getPart("cvFile");

        if (filePart == null || filePart.getSize() == 0) {
            resp.sendRedirect(req.getContextPath() + "/ta/profile?error=no_file");
            return;
        }

        String fileName = filePart.getSubmittedFileName();
        if (fileName == null || fileName.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/ta/profile?error=no_file");
            return;
        }
        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : "";
        if (!ext.matches("(?i)\\.(pdf|doc|docx|txt)")) {
            resp.sendRedirect(req.getContextPath() + "/ta/profile?error=invalid_type");
            return;
        }

        DataStorage storage = new DataStorage(getServletContext());
        Path uploadDir = storage.getUploadPath();
        Files.createDirectories(uploadDir);

        String safeName = "cv_" + userId + "_" + System.currentTimeMillis() + ext;
        Path targetFile = uploadDir.resolve(safeName);

        try (InputStream is = filePart.getInputStream()) {
            Files.copy(is, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }

        String relativePath = "uploads/" + safeName;
        TAProfile profile = storage.getProfileByUserId(userId);
        if (profile == null) {
            profile = new TAProfile(userId);
        }
        profile.setCvFilePath(relativePath);

        boolean aiFilled = false;
        try {
            String plain = ResumeTextExtractor.extract(targetFile, ext);
            DeepSeekClient ds = new DeepSeekClient();
            if (ds.isConfigured() && plain != null && !plain.trim().isEmpty()) {
                LlmProfileExtractionService extractor = new LlmProfileExtractionService(ds);
                aiFilled = extractor.extractAndMergeProfile(profile, plain);
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "CV text extraction or DeepSeek profile merge failed", ex);
        }

        storage.saveProfile(profile);

        String redirect = req.getContextPath() + "/ta/profile?cv_success=1";
        if (aiFilled) {
            redirect += "&ai_fill=1";
        }
        resp.sendRedirect(redirect);
    }
}
