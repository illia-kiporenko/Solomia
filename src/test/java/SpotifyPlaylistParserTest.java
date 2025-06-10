import bots.bot.music.SpotifyParser.SpotifyPlaylistParser;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SpotifyPlaylistParserTest {

    @Test
    public void testParseTrackId() throws IOException {
        // Тестування з валідним посиланням
        String validLink = "https://open.spotify.com/track/6W21LNLz9Sw7sUSNWMSHRu?si=007cc77a41f9477c";
        String result = SpotifyPlaylistParser.parseTrackId(validLink);
        assertEquals("6W21LNLz9Sw7sUSNWMSHRu", result);

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
        String result = SpotifyPlaylistParser.parseSoloTrack(validLink, "AQDLdiScWqw6yLSKk61399NUNjs-kOhFpSAM7byibbcvJGuey-lUlxT-F-JeOEcFvOXEUmthJk7zGD_aPiv7CdCMrberLuZf-p0UbTgFRRcGU7S-7kfmjqXQjmKozzGbX2E");
        assertEquals("Toxicity System Of A Down", result);
    }
}
