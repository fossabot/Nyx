package com.larryhsiao.nyx.core.youtube;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit-test for the class {@link UrlVideoId}
 */
class UrlVideoIdTest {

    /**
     * Check the output from URL that generated by YouTube app.
     */
    @Test
    void urlFromApp() {
        Assertions.assertEquals(
            "id",
            new UrlVideoId("https://youtu.be/id").value()
        );
    }

    /**
     * Check the output from URL from the website.
     */
    @Test
    void urlFromWebsite() {
        Assertions.assertEquals(
            "id",
            new UrlVideoId("https://www.youtube.com/watch?v=id").value()
        );
    }
}