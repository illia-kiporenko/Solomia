package bots.bot.config;

import bots.bot.coin.ProfileRepository;
import bots.bot.coin.VoiceListener;
import bots.bot.commands.SlashCommandListener;
import bots.bot.music.JDACommands;
import bots.bot.music.commands.*;
import bots.bot.playlist.PlaylistRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Slf4j
@Configuration
public class BotConfig {

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.refresh}")
    private String refresh;

    private static final GatewayIntent[] INTENTS = {
            GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.MESSAGE_CONTENT
    };

    @Bean
    public JDA jda(SlashCommandListener slashCommandListener, JDACommands jdaCommands, VoiceListener voiceListener) throws InterruptedException {
        JDA jda = JDABuilder.createDefault(botToken)
                .setActivity(Activity.watching("checks"))
                .addEventListeners(slashCommandListener, jdaCommands, voiceListener)
                .enableIntents(Arrays.asList(INTENTS))
                .build();

        jda.awaitReady();
        return jda;
    }


    @Bean
    public JDACommands jdaCommands() {
        log.info("Prefix commands registering");
        JDACommands jdaCommands = new JDACommands("!");
        jdaCommands.registerCommand(new Play());
        jdaCommands.registerCommand(new Stop());
        jdaCommands.registerCommand(new Skip());
        jdaCommands.registerCommand(new Pause());
        jdaCommands.registerCommand(new CurrentTrack());
        jdaCommands.registerCommand(new YouTube());
        log.info("Prefix commands successfully registered");
        return jdaCommands;
    }

    @Bean
    public SlashCommandListener slashCommandListener(ProfileRepository profileRepository, PlaylistRepository playlistRepository) {
        return new SlashCommandListener(profileRepository, playlistRepository, refresh);
    }

    @Bean
    public JDA registerCommands(JDA jda) {
        log.info("Slash commands registering...");
        jda.updateCommands().addCommands(
                Commands.slash("play", "Play the chosen playlist by its name.")
                        .addOption(OptionType.STRING, "title", "Title of the playlist", true),
                Commands.slash("balance", "Check your account balance."),
                Commands.slash("create", "Create your own playlist.")
                        .addOption(OptionType.STRING, "title", "Title of the playlist", true)
                        .addOption(OptionType.STRING, "tracks", "List of tracks separated by triple spaces", true),
                Commands.slash("list", "Show the list of playlists.")
                        .addOption(OptionType.STRING, "title", "Show the track list of a specific playlist.")
                        .addOption(OptionType.INTEGER, "number", "Page number"),
                Commands.slash("add", "Add tracks to a playlist.")
                        .addOption(OptionType.STRING, "title", "Name of the playlist to which the tracks will be added", true)
                        .addOption(OptionType.STRING, "tracks", "Names of the tracks to be added, separated by triple spaces", true),
                Commands.slash("remove", "Remove tracks from a playlist.")
                        .addOption(OptionType.STRING, "title", "Name of the playlist from which the tracks will be removed", true)
                        .addOption(OptionType.INTEGER, "number", "Track number to be removed", true),
                Commands.slash("delete", "Delete a chosen playlist.")
                        .addOption(OptionType.STRING, "title", "Title of the playlist to be deleted"),
                Commands.slash("queue", "Check the current composition queue."),
                Commands.slash("spotify", "Convert a Spotify playlist to a Discord one.")
                        .addOption(OptionType.STRING, "link", "Link to your Spotify playlist to be converted")
                        .addOption(OptionType.STRING, "title", "Name of the playlist to be created or to be expanded")
                        .addOption(OptionType.INTEGER, "number", "Amount of composition to be added")
        ).queue(success -> log.info("Successfully added slash commands!"),
                    failure -> log.error("Failed to add slash commands!", failure));
        return jda;
    }
}
