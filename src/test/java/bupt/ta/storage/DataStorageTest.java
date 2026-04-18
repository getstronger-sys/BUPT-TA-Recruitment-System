package bupt.ta.storage;

import bupt.ta.model.*;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
    public void testSiteNotifications() throws Exception {
        Path tmp = Files.createTempDirectory("ta-test");
        try {
            DataStorage storage = new DataStorage(tmp.toString());
            SiteNotification n = new SiteNotification();
            n.setRecipientUserId("U001");
            n.setKind("TEST");
            n.setTitle("Hello");
            n.setBody("Body text");
            n.setRead(false);
            storage.addSiteNotification(n);
            assertNotNull(n.getId());
            assertEquals(1, storage.countUnreadSiteNotificationsForUser("U001"));
            assertTrue(storage.markSiteNotificationRead(n.getId(), "U001"));
            assertEquals(0, storage.countUnreadSiteNotificationsForUser("U001"));

            SiteNotification n2 = new SiteNotification();
            n2.setRecipientUserId("U001");
            n2.setKind("TEST");
            n2.setTitle("Second");
            n2.setBody("More");
            n2.setRead(false);
            storage.addSiteNotification(n2);
            assertEquals(1, storage.countUnreadSiteNotificationsForUser("U001"));
            assertEquals(1, storage.markAllSiteNotificationsReadForUser("U001"));
            assertEquals(0, storage.countUnreadSiteNotificationsForUser("U001"));
            assertEquals(0, storage.getSiteNotificationsForUser("U999").size());

            assertNotNull(storage.getSiteNotificationByIdForUser(n2.getId(), "U001"));
            assertNull(storage.getSiteNotificationByIdForUser(n2.getId(), "U999"));
            assertNull(storage.getSiteNotificationByIdForUser("N99999", "U001"));

            SiteNotification u2 = new SiteNotification();
            u2.setRecipientUserId("U002");
            u2.setKind("T");
            u2.setTitle("Other user");
            u2.setBody("x");
            storage.addSiteNotification(u2);

            SiteNotification n3 = new SiteNotification();
            n3.setRecipientUserId("U001");
            n3.setKind("T");
            n3.setTitle("Batch target");
            n3.setBody("z");
            storage.addSiteNotification(n3);
            assertEquals(1, storage.countUnreadSiteNotificationsForUser("U001"));
            assertEquals(1, storage.markSiteNotificationsReadForUser("U001", Arrays.asList(n3.getId())));
            assertEquals(0, storage.countUnreadSiteNotificationsForUser("U001"));
            assertEquals(1, storage.countUnreadSiteNotificationsForUser("U002"));

            assertTrue(storage.markSiteNotificationUnread(n3.getId(), "U001"));
            assertEquals(1, storage.countUnreadSiteNotificationsForUser("U001"));
            assertFalse(storage.markSiteNotificationUnread("N99999", "U001"));

            storage.markSiteNotificationRead(n.getId(), "U001");
            storage.markSiteNotificationRead(n2.getId(), "U001");
            assertEquals(2, storage.markSiteNotificationsUnreadForUser("U001", Arrays.asList(n.getId(), n2.getId())));
            assertEquals(3, storage.countUnreadSiteNotificationsForUser("U001"));
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

    @Test
    public void testInterviewSlotStorage() throws Exception {
        Path tmp = Files.createTempDirectory("ta-test");
        try {
            DataStorage storage = new DataStorage(tmp.toString());
            InterviewSlot slot = new InterviewSlot();
            slot.setJobId("J0001");
            slot.setStartsAt("2026-04-20T14:00");
            slot.setEndsAt("2026-04-20T14:45");
            slot.setLocation("Room 402");
            slot.setCapacity(2);
            slot.setNotes("Bring teaching demo slides.");
            storage.addInterviewSlot(slot);

            assertNotNull(slot.getId());
            assertEquals(1, storage.getInterviewSlotsByJobId("J0001").size());
            assertEquals("Room 402", storage.getInterviewSlotById(slot.getId()).getLocation());

            assertTrue(storage.deleteInterviewSlot(slot.getId()));
            assertNull(storage.getInterviewSlotById(slot.getId()));
        } finally {
            deleteRecursive(tmp);
        }
    }

    @Test
    public void testConcurrentApplicationAddsRemainUniqueAcrossStorageInstances() throws Exception {
        Path tmp = Files.createTempDirectory("ta-test");
        int workers = 20;
        ExecutorService pool = Executors.newFixedThreadPool(workers);
        try {
            DataStorage storage = new DataStorage(tmp.toString());
            CountDownLatch ready = new CountDownLatch(workers);
            CountDownLatch start = new CountDownLatch(1);
            Future<?>[] futures = new Future<?>[workers];

            for (int i = 0; i < workers; i++) {
                final int idx = i;
                futures[i] = pool.submit(() -> {
                    ready.countDown();
                    assertTrue("Workers did not reach the start gate in time", ready.await(5, TimeUnit.SECONDS));
                    assertTrue("Start signal timed out", start.await(5, TimeUnit.SECONDS));

                    DataStorage concurrentStorage = new DataStorage(tmp.toString());
                    Application app = new Application();
                    app.setJobId("J" + String.format("%04d", idx + 1));
                    app.setApplicantId("U" + String.format("%03d", idx + 100));
                    app.setApplicantName("Applicant " + idx);
                    concurrentStorage.addApplication(app);
                    return null;
                });
            }

            assertTrue("Workers were not ready in time", ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            for (Future<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }

            List<Application> apps = storage.loadApplications();
            Set<String> ids = new HashSet<>();
            for (Application app : apps) {
                assertTrue("Duplicate application ID detected: " + app.getId(), ids.add(app.getId()));
            }
            assertEquals(workers, apps.size());
            assertEquals(workers, ids.size());
        } finally {
            pool.shutdownNow();
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
