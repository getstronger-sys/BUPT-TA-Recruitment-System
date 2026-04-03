package bupt.ta.storage;

import bupt.ta.model.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import javax.servlet.ServletContext;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * JSON file-based data storage. All data stored in simple text (JSON) files.
 * Thread-safe for concurrent access.
 */
public class DataStorage {
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = "users.json";
    private static final String PROFILES_FILE = "profiles.json";
    private static final String JOBS_FILE = "jobs.json";
    private static final String APPLICATIONS_FILE = "applications.json";

    private final Path basePath;
    private final Gson gson;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public DataStorage(ServletContext ctx) {
        this.basePath = resolveServletBasePath(ctx);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        ensureDataDir();
    }

    public DataStorage(String baseDir) {
        this.basePath = Paths.get(baseDir, DATA_DIR);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        ensureDataDir();
    }

    private Path resolveServletBasePath(ServletContext ctx) {
        String override = firstNonBlank(System.getProperty("ta.data.dir"), System.getenv("TA_DATA_DIR"));
        if (override != null) {
            return Paths.get(override).toAbsolutePath().normalize();
        }

        Path projectDataPath = detectProjectDataPath();
        if (projectDataPath != null) {
            return projectDataPath;
        }

        String realPath = ctx != null ? ctx.getRealPath("/") : null;
        return Paths.get(realPath != null ? realPath : ".", DATA_DIR).toAbsolutePath().normalize();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private Path detectProjectDataPath() {
        String userDir = System.getProperty("user.dir");
        if (userDir == null || userDir.trim().isEmpty()) {
            return null;
        }

        Path current = Paths.get(userDir).toAbsolutePath().normalize();
        for (Path path = current; path != null; path = path.getParent()) {
            if (Files.exists(path.resolve("pom.xml")) && Files.exists(path.resolve("src"))) {
                return path.resolve(DATA_DIR);
            }
        }
        return null;
    }

    private void ensureDataDir() {
        try {
            Files.createDirectories(basePath);
            initSampleDataIfEmpty();
        } catch (IOException e) {
            throw new RuntimeException("Cannot create data directory: " + basePath, e);
        }
    }

    private void initSampleDataIfEmpty() throws IOException {
        Path usersPath = basePath.resolve(USERS_FILE);
        if (!Files.exists(usersPath) || Files.size(usersPath) == 0) {
            List<User> defaultUsers = Arrays.asList(
                    createUser("U001", "ta1", "ta123", "TA", "ta1@bupt.edu.cn", "Zhang San"),
                    createUser("U002", "ta2", "ta123", "TA", "ta2@bupt.edu.cn", "Li Si"),
                    createUser("U003", "mo1", "mo123", "MO", "mo1@bupt.edu.cn", "Wang MO"),
                    createUser("U004", "admin", "admin123", "ADMIN", "admin@bupt.edu.cn", "System Admin")
            );
            save(USERS_FILE, defaultUsers);
        }
        Path jobsPath = basePath.resolve(JOBS_FILE);
        if (!Files.exists(jobsPath) || Files.size(jobsPath) == 0) {
            Job j1 = new Job();
            j1.setId("J0001");
            j1.setTitle("TA for Software Engineering");
            j1.setModuleCode("EBU6304");
            j1.setModuleName("Software Engineering");
            j1.setDescription("Support lectures, labs and coursework marking.");
            j1.setRequiredSkills(Arrays.asList("Java", "Software Engineering"));
            j1.setPostedBy("U003");
            j1.setPostedByName("Wang MO");
            j1.setStatus("OPEN");
            j1.setCreatedAt(java.time.LocalDateTime.now().toString());
            j1.setMaxApplicants(2);
            j1.setJobType("MODULE_TA");
            save(JOBS_FILE, Arrays.asList(j1));
        }
    }

    private User createUser(String id, String uname, String pwd, String role, String email, String name) {
        User u = new User(id, uname, pwd, role);
        u.setEmail(email);
        u.setRealName(name);
        return u;
    }

    private <T> void save(String filename, T data) throws IOException {
        lock.writeLock().lock();
        try {
            Path file = basePath.resolve(filename);
            String json = gson.toJson(data);
            Files.write(file, json.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private <T> T load(String filename, Type type) throws IOException {
        lock.readLock().lock();
        try {
            Path file = basePath.resolve(filename);
            if (!Files.exists(file) || Files.size(file) == 0) {
                return null;
            }
            String json = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            return gson.fromJson(json, type);
        } finally {
            lock.readLock().unlock();
        }
    }

    // ---- Users ----
    public User findByUsername(String username) throws IOException {
        List<User> users = loadUsers();
        return users.stream().filter(u -> u.getUsername().equals(username)).findFirst().orElse(null);
    }

    public User findUserById(String id) throws IOException {
        List<User> users = loadUsers();
        return users.stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null);
    }

    @SuppressWarnings("unchecked")
    public List<User> loadUsers() throws IOException {
        List<User> list = load(USERS_FILE, new TypeToken<ArrayList<User>>(){}.getType());
        return list != null ? list : new ArrayList<>();
    }

    public void saveUser(User user) throws IOException {
        List<User> users = loadUsers();
        users.removeIf(u -> u.getId().equals(user.getId()));
        users.add(user);
        save(USERS_FILE, users);
    }

    public User addUser(User user) throws IOException {
        List<User> users = loadUsers();
        String newId = "U" + String.format("%03d", users.size() + 1);
        user.setId(newId);
        users.add(user);
        save(USERS_FILE, users);
        return user;
    }

    // ---- TA Profiles ----
    @SuppressWarnings("unchecked")
    public List<TAProfile> loadProfiles() throws IOException {
        List<TAProfile> list = load(PROFILES_FILE, new TypeToken<ArrayList<TAProfile>>(){}.getType());
        return list != null ? list : new ArrayList<>();
    }

    public TAProfile getProfileByUserId(String userId) throws IOException {
        return loadProfiles().stream().filter(p -> p.getUserId().equals(userId)).findFirst().orElse(null);
    }

    public void saveProfile(TAProfile profile) throws IOException {
        List<TAProfile> profiles = loadProfiles();
        profiles.removeIf(p -> p.getUserId().equals(profile.getUserId()));
        profiles.add(profile);
        save(PROFILES_FILE, profiles);
    }

    // ---- Jobs ----
    @SuppressWarnings("unchecked")
    public List<Job> loadJobs() throws IOException {
        List<Job> list = load(JOBS_FILE, new TypeToken<ArrayList<Job>>(){}.getType());
        return list != null ? list : new ArrayList<>();
    }

    public Job getJobById(String id) throws IOException {
        return loadJobs().stream().filter(j -> j.getId().equals(id)).findFirst().orElse(null);
    }

    public void saveJob(Job job) throws IOException {
        List<Job> jobs = loadJobs();
        jobs.removeIf(j -> j.getId().equals(job.getId()));
        jobs.add(job);
        save(JOBS_FILE, jobs);
    }

    public Job addJob(Job job) throws IOException {
        List<Job> jobs = loadJobs();
        String newId = "J" + String.format("%04d", jobs.size() + 1);
        job.setId(newId);
        job.setCreatedAt(java.time.LocalDateTime.now().toString());
        job.setStatus("OPEN");
        jobs.add(job);
        save(JOBS_FILE, jobs);
        return job;
    }

    // ---- Applications ----
    @SuppressWarnings("unchecked")
    public List<Application> loadApplications() throws IOException {
        List<Application> list = load(APPLICATIONS_FILE, new TypeToken<ArrayList<Application>>(){}.getType());
        return list != null ? list : new ArrayList<>();
    }

    public List<Application> getApplicationsByJobId(String jobId) throws IOException {
        return loadApplications().stream().filter(a -> a.getJobId().equals(jobId)).collect(Collectors.toList());
    }

    public List<Application> getApplicationsByApplicantId(String applicantId) throws IOException {
        return loadApplications().stream().filter(a -> a.getApplicantId().equals(applicantId)).collect(Collectors.toList());
    }

    public boolean hasApplied(String jobId, String applicantId) throws IOException {
        return loadApplications().stream()
                .anyMatch(a -> a.getJobId().equals(jobId) && a.getApplicantId().equals(applicantId));
    }

    public void saveApplication(Application app) throws IOException {
        List<Application> apps = loadApplications();
        apps.removeIf(a -> a.getId().equals(app.getId()));
        apps.add(app);
        save(APPLICATIONS_FILE, apps);
    }

    public Application addApplication(Application app) throws IOException {
        List<Application> apps = loadApplications();
        String newId = "A" + String.format("%05d", apps.size() + 1);
        app.setId(newId);
        app.setAppliedAt(java.time.LocalDateTime.now().toString());
        app.setStatus("PENDING");
        apps.add(app);
        save(APPLICATIONS_FILE, apps);
        return app;
    }

    public Path getBasePath() { return basePath; }
    public Path getUploadPath() { return basePath.resolve("uploads"); }
}
