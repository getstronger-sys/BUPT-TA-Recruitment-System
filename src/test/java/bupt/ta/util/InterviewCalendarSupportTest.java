package bupt.ta.util;

import bupt.ta.model.Application;
import bupt.ta.model.Job;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class InterviewCalendarSupportTest {

    @Test
    public void parseInterviewTimeAcceptsCommonFormats() {
        LocalDateTime parsed = InterviewCalendarSupport.parseInterviewTime("2026-04-18 14:30");
        assertNotNull(parsed);
        assertEquals(2026, parsed.getYear());
        assertEquals(4, parsed.getMonthValue());
        assertEquals(18, parsed.getDayOfMonth());
        assertEquals(14, parsed.getHour());
        assertEquals(30, parsed.getMinute());
    }

    @Test
    public void buildCalendarFileIncludesInterviewFields() {
        Application app = new Application();
        app.setId("A00002");
        app.setJobId("J0001");
        app.setInterviewTime("2026-04-18 14:30");
        app.setInterviewLocation("EECS Building Room 402");
        app.setInterviewAssessment("Teaching demo");
        app.setPreferredRole("TA-2");

        Job job = new Job();
        job.setTitle("TA for Software Engineering");
        job.setModuleCode("EBU6304");
        job.setModuleName("Software Engineering");

        String calendar = InterviewCalendarSupport.buildCalendarFile(app, job);
        assertTrue(calendar.contains("BEGIN:VCALENDAR"));
        assertTrue(calendar.contains("SUMMARY:Interview - TA for Software Engineering \\(EBU6304\\)".replace("\\", "")));
        assertTrue(calendar.contains("DTSTART:20260418T143000"));
        assertTrue(calendar.contains("LOCATION:EECS Building Room 402"));
        assertTrue(calendar.contains("Assessment: Teaching demo"));
    }
}
