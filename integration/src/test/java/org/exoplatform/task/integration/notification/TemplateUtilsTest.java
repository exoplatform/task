package org.exoplatform.task.integration.notification;


import org.junit.Test;
import java.util.Date;
import java.util.TimeZone;
import static org.junit.Assert.*;

public class TemplateUtilsTest {

    @Test
    public void testformat() {
        assertNull(TemplateUtils.format(null, null));
        Date date = new Date();
        TimeZone timeZone = TimeZone.getDefault();
        assertNotNull(TemplateUtils.format(date, timeZone));

    }
}
