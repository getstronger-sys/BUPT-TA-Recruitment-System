package bupt.ta.storage;

import bupt.ta.model.*;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * Unit tests for DataStorage using a temporary directory.
 */
public class DataStorageTest {

    @Test
    public void testUserStorage() throws Exception {
        Path tmp = Files.createTempDirectory("ta-test");
        try {
            DataStorage storage = new DataStorage(tmp.toString());
            User u = new User();
            u.setUsername("testuser");
            u.setPassword("test123");
            u.setRole("TA");
            storage.addUser(u);
            assertNotNull(u.getId());

            User found = storage.findByUsername("testuser");
            assertNotNull(found);
            assertEquals("testuser", found.getUsername());
            assertEquals("TA", found.getRole());
        } finally {
            deleteRecursive(tmp);
        }
    }

    @Test
    public void testJobStorage() throws Exception {
        Path tmp = Files.createTempDirectory("ta-test");
        try {
            DataStorage storage = new DataStorage(tmp.toString());
            storage.addUser(new bupt.ta.model.User("U003", "mo1", "x", "MO"));

            Job j = new Job();
            j.setTitle("Test Job");
            j.setModuleCode("EBU6304");
            j.setPostedBy("U003");
            storage.addJob(j);
            assertNotNull(j.getId());
            assertEquals("OPEN", j.getStatus());

            Job loaded = storage.getJobById(j.getId());
            assertNotNull(loaded);
            assertEquals("Test Job", loaded.getTitle());
        } finally {
            deleteRecursive(tmp);
        }
    }

    @Test
    public void testApplicationStorage() throws Exception {
        Path tmp = Files.createTempDirectory("ta-test");
        try {
            DataStorage storage = new DataStorage(tmp.toString());
            Application a = new Application();
            a.setJobId("J0001");
            a.setApplicantId("U001");
            a.setApplicantName("Test TA");
            storage.addApplication(a);
            assertNotNull(a.getId());
            assertEquals("PENDING", a.getStatus());

            assertTrue(storage.hasApplied("J0001", "U001"));
            assertFalse(storage.hasApplied("J0001", "U002"));

            a.setStatus("WITHDRAWN");
            storage.saveApplication(a);
            assertFalse("WITHDRAWN should allow applying again", storage.hasApplied("J0001", "U001"));

            Application b = new Application();
            b.setJobId("J0002");
            b.setApplicantId("U001");
            b.setApplicantName("Test TA");
            storage.addApplication(b);
            b.setStatus("INTERVIEW");
            storage.saveApplication(b);
            assertTrue("INTERVIEW should block another apply to same job", storage.hasApplied("J0002", "U001"));
        } finally {
            deleteRecursive(tmp);
        }
    }

    @Test
    public void testWithdrawnApplicationDoesNotBlockReapply() throws Exception {
        Path tmp = Files.createTempDirectory("ta-test");
        try {
            DataStorage storage = new DataStorage(tmp.toString());
            Application a = new Application();
            a.setJobId("J0001");
            a.setApplicantId("U001");
            a.setApplicantName("Test TA");
            storage.addApplication(a);

            assertTrue(storage.hasApplied("J0001", "U001"));

            a.setStatus("WITHDRAWN");
            storage.saveApplication(a);

            assertFalse(storage.hasApplied("J0001", "U001"));
        } finally {
            deleteRecursive(tmp);
        }
    }

    @Test
    public void testFindByEmailAndStudentId() throws Exception {
        Path tmp = Files.createTempDirectory("ta-test");
        try {
            DataStorage storage = new DataStorage(tmp.toString());

            User user = new User();
            user.setUsername("applicant");
            user.setPassword("test123");
            user.setRole("TA");
            user.setEmail("applicant@bupt.edu.cn");
            user.setStudentId("20230001");
            storage.addUser(user);

            TAProfile profile = new TAProfile(user.getId());
            profile.setStudentId("20230001");
            profile.setProgramme("Computer Science");
            profile.setYearOfStudy("Year 2");
            profile.setTaExperience("Tutored first-year programming labs.");
            storage.saveProfile(profile);

            assertNotNull(storage.findByEmail("APPLICANT@bupt.edu.cn"));
            assertNull(storage.findByEmail("missing@bupt.edu.cn"));
            assertNotNull(storage.findProfileByStudentId("20230001"));
            assertNotNull(storage.findProfileByStudentId(" 20230001 "));
            assertNull(storage.findProfileByStudentId("20239999"));
            TAProfile saved = storage.getProfileByUserId(user.getId());
            assertEquals("Computer Science", saved.getProgramme());
            assertEquals("Year 2", saved.getYearOfStudy());
            assertEquals("Tutored first-year programming labs.", saved.getTaExperience());
        } finally {
            deleteRecursive(tmp);
        }
    }

    private void deleteRecursive(Path p) throws IOException {
        if (Files.exists(p)) {
            Files.walk(p).sorted((a, b) -> -a.compareTo(b)).forEach(path -> {
                try { Files.delete(path); } catch (IOException ignored) {}
            });
        }
    }
}
