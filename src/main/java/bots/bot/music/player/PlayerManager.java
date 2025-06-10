package bots.bot.music.player;


import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.TvHtml5Embedded;
import dev.lavalink.youtube.clients.Web;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.*;


@Slf4j
@Component
public class PlayerManager {

    private static PlayerManager INSTANCE;
    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;

    public PlayerManager(){

        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        YoutubeAudioSourceManager ytms = new YoutubeAudioSourceManager(true, new TvHtml5Embedded());
        String poToken = "MnQsFEzgYOA0hywsyNoDx2aQqcVket0TpfJzJeKXLdQigxYhsiGpcG75oeL7DRljkRsUTXgpd3CjJzwLuraG2096q09bu-bvXqV3a3J-NV-592zaN3iCuhBihPAgNjx4iUbyk8aWtHHWwfBM5Xvm7sF1GKV-LA==";
        String visitorData = "Cgt5NzdmVzNBOEdVWSi0m5u6BjIKCgJERRIEEgAgEQ%3D%3D";
        Web.setPoTokenAndVisitorData(poToken, visitorData);
        ytms.useOauth2("1//09SmKa9w4RONOCgYIARAAGAkSNwF-L9IrwkLyiUUsOcyK7C7l4eDf5TRF1I5bQgvew6AWDx1WkIipqu-MPfOydjTjAOSuCbAHOWA", false);
        audioPlayerManager.registerSourceManager(ytms);
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);

    }

    public GuildMusicManager getMusicManager(Guild guild){
        return this.musicManagers.computeIfAbsent(Long.valueOf(guild.getIdLong()), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }

    public void loadAndPlay(TextChannel textChannel, String trackUrl){
        final GuildMusicManager musicManager = this.getMusicManager(textChannel.getGuild());

        this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                musicManager.scheduler.queue(audioTrack);
                log.info("Loaded track {} by {} in guild {}", audioTrack.getInfo().title, audioTrack.getInfo().author, textChannel.getGuild().getId());
                textChannel.sendMessage("```Adding to queue " + audioTrack.getInfo().title + " by " + audioTrack.getInfo().author + "```").queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                final List<AudioTrack> tracks = audioPlaylist.getTracks();
                if(!tracks.isEmpty()){
                    musicManager.scheduler.queue(tracks.get(0));
                    textChannel.sendMessage("```Adding to queue " + tracks.get(0).getInfo().title + " by " + tracks.get(0).getInfo().author + "```").queue();
                }
            }

            @Override
            public void noMatches() {
                textChannel.sendMessage("```Sorry, I couldn't find anything suitable. Could you please provide more details for your request?```").queue();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                textChannel.sendMessage("```An unexpected error occurred; perhaps this will help clarify the situation: " + e.getMessage() + "```").queue();
            }
        });
    }
    public void loadAndPlaylist(TextChannel textChannel, List<String> trackUrl) {
        GuildMusicManager musicManager = this.getMusicManager(textChannel.getGuild());
        Iterator musicIterator = trackUrl.iterator();

        while(musicIterator.hasNext()) {
            String link = String.valueOf(musicIterator.next());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            this.audioPlayerManager.loadItemOrdered(musicManager, link, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack audioTrack) {
                    musicManager.scheduler.queue(audioTrack);
                    textChannel.sendMessage("```Adding to queue " + audioTrack.getInfo().title + " by " + audioTrack.getInfo().author + "```").queue();
                }

                @Override
                public void playlistLoaded(AudioPlaylist audioPlaylist) {
                    final List<AudioTrack> tracks = audioPlaylist.getTracks();
                    if(!tracks.isEmpty()){
                        musicManager.scheduler.queue(tracks.get(0));
                        textChannel.sendMessage("```Adding to queue " + tracks.get(0).getInfo().title + " by " + tracks.get(0).getInfo().author + "```").queue();
                    }
                }

                @Override
                public void noMatches() {
                    textChannel.sendMessage("Track wasn't found!Sorry...").queue();
                }

                @Override
                public void loadFailed(FriendlyException e) {
                    textChannel.sendMessage("Failed to load " + link + " because of " + e.getMessage()).queue();
                }
            });
        }
    }

    public static PlayerManager getInstance(){
        if(INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

}