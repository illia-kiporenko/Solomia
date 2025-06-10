package bots.bot.music.commands;

import bots.bot.music.ICommand;
import bots.bot.music.SpotifyParser.SpotifyPlaylistParser;
import bots.bot.music.autoleave.AdvancedHashMap;
import bots.bot.music.autoleave.AutoLeave;
import bots.bot.music.autoleave.LeftTimerTask;
import bots.bot.music.player.PlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.*;
import org.json.JSONException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Play implements ICommand {
    public static AdvancedHashMap<Long, Timer, Long> leaveList = new AdvancedHashMap<>();
    private static final long IDLE_TIME_THRESHOLD = 5 * 60 * 1000; // 5 minutes in milliseconds


    @Override
    public void execute(MessageReceivedEvent event) throws InterruptedException, IOException, JSONException {

        if(!event.getMember().getVoiceState().inAudioChannel()){
            event.getChannel().asTextChannel().sendMessage("```You need to be in a voice channel for this command to work.```").queue();
            return;
        }
        if(!event.getMember().getGuild().getSelfMember().getVoiceState().inAudioChannel()){
            final AudioManager audioManager = event.getGuild().getAudioManager();
            final VoiceChannel memberChannel = (VoiceChannel) event.getMember().getVoiceState().getChannel();

            audioManager.openAudioConnection(memberChannel);
        }

        String[] lines = event.getMessage().getContentRaw().split("\n");
        String[] songs = new String[lines.length];

        for (int i = 0; i < lines.length; i++) {
            if(lines[i].length()<6){
                return;
            }
            songs[i] = lines[i].substring(6); // Remove "!play " prefix
        }
        for(String link: songs) {
            link = prepareLink(link);

            System.out.println(event.getMember().getUser().getName() + " " + event.getGuild().getName());

            PlayerManager.getInstance().loadAndPlay(event.getChannel().asTextChannel(), link);


            Thread.sleep(1000);
            setTimer(event.getGuild(),1);

        }
    }

    private String prepareLink(String link) throws IOException {
        if(link.contains("youtu")){
            String regex = "(?:https?://)?(?:www\\.|m\\.)?(?:youtube\\.com/watch\\?v=|youtu\\.be/)([a-zA-Z0-9_-]{11})";

            Pattern pattern = Pattern.compile(regex);

            Matcher matcher = pattern.matcher(link);

            if (matcher.find()) {
                String videoId = matcher.group(1);
                return "https://www.youtube.com/watch?v=" + videoId;
            } else {
                return null;
            }
        }else if(link.contains("open.spotify")){
            return "ytsearch: " + SpotifyPlaylistParser.parseSoloTrack(link, "AQDLdiScWqw6yLSKk61399NUNjs-kOhFpSAM7byibbcvJGuey-lUlxT-F-JeOEcFvOXEUmthJk7zGD_aPiv7CdCMrberLuZf-p0UbTgFRRcGU7S-7kfmjqXQjmKozzGbX2E") + " audio";
        }else{
            if (!isUrl(link)) {
                return  "ytsearch:" + String.join(" ", link) + " audio";
            }else{
                return link;
            }
        }
    }

    private void setTimer(Guild guild, int quantity) {
        if(!leaveList.containsKey(guild.getIdLong())){
            Timer timer = new Timer();
            timer.schedule(new LeftTimerTask(guild), IDLE_TIME_THRESHOLD*quantity);
            long executeTime = System.currentTimeMillis() + IDLE_TIME_THRESHOLD*quantity;
            leaveList.put(guild.getIdLong(), timer, executeTime);
            log.info("Going to leave from {} at {}", guild.getName(), new Date(executeTime));
        }else{
            leaveList.get(guild.getIdLong()).getValue1().cancel();
            Timer timer = new Timer();
            long newDelay = leaveList.get(guild.getIdLong()).getValue2()-System.currentTimeMillis()+IDLE_TIME_THRESHOLD*quantity;
            log.info("New delay time is - {} sec", newDelay / 1000.0);
            timer.schedule(new LeftTimerTask(guild), newDelay);
            leaveList.updateValues(guild.getIdLong(), timer, newDelay+System.currentTimeMillis());
            log.info("Previous timer was canceled! Starting new one:");
            log.info("Going to leave from {} at {}", guild.getName(), new Date(newDelay + System.currentTimeMillis()));
        }
    }

    public void executePlaylist(SlashCommandInteractionEvent event, List<String> songs, String songName) throws InterruptedException, IOException, JSONException {

        if(!event.getMember().getVoiceState().inAudioChannel()){
            event.getChannel().asTextChannel().sendMessage("```You need to be in a voice channel for this command to work.```").queue();
            return;
        }
        if(!event.getMember().getGuild().getSelfMember().getVoiceState().inAudioChannel()){
            final AudioManager audioManager = event.getGuild().getAudioManager();
            final VoiceChannel memberChannel = event.getMember().getVoiceState().getChannel().asVoiceChannel();

            audioManager.openAudioConnection(memberChannel);
        }
        List<String> urlSong = new ArrayList<>();
        for(String link: songs) {
            if (!isUrl(link)) {
                urlSong.add("ytsearch:" + String.join(" ", link) + " audio");
            } else {
                if(link.contains("open.spotify")){
                    link = "ytsearch: " + SpotifyPlaylistParser.parseSoloTrack(link, "AQDLdiScWqw6yLSKk61399NUNjs-kOhFpSAM7byibbcvJGuey-lUlxT-F-JeOEcFvOXEUmthJk7zGD_aPiv7CdCMrberLuZf-p0UbTgFRRcGU7S-7kfmjqXQjmKozzGbX2E") + " audio";
                }
                urlSong.add(link);
            }
        }
        for(String song: urlSong){
            System.out.println(song);
        }
        System.out.println(event.getMember().getUser().getName() + " " + event.getGuild().getName());

        PlayerManager.getInstance().loadAndPlaylist(event.getChannel().asTextChannel(), urlSong);


        setTimer(event.getGuild(), urlSong.size());

        event.reply("```You are listening to "+ songName + "```").setEphemeral(true).queue();
    }

    private boolean isUrl(String url){
        try{
            new URI(url);
            return true;
        } catch (URISyntaxException e){
            return false;
        }
    }

    @Override
    public String getName() {
        return "play";
    }
}