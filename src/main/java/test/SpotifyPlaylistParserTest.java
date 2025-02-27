package test;

import bots.bot.music.SpotifyParser.SpotifyPlaylistParser;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class SpotifyPlaylistParserTest {

    @Test
    public void testParseTrackId() throws IOException {
        // Тестування з валідним посиланням
        String validLink = "https://open.spotify.com/track/4SupI9OXg3hwrymR85hkhL";
        String result = SpotifyPlaylistParser.parseTrackId(validLink);
        assertEquals("4SupI9OXg3hwrymR85hkhL", result);

        // Тестування з невалідним посиланням
        String invalidLink = "https://open.spotify.com/track/";
        result = SpotifyPlaylistParser.parseTrackId(invalidLink);
        assertNull(result);
    }

    @Test
    public void testParsePlaylistId() {
        // Тестування з валідним посиланням
        String validLink = "https://open.spotify.com/playlist/5dSfsTZyyq71Izgn3V390K?si=e5bca6c591b84f74";
        String result = SpotifyPlaylistParser.parsePlaylistId(validLink);
        assertEquals("5dSfsTZyyq71Izgn3V390K", result);

        // Тестування з невалідним посиланням
        String invalidLink = "https://open.spotify.com/playlist/";
        result = SpotifyPlaylistParser.parsePlaylistId(invalidLink);
        assertNull(result);
    }

    @Test
    public void testParseSoloTrack() throws IOException {
        // Тестування з валідним писиланням
        String validLink = "https://open.spotify.com/track/0snQkGI5qnAmohLE7jTsTn?si=3dc91954a9944eac";
        String result = SpotifyPlaylistParser.parseSoloTrack(validLink);
        assertEquals("Toxicity System Of A Down", result);
    }
}
